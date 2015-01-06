package models;

import interfaces.PlugIn;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import misc.HttpTools;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import controllers.Application;
import play.Logger;
import play.db.ebean.Model;
import websocket.WebSocketMessage;

@Entity
public class Module extends Model implements Comparable<Module> {

	public static final int FULL_SIZE = 12, HALF_SIZE = 6;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public int id;

	public int size = FULL_SIZE;

	public int moduleOrder = 0;

	public int remote = 0;

	public String pluginId;
	
	public int page = 1;

	public static final int REMOTE = 1, LOCAL = 0;
	public static final String REMOTE_API = "api_key", REMOTE_URL=  "url", REMOTE_ID = "id", REMOTE_NAME = "name", METHOD = "method", COMMAND = "command";

	// Json of the data hold by the module.
	@Column(columnDefinition = "TEXT")
	public String data;

	@Transient
	public PlugIn plugin;

	@Transient
	private Map<String, String> settingsMap = new Hashtable<String, String>();

	public static Finder<Integer, Module> find = new ModuleFinder(Integer.class, Module.class);

	public void setSize(int size) {
		this.size = size;
	}

	public void setModuleOrder(int moduleOrder) {
		this.moduleOrder = moduleOrder;
	}

	public PlugIn getPlugin() {
		return plugin;
	}

