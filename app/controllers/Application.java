package controllers;

import interfaces.PlugIn;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.google.common.io.Files;

import misc.Constants;
import misc.HttpTools;
import models.Module;
import models.Page;
import models.RemoteFavorite;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.WebSocket;
import websocket.BigModuleWebSocket;
import websocket.ModulesWebSocket;
import views.html.*;

public class Application extends Controller {

	public static ModulesWebSocket ws = new ModulesWebSocket();

	public static List<Module> modules = Module.find.all();

	private static final String FILE_CACHE = Play.application().path().getPath() + "/cache/files/";
	public static final Map<Long, BigModuleWebSocket> bigWs = new Hashtable<>();

	public static Result index() {
		Logger.info("index()");
		Collections.sort(modules);

		/*
		 * Logger.info("Controller init"); for (Module module: modules) { try {
		 * //module.init(); } catch (Exception e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } }
		 */

		return ok(index.render(modules, Page.find.all()));
	}

	public static WebSocket<String> socket() {
		return ws;
	}

	public static Result add() {
		Logger.info("add()");
		return ok(addModule.render(Constants.PLUGINS));
	}

	public static Result addRemote() {
		Logger.info("addRemote()");
		return ok(addRemoteModule.render(RemoteFavorite.find.all()));
	}

	public static Result exploreRemoteHost() {
		Logger.info("exploreRemoteHost()");
		Map<String, String[]> values = request().body().asFormUrlEncoded();
		Map<String, String> params = new HashMap<String, String>();

		params.put(Module.REMOTE_API, values.get(Module.REMOTE_API)[0]);

		try {
			String response = HttpTools.sendPost(values.get(Module.REMOTE_URL)[0], params);
			return ok(response);

		} catch (IOException e) {
			return notFound(e.getMessage());
		}

	}

	public static Result addRemoteModule(int page) {
		Logger.info("addRemoteModule({})", page);
		Map<String, String[]> values = request().body().asFormUrlEncoded();

		Module module = new Module();
		module.setRemote(Module.REMOTE);
		module.setPage(page);
		module.setPluginId(values.get("class")[0]);

		Map<String, String> settings = new Hashtable<String, String>();

		String url = values.get(Module.REMOTE_URL)[0];

		if (!url.startsWith("http")) {
			url = "http://" + url;
		}

		if (!url.endsWith("/")) {
			url += "/";
		}

		settings.put(Module.REMOTE_API, values.get(Module.REMOTE_API)[0]);
		settings.put(Module.REMOTE_URL, url);
		settings.put(Module.REMOTE_ID, values.get(Module.REMOTE_ID)[0]);
		settings.put(Module.REMOTE_NAME, values.get(Module.REMOTE_NAME)[0]);

		module.setSettingsMap(settings);
		module.save();
		module.init();

		modules = Module.find.all();

		offsetModulesRowBy(module.getPlugin().getHeight(), module.id);
		ws.moduleListChanged();
		return redirect("/");
	}

	/**
	 * Offset modules row by 'offset' rows except 'except' module id to avoid
	 * grid bugs Used when adding a module, a remote modue, or moving a module
	 * to a new page
	 * 
	 * @param offset
	 * @param except
	 */
	private static void offsetModulesRowBy(int offset, int except) {
		for (Module module : modules) {
			if (module.id != except) {
				module.setRow(module.row + offset);
				module.save();
			}
		}
	}

