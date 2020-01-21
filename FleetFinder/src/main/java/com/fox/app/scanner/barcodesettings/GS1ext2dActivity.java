package com.fox.app.scanner.barcodesettings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.avery.sampleapp.R;

import avd.api.barcodes.twodimensional.Gs1Ext2D;
import avd.api.core.exceptions.ApiConfigurationException;
import avd.api.core.exceptions.ApiScannerException;
import avd.api.scanners.ScannerSe4500;

public class GS1ext2dActivity extends BarcodePropertiesSettingActivity {

	private Gs1Ext2D gs1Ext2D = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gs1ext2d);
	}

	@Override
	public void refreshControls() throws ApiScannerException
	{
		gs1Ext2D = ((ScannerSe4500) device.getScanner()).getGs1Ext2D();

		ToggleButton tbCompositeCcAbValue = (ToggleButton) findViewById(R.id.tbCompositeCcAbValue);
		tbCompositeCcAbValue.setChecked(gs1Ext2D.isCompositeCcAbEnabled());
		tbCompositeCcAbValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableCompositeCcab(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tbCompositeCCCValue = (ToggleButton) findViewById(R.id.tbCompositeCCCValue);
		tbCompositeCCCValue.setChecked(gs1Ext2D.isCompositeCccEnabled());
		tbCompositeCCCValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableCompositeCCC(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

		ToggleButton tbExpandedValue = (ToggleButton) findViewById(R.id.tbExpandedValue);
		tbExpandedValue.setChecked(gs1Ext2D.isExpandedEnabled());
		tbExpandedValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableExpanded(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});
		ToggleButton tbLimitedValue = (ToggleButton) findViewById(R.id.tbLimitedValue);
		tbLimitedValue.setChecked(gs1Ext2D.isLimitedEnabled());
		tbLimitedValue
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							enableLimited(isChecked);
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Setting parameter failed", e, device);
						}
					}
				});

	}

	private void enableCompositeCcab(Boolean value) throws ApiScannerException {
		gs1Ext2D.setCompositeCcAbEnabled(value);
	}

	private void enableCompositeCCC(Boolean value) throws ApiScannerException {
		gs1Ext2D.setCompositeCccEnabled(value);
	}

	private void enableExpanded(Boolean value) throws ApiScannerException {
		gs1Ext2D.setExpandedEnabled(value);
	}

	private void enableLimited(Boolean value) throws ApiScannerException {
		gs1Ext2D.setLimitedEnabled(value);
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
