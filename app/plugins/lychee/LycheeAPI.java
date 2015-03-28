package plugins.lychee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import play.Logger;
import misc.HttpTools;

public class LycheeAPI {
	private String url, username, password, baseUrl;
	private final static String FUNCTION = "function", USER = "user", PASSWORD = "password", ALBUM_ID = "albumID", PHOTO_ID = "photoID", TAGS = "tags";

	public LycheeAPI(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.baseUrl = url;
		if (!this.url.endsWith("/")) {
			this.url += "/";
			this.baseUrl += "/";
		}

		this.url += "php/api.php";
	}

	private boolean login() throws IOException, LoginFailedException {
		Logger.info("[LYCHEE] Logging in to [{}] with user[{}] password[{}]", url, username, password);
		Map<String, String> params = new Hashtable<>();
		params.put(FUNCTION, "login");
		params.put(PASSWORD, password);
		params.put(USER, username);

		String response = HttpTools.sendPost(url, params);
		Logger.info("[LYCHEE] Response: " + response);
		if (response.trim().equalsIgnoreCase("1")) {
			return true;
		} else {
			throw new LoginFailedException();
		}
	}

	public List<LycheeAlbum> getAlbums() throws IOException, LoginFailedException {
		login();

		Logger.info("[LYCHEE] Getting albums");
		Map<String, String> params = new Hashtable<>();
		params.put(FUNCTION, "getAlbums");

		String response = HttpTools.sendPost(url, params);
		Logger.info("[LYCHEE] Response: " + response);

		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(response);

		return parseAlbums(json);
	}

	public LycheeAlbum getAlbum(String id) throws IOException, LoginFailedException {
		login();
		Logger.info("[LYCHEE] Getting album[{}]", id);

		Map<String, String> params = new Hashtable<>();
		params.put(FUNCTION, "getAlbum");
		params.put(ALBUM_ID, id);

		String response = HttpTools.sendPost(url, params);
		Logger.info("[LYCHEE] Response: " + response);

		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(response);

		return parseAlbum(json);
	}

	public void togglePhotoPublic(String photoId) throws IOException, LoginFailedException {
		Logger.info("[LYCHEE] Setting public photo[{}]", photoId);
		login();
		Map<String, String> params = new Hashtable<>();
		params.put(FUNCTION, "setPhotoPublic");
		params.put(PHOTO_ID, photoId);
		
		Logger.info("[LYCHEE] Response: " + HttpTools.sendPost(url, params));

	}
	
	public void togglePhotoStar(String photoId) throws IOException, LoginFailedException {
		Logger.info("[LYCHEE] setting star photo[{}]", photoId);
		login();
		Map<String, String> params = new Hashtable<>();
		params.put(FUNCTION, "setPhotoStar");
		params.put("photoIDs", photoId);
		
		Logger.info("[LYCHEE] Response: " + HttpTools.sendPost(url, params));

	}
	
	public void uploadPicture(File f, String albumId, String tags) throws IOException, LoginFailedException{
		Logger.info("[LYCHEE] Uploading picture [{}]", f.getName());
		login();
		Map<String, String> params = new Hashtable<>();
		params.put(FUNCTION, "upload");
		params.put(ALBUM_ID, albumId);
		params.put(TAGS, tags);
		
		Logger.info("[LYCHEE] Response: " + HttpTools.uploadFile(url, params, f));
	}

	private List<LycheeAlbum> parseAlbums(JsonObject json) {

		ArrayList<LycheeAlbum> albums = new ArrayList<>();

		LycheeAlbum unsorted = new LycheeAlbum();
		unsorted.setTitle("Unsorted");
		unsorted.setId("0");
		JsonElement element = json.get("unsortedThumb0");
		if (element != null) {
			unsorted.setThumb0(baseUrl + element.getAsString());
		}
		element = json.get("unsortedThumb1");
		if (element != null) {
			unsorted.setThumb1(baseUrl + element.getAsString());
		}
		element = json.get("unsortedThumb2");
		if (element != null) {
			unsorted.setThumb2(baseUrl + element.getAsString());
		}
		albums.add(unsorted);

		// STARRD ALBUM
		LycheeAlbum starred = new LycheeAlbum();
		starred.setTitle("Starred");
		starred.setId("f");
		element = json.get("starredThumb0");
		if (element != null) {
			starred.setThumb0(baseUrl + element.getAsString());
		}
		element = json.get("starredThumb1");
		if (element != null) {
			starred.setThumb1(baseUrl + element.getAsString());
		}
		element = json.get("starredThumb2");
		if (element != null) {
			starred.setThumb2(baseUrl + element.getAsString());
		}
		albums.add(starred);

		// Public ALBUM
		LycheeAlbum publicAlbum = new LycheeAlbum();
		publicAlbum.setTitle("Public");
		publicAlbum.setId("s");
		element = json.get("publicThumb0");
		if (element != null) {
			publicAlbum.setThumb0(baseUrl + element.getAsString());
		}
		element = json.get("publicThumb1");
		if (element != null) {
			publicAlbum.setThumb1(baseUrl + element.getAsString());
		}
		element = json.get("publicThumb2");
		if (element != null) {
			publicAlbum.setThumb2(baseUrl + element.getAsString());
		}
		albums.add(publicAlbum);

		// Recent ALBUM
		LycheeAlbum recent = new LycheeAlbum();
		recent.setTitle("Recent");
		recent.setId("r");
		element = json.get("recentThumb0");
		if (element != null) {
			recent.setThumb0(baseUrl + element.getAsString());
		}
		element = json.get("recentThumb1");
		if (element != null) {
			recent.setThumb1(baseUrl + element.getAsString());
		}
		element = json.get("recentThumb2");
		if (element != null) {
			recent.setThumb2(baseUrl + element.getAsString());
		}
		albums.add(recent);

		Iterator<Entry<String, JsonElement>> restOfAlbums = json.getAsJsonObject("content").entrySet().iterator();
		while (restOfAlbums.hasNext()) {
			JsonObject jsonAlbum = restOfAlbums.next().getValue().getAsJsonObject();

			albums.add(parseAlbum(jsonAlbum));
		}

		return albums;
	}

