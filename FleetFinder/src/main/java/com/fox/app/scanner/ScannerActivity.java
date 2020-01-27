package com.fox.app.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.avery.sampleapp.R;
import com.fox.app.Activities.SampleAppActivity;
import com.fox.app.SampleApplication;
import com.fox.app.SampleApplication.DeviceData;
import com.fox.app.Utils.SharedPreferenceMethod;
import com.fox.app.connection.ConnectionActivity;
import com.fox.app.printer.PrinterActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import avd.api.core.IDevice;
import avd.api.core.IScanner;
import avd.api.core.exceptions.ApiPrinterException;
import avd.api.core.exceptions.ApiScannerException;

public class ScannerActivity extends SampleAppActivity {

    private SampleApplication application = null;
    ImageView connectionBtn, printerBtn;
    private ToggleButton tbScanEngine = null;
    TextView barcodeTxt, batchIdTxt, batchDateTxt, labelSequenceTxt, ScanDateTxt;
    int FLAG = 0;
    SharedPreferenceMethod sharedPreferenceMethod;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_scanner);


        sharedPreferenceMethod = new SharedPreferenceMethod(this);
        barcodeTxt = findViewById(R.id.barcodeTxt);
        batchIdTxt = findViewById(R.id.batchIdTxt);
        batchDateTxt = findViewById(R.id.batchDateTxt);
        labelSequenceTxt = findViewById(R.id.labelSequenceTxt);
        ScanDateTxt = findViewById(R.id.scannedDateTxt);
        connectionBtn = findViewById(R.id.connectionBtn);
        printerBtn = findViewById(R.id.printerbtn);

        if (getIntent().hasExtra("Scanner")) {
            Toast.makeText(ScannerActivity.this, "Pathfinder Connected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        application = (SampleApplication) getApplication();
        application.requiredDevice = application.ANY_DEVICE;
        application.lastActivityForAnyDevice = ScannerActivity.class;
        this.setupDefaults();
    }

    @Override
    public void onPause() {
        unregisterReceiver(mDeviceSelectedReceiver);
        if (isFinishing()) {
            try {
                application.removeScannerActivity();
            } catch (ApiScannerException e) {
                e.printStackTrace();
            } // No need to do anything here - we are closing the activity anyway
        }
        super.onPause();
    }

    private final BroadcastReceiver mDeviceSelectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (application.allDevicesSelected())
                tbScanEngine.setEnabled(false);
            else {
                tbScanEngine.setEnabled(true);
                tbScanEngine.setChecked(application.isScannerEnabled());
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
       /* if (FLAG == 0) {
//            Toast.makeText(application, "Press back again to close the app!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ScannerActivity.this,ConnectionActivity.class));

            FLAG = 1;
        } else {

            String[] deviceParams = sharedPreferenceMethod.getDeviceName().split("\n");

//            finishAffinity();
        }*/
    }

    private void setupDefaults() {
        application.initializeDeviceIndexButtons(this);
        IntentFilter filter = new IntentFilter(SampleApplication.INTENT_ACTION_UPDATE_DEVICE_SELECTION);
        registerReceiver(mDeviceSelectedReceiver, filter);
        sendBroadcast(new Intent(SampleApplication.INTENT_ACTION_UPDATE_DEVICE_SELECTION));
        tbScanEngine = (ToggleButton) findViewById(R.id.tbScanEngine);
        tbScanEngine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                application.setScanEngineEnabled(application.getDeviceData(), isChecked);
            }
        });
        Button btnStartScan = (Button) findViewById(R.id.btnStartScan);
        btnStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startScan();
                } catch (ApiScannerException e) {
                    showStandardErrorMessageBox("Scanner start fail", e, application.getDevice());
                }
            }
        });
        connectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScannerActivity.this, ConnectionActivity.class));
            }
        });
        printerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScannerActivity.this, PrinterActivity.class));

            }
        });
        Button btnStopScan = (Button) findViewById(R.id.btnStopScan);
        btnStopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    stopScan();
                } catch (ApiScannerException e) {
                    showStandardErrorMessageBox("Scanner stop fail", e, application.getDevice());
                }
            }
        });

        try {
            application.addScannerActivity();
        } catch (ApiScannerException e) {
            showMessageBox("Adding listener failed",
                    String.format("Scanner listener has not been added. Error code is %d. Scan results processing is inactive.", e.getErrorCode()),
                    "Ok", null, null, null, application.getDevice());
        }


        Button btnScannerSettings = (Button) findViewById(R.id.btnScannerSettings);
        btnScannerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    application.removeScannerActivity();
                } catch (ApiScannerException e) {
                    e.printStackTrace();
                }
                showScannerSettings();
            }
        });
    }

    private void performScannerAction(String actionName) {
        Method action = null;
        try {
            action = IScanner.class.getMethod(actionName, (Class<?>[]) null);
        } catch (NoSuchMethodException e) {
            return;
        }
        IDevice device = null;

        String line;
        if (actionName == "startScan")
            line = "Starting scanning for ";
        else
            line = "Stopping scanning for ";

        try {
            if (application.allDevicesSelected()) {
                line += "all devices\n";
                application.addScannerActivityLine(line);
                for (DeviceData deviceData : application.connectedDevicesData.values()) {
                    device = deviceData.device;
                    IScanner scanner = device.getScanner();
                    action.invoke(scanner, (Object[]) null);
                }
            } else {
                device = application.getDevice();

                line += "device " + device.getSerial() + "\n";
                application.addScannerActivityLine(line);

                IScanner scanner = device.getScanner();
                action.invoke(scanner, (Object[]) null);
            }
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
            ApiPrinterException ex = (ApiPrinterException) e.getTargetException();
            showStandardErrorMessageBox("\"" + action.getName() + "\" command failed", ex, device);
        }
    }

    private void startScan() throws ApiScannerException {
        performScannerAction("startScan");
    }

    private void stopScan() throws ApiScannerException {
        performScannerAction("stopScan");
    }

    private void showScannerSettings() {
        if (application.allDevicesSelected()) {
            showMessageBox("Impossible action",
                    "Setting scanner parameters is not allowed for all connected devices at once. Please select one of the devices from the list before proceeding.",
                    "Ok", null, null, null, null);
            return;
        }
        Intent propertiesIntent = new Intent(this, ScannerPropertiesActivity.class);
        startActivity(propertiesIntent);
    }
}