	public static Result addModule(int page, String moduleClass) {
		Logger.info("addRemoteModule({}, {})", page, moduleClass);
		Class<?> clazz;
		try {
			clazz = Class.forName(moduleClass);

			Constructor<?> ctor = clazz.getConstructor();
			PlugIn plugin = (PlugIn) ctor.newInstance();

			if (!plugin.hasSettings()) {
				Module module = new Module();
				module.setPluginId(plugin.getClass().getName());
				module.setPage(page);
				module.save();

				modules = Module.find.all();
				module.init();
				offsetModulesRowBy(module.getPlugin().getHeight(), module.id);
				ws.moduleListChanged();
				return redirect("/");

			} else {
				return ok(moduleSettings.render(plugin, page));
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return redirect("/");

	}

	public static Result saveModule(int page) {
		Logger.info("saveModule({})", page);
		Map<String, String[]> values = request().body().asFormUrlEncoded();
		Map<String, String> settings = new java.util.Hashtable<String, String>();

		for (String key : values.keySet()) {
			if (!key.equalsIgnoreCase("class")) {

				settings.put(key, values.get(key)[0]);
			}
		}

		Class<?> clazz;
		try {
			clazz = Class.forName(values.get("class")[0]);

			Constructor<?> ctor = clazz.getConstructor();
			PlugIn plugin = (PlugIn) ctor.newInstance();

			Module module = new Module();
			module.setPluginId(plugin.getClass().getName());
			module.setSettingsMap(settings);
			module.setPage(page);
			module.save();
			module.init();
			offsetModulesRowBy(module.getPlugin().getHeight(), module.id);
			modules = Module.find.all();
			ws.moduleListChanged();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return redirect("/");
	}

	public static Result showBig(int moduleId) {
		Logger.info("showBig({})", moduleId);
		Module module = Module.find.byId(moduleId);
		module.init();
		return ok(big.render(module));
	}

	public static WebSocket<String> bigSocket(int moduleId) {
		long id = System.currentTimeMillis();
		bigWs.put(id, new BigModuleWebSocket(moduleId, id));
		return bigWs.get(id);
	}

	public synchronized static Result saveMobileOrder() {
		Logger.info("saveMobileOrder()");
		Map<String, String[]> values = request().body().asFormUrlEncoded();

		String[] orders = values.get("order")[0].split("\\|");

		for (int i = 0; i < orders.length; i++) {
			String[] order = orders[i].split("-");
			int moduleId = Integer.parseInt(order[0]);
			int moduleOrder = Integer.parseInt(order[1]);

			Module module = Module.find.byId(moduleId);
			module.setMobileOrder(moduleOrder);

			Logger.info("Saving module #{}, order:{}", module.id, module.mobileOrder);
			module.update();

		}
		modules = Module.find.all();
		return ok();
	}

	public synchronized static Result saveDesktopOrder() {
		Logger.info("saveDesktopOrder()");
		Map<String, String[]> values = request().body().asFormUrlEncoded();

		String[] orders = values.get("order")[0].split("\\|");

		for (int i = 0; i < orders.length; i++) {
			String[] order = orders[i].split("-");
			int moduleId = Integer.parseInt(order[0]);
			int moduleCol = Integer.parseInt(order[1]);
			int moduleRow = Integer.parseInt(order[2]);

			Module module = Module.find.byId(moduleId);
			module.setRow(moduleRow);
			module.setCol(moduleCol);

			Logger.info("Saving module #{}, col:{}, row:{}", module.id, module.col, module.row);
			module.update();

		}
		modules = Module.find.all();
		return ok();
	}

	public static Result editModule(int moduleId) {
		Logger.info("editModule({})", moduleId);
		return ok(edit.render(Module.find.byId(moduleId)));
	}

	public static Result saveEdittedModule(int moduleId) {
		Logger.info("saveEdittedModule({})", moduleId);
		Module module = Module.find.byId(moduleId);

		Map<String, String[]> values = request().body().asFormUrlEncoded();
		Map<String, String> settings = new java.util.Hashtable<String, String>();

		for (String key : values.keySet()) {
			if (!key.equalsIgnoreCase("class")) {
				settings.put(key, values.get(key)[0]);
			}
		}

		module.setSettingsMap(settings);

		module.update();

		modules = Module.find.all();
		ws.moduleListChanged();

		return redirect("/");
	}

	public static Result deleteModule(int moduleId) {
		Logger.info("deleteModule({})", moduleId);
		Module module = Module.find.byId(moduleId);
		module.delete();

		modules = Module.find.all();
		ws.moduleListChanged();
		return redirect("/");

	}

	public static Result moveModule(int moduleId, int to) {
		Logger.info("moveModule({}, {})", moduleId, to);
		Module module = Module.find.byId(moduleId);

		if (module != null && module.page != to) {
			module.setPage(to);
			module.setMobileOrder(0);
			module.setCol(1);
			module.setRow(1);
			module.update();
			modules = Module.find.all();
			ws.moduleListChanged();
		} else {
			return notFound();
		}

		return ok();
	}

	public static Result uploadFile(long clientId, int moduleId, String method, String message) throws IOException {

		File cache = new File(FILE_CACHE);
		if (!cache.exists()) {
			cache.mkdirs();
		}

		Logger.info("uploadFile({},{},{},{})", clientId, moduleId, method, message);
		MultipartFormData body = request().body().asMultipartFormData();

		List<FilePart> files = new ArrayList<>();
		files.addAll(body.getFiles());
		Logger.info("[{}] Files to send to module[{}]", files.size(), moduleId);
		List<File> filesToSend = new ArrayList<File>();
		for (FilePart file : files) {
			File f = new File(FILE_CACHE + file.getFilename());
			f.deleteOnExit();
			Files.copy(file.getFile(), f);

			filesToSend.add(f);
		}

		ws.sendFileToModule(clientId, moduleId, method, message, filesToSend);

		return ok();
	}

	public static Result uploadFileBig(long clientId, int moduleId, String method, String message) throws IOException {

		File cache = new File(FILE_CACHE);
		if (!cache.exists()) {
			cache.mkdirs();
		}

		Logger.info("uploadFileBig({},{},{},{})", clientId, moduleId, method, message);
		MultipartFormData body = request().body().asMultipartFormData();

		List<FilePart> files = new ArrayList<>();
		files.addAll(body.getFiles());
		Logger.info("[{}] Files to send to module[{}]", files.size(), moduleId);
		List<File> filesToSend = new ArrayList<File>();
		for (FilePart file : files) {
			File f = new File(FILE_CACHE + file.getFilename());
			f.deleteOnExit();
			Files.copy(file.getFile(), f);

			filesToSend.add(f);
		}

		bigWs.get(clientId).sendFileToModule(clientId, moduleId, method, message, filesToSend);

		return ok();
	}
}
