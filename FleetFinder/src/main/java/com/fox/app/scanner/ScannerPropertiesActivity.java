package com.fox.app.scanner;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.avery.sampleapp.R;

import avd.api.core.ScanMode;
import avd.api.core.exceptions.ApiConfigurationException;
import avd.api.core.exceptions.ApiException;
import avd.api.core.exceptions.ApiScannerException;
import com.fox.app.Activities.PropertiesSettingActivity;
import com.fox.app.SampleApplication;
import avd.api.scanners.ScannerSe4500;
import avd.sdk.CompanionAPIConstants;

public class ScannerPropertiesActivity extends PropertiesSettingActivity {
	private enum ProperScannerValues {TimeOut, AimDuration, BiDirRedundancy}
	private boolean[]   properScannerValues = new boolean[ProperScannerValues.values().length];
	private boolean     allValuesAreProper = true;

	public static final int TimeOutTrueUpperBound = 99;
	public static final int TimeOutUpperBound = 255;
	
	public static final int AimDurationTrueUpperBound = 99;
	public static final int AimDurationUpperBound = 255;
	
	public static final int BidirRedundancyTrueUpperBound = 4;
	public static final int BidirRedundancyTrueLowerBound = 1;
	public static final int BidirRedundancyUpperBound = 15;

	private TextView tvTimeOutValue = null;
	private SeekBar sbTimeOut = null;

	private TextView tvAimDurationValue = null;
	private SeekBar sbAimDuration = null;

	private TextView tvBidirValue = null;
	private SeekBar sbBidir = null;
	
