package misc;

import interfaces.PlugIn;

import java.util.ArrayList;
import java.util.List;

import plugins.couchpotato.CouchpotatoPlugin;
import plugins.dyndns.DynDNSPlugin;
import plugins.googlepubliccalendar.GooglePublicCalendarPlugin;
import plugins.harddisk.HardDiskPlugin;
import plugins.lychee.LycheePlugin;
import plugins.osx.OsXPlugin;
import plugins.portmapper.PortMapperPlugin;
import plugins.sickbeard.SickbeardPlugin;
import plugins.systeminfo.SysteminfoPlugin;
import plugins.transmission.TransmissionPlugin;
import plugins.twitter.TwitterPlugin;
import plugins.yamahaamp.YamahaAmpPlugin;


public class Constants {
	public static final List<PlugIn> PLUGINS = new ArrayList<>();
	
	static{
		
		PLUGINS.add(new SysteminfoPlugin());
		PLUGINS.add(new TransmissionPlugin());
		PLUGINS.add(new YamahaAmpPlugin());
		PLUGINS.add(new CouchpotatoPlugin());
		PLUGINS.add(new GooglePublicCalendarPlugin());
		PLUGINS.add(new PortMapperPlugin());
		PLUGINS.add(new OsXPlugin());
		PLUGINS.add(new TwitterPlugin());
		PLUGINS.add(new DynDNSPlugin());
		PLUGINS.add(new PortMapperPlugin());
		PLUGINS.add(new SickbeardPlugin());
		PLUGINS.add(new HardDiskPlugin());
		PLUGINS.add(new LycheePlugin());
	}
}
