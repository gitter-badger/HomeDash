package plugins.twitter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import models.Module;
import play.Logger;
import play.twirl.api.Html;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import views.html.plugins.twitter.settings;
import views.html.plugins.twitter.small;
import websocket.WebSocketMessage;
import interfaces.PlugIn;

public class TwitterPlugin implements PlugIn {
	private Map<String, String> twitterKeys;

	private final static String CONSUMER_SECRET = "consumerSecret", CONSUMER_KEY = "consumerKey", ACCESS_TOKEN = "accessToken", ACCESS_TOKEN_SECRET = "accessTokenSecret";

	private List<Tweet> tweets = new ArrayList<Tweet>();

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
	public Object smallScreenRefresh(Map<String, String> settings) {
		return tweets;
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings, long count) {
		return null;
	}

	@Override
	public WebSocketMessage processCommand(String method, String command) {
		// TODO Auto-generated method stub
		return null;
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
				Tweet tweet = new Tweet();
				tweet.username = status.getUser().getName();
				tweet.content = status.getText();
				tweet.isRetweet = status.isRetweet();
				tweet.userPicture = status.getUser().getProfileImageURL();
				tweet.id = status.getId();
				tweet.date = status.getCreatedAt();
				tweets.add(tweet);
			}

		} catch (TwitterException e) {
			Logger.error("Can't get Timeline", e);
		}
	}

	// //INNER CLASSES
	private class Tweet {
		public Date date;
		public long id;
		public String userPicture;
		public String username, content;
		public boolean isRetweet;
	}
}
