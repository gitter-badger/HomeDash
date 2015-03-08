package websocket;

import java.lang.reflect.Modifier;

import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.GsonBuilder;

public class WebSocketMessage {
	
	public final static String METHOD_START = "start",
			METHOD_ERROR = "error", METHOD_SUCCESS = "success", METHOD_REFRESH = "refresh", METHOD_CHANGE_PAGE = "changePage", REMOTE_MODULE_NOT_FOUND = "remote404", RELOAD_OTHERS = "reloadOthers";
	
	private String method;
	private Object message, extra;
	private int id;
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Object getMessage() {
		return message;
	}
	public void setMessage(Object message) {
		this.message = message;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public Object getExtra() {
		return extra;
	}
	public void setExtra(Object extra) {
		this.extra = extra;
	}
	public String toJSon(){
		GsonBuilder builder = new GsonFireBuilder().enableExposeMethodResult().createGsonBuilder();
		builder.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE);

		return builder.serializeSpecialFloatingPointValues().create().toJson(this);
		
	}
	
}
