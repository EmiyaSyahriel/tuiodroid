/*
 TUIOdroid http://www.tuio.org/
 An Open Source TUIO Tracker for Android
 (c) 2011 by Tobias Schwirten and Martin Kaltenbrunner

 TUIOdroid is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 TUIOdroid is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with TUIOdroid.  If not, see <http://www.gnu.org/licenses/>.
*/

package tuioDroid.impl;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import tuioDroid.impl.R;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.os.Process;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Activity that provides Settings for the user
 * @author Tobias Schwirten
 * @author Martin Kaltenbrunner
 */

public class SettingsActivity extends Activity{
	private boolean shouldKill = false;
	/**
	 *  Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingslayout);
		shouldKill = false;

		Button btn_OK = (Button)findViewById(R.id.saveButton);
		btn_OK.setOnClickListener(listener_OkBtn);

		//Button btn_KO = (Button)findViewById(R.id.cancelButton);
		//btn_KO.setOnClickListener(listener_KoBtn);

		EditText editText_IP = (EditText)findViewById(R.id.et_IP);
		String ip = (getIntent().getExtras().getString(Consts.INTACT_I_IP_TARGET));
		editText_IP.setText(ip);

		EditText editText_port = (EditText)findViewById(R.id.et_Port);
		int port = getIntent().getExtras().getInt(Consts.INTACT_I_UDP_PORT);
		editText_port.setText(Integer.toString(port));

		CheckBox checker_Verbose = (CheckBox)findViewById(R.id.checkB_Verbosity);
		boolean sendPeriodicUpdates = getIntent().getExtras().getBoolean(Consts.OPTK_VERBOSE);
		checker_Verbose.setChecked(sendPeriodicUpdates);

		CheckBox checker_Info = (CheckBox)findViewById(R.id.checkB_ExtraInfo);
		boolean drawAdditionalInfo = getIntent().getExtras().getBoolean(Consts.OPTK_EXTRA_INFO);
		checker_Info.setChecked(drawAdditionalInfo);

		Spinner orientSpinner = (Spinner) findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> orientAdapter = ArrayAdapter.createFromResource(this, R.array.orientation_array, android.R.layout.simple_spinner_item);
		orientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		orientSpinner.setAdapter(orientAdapter);

		Spinner setmetSpinner = (Spinner) findViewById(R.id.spinner_2);
		ArrayAdapter<CharSequence> setmetAdapter = ArrayAdapter.createFromResource(this, R.array.opensettings_array, android.R.layout.simple_spinner_item);
		setmetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		setmetSpinner.setAdapter(setmetAdapter);
		orientSpinner.setSelection(getIntent().getExtras().getInt(Consts.OPTK_SCREEN_ORIENTATION));
		setmetSpinner.setSelection(getIntent().getExtras().getInt(Consts.OPTK_SETTING_METHOD));

		TextView ipView = (TextView)findViewById(R.id.localIP);
		String localIP = getLocalIpAddress();
		if (localIP!=null) ipView.setText(getString(R.string.local_ip_placeholder_format, localIP));
		else {
			ipView.setTextColor(Color.RED);
			ipView.setText(R.string.no_network);
		}
	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if ((!inetAddress.isLoopbackAddress()) && (inetAddress instanceof Inet4Address)) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException sockex) {}
		catch(NullPointerException npex){}
		return null;
	}


	/**
	 *  Called when the options menu is created
	 *  Options menu is defined in m.xml
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.m, menu);
		return true;
	}

	/**
	 * Called when the user selects an Item in the Menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.help){
			this.openHelpActivity();
			return true;
		}else if(id == R.id.exit){
			shouldKill = true;
			listener_OkBtn.onClick(null);
			return true;
		}else{
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Opens the Activity that Help information
	 */
	private void openHelpActivity (){
		Intent myIntent = new Intent(SettingsActivity.this, HelpActivity.class);
		startActivity(myIntent);
	}


	/**
	 * Listener for the OK button
	 */
	private OnClickListener listener_OkBtn = new OnClickListener(){

		public void onClick(View v){

			String ip = ((TextView) findViewById(R.id.et_IP)).getText().toString();

			try { InetAddress.getByName(ip); }
			catch (Exception e) {
				((TextView) findViewById(R.id.et_IP)).setText(R.string.reval_invalid_address);
				return;
			}

			int port = 3333;
			try { port = Integer.parseInt(((TextView) findViewById(R.id.et_Port)).getText().toString()); }
			catch (Exception e) { port = 0; }
			if (port<1024) {
				((TextView) findViewById(R.id.et_Port)).setText(R.string.reval_invalid_port);
				return;
			}


			Intent responseIntent = new Intent();

			responseIntent.putExtra(Consts.INTACT_O_IP_TARGET,((TextView) findViewById(R.id.et_IP)).getText().toString());
			responseIntent.putExtra(Consts.INTACT_O_UDP_PORT, ((TextView) findViewById(R.id.et_Port)).getText().toString());
			responseIntent.putExtra(Consts.OPTK_EXTRA_INFO, ((CheckBox)findViewById(R.id.checkB_ExtraInfo)).isChecked());
			responseIntent.putExtra(Consts.OPTK_VERBOSE, ((CheckBox)findViewById(R.id.checkB_Verbosity)).isChecked());
			responseIntent.putExtra(Consts.OPTK_SCREEN_ORIENTATION,  ((Spinner) (findViewById(R.id.spinner))).getSelectedItemPosition());
			responseIntent.putExtra(Consts.OPTK_SETTING_METHOD,  ((Spinner) (findViewById(R.id.spinner_2))).getSelectedItemPosition());
			responseIntent.putExtra(Consts.INTACT_O_SHOULD_EXIT,  shouldKill);

			/*Setting result for this activity */
			setResult(RESULT_OK, responseIntent);

			finish();
		}
	};

/**
 * Listener for the Cancel button
 */
/*private OnClickListener listener_KoBtn = new OnClickListener(){

	  public void onClick(View v){
          finish();
       }
   };*/



}
