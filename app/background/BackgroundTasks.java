package background;

import interfaces.PlugIn;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.Module;
import controllers.Application;

public class BackgroundTasks implements Runnable {
	private ExecutorService exec = Executors.newSingleThreadExecutor();
	private boolean refresh = true;

	public BackgroundTasks() {
		exec.execute(this);
	}

	@Override
	public void run() {
		while (refresh) {
			try {

				for (Module module : Application.modules) {
					module.doInBackground();
				}

				Thread.sleep(PlugIn.TEN_MINUTES);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
