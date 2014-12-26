package interfaces;

import java.util.Map;

import models.Module;
import play.twirl.api.Html;
import websocket.WebSocketMessage;

public interface PlugIn {
	public static final int NO_REFRESH = 0, ONE_SECOND = 1000, FIVE_SECONDS = 5000, TEN_SECONDS = 10000, ONE_MINUTE = 60000, TEN_MINUTES = 600000;
	
	/**
	 * Get the id of the plugin
	 * @return a String without space and capital letters
	 */
	public String getId();
	
	/**
	 * Method to know if the plug in has a big screen
	 * @return
	 */
	public boolean hasBigScreen();

	/**
	 * Get the name of the plugin
	 * @return
	 */
	public String getName();
	
	/**
	 * Refresh data on small screen view
	 * @param settings
	 * @return
	 */
	public Object smallScreenRefresh(Map<String, String>  settings);
	
	/**
	 * Refresh data on big screen
	 * @param settings
	 * @param occurence of refreshing, start with 0
	 * @return
	 */
	public Object bigScreenRefresh(Map<String, String>  settings, long count);
	
	/**
	 * Process a command sent by the client
	 * @param method
	 * @param command
	 * @return
	 */
	public WebSocketMessage processCommand(String method, String command);
	
	/**
	 * Get small view
	 * @param module
	 * @return
	 */
	public Html getSmallView(Module module);
	
	/**
	 * Get the settings view
	 * @param module
	 * @return
	 */
	public Html getSettingsView(Module module);
	
	/**
	 * Get big view
	 * @param module
	 * @return
	 */
	public Html getBigView(Module module);
	
	/**
	 * Flag to know if the plugin has settings
	 * @return
	 */
	public boolean hasSettings();
	
	/**
	 * If the plugin links to an external page
	 * @return
	 */
	public String getExternalLink();
	
	/**
	 * Method called when the plugin is initiated by a module
	 * @param settings
	 * @param data
	 */
	public void init(Map<String, String>  settings, String data);
	
	/**
	 * Save data to database
	 * @return object that will be serialized in JSON
	 */
	public Object saveData();
	
	/**
	 * Flag to know if the plugin has extra CSS file
	 * @return
	 */
	public boolean hasCss();
	
	/**
	 * Method that will be done in background wether there is a client connected or not
	 */
	public void doInBackground(Map<String, String>  settings);
	/**
	 * Refresh every x ms
	 * @return
	 */
	public int getRefreshRate();
	
	/**
	 * Refresh rate of background tasks
	 * @return
	 */
	public int getBackgroundRefreshRate();
	
	/**
	 * Get refresh rate
	 * @return
	 */
	public int getBigScreenRefreshRate();
}
