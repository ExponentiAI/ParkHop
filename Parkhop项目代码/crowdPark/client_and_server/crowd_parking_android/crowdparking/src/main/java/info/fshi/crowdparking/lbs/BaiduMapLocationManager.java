package info.fshi.crowdparking.lbs;

import info.fshi.crowdparking.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

public class BaiduMapLocationManager {

	private LocationClient mLocationClient = null;
	public BDLocationListener mLocationListener = new BaiduMapLocationListener();
	private GeoCoder geoCoder = null;

	private final String TAG = "baidumaplocationmanager";

	//	// residence
	//	private static final String[] POI_TYPE_RESIDENCE = {"house", "hotel"};
	//	// entertain
	//	private static final String[] POI_TYPE_ENTERTAIN = {"cater", "shopping"};
	//	// life
	//	private static final String[] POI_TYPE_LIFE = {"education", "life", "hospital"};


	//	// key
	//	public static final String KEY_POI_TYPE_POSITION = "pos";
	//	public static final String KEY_POI_TYPE_LIFE = "life";
	//	public static final String KEY_POI_TYPE_RESIDENCE = "residence";
	//	public static final String KEY_POI_TYPE_ENTERTAIN = "entertain";

	//	private HashMap<String, ArrayList<String>> poiTrue = null;
	//	private HashMap<String, ArrayList<String>> poiFake = null;
	//
	//	private ArrayList<String> poiUidTrue = null;
	//	private ArrayList<String> poiUidFake = null;

	public static BDLocation currentBdLocation = null;

	private ReverseGeoCodeResult currentGeoInfo = null;
	private ReverseGeoCodeResult fakeGeoInfo = null;
	
	HashMap<String, Integer> questions = new HashMap<String, Integer>();

	//	private PoiSearch poiSearch = null;

