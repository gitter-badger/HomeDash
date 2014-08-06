package models;

import interfaces.PlugIn;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

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

	public String pluginId;

	@Transient
	private PlugIn plugin;

	@Transient
	private Map<String, String> settingsMap = new Hashtable<String, String>();

	public static Finder<Integer, Module> find = new ModuleFinder(
			Integer.class, Module.class);

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

	public WebSocketMessage refreshModule() {
		try {
			WebSocketMessage response = new WebSocketMessage();
			response.setMethod(WebSocketMessage.METHOD_REFRESH);
			response.setId(id);
			response.setMessage(plugin.refresh(settingsMap));
			return response;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public WebSocketMessage bigScreenRefreshModule() {
		try {
			WebSocketMessage response = new WebSocketMessage();
			response.setMethod(WebSocketMessage.METHOD_REFRESH);
			response.setId(id);
			response.setMessage(plugin.bigScreenRefresh(settingsMap));
			return response;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
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

			this.getPlugin().init(settingsMap);

			Logger.info("Module {} ready, {} settings", plugin.getName(),
					settingsMap.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

		// TODO Auto-generated method stub
		for (String name : settingsMap.keySet()) {
			ModuleSetting setting = new ModuleSetting();
			setting.moduleId = id;
			setting.value = settingsMap.get(name);
			setting.name = name;

			ModuleSetting tmp = ModuleSetting.find.where()
					.ieq("module_id", Integer.toString(setting.moduleId))
					.ieq("name", setting.name).findUnique();
			if (tmp != null)
				tmp.delete();

			// setting.delete();
			setting.save();
		}
	}
	
	
	@Override
	public void delete() {
		for (String name : settingsMap.keySet()) {
			ModuleSetting setting = new ModuleSetting();
			setting.moduleId = id;
			setting.value = settingsMap.get(name);
			setting.name = name;

			ModuleSetting tmp = ModuleSetting.find.where()
					.ieq("module_id", Integer.toString(setting.moduleId))
					.ieq("name", setting.name).findUnique();
			if (tmp != null)
				tmp.delete();
		}

		super.delete();
	}

	public WebSocketMessage processCommand(String method, String command) {
		WebSocketMessage response = plugin.processCommand(method, command);
		response.setId(id);

		return response;
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
				List<ModuleSetting> settings = ModuleSetting.find.where()
						.ieq("module_id", Integer.toString(module.id))
						.findList();
				for (ModuleSetting setting : settings) {
					module.getSettingsMap().put(setting.name, setting.value);
					Logger.info("Getting setting {} -> {}", setting.name,
							setting.value);
				}


				module.init();
			}

			return modules;
		}

		@Override
		public Module byId(Integer arg0) {
			Module module = super.byId(arg0);

			List<ModuleSetting> settings = ModuleSetting.find.where()
					.ieq("module_id", Integer.toString(module.id)).findList();
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
				List<ModuleSetting> settings = ModuleSetting.find.where()
						.ieq("module_id", Integer.toString(module.id))
						.findList();
				for (ModuleSetting setting : settings) {
					module.getSettingsMap().put(setting.name, setting.value);
					Logger.info("Getting setting {} -> {}", setting.name,
							setting.value);
				}
				
				module.init();

			}

			return modules;
		}

	}

	@Override
	public int compareTo(Module o) {
		// TODO Auto-generated method stub
		return Integer.valueOf(moduleOrder).compareTo(o.moduleOrder);
	}

}
