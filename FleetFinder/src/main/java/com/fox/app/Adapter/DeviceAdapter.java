package com.fox.app.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.avery.sampleapp.R;
import com.fox.app.Utils.SharedPreferenceMethod;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.Holder> {
    Context context;
    ArrayList<String> deviceNameSerial = new ArrayList<String>();
    GetDevice getDevice;
    SharedPreferenceMethod sharedPreferenceMethod;
    String savedDeviceCheck;

    public DeviceAdapter(Context context, ArrayList<String> deviceNameSerial, SharedPreferenceMethod sharedPreferenceMethod) {
        this.context = context;
        this.deviceNameSerial = deviceNameSerial;
        this.sharedPreferenceMethod = sharedPreferenceMethod;
    }

    public void setOnClickLister(GetDevice getDevice) {
        this.getDevice = getDevice;
    }

    @NonNull
    @Override
    public DeviceAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.paired_devices, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final DeviceAdapter.Holder holder, final int position) {
        holder.deviceName.setText(deviceNameSerial.get(position));
        Log.e("SHARED", "onBindViewHolder: " + sharedPreferenceMethod.getDeviceName());
        if (!sharedPreferenceMethod.getDeviceName().equals("") && deviceNameSerial.get(position).equals(sharedPreferenceMethod.getDeviceName())) {
            holder.connectionButton.setText("Disconnect");
        }
        holder.connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDevice.deviceDetails(deviceNameSerial.get(position), position);
                if (holder.connectionButton.getText().equals("Connected")) {
                    holder.connectionButton.setText("Connect");
                    sharedPreferenceMethod.saveDeviceName("");
                    return;
                }
                if (holder.connectionButton.getText().equals("Connect")) {
                    holder.connectionButton.setText("Connecting...");
                }/*else {
                    holder.connectionButton.setText("Connected");
                    sharedPreferenceMethod.saveDeviceName(deviceNameSerial.get(position));
                }*/
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceNameSerial.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView deviceName;
        Button connectionButton;

        public Holder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.tvDevice);
            connectionButton = itemView.findViewById(R.id.connectionButton);
        }
    }

    public interface GetDevice {
        void deviceDetails(String Device, int position);
    }
}
