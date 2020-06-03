package info.fshi.crowdparking;

import info.fshi.crowdparking.lbs.BaiduMapLocationManager;
import info.fshi.crowdparking.model.Question;
import info.fshi.crowdparking.network.WebServerConnector;
import info.fshi.crowdparking.utils.Constants;
import info.fshi.crowdparking.utils.Packet;
import info.fshi.crowdparking.utils.SharedPreferencesUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ParkingLotReportDialog extends Dialog {

	private final String TAG = "parkinglotreportdialog";

	WebServerConnector mWebServerConnector;
	BaiduMapLocationManager mLocationManager;

	Context mContext;

	static HashMap<String, Integer> answers = null;
	HashMap<String, Integer> questions = null;

	ArrayList<Question> questionList = null;
	ParkingLotQuestionListAdapter quesitonListAdapter = null;

	public ParkingLotReportDialog(Context context, BaiduMapLocationManager locationManager) {
		super(context);
		// TODO Auto-generated constructor stub
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_parking_lot_report);
		mContext = context;
		mWebServerConnector = new WebServerConnector(context);
		mLocationManager = locationManager;
		questionList = new ArrayList<Question>();

		quesitonListAdapter = new ParkingLotQuestionListAdapter(mContext, R.layout.question, questionList);
		ListView questionLv = (ListView) findViewById(R.id.question_list);
		questionLv.setAdapter(quesitonListAdapter);

		answers = new HashMap<String, Integer>();

		questions = mLocationManager.getQuestions();
		Log.d(TAG, questions.toString());
		Iterator<Entry<String, Integer>> iter = questions.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			Question q = new Question();
			q.content = entry.getKey();
			questionList.add(q);
			answers.put(entry.getKey(), 0);
		}

		quesitonListAdapter.notifyDataSetChanged();

		Button reportButton = (Button) findViewById(R.id.report_button);
		reportButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText parkinglotName = (EditText) findViewById(R.id.editParkingLotName);
				if(parkinglotName.getText().length() > 0){
					String name = parkinglotName.getText().toString();
					EditText parkinglotDesc = (EditText) findViewById(R.id.editParkingLotDesc);
					if(parkinglotDesc.getText().length() > 0){
						String desc = parkinglotDesc.getText().toString();
						EditText minBid = (EditText) findViewById(R.id.editParkingLotBid);
						if(minBid.getText().length() > 0){
							DecimalFormat fnum = new DecimalFormat("##0.00"); 
							String bid = fnum.format(Float.parseFloat(minBid.getText().toString()));
							EditText price = (EditText) findViewById(R.id.editParkingLotPrice);
							if(price.getText().length() > 0){
								String lotPrice = fnum.format(Float.parseFloat(price.getText().toString()));
								// JSON
								JSONObject param = new JSONObject();
								// answers
								StringBuilder answerParam = new StringBuilder();
								Iterator<Entry<String, Integer>> iter = questions.entrySet().iterator();
								boolean isBegin = true;
								while (iter.hasNext()) {
									Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
									if(!isBegin){
										answerParam.append(",");
									}
									answerParam.append(entry.getKey());
									answerParam.append("=");
									if(answers.get(entry.getKey()) == entry.getValue()){
										answerParam.append(1);
									}else{
										answerParam.append(0);
									}
									isBegin = false;
								}

								Log.d(TAG, questions.toString());
								Log.d(TAG, answers.toString());
								System.out.println(answerParam);
								try {
									param.put(Packet.KEY_SOURCER_ID, SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_USER_ID, 0));
									param.put(Packet.KEY_SESSION_KEY, SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_SESSION_KEY, null));
									param.put(Packet.KEY_PARKING_LOT_NAME, name);
									param.put(Packet.KEY_PARKING_LOT_DESCRIPTION, desc);
									param.put(Packet.KEY_PARKING_LOT_MIN_BID, bid);
									param.put(Packet.KEY_PARKING_LOT_PRICE, lotPrice);
									param.put(Packet.KEY_PARKING_LOT_LATITUDE, BaiduMapLocationManager.currentBdLocation.getLatitude());
									param.put(Packet.KEY_PARKING_LOT_LONGITUDE, BaiduMapLocationManager.currentBdLocation.getLongitude());
									param.put(Packet.KEY_PARKING_LOT_ADDRESS, BaiduMapLocationManager.currentBdLocation.getAddrStr());
									param.put(Packet.KEY_PARKING_LOT_ANSWER, answerParam.toString());
									mWebServerConnector.postDataToServer(Constants.REPORT_URL, param);
									ParkingLotReportDialog.this.dismiss();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}
					}
				}
				mWebServerConnector.getDataFromServer(Constants.PARKING_LOT_UPDATE_URL);
			}
		});

		Button cancelButton = (Button) findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ParkingLotReportDialog.this.dismiss();
			}
		});
	}	

}