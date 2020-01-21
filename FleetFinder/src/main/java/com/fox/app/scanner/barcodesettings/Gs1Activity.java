  package com.fox.app.scanner.barcodesettings;

  import android.content.DialogInterface;
  import android.content.DialogInterface.OnClickListener;
  import android.os.Bundle;
  import android.widget.CompoundButton;
  import android.widget.ToggleButton;

  import com.avery.sampleapp.R;

  import avd.api.barcodes.onedimensional.Gs1;
  import avd.api.core.exceptions.ApiConfigurationException;
  import avd.api.core.exceptions.ApiScannerException;
  import avd.api.scanners.ScannerSe4500;

public class Gs1Activity extends BarcodePropertiesSettingActivity {

	private Gs1 gs1 = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gs1);
	}

	@Override
	public void refreshControls() throws ApiScannerException
	{
		gs1 = ((ScannerSe4500) device.getScanner()).getGs1();

		ToggleButton tbEnabledValue = (ToggleButton) findViewById(R.id.tbEnabledValue);
		tbEnabledValue.setChecked(gs1.isEnabled());
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

		ToggleButton tvUpcEanConversionValue = (ToggleButton) findViewById(R.id.tbUpcEanConversionValue);
		tvUpcEanConversionValue.setChecked(gs1.isUpcEanConversionEnabled());
		tvUpcEanConversionValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableUPC_EAN_Conversion(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});
	}

	private void enable(Boolean value) throws ApiScannerException{
		gs1.setEnabled(value);
	}

	private void enableUPC_EAN_Conversion(Boolean value) throws ApiScannerException{
		gs1.setUpcEanConversionEnabled(value);
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
