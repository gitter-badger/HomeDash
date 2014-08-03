package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class ModuleSetting extends Model{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public int id;
	
	public int moduleId;
	
	
	public String name;
	public String value;

	
	public static Finder<Integer, ModuleSetting> find = new Finder<Integer, ModuleSetting>(
			Integer.class, ModuleSetting.class);

}
