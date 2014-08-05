package websocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

import models.Module;
import play.Logger;
import play.libs.F;
import play.libs.F.Callback0;
import play.mvc.WebSocket;

public class BigModuleWebSocket extends WebSocket<String> implements
		F.Callback<String>, Callback0 {

	private Module module;

	private WebSocket.In<String> in;
	private WebSocket.Out<String> out;
	private ExecutorService exec;
	private boolean refresh = false;
	private int time = 0;
	private Gson gson = new Gson();

	public BigModuleWebSocket(int moduleId) {
		module = Module.find.byId(moduleId);
	}

	/**
	 * On close
	 */
	@Override
	public void invoke() throws Throwable {
		refresh = false;
		exec.shutdownNow();
		exec = null;
		out.close();
	}

	/**
	 * On message
	 */
	@Override
	public void invoke(String message) throws Throwable {
		Logger.info("Message recieved:" + message);

		WebSocketMessage socketMessage = gson.fromJson(message,
				WebSocketMessage.class);

		WebSocketMessage response = new WebSocketMessage();

		if (socketMessage.getMessage().toString()
				.equalsIgnoreCase(WebSocketMessage.METHOD_START)) {
			startRefresh();
		} else {

			response = module.processCommand(socketMessage.getMethod(),
					socketMessage.getMessage().toString());
			out.write(response.toJSon());
			return;
		}

	}

	private void startRefresh() {
		exec = Executors.newFixedThreadPool(1);

		refresh = true;
		//module.init();

		exec.execute(new Runnable() {

			@Override
			public void run() {
				refreshModules();
			}
		});
	}

	private void refreshModules() {
		try {
			while (refresh) {
				if (time % module.getPlugin().getRefreshRate() == 0) {
					Logger.info("Refreshing module [{}]", module.id);
					WebSocketMessage response = module.bigScreenRefreshModule();
					response.setMethod("refresh");
					Logger.debug(response.toJSon());
					out.write(response.toJSon());
				}
				Thread.sleep(1000);
				time += 1000;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Logger.info("Refresh interrupted");
		}

	}

	@Override
	public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {

		Logger.info("Module WebSocket for module #{}", module.id);
		this.in = in;

		this.out = out;

		// For each event received on the socket,
		this.in.onMessage(this);

		// When the socket is closed.
		this.in.onClose(this);

	}

}
