package plugins.harddisk;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

import play.twirl.api.Html;
import plugins.harddisk.views.html.settings;
import plugins.harddisk.views.html.small;
import models.Module;
import websocket.WebSocketMessage;
import interfaces.PlugIn;

public class HardDiskPlugin implements PlugIn{
	private final String SETTING_PATH = "path";
	
	
	@Override
	public String getId() {
		return "harddisk";
	}

	@Override
	public boolean hasBigScreen() {
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Hard Disk";
	}

	@Override
	public String getDescription() {
		return "Show the usage of a single mount point";
	}

	@Override
	public Object smallScreenRefresh(Map<String, String> settings) {
		
		File root = new File(settings.get(SETTING_PATH));

		long usedSpace = root.getTotalSpace() - root.getFreeSpace();

		Map<String, String> diskSpace = new Hashtable<String, String>();

		
		 Map<String, String> spaces = new Hashtable<>();
		//String[] space = new String[] { root.getTotalSpace(), true), humanReadableByteCount(root.getFreeSpace(), true), humanReadableByteCount(usedSpace, true) };
		 spaces.put("path", root.getAbsolutePath());
		 spaces.put("total", Long.toString(root.getTotalSpace()));
		 spaces.put("free", Long.toString(root.getFreeSpace()));
		 spaces.put("used", Long.toString(usedSpace));
		 spaces.put("pretty", humanReadableByteCount(usedSpace, root.getTotalSpace(), true));
		
		return spaces;
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
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void doInBackground(Map<String, String> settings) {
		
	}

	@Override
	public int getRefreshRate() {
		return ONE_MINUTE;
	}

	@Override
	public int getBackgroundRefreshRate() {
		return 0;
	}

	@Override
	public int getBigScreenRefreshRate() {
		return 0;
	}

	@Override
	public int getWidth() {
		return 2;
	}

	@Override
	public int getHeight() {
		return 1;
	}
	
	////// CLASS methods
	
	private String humanReadableByteCount(long usedBytes, long maxBytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (maxBytes < unit)
			return maxBytes + " B";
		int exp = (int) (Math.log(maxBytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.2f / %.2f %sB", usedBytes / Math.pow(unit, exp), maxBytes / Math.pow(unit, exp), pre);
	}

}
