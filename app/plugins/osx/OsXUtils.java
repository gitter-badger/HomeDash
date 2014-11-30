package plugins.osx;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;

import misc.Utils;

import org.apache.commons.imaging.formats.icns.IcnsImageParser;
import org.xml.sax.SAXException;

import play.Logger;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;

public class OsXUtils {
	private static final int MAX_SIZE = 64;

	private static String executeAppleScript(String script) throws ScriptException, IOException, InterruptedException {
		/*
		 * ScriptEngineManager mgr = new ScriptEngineManager(); ScriptEngine
		 * ascript = mgr.getEngineByName("AppleScript");
		 * System.out.println(ascript); return ascript.eval(script);
		 */

		String[] cmd = { "osascript", "-e", script };
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		String result = "";
		String tmp;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		while ((tmp = bufferedReader.readLine()) != null) {
			result += tmp;
		}

		Logger.info("Command: \n{}", script);
		Logger.info("Result \n{}", result);
		return result;
	}

	public static String[] getDockAppsByPath() throws ScriptException, IOException, InterruptedException {

		StringBuilder script = new StringBuilder();

		script.append("set plistpath to (path to preferences folder as text) & \"com.apple.dock.plist\"\n");

		script.append("tell application \"System Events\"\n");
		script.append("	set plistContents to contents of property list file plistpath\n");
		script.append("set pListItems to value of plistContents\n");
		script.append("end tell\n");
		script.append("set persistentAppsList to |persistent-apps| of pListItems\n");

		script.append("set dockAppsList to {}\n");
		script.append("repeat with thisRecord in persistentAppsList\n");
		script.append("	set end of dockAppsList to _CFURLString of |file-data| of |tile-data| of thisRecord\n");
		script.append("end repeat\n");

		script.append("return dockAppsList\n");

		// System.out.println("Test " + executeAppleScript(script.toString()));

		return executeAppleScript(script.toString()).split("\\,");
	}

	public static String[] getRunningAppsByName() throws ScriptException, IOException, InterruptedException {

		StringBuilder script = new StringBuilder();
		script.append("tell application \"System Events\"\n");
		script.append("set listOfProcesses to (name of every process where background only is false)\n");
		script.append("return listOfProcesses\n");
		script.append("end tell\n");

		return executeAppleScript(script.toString()).split("\\,");

	}

	public static boolean isAppRunning(String appName) throws ScriptException, IOException, InterruptedException {

		String script = "tell application \"System Events\" to (name of processes) contains \"" + appName + "\"";

		return Integer.parseInt(executeAppleScript(script)) == 1;
	}

	public static void startApplication(String appName) throws ScriptException, IOException, InterruptedException {

		String script = "tell application \"" + appName + "\" to activate";

		executeAppleScript(script);
	}

	public static void quitApplication(String appName) throws ScriptException, IOException, InterruptedException {

		String script = "tell application \"" + appName + "\" to quit";

		executeAppleScript(script);
	}

	public static String getIconPath(String plistPath) throws IOException, PropertyListFormatException, ParseException, ParserConfigurationException, SAXException {

		File file = new File(plistPath);
		NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(file);
		String icon = rootDict.objectForKey("CFBundleIconFile").toString();

		return file.getAbsolutePath().replace("info.plist", "Resources/" + icon);
	}

	public static String getAppPath(String appName) throws ScriptException, IOException, InterruptedException {

		String script = "set theApp to \"" + appName + "\"" + "\nset pathToTarget to POSIX path of (path to application theApp)";

		return Utils.removeLastCharacter((String) executeAppleScript(script));
	}

	public static boolean convertIcnsToPng(String iconPath, String pngPath) throws Exception {
		pngPath = System.getProperty("user.dir") + "/" + pngPath;

		if (!iconPath.endsWith(".icns")) {
			iconPath += ".icns";
		}

		IcnsImageParser parser = new IcnsImageParser();
		List<BufferedImage> images = parser.getAllBufferedImages(new File(iconPath));

		BufferedImage image = null;

		// /finding closest image to 64px;
		for (BufferedImage tmp : images) {
			if (tmp.getWidth() <= MAX_SIZE) {
				if (image == null) {
					image = tmp;
				} else if (tmp.getWidth() > image.getWidth()) {
					image = tmp;
				}
			}
		}

		if (image != null) {
			Logger.info("Icon selected {}px", image.getWidth());

			File outputfile = new File(pngPath);
			ImageIO.write(image, "png", outputfile);
		}

		return true;

	}
}
