package com.fox.app.connection;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.avery.sampleapp.R;
import com.fox.app.Activities.AccessPathfinderActivity;
import com.fox.app.Adapter.DeviceAdapter;
import com.fox.app.Activities.SampleAppActivity;
import com.fox.app.SampleApplication;
import com.fox.app.Utils.SharedPreferenceMethod;
import com.fox.app.Adapter.UnpairedDevicesAdapter;
import com.fox.app.scanner.ScannerActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import avd.api.core.ConnectionType;
import avd.api.core.exceptions.ApiCommunicationException;
import avd.api.core.exceptions.ApiDeviceException;
import avd.api.core.exceptions.ApiDeviceManagerException;
import avd.api.core.exceptions.ApiPrinterException;
import avd.api.core.exceptions.ApiScannerException;
import avd.api.devices.management.DeviceConnectionInfo;

public class ConnectionActivity extends SampleAppActivity implements DeviceAdapter.GetDevice, UnpairedDevicesAdapter.GetDevice {
    private static final int REQUEST_ENABLE_BT = 3;
    SharedPreferenceMethod sharedPreferenceMethod;
    private RecyclerView pairedListView = null;
    private RecyclerView newDevicesListView = null;
    ArrayList<String> pairedDeviceList = new ArrayList<>();
    ArrayList<String> unpairedDeviceList = new ArrayList<>();
    private ProgressDialog progressDialog = null;
    DeviceAdapter deviceAdapter;
    UnpairedDevicesAdapter unpairedDevicesAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;
    DeviceAdapter.GetDevice getDevice;
    String selectedDevice="";
    private SampleApplication application = null;
    private int getPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_connection);

        application = ((SampleApplication) getApplication());
        sharedPreferenceMethod = new SharedPreferenceMethod(this);
        application.requiredDevice = application.NO_DEVICES;
        application.lastActivityForAnyDevice = ConnectionActivity.class;

        Button scanButton = (Button) findViewById(R.id.btnScanForDevices);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                Toast.makeText(application, "Scanning for new Devices...", Toast.LENGTH_SHORT).show();
            }
        });


        deviceAdapter = new DeviceAdapter(this, pairedDeviceList, sharedPreferenceMethod);
        unpairedDevicesAdapter = new UnpairedDevicesAdapter(this, unpairedDeviceList);

//        newDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice);


        deviceAdapter.setOnClickLister(this);
        unpairedDevicesAdapter.setOnClickLister(this);
        pairedListView = findViewById(R.id.paired_devices);
        pairedListView.setHasFixedSize(true);
        pairedListView.setItemAnimator(new DefaultItemAnimator());
        pairedListView.setLayoutManager(new LinearLayoutManager(this));
//        pairedListView.setOnItemClickListener(mDeviceClickListenerPaired);

        newDevicesListView = findViewById(R.id.new_devices);
        newDevicesListView.setHasFixedSize(true);
        newDevicesListView.setLayoutManager(new LinearLayoutManager(this));
        newDevicesListView.setItemAnimator(new DefaultItemAnimator());
        newDevicesListView.setAdapter(unpairedDevicesAdapter);
//        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

