package com.fox.app.Activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avery.sampleapp.R;
import com.fox.app.SampleApplication;
import com.fox.app.Utils.DBHelper;
import com.fox.app.connection.ConnectionActivity;
import com.fox.app.printer.PrinterActivity;
import com.fox.app.printer.scenarios.PrinterScenariosActivity;
import com.fox.app.scanner.ScannerActivity;
import com.fox.app.system.SystemActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import avd.api.core.imports.ResourceMediaType;
import avd.api.resources.ApiResourceException;
import avd.api.resources.ResourceManager;

public class MainActivity extends SampleAppActivity {
    private static final int REQUEST_CONNECTION_ACTIVITY = 1;

    private Button btnConnection = null;
    private Button btnPrinter = null;
    private Button btnPrinterScenarios = null;
    private Button btnScanner = null;
    private Button btnSystem = null;

    private SampleApplication application = null;

    private File resourceDirs = null;
    DBHelper dbHelper;
    TextView tvDevice;
    private int FLAG=0;

    @Override
    protected void onResume() {
        super.onResume();
        application.requiredDevice = application.NO_DEVICES;
        application.lastActivityForAnyDevice = MainActivity.class;

        application.initializeDeviceIndexButtons(this);
        if (!application.areResourcesRegistered) {
            registerResources();
            application.areResourcesRegistered = true;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // This is done to disable the 'Settings' button of Android devices.
        return false;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnected = !application.connectedDevicesData.isEmpty();
            if (isConnected) {

                btnScanner.setVisibility(View.VISIBLE);
                if (FLAG == 0) {
                    startActivity(new Intent(MainActivity.this, ScannerActivity.class));
                    FLAG = 1;
                }
            }
            btnPrinter.setEnabled(isConnected);
            btnPrinterScenarios.setEnabled(isConnected);
            btnScanner.setEnabled(isConnected);
            btnSystem.setEnabled(isConnected);

            application.isButtonPressAllowed = true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        application = ((SampleApplication) getApplication());
        dbHelper = new DBHelper(this);
        IntentFilter filter = new IntentFilter(SampleApplication.INTENT_ACTION_UPDATE_SCREEN_CONTROLS);
        registerReceiver(mReceiver, filter);

        tvDevice = findViewById(R.id.tvDevice);

        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Toast.makeText(this, getResources().getText(R.string.bluetooth_is_not_available).toString(), Toast.LENGTH_LONG).show();
            finish();
        }

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();
        /*if (tvDevice.getText().toString().contains("Connected to")) {
            startActivity(new Intent(this, ScannerActivity.class));
        }*/
if(getIntent().hasExtra("Scanner")){
    Intent intent = new Intent(MainActivity.this, ConnectionActivity.class);
    intent.putExtra("Scanner", "scanner");
    startActivity(intent);

}
        btnConnection = (Button) findViewById(R.id.btnConnection);
        btnConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!application.isButtonPressAllowed)
                    return;

                application.isButtonPressAllowed = false;
                Intent serverIntent = new Intent(MainActivity.this, ConnectionActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECTION_ACTIVITY);
            }
        });
		
      /*  dbHelper.insertProductData("Product 1","5435168163", "5$");
        dbHelper.insertProductData("Product 2","123456789", "15$");
        dbHelper.insertProductData("Product 3","876867573", "25$");*/

        btnSystem = (Button) findViewById(R.id.btnSystem);
        btnSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (application.allDevicesSelected()) {
                    showMessageBox("Impossible action",
                            "Setting system parameters is not allowed for all connected devices at once. Please select one of the devices from the list before proceeding.",
                            "Ok", null, null, null, null);
                    return;
                }

                if (!application.isButtonPressAllowed)
                    return;

                application.isButtonPressAllowed = false;
                application.addSystemActivity(); // Implements subscribing to trigger press callback - appropriate here.
                startActivity(new Intent(MainActivity.this, SystemActivity.class));
            }
        });


        btnPrinter = (Button) findViewById(R.id.btnPrinter);
        btnPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!application.isButtonPressAllowed)
                    return;

                application.isButtonPressAllowed = false;
                startActivity(new Intent(MainActivity.this, PrinterActivity.class));
            }
        });

        btnScanner = (Button) findViewById(R.id.btnScanner);
        btnScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!application.isButtonPressAllowed)
                    return;

                application.isButtonPressAllowed = false;
                startActivity(new Intent(MainActivity.this, ScannerActivity.class));
            }
        });

        btnPrinterScenarios = (Button) findViewById(R.id.btnPrinterScenarios);
        btnPrinterScenarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!application.isButtonPressAllowed)
                    return;

                application.isButtonPressAllowed = false;
                startActivity(new Intent(MainActivity.this, PrinterScenariosActivity.class));
            }
        });

        Button btnResourceManagement = (Button) findViewById(R.id.btnResourceManagement);
        btnResourceManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!application.isButtonPressAllowed)
                    return;

                application.isButtonPressAllowed = false;
                startActivity(new Intent(MainActivity.this, ResourcesActivity.class));
            }
        });

        sendBroadcast(new Intent(SampleApplication.INTENT_ACTION_UPDATE_SCREEN_CONTROLS));
