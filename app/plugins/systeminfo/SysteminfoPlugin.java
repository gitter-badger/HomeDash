package plugins.systeminfo;

import interfaces.PlugIn;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import models.Module;
import play.twirl.api.Html;
import views.html.plugins.systeminfo.settings;
import websocket.WebSocketMessage;

import com.sun.management.OperatingSystemMXBean;

public class SysteminfoPlugin implements PlugIn {
	private List<String> roots = new ArrayList<String>();
	
	@Override
	public boolean hasBigScreen() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public String getId() {
		return "systeminfo";
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "SystemInfo";
	}
	
	@Override
	public boolean hasCss() {
		return false;
	}
	
	@Override
	public WebSocketMessage processCommand(String method, String command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Html getView(Module module) {
		return views.html.plugins.systeminfo.small.render(module);
	}

	@Override
	public Html getSettingsView(Module module) {
		return settings.render(module);
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public Object refresh(Map<String, String>  settings) {
		try {
			SystemInfoData data = new SystemInfoData();
			//File[] roots = File.listRoots();

			OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
					OperatingSystemMXBean.class);			
			
			data.cpuUsage = osBean.getSystemCpuLoad();
			
			data.maxRam = osBean.getTotalPhysicalMemorySize();
			
			data.availableRam = osBean.getFreePhysicalMemorySize();
			
			data.usedRam = data.maxRam - data.availableRam;
			
			for (String path : roots) {
				
				File root = new File(path);
				
				long usedSpace = root.getTotalSpace() - root.getFreeSpace();
				
				String[] space =  new String[]{humanReadableByteCount(root.getTotalSpace(), true), humanReadableByteCount(root.getFreeSpace(), true), humanReadableByteCount(usedSpace, true)};
				
				data.diskSpace.put(root.getAbsolutePath(), space);
				
			}
			
			
			return data;
		} catch (Exception e) {

		}
		return null;
	}

	@Override
	public Html getBigView(Module module) {
		return null;
	}

	public class SystemInfoData {
		public double cpuUsage = 0;
		public double maxRam, availableRam, usedRam;
		public Hashtable<String, String[]> diskSpace = new Hashtable<String, String[]>();
	}
	
	private String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	@Override
	public int getRefreshRate() {
		// TODO Auto-generated method stub
		return TEN_SECONDS;
	}

	@Override
	public void init(Map<String, String> settings) {
		for(String root:settings.keySet()){
			String path = settings.get(root);
			if(!path.trim().equalsIgnoreCase("")){
				roots.add(settings.get(root));
			}
		}
		
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExternalLink() {
		return null;
	}

}
