package exceptions;

import java.util.Hashtable;
import java.util.Map;

public class FormException extends Exception{
	private Map<String, String> errors = new Hashtable<String, String>();

	public Map<String, String> getErrors() {
		return errors;
	}

	public void setErrors(Map<String, String> errors) {
		this.errors = errors;
	}
	
	public void add(String input, String error){
		errors.put(input, error);
	}
}
