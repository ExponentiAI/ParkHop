package info.fshi.crowdparking.utils;

public abstract class Constants {

	public static final double LOCATION_UPDATE_SENSITIVITY = 0.0001;
	public static final double POI_SEARCH_ACCURACY = 0.001;
	
	
	// intent
	public static final String INTENT_PARKING_LOT_INDEX = "intent_parking_lot_index";
	public static final String INTENT_PARKING_LOT_LOCATION = "intent_parking_lot_location";
	public static final String INTENT_PARKING_LOT_ID = "intent_parking_lot_id";
	
	// alarm
	public static final long PARKING_LOT_UPDATE_INTERVAL = 300000;
	public static final long PARKING_LOCATION_UPDATE_INTERVAL = 60000;
	
	// user id used for intent to report parking lot
	public static final String INTENT_CURRENT_USER_ID = "intent_current_user_id";
	
	// web connection
	public static final String SERVER_ADDRESS = "http://192.168.0.113:8000";
	public static final String REPORT_URL = "/parking/report/";
	public static final String BID_URL = "/parking/bid/";
	public static final String LOGIN_URL = "/parking/login/";
	public static final String PARKING_BIDS_URL = "/parking/#/bids/";
	public static final String PARKING_LOT_UPDATE_URL = "index";
	public static final String PARKING_LOT_UPDATE_URL_MY = "/index/my/";
	public static final String PARKING_LOT_CANCEL_BID = "/parking/#/cancel_bid/";
	
	// shared preference
	public static final String SP_USERNAME = "sp_username";
	public static final String SP_PASSWORD = "sp_password";
	public static final String SP_USER_ID = "sp_user_id";
	public static final String SP_SESSION_KEY = "sp_session_key";
	public static final String SP_USER_TYPE = "sp_user_type";
	
	public static final int MAX_QUESTIONS = 8;
	
}
