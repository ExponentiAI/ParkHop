package info.fshi.crowdparking.auth;

import info.fshi.crowdparking.R;
import info.fshi.crowdparking.network.WebServerConnector;
import info.fshi.crowdparking.utils.Constants;
import info.fshi.crowdparking.utils.Packet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	WebServerConnector mWebServerConnector;
	Context mContext;

	public static Button loginButton;
	public static Dialog loginLoadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mContext = this;

		loginLoadingDialog = new Dialog(mContext);
		loginLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		loginLoadingDialog.setContentView(R.layout.dialog_login_loading);
		mWebServerConnector = new WebServerConnector(mContext);

		Button loginButtonDriver = (Button) findViewById(R.id.login_button_driver);
		loginButtonDriver.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TextView tvUsername = (TextView) findViewById(R.id.username);
				TextView tvPassword = (TextView) findViewById(R.id.password);
				String username = tvUsername.getText().toString();
				String password = tvPassword.getText().toString();
				if(username.length() == 0 || password.length() == 0){
					Toast.makeText(mContext, "用户名与密码不能为空", Toast.LENGTH_LONG).show();
				}else{
					loginLoadingDialog.show();
					JSONObject param = new JSONObject();
					try {
						param.put(Packet.KEY_USER, username);
						param.put(Packet.KEY_PASS, password);
						param.put(Packet.KEY_USER_TYPE, "1");
						mWebServerConnector.postDataToServer(Constants.LOGIN_URL, param);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		Button loginButtonSourcer = (Button) findViewById(R.id.login_button_sourcer);
		loginButtonSourcer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TextView tvUsername = (TextView) findViewById(R.id.username);
				TextView tvPassword = (TextView) findViewById(R.id.password);
				String username = tvUsername.getText().toString();
				String password = tvPassword.getText().toString();
				if(username.length() == 0 || password.length() == 0){
					Toast.makeText(mContext, "用户名与密码不能为空", Toast.LENGTH_LONG).show();
				}else{
					loginLoadingDialog.show();
					JSONObject param = new JSONObject();
					try {
						param.put(Packet.KEY_USER, username);
						param.put(Packet.KEY_PASS, password);
						param.put(Packet.KEY_USER_TYPE, "0");
						mWebServerConnector.postDataToServer(Constants.LOGIN_URL, param);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

}
