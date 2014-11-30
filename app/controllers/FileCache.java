package controllers;

import java.io.File;

import play.mvc.Controller;
import play.mvc.Result;

public class FileCache extends Controller{

	
	public static Result at(String path){
		File f = new File("cache/"+path);
		
		
		if(f.exists()){
			return ok(f);
		}else{
			return notFound();
		}
	}
}
