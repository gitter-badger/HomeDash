package controllers;

import interfaces.PlugIn;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import misc.Constants;
import misc.HttpTools;
import models.Module;
import models.Page;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.addModule;
import views.html.addRemoteModule;
import views.html.big;
import views.html.edit;
import views.html.index;
import views.html.moduleSettings;
import websocket.BigModuleWebSocket;
import websocket.ModulesWebSocket;

public class Application extends Controller {

	public static ModulesWebSocket ws = new ModulesWebSocket();

	public static List<Module> modules = Module.find.all();

	public static Result index() {

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
		return ok(addModule.render(Constants.PLUGINS));
	}

	public static Result addRemote() {
		return ok(addRemoteModule.render());
	}

	public static Result exploreRemoteHost() {
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
		Map<String, String[]> values = request().body().asFormUrlEncoded();

		Module module = new Module();
		module.remote = Module.REMOTE;
		module.page = page;
		module.pluginId = values.get("class")[0];

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

		modules = Module.find.all();
		ws.moduleListChanged();
		return redirect("/");
	}

	public static Result addModule(int page, String moduleClass) {
		Class<?> clazz;
		try {
			clazz = Class.forName(moduleClass);

			Constructor<?> ctor = clazz.getConstructor();
			PlugIn plugin = (PlugIn) ctor.newInstance();

			if (!plugin.hasSettings()) {
				Module module = new Module();
				module.pluginId = plugin.getClass().getName();
				module.page = page;
				module.save();

				modules = Module.find.all();
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
			module.pluginId = plugin.getClass().getName();
			module.setSettingsMap(settings);
			module.page = page;
			module.save();

			modules = Module.find.all();
			ws.moduleListChanged();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return redirect("/");
	}

	public static Result showBig(int moduleId) {
		Module module = Module.find.byId(moduleId);
		module.init();
		return ok(big.render(module));
	}

	public static WebSocket<String> bigSocket(int moduleId) {
		return new BigModuleWebSocket(moduleId);
	}

	public static Result settings() {
		Map<String, String[]> values = request().body().asFormUrlEncoded();

		String[] sizes = values.get("sizes")[0].split("\\|");
		String[] orders = values.get("order")[0].split("\\|");

		for (int i = 0; i < sizes.length; i++) {
			String[] size = sizes[i].split("-");
			String[] order = orders[i].split("-");
			int moduleId = Integer.parseInt(size[0]);
			int moduleSize = Integer.parseInt(size[1]);
			int moduleOrder = Integer.parseInt(order[1]);

			Module module = Module.find.byId(moduleId);
			module.setSize(moduleSize);
			module.setModuleOrder(moduleOrder);

			Logger.info("Saving module #{}, size:{}, order:{}", module.id, module.size, module.moduleOrder);
			module.save();

		}
		modules = Module.find.all();
		return ok();
	}

	public static Result editModule(int moduleId) {
		return ok(edit.render(Module.find.byId(moduleId)));
	}

	public static Result saveEdittedModule(int moduleId) {
		Module module = Module.find.byId(moduleId);

		Map<String, String[]> values = request().body().asFormUrlEncoded();
		Map<String, String> settings = new java.util.Hashtable<String, String>();

		for (String key : values.keySet()) {
			if (!key.equalsIgnoreCase("class")) {
				settings.put(key, values.get(key)[0]);
			}
		}

		module.setSettingsMap(settings);

		module.save();

		modules = Module.find.all();
		ws.moduleListChanged();

		return redirect("/");
	}

	public static Result deleteModule(int moduleId) {
		Module module = Module.find.byId(moduleId);
		module.delete();

		modules = Module.find.all();
		ws.moduleListChanged();
		return redirect("/");

	}

	public static Result moveModule(int moduleId, int to) {
		Module module = Module.find.byId(moduleId);

		if (module != null && module.page != to) {
			module.page = to;
			module.moduleOrder = 0;
			module.size = 12;
			module.save();
			modules = Module.find.all();
			ws.moduleListChanged();
		} else {
			return notFound();
		}

		return ok();
	}

}