	private LycheeAlbum parseAlbum(JsonObject jsonAlbum) {
		JsonElement element = jsonAlbum.get("id");
		LycheeAlbum album = new LycheeAlbum();
		if (element != null) {
			album.setId(element.getAsString());
		}

		element = jsonAlbum.get("title");
		if (element != null) {
			album.setTitle(element.getAsString());
		}

		element = jsonAlbum.get("public");
		if (element != null) {
			album.setPublic(element.getAsBoolean());
		}

		element = jsonAlbum.get("sysstamp");
		if (element != null) {
			album.setSysstamp(element.getAsLong());
		}

		element = jsonAlbum.get("password");
		if (element != null) {
			album.setPassword(element.getAsBoolean());
		}

		/*
		 * "public":"0", "sysstamp":"1414811857", "password":false,
		 * "sysdate":"November 2014",
		 * "thumb0":"uploads\/thumb\/21c06059379f20947fc073cd30b73d25.jpg",
		 * "thumb1":"uploads\/thumb\/768eb0da1ff3748e9dca6e8a277a7f3c.jpg",
		 * "thumb2":"uploads\/thumb\/2338f3ee36327f03db2ccaffecb20742.jpg"
		 */

		element = jsonAlbum.get("sysdate");
		if (element != null) {
			album.setSysDate(element.getAsString());
		}

		element = jsonAlbum.get("thumb0");
		if (element != null) {
			album.setThumb0(baseUrl + element.getAsString());
		}

		element = jsonAlbum.get("thumb1");
		if (element != null) {
			album.setThumb1(baseUrl + element.getAsString());
		}

		element = jsonAlbum.get("thumb2");
		if (element != null) {
			album.setThumb2(baseUrl + element.getAsString());
		}

		Logger.info("[LYCHEE] Parsed ablum with title[{}]", album.getTitle());

		// Parsing pictures
		try {
			Iterator<Entry<String, JsonElement>> pictures = jsonAlbum.getAsJsonObject("content").entrySet().iterator();

			while (pictures.hasNext()) {
				JsonObject jsonPicture = pictures.next().getValue().getAsJsonObject();

				album.getPictures().add(parsePicture(jsonPicture));
			}

			Logger.info("[LYCHEE] Found [{}] pictures for album", album.getPictures().size());
		} catch (Exception e) {
			Logger.info("[LYCHEE] No pictures for album");
		}

		return album;
	}

	private LycheePicture parsePicture(JsonObject jsonPicture) {
		LycheePicture picture = new LycheePicture();

		JsonElement element = jsonPicture.get("id");
		if (element != null) {
			picture.setId(element.getAsString());
		}

		element = jsonPicture.get("title");
		if (element != null) {
			picture.setTitle(element.getAsString());
		}

		element = jsonPicture.get("tags");
		if (element != null) {
			picture.setTags(element.getAsString());
		}

		element = jsonPicture.get("public");
		if (element != null) {
			picture.setPublic(element.getAsInt() == 1);
		}

		element = jsonPicture.get("star");
		if (element != null) {
			picture.setStar(element.getAsInt() == 1);
		}

		element = jsonPicture.get("album");
		if (element != null) {
			picture.setAlbum(element.getAsString());
		}

		element = jsonPicture.get("thumbUrl");
		if (element != null) {
			picture.setThumbUrl(baseUrl + element.getAsString());
		}

		element = jsonPicture.get("takestamp");
		if (element != null) {
			picture.setTakeStamp(element.getAsLong());
		}

		element = jsonPicture.get("url");
		if (element != null) {
			picture.setUrl(baseUrl + element.getAsString());
		}

		element = jsonPicture.get("sysdate");
		if (element != null) {
			picture.setSysDate(element.getAsString());
		}

		element = jsonPicture.get("previousPhoto");
		if (element != null) {
			picture.setPreviousPhoto(element.getAsString());
		}

		element = jsonPicture.get("nextPhoto");
		if (element != null) {
			picture.setNextPhoto(element.getAsString());
		}

		element = jsonPicture.get("cameraDate");
		if (element != null) {
			picture.setCameraDate(element.getAsLong());
		}

		return picture;
	}

	public class LoginFailedException extends Exception {
	}

}
