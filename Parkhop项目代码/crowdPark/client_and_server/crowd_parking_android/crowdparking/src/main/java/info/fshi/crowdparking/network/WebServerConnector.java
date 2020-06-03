package info.fshi.crowdparking.network;

import info.fshi.crowdparking.MyParkingLotListActivity;
import info.fshi.crowdparking.ParkingLotActivity;
import info.fshi.crowdparking.ParkingLotBidList;
import info.fshi.crowdparking.ParkingLotList;
import info.fshi.crowdparking.ParkingLotListActivity;
import info.fshi.crowdparking.auth.LoginActivity;
import info.fshi.crowdparking.lbs.BaiduMapLocationManager;
import info.fshi.crowdparking.model.Bid;
import info.fshi.crowdparking.model.ParkingLot;
import info.fshi.crowdparking.utils.Constants;
import info.fshi.crowdparking.utils.Packet;
import info.fshi.crowdparking.utils.SharedPreferencesUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;


public class WebServerConnector {

	//private final static String TAG = "webserverconnector";

	Context mContext;

	public WebServerConnector(Context context){
		mContext = context;
	}
	/**
	 * 此类允许进行后台操作并在UI线程上发布结果，而无需操作线程和/或处理程序
	 * @author Administrator
	 */
	private class userLoginTask extends AsyncTask<JSONObject, Void, String> {

		protected String doInBackground(JSONObject... data) {

			JSONObject param = data[0];
			// init the counter
			StringBuilder requestUrl = new StringBuilder
					(Constants.SERVER_ADDRESS);
			requestUrl.append(Constants.LOGIN_URL);
			//利用http协议传递消息
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(requestUrl.toString());
			String sessionKey = null;
			int type;
			int id;
			try {
				HttpResponse httpResponse;

				// send request
				try {
					// extract geo location

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_USER, param.getString(Packet.KEY_USER)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_PASS, param.getString(Packet.KEY_PASS)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_USER_TYPE, param.getString(Packet.KEY_USER_TYPE)));
					HttpEntity se = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
					httppost.setEntity(se);
					httpResponse = httpclient.execute(httppost);
					// handle response
					JSONObject json = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
					if(json.length() > 0){
						id = json.getInt(Packet.KEY_USER_ID);
						type = json.getInt(Packet.KEY_USER_TYPE);
						sessionKey = json.getString(Packet.KEY_SESSION_KEY);
						SharedPreferencesUtil.savePreferences(mContext, Constants.SP_USER_ID, id);
						SharedPreferencesUtil.savePreferences(mContext, Constants.SP_SESSION_KEY, sessionKey);
						SharedPreferencesUtil.savePreferences(mContext, Constants.SP_USER_TYPE, type);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
			}
			finally {
				httppost.abort();
			}
			return sessionKey;

		}

		@Override
		protected void onPostExecute(String sessionKey) {
			// TODO Auto-generated method stub
			if(sessionKey != null){
				// successfully registerd
				LoginActivity.loginLoadingDialog.dismiss();
				Intent i = new Intent(mContext, ParkingLotListActivity.class);
				mContext.startActivity(i);
			}else{
				LoginActivity.loginLoadingDialog.dismiss();
				Toast.makeText(mContext, "登陆失败", Toast.LENGTH_LONG).show();
			}
		}
	}

