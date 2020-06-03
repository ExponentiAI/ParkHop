package info.fshi.crowdparking.lbs;

import info.fshi.crowdparking.utils.Constants;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class BaiduMapLocationUpdateAlarm extends BroadcastReceiver {

	static final String TAG = "BaiduMapLocationUpdateAlarm"; /** for logging */   
	private static WakeLock wakeLock; 
	private static final String WAKE_LOCK = "BaiduMapLocationUpdateWakeLock";

	private static PendingIntent alarmIntent;

	private static long interval;

	private static  BaiduMapLocationManager mLocationManager = null;

	public BaiduMapLocationUpdateAlarm(){}
	
	private Context mContext;

	public BaiduMapLocationUpdateAlarm(Context context, BaiduMapLocationManager locationManager){
		mContext = context;
		mLocationManager = locationManager;
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

		Intent intent = new Intent(context, BaiduMapLocationUpdateAlarm.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmMgr.cancel(alarmIntent);
		mLocationManager.stopLocationManager();
		mLocationManager = null;
		releaseWakeLock();
	}

	/**
	 * Schedules a location update
	 * @param time after how many milliseconds (0 for immediately)?
	 */
	public void scheduleUpdate(Context context, long time) {

		Log.d(TAG, "scheduling a new location update at " + Long.toString( time ));
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, BaiduMapLocationUpdateAlarm.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmMgr.cancel(alarmIntent);

		interval = Constants.PARKING_LOCATION_UPDATE_INTERVAL;
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time, interval, alarmIntent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// start scan
		
		if(mLocationManager != null){
			Log.d(TAG, "location reporting");
			mLocationManager.startLocationManager();
			mLocationManager.requestLocationUpdates();
		}else{
			Log.d(TAG, "location manager null");
		}

		long newInterval = Constants.PARKING_LOCATION_UPDATE_INTERVAL;

		scheduleUpdate(context, System.currentTimeMillis() + newInterval);
	}
	
}
