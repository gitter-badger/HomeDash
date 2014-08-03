package plugins.sickbeard;

import interfaces.PlugIn;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import misc.HttpTools;
import models.Module;

import org.json.JSONObject;

import play.Logger;
import play.twirl.api.Html;
import views.html.plugins.sickbeard.settings;
import views.html.plugins.sickbeard.small;
import websocket.WebSocketMessage;

import com.google.gson.Gson;

public class SickbeardPlugin implements PlugIn {
	public String url, apiKey;

	public static final String URL = "url", API_KEY = "apiKey";
	private static final String SHOWS = "/?cmd=shows";
	private Gson gson = new Gson();
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	private String baseUrl;

	@Override
	public boolean hasBigScreen() {
		return false;
	}

	@Override
	public boolean hasCss() {
		return false;
	}
	
	@Override
	public String getName() {
		return "Sickbeard";
	}

	@Override
	public Object refresh(Map<String, String> settings) {
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

						if (!show.getString("next_ep_airdate").trim()
								.equalsIgnoreCase("")) {
							try {
								TvShowObject showObject = new TvShowObject();
								showObject.name = show.getString("show_name");
								showObject.nextShowing = df.parse(show
										.getString("next_ep_airdate"));
								showObject.nextShowingReadable = show
										.getString("next_ep_airdate");
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
	public Object bigScreenRefresh(Map<String, String> settings) {
		return null;
	}

	@Override
	public WebSocketMessage processCommand(String method, String command) {

		return null;
	}

	@Override
	public Html getView(Module module) {
		return small.render(module);
	}

	@Override
	public Html getSettingsView() {
		return settings.render(null);
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
	public void init(Map<String, String> settings) {
		Logger.info("Initiating Sickbeard plugin.");
		url = settings.get(URL);
		if (!url.endsWith("/")) {
			url += "/";
		}

		if (!url.startsWith("http")) {
			url = "http://"+url;
		}

		baseUrl = url;

		url += "api";
		apiKey = settings.get(API_KEY);
	}

	@Override
	public int getRefreshRate() {
		return ONE_MINUTE;
	}

	private class TvShowObject implements Comparable<TvShowObject> {
		public String name, nextShowingReadable;
		public Date nextShowing;

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

}