	private class reportParkingLotTask extends AsyncTask<JSONObject, Void, Integer> {
		protected Integer doInBackground(JSONObject... data) {
			JSONObject param = data[0];
			// init the counter
			StringBuilder requestUrl = new StringBuilder(Constants.SERVER_ADDRESS);
			requestUrl.append(Constants.REPORT_URL);
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(requestUrl.toString());

			Integer retSrc = 0;

			try {
				HttpResponse httpResponse;

				// send request
				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_SOURCER_ID, param.getString(Packet.KEY_SOURCER_ID)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_SESSION_KEY, param.getString(Packet.KEY_SESSION_KEY)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_PARKING_LOT_NAME, param.getString(Packet.KEY_PARKING_LOT_NAME)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_PARKING_LOT_DESCRIPTION, param.getString(Packet.KEY_PARKING_LOT_DESCRIPTION)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_PARKING_LOT_MIN_BID, param.getString(Packet.KEY_PARKING_LOT_MIN_BID)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_PARKING_LOT_PRICE, param.getString(Packet.KEY_PARKING_LOT_PRICE)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_PARKING_LOT_ADDRESS, param.getString(Packet.KEY_PARKING_LOT_ADDRESS)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_PARKING_LOT_LATITUDE, param.getString(Packet.KEY_PARKING_LOT_LATITUDE)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_PARKING_LOT_LONGITUDE, param.getString(Packet.KEY_PARKING_LOT_LONGITUDE)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_PARKING_LOT_ANSWER, param.getString(Packet.KEY_PARKING_LOT_ANSWER)));
					HttpEntity se = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
					httppost.setEntity(se);
					httpResponse = httpclient.execute(httppost);
					// handle response
					retSrc = Integer.parseInt(EntityUtils.toString(httpResponse.getEntity()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
			}
			finally {
				httppost.abort();
			}
			return retSrc;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			if(result.intValue() == 0){
				Toast.makeText(mContext, "Connection failed", Toast.LENGTH_LONG).show();
			}else if(result.intValue() == 1){
				Toast.makeText(mContext, "Successfully sent", Toast.LENGTH_LONG).show();
			}
		}
	}

	private class bidParkingLotTask extends AsyncTask<JSONObject, Void, Integer> {
		protected Integer doInBackground(JSONObject... data) {
			JSONObject param = data[0];
			// init the counter
			StringBuilder requestUrl = new StringBuilder(Constants.SERVER_ADDRESS);
			requestUrl.append(Constants.BID_URL);
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(requestUrl.toString());
			Integer retSrc = 0;

			try {
				HttpResponse httpResponse;
				// send request
				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_PARKING_LOT_ID, param.getString(Packet.KEY_PARKING_LOT_ID)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_SESSION_KEY, param.getString(Packet.KEY_SESSION_KEY)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_DRIVER_ID, param.getString(Packet.KEY_DRIVER_ID)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_BID_BID, param.getString(Packet.KEY_BID_BID)));
					HttpEntity se = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
					httppost.setEntity(se);
					httpResponse = httpclient.execute(httppost);
					// handle response
					retSrc = Integer.parseInt(EntityUtils.toString(httpResponse.getEntity()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
			}
			finally {
				httppost.abort();
			}
			return retSrc;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			if(result.intValue() == 0){
				Toast.makeText(mContext, "Connection failed", Toast.LENGTH_LONG).show();
			}else if(result.intValue() == 1){
				Toast.makeText(mContext, "Successfully sent", Toast.LENGTH_LONG).show();
			}
		}
	}

	private class cancelBidParkingLotTask extends AsyncTask<JSONObject, Void, Integer> {
		int parkingId;
		protected Integer doInBackground(JSONObject... data) {
			JSONObject param = data[0];
			// init the counter
			StringBuilder requestUrl = new StringBuilder(Constants.SERVER_ADDRESS);
			try {
				parkingId = Integer.parseInt(param.getString(Packet.KEY_PARKING_LOT_ID));
				requestUrl.append(Constants.PARKING_LOT_CANCEL_BID.replace("#", String.valueOf(parkingId)));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(requestUrl.toString());
			Integer retSrc = 0;

			try {
				HttpResponse httpResponse;
				// send request
				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_SESSION_KEY, param.getString(Packet.KEY_SESSION_KEY)));
					nameValuePairs.add(new BasicNameValuePair(Packet.KEY_DRIVER_ID, param.getString(Packet.KEY_DRIVER_ID)));
					HttpEntity se = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
					httppost.setEntity(se);
					httpResponse = httpclient.execute(httppost);
					// handle response
					retSrc = Integer.parseInt(EntityUtils.toString(httpResponse.getEntity()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
			}
			finally {
				httppost.abort();
			}
			return retSrc;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			if(result.intValue() == 0){
				Toast.makeText(mContext, "Connection failed", Toast.LENGTH_LONG).show();
			}else if(result.intValue() == 1){
				Toast.makeText(mContext, "Canceled", Toast.LENGTH_LONG).show();
			}
			getDataFromServer(Constants.PARKING_BIDS_URL.replace("#", String.valueOf(parkingId)));
		}
	}

