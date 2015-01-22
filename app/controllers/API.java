package controllers;

import interfaces.PlugIn;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import models.Module;
import models.Setting;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import websocket.WebSocketMessage;

import com.google.gson.Gson;

public class API extends Controller {
	private static final Gson gson = new Gson();
	private final static String DEVICE = "deviceName", MODULES = "modules";

	/**
	 * Explore current instance, showing device name and its current modules
	 * 
	 * @return
	 */
	public static Result explore() {
		response().setHeader("Access-Control-Allow-Origin", "*");

		String deviceName = Setting.get(Setting.DEVICE_NAME);

		Map<String, Object> result = new Hashtable<String, Object>();

		result.put(DEVICE, deviceName);

		List<ModuleExposed> modules = new ArrayList<ModuleExposed>();
		for (Module module : Application.modules) {
			if (module.remote == Module.LOCAL) {
				ModuleExposed toAdd = new ModuleExposed();
				toAdd.setId(module.id);
				toAdd.setPluginId(module.pluginId);

				Class<?> clazz;
				try {
					clazz = Class.forName(toAdd.getPluginId());

					Constructor<?> ctor = clazz.getConstructor();
					PlugIn plugin = (PlugIn) ctor.newInstance();

					toAdd.setModuleName(plugin.getName());

					modules.add(toAdd);
				} catch (Exception e) {

				}
			}
		}

		result.put(MODULES, modules);

		return ok(gson.toJson(result));
	}

	/**
	 * Send module a refresh message.
	 * 
	 * @param moduleId
	 * @return
	 */
	public static Result refreshModule(int moduleId) {
		response().setHeader("Access-Control-Allow-Origin", "*");
		
		Module module = getModuleFromId(moduleId);

		if (module != null) {
			return ok(gson.toJson(module.refreshModule()));
		} else {
			return notFound("Can't find module with ID:" + moduleId);
		}
	}

	/**
	 * Sends module a big refresh message when displaying a remote module on a
	 * big screen
	 * 
	 * @param moduleId
	 * @param count
	 * @return
	 */
	public static Result bigRefreshModule(int moduleId, long count) {
	    response().setHeader("Access-Control-Allow-Origin", "*"); 

		Module module = getModuleFromId(moduleId);

		if (module != null) {
			return ok(gson.toJson(module.bigScreenRefreshModule(count)));
		} else {
			return notFound("Can't find module with ID:" + moduleId);
		}
	}

	/**
	 * Send a message to a module
	 * 
	 * @param moduleId
	 * @return
	 */
	public static Result sendMessage(int moduleId) {
	    response().setHeader("Access-Control-Allow-Origin", "*"); 

		Module module = getModuleFromId(moduleId);
		Map<String, String[]> post = request().body().asFormUrlEncoded();

		if (module != null) {
			if (post.containsKey(Module.COMMAND) && post.containsKey(Module.METHOD)) {
				String command = post.get(Module.COMMAND)[0];
				String method = post.get(Module.METHOD)[0];
				Logger.info("Received message method:{}, command:{}", method, command);
				WebSocketMessage response = module.processCommand(method, command);
				return ok(response.toJSon());

			} else {

				return notFound("No message defined");
			}
		} else {
			return notFound("No module found");
		}
	}

	private static Module getModuleFromId(int moduleId) {
		Module module = null;
		for (Module m : Application.modules) {
			if (m.id == moduleId) {
				module = m;
				break;
			}
		}

		return module;
	}

	private static class ModuleExposed {
		private int id;
		private String pluginId;
		private String moduleName;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getPluginId() {
			return pluginId;
		}

		public void setPluginId(String pluginId) {
			this.pluginId = pluginId;
		}

		public String getModuleName() {
			return moduleName;
		}

		public void setModuleName(String moduleName) {
			this.moduleName = moduleName;
		}

	}

}
