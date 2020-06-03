package info.fshi.crowdparking;

import info.fshi.crowdparking.model.Question;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ParkingLotQuestionListAdapter extends ArrayAdapter<Question>{
	Context context;
	int layoutResourceId;
	ArrayList<Question> questionList;

	public ParkingLotQuestionListAdapter(Context context, int resourceid, ArrayList<Question> list) {
		super(context, resourceid, list);
		this.layoutResourceId = resourceid;
		this.context = context;
		this.questionList = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		QuestionHolder holder = null;

		if(row == null)
		{
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new QuestionHolder();

			holder.question= (CheckBox)row.findViewById(R.id.question);
			row.setTag(holder);
		}
		else
		{
			holder = (QuestionHolder)row.getTag();
		}


		Question q = questionList.get(position);
		holder.question.setText(q.content);	
		holder.question.setOnCheckedChangeListener(questionCheckedListener);

		return row;
	}

	OnCheckedChangeListener questionCheckedListener = new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			// TODO Auto-generated method stub
			
			if(arg1){
				ParkingLotReportDialog.answers.put(arg0.getText().toString(), 1);
			}else{
				ParkingLotReportDialog.answers.put(arg0.getText().toString(), 0);
			}
			System.out.println(ParkingLotReportDialog.answers.toString());
		}
	};

	static class QuestionHolder
	{
		CheckBox question;
	}

}
