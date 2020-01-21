package com.fox.app.scanner.barcodesettings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.avery.sampleapp.R;

import avd.api.barcodes.onedimensional.Upc;
import avd.api.barcodes.onedimensional.UpcPreambleTransmissionMode;
import avd.api.core.baseimplementation.BaseBarcode;
import avd.api.core.exceptions.ApiConfigurationException;
import avd.api.core.exceptions.ApiScannerException;
import avd.api.scanners.ScannerSe4500;
import avd.sdk.CompanionAPIErrors;

public class UPCActivity extends BarcodePropertiesSettingActivity {

	private Upc upc = null;

	private TextView tvSupplementalModeValue = null;
	private SeekBar sbSupplementalMode = null;

	private TextView tvSupplementalRedundancyValue = null;
	private SeekBar sbSupplementalRedundancy = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upc);
	}

	@Override
	public void refreshControls() throws ApiScannerException
	{
		upc = ((ScannerSe4500) device.getScanner()).getUpc();

		ToggleButton tbEnabledValue = (ToggleButton) findViewById(R.id.tbEnabledValue);
		tbEnabledValue.setChecked(upc.isEnabled());
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

		ToggleButton tbUPCEEnabledValue = (ToggleButton) findViewById(R.id.tbUPCEEnabledValue);
		tbUPCEEnabledValue.setChecked(upc.isUpceEnabled());
		tbUPCEEnabledValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableUPCE(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tbUPCE1EnabledValue = (ToggleButton) findViewById(R.id.tbUPCE1EnabledValue);
		tbUPCE1EnabledValue.setChecked(upc.isUpce1Enabled());
		tbUPCE1EnabledValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableUPCE1(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tbEAN8EnabledValue = (ToggleButton) findViewById(R.id.tbEAN8EnabledValue);
		tbEAN8EnabledValue.setChecked(upc.isEan8Enabled());
		tbEAN8EnabledValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableEAN8(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tbEAN13EnabledValue = (ToggleButton) findViewById(R.id.tbEAN13EnabledValue);
		tbEAN13EnabledValue.setChecked(upc.isEan13Enabled());
		tbEAN13EnabledValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableEAN13(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tbEANBooklandEnabledValue = (ToggleButton) findViewById(R.id.tbEANBooklandEnabledValue);
		tbEANBooklandEnabledValue.setChecked(upc.isEanBooklandEnabled());
		tbEANBooklandEnabledValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableEANBookland(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		int supplementalMode = upc.getSupplementalMode();

		tvSupplementalModeValue = (TextView) findViewById(R.id.tvSupplementalModeValue);
		tvSupplementalModeValue.setText(String.valueOf(supplementalMode));

		sbSupplementalMode = (SeekBar) findViewById(R.id.sbSupplementalMode);
		sbSupplementalMode.setMax(BaseBarcode.SupplementalModeUpperBound);
		sbSupplementalMode.setProgress(supplementalMode);
		sbSupplementalMode
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						tvSupplementalModeValue.setText(String
								.valueOf(progress));

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						try {
							upc.setSupplementalMode(seekBar.getProgress());
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		int supplementalRedundancy = upc.getSupplementalRedundancy();

		tvSupplementalRedundancyValue = (TextView) findViewById(R.id.tvSupplementalRedundancyValue);
		tvSupplementalRedundancyValue.setText(String
				.valueOf(supplementalRedundancy));

		sbSupplementalRedundancy = (SeekBar) findViewById(R.id.sbSupplementalRedundancy);
		sbSupplementalRedundancy.setMax(BaseBarcode.SupplementalRedundancyUpperBound - BaseBarcode.SupplementalRedundancyLowerBound);
		sbSupplementalRedundancy.setProgress(supplementalRedundancy - BaseBarcode.SupplementalRedundancyLowerBound);
		sbSupplementalRedundancy
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						tvSupplementalRedundancyValue.setText(String
								.valueOf(progress + BaseBarcode.SupplementalRedundancyLowerBound));

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						try {
							upc.setSupplementalRedundancy(seekBar.getProgress() + BaseBarcode.SupplementalRedundancyLowerBound);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tvUPCACheckDigitsValue = (ToggleButton) findViewById(R.id.tvUPCACheckDigitsValue);
		tvUPCACheckDigitsValue.setChecked(upc
				.isUpcaCheckDigitsTransmissionEnabled());
		tvUPCACheckDigitsValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableDigitUPCA(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tvUPCECheckDigitsValue = (ToggleButton) findViewById(R.id.tvUPCECheckDigitsValue);
		tvUPCECheckDigitsValue.setChecked(upc
				.isUpceCheckDigitsTransmissionEnabled());
		tvUPCECheckDigitsValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableDigitUPCE(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tvUPCE1CheckDigitsValue = (ToggleButton) findViewById(R.id.tvUPCE1CheckDigitsValue);
		tvUPCE1CheckDigitsValue.setChecked(upc
				.isUpce1CheckDigitsTransmissionEnabled());
		tvUPCE1CheckDigitsValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableDigitUPCE1(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		RadioGroup rgUPCAPreambleMode = (RadioGroup) findViewById(R.id.rgUPCAPreambleMode);
		rgUPCAPreambleMode.check(getIDForCheckUpcaPreambleMode());
		rgUPCAPreambleMode
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						int idToCheck = 0;
						switch (checkedId) {
						case R.id.rbUPCAPreambleModeNone: {
							idToCheck = 0;
							break;
						}
						case R.id.rbUPCAPreambleModeAll: {
							idToCheck = 1;
							break;
						}
						case R.id.rbUPCAPreambleModeCountryCode: {
							idToCheck = 2;
							break;
						}
						}
						try {
							upc.setUpcaPreambleMode(UpcPreambleTransmissionMode.getUpcPreambleTransmissionMode(idToCheck));
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		RadioGroup rgUPCEPreambleMode = (RadioGroup) findViewById(R.id.rgUPCEPreambleMode);
		rgUPCEPreambleMode.check(getIDForCheckUpcePreambleMode());
		rgUPCEPreambleMode
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						int idToCheck = 0;

						switch (checkedId) {
						case R.id.rbUPCEPreambleModeNone: {
							idToCheck = 0;

							break;
						}
						case R.id.rbUPCEPreambleModeAll: {
							idToCheck = 1;

							break;
						}
						case R.id.rbUPCEPreambleModeCountryCode: {
							idToCheck = 2;
							break;
						}

						}
						try {
							upc.setUpcePreambleMode(UpcPreambleTransmissionMode.getUpcPreambleTransmissionMode(idToCheck));
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		RadioGroup rgUPCE1PreambleMode = (RadioGroup) findViewById(R.id.rgUPCE1PreambleMode);
		rgUPCE1PreambleMode.check(getIDForCheckUpce1PreambleMode());
		rgUPCE1PreambleMode
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						int idToCheck = 0;
						switch (checkedId) {
						case R.id.rbUPCE1PreambleModeNone: {
							idToCheck = 0;
							break;
						}
						case R.id.rbUPCE1PreambleModeAll: {
							idToCheck = 1;
							break;
						}
						case R.id.rbUPCE1PreambleModeCountryCode: {
							idToCheck = 2;
							break;
						}
						}
						try {
							upc.setUpce1PreambleMode(UpcPreambleTransmissionMode.getUpcPreambleTransmissionMode(idToCheck));
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tvCouponCodeValue = (ToggleButton) findViewById(R.id.tvCouponCodeValue);
		tvCouponCodeValue.setChecked(upc.isCouponCodeEnabled());
		tvCouponCodeValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableCouponCode(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tvUPCEtoUPCAConversionValue = (ToggleButton) findViewById(R.id.tvUPCEtoUPCAConversionValue);
		tvUPCEtoUPCAConversionValue.setChecked(upc
				.isUpceToUpcaConversionEnabled());
		tvUPCEtoUPCAConversionValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableUPCEToUPCAConversion(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tvUPCE1toUPCAConversionValue = (ToggleButton) findViewById(R.id.tvUPCE1toUPCAConversionValue);
		tvUPCE1toUPCAConversionValue.setChecked(upc
				.isUpce1ToUpcaConversionEnabled());
		tvUPCE1toUPCAConversionValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableUPCE1ToUPCAConversion(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tvEAN8ZerosExtentionValue = (ToggleButton) findViewById(R.id.tvEAN8ZerosExtentionValue);
		tvEAN8ZerosExtentionValue.setChecked(upc.isEan8ZerosExtensionEnabled());
		tvEAN8ZerosExtentionValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableEAN8ZerosExtention(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tvEAN8ToEAN13ConversionValue = (ToggleButton) findViewById(R.id.tvEAN8ToEAN13ConversionValue);
		tvEAN8ToEAN13ConversionValue.setChecked(upc
				.isEan8ToEan13ConversionEnabled());
		tvEAN8ToEAN13ConversionValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableEAN8ToEAN13Conversion(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

	}
	
	private int getIDForCheckUpcaPreambleMode() throws ApiScannerException
	{
		int idToCheck = 0;
		switch (upc.getUpcaPreambleMode().getValue()) {
		case 0: {
			idToCheck = R.id.rbUPCAPreambleModeNone;
			break;
		}
		case 1: {
			idToCheck = R.id.rbUPCAPreambleModeAll;
			break;
		}
		case 2: {
			idToCheck = R.id.rbUPCAPreambleModeCountryCode;
			break;
		}
		default:
			throw new ApiScannerException(null, CompanionAPIErrors.CD_ERROR_INVALID_PARAMETER);
		}
		
		return idToCheck;
	}
	
	private int getIDForCheckUpcePreambleMode() throws ApiScannerException
	{
		int idToCheck = 0;
		switch (upc.getUpcePreambleMode().getValue()) {
		case 0: {
			idToCheck = R.id.rbUPCEPreambleModeNone;
			break;
		}
		case 1: {
			idToCheck = R.id.rbUPCEPreambleModeAll;
			break;
		}
		case 2: {
			idToCheck = R.id.rbUPCEPreambleModeCountryCode;
			break;
		}
		default:
			throw new ApiScannerException(null, CompanionAPIErrors.CD_ERROR_INVALID_PARAMETER);
		}
		
		return idToCheck;
	}
	
	private int getIDForCheckUpce1PreambleMode() throws ApiScannerException
	{
		int idToCheck = 0;
		switch (upc.getUpce1PreambleMode().getValue()) {
		case 0: {
			idToCheck = R.id.rbUPCE1PreambleModeNone;
			break;
		}
		case 1: {
			idToCheck = R.id.rbUPCE1PreambleModeAll;
			break;
		}
		case 2: {
			idToCheck = R.id.rbUPCE1PreambleModeCountryCode;
			break;
		}
		default:
			throw new ApiScannerException(null, CompanionAPIErrors.CD_ERROR_INVALID_PARAMETER);
		}
		
		return idToCheck;
	}

	private void enable(Boolean value) throws ApiScannerException {
		upc.setEnabled(value);
	}

	private void enableUPCE(Boolean value) throws ApiScannerException {
		upc.setUpceEnabled(value);
	}

	private void enableUPCE1(Boolean value) throws ApiScannerException {
		upc.setUpce1Enabled(value);
	}

	private void enableEAN8(Boolean value) throws ApiScannerException {
		upc.setEan8Enabled(value);
	}

	private void enableEAN13(Boolean value) throws ApiScannerException {
		upc.setEan13Enabled(value);
	}

	private void enableEANBookland(Boolean value) throws ApiScannerException {
		upc.setEanBooklandEnabled(value);
	}

	private void enableDigitUPCA(Boolean value) throws ApiScannerException {
		upc.setUpcaCheckDigitsTransmissionEnabled(value);
	}

	private void enableDigitUPCE(Boolean value) throws ApiScannerException {
		upc.setUpceCheckDigitsTransmissionEnabled(value);
	}

	private void enableDigitUPCE1(Boolean value) throws ApiScannerException {
		upc.setUpce1CheckDigitsTransmissionEnabled(value);
	}

	private void enableCouponCode(Boolean value) throws ApiScannerException {
		upc.setCouponCodeEnabled(value);
	}

	private void enableUPCEToUPCAConversion(Boolean value)
			throws ApiScannerException {
		upc.setUpceToUpcaConversionEnabled(value);
	}

	private void enableUPCE1ToUPCAConversion(Boolean value)
			throws ApiScannerException {
		upc.setUpce1ToUpcaConversionEnabled(value);
	}

	private void enableEAN8ZerosExtention(Boolean value)
			throws ApiScannerException {
		upc.setEan8ZerosExtensionEnabled(value);
	}

	private void enableEAN8ToEAN13Conversion(Boolean value)
			throws ApiScannerException {
		upc.setEan8ToEan13ConversionEnabled(value);
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
