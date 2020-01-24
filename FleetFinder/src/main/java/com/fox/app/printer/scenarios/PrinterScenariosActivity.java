package com.fox.app.printer.scenarios;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.avery.sampleapp.R;
import avd.api.core.exceptions.ApiDeviceException;
import avd.api.core.exceptions.ApiScannerException;
import com.fox.app.Activities.SampleAppActivity;
import com.fox.app.SampleApplication;
import com.fox.app.SampleApplication.DeviceData;

public class PrinterScenariosActivity extends SampleAppActivity {
	private SampleApplication application = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_printer_scenarios);

		Button printerScenarious = (Button) findViewById(R.id.btnScanPrintTest);
		printerScenarious.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					if (application.allDevicesSelected()) {
						for (DeviceData deviceData : application.connectedDevicesData.values())
							deviceData.device.getScanner().startScan();
					} else
						application.getDevice().getScanner().startScan();
				} catch (ApiScannerException e) {
					showStandardErrorMessageBox("Scanner start fail", e, application.getDevice());
				}
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		try { ((SampleApplication) getApplication()).removeScanPrintScenario(); } catch (ApiScannerException e) { }
		super.onBackPressed();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();

		application = (SampleApplication) getApplication();
		application.requiredDevice = application.ANY_DEVICE;
		application.lastActivityForAnyDevice = PrinterScenariosActivity.class;

		application.initializeDeviceIndexButtons(this);

		try {
			//Adding scan listener, in which we would print the scanned data.
			application.addScanPrintScenario();
		} catch (ApiDeviceException e) {
			showMessageBox("Trigger mode setting failed",
									   String.format("Setting trigger mode to \"Scan\" failed. Error code is %d. " +
										   	         "You will not be able to initate scanning by pressing trigger. " +
													 "You can still try to initiate scanning by pressing the button.", e.getErrorCode()),
													 "Ok", null, null, null, application.getDevice());
		} catch (ApiScannerException e) {
			showMessageBox("Scanner setup failed",
	                   String.format("Scanner setup has failed. Error code is %d. Scanning will not be possible.", e.getErrorCode()),
	                   "Ok", null, null, null, application.getDevice());
		}
	}
}