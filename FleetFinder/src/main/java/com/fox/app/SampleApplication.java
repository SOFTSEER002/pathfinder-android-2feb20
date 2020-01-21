package com.fox.app;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.avery.sampleapp.R;
import com.fox.app.printer.scenarios.PrinterScenariosActivity;
import com.fox.app.scanner.ScannerActivity;
import com.fox.app.system.SystemActivity;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import avd.api.core.BarcodeType;
import avd.api.core.ConnectionState;
import avd.api.core.IDevice;
import avd.api.core.IListenerError;
import avd.api.core.IListenerScan;
import avd.api.core.IListenerTriggerPress;
import avd.api.core.IPrinter;
import avd.api.core.exceptions.ApiCommunicationException;
import avd.api.core.exceptions.ApiDeviceException;
import avd.api.core.exceptions.ApiDeviceManagerException;
import avd.api.core.exceptions.ApiException;
import avd.api.core.exceptions.ApiPrinterException;
import avd.api.core.exceptions.ApiScannerException;
import avd.api.core.imports.ButtonState;
import avd.api.core.imports.ButtonType;
import avd.api.core.imports.ResourceMediaType;
import avd.api.core.imports.TriggerMode;
import avd.api.devices.management.DeviceConnectionInfo;
import avd.api.devices.management.DeviceManager;
import avd.api.resources.ApiResourceException;
import avd.api.resources.ResourceManager;
import avd.sdk.CompanionAPIErrors;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class SampleApplication extends Application {
    private static final String LOG_FOLDER_ENV_VARIABLE = "av.sampleapp.logpath";
    private static final String LOG_FOLDER = "Download/ApiLog";
    private static final String LOG_CONFIG_FILE = "logconfig.xml";

    private IListenerError connectionErrorCallback;
    private IListenerError printerErrorCallback;
    private IListenerError systemErrorCallback;

    private TextView tvScannerActivityText = null;
    private IListenerScan scannerActivityCallback = null;
    private String scannerActivityText = "";

    private Button btnDeviceLeft = null;
    private Button btnDeviceRight = null;
    private TextView tvDeviceName = null;
    private BroadcastReceiver mDeviceSelectedReceiver = null;

    private IListenerTriggerPress triggerPressCallback;

    private IListenerScan scanPrintScenarioCallback;
    DBHelper dbHelper;
    public OnClickListener abortPrinterError;
    private OnClickListener clearPrinterError;
    private OnClickListener abortPrinterErrorAndResyncPrinter;
    private OnClickListener clearPrinterErrorAndResyncPrinter;
    private OnClickListener ignoreConnectionLoss;

    private DeviceManager deviceManager = new DeviceManager();
    public boolean areResourcesRegistered = false;

    public SampleAppActivity currentActivity = null;
    public boolean isButtonPressAllowed = true;
    private boolean isScanPrintScenarioActive = false;
    private boolean isScannerActive = false;
    private int numberOfUnresolvedConnectionLosses = 0;
    public String requiredDevice = NO_DEVICES;
    public Class<?> lastActivityForAnyDevice = null;

    public static final String INTENT_ACTION_UPDATE_SCREEN_CONTROLS = "Update screen controls";
    public static final String INTENT_ACTION_UPDATE_DEVICE_SELECTION = "Update device selection";
    public static final int INTENT_FILE_CHOOSER_ID = 1;
    public static String SDK_VERSION;
    public static String ANY_DEVICE;
    public static String ALL_DEVICES;
    public static String NO_DEVICES;

    public String resourcePath = null;

    private int counter = 0;

    // We have to pass data into the listener that would attempt reconnection, so we implement it as a custom class that
    // contains the data that we need to pass.
    private class AttemptReconnectionListener implements OnClickListener {
        private DeviceConnectionInfo reconnectDeviceConnectionInfo = null;

        public AttemptReconnectionListener(DeviceConnectionInfo reconnectDeviceConnectionInfo) {
            this.reconnectDeviceConnectionInfo = reconnectDeviceConnectionInfo;
        }

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            try {
                IDevice newDevice = createDevice(this.reconnectDeviceConnectionInfo);
                // If we are on a special screen, we need to re-add the callbacks.
                // We use direct addition instead of appropriate activity adding methods like addSystemActivity()
                // in order to eliminate unnecessary actions (like refreshing other controls) even if method contains no other action.
                // This is done to keep code uniform, so we do only listener additions instead of addSystemActivity in one case and
                // addListenerScan in other case, we use addListener for every case.
                // We need to do this only if device was subscribed to callbacks previously.
                // It was subscribed either if it was selected before disconnect or if all devices were selected.
                if ((getDevice() == null) || (getDevice() == newDevice)) {
                    if (currentActivity instanceof SystemActivity)
                        newDevice.addListenerTriggerPress(triggerPressCallback);
                    else if (currentActivity instanceof ScannerActivity)
                        newDevice.getScanner().addListenerScan(scannerActivityCallback);
                    else if (currentActivity instanceof PrinterScenariosActivity)
                        newDevice.getScanner().addListenerScan(scanPrintScenarioCallback);
                }

                --numberOfUnresolvedConnectionLosses;
                resolveConnectionLosses();
            } catch (ApiException e) {
                currentActivity.showMessageBox("Connection error", String.format("Reconnection attempt failed with error %d.", e.getErrorCode()),
                        "Attempt reconnect", this, "Ignore", ignoreConnectionLoss, null);
            }
        }
    }

    // We have to pass data into the listener that would ignore reconnection, so we implement it as a custom class that
    // contains the data that we need to pass.
    private class IgnoreReconnectionListener implements OnClickListener {
        private Class<?> availableActivity;

        public IgnoreReconnectionListener(Class<?> availableActivity) {
            this.availableActivity = availableActivity;
        }

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            // Intent removes from the back stack all activities down to but not including availableActivity, effectively reverting application to this activity.
            Intent i = new Intent(currentActivity, availableActivity);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            currentActivity.startActivity(i);
        }
    }


    public class DeviceData {
        public IDevice device;
        public boolean isScannerEnabled;
        public boolean isSystemParametersLoaded;
        public boolean isPrinterParametersLoaded;
        public boolean isScannerParametersLoaded;
        public boolean isBarcodeParametersLoaded;
    }

    private List<String> connectedDevicesNames = new ArrayList<String>();
    public Map<String, DeviceData> connectedDevicesData = new HashMap<String, DeviceData>();
    public String currentDeviceName = null;

    @Override
    public void onCreate() {
        super.onCreate();
        configureLogging();

        try {
            SDK_VERSION = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ALL_DEVICES = getResources().getText(R.string.all_devices).toString();
        ANY_DEVICE = getResources().getText(R.string.any_device).toString();
        NO_DEVICES = getResources().getText(R.string.no_devices).toString();
        currentDeviceName = NO_DEVICES;
        dbHelper = new DBHelper(this);
        abortPrinterError = new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                IDevice device = currentActivity.findDeviceAssociatedWithAlert(arg0);
                if (device == null)
                    return;

                try {
                    device.getPrinter().abortError();
                } catch (ApiPrinterException e) {
                }

                currentActivity.dismissAllAlertsForDevice(device, false); // All error messages pertaining to this device are irrelevant now.
            }

        };

        clearPrinterError = new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                IDevice device = currentActivity.findDeviceAssociatedWithAlert(arg0);
                if (device == null)
                    return;

                try {
                    device.getPrinter().clearError();
                } catch (ApiPrinterException e) {
                }

                currentActivity.dismissAllAlertsForDevice(device, false); // All error messages pertaining to this device are irrelevant now.
            }

        };

        abortPrinterErrorAndResyncPrinter = new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                IDevice device = currentActivity.findDeviceAssociatedWithAlert(arg0);
                if (device == null)
                    return;

                try {
                    device.getPrinter().abortError();
                    device.getPrinter().resync();
                } catch (ApiPrinterException e) {
                }

                currentActivity.dismissAllAlertsForDevice(device, false); // All error messages pertaining to this device are irrelevant now.
            }
        };

        clearPrinterErrorAndResyncPrinter = new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                IDevice device = currentActivity.findDeviceAssociatedWithAlert(arg0);
                if (device == null)
                    return;

                try {
                    device.getPrinter().clearError();
                    //device.getPrinter().resync();
                } catch (ApiPrinterException e) {
                }

                currentActivity.dismissAllAlertsForDevice(device, false); // All error messages pertaining to this device are irrelevant now.
            }
        };

        ignoreConnectionLoss = new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                --numberOfUnresolvedConnectionLosses;
                resolveConnectionLosses();
            }
        };

        connectionErrorCallback = new IListenerError() {

            @Override
            public void onErrorReceived(String errorText, int errorCode, IDevice device) {
                if (device == null)
                    return;

                ++numberOfUnresolvedConnectionLosses;

                eraseDevice(device.getSerial());

                AttemptReconnectionListener attemptReconnection = new AttemptReconnectionListener(new DeviceConnectionInfo(device.getSerial(), device.getConnection().getAddress(), device.getConnection().getType()));

                currentActivity.showMessageBox("Connection error", String.format("Connection error %d. %s", errorCode, errorText),
                        "Attempt reconnection", attemptReconnection,
                        "Ignore", ignoreConnectionLoss, device);

            }
        };

        systemErrorCallback = new IListenerError() {

            @Override
            public void onErrorReceived(String errorText, int errorCode, IDevice device) {
                if (device != null)
                    currentActivity.showMessageBox("Device error", String.format("Device error %d.", errorCode),
                            "Abort", abortPrinterError, "Ignore", null, device);
            }
        };

        printerErrorCallback = new IListenerError() {

            @Override
            public void onErrorReceived(String errorText, int errorCode, IDevice device) {
                if (device == null)
                    return;

                if ((errorCode == CompanionAPIErrors.PE_MISSING_BLACK_MARK) ||
                        (errorCode == CompanionAPIErrors.PE_UNEXPECTED_BLACK_MARK) ||
                        (errorCode == CompanionAPIErrors.PE_SENSE_MARK_IS_TOO_LONG)) {

                    currentActivity.createAndShowMessageBox("Printer Error", String.format("%s\nError code is %d.", errorText, errorCode),
                            "Abort Print Job & Resync", abortPrinterErrorAndResyncPrinter,
                            "Ignore", null,
                            "Clear Error", clearPrinterErrorAndResyncPrinter, device);
                } else {
                    currentActivity.createAndShowMessageBox("Printer Error", String.format("%s\nError code is %d.", errorText, errorCode),
                            "Abort Print Job", abortPrinterError,
                            "Ignore", null,
                            "Clear Error", clearPrinterError, device);
                }
            }
        };

        scannerActivityCallback = new IListenerScan() {

            @Override
            public void onScanReceived(String scanData, BarcodeType barcodeType, IDevice device) {
                if ((tvScannerActivityText == null) || (device == null))
                    return;

                String deviceName = device.getSerial();
                if (allDevicesSelected() || currentDeviceName.equals(deviceName)) {
                    if (dbHelper.columnExists(scanData) != null) {
                        Log.e("BARCODE SCANNED", "onScanReceived: already there !");
                        printSample(dbHelper.columnExists(scanData));
                    } else {
                        Log.e("BARCODE SCANNED", "new");

                    }
                    ;
                    scannerActivityText = scannerActivityText + "Scanned: " + scanData + ". Barcode type: " + barcodeType + ". Device: " + deviceName + "\n";

                    currentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvScannerActivityText.setText(scannerActivityText);
                            Layout layout = tvScannerActivityText.getLayout();
                            int scrollDelta = layout.getLineBottom(tvScannerActivityText.getLineCount() - 1) - tvScannerActivityText.getScrollY() - tvScannerActivityText.getHeight();
                            if (scrollDelta > 0)
                                tvScannerActivityText.scrollBy(0, scrollDelta);
                        }
                    });
                }
            }
        };

        scanPrintScenarioCallback = new IListenerScan() {
            @Override
            public void onScanReceived(final String scanData, BarcodeType barcodeType, IDevice device) {
                final String stringBarcodeType = apiBarcodeTypeToLntBarcodeType(barcodeType);
                final int indexBarcode = lntBarcodeTypeToLntFieldIndex(stringBarcodeType);
                final byte[][] printData = {scanData.getBytes()};
                final byte[][] barcodeData = new byte[lntBarcodeTypeFieldIndices.length][];
                for (int i = 0; i < barcodeData.length; ++i) {
                    if (i == indexBarcode)
                        barcodeData[i] = scanData.getBytes();
                    else
                        barcodeData[i] = "".getBytes();
                }

                if (device == null)
                    return;

                final String deviceName = device.getSerial();
                if (allDevicesSelected() || currentDeviceName.equals(deviceName)) {
                    final IDevice deviceLocal = device;
                    currentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                if (indexBarcode == -1)
                                    deviceLocal.getPrinter().print("Barcode Text Print LNT", 1, printData);
                                else
                                    deviceLocal.getPrinter().print("Barcode Print LNT", 1, barcodeData);
                            } catch (ApiPrinterException e) {
                                currentActivity.showErrorMessageBox("Scanned label print failed.", e, "Abort", abortPrinterError, "Ignore", null, deviceLocal);
                            }
                        }
                    });
                }
            }
        };

        triggerPressCallback = new IListenerTriggerPress() {

            @Override
            public void onTriggerPressReceived(ButtonType buttonType, ButtonState buttonState, IDevice device) {
                if (device == null)
                    return;

                String triggerPressData = buttonType == ButtonType.Trigger ? "Trigger button" : "Feed button";
                triggerPressData = triggerPressData + " " + ((buttonState == ButtonState.Pressed) ? "is pressed" : "is released");

                currentActivity.showMessageBox("Button press notice", triggerPressData, "Ok", null, null, null, device);
            }
        };

        mDeviceSelectedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateDeviceIndexPresentation();
            }
        };
        IntentFilter filter = new IntentFilter(SampleApplication.INTENT_ACTION_UPDATE_DEVICE_SELECTION);
        registerReceiver(mDeviceSelectedReceiver, filter);
    }

    public IDevice createDevice(DeviceConnectionInfo deviceConnectionInfo) throws ApiDeviceManagerException, ApiScannerException, ApiPrinterException, ApiDeviceException, ApiCommunicationException {
        IDevice device = deviceManager.createDevice(deviceConnectionInfo);

        if (device != null) {
            if (connectedDevicesData.isEmpty())
                startService(new Intent(this, DeviceService.class));

            DeviceData deviceData = new DeviceData();
            device.addListenerDeviceError(systemErrorCallback);
            device.getConnection().addListenerConnectionError(connectionErrorCallback);
            device.getPrinter().addListenerPrinterError(printerErrorCallback);
            device.getScanner().enableScan(true);

            deviceData.device = device;
            deviceData.isScannerEnabled = true;
            deviceData.isSystemParametersLoaded = false;
            deviceData.isPrinterParametersLoaded = false;
            deviceData.isScannerParametersLoaded = false;
            deviceData.isBarcodeParametersLoaded = false;

            connectedDevicesData.put(deviceConnectionInfo.getSerial(), deviceData);
            connectedDevicesNames.add(deviceConnectionInfo.getSerial());

            int status;
            status = device.getStatus();
            if (status != 0)
                currentActivity.showMessageBox("Bad Device Status", String.format("Device status indicates an error. Error code is %d.", status),
                        "Abort", abortPrinterError, "Ignore", null, null);
            else {
                status = device.getPrinter().getStatus();
                if (status != 0)
                    currentActivity.showMessageBox("Bad Printer Status", String.format("Printer status indicates an error. Error code is %d.", status),
                            "Abort", abortPrinterError, "Ignore", null, null);
            }
        }

        sendBroadcast(new Intent(INTENT_ACTION_UPDATE_DEVICE_SELECTION));
        sendBroadcast(new Intent(INTENT_ACTION_UPDATE_SCREEN_CONTROLS));

        return device;
    }

    public void eraseDevice(String deviceSerial) {
        DeviceData deviceData = connectedDevicesData.get(deviceSerial);
        if (deviceData == null)
            return;

        if (deviceData.device.getConnection().getState() == ConnectionState.Open) {
            try {
                deviceData.device.removeListenerDeviceError(systemErrorCallback);
                deviceData.device.getConnection().removeListenerConnectionError(connectionErrorCallback);
                deviceData.device.getPrinter().removeListenerPrinterError(printerErrorCallback);
            } catch (ApiDeviceException e) {
                currentActivity.showErrorMessageBox("Device error", e, "Ok", null, null, null, null);
            } catch (ApiCommunicationException e) {
                currentActivity.showErrorMessageBox("Communication error", e, "Ok", null, null, null, null);
            } catch (ApiPrinterException e) {
                currentActivity.showErrorMessageBox("Printer error", e, "Ok", null, null, null, null);
            }
        }
        deviceManager.removeDevice(deviceData.device.getConnection().getAddress());

        currentActivity.dismissAllAlertsForDevice(deviceData.device, false);

        connectedDevicesData.remove(deviceSerial);
        connectedDevicesNames.remove(deviceSerial);

        resolveConnectionLosses(); // Automatic attempt to resolve connection losses if device erasure has been caused by other reasons than connection loss.
    }

    // Erase all device instances and data on them from the application.
    public void eraseDevices() {
        // All alerts associated with devices should be permanently closed.
        currentActivity.dismissAllAlerts(false);
        // Caches of device instances and data on them are cleared. Also performing zeroing of the numberOfUnresolvedConnectionLosses.
        // This method is now invoked solely when bluetooth is turned off, but zeroing should be done in any case
        // as no devices means no unresolved connections and no messages and user will always be redirected to proper screen).
        numberOfUnresolvedConnectionLosses = 0;
        connectedDevicesNames.clear();
        connectedDevicesData.clear();
        currentDeviceName = NO_DEVICES;
    }

    private void printSample(String s) {

        String text = "Product Name :- BLCK\n\tprice 5$";
        byte[][] stringText = {s.getBytes()};
        if (this.allDevicesSelected()) {
            for (DeviceData deviceData : this.connectedDevicesData.values()) {
                IDevice device = deviceData.device;
                IPrinter printer = device.getPrinter();
                try {
                    printer.print("Data Print", 1, stringText);
                } catch (ApiPrinterException e) {
                    e.printStackTrace();
                }
            }
        } else {
            IDevice device = this.getDevice();
            IPrinter printer = device.getPrinter();
            try {
                printer.print("Barcode Text Print LNT", 1, stringText);
            } catch (ApiPrinterException e) {
                e.printStackTrace();
            }
        }

    }

    // This method is invoked after all the connection losses have been resolved. It determines whether the user can
    // stay on current screen, and, if it is not possible, to which screen they should be directed.
    private void navigateToProperScreen() {
        Class<?> availableActivity = MainActivity.class; // Default screen to which user should be directed.
        String message = null;  // Message with which user would be directed to another screen.

        if (requiredDevice == NO_DEVICES) // Current screen requires no devices to be present at it. We will stay on it.
            return;

        if (requiredDevice == ANY_DEVICE) // Current screen requires at least one arbitrary device connected to the application.
        {
            if (currentDeviceName != NO_DEVICES) // At least one device is connected. We will stay on this screen.
                return;

            // No devices connected. We will redirect the user to the main screen.
            message = "You cannot stay on this screen as you have lost connection to all devices.You will be redirected to the main screen.";
        } else // Current screen requires a specific device connected to the application.
        {
            if (currentDeviceName == requiredDevice) // Device is still connected. We will stay on this screen.
                return;

            // We lost connection to the required device. We will redirect the user to the nearest available screen (main screen by default).
            message = "You cannot stay on this screen as you have lost connection to the device, which properties you are viewing. You will be redirected to the nearest available activity.";
            if (currentDeviceName != NO_DEVICES) // At least one device is connected. We will redirect the user to the last activity that does not require a specific device.
                availableActivity = lastActivityForAnyDevice;
        }

        // Show message box on proper activity, hence it needs to be shown after the intent is sent.
        currentActivity.showMessageBox("Connection error", message, "Ok", new IgnoreReconnectionListener(availableActivity), null, null, null);
    }

    // This method is invoked when a connection loss is handled. It performs necessary actions to clean up the application state
    // after all connection losses have been handled.
    private void resolveConnectionLosses() {
        if (numberOfUnresolvedConnectionLosses > 0) // Not all connection losses have been resolved yet. Do nothing.
            return;

        if (connectedDevicesData.isEmpty())  // Stop service that displays 6140 icon whenever at least one device is connected.
            stopService(new Intent(this, DeviceService.class));

        // If there are no more connected devices, set "No devices" as current selected device.
        // If only one device is connected, choose it as currently selected device.
        // If several devices are connected, there was a device selected (not "All Devices" option was selected),
        // and that device is no more connected, select the first device from the list of connected devices.
        if (connectedDevicesNames.isEmpty())
            currentDeviceName = NO_DEVICES;
        else if ((connectedDevicesNames.size() == 1) ||
                (!allDevicesSelected() && !connectedDevicesNames.contains(currentDeviceName)))
            currentDeviceName = connectedDevicesNames.listIterator().next();

        navigateToProperScreen();

        // Update the screen
        sendBroadcast(new Intent(INTENT_ACTION_UPDATE_DEVICE_SELECTION));
        sendBroadcast(new Intent(INTENT_ACTION_UPDATE_SCREEN_CONTROLS));
    }

    public boolean allDevicesSelected() {
        return (currentDeviceName == ALL_DEVICES);
    }

    private void scrollDeviceIndexLeft() {
        if (allDevicesSelected())
            currentDeviceName = connectedDevicesNames.get(connectedDevicesNames.size() - 1);
        else {
            int indexOfDevice = connectedDevicesNames.indexOf(currentDeviceName) - 1;
            currentDeviceName = connectedDevicesNames.get(indexOfDevice);
        }

        sendBroadcast(new Intent(INTENT_ACTION_UPDATE_DEVICE_SELECTION));
    }

    private void scrollDeviceIndexRight() {
        int indexOfDevice = connectedDevicesNames.indexOf(currentDeviceName) + 1;
        if (indexOfDevice == connectedDevicesNames.size())
            currentDeviceName = ALL_DEVICES;
        else
            currentDeviceName = connectedDevicesNames.get(indexOfDevice);

        sendBroadcast(new Intent(INTENT_ACTION_UPDATE_DEVICE_SELECTION));
    }

    public void initializeDeviceIndexButtons(Activity currentActivity) {

        tvDeviceName = (TextView) currentActivity.findViewById(R.id.tvDeviceName);
        btnDeviceLeft = (Button) currentActivity.findViewById(R.id.btnScrollDeviceIndexLeft);
        btnDeviceLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isButtonPressAllowed)
                    return;

                scrollDeviceIndexLeft();
            }
        });

        btnDeviceRight = (Button) currentActivity.findViewById(R.id.btnScrollDeviceIndexRight);
        btnDeviceRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isButtonPressAllowed)
                    return;

                scrollDeviceIndexRight();
            }
        });

        updateDeviceIndexPresentation();
    }

    private void updateDeviceIndexPresentation() {
        if (connectedDevicesNames.size() < 2) {
            btnDeviceLeft.setVisibility(View.INVISIBLE);
            btnDeviceRight.setVisibility(View.INVISIBLE);
        } else {
            if (connectedDevicesNames.indexOf(currentDeviceName) == 0) {
                btnDeviceLeft.setVisibility(View.INVISIBLE);
                btnDeviceRight.setVisibility(View.VISIBLE);
            } else if (allDevicesSelected()) {
                btnDeviceLeft.setVisibility(View.VISIBLE);
                btnDeviceRight.setVisibility(View.INVISIBLE);
            } else {
                btnDeviceLeft.setVisibility(View.VISIBLE);
                btnDeviceRight.setVisibility(View.VISIBLE);
            }
        }

        tvDeviceName.setText(currentDeviceName);
    }

    public IDevice getDevice() {
        if ((currentDeviceName == NO_DEVICES) || (currentDeviceName == ALL_DEVICES) || !connectedDevicesData.containsKey(currentDeviceName))
            return null;

        return connectedDevicesData.get(currentDeviceName).device;
    }

    public DeviceData getDeviceData() {
        if ((currentDeviceName == NO_DEVICES) || (currentDeviceName == ALL_DEVICES))
            return null;

        return connectedDevicesData.get(currentDeviceName);
    }

    public boolean isScannerEnabled() {
        DeviceData data = getDeviceData();
        if (data == null)
            return false;

        return data.isScannerEnabled;
    }

    public void setScanEngineEnabled(DeviceData deviceData, boolean newIsScannerEnabled) {
        if (deviceData == null)
            return;

        try {
            deviceData.device.getScanner().enableScan(newIsScannerEnabled);
            deviceData.isScannerEnabled = newIsScannerEnabled;
        } catch (ApiScannerException e) {
            currentActivity.showErrorMessageBox("Scanner enabling/disabling setting failed.", e, "Abort", abortPrinterError, "Ignore", null, deviceData.device);
        }
    }

    private void configureLogging() {

        File logDir = new File(Environment.getExternalStorageDirectory(), LOG_FOLDER);

        if (!logDir.exists() || !logDir.isDirectory()) {
            logDir.mkdirs();
        }

        System.setProperty(LOG_FOLDER_ENV_VARIABLE, logDir.getAbsolutePath());

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        try {
            context.reset();
            configurator.doConfigure(getAssets().open(LOG_CONFIG_FILE));
        } catch (JoranException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addScannerActivity() throws ApiScannerException {
        tvScannerActivityText = (TextView) currentActivity.findViewById(R.id.tvMain);

        tvScannerActivityText.setText(scannerActivityText);
        tvScannerActivityText.setMovementMethod(new ScrollingMovementMethod());

        if (isScannerActive)
            return;

        for (DeviceData deviceData : connectedDevicesData.values())
            deviceData.device.getScanner().addListenerScan(scannerActivityCallback);

        isScannerActive = true;
    }

    public void removeScannerActivity() throws ApiScannerException {
        tvScannerActivityText = null;

        if (!isScannerActive)
            return;

        for (DeviceData deviceData : connectedDevicesData.values())
            deviceData.device.getScanner().removeListenerScan(scannerActivityCallback);

        isScannerActive = false;
    }

    public void addScannerActivityLine(String line) {
        scannerActivityText += line;
        if (tvScannerActivityText != null)
            tvScannerActivityText.setText(scannerActivityText);
    }

    public void addScanPrintScenario() throws ApiScannerException, ApiDeviceException {
        if (isScanPrintScenarioActive)
            return;

        for (DeviceData deviceData : connectedDevicesData.values()) {
            //Setting trigger mode to scan, so we can use trigger for scan&print scenarios, and not refer to the button.
            deviceData.device.setTriggerMode(TriggerMode.Scan);

            //Enabling the scanner, so that it works, otherwise this scenario would be rendered useless.
            setScanEngineEnabled(deviceData, true);

            deviceData.device.getScanner().addListenerScan(scanPrintScenarioCallback);
        }

        isScanPrintScenarioActive = true;
    }

    public void removeScanPrintScenario() throws ApiScannerException {
        if (!isScanPrintScenarioActive)
            return;

        for (DeviceData deviceData : connectedDevicesData.values())
            deviceData.device.getScanner().removeListenerScan(scanPrintScenarioCallback);

        isScanPrintScenarioActive = false;
    }

    public void addSystemActivity() {
        // It is guaranteed that this methos is called only when getDevice() returns a correct device.
        IDevice device = getDevice();
        try {
            device.addListenerTriggerPress(triggerPressCallback);
        } catch (ApiDeviceException e) {
            currentActivity.showMessageBox("Adding listener failed",
                    String.format("Trigger press listener has not been added. Error code is %d. Listening to trigger press will be inactive.", e.getErrorCode()),
                    "Ok", null, null, null, device);
        }
    }

    public void removeSystemActivity() {
        IDevice currentDevice = getDevice();
        if (currentDevice != null) {
            try {
                currentDevice.removeListenerTriggerPress(triggerPressCallback);
            } catch (ApiDeviceException e) {
            }
        }
    }

    public void registerResource(ResourceMediaType resourceMediaType, String alias, String fileName) throws ApiResourceException {
        File file = new File(resourcePath, fileName);
        try {
            final InputStream is = getResources().getAssets().open(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            currentActivity.showMessageBox("Resource file not found", "Unable to copy resource from application to device storage.", "OK", null, null, null, null);
            return;
        } catch (IOException e) {
            currentActivity.showMessageBox("Resource copying failed", "Unable to copy resource from application to device storage.", "OK", null, null, null, null);
            return;
        }

        if (!file.exists()) {
            currentActivity.showMessageBox("Resource copying failed", "Resource was properly copied, but is not detected at destination.", "OK", null, null, null, null);
            return;
        }

        ResourceManager.registerResource(resourceMediaType, alias, file.getAbsolutePath());
        if (!ResourceManager.checkResource(resourceMediaType, alias)) {
            currentActivity.showMessageBox("Resource copying failed", "Resource is registered, but its registration is not verified by the resource check.", "OK", null, null, null, null);
        }
    }

    private String apiBarcodeTypeToLntBarcodeType(BarcodeType barcodeType) {
        String result = "";
        switch (barcodeType) {
            case Upca:
                result = "upca";
                break;
            case Upca_2:
                result = "upca+2";
                break;
            case Upca_5:
                result = "upca+5";
                break;
            case Upce:
                result = "upce";
                break;
            case Upce_2:
                result = "upce+2";
                break;
            case Upce_5:
                result = "upce+5";
                break;
            case Upce1:
                result = "upce1";
                break;
            case Ean8:
                result = "ean8";
                break;
            case Ean8_2:
                result = "ean8+2";
                break;
            case Ean8_5:
                result = "ean8+5";
                break;
            case Ean13:
                result = "ean13";
                break;
            case Ean13_2:
                result = "ean13+2";
                break;
            case Ean13_5:
                result = "ean13+5";
                break;
            case CodeInterleaved2of5:
                result = "i2of5";
                break;
            case Code39:
                result = "code39";
                break;
            case Code93:
                result = "code93";
                break;
            case Codabar:
                result = "codabar";
                break;
            case Code128:
                result = "code128";
                break;
            case Pdf417:
                result = "pdf417";
                break;
            case MicroPdf:
                result = "micropdf417";
                break;
            case Msi:
                result = "msi";
                break;
            case MaxiCode:
                result = "maxicode";
                break;
            case DataMatrix:
                result = "data matrix";
                break;
            case Qr:
                result = "qr";
                break;
            case Rss14:
            case RssExpanded:
            case RssLimited:
                result = "rss";
                break;
            case Gs1Cca_Ean13:
            case Gs1Cca_Ean8:
            case Gs1Cca_Ean128:
            case Gs1Cca_Rss14:
            case Gs1Cca_RssExpanded:
            case Gs1Cca_RssLimited:
            case Gs1Cca_Upca:
            case Gs1Cca_Upce:
            case Gs1Ccb_Ean13:
            case Gs1Ccb_Ean8:
            case Gs1Ccb_Ean128:
            case Gs1Ccb_Rss14:
            case Gs1Ccb_RssExpanded:
            case Gs1Ccb_RssLimited:
            case Gs1Ccb_Upca:
            case Gs1Ccb_Upce:
                result = "gs1";
                break;
            default:
                result = "";
                //itf, code2of5, nw7, code128a, code128b, code128c, code16, quick response, postnet, gs1databar,
        }

        return result;
    }

    // This indices denote the usage of appropriate barcode type indicators in barcode fields of the LNT "BarcodePrint.LNT".
    // We need to determine under which index to print the barcode when printing this LNT and the function below
    // performs the search of the barcode type in this array and returns appropriate index.
    // IMPORTANT : If you alter barcode fields in BarcodePrint.LNT (by changing their order or adding new ones),
    // be sure to update this array as well!
    private static final String[] lntBarcodeTypeFieldIndices = {"upca", "upca+2", "upca+5", "upce", "upce1", "upce+2", "upce+5",
            "ean8", "ean8+2", "ean8+5", "ean13", "ean13+2", "ean13+5", "i2of5", "itf", "code39", "code93", "code2of5", "codabar",
            "nw7", "msi", "code128", "code128a", "code128b", "code128c", "pdf417", "micropdf417", "maxicode", "code16",
            "data matrix", "quick response", "qr", "postnet", "gs1databar", "gs1", "rss"};

    private int lntBarcodeTypeToLntFieldIndex(String lntBarcodeType) {
        for (int result = 0; result < lntBarcodeTypeFieldIndices.length; ++result) {
            if (lntBarcodeType.equals(lntBarcodeTypeFieldIndices[result]))
                return result;
        }

        return -1;
    }
}

