package controllers;

import java.io.File;

import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

public class FileCache extends Controller{

	
	public static Result at(String path){
		File f = new File(Play.application().path().getPath()+"/cache/"+path.trim());
				
		if(f.exists()){
			Logger.info("Serving file [{}]", f.getAbsolutePath());
			return ok(f, true);
		}else{
			Logger.info("File [{}] doesn't exist", f.getAbsolutePath());
			return notFound();
		}
	}
}
