package misc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import play.Logger;

public class HttpTools {

	static {
		initcookies();
	}

	public static String sendGet(String url) throws IOException {

		Logger.info("Sending GET request to: {}", url);

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header

		int responseCode = con.getResponseCode();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		Logger.info(""
				+ "response code: [{}] Response: {}",responseCode, response.toString());
		// print result
		return response.toString();

	}

	/*
	 * public static String connect(HttpGet httpget) throws IOException {
	 * Logger.info("Sending GET request to: {}", httpget.getURI().toString());
	 * String strResponse = null; // Prepare a request object // HttpGet httpget
	 * = new HttpGet(url);
	 * 
	 * // Execute the request httpget.setHeader("Accept", "application/json");
	 * HttpResponse response; response = httpclient.execute(httpget); // Examine
	 * the response status // Log.i("bgm",
	 * response.getStatusLine().getStatusCode());
	 * 
	 * // Get hold of the response entity HttpEntity entity =
	 * response.getEntity(); // If the response does not enclose an entity,
	 * there is no need // to worry about connection release
	 * 
	 * if (entity != null) { // A Simple JSON Response Read InputStream instream
	 * = entity.getContent(); String result = convertStreamToString(instream);
	 * strResponse = result; // now you have the string representation of the
	 * HTML request instream.close(); }
	 * 
	 * 
	 * 
	 * return strResponse; }
	 */

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static void initcookies() {

		CookieManager manager = new CookieManager();
		manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(manager);
	}

	public static String sendPost(String url, Map<String, String> params) throws IOException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		StringBuilder urlParameters = new StringBuilder();

		int i = 1;
		for (String key : params.keySet()) {
			urlParameters.append(key);
			urlParameters.append("=");
			urlParameters.append(URLEncoder.encode(params.get(key)));
			if (i < params.size()) {
				urlParameters.append("&");
			}
		}

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters.toString());
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		Logger.info("\nSending 'POST' request to URL : " + url);
		Logger.info("Post parameters : " + urlParameters);
		Logger.info("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		return response.toString();
	}

	public static String rawPost(String url, String requestBody) throws IOException {
		Logger.info("Sending request to: {} Body:\n{}", url, requestBody);
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(requestBody);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		return response.toString();

	}

	public static String uploadFile(String url, Map<String, String> params, File f) throws UnsupportedEncodingException, IOException {
		Logger.info("\nSending 'POST' request to URL[{}] with file[{}] ", url, f.getAbsolutePath());

		MultipartUtility multipart = new MultipartUtility(url, "UTF-8");

		for (String key : params.keySet()) {
			multipart.addFormField(key, URLEncoder.encode(params.get(key)));
		}

		multipart.addFilePart("fileUpload", f);

		List<String> responses = multipart.finish();

		String response = "";
		for (String line : responses) {
			response += line;
		}

		return response;
	}

}
