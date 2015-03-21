package plugins.dyndns;

import interfaces.PlugIn;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import models.Module;
import notifications.Notifications;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import play.Logger;
import play.twirl.api.Html;
import plugins.dyndns.inputs.FormInput;
import plugins.dyndns.providers.DynDNSProvider;
import plugins.dyndns.providers.implementations.DynDNS;
import plugins.dyndns.providers.implementations.NoIP;
import plugins.dyndns.views.html.settings;
import plugins.dyndns.views.html.small;
import websocket.WebSocketMessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DynDNSPlugin implements PlugIn {
	private final int OVH = 0, NO_IP = 1, DYN_DNS = 2;

	private final String LAST_UPDATE = "lastUpdate", IP = "ip", PROVIDERS = "providers", METHOD_FORCE_REFRESH = "forceRefresh",  METHOD_GET_FIELDS = "getFields", METHOD_ADD_PROVIDER = "addProvider", METHOD_DELETE_PROVIDER = "deleteProvider";
	private final String SETTING_NOTIFICATIONS = "notifications";
	private String ip = "";
	private List<DynDNSProvider> providers = new ArrayList<>();
	private Date lastUpdate;

	private boolean sendNotifications= false;
	
	private String ipCheckURL = "https://secure.internode.on.net/webtools/showmyip?textonly=1";

	private final Pattern pattern = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
	
	@Override
	public String getId() {
		return "dyndns";
	}

	@Override
	public boolean hasBigScreen() {
		return false;
	}

	@Override
	public String getName() {
		return "Dynamic DNS";
	}
	
	@Override
	public String getDescription() {
		return "Manage your dynamic dns entries from various providers.";
	}

	@Override
	public Object smallScreenRefresh(Map<String, String> settings) {
		System.out.println(providers.size());
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(IP, ip);
		data.put(PROVIDERS, providers);
		data.put(LAST_UPDATE, lastUpdate);
		return data;
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings, long count) {
		return null;
	}

	@Override
	public WebSocketMessage processCommand(String method, String command) {
		WebSocketMessage message = new WebSocketMessage();
		message.setMethod(method);
		switch (method) {
		case METHOD_GET_FIELDS:
			message.setMessage(getFields(command));
			break;
		case METHOD_ADD_PROVIDER:
			if(addProvider(command)){
				message.setMethod(WebSocketMessage.METHOD_SUCCESS);
				message.setMessage("Provider added !");
			}else{
				message.setMethod(WebSocketMessage.METHOD_ERROR);
				message.setMessage("Provider added, but error while updating IP !");
			}
			message.setExtra(providers);
			break;

		case METHOD_DELETE_PROVIDER:
			deleteProvider(command);
			message.setMethod(WebSocketMessage.METHOD_SUCCESS);
			message.setMessage("Provider deleted !");
			message.setExtra(providers);
			break;
			
		case METHOD_FORCE_REFRESH:
			refreshProviders();
			message.setMethod(WebSocketMessage.METHOD_SUCCESS);
			message.setMessage("Refresh complete");
			message.setExtra(providers);
		}
		return message;
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
	public Html getBigView(Module module) {
		return null;
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public String getExternalLink() {
		return null;
	}

	@Override
	public void init(Map<String, String> settings, String data) {
		if (data != null && !data.equalsIgnoreCase("")) {
			Gson gson = new GsonBuilder().create();

			SavedData savedData = gson.fromJson(data, SavedData.class);

			List<SaveFormatProvider> formattedProviders = savedData.providers;
			this.ip = savedData.ip;

			this.lastUpdate = savedData.lastUpdate;
			
			for (SaveFormatProvider saved : formattedProviders) {
				try {
					Class<?> clazz;
					clazz = Class.forName(saved.providerClass);

					Constructor<?> ctor = clazz.getConstructor();
					DynDNSProvider provider = (DynDNSProvider) ctor.newInstance();
					provider.setData(saved.data);

					providers.add(provider);
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					Logger.info("Can't create provider of class: [{}]", saved.providerClass);
				}
			}
			
			Logger.info("Providers loaded size: [{}]", providers.size());
			
			if(settings.containsKey(SETTING_NOTIFICATIONS)){
				sendNotifications = true;
			}
		}
	}

	@Override
	public Object saveData() {

		SavedData data = new SavedData();
		data.ip = ip;
		data.lastUpdate = lastUpdate;

		List<SaveFormatProvider> formattedProviders = new ArrayList<>();
		for (DynDNSProvider provider : providers) {
			SaveFormatProvider saved = new SaveFormatProvider(provider.getClass().getCanonicalName(), provider.getData());
			formattedProviders.add(saved);
		}

		data.providers = formattedProviders;
		return data;
	}

	@Override
	public boolean hasCss() {
		return true;
	}

	@Override
	public void doInBackground(Map<String, String> settings) {
		try {
			
			String ip = getIP();

			
			if (pattern.matcher(ip).matches() && !this.ip.equalsIgnoreCase(ip)) {
				this.ip = ip;
				Logger.info("New IP [{}] updating providers", ip);
				refreshProviders();
				

			} else {
				Logger.info("IP[{}] is the same or not valid, nothing to do", ip);
			}
		} catch (IllegalStateException | IOException e) {
			Logger.error("Can't get external IP", e);
		}
	}

	@Override
	public int getRefreshRate() {
		return ONE_MINUTE * 20;
	}

	@Override
	public int getBackgroundRefreshRate() {
		return ONE_MINUTE;
	}

	@Override
	public int getBigScreenRefreshRate() {
		return 0;
	}
	
	@Override
	public int getWidth() {
		return 4;
	}
	
	@Override
	public int getHeight() {
		return 3;
	}
	
	@Override
	public Map<String, String> exposeSettings(Map<String, String> settings) {
		return null;
	}

	// ////
	// DynDNS Method

	private String getIP() throws IllegalStateException, IOException {
		HttpClient client = HttpClientBuilder.create().build();

		HttpGet get = new HttpGet(ipCheckURL);

		Logger.info("Sending to [{}]");

		HttpResponse response;
		response = client.execute(get);

		
		
		String responseStr = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		Logger.info("Status[{}], Response: [{}]", response.getStatusLine().getStatusCode(), responseStr.trim());

		return responseStr.trim();

	}

	private List<FormInput> getFields(String command) {
		switch (Integer.parseInt(command)) {
		case OVH:
			return new plugins.dyndns.providers.implementations.OVH().getForm();
		case NO_IP:
			return new NoIP().getForm();
		case DYN_DNS:
			return new DynDNS().getForm();

		}

		return new ArrayList<>();
	}

	private boolean addProvider(String command) {
		Logger.info("Adding provider from data:\n {}", command);
		InputWrapper[] inputs = new GsonBuilder().create().fromJson(command, InputWrapper[].class);

		Map<String, String> data = new HashMap<>();

		DynDNSProvider provider = null;
		for (InputWrapper input : inputs) {
			if (input.name.equalsIgnoreCase("ddns-provider")) {
				switch (Integer.parseInt(input.value)) {
				case OVH:
					provider = new plugins.dyndns.providers.implementations.OVH();
					break;
				case NO_IP:
					provider = new NoIP();
					break;
				case DYN_DNS:
					provider = new DynDNS();
					break;
				}
			} else {
				data.put(input.name, input.value);
			}
		}

		if (provider != null) {
			provider.setData(data);
			providers.add(provider);
		}
		
		return provider.updateIP(ip);
	}

	private void deleteProvider(String command) {
		int index = -1;
		for (DynDNSProvider provider : providers) {
			if (provider.getId().equalsIgnoreCase(command)) {
				index = providers.indexOf(provider);
			}
		}

		if (index > -1) {
			providers.remove(index);
		}
	}
	
	private void refreshProviders() {
		StringBuilder builder = new StringBuilder();
		builder.append("IP:"+this.ip+"\n\n");
		builder.append("Update report:\n\n");
		
		for (DynDNSProvider provider : providers) {
			boolean update = provider.updateIP(this.ip);
			Logger.info("Updating [{} - {}], success ? [{}]", provider.getName(), provider.getHostname(), update);
			builder.append("["+provider.getName()+" - "+provider.getHostname()+"], success ? ["+update+"]\n");
		}
		
		lastUpdate = new Date();
		
		if(sendNotifications){
			Notifications.send("DynDNS", builder.toString());
		}
	}

	// // inner class

	private class SaveFormatProvider {
		public String providerClass = "";
		public Map<String, String> data;

		public SaveFormatProvider(String providerClass, Map<String, String> data) {
			super();
			this.providerClass = providerClass;
			this.data = data;
		}
	}

	private class InputWrapper {
		public String name, value;
	}

	private class SavedData {
		public String ip;
		public List<SaveFormatProvider> providers;
		public Date lastUpdate;
	}

}
