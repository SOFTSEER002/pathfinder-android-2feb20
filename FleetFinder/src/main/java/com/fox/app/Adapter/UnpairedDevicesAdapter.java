package com.fox.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.avery.sampleapp.R;

import java.util.ArrayList;

public class UnpairedDevicesAdapter extends RecyclerView.Adapter<UnpairedDevicesAdapter.Holder> {
    Context context;
    ArrayList<String> deviceNameSerial = new ArrayList<String>();
    GetDevice getDevice;


    public UnpairedDevicesAdapter(Context context, ArrayList<String> deviceNameSerial ) {
        this.context = context;
        this.deviceNameSerial = deviceNameSerial;

    }
    public void setOnClickLister(GetDevice getDevice) {
        this.getDevice = getDevice;
    }

    @NonNull
    @Override
    public UnpairedDevicesAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.unpaired_devices, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final UnpairedDevicesAdapter.Holder holder, final int position) {
        holder.deviceName.setText(deviceNameSerial.get(position));
        if(deviceNameSerial.get(position).contains("No devices found")){
            holder.linearLayout.setEnabled(false);
        }else{
            holder.linearLayout.setEnabled(true);

        }
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDevice.deviceDetails(holder.deviceName.getText().toString(), position);

            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceNameSerial.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView deviceName;
        LinearLayout linearLayout;
        public Holder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.tvDevice);
            linearLayout = itemView.findViewById(R.id.linearLayout);
        }
    }

    public interface GetDevice {
        void deviceDetails(String Device, int position);
    }
}
