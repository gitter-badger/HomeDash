package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

import com.google.gson.annotations.Expose;


@Entity
public class Page extends Model{
	
	private static final long serialVersionUID = 90175401985687572L;
	
	@Id
	@Expose public int id;
	@Expose public String name;
	
	public static Finder<Integer, Page> find = new Finder<Integer, Page>(
			Integer.class, Page.class);

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static void setFind(Finder<Integer, Page> find) {
		Page.find = find;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	
}
