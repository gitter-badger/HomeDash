package plugins.lychee;

import interfaces.PlugIn;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import models.Module;
import models.Setting;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Strings;
import com.google.common.io.Files;

import play.Logger;
import play.Play;
import play.twirl.api.Html;
import plugins.lychee.LycheeAPI.LoginFailedException;
import plugins.lychee.views.html.big;
import plugins.lychee.views.html.settings;
import plugins.lychee.views.html.small;
import websocket.WebSocketMessage;

public class LycheePlugin implements PlugIn {

	private final String GET_RECENT = "getRecent", TOGGLE_STAR = "toggleStar", TOGGLE_PUBLIC = "togglePublic", UPLOAD_PICTURE = "uploadPicture", GET_ALBUM = "getAlbum";
	private final String URL = "url", USERNAME = "username", PASSWORD = "password";

	private final String PNG_PATH = "cache/plugins/" + getId() + "/images/";
	private final String FULL_PNG_PATH = Play.application().path().getPath() + "/" + PNG_PATH;

	private String url;
	private LycheeAPI api;

	@Override
	public String getId() {
		return "lychee";
	}

	@Override
	public boolean hasBigScreen() {
		return true;
	}

	@Override
	public String getName() {
		return "Lychee";
	}

	@Override
	public String getDescription() {
		return "Interact with your Lychee installation";
	}

	@Override
	public Object smallScreenRefresh(Map<String, String> settings) {
		try {
			List<LycheeAlbum> albums = getAlbums();
			Map<String, Object> toSend = new Hashtable<>();

			List<String> thumbs = new ArrayList<>();
			for (LycheeAlbum album : albums) {
				if (album.getThumb0() != null && !album.getThumb0().trim().equalsIgnoreCase("")) {
					String fileName = (new File(album.getThumb0())).getName();
					File f = new File(FULL_PNG_PATH + fileName);

					if (!f.exists()) {
						FileUtils.copyURLToFile(new java.net.URL(album.getThumb0()), f);
					}

					thumbs.add(PNG_PATH + fileName);
				}

				if (album.getThumb1() != null && !album.getThumb1().trim().equalsIgnoreCase("")) {
					String fileName = (new File(album.getThumb1())).getName();
					File f = new File(FULL_PNG_PATH + fileName);
					if (!f.exists()) {
						FileUtils.copyURLToFile(new java.net.URL(album.getThumb1()), f);
					}

					thumbs.add(PNG_PATH + fileName);
				}

				if (album.getThumb2() != null && !album.getThumb2().trim().equalsIgnoreCase("")) {
					String fileName = (new File(album.getThumb2())).getName();
					File f = new File(FULL_PNG_PATH + fileName);
					if (!f.exists()) {
						FileUtils.copyURLToFile(new java.net.URL(album.getThumb2()), f);
					}

					thumbs.add(PNG_PATH + fileName);
				}
			}

			toSend.put("thumbs", thumbs);
			toSend.put("count", albums.size());

			return toSend;
		} catch (IOException | LoginFailedException e) {
			Logger.error("[LycheePlugin] Couldn't refresh", e);
			return null;
		}
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings, long count) {
		try {
			return api.getAlbums();
		} catch (Exception e) {
			Logger.error("Coudldn't get lychee albums");
			return null;
		}
	}

