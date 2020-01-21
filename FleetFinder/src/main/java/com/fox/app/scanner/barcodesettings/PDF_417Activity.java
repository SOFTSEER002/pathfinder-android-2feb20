package com.fox.app.scanner.barcodesettings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.avery.sampleapp.R;

import avd.api.barcodes.twodimensional.Pdf417;
import avd.api.core.exceptions.ApiConfigurationException;
import avd.api.core.exceptions.ApiScannerException;
import avd.api.scanners.ScannerSe4500;

public class PDF_417Activity extends BarcodePropertiesSettingActivity {

	private Pdf417 pdf417 = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pdf_417);
	}

	@Override
	public void refreshControls() throws ApiScannerException
	{
		pdf417 = ((ScannerSe4500) device.getScanner()).getPdf417();

		ToggleButton tbEnabledValue = (ToggleButton) findViewById(R.id.tbEnabledValue);
		tbEnabledValue.setChecked(pdf417.isEnabled());
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

		ToggleButton tbMicroEnabledValue = (ToggleButton) findViewById(R.id.tbMicroEnabledValue);
		tbMicroEnabledValue.setChecked(pdf417.isMicroEnabled());
		tbMicroEnabledValue
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

		ToggleButton tbCode128EmulationValue = (ToggleButton) findViewById(R.id.tbCode128EmulationValue);
		tbCode128EmulationValue.setChecked(pdf417.isCode128EmulationEnabled());
		tbCode128EmulationValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableCode128Emulation(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

	}

	private void enable(Boolean value) throws ApiScannerException {
		pdf417.setEnabled(value);
	}

	private void enableMicro(Boolean value) throws ApiScannerException {
		pdf417.setMicroEnabled(value);
	}

	private void enableCode128Emulation(Boolean value)
			throws ApiScannerException {
		pdf417.setCode128EmulationEnabled(value);
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
