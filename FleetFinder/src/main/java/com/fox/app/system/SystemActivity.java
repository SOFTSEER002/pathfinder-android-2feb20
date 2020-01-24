package com.fox.app.system;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.avery.sampleapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import avd.api.core.exceptions.ApiConfigurationException;
import avd.api.core.exceptions.ApiDeviceException;
import avd.api.core.exceptions.ApiException;
import avd.api.core.imports.FeedButtonMode;
import avd.api.core.imports.TriggerMode;
import com.fox.app.Activities.PropertiesSettingActivity;
import com.fox.app.SampleApplication;

public class SystemActivity extends PropertiesSettingActivity {
	private TextView tvVolumeValue = null;
	private TextView tvPlaySoundNumber = null;
	private Spinner spinnerSoundAlias = null; 
	
	private final String PLAY_SOUND_SCAN_GOOD = "ScanGood";
	private final String PLAY_SOUND_SCAN_BAD = "Scan_Bad";
	private final String PLAY_LOOK_PASS = "LookPass";
	private final String PLAY_LOOK_FAIL = "LookFail";
	private final List<String> listSoundNames = new ArrayList<String>(Arrays.asList(PLAY_SOUND_SCAN_GOOD, PLAY_SOUND_SCAN_BAD, PLAY_LOOK_PASS,PLAY_LOOK_FAIL));
	