	@Override
	public WebSocketMessage processCommand(String method, String command, Object extraPackage) {
		WebSocketMessage response = new WebSocketMessage();

		if (method.equalsIgnoreCase(GET_RECENT)) {
			try {
				response.setMessage(getRecent());
				response.setMethod(GET_RECENT);
			} catch (LoginFailedException e) {
				Logger.error("Login failed", e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Logging in to Lychee failed.");
			} catch (Exception e) {
				Logger.error("Error while gettign recent album", e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Error while getting recent album.");
			}
		} else if (method.equalsIgnoreCase(TOGGLE_PUBLIC)) {
			try {
				response.setMessage(togglePublic(command));
				response.setMethod(TOGGLE_PUBLIC);
			} catch (LoginFailedException e) {
				Logger.error("Login failed", e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Logging in to Lychee failed.");
			} catch (Exception e) {
				Logger.error("Error while gettign recent album", e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Error while setting picture public.");
			}
		} else if (method.equalsIgnoreCase(TOGGLE_STAR)) {
			try {
				response.setMessage(toggleStar(command));
				response.setMethod(TOGGLE_STAR);
			} catch (LoginFailedException e) {
				Logger.error("Login failed", e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Logging in to Lychee failed.");
			} catch (Exception e) {
				Logger.error("Error while gettign recent album", e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Error while setting picture Star.");
			}
		} else if (method.equalsIgnoreCase(UPLOAD_PICTURE)) {
			try {
				response.setMessage(uploadPictures((List<File>) extraPackage, command));
				response.setMethod(UPLOAD_PICTURE);
			} catch (LoginFailedException e) {
				Logger.error("Login failed", e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Logging in to Lychee failed.");
			} catch (Exception e) {
				Logger.error("Error while gettign uploading pictures", e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Error while uploading pitctures to lychee.");
			}
		} else if (method.equalsIgnoreCase(GET_ALBUM)) {
			try {
				response.setMessage(api.getAlbum(command));
				response.setMethod(GET_ALBUM);
			} catch (LoginFailedException e) {
				Logger.error("Login failed", e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Logging in to Lychee failed.");
			} catch (Exception e) {
				Logger.error("Error while gettign uploading pictures", e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Error while uploading pitctures to lychee.");
			}
		}
		return response;
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
		return big.render(module);
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public String getExternalLink() {
		return url;
	}

	@Override
	public void init(Map<String, String> settings, String data) {
		MessageDigest md5;
		try {

			md5 = MessageDigest.getInstance("MD5");
			md5.update(settings.get(PASSWORD).getBytes());

			byte[] byteData = md5.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}

			url = settings.get(URL);
			String username = Setting.get(USERNAME);

			api = new LycheeAPI(url, username, sb.toString());

			File f = new File(FULL_PNG_PATH);
			if (!f.exists()) {
				f.mkdirs();
			}
			f.deleteOnExit();
		} catch (NoSuchAlgorithmException e) {
			Logger.error("Can't generate md5 password", e);
		}
	}

	@Override
	public Object saveData() {
		return null;
	}

	@Override
	public boolean hasCss() {
		return true;
	}

	@Override
	public void doInBackground(Map<String, String> settings) {

	}

	@Override
	public int getRefreshRate() {
		return ONE_HOUR;
	}

	@Override
	public int getBackgroundRefreshRate() {
		return NO_REFRESH;
	}

	@Override
	public int getBigScreenRefreshRate() {
		return ONE_HOUR;
	}

	@Override
	public int getWidth() {
		return 2;
	}

	@Override
	public int getHeight() {
		return 2;
	}

	@Override
	public Map<String, String> exposeSettings(Map<String, String> settings) {
		Map<String, String> result = new Hashtable<>();
		result.put("Url", url);
		return result;
	}

	// ////////////
	// // Lychee methods
	private List<LycheeAlbum> getAlbums() throws IOException, LoginFailedException {
		List<LycheeAlbum> albums = api.getAlbums();
		Collections.sort(albums);
		return albums;
	}

	private LycheeAlbum getRecent() throws IOException, LoginFailedException {
		LycheeAlbum recentAlbum = api.getAlbum("r");
		Collections.sort(recentAlbum.getPictures());
		java.util.Collections.reverse(recentAlbum.getPictures());
		return recentAlbum;
	}

	private boolean toggleStar(String command) throws IOException, LoginFailedException {
		api.togglePhotoStar(command);
		return true;
	}

	private boolean togglePublic(String command) throws IOException, LoginFailedException {
		api.togglePhotoPublic(command);
		return true;
	}

	private boolean uploadPictures(List<File> files, String albumId) throws IOException, LoginFailedException {
		Logger.info("Files to upload:{}", files.size());
		for (File f : files) {
			api.uploadPicture(f, albumId, "");
		}
		return true;
	}

}
