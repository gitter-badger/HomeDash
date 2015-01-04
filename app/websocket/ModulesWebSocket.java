package websocket;

import interfaces.PlugIn;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.Module;
import play.Logger;
import play.libs.F;
import play.libs.F.Callback0;
import play.mvc.WebSocket;

import com.google.gson.Gson;

import controllers.Application;

public class ModulesWebSocket extends WebSocket<String> {

	private ExecutorService exec;
	private boolean refresh = false;
	private long time = 0;

	Map<Long, WebSocketClient> clients = new Hashtable<Long, ModulesWebSocket.WebSocketClient>();

	private final int THREADS_COUNT = 5;

	private Gson gson = new Gson();

	@Override
	public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {

		WebSocketClient client = new WebSocketClient(in, out, System.currentTimeMillis());

		clients.put(client.id, client);
		time = 0;
		if (clients.size() > 0) {
			startRefresh();
		}

		Logger.info("WebSocket clients {}", clients.size());
	}

	/**
	 * On message
	 */
	public void receiveMessage(long clientId, String message) throws Throwable {

		Logger.info("Received message:{}", message);
		WebSocketMessage socketMessage = gson.fromJson(message, WebSocketMessage.class);

		WebSocketMessage response = new WebSocketMessage();

		if (socketMessage.getMethod().toString().equalsIgnoreCase(WebSocketMessage.METHOD_START)) {
			// startRefresh();
			try {
				clients.get(clientId).page = Double.valueOf(socketMessage.getMessage().toString()).intValue();
				Logger.info("Client [{}] connected on page [{}]", clientId, socketMessage.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(socketMessage.getMethod().toString().equalsIgnoreCase(WebSocketMessage.METHOD_CHANGE_PAGE)){
			clients.get(clientId).page = Double.valueOf(socketMessage.getMessage().toString()).intValue();
			Logger.info("Client [{}] changed page to page [{}]", clientId, socketMessage.getMessage());
		} else {

			for (Module module : Application.modules) {
				if (module.id == socketMessage.getId()) {
					response = module.processCommand(socketMessage.getMethod(), socketMessage.getMessage().toString());
					clients.get(clientId).out.write(response.toJSon());
					return;
				}
			}

		}

	}

	private void startRefresh() {
		if (exec != null) {
			stopRefresh();
		}

		exec = Executors.newFixedThreadPool(THREADS_COUNT);

		refresh = true;
		Logger.info("WEBSOCKET init");

		exec.execute(new Runnable() {

			@Override
			public void run() {
				refreshModules();
			}
		});
	}

	private void stopRefresh() {
		refresh = false;
		exec.shutdownNow();
		exec = null;
		time = 0;

		for (Module module : Application.modules) {
			module.saveData();
		}
	}

	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

	private void refreshModules() {
		try {
			while (refresh) {
				for (final Module module : Application.modules) {

					int refreshRate = module.getPlugin().getRefreshRate();
					if (refreshRate != PlugIn.NO_REFRESH && time % refreshRate == 0) {
						// check the eligible clients to receive message;
						final List<WebSocketClient> eligibleClients = getEligibleClients(module);
						if (eligibleClients.size() > 0) {
							exec.execute(new Runnable() {

								@Override
								public void run() {
									try {
										Logger.info("Refreshing module [{}]", module.id);

										WebSocketMessage response = module.refreshModule();
										response.setMethod("refresh");

										sendToClients(eligibleClients, response.toJSon());
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
						}

					}
				}
				Thread.sleep(1000);
				time += 1000;
				if (time > Integer.MAX_VALUE) {
					time = 0;
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Logger.info("Loop interrupted");
		}

	}

	private List<WebSocketClient> getEligibleClients(Module module) {
		List<WebSocketClient> eligible = new ArrayList<WebSocketClient>();

		for (long id : clients.keySet()) {
			WebSocketClient client = clients.get(id);
			Logger.info("Testing client page[{}] on module [{}] page [{}]", client.page, module.getPlugin().getName(), module.page);
			if (client.page == module.page) {
				eligible.add(client);
			}
		}

		return eligible;
	}

	private void sendToClients(List<WebSocketClient> eligibleClients, String message) {
		Logger.info("Sending message to clients: {}", message);
		for (WebSocketClient client : eligibleClients) {
			client.out.write(message);
		}
	}

	private void sendToClients(String message) {
		for (long id : clients.keySet()) {
			clients.get(id).out.write(message);
		}
	}

	private void removeClient(long id) {
		clients.remove(id);
		Logger.info("WebSocket clients {}", clients.size());
		if (clients.size() == 0) {
			stopRefresh();
		}
	}

	private class WebSocketClient implements F.Callback<String>, Callback0 {

		public WebSocket.In<String> in;
		public WebSocket.Out<String> out;
		public long id;
		public int page = 0;

		public WebSocketClient(play.mvc.WebSocket.In<String> in, play.mvc.WebSocket.Out<String> out, long id) {
			super();
			this.in = in;
			this.out = out;
			this.id = id;

			this.in.onClose(this);
			this.in.onMessage(this);
		}

		@Override
		public void invoke() throws Throwable {
			out.close();
			removeClient(id);

		}

		@Override
		public void invoke(String message) throws Throwable {
			Logger.info("[#{}] Message recieved:", id, message);

			receiveMessage(id, message);
		}
	}

	public void moduleListChanged() {
		WebSocketMessage message = new WebSocketMessage();
		message.setMethod("reload");
		sendToClients(message.toJSon());
	}

}
