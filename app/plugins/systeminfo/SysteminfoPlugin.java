package plugins.systeminfo;

import interfaces.PlugIn;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import models.Module;
import notifications.Notifications;
import play.Logger;
import play.twirl.api.Html;
import plugins.systeminfo.views.html.*;
import websocket.WebSocketMessage;

import com.sun.management.OperatingSystemMXBean;

public class SysteminfoPlugin implements PlugIn {
	private List<String> roots = new ArrayList<String>();
	private List<CpuInfo> cpuInfo = new ArrayList<CpuInfo>();
	private List<RamInfo> ramInfo = new ArrayList<RamInfo>();
	private final int MAX_INFO_SIZE = 100, WARNING_THRESHOLD = 90;
	private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
	private final String SETTING_NOTIFICATIONS = "notifications";
	private final DecimalFormat nf = new DecimalFormat("#,###,###,##0.00");

	@Override
	public boolean hasBigScreen() {
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
	public String getDescription() {
		return "Monitor your server CPU, RAM and HDD.";
	}

	@Override
	public boolean hasCss() {
		return true;
	}

	@Override
	public WebSocketMessage processCommand(String method, String command) {
		// TODO Auto-generated method stub
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
	public boolean hasSettings() {
		return true;
	}

	@Override
	public Object smallScreenRefresh(Map<String, String> settings) {
		try {
			SystemInfoData data = new SystemInfoData();
			// File[] roots = File.listRoots();

			if (cpuInfo.size() > 0 && ramInfo.size() > 0) {
				data.cpuInfo = this.cpuInfo;
				data.ramInfo = this.ramInfo;
			}

			for (String path : roots) {

				File root = new File(path);

				long usedSpace = root.getTotalSpace() - root.getFreeSpace();

				String[] space = new String[] { humanReadableByteCount(root.getTotalSpace(), true), humanReadableByteCount(root.getFreeSpace(), true), humanReadableByteCount(usedSpace, true) };

				data.diskSpace.put(root.getAbsolutePath(), space);

			}

			return data;
		} catch (Exception e) {

		}
		return null;
	}

	@Override
	public Html getBigView(Module module) {
		return big.render(module);
	}

	@Override
	public int getRefreshRate() {
		// TODO Auto-generated method stub
		return ONE_SECOND*3;
	}

	@Override
	public void init(Map<String, String> settings, String data) {
		for (String root : settings.keySet()) {
			if (root.startsWith("path")) {
				String path = settings.get(root);
				if (!path.trim().equalsIgnoreCase("")) {
					roots.add(settings.get(root));
				}
			}
		}

	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings, long count) {
		return null;
	}

	@Override
	public String getExternalLink() {
		return null;
	}

	@Override
	public Object saveData() {
		return null;
	}

	@Override
	public void doInBackground(Map<String, String> settings) {
		try {
			OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
			CpuInfo cpu = getCPUInfo(osBean);
			RamInfo ram = getRamInfo(osBean);

			if (settings.containsKey(SETTING_NOTIFICATIONS) && cpuInfo.size() > 0 && ramInfo.size() > 0) {
				CpuInfo oldCpu = cpuInfo.get(cpuInfo.size() - 1);
				RamInfo oldRam = ramInfo.get(ramInfo.size() - 1);

				if ((oldCpu.cpuUsage < WARNING_THRESHOLD && cpu.cpuUsage >= WARNING_THRESHOLD) || (oldRam.percentageUsed < WARNING_THRESHOLD && ram.percentageUsed >= WARNING_THRESHOLD)) {
					Logger.debug("Sending high load warning");
					Notifications.send("Warning", "CPU load (" + nf.format(cpu.cpuUsage) + "%) or Ram load (" + nf.format(ram.percentageUsed) + "%)  became over " + WARNING_THRESHOLD + "%.\n Date: "
							+ new Date());
				}
			}

			cpuInfo.add(cpu);
			ramInfo.add(ram);

			if (cpuInfo.size() > MAX_INFO_SIZE) {
				cpuInfo.remove(0);
			}

			if (ramInfo.size() > MAX_INFO_SIZE) {
				ramInfo.remove(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getBackgroundRefreshRate() {
		return ONE_SECOND;
	}

	@Override
	public int getBigScreenRefreshRate() {
		return ONE_SECOND;
	}

	// ////////////
	// Class method
	// //////////
	private String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Getting data
	 */
	public CpuInfo getCPUInfo(OperatingSystemMXBean osBean) {

		CpuInfo info = new CpuInfo();
		info.cpuUsage = Math.ceil(osBean.getSystemCpuLoad() * 100);
		return info;
	}

	public RamInfo getRamInfo(OperatingSystemMXBean osBean) {
		RamInfo info = new RamInfo();

		info.maxRam = osBean.getTotalPhysicalMemorySize();

		info.availableRam = osBean.getFreePhysicalMemorySize();

		info.usedRam = info.maxRam - info.availableRam;

		info.percentageUsed = Math.ceil((info.usedRam / info.maxRam) * 100);

		

		return info;
	}

	// ///////////////////
	// ////Inner classes
	// //////////////////

	public class SystemInfoData {
		public List<CpuInfo> cpuInfo;
		public List<RamInfo> ramInfo;
		public Hashtable<String, String[]> diskSpace = new Hashtable<String, String[]>();
	}

	public class CpuInfo {
		public double cpuUsage = 0;
	}

	public class RamInfo {
		public double maxRam, availableRam, usedRam, percentageUsed;
	}
}
