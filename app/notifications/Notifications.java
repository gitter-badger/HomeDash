package notifications;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import models.Setting;
import notifications.implementations.PushBullet;

public class Notifications {
	
	public static void send(String title, String body){
		if(Setting.get(Setting.PUSH_BULLET).equalsIgnoreCase("1")){
			PushBullet pb = new PushBullet();
			Map<String, String> settings = new HashMap<String, String>();
			settings.put(PushBullet.API_KEY, Setting.get(Setting.PUSH_BULLET_API_KEY));
			System.out.println(settings.get(PushBullet.API_KEY));
			if(pb.setSettings(settings)){
				try {
					pb.sendNotification(title, body);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
