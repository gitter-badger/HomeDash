package plugins.portmapper;

import interfaces.PlugIn;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import models.Module;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import play.Logger;
import play.twirl.api.Html;
import views.html.plugins.portmapper.small;
import websocket.WebSocketMessage;

public class PortMapperPlugin implements PlugIn {

	private List<MappingObject> forcedPorts = new ArrayList<>();
	
	private GatewayDevice router;
	private final String METHOD_GET_ROUTER = "getRouter",
			METHOD_GET_MAPPINGS = "getMappings", METHOD_ADDPORT = "addPort",
			METHOD_REMOVE_PORT = "removePort", METHOD_ADDPORTFORCED = "addPortForce", SAVE_PORT = "savePort";

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return "portmapper";
	}

	@Override
	public boolean hasBigScreen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Port Mapper";
	}

	@Override
	public Object smallScreenRefresh(Map<String, String> settings) {
		try {

			return getMappings();
		} catch (IOException | SAXException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings, long count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebSocketMessage processCommand(String method, String command) {
		WebSocketMessage response = new WebSocketMessage();
		if (method.equalsIgnoreCase(METHOD_GET_ROUTER)) {
			try {
				getRouter();
				if (router != null) {
					RouterObject object = new RouterObject();
					object.name = router.getFriendlyName();
					object.externalIp = router.getExternalIPAddress();
					response.setMessage(object);
					response.setMethod(METHOD_GET_ROUTER);

				} else {
					response.setMessage("Can't get router information");
					response.setMethod("error");
				}
			} catch (Exception e) {
				e.printStackTrace();
				response.setMessage("Can't get router information");
				response.setMethod("error");
			}
		} else if (method.equalsIgnoreCase(METHOD_GET_MAPPINGS)) {
			try {
				response.setMethod(METHOD_GET_MAPPINGS);
				response.setMessage(getMappings());
			} catch (Exception e) {
				e.printStackTrace();
				response.setMessage("Can't get mapping information");
				response.setMethod("error");
			}
		} else if (method.equalsIgnoreCase(METHOD_ADDPORT)) {
			try {
				addPort(command, false);
				response.setMethod(METHOD_GET_MAPPINGS);
				response.setMessage(getMappings());
			} catch (Exception e) {
				e.printStackTrace();
				response.setMessage("Error while adding port:" + e.getMessage());
				response.setMethod("error");
			}
		}else if (method.equalsIgnoreCase(METHOD_ADDPORTFORCED)) {
			try {
				addPort(command, true);
				response.setMethod(METHOD_GET_MAPPINGS);
				response.setMessage(getMappings());
			} catch (Exception e) {
				e.printStackTrace();
				response.setMessage("Error while adding port:" + e.getMessage());
				response.setMethod("error");
			}
		}else if (method.equalsIgnoreCase(METHOD_REMOVE_PORT)) {
			try {
				removePort(command);
				response.setMethod(METHOD_GET_MAPPINGS);
				response.setMessage(getMappings());
			} catch (Exception e) {
				e.printStackTrace();
				response.setMessage("Error while Removing port:" + e.getMessage());
				response.setMethod("error");
			}
		}else if (method.equalsIgnoreCase(SAVE_PORT)) {
			try {
				savePort(command);
				response.setMethod(SAVE_PORT);
				response.setMessage(getMappings());
			} catch (Exception e) {
				e.printStackTrace();
				response.setMessage("Error while Removing port:" + e.getMessage());
				response.setMethod("error");
			}
		}
		return response;
	}

	

	@Override
	public Html getSmallView(Module module) {
		// TODO Auto-generated method stub
		return small.render(module);
	}

	@Override
	public Html getSettingsView(Module module) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Html getBigView(Module module) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSettings() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getExternalLink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(Map<String, String> settings, String data) {
		Gson gson = new Gson();
		Type collectionType = new TypeToken<ArrayList<MappingObject>>(){}.getType();
		forcedPorts = gson.fromJson(data, collectionType);
		if(forcedPorts == null){
			forcedPorts = new ArrayList<MappingObject>();
		}
		Logger.info("Loaded {} forced ports", forcedPorts.size());
	}

	@Override
	public boolean hasCss() {
		return true;
	}

	@Override
	public int getRefreshRate() {
		// TODO Auto-generated method stub
		return ONE_MINUTE * 10;
	}

	private List<MappingObject> getMappings() throws IOException, SAXException {
		Logger.info("Refreshing Mapping");
		List<MappingObject> result = new ArrayList<MappingObject>();
		if (router != null) {
			// Integer portMapCount = router.getPortMappingNumberOfEntries();

			PortMappingEntry portMapping = new PortMappingEntry();
			int pmCount = 0;
			do {
				if (router.getGenericPortMappingEntry(pmCount, portMapping)) {
					Logger.info("Portmapping #" + pmCount
							+ " successfully retrieved ("
							+ portMapping.getPortMappingDescription() + ":"
							+ portMapping.getExternalPort() + ")");
					MappingObject object = new MappingObject();
					object.externalPort = portMapping.getExternalPort();
					object.internalPort = portMapping.getInternalPort();
					object.internalIp = portMapping.getInternalClient();
					object.name = portMapping.getPortMappingDescription();
					object.protocol = portMapping.getProtocol();
					
					result.add(object);
					// portMapping = new PortMappingEntry();
				} else {
					Logger.info("Portmapping #" + pmCount + " retrieval failed");
					break;
				}
				pmCount++;
			} while (portMapping != null);
			
			
			//Checking against forced ports
			for(MappingObject mapping:forcedPorts){
				if(!result.contains(mapping)){
					Logger.info("Mapping {} on port {} missing", mapping.protocol, mapping.externalPort);
					router.addPortMapping(mapping.externalPort, mapping.internalPort, mapping.internalIp, mapping.protocol, mapping.name);
					result.add(mapping);
				}else{
					int index = result.indexOf(mapping);
					result.get(index).forced = true;
				}
			}
		} else {
			Logger.info("No router yet");

		}
		
		
		
		return result;
	}

	/**
	 * Getting router info
	 */
	private void getRouter() {
		try {
			GatewayDiscover gatewayDiscover = new GatewayDiscover();
			Map<InetAddress, GatewayDevice> gateways = gatewayDiscover
					.discover();
			router = gatewayDiscover.getValidGateway();
		} catch (IOException | SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void removePort(String message) throws NumberFormatException, IOException, SAXException {
		if(router != null){
			Logger.info("Removing port:{}", message);
			String split[] = message.split("\\|");
			router.deletePortMapping(Integer.parseInt(split[0]), split[1]);
			MappingObject mapping = new MappingObject();
			mapping.externalPort = Integer.parseInt(split[0]);
			mapping.protocol = split[1];
			mapping.forced = true;
			
			if(forcedPorts.contains(mapping)){
				Logger.info("Port in saved list, removing it");
				forcedPorts.remove(mapping);
			}
		}
	}
	
	private void savePort(String message) {
		Logger.info("Saving port:{}", message);
		String split[] = message.split("\\|");
		
		MappingObject mapping = new MappingObject();
		mapping.forced = true;
		mapping.protocol = split[1];
		mapping.externalPort = Integer.parseInt(split[0]);
		mapping.internalPort = Integer.parseInt(split[0]);
		mapping.internalIp = split[2];
		mapping.name = split[3];

		forcedPorts.add(mapping);		
	}

	private void addPort(String message, boolean forced) throws NumberFormatException,
			IOException, SAXException {
		if (router != null) {
			Logger.info("Adding port:{}", message);
			String[] split = message.split("\\|");
			router.addPortMapping(Integer.parseInt(split[2]), Integer.parseInt(split[3]), split[4], split[1], split[0]);
			
			if(forced){
				MappingObject obj = new MappingObject();
				obj.externalPort = Integer.parseInt(split[2]);
				obj.internalPort = Integer.parseInt(split[3]);
				obj.internalIp = split[4];
				obj.protocol = split[1];
				obj.name = split[0];
				obj.forced = true;
				if(!forcedPorts.contains(obj)){
					forcedPorts.add(obj);
					Logger.info("Added ports to forced ports. Size: {}", forcedPorts.size());
				}
			}
		}
	}


	@Override
	public Object saveData() {
		Logger.info("Saving ports, size: {}", forcedPorts.size());
		return forcedPorts;
	}

	@Override
	public void doInBackground(Map<String, String>  settings) {
		Logger.info("Doing in background");
		try {
			getRouter();
			getMappings();
		} catch (IOException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private class MappingObject {
		public String protocol, name, internalIp;
		public int externalPort, internalPort;
		public boolean forced = false;
		@Override
		public boolean equals(Object obj) {
			try{
				MappingObject o = (MappingObject) obj;
				return protocol.equalsIgnoreCase(o.protocol) && externalPort == o.externalPort;
			}catch(Exception e){
				return false;
			}
		}
	}
	
	private class RouterObject {
		public String name, externalIp;
	}
	
	@Override
	public int getBackgroundRefreshRate() {
		return ONE_MINUTE * 10;
	}
	
	@Override
	public int getBigScreenRefreshRate() {
		return NO_REFRESH;
	}
}
