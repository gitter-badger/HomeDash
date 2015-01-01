package controllers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import exceptions.FormException;
import models.Setting;
import notifications.Notifications;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.globalSettings;

public class GlobalSettings extends Controller {

	public static Result index() {
		Map<String, Setting> settings = (Map<String, Setting>) Setting.find.findMap();

		return ok(globalSettings.render(settings, new HashMap<String, String>()));
	}

	public static Result save() {

		Map<String, String[]> post = request().body().asFormUrlEncoded();
		Map<String, String> values = new HashMap<String, String>();

		FormException errors = new FormException();

		for (String key : post.keySet()) {
			values.put(key, post.get(key)[0].trim());
		}

		if (!values.containsKey(Setting.AUTHENTICATE)) {
			values.put(Setting.AUTHENTICATE, "0");
		}

		if (!values.containsKey(Setting.PUSH_BULLET)) {
			values.put(Setting.PUSH_BULLET, "0");
		}

		/**
		 * AUTH VALIDATION
		 */
		String oldAuthValue = Setting.get(Setting.AUTHENTICATE);
		String oldUser = Setting.get(Setting.USER_NAME);
		if (values.get(Setting.AUTHENTICATE).equalsIgnoreCase("1")) {

			if (!values.get(Setting.PASSWORD).trim().equalsIgnoreCase("") && !values.get(Setting.USER_NAME).trim().equalsIgnoreCase("")) {
				// Creating md5 of the password
				try {

					values.put(Setting.PASSWORD, Login.hashString(values.get(Setting.USER_NAME) + "|" + values.get(Setting.PASSWORD)));
					response().discardCookie(Login.COOKIE_NAME);
				} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} else if (
					(oldAuthValue.equalsIgnoreCase("0") && (values.get(Setting.PASSWORD).trim().equalsIgnoreCase("") || values.get(Setting.USER_NAME).trim().equalsIgnoreCase("")))
					|| (!oldUser.equalsIgnoreCase(values.get(Setting.USER_NAME)) && (values.get(Setting.PASSWORD).trim().equalsIgnoreCase("") || values.get(Setting.USER_NAME).trim().equalsIgnoreCase("")))
			) {
				errors.add(Setting.PASSWORD, "User name or password can't be empty");
			} else {
				values.remove(Setting.PASSWORD);
				values.remove(Setting.USER_NAME);
			}
		} else {
			values.remove(Setting.PASSWORD);
			values.remove(Setting.USER_NAME);
		}
		
		/**
		 * PUSH BULLET VALIDATION
		 */
		if (values.get(Setting.PUSH_BULLET).equalsIgnoreCase("1")) {
			if(values.get(Setting.PUSH_BULLET_API_KEY).equalsIgnoreCase("")){
				errors.add(Setting.PUSH_BULLET_API_KEY, "API key can't be empty");
			}
		}
		
		
		if(errors.getErrors().size() == 0){
			for (String key : values.keySet()) {
				Setting.set(key, values.get(key));
			}

			return redirect("/");
		}else{
			Map<String, Setting> settings = (Map<String, Setting>) Setting.find.findMap();
			return ok(globalSettings.render(settings, errors.getErrors()));
		}
	}

	public static Result testNotifications() {
		String title = "Home Dash", body = "This is a test notification from Home Dash";
		Notifications.send(title, body);
		return ok();
	}

	
	public static Result generateAPI(){
		String apiKey;
		try {
			apiKey = Login.hashString(new Date()+Play.application().path().getAbsolutePath()+System.currentTimeMillis());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			return notFound();
		}
		
		return ok(apiKey);
	}
}
