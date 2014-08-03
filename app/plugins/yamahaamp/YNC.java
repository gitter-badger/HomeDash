package plugins.yamahaamp;

import java.util.HashMap;
import java.util.Map;

public class YNC {
	public final static Map<String, YNCRequest> COMMANDS = new HashMap<String, YNCRequest>();
	public final static String POWER_ON = "On", STAND_BY = "Standby",
			TOGGLE_POWER = "On/Standby", GET_POWER_STATUS = "powerStatus", GET_VOLUME = "volume", VOLUME_DOWN = "volumeDown", VOLUME_UP = "volumeUp", GET_INPUT = "getInput";
	
	public final static String HDMI1 = "HDMI1", HDMI2 = "HDMI2", HDMI3 = "HDMI3", HDMI4 = "HDMI4", HDMI5 = "HDMI5", HDMI6 = "HDMI6", HDMI7 = "HDMI7";
	
	
	private final static String GET_PARAM = "GetParam";
	static {

		// POWER ON
		YNCRequest request = new YNCRequest();
		request.type = YNCRequest.TYPE_PUT;
		request.buildRequest(POWER_ON, "Main_Zone", "Power_Control", "Power");
		COMMANDS.put(POWER_ON, request);

		// StandBy
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_PUT;
		request.buildRequest(STAND_BY, "Main_Zone", "Power_Control", "Power");
		COMMANDS.put(STAND_BY, request);

		// TogglePower
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_PUT;
		request.buildRequest(TOGGLE_POWER, "Main_Zone", "Power_Control","Power");
		COMMANDS.put(TOGGLE_POWER, request);

		// TogglePower
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_GET;
		request.buildRequest(GET_PARAM, "Main_Zone", "Power_Control","Power");
		COMMANDS.put(GET_POWER_STATUS, request);

		//GetVolume
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_GET;
		request.buildRequest(GET_PARAM, "Main_Zone", "Volume","Lvl");
		COMMANDS.put(GET_VOLUME, request);
		
		//VolumeDown
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_PUT;
		request.buildRequest("<Val>Down</Val><Exp></Exp><Unit></Unit>", "Main_Zone", "Volume","Lvl");
		COMMANDS.put(VOLUME_DOWN, request);
		
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_PUT;
		request.buildRequest("<Val>Up</Val><Exp></Exp><Unit></Unit>", "Main_Zone", "Volume","Lvl");
		COMMANDS.put(VOLUME_UP, request);
		
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_GET;
		request.buildRequest(GET_PARAM, "Main_Zone", "Input","Input_Sel");
		COMMANDS.put(GET_INPUT, request);
		
		
		
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_PUT;
		request.buildRequest(HDMI1, "Main_Zone", "Input","Input_Sel");
		COMMANDS.put(HDMI1, request);
	
		
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_PUT;
		request.buildRequest(HDMI2, "Main_Zone", "Input","Input_Sel");
		COMMANDS.put(HDMI2, request);
		
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_PUT;
		request.buildRequest(HDMI3, "Main_Zone", "Input","Input_Sel");
		COMMANDS.put(HDMI3, request);
		
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_PUT;
		request.buildRequest(HDMI4, "Main_Zone", "Input","Input_Sel");
		COMMANDS.put(HDMI4, request);
		
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_PUT;
		request.buildRequest(HDMI5, "Main_Zone", "Input","Input_Sel");
		COMMANDS.put(HDMI5, request);
		
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_PUT;
		request.buildRequest(HDMI6, "Main_Zone", "Input","Input_Sel");
		COMMANDS.put(HDMI6, request);
		
		request = new YNCRequest();
		request.type = YNCRequest.TYPE_PUT;
		request.buildRequest(HDMI7, "Main_Zone", "Input","Input_Sel");
		COMMANDS.put(HDMI7, request);
		
		
		
		
		

	}

}
