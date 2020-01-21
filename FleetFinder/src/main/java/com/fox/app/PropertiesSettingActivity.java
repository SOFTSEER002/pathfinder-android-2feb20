package com.fox.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import avd.api.core.IDevice;
import avd.api.core.exceptions.ApiException;

public abstract class PropertiesSettingActivity extends SampleAppActivity
{
	protected SampleApplication application = null;
	protected IDevice device = null;
	 
	protected abstract String getMessageObtainingParametersFailed();
	
	protected abstract void unloadParameters();
	
	// This is a custom method, intended to refresh 6140 parameters when displaying screen.
	// Should be overridden by every activity, where 6140 parameters are displayed.
	public abstract void refreshControls() throws ApiException;

	protected boolean isDeviceConnected()
	{
		return ((device != null) && application.connectedDevicesData.containsKey(device.getSerial()));
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
			device = application.getDevice();
			if (device == null)
				return;
        	
    		try
    		{ 
    			refreshControls();
    		}
    		catch (ApiException e)
    		{
    			OnClickListener abortActivity = new OnClickListener() {
    				
    				@Override
    				public void onClick(DialogInterface arg0, int arg1) {
    					unloadParameters();
    					finish();
    				}
    			};
    				
    			showErrorMessageBox(getMessageObtainingParametersFailed(), e, "Ok", abortActivity, null, null, device);
    		}
        }
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		application = (SampleApplication) getApplication();
	}

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter(SampleApplication.INTENT_ACTION_UPDATE_SCREEN_CONTROLS);
		registerReceiver(mReceiver, filter);

		// super.onResume() should be invoked after subscription to the receiver is implemented,
		// otherwise controls wouldn't be set properly when activity appears, as they're dependent on
		// SampleAppActivity emitting broadcast in its implementation of onResume.
		super.onResume();

		// A screen requires that current device be connected to the application while this screen is active
		application.requiredDevice = application.currentDeviceName;
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(mReceiver);
	}
}
