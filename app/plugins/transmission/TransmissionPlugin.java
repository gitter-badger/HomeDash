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
import ca.benow.transmission.model.TorrentStatus.StatusField;
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
			METHOD_ALTSPEED = "altSpeed", METHOD_REMOVETORRENT = "removeTorrent", METHOD_PAUSETORRENT = "pauseTorrent";

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
	public Object smallScreenRefresh(Map<String, String> settings) {

		try {

			return getSessionStats();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings, long count) {
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
		}else if (method.equalsIgnoreCase(METHOD_PAUSETORRENT)) {
			response = pauseTorrent(Integer.parseInt(command));
		}else if (method.equalsIgnoreCase(METHOD_REMOVETORRENT)) {
			response = removeTorrent(Integer.parseInt(command));
		} else {
			response.setMethod(WebSocketMessage.METHOD_ERROR);
			response.setMessage("No matching method.");
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
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getRefreshRate() {
		// TODO Auto-generated method stub
		return FIVE_SECONDS;
	}

	@Override
	public void init(Map<String, String> settings, String data) {
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
	
	@Override
	public String getExternalLink() {
		return "http://" + url + ":" + port;
	}

	@Override
	public Object saveData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doInBackground(Map<String, String>  settings) {		
	}
	
	@Override
	public int getBackgroundRefreshRate() {
		return NO_REFRESH;
	}
	
	@Override
	public int getBigScreenRefreshRate() {
		return FIVE_SECONDS;
	}

	///////////////
	/// PLUG IN METHODS
	
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
	
	private WebSocketMessage pauseTorrent(int id){
		WebSocketMessage response = new WebSocketMessage();
		try {
			int[] ids = {id};
			TorrentStatus torrent = client.getTorrents(ids, TorrentField.status).get(0);
			
			if(torrent.getStatus() == StatusField.stopped){
				client.startTorrents(id);
				response.setMessage("Torrent resumed successfully !");
			}else{
				client.stopTorrents(id);
				response.setMessage("Torrent paused successfully !");
			}
			
			response.setMethod(WebSocketMessage.METHOD_SUCCESS);

		} catch (Exception e) {
			response.setMethod(WebSocketMessage.METHOD_ERROR);
			response.setMessage("Error while seting alternate speed.");
		}
		return response;
	}
	
	private WebSocketMessage removeTorrent(int id){
		
		
		WebSocketMessage response = new WebSocketMessage();
		try {

			Object[] ids = {id};
			client.removeTorrents(ids, false);
			response.setMethod(WebSocketMessage.METHOD_SUCCESS);
			response.setMessage("Torrent removed successfully !");

		} catch (Exception e) {
			response.setMethod(WebSocketMessage.METHOD_ERROR);
			response.setMessage("Error while seting alternate speed.");
		}
		return response;
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
	
	////////////////
	///// INNER CLASSES
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

	

}