	public void postDataToServer(String url, JSONObject param){

		if(url.equalsIgnoreCase(Constants.REPORT_URL)){
			new reportParkingLotTask().execute(param);
		}
		if(url.equalsIgnoreCase(Constants.BID_URL)){
			new bidParkingLotTask().execute(param);
		}
		if(url.equalsIgnoreCase(Constants.LOGIN_URL)){
			new userLoginTask().execute(param);
		}
		if(url.equalsIgnoreCase(Constants.PARKING_LOT_CANCEL_BID)){
			new cancelBidParkingLotTask().execute(param);
		}
	}

	private class getAllParkingLots extends AsyncTask<Void, Void, Boolean> {

		ArrayList<ParkingLot> newParkingLots = new ArrayList<ParkingLot>(); 

		protected Boolean doInBackground(Void... data) {
			StringBuilder requestUrl = new StringBuilder(Constants.SERVER_ADDRESS);
			requestUrl.append("/").append("index").append("/");
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet requestTypeList = new HttpGet(requestUrl.toString());
			boolean retSrc = true;
			try {
				HttpResponse response = httpclient.execute(requestTypeList);
				// send request
				JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
				Iterator<?> keysIterator = json.keys();
				//					ParkingLotList.init();
				while (keysIterator.hasNext())
				{
					String id = keysIterator.next().toString();
					ParkingLot parkingLot = new ParkingLot();
					parkingLot._id = Integer.valueOf(id);
					JSONObject info = json.getJSONObject(id);
					parkingLot.name = info.getString("name");
					parkingLot.address = info.getString("addr");
					parkingLot.description = info.getString("desc");
					parkingLot.mBidding = (float) info.getDouble("bid");
					
					parkingLot.latitude = (float) info.getDouble("lat");
					parkingLot.longitude = (float) info.getDouble("lon");
					
					if(info.has("price")){
						parkingLot.price = (float) info.getDouble("price");
					}
					
					if(info.has("cred")){
						parkingLot.credibility = (float) info.getDouble("cred");
					}
					
					LatLng pt_parkinglot = new LatLng(parkingLot.latitude, parkingLot.longitude);
					if(BaiduMapLocationManager.currentBdLocation != null){
						LatLng pt_myposition = new LatLng(BaiduMapLocationManager.currentBdLocation.getLatitude(), BaiduMapLocationManager.currentBdLocation.getLongitude());
						double ptDistance = DistanceUtil.getDistance(pt_myposition, pt_parkinglot);
						parkingLot.distance = ptDistance;
					}
					newParkingLots.add(parkingLot);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retSrc = false;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retSrc = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retSrc = false;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retSrc = false;
			} finally {
				requestTypeList.abort();
			}
			return retSrc;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			ParkingLotList.update(newParkingLots);
			ParkingLotList.parkingLotListAdapter.sortList();
			ParkingLotList.parkingLotListAdapter.notifyDataSetChanged();
			ParkingLotListActivity.progressBar.setVisible(false);
		}
	}
	
	private class getMyParkingLots extends AsyncTask<Void, Void, Boolean> {

		ArrayList<ParkingLot> newParkingLots = new ArrayList<ParkingLot>(); 

