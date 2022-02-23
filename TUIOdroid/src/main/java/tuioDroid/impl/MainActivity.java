/*
 TUIOdroid http://www.tuio.org/
 An Open Source TUIO Tracker for Android
 (c) 2011 by Tobias Schwirten and Martin Kaltenbrunner
 (c) 2022 EmiyaSyahriel
 
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

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import android.hardware.*;
import java.net.*;

/**
 * Main Activity
 * @author Tobias Schwirten
 * @author Martin Kaltenbrunner
 */
public class MainActivity extends Activity {
  
	/**
	 * View that shows the Touch points etc
	 */
	private TouchView touchView;
	
	/**
	 * Request Code for the Settings activity to define 
	 * which child activity calls back
	 */
	private static final int REQUEST_CODE_SETTINGS = 0;
	
	/**
	 * IP Address for OSC connection
	 */
	private String oscIP;
	
	/**
	 * Port for OSC connection
	 */
	private int oscPort;
	
	/**
	 * Adjusts the Touch View
	 */
	private boolean drawAdditionalInfo;
	
	/**
	 * Adjusts the TUIO verbosity
	 */
	private boolean sendPeriodicUpdates;
	
	/**
	 * Adjusts the Touch View
	 */
	private int screenOrientation;
	
	/**
	 * Detects shaking gesture
	 */	
	private SensorManager sensorManager;

	/**
	 * Methods on how to open settings
	 */
	private int openSettingsMethod;

	/**
	 * Set if the first volume button is pressed
	 */
	private boolean isOneVolumeBtnPressed;

	private boolean showSettings = false;

	/**
	 *  Called when the activity is first created. 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /* load preferences */
        SharedPreferences settings = this.getPreferences(MODE_PRIVATE);
      
        /* get Values */
        oscIP = settings.getString(Consts.OPTK_IP_TARGET, "192.168.1.2");
        System.out.println ("OSC IP: " + oscIP);
        oscPort = settings.getInt(Consts.OPTK_UDP_PORT, 3333);
        drawAdditionalInfo = settings.getBoolean(Consts.OPTK_EXTRA_INFO, false);
        sendPeriodicUpdates = settings.getBoolean(Consts.OPTK_VERBOSE, true);
        screenOrientation = settings.getInt (Consts.OPTK_SCREEN_ORIENTATION, 0);
        openSettingsMethod = settings.getInt (Consts.OPTK_SETTING_METHOD, 0);
        this.adjustScreenOrientation(this.screenOrientation);
        
        touchView  = new TouchView(this,oscIP,oscPort,drawAdditionalInfo,sendPeriodicUpdates);
        setContentView(touchView);
        