	private ScannerSe4500 scanner = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanner_properties);
	}
	
	private boolean checkOnLeaveScreen()
	{
		allValuesAreProper = true;
		for (int i = 0; i < properScannerValues.length; ++i)
		{
			allValuesAreProper = allValuesAreProper && properScannerValues[i];
			if (!allValuesAreProper)
				break;
		}
		
		return allValuesAreProper;
	}
	
	@Override
	public void onBackPressed() {

		boolean allValuesAreProper = checkOnLeaveScreen();

		if (allValuesAreProper) {
			super.onBackPressed();
			endSetSession();
		} else {
			showMessageBox("Set improper scanner settings", "Some of the settings were assigned values, that are not allowed." +
							"These values are highlighted in red, they will not be actually set, and an error will occur." +
							"Press \"Confirm\" to attempt setting improper values. Press \"Cancel\" to stay on this page without setting anything.",
					"Confirm", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							ScannerPropertiesActivity.super.onBackPressed();
							endSetSession();
						}
					}, "Cancel", null, device);
		}
	}

	private void endSetSession()
	{
		if (!isDeviceConnected())
			return;

		try {
			scanner.endSetSession();

			//If endSetSession did not throw an exception during setting improper parameters, we must force unloading of improper parameters
			if (!allValuesAreProper)
				unloadParameters();
		} catch (ApiConfigurationException e) {
			OnClickListener stopActivity = new OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					unloadParameters();
				}
			};

			showErrorMessageBox("Setting scanner parameters failed", e, "Ok", stopActivity, null, null, device);
		}
	}
	
	@Override
	public void refreshControls() throws ApiConfigurationException, ApiScannerException
	{
		scanner = (ScannerSe4500) device.getScanner();

		scanner.beginSetSession();

		if (!application.getDeviceData().isScannerParametersLoaded)
		{
			scanner.loadBaseSettings();
			application.getDeviceData().isScannerParametersLoaded = true;
		}

		RadioGroup rgScanMode = (RadioGroup) findViewById(R.id.rgScanMode);
		rgScanMode.check(getIDForCheckForScanMode());
		rgScanMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
			byte scanModeID;
			switch (checkedId) {
				case R.id.rbCompatible: {
					scanModeID = (byte) CompanionAPIConstants.SCN_SM_CON_COMPATIBLE;
					break;
				}
				case R.id.rbContinuous: {
					scanModeID = (byte) CompanionAPIConstants.SCN_SM_CON_CONTINUOUS;
					break;
				}
				case R.id.rbMomentary: {
					scanModeID = (byte) CompanionAPIConstants.SCN_SM_CON_MOMENTARY;
					break;
				}
				default:
					scanModeID = (byte) CompanionAPIConstants.SCN_SM_CON_CONTINUOUS;
					break;
			}

			try {
				scanner.setMode(ScanMode.getScanMode(scanModeID));
			} catch (ApiScannerException e) {
				showStandardErrorMessageBox("Parameter setting fail", e, device);
			}
			}
		});

		RadioGroup rgLinearSecurity = (RadioGroup) findViewById(R.id.rgLinearSecurity);
		rgLinearSecurity.check(getIDForCheckForLinearSecurity());
		rgLinearSecurity.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
			byte linearSecurityID;
			switch (checkedId) {
				case R.id.rb1: {
					linearSecurityID = 1;

					break;
				}
				case R.id.rb2: {
					linearSecurityID = 2;

					break;
				}
				case R.id.rb3: {
					linearSecurityID = 3;

					break;
				}
				case R.id.rb4: {
					linearSecurityID = 4;

					break;
				}
				default:
					linearSecurityID = 1;

					break;
			}

			try {
				scanner
						.setLinearSecurityLevel(linearSecurityID);
			} catch (ApiScannerException e) {
				showStandardErrorMessageBox("Parameter setting fail", e, device);
			}
			}
		});

		tvTimeOutValue = (TextView) findViewById(R.id.tvTimeOutValue);
		int timeOut = scanner.getDecodeSessionTimeout();
		// Adjusting timeOut if it an arithmetic overflow occurred due to timeOut being saved a single byte in SDK
		if (timeOut < 0)
			timeOut += 256;
		properScannerValues[ProperScannerValues.TimeOut.ordinal()] = timeOut <= TimeOutTrueUpperBound;
		if (properScannerValues[ProperScannerValues.TimeOut.ordinal()])
			tvTimeOutValue.setTextColor(android.graphics.Color.BLACK);
		else
			tvTimeOutValue.setTextColor(android.graphics.Color.RED);
		tvTimeOutValue.setText(String.valueOf(timeOut));

		sbTimeOut = (SeekBar) findViewById(R.id.sbTimeOut);
		sbTimeOut.setMax(TimeOutUpperBound);
		sbTimeOut.setProgress(timeOut);
		sbTimeOut.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar,
										  int progress, boolean fromUser) {
				properScannerValues[ProperScannerValues.TimeOut.ordinal()] = progress <= TimeOutTrueUpperBound;
				if (properScannerValues[ProperScannerValues.TimeOut.ordinal()])
					tvTimeOutValue.setTextColor(android.graphics.Color.BLACK);
				else
					tvTimeOutValue.setTextColor(android.graphics.Color.RED);

				tvTimeOutValue.setText(String.valueOf(progress));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					scanner.setDecodeSessionTimeout((byte) seekBar.getProgress());
				} catch (ApiScannerException e) {
					showStandardErrorMessageBox("Parameter setting fail", e, device);
				}
			}
		});

		tvAimDurationValue = (TextView) findViewById(R.id.tvAimDurationValue);
		int aimDuration = scanner.getAimDuration();
		// Adjusting aimDuration if it an arithmetic overflow occurred due to aimDuration being saved a single byte in SDK
		if (aimDuration < 0)
			aimDuration += 256;
		properScannerValues[ProperScannerValues.AimDuration.ordinal()] = aimDuration <= AimDurationTrueUpperBound;
		if (properScannerValues[ProperScannerValues.AimDuration.ordinal()])
			tvAimDurationValue.setTextColor(android.graphics.Color.BLACK);
		else
			tvAimDurationValue.setTextColor(android.graphics.Color.RED);
		tvAimDurationValue.setText(String.valueOf(aimDuration));

		sbAimDuration = (SeekBar) findViewById(R.id.sbAimDuration);
		sbAimDuration.setMax(AimDurationUpperBound);
		sbAimDuration.setProgress(aimDuration);
		sbAimDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar,
										  int progress, boolean fromUser) {
				properScannerValues[ProperScannerValues.AimDuration.ordinal()] = progress <= AimDurationTrueUpperBound;
				if (properScannerValues[ProperScannerValues.AimDuration.ordinal()])
					tvAimDurationValue.setTextColor(android.graphics.Color.BLACK);
				else
					tvAimDurationValue.setTextColor(android.graphics.Color.RED);

				tvAimDurationValue.setText(String.valueOf(progress));

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					scanner.setAimDuration((byte) seekBar.getProgress());
				} catch (ApiScannerException e) {
					showStandardErrorMessageBox("Parameter setting fail", e, device);
				}
			}
		});

		tvBidirValue = (TextView) findViewById(R.id.tvBidirValue);
		int bidir = scanner.getBidirRedundancy();
		properScannerValues[ProperScannerValues.BiDirRedundancy.ordinal()] = (bidir >= BidirRedundancyTrueLowerBound) && (bidir <= BidirRedundancyTrueUpperBound);
		if (properScannerValues[ProperScannerValues.BiDirRedundancy.ordinal()])
			tvBidirValue.setTextColor(android.graphics.Color.BLACK);
		else
			tvBidirValue.setTextColor(android.graphics.Color.RED);
		tvBidirValue.setText(String.valueOf(bidir));

		sbBidir = (SeekBar) findViewById(R.id.sbBidir);
		sbBidir.setMax(BidirRedundancyUpperBound);
		sbBidir.setProgress(bidir);
		sbBidir.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				properScannerValues[ProperScannerValues.BiDirRedundancy.ordinal()] = (progress >= BidirRedundancyTrueLowerBound) && (progress <= BidirRedundancyTrueUpperBound);
				if (properScannerValues[ProperScannerValues.BiDirRedundancy.ordinal()])
					tvBidirValue.setTextColor(android.graphics.Color.BLACK);
				else
					tvBidirValue.setTextColor(android.graphics.Color.RED);

				tvBidirValue.setText(String.valueOf(progress));

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					scanner.setBidirRedundancy((byte) seekBar.getProgress());
				} catch (ApiScannerException e) {
					showStandardErrorMessageBox("Parameter setting fail", e, device);
				}
			}
		});


		
		ToggleButton tbSelectiveScanningValue = (ToggleButton) findViewById(R.id.tbSelectiveScanningValue);
		tbSelectiveScanningValue.setChecked(scanner.isPickListModeEnabled());
		tbSelectiveScanningValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				try {
					enableSelectiveScanningValue(isChecked);
				} catch (ApiScannerException e) {
					showStandardErrorMessageBox("Parameter setting fail", e, device);
				}
			}
		});

		Button btnBarcodeSettings = (Button) findViewById(R.id.btnBarcodeSettings);
		btnBarcodeSettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showBarcodeSettings();
			}
		});

		Button btnResetSettings = (Button) findViewById(R.id.btnResetSettings);
		btnResetSettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSettings();
			}
		});
	}

	private void enableSelectiveScanningValue(Boolean value) throws ApiScannerException {
		scanner.setIsPickListModeEnabled(value);
	}
	
	@Override
	protected void unloadParameters() {
		application.getDeviceData().isScannerParametersLoaded = false;
		application.getDeviceData().isBarcodeParametersLoaded = false;
		scanner.unloadSettings();
	}

	private void resetSettings() {
		showMessageBox("Reset scanner settings",
				"Are you sure you want to reset all scanner settings to their default values?",
				"Yes", new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						try {
							application.getDeviceData().isScannerParametersLoaded = false;
							application.getDeviceData().isBarcodeParametersLoaded = false;
							scanner.resetConfiguration();
							application.getDeviceData().isScannerParametersLoaded = true;
							application.getDeviceData().isBarcodeParametersLoaded = true;
						} catch (ApiException e) {
							showStandardErrorMessageBox("Parameter resetting fail", e, device);
						}

						sendBroadcast(new Intent(SampleApplication.INTENT_ACTION_UPDATE_SCREEN_CONTROLS));
					}
				}, "No", null, device);
	}

	private void gotoBarcodeScreen()
	{
		if (!application.getDeviceData().isBarcodeParametersLoaded)
		{
			try
    		{ 
				scanner.loadSettings();
				application.getDeviceData().isBarcodeParametersLoaded = true;
    		}
    		catch (ApiException e)
    		{
    			showErrorMessageBox("Obtaining barcode parameters failed", e, "Ok", null, null, null, device);
    		}
			
		}
		Intent propertiesIntent = new Intent(this, BarcodeSettingsActivity.class);
		startActivity(propertiesIntent);
	}
	
	private void showBarcodeSettings() {
		allValuesAreProper = checkOnLeaveScreen();
		
		if (allValuesAreProper) {
			gotoBarcodeScreen();
			endSetSession();
		}
		else
		{
			showMessageBox("Improper scanner settings", "Some of the settings were assigned values, that are not allowed. " +
							"This will result in error, and you wouldn't be able to work with barcodes after that. " +
							"Please, assign correct values to the parameters or return to previous screen to set improper values and trigger appropriate error messages.",
					"Ok", null, null, null, device);
		}
	}

	private int getIDForCheckForScanMode() throws ApiScannerException {
		int retVal;
		switch (scanner.getMode()) {
		case Compatible:
			retVal = R.id.rbCompatible;
			break;

		case Continuous:
			retVal = R.id.rbContinuous;
			break;

		case Momentary:
			retVal = R.id.rbMomentary;
			break;

		default:
			retVal = R.id.rbContinuous;

			break;
		}
		return retVal;
	}

	private int getIDForCheckForLinearSecurity() throws ApiScannerException {
		int retVal;
		switch (scanner.getLinearSecurityLevel()) {
		case 1:
			retVal = R.id.rb1;
			break;

		case 2:
			retVal = R.id.rb2;
			break;

		case 3:
			retVal = R.id.rb3;
			break;

		case 4:
			retVal = R.id.rb4;
			break;

		default:
			retVal = R.id.rb1;
			break;
		}
		return retVal;
	}

	@Override
	protected String getMessageObtainingParametersFailed() {
		return "Obtaining scanner parameters failed";
	}
}
