package com.fox.app.scanner;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.fox.app.scanner.barcodesettings.Gs1Activity;
import com.fox.app.scanner.barcodesettings.QRCodeActivity;
import com.fox.app.scanner.barcodesettings.UPCActivity;
import com.avery.sampleapp.R;
import avd.api.core.exceptions.ApiScannerException;
import com.fox.app.SampleAppActivity;
import com.fox.app.SampleApplication;
import com.fox.app.scanner.barcodesettings.CodaBarActivity;
import com.fox.app.scanner.barcodesettings.Code128Activity;
import com.fox.app.scanner.barcodesettings.Code39Activity;
import com.fox.app.scanner.barcodesettings.Code93Activity;
import com.fox.app.scanner.barcodesettings.D2of5Activity;
import com.fox.app.scanner.barcodesettings.DataMatrixActivity;
import com.fox.app.scanner.barcodesettings.GS1ext2dActivity;
import com.fox.app.scanner.barcodesettings.I2of5Activity;
import com.fox.app.scanner.barcodesettings.MSIActivity;
import com.fox.app.scanner.barcodesettings.MaxiCodeActivity;
import com.fox.app.scanner.barcodesettings.PDF_417Activity;

public class BarcodeSettingsActivity extends SampleAppActivity {
	
	private SampleApplication application = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_settings);

		application = ((SampleApplication) getApplication());
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		this.setupDefaults();
	}

	private void setupDefaults() {
		Button btnCodabar = (Button) findViewById(R.id.btnCodabar);
		btnCodabar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showCodabar();
			}
		});

		Button btnCode128 = (Button) findViewById(R.id.btnCode128);
		btnCode128.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showCode128();
			}
		});

		Button btnCode39 = (Button) findViewById(R.id.btnCode39);
		btnCode39.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showCode39();
			}
		});

		Button btnCode93 = (Button) findViewById(R.id.btnCode93);
		btnCode93.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showCode93();
			}
		});

		Button btnMSI = (Button) findViewById(R.id.btnMSI);
		btnMSI.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showMSI();
			}
		});

		Button btnD2of5 = (Button) findViewById(R.id.btnD2of5);
		btnD2of5.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showD2Of5();
			}
		});

		Button btnUPC = (Button) findViewById(R.id.btnUPC);
		btnUPC.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showUPC();
			}
		});

		Button btnGS1 = (Button) findViewById(R.id.btnGS1);
		btnGS1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showGS1();
			}
		});

		Button btnI2of5 = (Button) findViewById(R.id.btnI2of5);
		btnI2of5.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showI2Of5();
			}
		});

		Button btnDataMatrix = (Button) findViewById(R.id.btnDataMatrix);
		btnDataMatrix.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDataMatrix();
			}
		});

		Button btnGS12D = (Button) findViewById(R.id.btnGS12D);
		btnGS12D.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showGS1_2D();
			}
		});

		Button btnMaxiCode = (Button) findViewById(R.id.btnMaxiCode);
		btnMaxiCode.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showMaxiCode();
			}
		});

		Button btnPDF417 = (Button) findViewById(R.id.btnPDF417);
		btnPDF417.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showPDF417();
			}
		});

		Button btnQRCode = (Button) findViewById(R.id.btnQRCode);
		btnQRCode.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showQRCode();
			}
		});

		Button btnDisableBarcodes = (Button) findViewById(R.id.btnDisableBarcodes);
		btnDisableBarcodes.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				disableAllBarcodes();
			}
		});
	}

	private void showCodabar() {
		application.getDevice().getScanner().beginSetSession();
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		Intent intent = new Intent(this, CodaBarActivity.class);
		startActivity(intent);
	}

	private void showCode128() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();
		Intent intent = new Intent(this, Code128Activity.class);
		startActivity(intent);
	}

	private void showCode39() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();
		Intent intent = new Intent(this, Code39Activity.class);
		startActivity(intent);
	}

	private void showCode93() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();
		Intent intent = new Intent(this, Code93Activity.class);
		startActivity(intent);
	}

	private void showMSI() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();
		Intent intent = new Intent(this, MSIActivity.class);
		startActivity(intent);
	}

	private void showD2Of5() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();
		Intent intent = new Intent(this, D2of5Activity.class);
		startActivity(intent);
	}

	private void showUPC() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();
		Intent intent = new Intent(this, UPCActivity.class);
		startActivity(intent);
	}

	private void showGS1() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();	
		Intent intent = new Intent(this, Gs1Activity.class);
		startActivity(intent);
	}

	private void showI2Of5() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();
		Intent intent = new Intent(this, I2of5Activity.class);
		startActivity(intent);
	}

	private void showDataMatrix() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();
		Intent intent = new Intent(this, DataMatrixActivity.class);
		startActivity(intent);
	}

	private void showGS1_2D() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();
		Intent intent = new Intent(this, GS1ext2dActivity.class);
		startActivity(intent);
	}

	private void showMaxiCode() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();
		Intent intent = new Intent(this, MaxiCodeActivity.class);
		startActivity(intent);
	}

	private void showPDF417() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();
		Intent intent = new Intent(this, PDF_417Activity.class);
		startActivity(intent);
	}

	private void showQRCode() {
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		application.getDevice().getScanner().beginSetSession();
		Intent intent = new Intent(this, QRCodeActivity.class);
		startActivity(intent);
	}

	private void disableAllBarcodes() {	
		if (!application.isButtonPressAllowed)
			return;
		
		application.isButtonPressAllowed = false;
		showMessageBox("Disable all barcodes",
				"Are you sure you want to disable all barcodes for the device?",
				"Yes", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						try {
							application.getDevice().getScanner().disableAllBarcodes();
							application.getDevice().getScanner().unloadSettings();
						} catch (ApiScannerException e) {
							showStandardErrorMessageBox("Disabling barcodes failed", e, application.getDevice());
						}
						
						application.isButtonPressAllowed = true;
					}
				}, "No", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						application.isButtonPressAllowed = true;
					}
				}, application.getDevice());
	}
}
