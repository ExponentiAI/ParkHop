package info.fshi.crowdparking;

import info.fshi.crowdparking.network.WebServerConnector;
import info.fshi.crowdparking.utils.Constants;
import info.fshi.crowdparking.utils.Packet;
import info.fshi.crowdparking.utils.SharedPreferencesUtil;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ParkingLotBidDialog extends Dialog {
	WebServerConnector mWebServerConnector;
	float minBid;
	int parkingId; // id of the bid parking lot
	Context mContext;
	final boolean updateBid;

	public ParkingLotBidDialog(Context context, int pId, float bid, boolean update) {
		super(context);
		mContext = context;
		this.minBid = bid;
		this.parkingId = pId;
		// TODO Auto-generated constructor stub
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_parking_lot_bid);

		updateBid = update;
		
		mWebServerConnector = new WebServerConnector(context);
		
		TextView tvMinBid = (TextView) findViewById(R.id.current_min_bid);
		tvMinBid.setText(Currency.getInstance(Locale.CHINA).getSymbol(Locale.CHINA) + String.valueOf(minBid));
		Button bidButton = (Button) findViewById(R.id.bid_button);
		bidButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText bid = (EditText) findViewById(R.id.editParkingLotBid);
				if(bid.getText().length() > 0){
					DecimalFormat fnum = new DecimalFormat("##0.00");
					float cBid = Float.parseFloat(bid.getText().toString());
					if(cBid >= minBid){
						JSONObject param = new JSONObject();
						try {
							param.put(Packet.KEY_PARKING_LOT_ID, parkingId);
							param.put(Packet.KEY_DRIVER_ID, SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_USER_ID, 0));
							param.put(Packet.KEY_SESSION_KEY, SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_SESSION_KEY, null));
							param.put(Packet.KEY_BID_BID, fnum.format(cBid));
							mWebServerConnector.postDataToServer(Constants.BID_URL, param);
							ParkingLotBidDialog.this.dismiss();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mWebServerConnector.getDataFromServer(Constants.PARKING_LOT_UPDATE_URL);
						if(updateBid)mWebServerConnector.getDataFromServer(Constants.PARKING_BIDS_URL.replace("#", String.valueOf(parkingId)));
					}
					else{
						Toast.makeText(mContext, "Bid must be bigger than min bid " + minBid, Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		Button cancelButton = (Button) findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ParkingLotBidDialog.this.dismiss();
			}
		});
	}
}
