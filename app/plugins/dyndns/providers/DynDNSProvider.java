package plugins.dyndns.providers;

import java.util.List;
import java.util.Map;

import com.github.julman99.gsonfire.annotations.ExposeMethodResult;
import com.google.gson.annotations.Expose;

import plugins.dyndns.inputs.FormInput;




public interface DynDNSProvider {
	public boolean updateIP(String ip);
	public List<FormInput> getForm();
	public void setData(Map<String, String> data);

	@ExposeMethodResult("data")
	public Map<String, String> getData();
	
	@ExposeMethodResult("name")
	public String getName();
	
	@ExposeMethodResult("id")
	public String getId();
	
	@ExposeMethodResult("hostname")
	public String getHostname();
}
