package interfaces;

import java.util.Map;

import models.Module;
import play.twirl.api.Html;
import websocket.WebSocketMessage;

public interface PlugIn {
	public static final int NO_REFRESH = 0, ONE_SECOND = 1000, FIVE_SECONDS = 5000, TEN_SECONDS = 10000, ONE_MINUTE = 60000, TEN_MINUTES = 600000;
	
	public String getId();
	public boolean hasBigScreen();
	public String getName();
	public Object refresh(Map<String, String>  settings);
	public Object bigScreenRefresh(Map<String, String>  settings);
	public WebSocketMessage processCommand(String method, String command);
	public Html getView(Module module);
	public Html getSettingsView(Module module);
	public Html getBigView(Module module);
	public boolean hasSettings();
	public String getExternalLink();
	public void init(Map<String, String>  settings);
	public boolean hasCss();
	/**
	 * Refresh every x ms
	 * @return
	 */
	public int getRefreshRate();
}
