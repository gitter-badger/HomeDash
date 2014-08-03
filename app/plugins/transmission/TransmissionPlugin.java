package plugins.transmission;

import interfaces.PlugIn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.Module;

import org.json.JSONException;

import play.Logger;
import play.twirl.api.Html;
import views.html.plugins.transmission.big;
import views.html.plugins.transmission.settings;
import views.html.plugins.transmission.small;
import websocket.WebSocketMessage;
import ca.benow.transmission.AddTorrentParameters;
import ca.benow.transmission.TransmissionClient;
import ca.benow.transmission.model.SessionStatus;
import ca.benow.transmission.model.TorrentStatus;
import ca.benow.transmission.model.TorrentStatus.TorrentField;
import ca.benow.transmission.model.TransmissionSession.SessionField;
import ca.benow.transmission.model.TransmissionSession.SessionPair;

public class TransmissionPlugin implements PlugIn {

	private TransmissionClient client;

	private String url;
	private int port;

	public static final String URL = "url", PORT = "port",
			USERNAME = "username", PASSWORD = "password";
	public static final String METHOD_ADDTORRENT = "addTorrent",
			METHOD_ALTSPEED = "altSpeed";

	@Override
	public boolean hasBigScreen() {
		return true;
	}

	@Override
	public boolean hasCss() {
		return false;
	}
	
	@Override
	public String getId() {
		return "transmission";
	}
	
	@Override
	public String getName() {
		return "Transmission";
	}

	@Override
	public Object refresh(Map<String, String> settings) {

		try {

			return getSessionStats();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	private TorrentSession getSessionStats() throws JSONException, IOException {
		TorrentSession obj = new TorrentSession();

		Map<SessionField, Object> session = client.getSession();
		obj.status = client.getSessionStats();
		obj.rpcVersion = Integer.parseInt(session.get(SessionField.rpcVersion)
				.toString());
		obj.alternateSpeeds = (Boolean) session
				.get(SessionField.altSpeedEnabled);

		return obj;
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings) {
		TorrentSession obj = new TorrentSession();

		try {
			obj = getSessionStats();

			TorrentField[] fields = new TorrentField[] { TorrentField.name,
					TorrentField.rateDownload, TorrentField.rateUpload,
					TorrentField.percentDone, TorrentField.id,
					TorrentField.status, TorrentField.downloadedEver,
					TorrentField.uploadedEver, TorrentField.totalSize };

			Logger.info(""+obj.rpcVersion);
			obj.torrents = new ArrayList<TransmissionPlugin.TorrentObject>();
			for (TorrentStatus torrent : client.getAllTorrents(fields)) {
				TorrentObject t = new TorrentObject();
				t.mapTorrent(torrent, obj.rpcVersion);
				
				obj.torrents.add(t);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return obj;
	}

	@Override
	public WebSocketMessage processCommand(String method, String command) {
		Logger.info("[Transmission] Recieved method [{}], command [{}]",
				method, command);

		WebSocketMessage response = new WebSocketMessage();

		if (method.equalsIgnoreCase(METHOD_ADDTORRENT)) {
			response = addTorrent(command);
		} else if (method.equalsIgnoreCase(METHOD_ALTSPEED)) {
			response = altSpeed(command.equalsIgnoreCase("true"));
		} else {
			response.setMethod(WebSocketMessage.METHOD_ERROR);
			response.setMessage("No matching method.");
		}
		return response;
	}

	private WebSocketMessage altSpeed(boolean altSpeed) {
		WebSocketMessage response = new WebSocketMessage();
		try {

			SessionPair pair = new SessionPair(SessionField.altSpeedEnabled,
					altSpeed);
			client.setSession(pair);

			response.setMethod(WebSocketMessage.METHOD_SUCCESS);
			response.setMessage("Alternate speed set successfully !");

		} catch (Exception e) {
			response.setMethod(WebSocketMessage.METHOD_ERROR);
			response.setMessage("Error while seting alternate speed.");
		}
		return response;
	}

	private WebSocketMessage addTorrent(String url) {
		WebSocketMessage response = new WebSocketMessage();
		try {
			AddTorrentParameters params = new AddTorrentParameters(url);
			client.addTorrent(params);
			response.setMethod(WebSocketMessage.METHOD_SUCCESS);
			response.setMessage("Torrent added successfully !");

		} catch (Exception e) {
			response.setMethod(WebSocketMessage.METHOD_ERROR);
			response.setMessage("Error while adding torrent.");
		}

		return response;

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
		return big.render(module);
	}

	@Override
	public boolean hasSettings() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getRefreshRate() {
		// TODO Auto-generated method stub
		return FIVE_SECONDS;
	}

	@Override
	public void init(Map<String, String> settings) {
		Logger.info("Initiating Transmission plugin.");

		url = settings.get(URL);
		port = Integer.parseInt(settings.get(PORT));

		if (settings.get(USERNAME).equalsIgnoreCase("")
				|| settings.get(PASSWORD).equalsIgnoreCase("")) {
			Logger.info("Connecting to [{}] No username and password.", url
					+ ":" + port);
			client = new TransmissionClient(settings.get(URL),
					Integer.parseInt(settings.get(PORT)));
		} else {
			Logger.info("Connecting to [{}] Using username [{}] and password.",
					url + ":" + port, settings.get(USERNAME));
			client = new TransmissionClient(url, port, settings.get(USERNAME),
					settings.get(PASSWORD));
		}

		Logger.info("Transmission client ready !");

	}

	// //

	private class TorrentSession {
		public SessionStatus status;
		public boolean alternateSpeeds;
		public List<TorrentObject> torrents;
		public int rpcVersion;
	}

	private class TorrentObject {
		public String name;
		public String statusStr;
		public int downloadSpeed, uploadSpeed, id, status;
		public double percentDone;
		public long downloaded, uploaded, totalSize;

		public void mapTorrent(TorrentStatus torrent, int rpcVersion) {

			name = torrent.getName();
			downloadSpeed = Integer.parseInt(torrent.getField(
					TorrentField.rateDownload).toString());
			uploadSpeed = Integer.parseInt(torrent.getField(
					TorrentField.rateUpload).toString());
			status = Integer.parseInt(torrent.getField(TorrentField.status)
					.toString());
			
			statusStr = TorrentStatus.getStatusString(status, rpcVersion);
			
			percentDone = Double.parseDouble(torrent.getField(
					TorrentField.percentDone).toString());
			id = torrent.getId();
			downloaded = Long.parseLong(torrent.getField(
					TorrentField.downloadedEver).toString());
			uploaded = Long.parseLong(torrent.getField(
					TorrentField.uploadedEver).toString());
			totalSize = Long.parseLong(torrent.getField(TorrentField.totalSize)
					.toString());
			Logger.debug(
					"Torrent #{}, Name: {}, UploadSpeed:{}, DownloadSpeed: {}, PercentDone: {}%, Status:{}",
					id, name, uploadSpeed, downloadSpeed, percentDone, statusStr);
		}
	}

	@Override
	public String getExternalLink() {
		return "http://" + url + ":" + port;
	}

}
