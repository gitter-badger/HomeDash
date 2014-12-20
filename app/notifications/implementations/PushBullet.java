package notifications.implementations;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import notifications.Notification;

public class PushBullet implements Notification {
	public static final String API_KEY = "api-key", DEVICES = "devices";
	private Map<String, String> settings;

	@Override
	public void sendNotification(String title, String content) throws IOException {
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(settings.get(API_KEY), "".toCharArray());
			}
		});

		String encodedTitle = URLEncoder.encode(title, "UTF-8");
		String encodedContent = URLEncoder.encode(content, "UTF-8");
		
		String url = "https://api.pushbullet.com/v2/pushes";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = "type=note&title="+encodedTitle+"&body="+encodedContent;

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());
	}

	@Override
	public boolean setSettings(Map<String, String> settings) {
		this.settings = settings;
		return settings.containsKey(API_KEY) && !settings.get(API_KEY).trim().equalsIgnoreCase("");
	}

}
