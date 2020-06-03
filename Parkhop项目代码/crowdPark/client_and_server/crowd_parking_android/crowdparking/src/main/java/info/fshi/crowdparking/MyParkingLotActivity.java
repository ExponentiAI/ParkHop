package info.fshi.crowdparking;

import info.fshi.crowdparking.lbs.BaiduMapLocationManager;
import info.fshi.crowdparking.model.ParkingLot;
import info.fshi.crowdparking.network.WebServerConnector;
import info.fshi.crowdparking.utils.Constants;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MyParkingLotActivity extends Activity {

	Context mContext;

	WebServerConnector mWebServerConnector;

	private final String TAG = "MyParkingLotActivity";

	private ParkingLot parkingLot = null;

	private BaiduMapLocationManager locationManager;

	public static TextView myBid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_my_parking_lot);
		mContext = this;

		myBid = (TextView) findViewById(R.id.my_bid);
		Intent intent = getIntent();
		int index = intent.getIntExtra(Constants.INTENT_PARKING_LOT_INDEX, -1);

		Log.d(TAG, "onCreate " + String.valueOf(index));
		parkingLot = ParkingLotList.myParkingLotList.get(index);

		locationManager = new BaiduMapLocationManager(mContext);
		locationManager.requestLocationUpdates(); // update once it is set

		TextView tvParkingLotName = (TextView) findViewById(R.id.my_parkinglot_name);
		TextView tvParkingLotLoc = (TextView) findViewById(R.id.my_parkinglot_loc);
		TextView tvParkingLotAddr = (TextView) findViewById(R.id.my_parkinglot_addr);
		TextView tvParkingLotDesc = (TextView) findViewById(R.id.my_parkinglot_desc);

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
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_parking_lot, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}

}
