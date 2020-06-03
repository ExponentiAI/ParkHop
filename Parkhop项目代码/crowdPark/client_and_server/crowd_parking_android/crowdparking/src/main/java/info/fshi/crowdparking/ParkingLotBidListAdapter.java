package info.fshi.crowdparking;

import info.fshi.crowdparking.model.Bid;

import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ParkingLotBidListAdapter extends ArrayAdapter<Bid> {

	Context context;
	int layoutResourceId;
	int parkingLotId;

	public ParkingLotBidListAdapter(Context context, int resourceid, int id) {
		super(context, resourceid, ParkingLotBidList.parkingLotBidLists.get(id));
		this.layoutResourceId = resourceid;
		this.context = context;
		this.parkingLotId = id;
	}

	/**
	 * comparator to sort list
	 * @author fshi
	 */
	public class ParkingLotBitAmountComparator implements Comparator<Bid>
	{
		@Override
		public int compare(Bid lhs, Bid rhs) {
			// TODO Auto-generated method stub
			return (int) (rhs.price - lhs.price);
		}
	}


	public void sortList(){
		Collections.sort(ParkingLotBidList.parkingLotBidLists.get(parkingLotId), new ParkingLotBitAmountComparator());
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ParkingLotBidHolder holder = null;

		if(row == null)
		{
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new ParkingLotBidHolder();

			holder.amount = (TextView)row.findViewById(R.id.bid_amount);
			holder.time = (TextView)row.findViewById(R.id.bid_time);
			row.setTag(holder);
		}
		else
		{
			holder = (ParkingLotBidHolder)row.getTag();
		}

		if(ParkingLotBidList.parkingLotBidLists.get(parkingLotId) != null){
			Bid bid = ParkingLotBidList.parkingLotBidLists.get(parkingLotId).get(position);
			if(bid != null){
				holder.amount.setText(Currency.getInstance(Locale.CHINA).getSymbol(Locale.CHINA) + String.valueOf(bid.price));	

				holder.time.setText(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.CHINA).format(new java.util.Date(bid.timestamp)));
			}
		}else{
			System.out.println("null");
		}

		return row;
	}

	static class ParkingLotBidHolder
	{
		TextView amount;
		TextView time;
	}


}
