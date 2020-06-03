package info.fshi.crowdparking;

import info.fshi.crowdparking.lbs.BaiduMapLocationManager;
import info.fshi.crowdparking.utils.Constants;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

public class ParkingLotMapActivity extends Activity {

	MapView mMapView = null; 

	BaiduMap mBaiduMap = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parking_lot_map);

		mMapView = (MapView) findViewById(R.id.bmapView);  
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);


		Intent intent = getIntent();
		double[] loc = intent.getDoubleArrayExtra(Constants.INTENT_PARKING_LOT_LOCATION);

		LatLng point = new LatLng(loc[0], loc[1]);

		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker);
		OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
		mBaiduMap.addOverlay(option);
		mBaiduMap.setMyLocationEnabled(true);  

		LatLng myLoc = new LatLng(BaiduMapLocationManager.currentBdLocation.getLatitude(), BaiduMapLocationManager.currentBdLocation.getLongitude());
		MyLocationData locData = new MyLocationData.Builder()  
		.accuracy(30)
		.direction(100).latitude(BaiduMapLocationManager.currentBdLocation.getLatitude())  
		.longitude(BaiduMapLocationManager.currentBdLocation.getLongitude()).build();
		mBaiduMap.setMyLocationData(locData);
		LatLng zoomPoint = new LatLng((point.latitude + myLoc.latitude)/2, (point.longitude + myLoc.longitude)/2);
		//		double zoomLevel = 1/Math.max(Math.abs(myLoc.latitude - point.latitude), Math.abs(myLoc.longitude - point.longitude));
		
		MapStatus mMapStatus = new MapStatus.Builder()
		.target(zoomPoint)
		.zoom(getZoomLevel(myLoc, point))
		.build();
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		mBaiduMap.setMapStatus(mMapStatusUpdate);
	}

	private float getZoomLevel(LatLng p1, LatLng p2){
		int zoomLevelDist[] = {2000000,1000000,500000,200000,100000,
				50000,25000,20000,10000,5000,2000,1000,500,100,50,20,0}; 

		int jl = (int)DistanceUtil.getDistance(p1, p2);
		int i;
		for(i=0; i<17; i++){
			if(zoomLevelDist[i]<jl){
				break;
			}
		}
		return i+4;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mMapView.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mMapView.onDestroy();
	}



}

