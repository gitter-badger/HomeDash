package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class RemoteFavorite extends Model {

	private static final long serialVersionUID = 6728157298715487380L;

	@Id
	public int id;
	
	public String name, apikey, url;

	public static Finder<Integer, RemoteFavorite> find = new Finder<Integer, RemoteFavorite>(
			Integer.class, RemoteFavorite.class);
	
	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	
	
}
