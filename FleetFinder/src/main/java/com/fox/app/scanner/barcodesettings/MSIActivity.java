package com.fox.app.scanner.barcodesettings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.avery.sampleapp.R;

import avd.api.barcodes.onedimensional.Msi;
import avd.api.core.baseimplementation.BaseBarcode;
import avd.api.core.exceptions.ApiConfigurationException;
import avd.api.core.exceptions.ApiScannerException;
import avd.api.scanners.ScannerSe4500;

public class MSIActivity extends BarcodePropertiesSettingActivity {

	private Msi msi = null;

	private TextView tvLength1Value = null;
	private SeekBar sbLength1 = null;

	private TextView tvLength2Value = null;
	private SeekBar sbLength2 = null;
	
	ToggleButton tvTwoChecksumsValue = null;
	ToggleButton tvMod10Mod11Value = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_msi);
	}

	@Override
	public void refreshControls() throws ApiScannerException
	{
		msi = ((ScannerSe4500) device.getScanner()).getMsi();

		tvTwoChecksumsValue = (ToggleButton) findViewById(R.id.tvTwoChecksumsValue);
		tvMod10Mod11Value = (ToggleButton) findViewById(R.id.tvMod10Mod11Value);

		ToggleButton tbEnabledValue = (ToggleButton) findViewById(R.id.tbEnabledValue);
		tbEnabledValue.setChecked(msi.isEnabled());
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

		int length1 = msi.getLength1();
		tvLength1Value = (TextView) findViewById(R.id.tvLength1Value);
		tvLength1Value.setText(String.valueOf(length1));

		sbLength1 = (SeekBar) findViewById(R.id.sbLength1);
		sbLength1.setMax(BaseBarcode.LengthUpperBound);
		sbLength1.setProgress(length1);
		sbLength1
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						tvLength1Value.setText(String.valueOf(progress));

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						try {
							msi.setLength1(seekBar.getProgress());
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}

					}
				});

		int length2 = msi.getLength2();

		tvLength2Value = (TextView) findViewById(R.id.tvLength2Value);
		tvLength2Value.setText(String.valueOf(length2));

		sbLength2 = (SeekBar) findViewById(R.id.sbLength2);
		sbLength2.setMax(BaseBarcode.LengthUpperBound);
		sbLength2.setProgress(length2);
		sbLength2
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						tvLength2Value.setText(String.valueOf(progress));

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						try {
							msi.setLength2(seekBar.getProgress());
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		
		ToggleButton tvCheckDigitstransmissionValue = (ToggleButton) findViewById(R.id.tvCheckDigitstransmissionValue);
		tvCheckDigitstransmissionValue.setChecked(msi.isCheckDigitsTransmissionEnabled());
		tvCheckDigitstransmissionValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableDigitTransmission(isChecked);
							
							//Mod10Mod11 and twoChecksums can be enabled only if checkDigit is enabled
						    //Implementing the dependency.
							enableTwoCheckSumButton();
							enableMod10_Mod11Button();
							
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		
		enableTwoCheckSumButton();
		tvTwoChecksumsValue.setChecked(msi.isTwoChecksumsEnabled());
		tvTwoChecksumsValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableTwoCheckSum(isChecked);
							//Mod10Mod11 can be enabled only if checkDigit and twoChecksums are enabled simultaneously
						    //Implementing the dependency.
							enableMod10_Mod11Button();
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});
		
		enableMod10_Mod11Button();
		tvMod10Mod11Value.setChecked(msi.isMod10Mod11Enabled());
		tvMod10Mod11Value
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableMod10_Mod11(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

	}

	private void enable(Boolean value) throws ApiScannerException {
		msi.setEnabled(value);
	}

	private void enableDigitTransmission(Boolean value)
			throws ApiScannerException {
		msi.setCheckDigitsTransmissionEnabled(value);
	}

	private void enableTwoCheckSum(Boolean value) throws ApiScannerException {
		msi.setTwoChecksumsEnabled(value);
	}
	
	private void enableTwoCheckSumButton() throws ApiScannerException {
		if (msi.isCheckDigitsTransmissionEnabled())
			tvTwoChecksumsValue.setEnabled(true);
		else
		{
			tvTwoChecksumsValue.setChecked(false);
			tvTwoChecksumsValue.setEnabled(false);
			
		}
	}

	private void enableMod10_Mod11(Boolean value) throws ApiScannerException {
		msi.setMod10Mod11Enabled(value);
	}
	
	private void enableMod10_Mod11Button() throws ApiScannerException {
		if (msi.isCheckDigitsTransmissionEnabled() && msi.isTwoChecksumsEnabled())
			tvMod10Mod11Value.setEnabled(true);
		else
		{
			tvMod10Mod11Value.setChecked(false);
			tvMod10Mod11Value.setEnabled(false);
		}
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
