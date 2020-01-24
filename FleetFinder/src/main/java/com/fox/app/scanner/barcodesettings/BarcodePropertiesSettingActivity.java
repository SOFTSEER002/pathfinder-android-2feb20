package com.fox.app.scanner.barcodesettings;

import com.fox.app.Activities.PropertiesSettingActivity;

public abstract class BarcodePropertiesSettingActivity extends PropertiesSettingActivity {

	@Override
	protected String getMessageObtainingParametersFailed() {
		return "Obtaining barcode parameters failed";
	}

	@Override
	protected void unloadParameters()
	{
		application.getDeviceData().isBarcodeParametersLoaded = false;
	}
}
