package plugins.dyndns.providers.implementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import play.Logger;
import plugins.dyndns.inputs.FormInput;
import plugins.dyndns.providers.DynDNSProvider;

public abstract class StandardProvider implements DynDNSProvider {
	protected transient String username, password, hostname;

	private static final String USERNAME = "username", PASSWORD = "password", HOSTNAME = "hostname";

	protected abstract String getUrl();

	@Override
	public void setData(Map<String, String> data) {
		username = data.get(USERNAME);
		password = data.get(PASSWORD);
		hostname = data.get(HOSTNAME);
	}

	@Override
	public List<FormInput> getForm() {
		List<FormInput> inputs = new ArrayList<>();
		inputs.add(new FormInput(USERNAME, "", "Username", FormInput.TYPE_TEXT));
		inputs.add(new FormInput(PASSWORD, "", "Password", FormInput.TYPE_PASSWORD));
		inputs.add(new FormInput(HOSTNAME, "", "Hostname", FormInput.TYPE_TEXT));
		
		return inputs;
	}
	
	@Override
	public Map<String, String> getData() {
		Map<String, String> data = new HashMap<String, String>();
		data.put(USERNAME, username);
		data.put(PASSWORD, password);
		data.put(HOSTNAME, hostname);
		
		return data;
	}
	
	@Override
	public boolean updateIP(String ip) {

		try {

			String url = this.getUrl() + (this.getUrl().contains("?")?"&":"?") +"hostname=" + this.hostname;
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet get = new HttpGet(url);

			
			//String encoding = Base64.encodeBase64String(("username" + ":" + "password").getBytes());

			//get.setHeader("Authorization", "Basic " + encoding);
			
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
			        AuthScope.ANY,
			        new UsernamePasswordCredentials(username, password));

			// Create AuthCache instance
			AuthCache authCache = new BasicAuthCache();
			// Generate BASIC scheme object and add it to the local auth cache
			

			// Add AuthCache to the execution context
			HttpClientContext context = HttpClientContext.create();
			context.setCredentialsProvider(credsProvider);
			context.setAuthCache(authCache);
			
			get.setHeader("User-Agent", "HomeDash https://github.com/lamarios/HomeDash");
			
			Logger.info("Sending to [{}]", url);

			HttpResponse response;
			response = client.execute(get, context);

			String responseStr = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			Logger.info("Status[{}], Response: [{}]", response.getStatusLine().getStatusCode(), responseStr.trim());

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && (responseStr.contains("nochg") || responseStr.contains("good"))) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Logger.error("Error while updating IP to [{}]", ip);
			return false;
		}
	}
	
	@Override
	public String getId() {
		return getName()+hostname+username;
	}
	
	@Override
	public String getHostname() {
		return hostname;
	}
	
	@Override
	public boolean equals(Object obj) {
		try{
			StandardProvider o = (StandardProvider) obj;
			return this.getId().equalsIgnoreCase(o.getId());
		}catch(Exception e){
			return false;
		}
	}
}
