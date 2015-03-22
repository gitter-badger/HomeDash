package interfaces;

import java.util.Map;

import models.Module;
import play.twirl.api.Html;
import websocket.WebSocketMessage;

public interface PlugIn {
	public static final int NO_REFRESH = 0, ONE_SECOND = 1000, ONE_MINUTE = ONE_SECOND * 60, ONE_HOUR = ONE_MINUTE * 60;
	
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
	 * Get Plugin description to be displayed on the addModule page.
	 * @return
	 */
	public String getDescription();
	
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
	public WebSocketMessage processCommand(String method, String command, Object extraPackage);
	
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
	 * Refresh every x ms on small size module
	 * @return
	 */
	public int getRefreshRate();
	
	/**
	 * Refresh rate of background tasks
	 * @return
	 */
	public int getBackgroundRefreshRate();
	
	/**
	 * Get refresh rate on the big screen
	 * @return
	 */
	public int getBigScreenRefreshRate();
	
	/**
	 *  Get height and Width of the module,
	 *  1 = 100px;
	 * @return
	 */
	public int getWidth();
	public int getHeight();
	
	
	/**
	 * List of settings that can be made public
	 * for example when your want to add a remote plugin the exposed settings will appear in the module list
	 * For example the transmission plugin will show the transmission URL
	 * @param settings
	 * @return
	 */
	public Map<String, String> exposeSettings(Map<String, String>  settings);
}