        sensorManager = (SensorManager) this.getBaseContext().getSystemService(Context.SENSOR_SERVICE);
    }

	/**
	 * Opens the Activity that provides the Settings
	 */
    private void openSettingsActivity (){
    	Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
    	myIntent.putExtra(Consts.INTACT_I_IP_TARGET, oscIP);
    	myIntent.putExtra(Consts.INTACT_I_UDP_PORT, oscPort);
    	myIntent.putExtra(Consts.OPTK_EXTRA_INFO, this.drawAdditionalInfo);
       	myIntent.putExtra(Consts.OPTK_VERBOSE, this.sendPeriodicUpdates);
      	myIntent.putExtra(Consts.OPTK_SCREEN_ORIENTATION, this.screenOrientation);
      	myIntent.putExtra(Consts.OPTK_SETTING_METHOD, this.openSettingsMethod);
      	showSettings = true;
    	startActivityForResult(myIntent, REQUEST_CODE_SETTINGS);
    }
    

    /**
     * Listens for results of new child activities. 
     * Different child activities are identified by their requestCode
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
       
    	 // See which child activity is calling us back.
    	if(requestCode == REQUEST_CODE_SETTINGS){
        	
        	switch (resultCode){
        	
        		case RESULT_OK:
        			Bundle dataBundle = data.getExtras(); 
        			isOneVolumeBtnPressed = false;
        	    	String ip = dataBundle.getString(Consts.INTACT_O_IP_TARGET);
        	    	
        	    	try { InetAddress.getByName(ip); } 
        	    	catch (Exception e) {
        	    		Toast.makeText(this, getString(R.string.invalid_ip_address), Toast.LENGTH_LONG).show();
        			}
        	    	
        	    	int port = 3333;
        	    	try { port = Integer.parseInt(dataBundle.getString(Consts.INTACT_O_UDP_PORT)); }
        	    	catch (Exception e) { port = 0; }
        	    	if (port<1024) Toast.makeText(this, getString(R.string.invalid_port), Toast.LENGTH_LONG).show();
        	    		
        	    	this.oscIP = ip;
            	    this.oscPort = port;        	
            	    this.drawAdditionalInfo = dataBundle.getBoolean(Consts.OPTK_EXTRA_INFO);
            	    this.sendPeriodicUpdates = dataBundle.getBoolean(Consts.OPTK_VERBOSE);
            	    	
            	    this.touchView.setNewOSCConnection(oscIP, oscPort);
            	    this.touchView.drawAdditionalInfo = this.drawAdditionalInfo;
            	    this.touchView.sendPeriodicUpdates = this.sendPeriodicUpdates;
            	    	
            	    /* Change behavior of screen rotation */
            	    this.screenOrientation  = dataBundle.getInt(Consts.OPTK_SCREEN_ORIENTATION);
            	    this.adjustScreenOrientation(this.screenOrientation);

            	    this.openSettingsMethod = dataBundle.getInt(Consts.OPTK_SETTING_METHOD);

        	    	/* Get preferences, edit and commit */
            	    SharedPreferences settings = this.getPreferences(MODE_PRIVATE);
            	    SharedPreferences.Editor editor = settings.edit();
            	    
            	    /* define Key/Value */
            	    editor.putString(Consts.OPTK_IP_TARGET, this.oscIP);
            	    editor.putInt(Consts.OPTK_UDP_PORT, this.oscPort);
            	    editor.putBoolean(Consts.OPTK_EXTRA_INFO,this.drawAdditionalInfo);
            	    editor.putBoolean(Consts.OPTK_VERBOSE,this.sendPeriodicUpdates);
            	    editor.putInt(Consts.OPTK_SCREEN_ORIENTATION,this.screenOrientation);
            	    editor.putInt(Consts.OPTK_SETTING_METHOD,this.openSettingsMethod);

            	    /* save Settings*/
            	    editor.apply();

            	    if(dataBundle.getBoolean(Consts.INTACT_O_SHOULD_EXIT, false))
            	    	finish();

        	    	break;
        	    default:
        	    	// Do nothing
        		
        	}
    	}
    }

    /**
     * Adjusts the screen orientation
     */
    @SuppressLint("SourceLockedOrientationActivity")
	private void adjustScreenOrientation (int screenOrientation){
    	
    	switch(screenOrientation){

    		case Consts.ORIENT_PORT: this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    		break;
				
    		case Consts.ORIENT_LAND: this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    		break;

			case Consts.ORIENT_SYST:
    		default: this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    		break;
    	}	
    }
    
    protected void onResume() {
      super.onResume();
      sensorManager.registerListener(shakeListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
      showSettings = false;
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	boolean retval = false;
		switch(keyCode){
			case KeyEvent.KEYCODE_VOLUME_UP:
				if(openSettingsMethod == Consts.SETMET_VOLUP){
					openSettingsActivity();
					retval = true;
				}else if(openSettingsMethod == Consts.SETMET_VOL2S){
					if(isOneVolumeBtnPressed){
						openSettingsActivity();
					}else{
						isOneVolumeBtnPressed = true;
					}
					retval = true;
				}
				break;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if(openSettingsMethod == Consts.SETMET_VOLDN){
					openSettingsActivity();
					retval = true;
				}else if(openSettingsMethod == Consts.SETMET_VOL2S){
					if(isOneVolumeBtnPressed){
						openSettingsActivity();
					}else{
						isOneVolumeBtnPressed = true;
					}
					retval = true;
				}
				break;
			default:
				retval = false;
				break;
		}
		return retval || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
    	boolean retval = false;
    	switch(keyCode){
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if(openSettingsMethod == Consts.SETMET_VOL2S) {
					isOneVolumeBtnPressed = false;
					retval =true;
				}
				break;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onBackPressed() {
    	if(openSettingsMethod == Consts.SETMET_BACKQ){
			openSettingsActivity();
		}else{
			super.onBackPressed();
		}
	}

	protected void onStop() {
      super.onStop();
      sensorManager.unregisterListener(shakeListener);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeListener);
    }

    private final SensorEventListener shakeListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
          float x = se.values[0];
          float y = se.values[1];
          float z = se.values[2];
          float shake = x*x + y*y + z*z;
           
          if ((!showSettings) && (shake>500) && openSettingsMethod == Consts.SETMET_SHAKE) {
        	  //android.util.Log.v("Accelerometer",""+shake);
        	  showSettings = true;
        	  openSettingsActivity();
          }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
      };

    
}