package plugins.dyndns;

import interfaces.PlugIn;

import java.util.Map;

import models.Module;
import play.twirl.api.Html;
import plugins.dyndns.views.html.small;
import websocket.WebSocketMessage;

public class DynDNSPlugin implements PlugIn{

	@Override
	public String getId() {
		return "dyndns";
	}

	@Override
	public boolean hasBigScreen() {
		return false;
	}

	@Override
	public String getName() {
		return "Dynamic DNS";
	}

	@Override
	public Object smallScreenRefresh(Map<String, String> settings) {
		return null;
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
		return null;
	}

	@Override
	public Html getBigView(Module module) {
		return null;
	}

	@Override
	public boolean hasSettings() {
		return false;
	}

	@Override
	public String getExternalLink() {
		return null;
	}

	@Override
	public void init(Map<String, String> settings, String data) {
		
	}

	@Override
	public Object saveData() {
		return null;
	}

	@Override
	public boolean hasCss() {
		return false;
	}

	@Override
	public void doInBackground(Map<String, String> settings) {
		
	}

	@Override
	public int getRefreshRate() {
		return 0;
	}

	@Override
	public int getBackgroundRefreshRate() {
		return 0;
	}

	@Override
	public int getBigScreenRefreshRate() {
		return 0;
	}

}
