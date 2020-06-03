package info.fshi.crowdparking.model;

public class Bid {

	public int _id; 
	public float price; // bidding price
	public int driverId; // id of the driver who made this bid
	public int parkinglotId; // id of the parking lot related to this bid
	public long timestamp; // time when this bid being made
	public boolean valid;
	
}
