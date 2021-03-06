package plugins.osx;

import interfaces.PlugIn;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import misc.Utils;
import models.Module;
import play.Logger;
import play.Play;
import play.twirl.api.Html;
import plugins.osx.views.html.*;
import websocket.WebSocketMessage;

public class OsXPlugin implements PlugIn {
	private Map<String, OsXApp> apps;
	private final String PNG_PATH = "cache/plugins/osx/images/";
	private final String FULL_PNG_PATH = Play.application().path().getPath() + "/" + PNG_PATH;

	private final String START_APP = "startApp", ACTIVATE_APP = "activateApp", QUIT_APP = "quitApp";

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return "osx";
	}

	@Override
	public boolean hasBigScreen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "OS X";
	}

	@Override
	public String getDescription() {
		return "Start and Quit the applications that are on your MacOS dock.";
	}

	@Override
	public Object smallScreenRefresh(Map<String, String> settings) {
		// TODO Auto-generated method stub
		try {
			apps = getDockAndRunningApplications();
			return apps;
		} catch (Exception e) {
			Logger.error("Can't get OsX apps", e);
			return null;
		}
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings, long count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebSocketMessage processCommand(String method, String command, Object extraPackage) {
		WebSocketMessage response = new WebSocketMessage();
		if (method.equalsIgnoreCase(START_APP)) {
			try {
				if (apps.containsKey(command)) {
					apps.get(command).setRunning(true);
					OsXUtils.startApplication(command);
					response.setMessage(apps);
					response.setMethod(WebSocketMessage.METHOD_REFRESH);
				}
			} catch (Exception e) {
				Logger.error("Error while Starting " + command, e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Error while Starting " + command);
			}
		} else if (method.equalsIgnoreCase(ACTIVATE_APP)) {
			try {
				if (apps.containsKey(command)) {
					OsXUtils.startApplication(command);
					return null;
				}
			} catch (Exception e) {
				Logger.error("Error while activating " + command, e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Error while activating " + command);
			}
		} else if (method.equalsIgnoreCase(QUIT_APP)) {
			try {
				if (apps.containsKey(command)) {
					OsXUtils.quitApplication(command);
					apps = getDockAndRunningApplications();
					response.setMessage(apps);
					response.setMethod(WebSocketMessage.METHOD_REFRESH);
				}
			} catch (Exception e) {
				Logger.error("Error while Quitting " + command, e);
				response.setMethod(WebSocketMessage.METHOD_ERROR);
				response.setMessage("Error while Quitting " + command);
			}
		}

		return response;
	}

	@Override
	public Html getSmallView(Module module) {
		// TODO Auto-generated method stub
		return small.render(module);
	}

	@Override
	public Html getSettingsView(Module module) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Html getBigView(Module module) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSettings() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getExternalLink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(Map<String, String> settings, String data) {
		// TODO Auto-generated method stub
		File f = new File(FULL_PNG_PATH);
		if (!f.exists()) {
			f.mkdirs();
		}
	}

	@Override
	public Object saveData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasCss() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void doInBackground(Map<String, String> settings) {

	}

	@Override
	public int getRefreshRate() {
		return ONE_SECOND * 30;
	}

	private Map<String, OsXApp> getDockAndRunningApplications() throws Exception {
		Map<String, OsXApp> apps = new LinkedHashMap<String, OsXApp>();
		String[] split;

		Logger.info("Getting all apps in the dock");
		for (String appPath : OsXUtils.getDockAppsByPath()) {
			appPath = appPath.trim();

			if (appPath.length() > 0 && !appPath.endsWith("java") && !appPath.endsWith("JavaApplicationStub")) {
				OsXApp app = new OsXApp();
				app.setPath(Utils.removeLastCharacter(appPath.replace("file://", "").replace("%20", " ")));

				setApp(app);

				if (app.getName().equalsIgnoreCase("java")) {
					continue;
				}
				apps.put(app.getName(), app);
			}
		}

		Logger.info("Getting running apps");

		for (String appName : OsXUtils.getRunningAppsByName()) {
			appName = appName.trim();
			if (appName.length() > 0 && !appName.equalsIgnoreCase("java") && !appName.equalsIgnoreCase("JavaApplicationStub")) {
				if (apps.containsKey(appName)) {
					Logger.info("App {} already in dock apps, changing status to running", appName);
					apps.get(appName).setRunning(true);
				} else {
					Logger.info("App not in dock, adding it to the app list");

					OsXApp app = new OsXApp();
					app.setPath(Utils.removeLastCharacter(OsXUtils.getAppPath(appName)));

					setApp(app);

					app.setRunning(true);
					apps.put(app.getName(), app);
				}
			}
		}

		apps.remove("Finder");
		return apps;
	}

	private void setApp(OsXApp app) throws Exception {
		String[] split;
		Logger.info("Found Dock application at path {}", app.getPath());

		split = app.getPath().split("/");
		app.setName(split[split.length - 1].replace(".app", ""));

		app.setIcnsPath(OsXUtils.getIconPath(app.getPath() + "/Contents/info.plist"));

		String pngName = PNG_PATH + app.getName() + ".png";
		pngName = pngName.replaceAll("( )+", "");

		String fullPngName = FULL_PNG_PATH + app.getName() + ".png";
		fullPngName = fullPngName.replaceAll("( )+", "");
		app.setPngPath(pngName);
		if (!new File(fullPngName).exists()) {
			OsXUtils.convertIcnsToPng(app.getIcnsPath(), fullPngName);
		}
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
		return 8;
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@Override
	public Map<String, String> exposeSettings(Map<String, String> settings) {
		return null;
	}
	
	@Override
	public Map<String, String> validateSettings(Map<String, String> settings) {
		return null;
	}
}
