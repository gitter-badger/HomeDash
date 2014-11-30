package plugins.osx;

public class OsXApp {
	private String name, path, icnsPath, pngPath;
	
	private boolean running = false;

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getIcnsPath() {
		return icnsPath;
	}

	public void setIcnsPath(String icnsPath) {
		this.icnsPath = icnsPath;
	}

	public String getPngPath() {
		return pngPath;
	}

	public void setPngPath(String pngPath) {
		this.pngPath = pngPath;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	
	
}
