package info.fshi.crowdparking;

import info.fshi.crowdparking.model.ParkingLot;

import java.util.ArrayList;

public class ParkingLotList {

	public static ArrayList<ParkingLot> parkingLotList = new ArrayList<ParkingLot>(); // mac:type

	public static ArrayList<ParkingLot> myParkingLotList = new ArrayList<ParkingLot>(); // mac:type
	
	public static ParkingLotListAdapter parkingLotListAdapter;
	public static MyParkingLotListAdapter myParkingLotListAdapter;
	
	public static void init(){
		parkingLotList = new ArrayList<ParkingLot>();
		myParkingLotList = new ArrayList<ParkingLot>();
	}
	
	public static void update(ArrayList<ParkingLot> newParkingLotList){
		parkingLotList.clear(); 
		for(ParkingLot parkingLot: newParkingLotList){
			parkingLotList.add(parkingLot);
		}
	}
	
	public static void updateMyParkingLotList(ArrayList<ParkingLot> newParkingLotList){
		myParkingLotList.clear(); 
		for(ParkingLot parkingLot: newParkingLotList){
			myParkingLotList.add(parkingLot);
		}
	}
	
}
