package plugins.sickbeard;

import interfaces.PlugIn;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import misc.HttpTools;
import models.Module;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.twirl.api.Html;
import plugins.sickbeard.views.html.*;
import websocket.WebSocketMessage;

import com.google.gson.Gson;

public class SickbeardPlugin implements PlugIn {
	public String url, apiKey;

	public static final String URL = "url", API_KEY = "apiKey";
	private static final String SHOWS = "/?cmd=future";
	private static final String POSTER = "http://thetvdb.com/banners/posters/[showId]-[count].jpg";
	private Gson gson = new Gson();
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	private final String PNG_PATH = "cache/plugins/" + getId() + "/images/";
	private final String FULL_PNG_PATH = Play.application().path().getPath() + "/" + PNG_PATH;

	private String baseUrl;

	@Override
	public boolean hasBigScreen() {
		return false;
	}

	@Override
	public boolean hasCss() {
		return true;
	}

	@Override
	public String getName() {
		return "Sickbeard";
	}

	@Override
	public String getDescription() {
		return "See the upcoming episodes list from your Sickbeard instance.";
	}

	@Override
	public Object smallScreenRefresh(Map<String, String> settings) {
		try {

			try {
				List<TvShowObject> comingShows = new ArrayList<SickbeardPlugin.TvShowObject>();

				String getUrl = url + "/" + apiKey + SHOWS;
				String result;
				result = HttpTools.sendGet(getUrl);

				JSONObject json = new JSONObject(result);

				json = json.getJSONObject("data");

				comingShows.addAll(parseTvShows(json.getJSONArray("today")));
				comingShows.addAll(parseTvShows(json.getJSONArray("soon")));
				comingShows.addAll(parseTvShows(json.getJSONArray("later")));

				Collections.sort(comingShows);

				return comingShows;
			} catch (Exception e1) {
				return false;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
	}

	private List<TvShowObject> parseTvShows(JSONArray array) {
		List<TvShowObject> comingShows = new ArrayList<TvShowObject>();

		for (int i = 0; i < array.length(); i++) {
			try {
				TvShowObject show = json2TvShow(array.getJSONObject(i));
				if(!comingShows.contains(show)){
					comingShows.add(show);
				}
			} catch (Exception e) {
				Logger.error("Can't read tv show", e);
			}
		}

		return comingShows;

	}

	private TvShowObject json2TvShow(JSONObject show) throws JSONException, ParseException {
		TvShowObject showObject = new TvShowObject();
		showObject.name = show.getString("show_name");
		showObject.nextShowing = df.parse(show.getString("airdate"));
		showObject.nextShowingReadable = show.getString("airdate");
		showObject.season = show.getInt("season");
		showObject.episode = show.getInt("episode");
		showObject.plot = show.getString("ep_plot");
		showObject.episodeName = show.getString("ep_name");
		showObject.showId = show.getLong("tvdbid");
		File f = new File(FULL_PNG_PATH + showObject.showId + ".jpg");
		if (!f.exists()) {
			for (int i = 1; i <= 10; i++) {
				try {

					String poster = POSTER.replace("[showId]", Long.toString(showObject.showId)).replace("[count]", Integer.toString(i));
					// System.out.println(poster);
					FileUtils.copyURLToFile(new java.net.URL(poster), f);
					break;
				} catch (Exception e) {
					Logger.info("Couldn't get poster for show [{}]", showObject.showId);
				}
			}
		}
		showObject.poster = PNG_PATH + showObject.showId + ".jpg";

		return showObject;
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings, long count) {
		return null;
	}

	@Override
	public WebSocketMessage processCommand(String method, String command) {

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
	public void init(Map<String, String> settings, String data) {
		Logger.info("Initiating Sickbeard plugin.");
		url = settings.get(URL);
		if (!url.endsWith("/")) {
			url += "/";
		}

		if (!url.startsWith("http")) {
			url = "http://" + url;
		}

		baseUrl = url;

		url += "api";
		apiKey = settings.get(API_KEY);

		File f = new File(FULL_PNG_PATH);
		if (!f.exists()) {
			f.mkdirs();
		}
		f.deleteOnExit();

	}

	@Override
	public int getRefreshRate() {
		return ONE_HOUR;
	}

	private class TvShowObject implements Comparable<TvShowObject> {
		public String name, nextShowingReadable, poster, plot, episodeName;
		public int episode, season;
		public Date nextShowing;
		public long showId;

		@Override
		public int compareTo(TvShowObject o) {
			return nextShowing.compareTo(o.nextShowing);
		}

		@Override
		public boolean equals(Object obj) {
			try {
				TvShowObject o = (TvShowObject) obj;
				return o.showId == this.showId;
			} catch (Exception e) {
				return false;
			}
		}
	}

	@Override
	public String getExternalLink() {
		return baseUrl;
	}

	@Override
	public String getId() {
		return "sickbeard";
	}

	@Override
	public Object saveData() {
		return null;
	}

	@Override
	public void doInBackground(Map<String, String> settings) {
	}

	@Override
	public int getBackgroundRefreshRate() {
		return NO_REFRESH;
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
		return 3;
	}
}
