package models;

import interfaces.PlugIn;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.google.gson.Gson;

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

	//Json of the data hold by the module.
    @Column(columnDefinition = "TEXT")
	public String data;
	
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
			response.setMessage(plugin.smallScreenRefresh(settingsMap));
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

			this.getPlugin().init(settingsMap, data);

			Logger.info("Module {} ready, {} settings", plugin.getName(),
					settingsMap.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveData(){
		Object s = plugin.saveData();
		if(s != null){
			Gson gson = new Gson();
			data = gson.toJson(s);
			
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
		for(ModuleSetting setting:oldSettings){
			setting.delete();
		}
		
		for (String name : settingsMap.keySet()) {
			ModuleSetting setting = new ModuleSetting();
			setting.moduleId = id;
			setting.value = settingsMap.get(name);
			setting.name = name;

			// setting.delete();
			setting.save();
		}
	}
	
	
	@Override
	public void delete() {
		List<ModuleSetting> oldSettings = ModuleSetting.find.where().ieq("module_id", Integer.toString(id)).findList();
		for(ModuleSetting setting:oldSettings){
			setting.delete();
		}

		super.delete();
	}

	public WebSocketMessage processCommand(String method, String command) {
		WebSocketMessage response = plugin.processCommand(method, command);
		response.setId(id);

		return response;
	}
	
	
	public void doInBackground(){
		plugin.doInBackground(settingsMap);
	}
	
	@Override
	public int compareTo(Module o) {
		// TODO Auto-generated method stub
		return Integer.valueOf(moduleOrder).compareTo(o.moduleOrder);
	}

	@Override
	public boolean equals(Object arg0) {
		try{
			Module m = (Module) arg0;
			return m.id == id;
		}catch(Exception e){
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

	
}