	private int playSoundNumber = 1;
	private String playSoundName = PLAY_SOUND_SCAN_GOOD;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_system);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		// We need to unsubscribe from the callback only when user manually goes to previous screen.
		// Otherwise the system performs unsubscribing automatically on SDK level.
		application.removeSystemActivity(); 
	}
	
	@Override
	public void refreshControls() throws ApiDeviceException, ApiConfigurationException
	{
		if (!application.getDeviceData().isSystemParametersLoaded)
		{
			device.loadSettings();
			application.getDeviceData().isSystemParametersLoaded = true;
		}
		
		TextView tvStatusValue = (TextView) findViewById(R.id.tvStatusValue);
		tvStatusValue.setText(String.valueOf(device.getStatus()));

		TextView tvSDKValue = (TextView) findViewById(R.id.tvSDKVersionValue);
        tvSDKValue.setText(SampleApplication.SDK_VERSION);

		TextView tvBatteryLevelValue = (TextView) findViewById(R.id.tvBatteryLevelValue);
		tvBatteryLevelValue.setText(String.valueOf(device.getBatteryLevel()));

		TextView tvBatteryVoltageValue = (TextView) findViewById(R.id.tvBatteryVoltageValue);
		tvBatteryVoltageValue.setText(String.valueOf(device.getBatteryVoltage()));
		
		TextView tvFirmwareVersionValue = (TextView) findViewById(R.id.tvFirmwareVersionValue);
		tvFirmwareVersionValue.setText(device.getModelVersion());
		
		TextView tvDeviceModelValue = (TextView) findViewById(R.id.tvDeviceModelValue);
		tvDeviceModelValue.setText(device.getModelName());
		
		TextView tvCdilVersionValue = (TextView) findViewById(R.id.tvCdilVersionValue);
		tvCdilVersionValue.setText(device.getCdilVersion());

		TextView tvDeviceComponentsValue = (TextView) findViewById(R.id.tvDeviceComponentsValue);
		tvDeviceComponentsValue.setText(device.getComponents());

		ToggleButton tbFeedButtonCallbackValue = (ToggleButton) findViewById(R.id.tbFeedButtonCallbackValue);
		tbFeedButtonCallbackValue.setChecked(device.getFeedButtonMode() == FeedButtonMode.Forward);
		tbFeedButtonCallbackValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				enableFeedButton(isChecked);
			}
		});


		RadioGroup rgTriggerMode = (RadioGroup) findViewById(R.id.rgTriggerMode);
		switch (device.getTriggerMode())
		{
		case Scan:
			rgTriggerMode.check(R.id.rbScan);
			break;
		case Drop:
			rgTriggerMode.check(R.id.rbDrop);
			break;
		case Forward:
			rgTriggerMode.check(R.id.rbForward);
			break;
		default:
			throw new IllegalArgumentException("Obtained unknown trigger mode");
		}
		rgTriggerMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				try {
					switch (checkedId) {
						case R.id.rbScan: {
							device.setTriggerMode(TriggerMode.Scan);
							break;
						}
						case R.id.rbDrop: {
							device.setTriggerMode(TriggerMode.Drop);
							break;
						}
						case R.id.rbForward: {
							device.setTriggerMode(TriggerMode.Forward);
							break;
						}

						default:
							throw new IllegalArgumentException("Trigger mode is set to wrong value");
					}
				} catch (IllegalArgumentException e) {
					showMessageBox("Setting parameter failed", "Unknown trigger mode is trying to be set to the device", "Ok", null, null, null, device);
				} catch (ApiException e) {
					showStandardErrorMessageBox("Setting parameter failed", e, device);
				}
			}
		});


		int soundVolume = device.getSoundVolume();
		tvVolumeValue = (TextView) findViewById(R.id.tvVolumeValue);
		tvVolumeValue.setText(String.valueOf(soundVolume));

		SeekBar sbVolume = (SeekBar) findViewById(R.id.sbVolume);
		sbVolume.setProgress(soundVolume);
		sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				tvVolumeValue.setText(String.valueOf(progress));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					device.setSoundVolume((byte) (seekBar.getProgress()));
				} catch (ApiDeviceException e) {
					showStandardErrorMessageBox("Setting parameter failed", e, device);
				}
			}
		});

		Button btnPlaySoundNumberValue = (Button) findViewById(R.id.btnPlaySound);
		btnPlaySoundNumberValue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playSound();
			}
		});

		tvPlaySoundNumber = (TextView) findViewById(R.id.tvPlaySoundNumberValue);
		tvPlaySoundNumber.setText(String.valueOf(playSoundNumber));

		Button btnPlaySoundNumberMinus = (Button) findViewById(R.id.btnPlaySoundNumberMinus);
		btnPlaySoundNumberMinus.setText("-");
		btnPlaySoundNumberMinus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (playSoundNumber > 1) {
					--playSoundNumber;
					tvPlaySoundNumber.setText(String.valueOf(playSoundNumber));
				}
			}
		});

		Button btnPlaySoundNumberPlus = (Button) findViewById(R.id.btnPlaySoundNumberPlus);
		btnPlaySoundNumberPlus.setText("+");
		btnPlaySoundNumberPlus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (playSoundNumber < 8) {
					++playSoundNumber;
					tvPlaySoundNumber.setText(String.valueOf(playSoundNumber));
				}
			}
		});

		ArrayAdapter<String> adapterSoundNames = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listSoundNames);
		adapterSoundNames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSoundAlias = (Spinner) findViewById(R.id.spinnerSoundAlias);
		spinnerSoundAlias.setAdapter(adapterSoundNames);

		Button btnResetSettingsButton = (Button) findViewById(R.id.btnResetSettingsButton);
		btnResetSettingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSettings();
			}
		});
	}
	
	private void enableFeedButton(Boolean value)
	{
		try {
			device.setFeedButtonMode(value ? FeedButtonMode.Forward : FeedButtonMode.Feed);
		} catch (ApiDeviceException e) {
			showStandardErrorMessageBox("Setting parameter failed", e, device);
		}
		
	}

	private void resetSettings()
	{
		showMessageBox("Reset all system settings",
				"Are you sure you want to reset all settings of the device (including printer and scanner settings) to their default values?",
				"Yes", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						try {
							application.getDeviceData().isSystemParametersLoaded = false;
							application.getDeviceData().isPrinterParametersLoaded = false;
							application.getDeviceData().isScannerParametersLoaded = false;
							application.getDeviceData().isBarcodeParametersLoaded = false;
									
							device.resetConfiguration();
							
							application.getDeviceData().isSystemParametersLoaded = true;
						} catch (ApiDeviceException e) {
							showStandardErrorMessageBox("Resetting parameter failed", e, device);
						}
						
						sendBroadcast(new Intent(SampleApplication.INTENT_ACTION_UPDATE_SCREEN_CONTROLS));
					}
				}, "No", null, device);
	}
	
	private void playSound()
	{
		try {
			playSoundName = spinnerSoundAlias.getSelectedItem().toString();
			device.playSound(playSoundName, playSoundNumber);
		} catch (ApiDeviceException e) {
			showStandardErrorMessageBox("Playing sound failed", e, device);
		}
	}
	
	@Override
	protected void unloadParameters() {
		application.getDeviceData().isSystemParametersLoaded = false;
		device.unloadSettings();
	}
	
	@Override
	protected String getMessageObtainingParametersFailed() {
		return "Obtaining system parameters failed";
	}

}