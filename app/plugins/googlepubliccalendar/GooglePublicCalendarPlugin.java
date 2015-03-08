package plugins.googlepubliccalendar;

import interfaces.PlugIn;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import misc.HttpTools;
import models.Module;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.twirl.api.Html;
import plugins.googlepubliccalendar.views.html.*;
import websocket.WebSocketMessage;

public class GooglePublicCalendarPlugin implements PlugIn{

	private final String CALENDAR_ID = "calendarId", API_KEY = "apiKey";
	private String calendarId, apiKey, timeZone = TimeZone.getDefault().getID();
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	private SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd");
	private final String URL = "https://www.googleapis.com/calendar/v3/calendars/[ID]/events?orderBy=startTime&singleEvents=true&timeMin=[STARTTIME]&timeZone=[TIMEZONE]&key=[APIKEY]";
	private String url ="";
	@Override
	public String getId() {
		return "googlepubliccalendar";
	}

	@Override
	public boolean hasBigScreen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Google Calendar";
	}
	
	@Override
	public String getDescription() {
		return "View upcoming events from any Google public calendar.";
	}

	@Override
	public Object smallScreenRefresh(Map<String, String> settings) {
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		String url = this.url.replace("[STARTTIME]", URLEncoder.encode(df.format(today.getTime())));
		
		

		
		try {
			JSONObject json = new JSONObject(HttpTools.sendGet(url));
			
			GoogleCalendar calendar = new GoogleCalendar();
			calendar.title = StringEscapeUtils.escapeHtml4(json.getString("summary"));
			calendar.description = StringEscapeUtils.escapeHtml4(json.getString("description"));
			
			JSONArray jsonarray = json.getJSONArray("items");

			for (int i = 0; i < jsonarray.length(); i++) {

				JSONObject jsonEvent = jsonarray.getJSONObject(i);
				
				GoogleCalendarEvent event = new GoogleCalendarEvent();
				event.summary = StringEscapeUtils.escapeHtml4(jsonEvent.getString("summary"));
				event.description = StringEscapeUtils.escapeHtml4(jsonEvent.getString("description"));
				event.link = jsonEvent.getString("htmlLink");
				try{
					Date date = df.parse(jsonEvent.getJSONObject("start").getString("dateTime"));
					event.startTime = df2.format(date);
				}catch(JSONException e){
					event.startTime = jsonEvent.getJSONObject("start").getString("date");
				}
				
				calendar.events.add(event);
				
				
			}
			
			return calendar;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (JSONException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Object bigScreenRefresh(Map<String, String> settings, long count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebSocketMessage processCommand(String method, String command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Html getSmallView(Module module) {
		// TODO Auto-generated method stub
		return small.render(module);
	}

	@Override
	public Html getSettingsView(Module module) {
		// TODO Auto-generated method stub
		return settings.render(module);
	}

	@Override
	public Html getBigView(Module module) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSettings() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getExternalLink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(Map<String, String> settings, String data) {
		
		apiKey = settings.get(API_KEY);
		calendarId = settings.get(CALENDAR_ID);
		url = URL.replace("[ID]", URLEncoder.encode(calendarId)).replace("[APIKEY]", apiKey).replace("[TIMEZONE]", URLEncoder.encode(timeZone));
	}

	@Override
	public boolean hasCss() {
		return true;
	}

	@Override
	public int getRefreshRate() {
		// TODO Auto-generated method stub
		return ONE_HOUR;
	}
	
	private class GoogleCalendar{
		public String title, description;
		public List<GoogleCalendarEvent> events = new ArrayList<GoogleCalendarEvent>();
	}
	
	private class GoogleCalendarEvent{
		public String summary, description, startTime, link;
	}

	@Override
	public Object saveData() {
		return null;
	}

	@Override
	public void doInBackground(Map<String, String>  settings) {
	}
	
	@Override
	public int getBackgroundRefreshRate() {
		return NO_REFRESH;
	}
	
	@Override
	public int getBigScreenRefreshRate() {
		return NO_REFRESH;
	}
	
	
	@Override
	public int getWidth() {
		return 4;
	}
	
	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 4;
	}

}
