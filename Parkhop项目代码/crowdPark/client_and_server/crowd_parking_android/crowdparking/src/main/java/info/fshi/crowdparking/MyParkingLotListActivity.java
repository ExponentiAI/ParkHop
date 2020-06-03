package info.fshi.crowdparking;

import info.fshi.crowdparking.lbs.BaiduMapLocationManager;
import info.fshi.crowdparking.lbs.BaiduMapLocationUpdateAlarm;
import info.fshi.crowdparking.network.ParkingLotUpdateAlarm;
import info.fshi.crowdparking.network.WebServerConnector;
import info.fshi.crowdparking.utils.Constants;
import info.fshi.crowdparking.utils.SharedPreferencesUtil;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.baidu.mapapi.SDKInitializer;

public class MyParkingLotListActivity extends Activity {

	Context mContext;

	WebServerConnector mWebServerConnector;

	private final String TAG = "MyParkingLotListActivity";

	private BaiduMapLocationManager locationManager;

	public static boolean isParkingLotListUpdating = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		SDKInitializer.initialize(getApplicationContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_parking_lot_list);
		mContext = this;

		String sessionKey = SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_SESSION_KEY, null);
		if(sessionKey == null){
			finish();
		}

		mWebServerConnector = new WebServerConnector(mContext);
		ParkingLotList.myParkingLotListAdapter = new MyParkingLotListAdapter(mContext, R.layout.myparkinglot);
		ListView parkingLotLv = (ListView) findViewById(R.id.my_parkinglot_list);
		parkingLotLv.setAdapter(ParkingLotList.myParkingLotListAdapter);

		if(SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_USER_TYPE, 0) == 1){
			parkingLotLv.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(mContext, MyParkingLotActivity.class);
					intent.putExtra(Constants.INTENT_PARKING_LOT_INDEX, position);
					startActivity(intent);
				}
			});
		}
		
		// location manager
		locationManager = new BaiduMapLocationManager(mContext);
		locationManager.startLocationManager();
		locationManager.requestLocationUpdates();
		new BaiduMapLocationUpdateAlarm(mContext, locationManager);
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onResume");
		super.onResume();
		new ParkingLotUpdateAlarm(mContext, mWebServerConnector);
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ParkingLotUpdateAlarm.stopReporting(mContext);
		BaiduMapLocationUpdateAlarm.stopReporting(mContext);
	}

	public static MenuItem progressBar;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_parking_lot_list, menu);
		
		progressBar = menu.findItem(R.id.menu_refresh);
		progressBar.setActionView(R.layout.actionbar_indeterminate_progress);
		progressBar.setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		//if (id == R.id.action_settings) {
		//	return true;
		//}
		if(id == R.id.action_refresh_parking_lot){
			mWebServerConnector.getDataFromServer(Constants.PARKING_LOT_UPDATE_URL_MY);
		}
		return super.onOptionsItemSelected(item);
	}
}