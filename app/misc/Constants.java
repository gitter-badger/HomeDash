package misc;

import java.util.Hashtable;
import java.util.Map;


public class Constants {
	public static final Map<String, String> PLUGINS = new Hashtable<String, String>();
	
	static{
		PLUGINS.put("SystemInfo", "plugins.systeminfo.SysteminfoPlugin");
		PLUGINS.put("Transmission", "plugins.transmission.TransmissionPlugin");
		PLUGINS.put("Sickbeard", "plugins.sickbeard.SickbeardPlugin");
		PLUGINS.put("Yamaha Amp", "plugins.yamahaamp.YamahaAmpPlugin");
		PLUGINS.put("Couchpotato", "plugins.couchpotato.CouchpotatoPlugin");
		PLUGINS.put("Google Public Calendar", "plugins.googlepubliccalendar.GooglePublicCalendarPlugin");
		PLUGINS.put("Port Mapoer", "plugins.portmapper.PortMapperPlugin");
	}
}
