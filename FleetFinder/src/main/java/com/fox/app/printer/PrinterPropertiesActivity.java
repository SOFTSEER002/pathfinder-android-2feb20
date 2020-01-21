package com.fox.app.printer;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ZoomControls;

import com.avery.sampleapp.R;

import avd.api.core.SupplyType;
import avd.api.core.exceptions.ApiConfigurationException;
import avd.api.core.exceptions.ApiException;
import avd.api.core.exceptions.ApiPrinterException;
import avd.api.core.imports.EnergyType;
import avd.api.printers.Printer6140;
import com.fox.app.PropertiesSettingActivity;
import com.fox.app.SampleApplication;
import avd.sdk.CompanionAPIConstants;

public class PrinterPropertiesActivity extends PropertiesSettingActivity {
	private static enum ProperPrinterValues{Contrast, SupplyPosition, MaxLength, HorizontalAdjust, VerticalAdjust,Speed};
	private boolean[] properPrinterValues = new boolean[ProperPrinterValues.values().length];
	private boolean allValuesAreProper = true;
	
	public static final int ContrastLowerBound = -120;
	public static final int ContrastUpperBound = 120;
	public static final int ContrastTrueLowerBound = -100;
	public static final int ContrastTrueUpperBound = 100;
	public static final int SupplyPositionLowerBound = -128;
	public static final int SupplyPositionUpperBound = 127;
	public static final int SupplyPositionTrueLowerBound = -99;
	public static final int SupplyPositionTrueUpperBound = 99;
	public static final int MaxLengthLowerBound = 0;
	public static final int MaxLengthUpperBound = 24;
	public static final int MaxLengthTrueLowerBound = 2;
	public static final int MaxLengthTrueUpperBound = 16;
	public static final int VerticalAdjustLowerBound = -128;
	public static final int VerticalAdjustUpperBound = 127;
	public static final int VerticalAdjustTrueLowerBound = -99;
	public static final int VerticalAdjustTrueUpperBound = 99;
	public static final int HorizontalAdjustLowerBound = -128;
	public static final int HorizontalAdjustUpperBound = 127;
	public static final int HorizontalAdjustTrueLowerBound = -99;
	public static final int HorizontalAdjustTrueUpperBound = 99;
	
	public static final int BackFeedDistanceUpperBound = 255;
	public static final int OverFeedDistanceUpperBound = 255;

	public static final int SpeedTrueUpperBound = 50;
	public static final int SpeedTrueLowerBound = 0;
	private static final int speedValues[] = {0, 15, 20, 30, 40, 50, 60, 70, 80, 90, 100};
	private int currentSpeedValue = 0;
	
	private SeekBar sbContrast = null;
	private TextView tvContrastValue = null;

	private TextView tvSupplyPositionValue = null;
	private SeekBar sbSupplyPosition = null;
	private TextView tvMaxLengthValue = null;
	private SeekBar sbMaxLength = null;
	private TextView tvVerticalAdjustValue = null;
	private SeekBar sbVerticalAdjust = null;
	private TextView tvHorizontalAdjustValue = null;
	private SeekBar sbHorizontalAdjust = null;
	/*
	private TextView tvBackDistanceValue = null;
	private SeekBar sbBackDistance = null;
	private TextView tvOverFeedDistanceValue = null;
	private SeekBar sbOverFeedDistance = null;
	*/
	private TextView tvSpeedValue = null;
	
