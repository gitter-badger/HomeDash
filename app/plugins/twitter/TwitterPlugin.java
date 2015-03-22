package plugins.twitter;

import interfaces.PlugIn;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import models.Module;
import play.Logger;
import play.twirl.api.Html;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import plugins.twitter.views.html.*;
import websocket.WebSocketMessage;

import com.google.gson.Gson;

public class TwitterPlugin implements PlugIn {
	private Map<String, String> twitterKeys;

	private final static String CONSUMER_SECRET = "consumerSecret", CONSUMER_KEY = "consumerKey", ACCESS_TOKEN = "accessToken", ACCESS_TOKEN_SECRET = "accessTokenSecret";

	private List<Tweet> tweets = new ArrayList<Tweet>();

	private final static String METHOD_SHOW_USER = "showUser", METHOD_SHOW_HASHTAG = "showHashtag", METHOD_NEW_TWEET = "newTweet";

	private Twitter twitter;
	private boolean refreshingData = false;

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return "twitter";
	}

	@Override
	public boolean hasBigScreen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Twitter";
	}

	@Override
	public String getDescription() {
		return "Browse your latest tweets.";
	}

	@Override
	public Object smallScreenRefresh(Map<String, String> settings) {
		return tweets;
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings, long count) {
		return null;
	}

	@Override
	public WebSocketMessage processCommand(String method, String command, Object extraPackage) {
		WebSocketMessage wsMessage = new WebSocketMessage();
		wsMessage.setMethod(method);

		if (method.equalsIgnoreCase(METHOD_SHOW_HASHTAG)) {
			try {
				wsMessage.setMessage(searchHashTag(command));
			} catch (TwitterException e) {
				wsMessage.setMethod(WebSocketMessage.METHOD_ERROR);
				wsMessage.setMessage("Can't get #" + command + " tweets");
			}
		} else if (method.equalsIgnoreCase(METHOD_SHOW_USER)) {
			try {
				wsMessage.setMessage(searchUser(command));
			} catch (TwitterException e) {
				wsMessage.setMethod(WebSocketMessage.METHOD_ERROR);
				wsMessage.setMessage("Can't get @" + command + " timeline");
			}
		} else if (method.equalsIgnoreCase(METHOD_NEW_TWEET)) {
			try {
				newTweet(command);

				wsMessage.setMessage("Tweet sent !");
				wsMessage.setExtra(tweets);
				wsMessage.setMethod(WebSocketMessage.METHOD_SUCCESS);
			} catch (TwitterException e) {
				wsMessage.setMethod(WebSocketMessage.METHOD_ERROR);
				wsMessage.setMessage("Can't send tweet");
			}
		}
		return wsMessage;
	}

	@Override
	public Html getSmallView(Module module) {
		return small.render(module);
	}

	@Override
	public Html getSettingsView(Module module) {
		return settings.render(module);
	}

	@Override
	public Html getBigView(Module module) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public String getExternalLink() {
		return "http://www.twitter.com";
	}

	@Override
	public void init(Map<String, String> settings, String data) {
		this.twitterKeys = settings;
		if (data != null) {
			this.tweets = new Gson().fromJson(data, tweets.getClass());
		}
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(settings.get(CONSUMER_KEY)).setOAuthConsumerSecret(settings.get(CONSUMER_SECRET)).setOAuthAccessToken(settings.get(ACCESS_TOKEN))
				.setOAuthAccessTokenSecret(settings.get(ACCESS_TOKEN_SECRET));
		TwitterFactory tf = new TwitterFactory(cb.build());
		this.twitter = tf.getInstance();
	}

	@Override
	public Object saveData() {
		return tweets;
	}

	@Override
	public boolean hasCss() {
		return true;
	}

	@Override
	public void doInBackground(Map<String, String> settings) {
		refreshTimeline();

	}

	@Override
	public int getRefreshRate() {
		// TODO Auto-generated method stub
		return ONE_MINUTE * 5;
	}

	@Override
	public int getBackgroundRefreshRate() {
		return ONE_MINUTE * 5;
	}

	@Override
	public int getBigScreenRefreshRate() {
		return NO_REFRESH;
	}

	@Override
	public int getWidth() {
		return 3;
	}

	@Override
	public int getHeight() {
		return 4;
	}

	@Override
	public Map<String, String> exposeSettings(Map<String, String> settings) {
		this.twitterKeys = settings;

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(settings.get(CONSUMER_KEY)).setOAuthConsumerSecret(settings.get(CONSUMER_SECRET)).setOAuthAccessToken(settings.get(ACCESS_TOKEN))
				.setOAuthAccessTokenSecret(settings.get(ACCESS_TOKEN_SECRET));
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();

		Map<String, String> result = new Hashtable<>();
		try {
			result.put("Account", "@"+twitter.getScreenName());
		} catch (Exception e) {
			Logger.error("Can't expose settings for twitter module", e);
		}
		return result;
	}

	// //////
	// / Twitter Methods
	// //

	private void refreshTimeline() {

		List<Status> statuses;
		tweets = new ArrayList<Tweet>();
		try {
			statuses = twitter.getHomeTimeline();
			Logger.info("Twitter home timeline size:[{}]", statuses.size());
			for (Status status : statuses) {
				tweets.add(fromStatusToTweet(status));
			}
		} catch (TwitterException e) {
			Logger.error("Can't get Timeline", e);
		}
	}

	private List<Tweet> searchUser(String command) throws TwitterException {
		Logger.info("Searching tweets for user @{}", command);

		List<Tweet> tweets = new ArrayList<>();
		for (Status status : twitter.getUserTimeline(command)) {
			tweets.add(fromStatusToTweet(status));
		}
		return tweets;
	}

	private List<Tweet> searchHashTag(String command) throws TwitterException {
		Query query = new Query(command);
		QueryResult result = twitter.search(query);
		query.count(100);
		List<Tweet> tweets = new ArrayList<>();
		for (Status status : result.getTweets()) {
			tweets.add(fromStatusToTweet(status));
		}
		return tweets;
	}

	private Tweet fromStatusToTweet(Status status) {
		Tweet tweet = new Tweet();
		tweet.username = status.getUser().getName();
		tweet.content = status.getText();
		tweet.isRetweet = status.isRetweet();
		tweet.userPicture = status.getUser().getProfileImageURL();
		tweet.id = status.getId();
		tweet.date = status.getCreatedAt();
		tweet.userId = status.getUser().getScreenName();
		return tweet;
	}

	private void newTweet(String command) throws TwitterException {
		twitter.updateStatus(command);
		refreshTimeline();
	}

	// //INNER CLASSES
	private class Tweet {
		public String userId;
		public Date date;
		public long id;
		public String userPicture;
		public String username, content;
		public boolean isRetweet;
	}
}
