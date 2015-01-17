package controllers;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import models.Setting;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import exceptions.FormException;

public class Login extends Controller{
	public static final String COOKIE_NAME = "homedash", FROM = "from";
	
	
	public static Result login(){
		Map<String, String[]> post = request().body().asFormUrlEncoded();

		String from = post.get(FROM)[0];
		
		if(post.containsKey(Setting.USER_NAME) && post.containsKey(Setting.PASSWORD)){
			String user = post.get(Setting.USER_NAME)[0]+"|"+post.get(Setting.PASSWORD)[0];
			try {
				user = hashString(user);
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				FormException exception = new FormException();
				exception.add(Setting.PASSWORD, "Error while processing request. Please try again later.");
				return ok(views.html.login.render(from, exception.getErrors()));
			}
			
			if(user.equalsIgnoreCase(Setting.get(Setting.PASSWORD))){
				Logger.info("Login successfull !");
				response().setCookie(COOKIE_NAME, user, 36000000);
				return redirect(from);
			}
				
			
		}
		
		Logger.info("Login failed");

		FormException exception = new FormException();
		exception.add(Setting.PASSWORD, "Wrong login or password");
		return ok(views.html.login.render(from, exception.getErrors()));
	}
	
	public static Result logout(){
		response().discardCookie(Login.COOKIE_NAME);
		return redirect("/");
	}
	
	
	public static String hashString(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md5;
		md5 = MessageDigest.getInstance("MD5");
		str += Play.application().configuration().getString("application.secret");

		md5.update(str.getBytes());
		
		byte[] byteData = md5.digest();
		StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        
		return  sb.toString();
	}
	
	
}
