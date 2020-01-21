package com.fox.app.connection;

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
import android.widget.ListView;
import android.widget.Toast;

import com.fox.app.SampleAppActivity;
import com.fox.app.SampleApplication;
import com.avery.sampleapp.R;

import java.util.Set;

import avd.api.core.ConnectionType;
import avd.api.core.exceptions.ApiCommunicationException;
import avd.api.core.exceptions.ApiDeviceException;
import avd.api.core.exceptions.ApiDeviceManagerException;
import avd.api.core.exceptions.ApiPrinterException;
import avd.api.core.exceptions.ApiScannerException;
import avd.api.devices.management.DeviceConnectionInfo;

public class ConnectionActivity extends SampleAppActivity {
    private static final int REQUEST_ENABLE_BT = 3;

    private ListView pairedListView = null;
    private ListView newDevicesListView = null;

    private ProgressDialog progressDialog = null;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;

    private SampleApplication application = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_connection);

        application = ((SampleApplication) getApplication());
        application.requiredDevice = application.NO_DEVICES;
        application.lastActivityForAnyDevice = ConnectionActivity.class;

        Button scanButton = (Button) findViewById(R.id.btnScanForDevices);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
            }
        });

        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice);
        newDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice);

        pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(newDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

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
                    newDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                newDevicesArrayAdapter.clear();
                setTitle(R.string.scanning);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (newDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    newDevicesArrayAdapter.add(noDevices);
                    newDevicesListView.setEnabled(false);
                } else
                    newDevicesListView.setEnabled(true);
            }
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
        pairedDevicesArrayAdapter.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedListView.setEnabled(true);
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                pairedListView.setItemChecked(pairedListView.getCount() - 1, application.connectedDevicesData.containsKey(device.getName()));
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
            pairedListView.setEnabled(false);
        }
        pairedDevicesArrayAdapter.notifyDataSetChanged();
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
                showMessageBox("Device Connected", "Device " + deviceSerial + " has been successfully connected.", "Ok", null, null, null, null);
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
            ListView parentListView = null;
            int itemIndex = -1;

            itemIndex = newDevicesArrayAdapter.getPosition(deviceInfo);
            if (itemIndex != -1)
                parentListView = newDevicesListView;
            else {
                parentListView = pairedListView;
                itemIndex = pairedDevicesArrayAdapter.getPosition(deviceInfo);
            }

            progressDialog.dismiss();
            if (result == CT_SUCCESS) {
                if (parentListView == newDevicesListView) {
                    newDevicesListView.setItemChecked(itemIndex, false);
                    newDevicesArrayAdapter.remove(deviceInfo);
                    displayPairedDevices();
                }
            } else
                parentListView.setItemChecked(itemIndex, false);

            newDevicesArrayAdapter.notifyDataSetChanged();
            sendBroadcast(new Intent(SampleApplication.INTENT_ACTION_UPDATE_SCREEN_CONTROLS));
        }
    }

}
