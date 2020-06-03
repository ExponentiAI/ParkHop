package info.fshi.crowdparking.model;

public class ParkingLot {
	
	public int _id; // id refers to the database id
	public String name; // name of the parking lot
	public int crowdSourcerId; // FK, the id of the crowd sourcer who reports this lot
	public double latitude; // location info
	public double longitude;
	public String description; // text description 
	public String address;
	public float mBidding; // minimum accepted bidding price
	public long timestamp; // time when this lot is reported
	public double distance;
	public float price;
	public float credibility;
	public float myBid;
	
}
