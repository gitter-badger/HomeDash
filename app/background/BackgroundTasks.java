package background;

import interfaces.PlugIn;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.Module;

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
				List<Module> modules = Module.find.all();

				for (Module module : modules) {
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
