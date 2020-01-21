package com.fox.app.scanner.barcodesettings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.avery.sampleapp.R;

import avd.api.barcodes.onedimensional.I2of5;
import avd.api.core.baseimplementation.BaseBarcode;
import avd.api.core.exceptions.ApiConfigurationException;
import avd.api.core.exceptions.ApiScannerException;
import avd.api.scanners.ScannerSe4500;

public class I2of5Activity extends BarcodePropertiesSettingActivity {

	private I2of5 i2of5 = null;

	private TextView tvLength1Value = null;
	private SeekBar sbLength1 = null;

	private TextView tvLength2Value = null;
	private SeekBar sbLength2 = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_i2of5);
	}

	@Override
	public void refreshControls() throws ApiScannerException
	{
		i2of5 = ((ScannerSe4500) device.getScanner()).getI2of5();

		ToggleButton tbEnabledValue = (ToggleButton) findViewById(R.id.tbEnabledValue);
		tbEnabledValue.setChecked(i2of5.isEnabled());
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

		int length1 = i2of5.getLength1();
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
							i2of5.setLength1(seekBar.getProgress());
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}

					}
				});

		int length2 = i2of5.getLength2();

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
							i2of5.setLength2(seekBar.getProgress());
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tvIntegrityCheckValue = (ToggleButton) findViewById(R.id.tvIntegrityCheckValue);
		tvIntegrityCheckValue.setChecked(i2of5.isIntegrityCheckEnabled());
		tvIntegrityCheckValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableIntegrityCheck(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tvConversionToean13Value = (ToggleButton) findViewById(R.id.tvConversionToean13Value);
		tvConversionToean13Value.setChecked(i2of5.isConversionToEan13Enabled());
		tvConversionToean13Value
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableConversionToEan13S(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tvCheckDigitstransmissionValue = (ToggleButton) findViewById(R.id.tvCheckDigitstransmissionValue);
		tvCheckDigitstransmissionValue.setChecked(i2of5
				.isCheckDigitsTransmissionEnabled());
		tvCheckDigitstransmissionValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableDigitTransmision(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

	}

	private void enable(Boolean value) throws ApiScannerException {
		i2of5.setEnabled(value);
	}

	private void enableIntegrityCheck(Boolean value) throws ApiScannerException {
		i2of5.setIntegrityCheckEnabled(value);
	}

	private void enableConversionToEan13S(Boolean value)
			throws ApiScannerException {
		i2of5.setConversionToEan13Enabled(value);
	}

	private void enableDigitTransmision(Boolean value)
			throws ApiScannerException {
		i2of5.setCheckDigitsTransmissionEnabled(value);
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
