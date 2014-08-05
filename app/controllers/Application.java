package controllers;

import interfaces.PlugIn;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import misc.Constants;
import models.Module;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.addModule;
import views.html.big;
import views.html.edit;
import views.html.index;
import views.html.moduleSettings;
import websocket.BigModuleWebSocket;
import websocket.ModulesWebSocket;

public class Application extends Controller {

	private static ModulesWebSocket ws = new ModulesWebSocket();
	
	public static Result index() {

		
		List<Module> modules = Module.find.all();
		Collections.sort(modules);
		
		/*Logger.info("Controller init");
		for (Module module: modules) {
			try {
				//module.init();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/

		return ok(index.render(modules));
	}

	public static WebSocket<String> socket() {
		return ws;
	}

	public static Result add() {
		return ok(addModule.render(Constants.PLUGINS));
	}

	public static Result addModule(String moduleClass) {
		Class<?> clazz;
		try {
			clazz = Class.forName(moduleClass);

			Constructor<?> ctor = clazz.getConstructor();
			PlugIn plugin = (PlugIn) ctor.newInstance();

			if (!plugin.hasSettings()) {
				Module module = new Module();
				module.pluginId = plugin.getClass().getName();
				module.save();
				
				return redirect("/");

			}else{
				return ok(moduleSettings.render(plugin));
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return redirect("/");

	}
	
	
	public static Result saveModule() {
		Map<String, String[]> values = request().body().asFormUrlEncoded();
		Map<String, String> settings = new java.util.Hashtable<String, String>();
		
		
		for(String key: values.keySet()){
			if(!key.equalsIgnoreCase("class")){
				
				
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
			module.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return redirect("/");
	}
	
	public static Result showBig(int moduleId){
		Module module = Module.find.byId(moduleId);
		module.init();
		return ok(big.render(module));
	}
	
	public static WebSocket<String> bigSocket(int moduleId) {
		return new BigModuleWebSocket(moduleId);
	}
	
	public static Result settings(){
		Map<String, String[]> values = request().body().asFormUrlEncoded();
		
		String[] sizes = values.get("sizes")[0].split("\\|");
		String[] orders = values.get("order")[0].split("\\|");
		
		for(int i = 0; i< sizes.length; i++){
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
		
		return ok();
	}
	
	public static Result editModule(int moduleId){
		return ok(edit.render(Module.find.byId(moduleId)));
	}
	
	public static Result saveEdittedModule(int moduleId){
		Module module = Module.find.byId(moduleId);
		
		Map<String, String[]> values = request().body().asFormUrlEncoded();
		Map<String, String> settings = new java.util.Hashtable<String, String>();
		
		
		for(String key: values.keySet()){
			if(!key.equalsIgnoreCase("class")){
				settings.put(key, values.get(key)[0]);
			}
		}
		
		module.setSettingsMap(settings);
		
		module.save();
		
		return redirect("/");
	}
	
	
}
