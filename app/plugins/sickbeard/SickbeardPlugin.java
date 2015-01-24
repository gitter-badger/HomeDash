package plugins.sickbeard;

import interfaces.PlugIn;

import java.io.File;
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
	private static final String SHOWS = "/?cmd=shows";
	private static final String POSTER = "/?cmd=show.getposter&tvdbid=[showId]";
	private Gson gson = new Gson();
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	private final String PNG_PATH = "cache/plugins/sickbeard/images/";
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
				Iterator<?> keys = json.keys();

				while (keys.hasNext()) {
					String key = (String) keys.next();
					if (json.get(key) instanceof JSONObject) {
						JSONObject show = (JSONObject) json.get(key);

						if (!show.getString("next_ep_airdate").trim().equalsIgnoreCase("")) {
							try {
								TvShowObject showObject = new TvShowObject();
								showObject.name = show.getString("show_name");
								showObject.nextShowing = df.parse(show.getString("next_ep_airdate"));
								showObject.nextShowingReadable = show.getString("next_ep_airdate");
								showObject.showId = show.getLong("tvdbid");
								// downloading poster
								if (show.getJSONObject("cache").getInt("poster") == 1) {
									try {
										File f = new File(FULL_PNG_PATH + showObject.showId);
										if (!f.exists()) {
											String poster = url + "/" + apiKey + POSTER.replace("[showId]", Long.toString(showObject.showId));
											System.out.println(poster);
											FileUtils.copyURLToFile(new java.net.URL(poster), f);
										}
										showObject.poster = PNG_PATH + showObject.showId;
									} catch (Exception e) {
										Logger.info("Couldn't get poster for show [{}]", showObject.showId);
									}
								}
								comingShows.add(showObject);

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}

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
		if(!f.exists()){
			f.mkdirs();
		}
		f.deleteOnExit();
		
		
	}

	@Override
	public int getRefreshRate() {
		return ONE_MINUTE;
	}

	private class TvShowObject implements Comparable<TvShowObject> {
		public String name, nextShowingReadable, poster;
		public Date nextShowing;
		public long showId;

		@Override
		public int compareTo(TvShowObject o) {
			return nextShowing.compareTo(o.nextShowing);
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
}
