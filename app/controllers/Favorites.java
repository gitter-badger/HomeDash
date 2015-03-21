package controllers;

import models.RemoteFavorite;
import play.mvc.Controller;
import play.mvc.Result;

public class Favorites  extends Controller {
	public static Result addFavortie(String name, String url, String apikey) {
		RemoteFavorite fav = new RemoteFavorite();
		fav.setName(name);
		fav.setApikey(apikey);
		fav.setUrl(url);
		fav.save();
		
		return ok(Integer.toString(fav.id));
	}
	
	public static Result removeFavorite(int id){
		RemoteFavorite favorite = RemoteFavorite.find.byId(id);
		
		if(favorite != null){
			favorite.delete();
			return ok("1");
		}
		
		return ok("-1");
	}
	
	public static Result isFavorite(String url){
		RemoteFavorite favorite = RemoteFavorite.find.where("url = '"+url+"'").findUnique();
		if(favorite != null){
			return ok(Integer.toString(favorite.id));
		}else{
			return ok("-1");
		}
	}

}
