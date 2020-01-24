package com.fox.app.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceMethod {
    Context context;
    SharedPreferences sp;

    public SharedPreferenceMethod(Context context) {
        this.context = context;

    }
        public void saveDeviceName(String deviceName){
            sp = context.getSharedPreferences("FleetFinder", Context.MODE_PRIVATE);
            SharedPreferences.Editor sp_editior = sp.edit();
            sp_editior.putString("deviceName", deviceName);
            sp_editior.commit();
        }

    public String getDeviceName() {
        SharedPreferences sp = context.getSharedPreferences("FleetFinder", Context.MODE_PRIVATE);
        return sp.getString("deviceName", "");
    }


}
