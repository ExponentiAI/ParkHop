package info.fshi.crowdparking.network;

import info.fshi.crowdparking.utils.Constants;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class ParkingLotUpdateAlarm extends BroadcastReceiver {

	static final String TAG = "ParkingLotAlarm"; /** for logging */   
	private static WakeLock wakeLock; 
	private static final String WAKE_LOCK = "ParkinglotUpdateWakeLock";

	private static PendingIntent alarmIntent;

	private static long interval;

	private static WebServerConnector mWebServerConnector = null;

	public ParkingLotUpdateAlarm(){}

	public ParkingLotUpdateAlarm(Context context, WebServerConnector webServerConnector){
		mWebServerConnector = webServerConnector;
		scheduleUpdate(context, System.currentTimeMillis());
	}

	/**
	 * Acquire the Wake Lock
	 * @param context
	 */
	public static void getWakeLock(Context context){

		releaseWakeLock();

		PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , WAKE_LOCK); 
		wakeLock.acquire();
	}

	public static void releaseWakeLock(){
		if(wakeLock != null)
			if(wakeLock.isHeld())
				wakeLock.release();
	}


	/**
	 * Stop the scheduled alarm
	 * @param context
	 */
	public static void stopReporting(Context context) {
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, ParkingLotUpdateAlarm.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmMgr.cancel(alarmIntent);

		releaseWakeLock();
	}

	/**
	 * Schedules a location update
	 * @param time after how many milliseconds (0 for immediately)?
	 */
	public void scheduleUpdate(Context context, long time) {

		Log.d(TAG, "scheduling a new parking lot update " + Long.toString( time ));
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, ParkingLotUpdateAlarm.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmMgr.cancel(alarmIntent);

		interval = Constants.PARKING_LOT_UPDATE_INTERVAL;
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time, interval, alarmIntent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// start scan
		if(mWebServerConnector != null){
			Log.d(TAG, "parking lot update");
			mWebServerConnector.getDataFromServer(Constants.PARKING_LOT_UPDATE_URL);
		}

		long newInterval = Constants.PARKING_LOT_UPDATE_INTERVAL;

		scheduleUpdate(context, System.currentTimeMillis() + newInterval);
	}

}
