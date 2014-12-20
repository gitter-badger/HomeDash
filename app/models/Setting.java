package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class Setting extends Model {
	private static final long serialVersionUID = 6941203591683647795L;

	public static String AUTHENTICATE = "authenticate", USER_NAME = "username", PASSWORD = "password", PUSH_BULLET = "pushbullet", PUSH_BULLET_API_KEY = "pushbullet_api";

	@Id
	public String name;
	public String value;

	public static Finder<String, Setting> find = new Finder<String, Setting>(String.class, Setting.class);

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

	public static String get(String key) {
		Setting setting = find.byId(key);
		if (setting != null) {
			return setting.value;
		} else {
			return "";
		}
	}

	public static void set(String key, String value) {
		Setting setting = find.byId(key);
		if (setting == null) {
			setting = new Setting();
			setting.setName(key);
		}

		setting.setValue(value);
		setting.save();
	}

}
