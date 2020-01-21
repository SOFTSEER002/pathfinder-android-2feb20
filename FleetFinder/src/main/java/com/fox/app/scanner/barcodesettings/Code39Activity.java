package com.fox.app.scanner.barcodesettings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.avery.sampleapp.R;
import avd.api.barcodes.onedimensional.Code39;
import avd.api.core.baseimplementation.BaseBarcode;
import avd.api.core.exceptions.ApiConfigurationException;
import avd.api.core.exceptions.ApiScannerException;
import avd.api.scanners.ScannerSe4500;

public class Code39Activity extends BarcodePropertiesSettingActivity {

	private Code39 code39 = null;
	private TextView tvLength1Value = null;
	private SeekBar sbLength1 = null;

	private TextView tvLength2Value = null;
	private SeekBar sbLength2 = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_code39);
	}

	@Override
	public void refreshControls() throws ApiScannerException
	{
		code39 = ((ScannerSe4500) device.getScanner()).getCode39();

		ToggleButton tbEnabledValue = (ToggleButton) findViewById(R.id.tbEnabledValue);
		tbEnabledValue.setChecked(code39.isEnabled());
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

		int length1 = code39.getLength1();
		tvLength1Value = (TextView) findViewById(R.id.tvLength1Value);
		tvLength1Value.setText(String.valueOf(length1));

		sbLength1 = (SeekBar) findViewById(R.id.sbLength1);
		sbLength1.setMax(BaseBarcode.LengthUpperBound);
		sbLength1.setProgress(length1);
		sbLength1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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
							code39.setLength1(seekBar.getProgress());
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}

					}
				});

		int length2 = code39.getLength2();

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
							code39.setLength2(seekBar.getProgress());
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tbPrefixAValue = (ToggleButton) findViewById(R.id.tbPrefixAValue);
		tbPrefixAValue.setChecked(code39.isPrefixAEnabled());
		tbPrefixAValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enablePrefixA(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tbFullAsciiValue = (ToggleButton) findViewById(R.id.tbFullAsciiValue);
		tbFullAsciiValue.setChecked(code39.isFullAsciiEnabled());
		tbFullAsciiValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableFullAscii(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});
		ToggleButton tvTrioptricEnabledValue = (ToggleButton) findViewById(R.id.tvTrioptricEnabledValue);
		tvTrioptricEnabledValue.setChecked(code39.isTrioptricEnabled());
		tvTrioptricEnabledValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableTrioptric(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});
		ToggleButton tvIntegrityCheckValue = (ToggleButton) findViewById(R.id.tvIntegrityCheckValue);
		tvIntegrityCheckValue.setChecked(code39.isIntegrityCheckEnabled());
		tvIntegrityCheckValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableIntegrity(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});
		ToggleButton tvConversionToCode32Value = (ToggleButton) findViewById(R.id.tvConversionToCode32Value);
		tvConversionToCode32Value.setChecked(code39
				.isConversionToCode32Enabled());
		tvConversionToCode32Value
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableConversionTo32(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});
		ToggleButton tvCheckDigitTransmissionValue = (ToggleButton) findViewById(R.id.tvCheckDigitTransmissionValue);
		tvCheckDigitTransmissionValue.setChecked(code39
				.isCheckDigitsTransmissionEnabled());
		tvCheckDigitTransmissionValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableDigitTransmisionValue(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

	}

	private void enable(Boolean value) throws ApiScannerException{
		code39.setEnabled(value);
	}

	private void enablePrefixA(Boolean value) throws ApiScannerException{
		code39.setPrefixAEnabled(value);
	}

	private void enableFullAscii(Boolean value) throws ApiScannerException{
		code39.setFullAsciiEnabled(value);
	}

	private void enableTrioptric(Boolean value) throws ApiScannerException{
		code39.setTrioptricEnabled(value);
	}

	private void enableIntegrity(Boolean value) throws ApiScannerException{
		code39.setIntegrityCheckEnabled(value);
	}

	private void enableConversionTo32(Boolean value) throws ApiScannerException{
		code39.setConversionToCode32Enabled(value);
	}

	private void enableDigitTransmisionValue(Boolean value) throws ApiScannerException{
		code39.setCheckDigitsTransmissionEnabled(value);
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
