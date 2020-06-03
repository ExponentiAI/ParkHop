package info.fshi.crowdparking.utils;

public abstract class Packet {

	// sourcer
	public static final String KEY_SOURCER_ID = "sourcer_id";
	public static final String KEY_SOURCER_NAME = "name";
	
	// driver
	public static final String KEY_DRIVER_ID = "driver_id";
	public static final String KEY_DIVER_NAME = "name";
	
	// parking lot
	public static final String KEY_PARKING_LOT_ID = "parking_id";
	public static final String KEY_PARKING_LOT_NAME = "name";
	public static final String KEY_PARKING_LOT_DESCRIPTION = "desc";
	public static final String KEY_PARKING_LOT_MIN_BID = "bid";
	public static final String KEY_PARKING_LOT_ADDRESS = "addr";
	public static final String KEY_PARKING_LOT_LATITUDE = "lat";
	public static final String KEY_PARKING_LOT_LONGITUDE = "lon";
	public static final String KEY_PARKING_LOT_PRICE = "price";
	public static final String KEY_PARKING_LOT_GEOCODE = "geo";
	
//	public static final String KEY_PARKING_LOT_QUESTION_POS = "score_pos";
//	public static final String KEY_PARKING_LOT_QUESTION_RES = "score_res";
//	public static final String KEY_PARKING_LOT_QUESTION_LIF = "score_life";
//	public static final String KEY_PARKING_LOT_QUESTION_ENT = "score_ent";

	public static final String KEY_PARKING_LOT_ANSWER = "answer"; // the name of poi
	
	// bid
	public static final String KEY_BID_ID = "bid_id";
	public static final String KEY_BID_BID = "bid";
	
	// login
	public static final String KEY_USER = "username";
	public static final String KEY_PASS = "password";
	
	// login response
	public static final String KEY_USER_ID = "user_id";
	public static final String KEY_USER_TYPE = "user_type";
	public static final String KEY_SESSION_KEY = "session_key";
	
}
