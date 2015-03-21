package plugins.yamahaamp;

import interfaces.PlugIn;

import java.util.Hashtable;
import java.util.Map;

import misc.HttpTools;
import models.Module;
import play.twirl.api.Html;
import plugins.yamahaamp.views.html.*;
import websocket.WebSocketMessage;

public class YamahaAmpPlugin implements PlugIn {

	private String host;
	private final String AMP_HOST = "host";
	private final String PATH = "/YamahaRemoteControl/ctrl";
	private final int PORT = 80;
	private final String METHOD_AMP_COMMAND = "ampCommand";

	@Override
	public boolean hasBigScreen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return "Yamaha Amp";
	}

	@Override
	public String getDescription() {
		return "Interact with your Yamaha amplifier, turn it on/off, ajust volume and select your input source.";
	}
	
	@Override
	public String getId() {
		return "yamahaamp";
	}
	
	@Override
	public boolean hasCss() {
		return true;
	}
	
	@Override
	public Object smallScreenRefresh(Map<String, String> settings) {

		try {

			return getAmpStatus();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private YamahaAmpStatus getAmpStatus() throws Exception {
		String url = "http://" + host + PATH;

		YamahaAmpStatus status = new YamahaAmpStatus();

		YNCRequest request = YNC.COMMANDS.get(YNC.GET_POWER_STATUS);
		String on = request.getResponseValue(
				HttpTools.rawPost(url, request.getRequest())).replaceAll(
				"[\n\r]", "");
		status.on = on.trim().equalsIgnoreCase("On");

		if (status.on) {

			request = YNC.COMMANDS.get(YNC.GET_VOLUME);
			String volume = request.getResponseValue(HttpTools.rawPost(url,
					request.getRequest()));
			volume = volume.replace("</Val><Exp>1</Exp><Unit>dB</Unit>", "")
					.replace("<Val>", "");

			request = YNC.COMMANDS.get(YNC.GET_INPUT);
			status.input = request.getResponseValue(
					HttpTools.rawPost(url, request.getRequest())).replaceAll(
					"[\n\r]", "");

			status.volume = Double.parseDouble(volume) / 10;
		}
		return status;
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings, long count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebSocketMessage processCommand(String method, String command) {
		WebSocketMessage response = new WebSocketMessage();

		if (method.equalsIgnoreCase(METHOD_AMP_COMMAND)) {
			try {

				sendCommand(command);
			} catch (Exception e) {
				response.setMessage("Error while sending '" + command
						+ "' to the amplifier.");
				response.setMethod(WebSocketMessage.METHOD_ERROR);
			}
		}
		return response;
	}

	private void sendCommand(String command) throws Exception {
		String url = "http://" + host + PATH;

		YNCRequest request = YNC.COMMANDS.get(command);
		HttpTools.rawPost(url, request.getRequest());

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
		this.host = settings.get(AMP_HOST);
	}

	@Override
	public int getRefreshRate() {
		return PlugIn.ONE_SECOND * 5;
	}

	private class YamahaAmpStatus {
		public boolean on;
		public String input;
		public double volume;
	}

	@Override
	public String getExternalLink() {
		return "http://"+host;
	}

	@Override
	public Object saveData() {
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
		return NO_REFRESH;
	}
	
	@Override
	public int getWidth() {
		return 4;
	}
	
	@Override
	public int getHeight() {
		return 2;
	}
	
	@Override
	public Map<String, String> exposeSettings(Map<String, String> settings) {
		Map<String, String> result = new Hashtable<>();
		result.put("Amplifier URL", settings.get(AMP_HOST));
		return result;
	}
	
	
}
