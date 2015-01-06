package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class ModuleSetting extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public int id;

	public int moduleId;

	public String name;
	public String value;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getModuleId() {
		return moduleId;
	}

	public void setModuleId(int moduleId) {
		this.moduleId = moduleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static Finder<Integer, ModuleSetting> find = new Finder<Integer, ModuleSetting>(
			Integer.class, ModuleSetting.class);

}
