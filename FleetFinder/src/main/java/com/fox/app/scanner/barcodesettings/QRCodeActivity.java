package com.fox.app.scanner.barcodesettings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.avery.sampleapp.R;

import avd.api.barcodes.twodimensional.InverseScanningMode;
import avd.api.barcodes.twodimensional.QrCode;
import avd.api.core.exceptions.ApiConfigurationException;
import avd.api.core.exceptions.ApiScannerException;
import avd.api.scanners.ScannerSe4500;
import avd.sdk.CompanionAPIErrors;

public class QRCodeActivity extends BarcodePropertiesSettingActivity {

	private QrCode qrCode = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qrcode);
	}

	@Override
	public void refreshControls() throws ApiScannerException
	{
		qrCode = ((ScannerSe4500) device.getScanner()).getQrCode();

		ToggleButton tbEnabledValue = (ToggleButton) findViewById(R.id.tbEnabledValue);
		tbEnabledValue.setChecked(qrCode.isEnabled());
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

		RadioGroup rgInverseMode = (RadioGroup) findViewById(R.id.rgInverseMode);
		rgInverseMode.check(getIDForCheckInverseScanning());
		rgInverseMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				int idToCheck = 0;
				switch (checkedId) {
				case R.id.rbInverseMode_Regular: {
					idToCheck = 0;

					break;
				}
				case R.id.rbInverseMode_Inverse: {
					idToCheck = 1;

					break;
				}
				case R.id.rbInverseMode_Both: {
					idToCheck = 2;
					break;
				}
				}
				try {
					qrCode.setInverseMode(InverseScanningMode.getInverseScanningMode(idToCheck));
				} catch (ApiScannerException e) {
					showStandardErrorMessageBox("Setting parameter failed", e, device);
				}
			}
		});

		ToggleButton tvMicroEnabledValue = (ToggleButton) findViewById(R.id.tvMicroEnabledValue);
		tvMicroEnabledValue.setChecked(qrCode.isMicroEnabled());
		tvMicroEnabledValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableMicro(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

	}

	private void enable(Boolean value) throws ApiScannerException {
		qrCode.setEnabled(value);
	}

	private void enableMicro(Boolean value) throws ApiScannerException {
		qrCode.setMicroEnabled(value);
	}
	
	private int getIDForCheckInverseScanning() throws ApiScannerException
	{
		int idToCheck = 0;
		switch (qrCode.getInverseMode().getValue()) {
		case 0: {
			idToCheck = R.id.rbInverseMode_Regular;
			break;
		}
		case 1: {
			idToCheck = R.id.rbInverseMode_Inverse;
			break;
		}
		case 2: {
			idToCheck = R.id.rbInverseMode_Both;
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
