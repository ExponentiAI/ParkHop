package info.fshi.crowdparking;

import info.fshi.crowdparking.listener.OnBidButtonClickListener;
import info.fshi.crowdparking.model.ParkingLot;
import info.fshi.crowdparking.utils.Constants;
import info.fshi.crowdparking.utils.SharedPreferencesUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ParkingLotListAdapter extends ArrayAdapter<ParkingLot> {

	Context context;
	int layoutResourceId;

	public ParkingLotListAdapter(Context context, int resourceid) {
		super(context, resourceid, ParkingLotList.parkingLotList);
		this.layoutResourceId = resourceid;
		this.context = context;
	}

	/**
	 * comparator to sort list
	 * @author fshi
	 */
	public class ParkingLotDistanceComparator implements Comparator<ParkingLot>
	{
		@Override
		public int compare(ParkingLot lhs, ParkingLot rhs) {
			// TODO Auto-generated method stub
			return (int) (lhs.distance - rhs.distance);
		}
	}


	public void sortList(){
		Collections.sort(ParkingLotList.parkingLotList, new ParkingLotDistanceComparator());
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ParkingLotHolder holder = null;

		if(row == null)
		{
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new ParkingLotHolder();

			holder.info = (TextView)row.findViewById(R.id.parkinglot_info);
			holder.loc = (TextView)row.findViewById(R.id.parkinglot_loc);
			holder.bid = (TextView)row.findViewById(R.id.parkinglot_bid);
			holder.price = (TextView)row.findViewById(R.id.parkinglot_price);
			holder.addr = (TextView)row.findViewById(R.id.parkinglot_list_addr);
			holder.bidButton = (Button) row.findViewById(R.id.bid_button);
			row.setTag(holder);
		}
		else
		{
			holder = (ParkingLotHolder)row.getTag();
		}

		ParkingLot parkingLot = ParkingLotList.parkingLotList.get(position);
		holder.info.setText(parkingLot.name);

		double ptDistance = parkingLot.distance;
		StringBuilder sb = new StringBuilder();
		if(ptDistance < 1000){
			sb.append((int) ptDistance);
			sb.append(" m");
		}else{
			sb.append((int) (ptDistance/1000));
			sb.append(" km");
		}
		holder.loc.setText(sb.toString());

		holder.bid.setText(Currency.getInstance(Locale.CHINA).getSymbol(Locale.CHINA) + String.valueOf(parkingLot.mBidding));
		holder.price.setText(Currency.getInstance(Locale.CHINA).getSymbol(Locale.CHINA) + String.valueOf(parkingLot.price));
		holder.addr.setText(parkingLot.address);
		
		if(SharedPreferencesUtil.loadSavedPreferences(context, Constants.SP_USER_TYPE, 0) == 1){
			holder.bidButton.setBackgroundResource(R.drawable.bid_button);
			holder.bidButton.setTextColor(Color.WHITE);
			holder.bidButton.setEnabled(true);
			holder.bidButton.setOnClickListener(new OnBidButtonClickListener(parkingLot._id, parkingLot.mBidding));
		}else{
			holder.bidButton.setVisibility(View.GONE);
		}

		return row;
	}

	static class ParkingLotHolder
	{
		TextView info;
		TextView loc;
		TextView bid;
		TextView price;
		TextView addr;
		Button bidButton;
	}


}
