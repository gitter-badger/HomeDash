import play.*;
import play.libs.F.Function;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import views.html.login;

import java.lang.reflect.Method;
import java.util.HashMap;

import background.BackgroundTasks;
import models.Setting;

public class Global extends GlobalSettings {

	private BackgroundTasks backgroundTasks;
	
	@Override
	public Action onRequest(final Request request, Method actionMethod) {
		System.out.println("before each request..." + request.toString() + " Method:" + actionMethod.getModifiers());
		// Request req

		if (!request.path().equalsIgnoreCase("/login") && Setting.get(Setting.AUTHENTICATE).equalsIgnoreCase("1")) {
			Logger.info("Checking if cookies are available and correct");

			Cookie cookie = request.cookie("homedash");

			if (cookie == null || (cookie != null && !cookie.value().equalsIgnoreCase(Setting.get(Setting.PASSWORD)))) {
				Logger.info("User not logged in");
				Action simple = new Action.Simple() {

					@Override
					public Promise<Result> call(Context arg0) throws Throwable {

						Promise<Result> promise = Promise.promise(new Function0<Result>() {
							public Result apply() {
								return ok(login.render(request.path(), new HashMap<String, String>()));
							}
						});

						return promise;
					}
				};
				
				return simple;
			}
		}
		return super.onRequest(request, actionMethod);
	}
	
	@Override
	public void onStart(Application arg0) {
		backgroundTasks= new BackgroundTasks();
	}
	
	@Override
	public void onStop(Application arg0) {
		backgroundTasks.shutdown();
		super.onStop(arg0);
	}
}
