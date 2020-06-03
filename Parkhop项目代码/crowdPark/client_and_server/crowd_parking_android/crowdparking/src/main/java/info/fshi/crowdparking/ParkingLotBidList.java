package info.fshi.crowdparking;

import info.fshi.crowdparking.model.Bid;

import java.util.ArrayList;

import android.util.SparseArray;

public class ParkingLotBidList {

	public static SparseArray<ArrayList<Bid>> parkingLotBidLists = new SparseArray<ArrayList<Bid>>();   
	
//	public static ArrayList<Bid> parkingLotBidList = new ArrayList<Bid>();

//	public static ParkingLotBidListAdapter parkingLotBidListAdapter;
	
	public static SparseArray<ParkingLotBidListAdapter> parkingLotBidListAdapters = new SparseArray<ParkingLotBidListAdapter>();  
		
	public static synchronized void addOrUpdate(int id, int userId, int driverId, Bid bid){
		for(Bid parkingLotBid : parkingLotBidLists.get(id)){
			if(parkingLotBid.driverId == driverId){
				parkingLotBidLists.get(id).remove(parkingLotBid);
			}
		}
		bid.valid = true;
		if(userId != driverId){
			parkingLotBidLists.get(id).add(bid);
		}
	}
	
	public static void invalidate(int id){
		for(Bid bid : parkingLotBidLists.get(id)){
			bid.valid = false;
		}
	}
}