		protected Boolean doInBackground(Void... data) {
			StringBuilder requestUrl = new StringBuilder(Constants.SERVER_ADDRESS);
			requestUrl.append("/").append("index/my/");
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet requestTypeList = new HttpGet(requestUrl.toString());
			boolean retSrc = true;
			try {
				HttpResponse response = httpclient.execute(requestTypeList);
				// send request
				JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
				Iterator<?> keysIterator = json.keys();
				//					ParkingLotList.init();
				while (keysIterator.hasNext())
				{
					String id = keysIterator.next().toString();
					ParkingLot parkingLot = new ParkingLot();
					parkingLot._id = Integer.valueOf(id);
					JSONObject info = json.getJSONObject(id);
					parkingLot.name = info.getString("name");
					parkingLot.address = info.getString("addr");
					parkingLot.description = info.getString("desc");
					parkingLot.mBidding = (float) info.getDouble("bid");
					
					parkingLot.latitude = (float) info.getDouble("lat");
					parkingLot.longitude = (float) info.getDouble("lon");
					
					if(info.has("price")){
						parkingLot.price = (float) info.getDouble("price");
					}
					
					if(info.has("cred")){
						parkingLot.credibility = (float) info.getDouble("cred");
					}
					
					LatLng pt_parkinglot = new LatLng(parkingLot.latitude, parkingLot.longitude);
					if(BaiduMapLocationManager.currentBdLocation != null){
						LatLng pt_myposition = new LatLng(BaiduMapLocationManager.currentBdLocation.getLatitude(), BaiduMapLocationManager.currentBdLocation.getLongitude());
						double ptDistance = DistanceUtil.getDistance(pt_myposition, pt_parkinglot);
						parkingLot.distance = ptDistance;
					}
					newParkingLots.add(parkingLot);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retSrc = false;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retSrc = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retSrc = false;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retSrc = false;
			} finally {
				requestTypeList.abort();
			}
			return retSrc;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			ParkingLotList.updateMyParkingLotList(newParkingLots);
			ParkingLotList.myParkingLotListAdapter.sortList();
			ParkingLotList.myParkingLotListAdapter.notifyDataSetChanged();
			MyParkingLotListActivity.progressBar.setVisible(false);
		}
	}

	private class getAllBids extends AsyncTask<String, Void, Boolean> {

		private int parkingId;
		private float myBid;

		protected Boolean doInBackground(String... data) {
			StringBuilder requestUrl = new StringBuilder(Constants.SERVER_ADDRESS);
			requestUrl.append(data[0]);
			parkingId = Integer.parseInt(data[0].split("/")[2]);
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet requestTypeList = new HttpGet(requestUrl.toString());
			boolean retSrc = true;

			try {
				HttpResponse response = httpclient.execute(requestTypeList);

				ParkingLotBidList.invalidate(parkingId);
				// send request
				JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
				Iterator<?> keysIterator = json.keys();
				//					ParkingLotList.init();
				while (keysIterator.hasNext())
				{
					String id = keysIterator.next().toString();
					Bid bid = new Bid();
					bid._id = Integer.valueOf(id);
					JSONObject info = json.getJSONObject(id);
					bid.price = (float) info.getDouble("bid");
					bid.timestamp = info.getLong("time");
					int userId = info.getInt("user_id");
					bid.driverId = userId;
					if(userId == SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_USER_ID, 0)){
						myBid = bid.price;

					}
					ParkingLotBidList.addOrUpdate(parkingId, SharedPreferencesUtil.loadSavedPreferences(mContext, Constants.SP_USER_ID, 0), userId, bid);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retSrc = false;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retSrc = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retSrc = false;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retSrc = false;
			}
			finally {
				requestTypeList.abort();
			}
			return retSrc;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			ParkingLotBidList.parkingLotBidListAdapters.get(parkingId).sortList();
			ParkingLotActivity.bidsLoading.setVisibility(View.GONE);
			ParkingLotBidList.parkingLotBidListAdapters.get(parkingId).notifyDataSetChanged();
			ParkingLotActivity.myBid.setText(Currency.getInstance(Locale.CHINA).getSymbol(Locale.CHINA) + String.valueOf(myBid));
		}
	}

	public void getDataFromServer(String url){
		if(url.equalsIgnoreCase("index")){
			new getAllParkingLots().execute();
			ParkingLotListActivity.progressBar.setVisible(true);
		}
		else if(url.contains("bids")){
			new getAllBids().execute(url);
			ParkingLotActivity.bidsLoading.setVisibility(View.VISIBLE);
		}
		else if(url.contains("my")){
			new getMyParkingLots().execute();
			MyParkingLotListActivity.progressBar.setVisible(true);
		}
	}
}