	private Printer6140 printer = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_printer_properties);
	}
	
	@Override
	public void onBackPressed() {
		allValuesAreProper = true;
		
		for (int i = 0; i < properPrinterValues.length; ++i)
		{
			allValuesAreProper = allValuesAreProper && properPrinterValues[i];
			if (!allValuesAreProper)
				break;
		}
		
		if (allValuesAreProper) {
			super.onBackPressed();
			endSetSession();
		}
		else {
			showMessageBox("Set improper printer settings", "Some of the settings were assigned values, that are not allowed. " +
							"These values are highlighted in red, they will not be actually set, and an error will occur. " +
							"Press \"Confirm\" to attempt setting improper values. Press \"Cancel\" to stay on this page without setting anything.",
					"Confirm", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							PrinterPropertiesActivity.super.onBackPressed();
							endSetSession();
						}
					}, "Cancel", null, device);
		}
	}
	
	@Override
	public void refreshControls() throws ApiPrinterException, ApiConfigurationException
	{
		printer = (Printer6140) device.getPrinter();

		printer.beginSetSession();

		if (!application.getDeviceData().isPrinterParametersLoaded)
		{
			printer.loadSettings();
			application.getDeviceData().isPrinterParametersLoaded = true;
		}
		
		TextView tvVersionValue = (TextView) findViewById(R.id.tvVersionValue);
		tvVersionValue.setText(printer.getModelVersion());
		
		RadioGroup rgSupplyType = (RadioGroup) findViewById(R.id.rgSupplyType);
		rgSupplyType.check(getIDForCheckForSupplyType());
		rgSupplyType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				byte supplyTypeID;
				switch (checkedId) {
					case R.id.rbSupplyTypeBlackMark: {
						supplyTypeID = (byte) CompanionAPIConstants.PRT_ST_CON_BLACKMARK;
						break;
					}
					case R.id.rbSupplyTypeNone: {
						supplyTypeID = (byte) CompanionAPIConstants.PRT_ST_CON_NONE;
						break;
					}
					case R.id.rbSupplyTypeLineMode: {
						supplyTypeID = (byte) CompanionAPIConstants.PRT_ST_CON_LINEMODE;
						break;
					}
					default:
						supplyTypeID = (byte) CompanionAPIConstants.PRT_ST_CON_NONE;
						break;
				}

				try {
					printer.setSupplyType(SupplyType.getSupplyType(supplyTypeID));
				} catch (ApiPrinterException e) {
					showStandardErrorMessageBox("Parameter setting fail", e, device);
				}
			}
		});

		tvContrastValue = (TextView) findViewById(R.id.tvContrastValue);
		int contrast = printer.getContrast();
		properPrinterValues[ProperPrinterValues.Contrast.ordinal()] = (contrast >= ContrastTrueLowerBound) && (contrast <= ContrastTrueUpperBound);
		if (properPrinterValues[ProperPrinterValues.Contrast.ordinal()])
			tvContrastValue.setTextColor(android.graphics.Color.BLACK);
		else
			tvContrastValue.setTextColor(android.graphics.Color.RED);
		tvContrastValue.setText(String.valueOf(contrast));
		sbContrast = (SeekBar) findViewById(R.id.sbContrast);

		sbContrast.setMax(ContrastUpperBound - ContrastLowerBound);
		sbContrast.setProgress(contrast - ContrastLowerBound);
		sbContrast
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						progress += ContrastLowerBound;
						properPrinterValues[ProperPrinterValues.Contrast.ordinal()] = (progress >= ContrastTrueLowerBound) && (progress <= ContrastTrueUpperBound); 
						if (properPrinterValues[ProperPrinterValues.Contrast.ordinal()])
							tvContrastValue.setTextColor(android.graphics.Color.BLACK);
						else
							tvContrastValue.setTextColor(android.graphics.Color.RED);
							
						tvContrastValue.setText(String.valueOf(progress));
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						try {
							printer.setContrast((byte) (seekBar.getProgress() + ContrastLowerBound));
						} catch (ApiPrinterException e) {
							showStandardErrorMessageBox("Parameter setting fail", e, device);
						}
					}
				});

		tvSupplyPositionValue = (TextView) findViewById(R.id.tvSupplyPositionValue);
		int supplyPosition = printer.getSupplyPosition();
		properPrinterValues[ProperPrinterValues.SupplyPosition.ordinal()] = (supplyPosition >= SupplyPositionTrueLowerBound) && (supplyPosition <= SupplyPositionTrueUpperBound);
		if (properPrinterValues[ProperPrinterValues.SupplyPosition.ordinal()])
			tvSupplyPositionValue.setTextColor(android.graphics.Color.BLACK);
		else
			tvSupplyPositionValue.setTextColor(android.graphics.Color.RED);
		tvSupplyPositionValue.setText(String.valueOf(supplyPosition));
		sbSupplyPosition = (SeekBar) findViewById(R.id.sbSupplyPosition);

		sbSupplyPosition.setMax(SupplyPositionUpperBound
				- SupplyPositionLowerBound);
		sbSupplyPosition.setProgress(supplyPosition - SupplyPositionLowerBound);

		sbSupplyPosition
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						progress += SupplyPositionLowerBound;
						properPrinterValues[ProperPrinterValues.SupplyPosition.ordinal()] = (progress >= SupplyPositionTrueLowerBound) && (progress <= SupplyPositionTrueUpperBound); 
						if (properPrinterValues[ProperPrinterValues.SupplyPosition.ordinal()])
							tvSupplyPositionValue.setTextColor(android.graphics.Color.BLACK);
						else
							tvSupplyPositionValue.setTextColor(android.graphics.Color.RED);
							
						tvSupplyPositionValue.setText(String.valueOf(progress));

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						try {
							printer.setSupplyPosition((byte) (seekBar
									.getProgress() + SupplyPositionLowerBound));
						} catch (ApiPrinterException e) {
							showStandardErrorMessageBox("Parameter setting fail", e, device);
						}
					}
				});

		tvMaxLengthValue = (TextView) findViewById(R.id.tvMaxLengthValue);
		int maxLength = printer.getMaxLabelLength();
		properPrinterValues[ProperPrinterValues.MaxLength.ordinal()] = (maxLength >= MaxLengthTrueLowerBound) && (maxLength <= MaxLengthTrueUpperBound);
		if (properPrinterValues[ProperPrinterValues.MaxLength.ordinal()])
			tvMaxLengthValue.setTextColor(android.graphics.Color.BLACK);
		else
			tvMaxLengthValue.setTextColor(android.graphics.Color.RED);
		tvMaxLengthValue.setText(String.valueOf(maxLength));
		sbMaxLength = (SeekBar) findViewById(R.id.sbMaxLength);

		sbMaxLength.setMax(MaxLengthUpperBound - MaxLengthLowerBound);
		sbMaxLength.setProgress(maxLength - MaxLengthLowerBound);

		sbMaxLength
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						progress += MaxLengthLowerBound;
						properPrinterValues[ProperPrinterValues.MaxLength.ordinal()] = (progress >= MaxLengthTrueLowerBound) && (progress <= MaxLengthTrueUpperBound); 
						if (properPrinterValues[ProperPrinterValues.MaxLength.ordinal()])
							tvMaxLengthValue.setTextColor(android.graphics.Color.BLACK);
						else
							tvMaxLengthValue.setTextColor(android.graphics.Color.RED);
							
						tvMaxLengthValue.setText(String.valueOf(progress));


					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						try {
							printer.setMaxLabelLength((byte) (seekBar
									.getProgress() + MaxLengthLowerBound));
						} catch (ApiPrinterException e) {
							showStandardErrorMessageBox("Parameter setting fail", e, device);
						}
					}
				});
		tvVerticalAdjustValue = (TextView) findViewById(R.id.tvVerticalAdjustValue);
		int verticalAdjust = printer.getVerticalAdjust();
		properPrinterValues[ProperPrinterValues.VerticalAdjust.ordinal()] = (verticalAdjust >= VerticalAdjustTrueLowerBound) && (verticalAdjust <= VerticalAdjustTrueUpperBound);
		if (properPrinterValues[ProperPrinterValues.VerticalAdjust.ordinal()])
			tvVerticalAdjustValue.setTextColor(android.graphics.Color.BLACK);
		else
			tvVerticalAdjustValue.setTextColor(android.graphics.Color.RED);
		tvVerticalAdjustValue.setText(String.valueOf(verticalAdjust));
		sbVerticalAdjust = (SeekBar) findViewById(R.id.sbVerticalAdjust);

		sbVerticalAdjust.setMax(VerticalAdjustUpperBound
				- VerticalAdjustLowerBound);
		sbVerticalAdjust.setProgress(verticalAdjust - VerticalAdjustLowerBound);

		sbVerticalAdjust
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						progress += VerticalAdjustLowerBound;
						properPrinterValues[ProperPrinterValues.VerticalAdjust.ordinal()] = (progress >= VerticalAdjustTrueLowerBound) && (progress <= VerticalAdjustTrueUpperBound); 
						if (properPrinterValues[ProperPrinterValues.VerticalAdjust.ordinal()])
							tvVerticalAdjustValue.setTextColor(android.graphics.Color.BLACK);
						else
							tvVerticalAdjustValue.setTextColor(android.graphics.Color.RED);
							
						tvVerticalAdjustValue.setText(String.valueOf(progress));
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						try {
							printer.setVerticalAdjust((byte) (seekBar
									.getProgress() + VerticalAdjustLowerBound));
						} catch (ApiPrinterException e) {
							showStandardErrorMessageBox("Parameter setting fail", e, device);
						}
					}
				});
		tvHorizontalAdjustValue = (TextView) findViewById(R.id.tvHorizontalAdjustValue);
		int horizontalAdjust = printer.getHorizontalAdjust();
		properPrinterValues[ProperPrinterValues.HorizontalAdjust.ordinal()] = (horizontalAdjust >= HorizontalAdjustTrueLowerBound) && (horizontalAdjust <= HorizontalAdjustTrueUpperBound);
		if (properPrinterValues[ProperPrinterValues.HorizontalAdjust.ordinal()])
			tvHorizontalAdjustValue.setTextColor(android.graphics.Color.BLACK);
		else
			tvHorizontalAdjustValue.setTextColor(android.graphics.Color.RED);
		tvHorizontalAdjustValue.setText(String.valueOf(horizontalAdjust));
		sbHorizontalAdjust = (SeekBar) findViewById(R.id.sbHorizontalAdjust);

		sbHorizontalAdjust.setMax(HorizontalAdjustUpperBound
				- HorizontalAdjustLowerBound);
		sbHorizontalAdjust.setProgress(horizontalAdjust
				- HorizontalAdjustLowerBound);

		sbHorizontalAdjust
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						progress += HorizontalAdjustLowerBound;
						properPrinterValues[ProperPrinterValues.HorizontalAdjust.ordinal()] = (progress >= HorizontalAdjustTrueLowerBound) && (progress <= HorizontalAdjustTrueUpperBound); 
						if (properPrinterValues[ProperPrinterValues.HorizontalAdjust.ordinal()])
							tvHorizontalAdjustValue.setTextColor(android.graphics.Color.BLACK);
						else
							tvHorizontalAdjustValue.setTextColor(android.graphics.Color.RED);
							
						tvHorizontalAdjustValue.setText(String.valueOf(progress));
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						try {
							printer.setHorizontalAdjust((byte) (seekBar
									.getProgress() + HorizontalAdjustLowerBound));
						} catch (ApiPrinterException e) {
							showStandardErrorMessageBox("Parameter setting fail", e, device);
						}
					}
				});
		/*
		tvBackDistanceValue = (TextView) findViewById(R.id.tvBackDistanceValue);
		int backFeedDistance = printer
				.getBackFeedDistance();
		tvBackDistanceValue.setText(String.valueOf(backFeedDistance));
		sbBackDistance = (SeekBar) findViewById(R.id.sbBackDistance);

		sbBackDistance.setMax(BackFeedDistanceUpperBound);
		sbBackDistance.setProgress(backFeedDistance);

		sbBackDistance
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						tvBackDistanceValue.setText(String.valueOf(progress));

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						try {
							printer
									.setBackFeedDistance((byte) (seekBar
											.getProgress()));
						} catch (ApiPrinterException e) {
							showStandardErrorMessageBox("Parameter setting fail", e, device);
						}
					}
				});
		tvOverFeedDistanceValue = (TextView) findViewById(R.id.tvOverFeedDistanceValue);
		int overFeedDistance = printer
				.getOverFeedDistance();

		tvOverFeedDistanceValue.setText(String.valueOf(overFeedDistance));
		sbOverFeedDistance = (SeekBar) findViewById(R.id.sbOverFeedDistance);

		sbOverFeedDistance.setMax(OverFeedDistanceUpperBound);
		sbOverFeedDistance.setProgress(overFeedDistance);

		sbOverFeedDistance
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						tvOverFeedDistanceValue.setText(String
								.valueOf(progress));

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						try {
							printer
									.setOverFeedDistance((byte) (seekBar
											.getProgress()));
						} catch (ApiPrinterException e) {
							showStandardErrorMessageBox("Parameter setting fail", e, device);
						}
					}
				});
		 */
		tvSpeedValue = (TextView) findViewById(R.id.tvSpeedValue);
		int speed = printer.getSpeed();
		properPrinterValues[ProperPrinterValues.Speed.ordinal()] = (speed >= SpeedTrueLowerBound) && (speed <= SpeedTrueUpperBound);
		if (properPrinterValues[ProperPrinterValues.Speed.ordinal()])
			tvSpeedValue.setTextColor(android.graphics.Color.BLACK);
		else
			tvSpeedValue.setTextColor(android.graphics.Color.RED);
		tvSpeedValue.setText(String.valueOf(speed));
		
		currentSpeedValue = 0;
		for (int i = 0; i < speedValues.length; ++i)
		{
			if (speedValues[i] == speed)
			{
				currentSpeedValue = i;
				break;
			}
		}

		ZoomControls zcSpeed = (ZoomControls) findViewById(R.id.zcSpeed);
		zcSpeed.setOnZoomInClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentSpeedValue < speedValues.length - 1)
				{
					++currentSpeedValue;
					setNewSpeed(speedValues[currentSpeedValue]);
				}
			}
		});
		zcSpeed.setOnZoomOutClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentSpeedValue > 0)
				{
					--currentSpeedValue;
					setNewSpeed(speedValues[currentSpeedValue]);
				}
			}
		});

		RadioGroup rgEnergy = (RadioGroup) findViewById(R.id.rgEnergy);
		rgEnergy.check(getIDForCheckForEnergy());
		rgEnergy.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				byte energyTypeID;
				switch (checkedId) {
				case R.id.rgEnergyStandard: {
					energyTypeID = (byte) CompanionAPIConstants.PRT_EN_CON_STANDARD;
					break;
				}
				case R.id.rgEnergyHigh: {
					energyTypeID = (byte) CompanionAPIConstants.PRT_EN_CON_HIGH;
					break;
				}
				default:
					energyTypeID = (byte) CompanionAPIConstants.PRT_EN_CON_STANDARD;
					break;
				}

				try {
					printer.setEnergyType(EnergyType.getEnergyType(energyTypeID));
				} catch (ApiPrinterException e) {
					showStandardErrorMessageBox("Parameter setting fail", e, device);
				}
			}
		});

		/*
		ToggleButton tbBackFeed = (ToggleButton) findViewById(R.id.tbBackFeed);
		tbBackFeed.setChecked(printer.isOnBackFeed());
		tbBackFeed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							printer.setIsOnBackFeed(isChecked);
						} catch (ApiPrinterException e) {
							showStandardErrorMessageBox("Parameter setting fail", e, device);
						}
					}
				});

		ToggleButton tbOverFeed = (ToggleButton) findViewById(R.id.tbOverFeed);
		tbOverFeed.setChecked(printer.isOnOverFeed());
		tbOverFeed
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							printer.setIsOnOverFeed(isChecked);
						} catch (ApiPrinterException e) {
							showStandardErrorMessageBox("Parameter setting fail", e, device);
						}
					}
				});
		*/
		ToggleButton tbLineMode = (ToggleButton) findViewById(R.id.tbLineMode);
		tbLineMode.setChecked(printer.isOnLineMode());
		tbLineMode
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						try {
							printer.setIsOnLineMode(isChecked);
						} catch (ApiPrinterException e) {
							showStandardErrorMessageBox("Parameter setting fail", e, device);
						}
					}
				});
		ToggleButton tbOnBlackMark = (ToggleButton) findViewById(R.id.tbOnBlackMark);
		tbOnBlackMark.setChecked(printer.isOnBlackMark());

		TextView tvStatusValue = (TextView) findViewById(R.id.tvStatusValue);
		int status = printer.getStatus();
		tvStatusValue.setText(String.valueOf(status));

		Button btnResetSettings = (Button) findViewById(R.id.btnResetSettings);
		btnResetSettings.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					resetSettings();
				} catch (ApiPrinterException e) {
					unloadParameters();
				}
			}
		});

	}

	private int getIDForCheckForSupplyType() throws ApiPrinterException {
		int retVal;
		switch (printer.getSupplyType()) {
		case BlackMark:
			retVal = R.id.rbSupplyTypeBlackMark;
			break;

		case NonIndex:
			retVal = R.id.rbSupplyTypeNone;
			break;

		case NonIndexLineMode:
			retVal = R.id.rbSupplyTypeLineMode;
			break;

		default:
			retVal = R.id.rbSupplyTypeNone;

			break;
		}
		return retVal;
	}

	private int getIDForCheckForEnergy() throws ApiPrinterException {
		int retVal;

		switch (printer.getEnergyType()) {
		case Standard:
			retVal = R.id.rgEnergyStandard;
			break;

		case High:
			retVal = R.id.rgEnergyHigh;
			break;

		default:
			retVal = R.id.rgEnergyStandard;
			break;
		}
		return retVal;
	}

	private void setNewSpeed(int speed) {
		
		try {
			properPrinterValues[ProperPrinterValues.Speed.ordinal()] = (speed >= SpeedTrueLowerBound) && (speed <= SpeedTrueUpperBound); 
			if (properPrinterValues[ProperPrinterValues.Speed.ordinal()])
				tvSpeedValue.setTextColor(android.graphics.Color.BLACK);
			else
				tvSpeedValue.setTextColor(android.graphics.Color.RED);

			printer.setSpeed((byte) speed);
			tvSpeedValue.setText(String.valueOf(speed));
		} catch (ApiPrinterException e) {
			showStandardErrorMessageBox("Parameter setting fail", e, device);
		}
	}
	
	@Override
	protected void unloadParameters() {
		application.getDeviceData().isPrinterParametersLoaded = false;
		printer.unloadSettings();
	}

	private void resetSettings() throws ApiPrinterException {
		
		showMessageBox("Reset printer settings",
				"Are you sure you want to reset all printer settings to their default values?",
				"Yes", new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						try {
							unloadParameters();

							tvContrastValue.setTextColor(android.graphics.Color.BLACK);
							tvSupplyPositionValue.setTextColor(android.graphics.Color.BLACK);
							tvMaxLengthValue.setTextColor(android.graphics.Color.BLACK);
							tvHorizontalAdjustValue.setTextColor(android.graphics.Color.BLACK);
							tvVerticalAdjustValue.setTextColor(android.graphics.Color.BLACK);
							tvSpeedValue.setTextColor(android.graphics.Color.BLACK);

							printer.setContrast((byte) 0);
							printer.setSupplyPosition((byte) 0);
							printer.setIsOnLineMode(false);
							printer.setEnergyType(EnergyType.Standard);
							printer.setSupplyType(SupplyType.BlackMark);

							printer.setIsOnBackFeed(false);
							printer.setIsOnOverFeed(false);
							printer.setOverFeedDistance((byte) 0);
							printer.setBackFeedDistance((byte) 0);
							printer.setMaxLabelLength((byte) 8);
							printer.setSpeed((byte) 0);
							printer.setVerticalAdjust((byte) 0);
							printer.setHorizontalAdjust((byte) 0);

							printer.endSetSession();
							printer.beginSetSession();


							sendBroadcast(new Intent(SampleApplication.INTENT_ACTION_UPDATE_SCREEN_CONTROLS));

						} catch (ApiException e) {
							showStandardErrorMessageBox("Parameter resetting fail", e, device);
						}
					}
				}, "No", null, device);
	}

	@Override
	protected String getMessageObtainingParametersFailed() {
		return "Obtaining printer parameters failed";
	}

	private void endSetSession()
	{
		if (!isDeviceConnected())
			return;

		try {
			printer.endSetSession();

			//If endSetSession did not throw an exception during setting improper parameters, we must force unloading of improper parameters
			if (!allValuesAreProper)
				unloadParameters();
		} catch (ApiConfigurationException e) {
			unloadParameters();
			showErrorMessageBox("Setting printer parameters failed", e, "Ok", null, null, null, device);
		}

	}
}
