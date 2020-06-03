package info.fshi.crowdparking;

import info.fshi.crowdparking.lbs.BaiduMapLocationManager;
import info.fshi.crowdparking.model.Bid;
import info.fshi.crowdparking.model.ParkingLot;
import info.fshi.crowdparking.network.WebServerConnector;
import info.fshi.crowdparking.utils.Constants;
import info.fshi.crowdparking.utils.Packet;
import info.fshi.crowdparking.utils.SharedPreferencesUtil;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ParkingLotActivity extends Activity {

	Context mContext;

	WebServerConnector mWebServerConnector;

	private final String TAG = "ParkingLotActivity";

	private ParkingLot parkingLot = null;

	private BaiduMapLocationManager locationManager;

	public static LinearLayout bidsLoading;

	public static TextView myBid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_parking_lot);
		mContext = this;

		bidsLoading = (LinearLayout) findViewById(R.id.bids_loading);
		myBid = (TextView) findViewById(R.id.my_bid);
		Intent intent = getIntent();
		int index = intent.getIntExtra(Constants.INTENT_PARKING_LOT_INDEX, -1);

		Log.d(TAG, "onCreate " + String.valueOf(index));
		parkingLot = ParkingLotList.parkingLotList.get(index);

		locationManager = new BaiduMapLocationManager(mContext);
		locationManager.requestLocationUpdates(); // update once it is set

		TextView tvParkingLotName = (TextView) findViewById(R.id.parkinglot_name);
		TextView tvParkingLotLoc = (TextView) findViewById(R.id.parkinglot_loc);
		TextView tvParkingLotAddr = (TextView) findViewById(R.id.parkinglot_addr);
		TextView tvParkingLotDesc = (TextView) findViewById(R.id.parkinglot_desc);

		tvParkingLotName.setText(parkingLot.name);

		double ptDistance = parkingLot.distance;
		StringBuilder sb = new StringBuilder();
		if(ptDistance < 1000){
			sb.append((int) ptDistance);
			sb.append(" m");
		}else{
			sb.append((int) (ptDistance/1000));
			sb.append(" km");
		}
		tvParkingLotLoc.setText(sb.toString());
		tvParkingLotAddr.setText(parkingLot.address);

		tvParkingLotAddr.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, ParkingLotMapActivity.class);
				double[] loc = {parkingLot.latitude, parkingLot.longitude};
				intent.putExtra(Constants.INTENT_PARKING_LOT_LOCATION, loc);
				startActivity(intent);
			}
		});
		
		tvParkingLotDesc.setText(parkingLot.description);

		if(index < 0){
			this.finish();
		}else{
			mWebServerConnector = new WebServerConnector(mContext);

			if(ParkingLotBidList.parkingLotBidLists.indexOfKey(parkingLot._id) < 0){
				ParkingLotBidList.parkingLotBidLists.append(parkingLot._id, new ArrayList<Bid>());
				ParkingLotBidList.parkingLotBidListAdapters.append(parkingLot._id, new ParkingLotBidListAdapter(mContext, R.layout.parkinglotbid, parkingLot._id));
			}
			ListView parkingLotBidLv = (ListView) findViewById(R.id.parkinglot_bid_list);
			parkingLotBidLv.setAdapter(ParkingLotBidList.parkingLotBidListAdapters.get(parkingLot._id));
			String url = Constants.PARKING_BIDS_URL.replace("#", String.valueOf(parkingLot._id));
			mWebServerConnector.getDataFromServer(url);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.parking_lot, menu);
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
		if (id == R.id.action_bid_parking_lot) {
			ParkingLotBidDialog bidDialog = new ParkingLotBidDialog(mContext, parkingLot._id, parkingLot.mBidding, true);
			bidDialog.show();
		}
		if (id == R.id.action_cancel_bid_parking_lot) {
			JSONObject param = new JSONObject();
			try {
				param.put(Packet.KEY_PARKING_LOT_ID, parkingLot._id);
				param.put(Packet.KEY_DRIVER_ID, SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_USER_ID, 0));
				param.put(Packet.KEY_SESSION_KEY, SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_SESSION_KEY, null));
				mWebServerConnector.postDataToServer(Constants.PARKING_LOT_CANCEL_BID, param);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return super.onOptionsItemSelected(item);
	}

}
