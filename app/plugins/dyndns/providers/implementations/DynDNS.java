package plugins.dyndns.providers.implementations;

public class DynDNS extends StandardProvider{

	@Override
	public String getName() {
		return "Dyn.com";
	}

	@Override
	protected String getUrl() {
		return "http://members.dyndns.org/nic/update";
	}

}
