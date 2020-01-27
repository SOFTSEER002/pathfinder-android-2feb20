package com.fox.app.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avery.sampleapp.R;
import com.fox.app.ApiService.ApiService;
import com.fox.app.ApiService.ApiUtils;
import com.fox.app.ApiService.ResponseModel;
import com.fox.app.Utils.CustomProgressBar;
import com.fox.app.connection.ConnectionActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccessPathfinderActivity extends Activity {
    EditText accessIdEt, accessPasswordET;
    Button submitAccessBtn;
    ApiService apiService;
    public static final String PREF_KEY_FIRST_START = "FOXAPP";
    public static final int REQUEST_CODE_INTRO = 122;
    boolean firstStart;
    CustomProgressBar customProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_pathfinder);

        apiService = ApiUtils.getAPIService();
        accessIdEt = findViewById(R.id.accessIdEt);
        accessPasswordET = findViewById(R.id.accessPasswordET);
        submitAccessBtn = findViewById(R.id.submitAccessBtn);
        customProgressBar = new CustomProgressBar(this);

        firstStart = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(PREF_KEY_FIRST_START, true);
        submitAccessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!accessIdEt.getText().toString().equals("") && !accessPasswordET.getText().toString().equals("")) {
                    customProgressBar.showProgress();
                    getBarcodeResponse("203550550", accessIdEt.getText().toString(), accessPasswordET.getText().toString());
                    PreferenceManager.getDefaultSharedPreferences(AccessPathfinderActivity.this).edit()
                            .putBoolean(PREF_KEY_FIRST_START, false)
                            .apply();
                }
            }
        });

    }

    void getBarcodeResponse(final String barcode, String accessId, String accessPassword) {
        apiService.getBarcodeResult(accessId, accessPassword, barcode).enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.code() == 200) {
                    customProgressBar.hideProgress();
                    Toast.makeText(AccessPathfinderActivity.this, "User Authenticated!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AccessPathfinderActivity.this, ConnectionActivity.class);
                    intent.putExtra("Scanner", "scanner");
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(AccessPathfinderActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                customProgressBar.hideProgress();

            }
        });
    }
}