//        Log.e(TAG, "onCreate: "+btnScanner.getVisibility() );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

    private void registerResources() {
        try {
            resourceDirs = new File(Environment.getExternalStorageDirectory().toString(), "Download");

            if (resourceDirs.canWrite() == false)
                resourceDirs = new File(getFilesDir().getAbsolutePath().toString());

            application.resourcePath = resourceDirs.getAbsolutePath() + "/";
            ResourceManager.initializeResourcePath(application.resourcePath);

            String[] listOfResources = ResourceManager.getResourceList(ResourceMediaType.Font);

            if (!Arrays.asList(listOfResources).contains("RobotoRegular"))
                application.registerResource(ResourceMediaType.Font, "RobotoRegular", "RobotoRegular.ttf");
            if (!Arrays.asList(listOfResources).contains("RobotoBold"))
                application.registerResource(ResourceMediaType.Font, "RobotoBold", "RobotoBold.ttf");

            listOfResources = ResourceManager.getResourceList(ResourceMediaType.Graphic);
            if (!Arrays.asList(listOfResources).contains("Avery Logo"))
                application.registerResource(ResourceMediaType.Graphic, "Avery Logo", "AveryDennisonLogo.png");

            listOfResources = ResourceManager.getResourceList(ResourceMediaType.Lnt);
            if (!Arrays.asList(listOfResources).contains("Sample Print LNT"))
                application.registerResource(ResourceMediaType.Lnt, "Sample Print LNT", "SamplePrint.LNT");
            if (!Arrays.asList(listOfResources).contains("Print LNT"))
                application.registerResource(ResourceMediaType.Lnt, "Print LNT", "Print.LNT");
            if (!Arrays.asList(listOfResources).contains("Label Shipment LNT"))
                Log.e("Label added!", "registerResources: Label added to path" );
                application.registerResource(ResourceMediaType.Lnt, "Label Shipment LNT", "LabelShipment.lnt");
            if (!Arrays.asList(listOfResources).contains("Sample Label LNT"))
                application.registerResource(ResourceMediaType.Lnt, "Sample Label LNT", "SampleLabel.LNT");
            if (!Arrays.asList(listOfResources).contains("Quantity Test LNT"))
                application.registerResource(ResourceMediaType.Lnt, "Quantity Test LNT", "QuantityTest.LNT");
            if (!Arrays.asList(listOfResources).contains("Barcode Print LNT"))
                application.registerResource(ResourceMediaType.Lnt, "Barcode Print LNT", "BarcodePrint.LNT");
            if (!Arrays.asList(listOfResources).contains("Barcode Text Print LNT"))
                application.registerResource(ResourceMediaType.Lnt, "Barcode Text Print LNT", "BarcodePrintTextOnly.LNT");
            if (!Arrays.asList(listOfResources).contains("Bold Italic Underline LNT"))
                application.registerResource(ResourceMediaType.Lnt, "Bold Italic Underline LNT", "BoldItalicUnderline.LNT");
            if (!Arrays.asList(listOfResources).contains("Bold Italic Underline MIX LNT"))
                application.registerResource(ResourceMediaType.Lnt, "Bold Italic Underline MIX LNT", "BoldItalicUnderlineMixed.LNT");
            if (!Arrays.asList(listOfResources).contains("BoldItalicUnderline NewLine LNT"))
                application.registerResource(ResourceMediaType.Lnt, "BoldItalicUnderline NewLine LNT", "BoldItalicUnderlineNewLine.LNT");
        } catch (ApiResourceException e) {
            showMessageBox("Resource loading failed", String.format("Unable to load resources. Error code is %d. Printing will be impossible.", e.getErrorCode()), "OK", null, null, null, null);
        }
    }
}
