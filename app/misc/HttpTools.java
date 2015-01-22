package misc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import play.Logger;

public class HttpTools {


	
	public static String sendGet(String url) throws IOException {
		 
		Logger.info("Sending GET request to: {}",url);

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
 
		int responseCode = con.getResponseCode();
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		return response.toString();
 
	}
	
	/*public static String connect(HttpGet httpget) throws IOException {
		Logger.info("Sending GET request to: {}", httpget.getURI().toString());
		String strResponse = null;
		// Prepare a request object
		// HttpGet httpget = new HttpGet(url);

		// Execute the request
		httpget.setHeader("Accept", "application/json");
		HttpResponse response;
			response = httpclient.execute(httpget);
			// Examine the response status
			// Log.i("bgm", response.getStatusLine().getStatusCode());

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {
				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				strResponse = result;
				// now you have the string representation of the HTML request
				instream.close();
			}

		

		return strResponse;
	}*/

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

	/*public static String rawPost(String hostName, int port, String path,
			String requestPath) throws Exception {
		
		
		Request.Post("http://targethost/login")
	    .bodyForm(Form.form().add("username",  "vip").add("password",  "secret").build())
	    .execute().returnContent();
		
		HttpProcessor httpproc = HttpProcessorBuilder.create()
				.add(new RequestContent()).add(new RequestTargetHost())
				.add(new RequestConnControl())
				.add(new RequestUserAgent("Test/1.1"))
				.add(new org.apache.http.protocol.RequestExpectContinue(true))
				.build();

		HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

		HttpCoreContext coreContext = HttpCoreContext.create();
		HttpHost host = new HttpHost(hostName, port);
		coreContext.setTargetHost(host);

		DefaultBHttpClientConnection conn = new DefaultBHttpClientConnection(
				8 * 1024);
		ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE;

		try {

			StringEntity requestBodies = new StringEntity(requestPath,
					ContentType.create("text/plain", Charset.forName("UTF-8")));

			if (!conn.isOpen()) {
				Socket socket = new Socket(host.getHostName(), host.getPort());
				conn.bind(socket);
			}
			BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest(
					"POST", path);
			request.setEntity(requestBodies);
			System.out.println(">> Request URI: "
					+ request.getRequestLine().getUri());

			httpexecutor.preProcess(request, httpproc, coreContext);
			HttpResponse response = httpexecutor.execute(request, conn,
					coreContext);
			httpexecutor.postProcess(response, httpproc, coreContext);

			String responseString = EntityUtils.toString(response.getEntity());

			if (!connStrategy.keepAlive(response, coreContext)) {
				conn.close();
			} else {
				System.out.println("Connection kept alive...");
			}
			
			
			return responseString;

		} finally {
			conn.close();
		}
	}*/
	
	public static String sendPost(String url, Map<String, String> params) throws IOException{
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
		
		StringBuilder urlParameters = new StringBuilder();
		
		int i = 1;
		for(String key: params.keySet()){
			urlParameters.append(key);
			urlParameters.append("=");
			urlParameters.append(URLEncoder.encode(params.get(key)));
			if( i <params.size()){
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
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		return response.toString();
	}
	
	public static String rawPost(String url, String requestBody) throws IOException {
		Logger.info("Sending request to: {} Body:\n{}",url, requestBody);
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
 
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(requestBody);
		wr.flush();
		wr.close();
 
		
		int responseCode = con.getResponseCode();
		
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		return response.toString();
 
	}
	
	
	/*public static String rawPost(String url, String requestBody) throws ClientProtocolException, IOException{
		CloseableHttpClient httpclient = HttpClients.createDefault();
	

		Logger.info("Sending request to: {} Body:\n{}",url, requestBody);
		
		HttpPost httpPost = new HttpPost(url);

		httpPost.setEntity(new StringEntity(requestBody,
				ContentType.create("text/plain", Charset.forName("UTF-8"))));
		CloseableHttpResponse response2 = httpclient.execute(httpPost);

		String result= "";
		try {
			HttpEntity entity2 = response2.getEntity();
			result = convertStreamToString(entity2.getContent());
		    // do something useful with the response body
		    // and ensure it is fully consumed
		    EntityUtils.consume(entity2);
		    
		} finally {
		    response2.close();
		}
		
		return result;

	}*/

}