	public void setPlugin(PlugIn plugin) {
		this.plugin = plugin;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public void setRemote(int remote) {
		this.remote = remote;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setData(String data) {
		this.data = data;
	}

	public static void setFind(Finder<Integer, Module> find) {
		Module.find = find;
	}

	public WebSocketMessage refreshModule() {
		try {
			WebSocketMessage response = new WebSocketMessage();
			if (remote == LOCAL) {
				response.setMethod(WebSocketMessage.METHOD_REFRESH);
				response.setId(id);
				response.setMessage(plugin.smallScreenRefresh(settingsMap));
			} else {
				String url = settingsMap.get(REMOTE_URL) + "api/refreshModule/" + settingsMap.get(REMOTE_ID);
				
				Map<String, String> params = new Hashtable<String, String>();
				params.put(REMOTE_API, settingsMap.get(REMOTE_API));
				
				response = new Gson().fromJson(HttpTools.sendPost(url, params), WebSocketMessage.class);
				response.setId(id);
			}
			return response;
		} catch (Exception e) {
			Logger.error("Error while refreshing module", e);
			WebSocketMessage response = new WebSocketMessage();
			response.setId(id);
			response.setMethod(WebSocketMessage.METHOD_ERROR);
			response.setMessage("Can't refresh module:" + e.getMessage());
			return response;
		}
	}

	public WebSocketMessage bigScreenRefreshModule(long count) {
		try {
			WebSocketMessage response = new WebSocketMessage();
			if (remote == LOCAL) {
				response.setMethod(WebSocketMessage.METHOD_REFRESH);
				response.setId(id);
				response.setMessage(plugin.bigScreenRefresh(settingsMap, count));
			} else {
				String url = settingsMap.get(REMOTE_URL) + "api/bigRefreshModule/" + settingsMap.get(REMOTE_ID) + "/" + count;
				
				Map<String, String> params = new Hashtable<String, String>();
				params.put(REMOTE_API, settingsMap.get(REMOTE_API));
				
				response = new Gson().fromJson(HttpTools.sendPost(url, params), WebSocketMessage.class);
				response.setId(id);
			}
			return response;
		} catch (Exception e) {
			Logger.error("Error while refreshing module", e);
			WebSocketMessage response = new WebSocketMessage();
			response.setId(id);
			response.setMethod(WebSocketMessage.METHOD_ERROR);
			response.setMessage("Can't refresh module:" + e.getMessage());
			return response;
		}
	}

	public void init() {
		try {

			if (plugin == null) {
				Class<?> clazz;
				clazz = Class.forName(pluginId);

				Constructor<?> ctor = clazz.getConstructor();
				plugin = (PlugIn) ctor.newInstance();

			}

			if (remote == LOCAL) {
				this.getPlugin().init(settingsMap, data);
			}

			Logger.info("Module {} ready, {} settings", plugin.getName(), settingsMap.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveData() {
		Object s = plugin.saveData();
		if (s != null) {
			Gson gson = new Gson();
			setData(gson.toJson(s));

			this.save();
		}
	}

	public Map<String, String> getSettingsMap() {
		return settingsMap;
	}

	public void setSettingsMap(Map<String, String> settingsMap) {
		this.settingsMap = settingsMap;
	};

	@Override
	public void save() {
		super.save();

		List<ModuleSetting> oldSettings = ModuleSetting.find.where().ieq("module_id", Integer.toString(id)).findList();
		for (ModuleSetting setting : oldSettings) {
			setting.delete();
		}

		for (String name : settingsMap.keySet()) {
			ModuleSetting setting = new ModuleSetting();
			setting.setModuleId(id);
			setting.setValue(settingsMap.get(name));
			setting.setName(name);

			// setting.delete();
			setting.save();
		}
	}

	@Override
	public void delete() {
		List<ModuleSetting> oldSettings = ModuleSetting.find.where().ieq("module_id", Integer.toString(id)).findList();
		for (ModuleSetting setting : oldSettings) {
			setting.delete();
		}

		super.delete();
	}

	public WebSocketMessage processCommand(String method, String command) {
		WebSocketMessage response = null;
		if (remote == LOCAL) {
			response = plugin.processCommand(method, command);
			response.setId(id);
		} else {
			String url = settingsMap.get(REMOTE_URL) + "api/sendMessage/" + settingsMap.get(REMOTE_ID);
			
			Map<String, String> params = new Hashtable<String, String>();
			params.put(REMOTE_API, settingsMap.get(REMOTE_API));
			params.put(COMMAND, command);
			params.put(METHOD, method);
			try {
				response = new Gson().fromJson(HttpTools.sendPost(url, params), WebSocketMessage.class);
			} catch (JsonSyntaxException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			response.setId(id);
		}
		return response;
	}

	public void doInBackground() {
		plugin.doInBackground(settingsMap);
	}

	@Override
	public int compareTo(Module o) {
		// TODO Auto-generated method stub
		return Integer.valueOf(moduleOrder).compareTo(o.moduleOrder);
	}

	@Override
	public boolean equals(Object arg0) {
		try {
			Module m = (Module) arg0;
			return m.id == id;
		} catch (Exception e) {
			return false;
		}
	}

	public static class ModuleFinder extends Finder<Integer, Module> {

		public ModuleFinder(Class<Integer> arg0, Class<Module> arg1) {
			super(arg0, arg1);
		}

		@Override
		public List<Module> all() {
			List<Module> modules = super.all();
			Logger.info("MODULE: findAll");
			for (Module module : modules) {
				List<ModuleSetting> settings = ModuleSetting.find.where().ieq("module_id", Integer.toString(module.id)).findList();
				for (ModuleSetting setting : settings) {
					module.getSettingsMap().put(setting.name, setting.value);
					Logger.info("Getting setting {} -> {}", setting.name, setting.value);
				}

				module.init();
			}

			return modules;
		}

		@Override
		public Module byId(Integer arg0) {
			Module module = super.byId(arg0);

			List<ModuleSetting> settings = ModuleSetting.find.where().ieq("module_id", Integer.toString(module.id)).findList();
			for (ModuleSetting setting : settings) {
				module.getSettingsMap().put(setting.name, setting.value);
			}

			module.init();

			return module;
		}

		@Override
		public List<Module> findList() {
			List<Module> modules = super.all();

			for (Module module : modules) {
				List<ModuleSetting> settings = ModuleSetting.find.where().ieq("module_id", Integer.toString(module.id)).findList();
				for (ModuleSetting setting : settings) {
					module.getSettingsMap().put(setting.name, setting.value);
					Logger.info("Getting setting {} -> {}", setting.name, setting.value);
				}

				module.init();

			}

			return modules;
		}

	}

}
