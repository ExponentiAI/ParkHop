package info.fshi.crowdparking.listener;

import info.fshi.crowdparking.ParkingLotBidDialog;
import android.view.View;
import android.view.View.OnClickListener;

public class OnBidButtonClickListener implements OnClickListener {

	int pId;
	float bid;
	
	public OnBidButtonClickListener(int parkingLotId, float minBid){
		pId = parkingLotId;
		bid = minBid;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		ParkingLotBidDialog bidDialog = new ParkingLotBidDialog(v.getContext(), pId, bid, false);
		bidDialog.show();

	}

}
