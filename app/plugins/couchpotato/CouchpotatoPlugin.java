package plugins.couchpotato;

import interfaces.PlugIn;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import misc.HttpTools;
import models.Module;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.twirl.api.Html;
import plugins.couchpotato.views.html.*;
import websocket.WebSocketMessage;

public class CouchpotatoPlugin implements PlugIn {
	public static final String URL = "url", API_KEY = "apiKey";
	public String url, apiKey, baseUrl;

	public static final String METHOD_SEARCH_MOVIE = "searchMovie", METHOD_MOVIE_LIST = "movieList", METHOD_ADD_MOVIE = "addMovie";

	private final String API_MOVIE_SEARCH = "/movie.search/?q=";
	private final String API_ADD_MOVIE = "/movie.add/?title=[TITLE]&identifier=[IMDB]";
	private final String API_AVAILABLE = "/app.available";
	private final String API_MOVIE_LIST = "/movie.list/?status=active";

	private final String PNG_PATH = "cache/plugins/"+getId()+"/images/";
	private final String FULL_PNG_PATH = Play.application().path().getPath() + "/" + PNG_PATH;

	private boolean initialized = false;

	@Override
	public boolean hasBigScreen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "CouchPotato";
	}

	@Override
	public String getDescription() {
		return "Add movies to your Couchpotato wanted list.";
	}

	@Override
	public Object smallScreenRefresh(Map<String, String> settings) {

		try {
			JSONObject json = new JSONObject(HttpTools.sendGet(url + API_AVAILABLE));

			JSONObject movieList = new JSONObject(HttpTools.sendGet(url + API_MOVIE_LIST));
			String poster = null;
			if (movieList.getBoolean("success")) {
				JSONArray movies = movieList.getJSONArray("movies");
				for (int i = 0; i < movies.length() && poster == null; i++) {
					JSONObject movieInfo = movies.getJSONObject(new Random().nextInt(movies.length())).getJSONObject("info");
					JSONArray images = movieInfo.getJSONObject("images").getJSONArray("poster_original");
					if (images.length() != 0) {

						// poster = images.getString(new
						// Random().nextInt(images.length()));

						File f = new File(FULL_PNG_PATH + movieInfo.getString("imdb") + ".jpg");
						if (!f.exists()) {
							FileUtils.copyURLToFile(new java.net.URL(images.getString(new Random().nextInt(images.length()))), f);
						}
						poster = PNG_PATH + movieInfo.getString("imdb") + ".jpg";

					}
				}
			}

			return poster;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings, long count) {
		return null;
	}

	@Override
	public WebSocketMessage processCommand(String method, String command) {
		WebSocketMessage response = new WebSocketMessage();
		if (method.equalsIgnoreCase(METHOD_SEARCH_MOVIE)) {
			try {
				response.setMessage(searchMovie(command));
				response.setMethod(METHOD_MOVIE_LIST);
			} catch (Exception e) {
				Logger.error("Error while searching movie", e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Error while searching movie.");
			}
		} else if (method.equalsIgnoreCase(METHOD_ADD_MOVIE)) {
			try {
				String[] split = command.split("___");
				addMovie(split[1], split[0]);

				response.setMethod(WebSocketMessage.METHOD_SUCCESS);
				response.setMessage("Movie added successfully !");
			} catch (Exception e) {
				Logger.error("Error while searching movie", e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Error while Adding movie.");
			}
		}
		return response;
	}

	private void addMovie(String imdbId, String movieName) throws IOException {
		String queryUrl = url + API_ADD_MOVIE.replace("[TITLE]", URLEncoder.encode(movieName)).replace("[IMDB]", imdbId);
		HttpTools.sendGet(queryUrl);
	}

	private List<MovieObject> searchMovie(String query) throws IOException {
		List<MovieObject> result = new ArrayList<CouchpotatoPlugin.MovieObject>();
		String queryUrl = url + API_MOVIE_SEARCH + URLEncoder.encode(query);

		JSONObject json = new JSONObject(HttpTools.sendGet(queryUrl));

		JSONArray jsonarray = json.getJSONArray("movies");

		for (int i = 0; i < jsonarray.length(); i++) {

			JSONObject movie = jsonarray.getJSONObject(i);

			MovieObject movieObject = new MovieObject();
			try {
				movieObject.imdbId = movie.getString("imdb");

				JSONArray images = movie.getJSONObject("images").getJSONArray("poster_original");
				if (images.length() != 0) {
					File f = new File(FULL_PNG_PATH + movieObject.imdbId + ".jpg");
					if (!f.exists()) {
						FileUtils.copyURLToFile(new java.net.URL(images.getString(0)), f);
					}
					movieObject.poster = PNG_PATH + movieObject.imdbId + ".jpg";
				}
			} catch (Exception e) {

			}

			try {
				movieObject.inLibrary = movie.getBoolean("in_library");
			} catch (JSONException e) {
				movieObject.inLibrary = true;
			}

			movieObject.originalTitle = movie.getString("original_title");
			try {
				movieObject.wanted = movie.getBoolean("in_wanted");
			} catch (JSONException e) {
				movieObject.wanted = true;
			}
			movieObject.year = movie.getInt("year");

			result.add(movieObject);

		}

		return result;

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
		return null;
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public void init(Map<String, String> settings, String data) {
		Logger.info("Initiating Couchpotato plugin.");

		url = settings.get(URL);

		if (!url.endsWith("/")) {
			url += "/";
		}

		if (!url.startsWith("http")) {
			url = "http://" + url;
		}

		baseUrl = url;
		apiKey = settings.get(API_KEY);

		url += "api/" + apiKey;
		Logger.info("Couchpotato URL:{}", url);

		File f = new File(FULL_PNG_PATH);
		if (!f.exists()) {
			f.mkdirs();
		}
		f.deleteOnExit();
	}

	@Override
	public int getRefreshRate() {
		return PlugIn.ONE_MINUTE;
	}

	private class MovieObject {
		public boolean inLibrary;
		public String originalTitle;
		public int year;
		public boolean wanted;
		public String imdbId;
		public String poster;

	}

	@Override
	public String getExternalLink() {
		return baseUrl;
	}

	@Override
	public String getId() {
		return "couchpotato";
	}

	@Override
	public boolean hasCss() {
		return true;
	}

	@Override
	public Object saveData() {
		return null;
	}

	@Override
	public void doInBackground(Map<String, String> settings) {
		// TODO Auto-generated method stub

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
		return 2;
	}

	@Override
	public int getHeight() {
		return 4;
	}

}