	public BaiduMapLocationManager(Context context){
		mLocationClient = new LocationClient(context);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);
		option.setIsNeedAddress(true);
		option.setNeedDeviceDirect(true);
		mLocationClient.setLocOption(option);
		mLocationClient.registerLocationListener(mLocationListener);
		mLocationClient.start();
	}

	public void startLocationManager(){
		mLocationClient.start();
	}

	public void stopLocationManager(){
		mLocationClient.stop();
	}
	
	public HashMap<String, Integer> getQuestions(){
		HashMap<String, Integer> maxQuestions = new HashMap<String, Integer>();
		Iterator<Entry<String, Integer>> iter = questions.entrySet().iterator();
		int count = 0;
		while (iter.hasNext() && count < Constants.MAX_QUESTIONS) {
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			maxQuestions.put(entry.getKey(), entry.getValue());
			count ++;
		}
		return maxQuestions;
	}

	private void prepareQuestions(){
		// pos 当前地址和偏移地址作为问题提给worker？
		if(!currentGeoInfo.getAddressDetail().district.isEmpty() && !fakeGeoInfo.getAddressDetail().district.isEmpty()){
			if(!currentGeoInfo.getAddressDetail().district.equalsIgnoreCase(fakeGeoInfo.getAddressDetail().district)){
				// if not equal, put both in questions
				questions.put(currentGeoInfo.getAddressDetail().district, 1);
				questions.put(fakeGeoInfo.getAddressDetail().district, 0);
			}else{
				// only put current in questions
				questions.put(currentGeoInfo.getAddressDetail().district, 1);
			}
		}else if(!currentGeoInfo.getAddressDetail().district.isEmpty()){
			// only put current in questions
			questions.put(currentGeoInfo.getAddressDetail().district, 1);
		}else{
			// only put fake in questions
			questions.put(fakeGeoInfo.getAddressDetail().district, 0);
		}

		if(!currentGeoInfo.getAddressDetail().street.isEmpty() && !fakeGeoInfo.getAddressDetail().street.isEmpty()){
			if(!currentGeoInfo.getAddressDetail().street.equalsIgnoreCase(fakeGeoInfo.getAddressDetail().street)){
				// if not equal, put both in questions
				questions.put(currentGeoInfo.getAddressDetail().street, 1);
				questions.put(fakeGeoInfo.getAddressDetail().street, 0);
			}else{
				// only put current in questions
				questions.put(currentGeoInfo.getAddressDetail().street, 1);
			}
		}else if(!currentGeoInfo.getAddressDetail().street.isEmpty()){
			// only put current in questions
			questions.put(currentGeoInfo.getAddressDetail().street, 1);
		}else{
			// only put fake in questions
			questions.put(fakeGeoInfo.getAddressDetail().street, 0);
		}

		if(currentGeoInfo.getBusinessCircle() != null && !currentGeoInfo.getBusinessCircle().isEmpty()){
			List<String> currentBusinessCircle = Arrays.asList(currentGeoInfo.getBusinessCircle().split(","));	
			if(fakeGeoInfo.getBusinessCircle() != null && !fakeGeoInfo.getBusinessCircle().isEmpty()){
				// has fake
				List<String> fakeBusinessCircle = Arrays.asList(fakeGeoInfo.getBusinessCircle().split(","));
				for(String bCircle : fakeBusinessCircle){
					if(!currentBusinessCircle.contains(bCircle)){
						questions.put(bCircle, 0);
					}
				}
			}
			for(String bCircle : currentBusinessCircle){
				questions.put(bCircle, 1);
			}
		}

		// current
		if(currentGeoInfo.getPoiList() != null){
			if(!currentGeoInfo.getPoiList().isEmpty()){
				ArrayList<String> currentPoiList = new ArrayList<String>();
				for(PoiInfo poi: currentGeoInfo.getPoiList()){
					currentPoiList.add(poi.name);
				}
				// fake
				if(fakeGeoInfo.getPoiList() != null){
					if(!fakeGeoInfo.getPoiList().isEmpty()){
						for(PoiInfo poi: fakeGeoInfo.getPoiList()){
							if(!currentPoiList.contains(poi.name)){
								questions.put(poi.name, 0);
							}
						}
					}
				}
				for(String poiName : currentPoiList){
					questions.put(poiName, 1);
				}
			}
		}
		

	}

	/* map {type: {name:T/F}}
	public HashMap<String, HashMap<String, Integer>> prepareQuestions(){
		HashMap<String, HashMap<String, Integer>> questions = new HashMap<String, HashMap<String, Integer>>();

		// pos questions
		HashMap<String, Integer> posQuestions = new HashMap<String, Integer>();
		posQuestions.put(currentGeoInfo.getAddressDetail().district, 1);
		posQuestions.put(fakeGeoInfo.getAddressDetail().district, 0);
		posQuestions.put(currentGeoInfo.getAddressDetail().street, 1);
		posQuestions.put(fakeGeoInfo.getAddressDetail().street, 0);

		List<String> currentBusinessCircle = Arrays.asList(currentGeoInfo.getBusinessCircle().split(","));
		List<String> fakeBusinessCircle = Arrays.asList(fakeGeoInfo.getBusinessCircle().split(","));
		ArrayList<String> trueCircle = new ArrayList<String>();
		ArrayList<String> fakeCircle = new ArrayList<String>();
		for(String cBCircle : currentBusinessCircle){
			if(cBCircle.length() > 2 && cBCircle.length() < 10){
				if(!fakeBusinessCircle.contains(cBCircle) && !currentGeoInfo.getAddressDetail().district.equalsIgnoreCase(cBCircle) && !currentGeoInfo.getAddressDetail().street.equalsIgnoreCase(cBCircle)){
					trueCircle.add(cBCircle);
				}else if(fakeBusinessCircle.contains(cBCircle)){
					fakeBusinessCircle.remove(cBCircle);
				}
			}
		}

		for(String fBCircle : fakeBusinessCircle){
			if(fBCircle.length() > 2 && fBCircle.length() < 10){
				if(!fakeGeoInfo.getAddressDetail().district.equalsIgnoreCase(fBCircle) && !fakeGeoInfo.getAddressDetail().street.equalsIgnoreCase(fBCircle)){
					fakeCircle.add(fBCircle);
				}
			}
		}

		int bCircleNum = Math.min(trueCircle.size(), fakeCircle.size());

		for(int i=0; i<bCircleNum; i++){
			posQuestions.put(trueCircle.get(i), 1);
			posQuestions.put(fakeCircle.get(i), 0);
		}

		questions.put(KEY_POI_TYPE_POSITION, posQuestions);

		// find the max/second max question num in all types
		int residenceSize = Math.min(poiTrue.get(KEY_POI_TYPE_RESIDENCE).size(), poiFake.get(KEY_POI_TYPE_RESIDENCE).size()); 
		int lifeSize = Math.min(poiTrue.get(KEY_POI_TYPE_LIFE).size(), poiFake.get(KEY_POI_TYPE_LIFE).size());
		int enterSize = Math.min(poiTrue.get(KEY_POI_TYPE_ENTERTAIN).size(), poiFake.get(KEY_POI_TYPE_ENTERTAIN).size());

		// res question
		HashMap<String, Integer> resQuestions = new HashMap<String, Integer>();
		for(int i=0; i<residenceSize; i++){
			resQuestions.put(poiTrue.get(KEY_POI_TYPE_RESIDENCE).get(i), 1);
			resQuestions.put(poiFake.get(KEY_POI_TYPE_RESIDENCE).get(i), 0);
		}
		questions.put(KEY_POI_TYPE_RESIDENCE, resQuestions);

		// enter question
		HashMap<String, Integer> entQuestions = new HashMap<String, Integer>();
		for(int i=0; i<enterSize; i++){
			entQuestions.put(poiTrue.get(KEY_POI_TYPE_ENTERTAIN).get(i), 1);
			entQuestions.put(poiFake.get(KEY_POI_TYPE_ENTERTAIN).get(i), 0);
		}
		questions.put(KEY_POI_TYPE_ENTERTAIN, entQuestions);
		// res question
		HashMap<String, Integer> lifeQuestions = new HashMap<String, Integer>();
		for(int i=0; i<lifeSize; i++){
			lifeQuestions.put(poiTrue.get(KEY_POI_TYPE_LIFE).get(i), 1);
			lifeQuestions.put(poiFake.get(KEY_POI_TYPE_LIFE).get(i), 0);
		}
		questions.put(KEY_POI_TYPE_LIFE, lifeQuestions);

		return questions;
	}*/

	/**
	 * 开始定位
	 */
	private void geoSearch(){

		// init 
		//		poiTrue = new HashMap<String, ArrayList<String>>();
		//		poiFake = new HashMap<String, ArrayList<String>>();
		//		poiUidTrue = new ArrayList<String>();
		//		poiUidFake = new ArrayList<String>();
		geoCoder = GeoCoder.newInstance();
		geoCoder.setOnGetGeoCodeResultListener(currentLocGeoCoderListener);
		LatLng pL = new LatLng(currentBdLocation.getLatitude(), currentBdLocation.getLongitude());
		// 当前坐标转化为地址
		geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(pL));
		Log.i(TAG, "geoSearch: "+ pL.toString());

	}

	/**
	 * 
	private void nearbySearch() {  
		PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();  
		nearbySearchOption.location(new LatLng(currentBdLocation.getLatitude(), currentBdLocation.getLongitude()));  
		nearbySearchOption.keyword("宜山路");  
		nearbySearchOption.radius(500);
		nearbySearchOption.pageNum(0);
		poiSearch.searchNearby(nearbySearchOption);

	} */

	/*
	OnGetPoiSearchResultListener poiSearchListener = new OnGetPoiSearchResultListener() {  
		@Override  
		public void onGetPoiResult(PoiResult poiResult) {  
			//			if (poiResult == null  
			//					|| poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			//				System.out.println("no result");
			//				return;  
			//			}  
			//
			//			if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
			//				for(PoiInfo poi : poiResult.getAllPoi()){
			//					System.out.println("poi name");
			//					System.out.println(poi.name);
			//					System.out.println(poi.type.name());
			//					System.out.println(poi.location.latitude);
			//					System.out.println(poi.location.longitude);
			//					System.out.println(currentBdLocation.getLatitude());
			//					System.out.println(currentBdLocation.getLongitude());
			//				}
			//			}  
		}

		@Override
		public void onGetPoiDetailResult(PoiDetailResult arg0) {
			// TODO Auto-generated method stub
			//			System.out.println("poi detail");
			//			System.out.println("name : " + arg0.getName());
			if(!arg0.getType().isEmpty()){
				if(poiUidTrue.contains(arg0.getUid())){
					if(Arrays.asList(POI_TYPE_RESIDENCE).contains(arg0.getType())){
						if(!poiTrue.containsKey(KEY_POI_TYPE_RESIDENCE)){
							poiTrue.put(KEY_POI_TYPE_RESIDENCE, new ArrayList<String>());
						}
						poiTrue.get(KEY_POI_TYPE_RESIDENCE).add(arg0.getName());
					}else if(Arrays.asList(POI_TYPE_ENTERTAIN).contains(arg0.getType())){
						if(!poiTrue.containsKey(KEY_POI_TYPE_ENTERTAIN)){
							poiTrue.put(KEY_POI_TYPE_ENTERTAIN, new ArrayList<String>());
						}
						poiTrue.get(KEY_POI_TYPE_ENTERTAIN).add(arg0.getName());
					}else if(Arrays.asList(POI_TYPE_LIFE).contains(arg0.getType())){
						if(!poiTrue.containsKey(KEY_POI_TYPE_LIFE)){
							poiTrue.put(KEY_POI_TYPE_LIFE, new ArrayList<String>());
						}
						poiTrue.get(KEY_POI_TYPE_LIFE).add(arg0.getName());
					}else{
						System.out.println("new type : " + arg0.getType());
					}
				}else if(poiUidFake.contains(arg0.getUid())){
					if(Arrays.asList(POI_TYPE_RESIDENCE).contains(arg0.getType())){
						if(!poiFake.containsKey(KEY_POI_TYPE_RESIDENCE)){
							poiFake.put(KEY_POI_TYPE_RESIDENCE, new ArrayList<String>());
						}
						poiFake.get(KEY_POI_TYPE_RESIDENCE).add(arg0.getName());
					}else if(Arrays.asList(POI_TYPE_ENTERTAIN).contains(arg0.getType())){
						if(!poiFake.containsKey(KEY_POI_TYPE_ENTERTAIN)){
							poiFake.put(KEY_POI_TYPE_ENTERTAIN, new ArrayList<String>());
						}
						poiFake.get(KEY_POI_TYPE_ENTERTAIN).add(arg0.getName());
					}else if(Arrays.asList(POI_TYPE_LIFE).contains(arg0.getType())){
						if(!poiFake.containsKey(KEY_POI_TYPE_LIFE)){
							poiFake.put(KEY_POI_TYPE_LIFE, new ArrayList<String>());
						}
						poiFake.get(KEY_POI_TYPE_LIFE).add(arg0.getName());
					}else{
						System.out.println("new type : " + arg0.getType());
					}
				}
			}
		}
	};
	 */

	private OnGetGeoCoderResultListener currentLocGeoCoderListener = new OnGetGeoCoderResultListener() {
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			Log.i(TAG, "onGetReverseGeoCodeResult: ");
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				Log.d(TAG, "no geo code result");
			}
			else{
				currentGeoInfo = result;
				//				System.out.println("street:" + result.getAddressDetail().street);
				//				if(!currentGeoInfo.getPoiList().isEmpty()){
				//					for(PoiInfo poi : currentGeoInfo.getPoiList()){
				//						PoiDetailSearchOption detailSearchOption = new PoiDetailSearchOption();
				//						poiSearch = PoiSearch.newInstance();  
				//						poiSearch.setOnGetPoiSearchResultListener(poiSearchListener);  
				//						detailSearchOption.poiUid(poi.uid);
				//						System.out.println(poi.name);
				//						poiUidTrue.add(poi.uid);
				//						poiSearch.searchPoiDetail(detailSearchOption);
				//					}
				//				}

				//再做一次偏移转化
				geoCoder.setOnGetGeoCodeResultListener(fakeLocGeoCoderListener);
				Log.i(TAG, "onGetReverseGeoCodeResult: "+currentBdLocation.getLatitude());
				Log.i(TAG, "onGetReverseGeoCodeResult: "+currentBdLocation.getLongitude());
				geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(currentBdLocation.getLatitude() + 0.05, currentBdLocation.getLongitude() - 0.05)));
			}
		}
		@Override
		public void onGetGeoCodeResult(GeoCodeResult result) {
			Log.i(TAG, "onGetGeoCodeResult: ");
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				Log.d(TAG, "no geo code result");
			}
		}
	};

	private OnGetGeoCoderResultListener fakeLocGeoCoderListener = new OnGetGeoCoderResultListener() {
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			Log.i(TAG, "onGetReverseGeoCodeResult: "+result.getAddress());
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				Log.d(TAG, "no geo code result");
			}
			else{
				fakeGeoInfo = result;
				//				System.out.println("street:" + result.getAddressDetail().street);
				//				System.out.println(result.getBusinessCircle());
				//				if(!fakeGeoInfo.getPoiList().isEmpty()){
				//					for(PoiInfo poi : fakeGeoInfo.getPoiList()){
				//						PoiDetailSearchOption detailSearchOption = new PoiDetailSearchOption();
				//						poiSearch = PoiSearch.newInstance();  
				//						poiSearch.setOnGetPoiSearchResultListener(poiSearchListener);  
				//						detailSearchOption.poiUid(poi.uid);
				//						poiUidFake.add(poi.uid);
				//						System.out.println(poi.name);
				//						poiSearch.searchPoiDetail(detailSearchOption);
				//					}
				//				}
			}
			prepareQuestions();
		}
		@Override
		public void onGetGeoCodeResult(GeoCodeResult result) {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				Log.d(TAG, "no geo code result");
			}
		}
	};

	public class BaiduMapLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return ;
			currentBdLocation = location;
			Log.i(TAG, "onReceiveLocation: "+currentBdLocation.getAddrStr());
			geoSearch();
		}
	}

	public void requestLocationUpdates(){
		if (mLocationClient != null && mLocationClient.isStarted()){
			Log.d("LocSDK5", "location request start");
			mLocationClient.requestLocation();
		}
		else 
			Log.d("LocSDK5", "locClient is null or not started");
	}
}