//        permission check
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
        // This will enable screen controls refreshment every time the screen is entered or an action requiring screen controls update happens
        IntentFilter filter = new IntentFilter(SampleApplication.INTENT_ACTION_UPDATE_SCREEN_CONTROLS);
        this.registerReceiver(mReceiver, filter);

        // This will enable new device added to the list of found devices in a response to the event when a new bluetooth device is found
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // This will enable clearance of display of found devices and setting an appropriate title in a response to the event when a new search for bluetooth devices is commenced
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        this.registerReceiver(mReceiver, filter);

        // This will enable setting an appropriate title and enabling list of found devices for selection in a response to the event when a new search for bluetooth devices is concluded
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        // This will disable all reactions to bluetooth actions and screen refreshment requirements
        unregisterReceiver(mReceiver);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK)
                    displayPairedDevices();
                else {
                    // Bluetooth has been disabled - user should be informed about this,
                    // data on devices should be erased and user should be navigated
                    // to the main screen.
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    application.eraseDevices();
                    finish();
                }
        }
    }

    private void doDiscovery() {
        setProgressBarIndeterminateVisibility(true);

        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        cancelBluetoothDiscovery();
        bluetoothAdapter.startDiscovery();
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            cancelBluetoothDiscovery();
            CheckedTextView ctv = (CheckedTextView) v;
            String info = ctv.getText().toString();
            String[] deviceParams = info.split("\n");

            // Instead of probing the text view for being checked, we check if the device specified in the text view is actually connected.
            // On some devices text view isChecked property is set to true later than this probing is executed.
            if (application.connectedDevicesData.containsKey(deviceParams[0].toString())) {
                application.eraseDevice(deviceParams[0]);
                showMessageBox("Device Disconnected", "Device " + deviceParams[0] + " has been successfully disconnected.", "Ok", null, null, null, null);
            } else {
                progressDialog = ProgressDialog.show(ConnectionActivity.this, "", getResources().getText(R.string.connecting).toString());
                ConnectTask task = new ConnectTask();
                task.execute(deviceParams[0], deviceParams[1], info);
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SampleApplication.INTENT_ACTION_UPDATE_SCREEN_CONTROLS.equals(action))
                displayPairedDevices();
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//                    newDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    unpairedDeviceList.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//                newDevicesArrayAdapter.clear();
                unpairedDeviceList.clear();
                setTitle(R.string.scanning);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (unpairedDevicesAdapter.getItemCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
//                    newDevicesArrayAdapter.add(noDevices);
                    unpairedDeviceList.add(noDevices);

                    newDevicesListView.setEnabled(false);
                } else
                    newDevicesListView.setEnabled(true);
            }
            newDevicesListView.setAdapter(unpairedDevicesAdapter);
        }
    };

    private void cancelBluetoothDiscovery() {
        //If user tries to connect to the device, before discovery was finished,
        //attempt may report that the socket is taken, and hence signal an error.
        //Attempting to cancel discovery before trying to connect to the device.
        boolean cancelledSuccessfully = !bluetoothAdapter.isDiscovering();
        int numberOfAttemptsToCancelDiscovery = 0; //Trying three times to cancel the discovery.
        while ((numberOfAttemptsToCancelDiscovery < 3) && !cancelledSuccessfully) {
            numberOfAttemptsToCancelDiscovery++;
            cancelledSuccessfully = bluetoothAdapter.cancelDiscovery(); //Cancelling the discovery and waiting for half a second
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void displayPairedDevices() {
//        pairedDevicesArrayAdapter.clear();
        pairedDeviceList.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedListView.setEnabled(true);
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
//                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                pairedDeviceList.add(device.getName() + "\n" + device.getAddress());
//                pairedListView.setItemChecked(pairedListView.getCount() - 1, application.connectedDevicesData.containsKey(device.getName()));
            }
            pairedListView.setAdapter(deviceAdapter);

        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
//            pairedDevicesArrayAdapter.add(noDevices);
            pairedDeviceList.add(noDevices);
            pairedListView.setEnabled(false);
        }
//        pairedDevicesArrayAdapter.notifyDataSetChanged();
//        deviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void deviceDetails(String Device, int position) {
        String[] deviceParams = Device.split("\n");
        getPosition = position;
        // Instead of probing the text view for being checked, we check if the device specified in the text view is actually connected.
        // On some devices text view isChecked property is set to true later than this probing is executed.
        if (application.connectedDevicesData.containsKey(deviceParams[0].toString())) {
            application.eraseDevice(deviceParams[0]);
            showMessageBox("Device Disconnected", "Device " + deviceParams[0] + " has been successfully disconnected.", "Ok", null, null, null, null);
        } else {
            selectedDevice = Device;
            progressDialog = ProgressDialog.show(ConnectionActivity.this, "", getResources().getText(R.string.connecting).toString());
            ConnectTask task = new ConnectTask();
            task.execute(deviceParams[0], deviceParams[1], Device);
        }
    }

    private class ConnectTask extends AsyncTask<String, Void, Integer> {

        private final Integer CT_SUCCESS = 0;
        private final Integer CT_ERROR = 1;

        private String deviceSerial = null;
        private String deviceAddress = null;
        private String deviceInfo = null;

        @Override
        protected Integer doInBackground(String... params) {
            deviceSerial = params[0];
            deviceAddress = params[1];
            deviceInfo = params[2];
            Log.e("Device Serial", "doInBackground: " + deviceSerial + "\n" + deviceAddress + "\n" + deviceInfo);

            Integer result = CT_ERROR;
            try {
                application.createDevice(new DeviceConnectionInfo(deviceSerial, deviceAddress, ConnectionType.Bluetooth));
                application.currentDeviceName = deviceSerial;
                result = CT_SUCCESS;

                if(!selectedDevice.equals("")) {
                    sharedPreferenceMethod.saveDeviceName(selectedDevice);
                }
//                showMessageBox("Device Connected", "Device " + deviceSerial + " has been successfully connected.", "Ok", null, null, null, null);
//                Toast.makeText(ConnectionActivity.this, "Device Connected " + deviceSerial, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ConnectionActivity.this, ScannerActivity.class);
                intent.putExtra("Scanner", "scanner");
                startActivity(intent);

            } catch (ApiDeviceManagerException e) {
                showErrorMessageBox("Device manager error", e, "Ok", null, null, null, null);
            } catch (ApiDeviceException e) {
                showErrorMessageBox("Device error", e, "Ok", null, null, null, null);
            } catch (ApiCommunicationException e) {
                showErrorMessageBox("Communication error", e, "Ok", null, null, null, null);
            } catch (ApiPrinterException e) {
                showErrorMessageBox("Printer error", e, "Ok", null, null, null, null);
            } catch (ApiScannerException e) {
                showErrorMessageBox("Scanner error", e, "Ok", null, null, null, null);
            }
            return result;
        }

        protected void onPostExecute(Integer result) {
            RecyclerView pListView = null;
            RecyclerView parentListView = null;
            int itemIndex = -1;

            itemIndex = getPosition;
            if (itemIndex != -1)
                parentListView = newDevicesListView;
            else {
                pListView = pairedListView;
                itemIndex = getPosition;
            }

            progressDialog.dismiss();
            if (result == CT_SUCCESS) {
                if (parentListView == newDevicesListView) {
                    displayPairedDevices();
                }
            } else
//                parentListView.setItemChecked(itemIndex, false);
//                unpairedDevicesAdapter.notifyDataSetChanged();
                sendBroadcast(new Intent(SampleApplication.INTENT_ACTION_UPDATE_SCREEN_CONTROLS));
        }
    }
    @Override
    protected void onPause() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        super.onPause();
    }


}
