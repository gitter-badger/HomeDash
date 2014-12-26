package background;

import interfaces.PlugIn;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.Module;
import controllers.Application;

public class BackgroundTasks implements Runnable {
	private ExecutorService exec = Executors.newSingleThreadExecutor();
	private boolean refresh = true;
	private long time = 0;

	public BackgroundTasks() {
		exec.execute(this);
	}

	@Override
	public void run() {
		while (refresh) {
			try {

				for (Module module : Application.modules) {
					int refreshRate = module.getPlugin().getBackgroundRefreshRate();
					if (refreshRate != PlugIn.NO_REFRESH && time % refreshRate == 0) {
						module.doInBackground();
						module.saveData();
					}
				}

				Thread.sleep(PlugIn.ONE_SECOND);
				time += 1000;
				if(time > Integer.MAX_VALUE){
					time = 0;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void shutdown() {
		this.refresh = false;
		exec.shutdown();
	}

}
