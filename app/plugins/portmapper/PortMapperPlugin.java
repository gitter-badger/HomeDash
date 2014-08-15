package plugins.portmapper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import models.Module;
import play.Logger;
import play.api.Plugin;
import play.twirl.api.Html;
import views.html.plugins.portmapper.small;
import websocket.WebSocketMessage;
import interfaces.PlugIn;

public class PortMapperPlugin implements PlugIn {

	private GatewayDevice router;
	private final String METHOD_GET_ROUTER = "getRouter",
			METHOD_GET_MAPPINGS = "getMappings", METHOD_ADDPORT = "addPort",
			METHOD_REMOVE_PORT = "removePort";

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
	public Object refresh(Map<String, String> settings) {
		try {

			return getMappings();
		} catch (IOException | SAXException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings) {
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
				addPort(command);
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
		}
		return response;
	}

	@Override
	public Html getView(Module module) {
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
	public void init(Map<String, String> settings) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasCss() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getRefreshRate() {
		// TODO Auto-generated method stub
		return PlugIn.TEN_MINUTES;
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
		}
	}

	private void addPort(String message) throws NumberFormatException,
			IOException, SAXException {
		if (router != null) {
			Logger.info("Adding port:{}", message);
			String[] split = message.split("\\|");
			router.addPortMapping(Integer.parseInt(split[2]), Integer.parseInt(split[3]), split[4], split[1], split[0]);
		}
	}

	private class MappingObject {
		public String protocol, name, internalIp;
		public int externalPort, internalPort;
	}

	private class RouterObject {
		public String name, externalIp;
	}
}
