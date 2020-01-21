package com.fox.app.scanner.barcodesettings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.avery.sampleapp.R;

import avd.api.barcodes.twodimensional.DataMatrix;
import avd.api.barcodes.twodimensional.InverseScanningMode;
import avd.api.barcodes.twodimensional.MirrorDecodingMode;
import avd.api.core.exceptions.ApiConfigurationException;
import avd.api.core.exceptions.ApiScannerException;
import avd.api.scanners.ScannerSe4500;
import avd.sdk.CompanionAPIErrors;

public class DataMatrixActivity extends BarcodePropertiesSettingActivity {

	private DataMatrix dataMatrix;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_matrix);
	}
	
	@Override
	public void refreshControls() throws ApiScannerException
	{
		dataMatrix = ((ScannerSe4500) device.getScanner()).getDataMatrix();

		ToggleButton tbEnabledValue = (ToggleButton) findViewById(R.id.tbEnabledValue);
		tbEnabledValue.setChecked(dataMatrix.isEnabled());
		tbEnabledValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enable(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		RadioGroup rgInverseScanningMode = (RadioGroup) findViewById(R.id.rgInverseScanningMode);
		rgInverseScanningMode.check(getIDForCheckInverseScanning());
		rgInverseScanningMode
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						int idToCheck = 0;
						switch (checkedId) {
						case R.id.rbInverseScanningModeRegular: {
							idToCheck = 0;
							break;
						}
						case R.id.rbInverseScanningModeInverse: {
							idToCheck = 1;

							break;
						}
						case R.id.rbInverseScanningModeBoth: {
							idToCheck = 2;
							break;
						}
						}
						try {
							dataMatrix.setInverseScanning(InverseScanningMode.getInverseScanningMode(idToCheck));
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		RadioGroup rgMirrorDecoding = (RadioGroup) findViewById(R.id.rgMirrorDecoding);
		rgMirrorDecoding.check(getIDForCheckMirrorDecoding());
		rgMirrorDecoding
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {

						int idToCheck = 0;

						switch (checkedId) {
						case R.id.rbMirrorDecodingNever: {
							idToCheck = 0;
							break;
						}
						case R.id.rbMirrorDecodingAlways: {
							idToCheck = 1;
							break;
						}
						case R.id.rbMirrorDecodingAutodetect: {
							idToCheck = 2;

							break;
						}
						}
						try {
							dataMatrix.setMirrorDecoding(MirrorDecodingMode.getMirrorDecodingMode(idToCheck));
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});
	}

	private void enable(Boolean value) throws ApiScannerException {
		dataMatrix.setEnabled(value);
	}
	
	private int getIDForCheckInverseScanning() throws ApiScannerException
	{
		int idToCheck = 0;
		switch (dataMatrix.getInverseScanning().getValue()) {
		case 0: {
			idToCheck = R.id.rbInverseScanningModeRegular;
			break;
		}
		case 1: {
			idToCheck = R.id.rbInverseScanningModeInverse;
			break;
		}
		case 2: {
			idToCheck = R.id.rbInverseScanningModeBoth;
			break;
		}
		default:
			throw new ApiScannerException(null, CompanionAPIErrors.CD_ERROR_INVALID_PARAMETER);
		}
		
		return idToCheck;
	}
	
	private int getIDForCheckMirrorDecoding() throws ApiScannerException
	{
		int idToCheck = 0;
		switch (dataMatrix.getMirrorDecoding().getValue()) {
		case 0: {
			idToCheck = R.id.rbMirrorDecodingNever;
			break;
		}
		case 1: {
			idToCheck = R.id.rbMirrorDecodingAlways;
			break;
		}
		case 2: {
			idToCheck = R.id.rbMirrorDecodingAutodetect;
			break;
		}
		default:
			throw new ApiScannerException(null, CompanionAPIErrors.CD_ERROR_INVALID_PARAMETER);
		}
		
		return idToCheck;
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		try {
			device.getScanner().endSetSession();
		} catch (ApiConfigurationException e) {
			OnClickListener stopActivity = new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					application.getDeviceData().isScannerParametersLoaded = false;
					device.getScanner().unloadSettings();
				}
			};
				
			showErrorMessageBox("Setting barcode parameters failed", e, "Ok", stopActivity, null, null, device);
		}
		
	}
}
