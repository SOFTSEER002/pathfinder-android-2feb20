package com.fox.app.printer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.avery.sampleapp.R;
import com.fox.app.Activities.SampleAppActivity;
import com.fox.app.SampleApplication;
import com.fox.app.SampleApplication.DeviceData;
import com.google.zxing.WriterException;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import avd.api.core.IDevice;
import avd.api.core.IPrinter;
import avd.api.core.exceptions.ApiPrinterException;

public class PrinterActivity extends SampleAppActivity {

    private int printSampleIndex = 0;
    private SampleApplication application = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_printer);

    }

    @Override
    public void onResume() {
        super.onResume();

        application = ((SampleApplication) getApplication());
        application.requiredDevice = application.ANY_DEVICE;
        application.lastActivityForAnyDevice = PrinterActivity.class;

        this.setupDefaults();
    }

    private void performPrinterAction(String actionName) {
        Method action = null;
        try {
            action = IPrinter.class.getMethod(actionName, (Class<?>[]) null);
        } catch (NoSuchMethodException e) {
            return;
        }

        IDevice device = null;
        try {
            if (application.allDevicesSelected()) {
                for (DeviceData deviceData : application.connectedDevicesData.values()) {
                    device = deviceData.device;
                    IPrinter printer = device.getPrinter();
                    action.invoke(printer, (Object[]) null);
                }
            } else {
                device = application.getDevice();
                IPrinter printer = device.getPrinter();
                action.invoke(printer, (Object[]) null);
            }
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
            ApiPrinterException ex = (ApiPrinterException) e.getTargetException();
            showStandardErrorMessageBox("\"" + action.getName() + "\" command failed", ex, device);
        }
    }

    private void setupDefaults() {
        application.initializeDeviceIndexButtons(this);

        Button btnResync = (Button) findViewById(R.id.btnResync);
        btnResync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPrinterAction("resync");
            }
        });
        Button btnGoToBlackMark = (Button) findViewById(R.id.btnGoToBlackMark);
        btnGoToBlackMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPrinterAction("goToBlack");
            }
        });
        Button btnCalibrate = (Button) findViewById(R.id.btnCalibrate);
        btnCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPrinterAction("calibrateBlackMark");
            }
        });
        Button btnFeedLabel = (Button) findViewById(R.id.btnFeedLabel);
        btnFeedLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPrinterAction("feedLabel");
            }
        });
        Button btnClearError = (Button) findViewById(R.id.btnClearError);
        btnClearError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPrinterAction("clearError");
            }
        });
        Button btnAbortError = (Button) findViewById(R.id.btnAbortError);
        btnAbortError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPrinterAction("abortError");
            }
        });
        Button btnAbortAllJobs = (Button) findViewById(R.id.btnAbortAllJobs);
        btnAbortAllJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPrinterAction("abortAllJobs");
            }
        });
        Button btnPrintSample = (Button) findViewById(R.id.btnPrintSample);
        btnPrintSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printSample();
            }
        });
        Button btnProperties = (Button) findViewById(R.id.btnProperties);
        btnProperties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProperties();
            }
        });

    }

    private void printSample() {
        printSampleIndex++;
        String barcode = "203540749";
        String batchId = "FOX17337";
        Bitmap bmp = generateQR("89-96-FOX17643");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
//            generateQR("89-96-FOX17643").
        byte[][] stringText = {batchId.getBytes(),byteArray};
        if (application.allDevicesSelected()) {
            for (DeviceData deviceData : application.connectedDevicesData.values()) {
                IDevice device = deviceData.device;
                IPrinter printer = device.getPrinter();
                try {
                    printer.print("BoldItalicUnderline NewLine LNT", 1, stringText);
                } catch (ApiPrinterException e) {
                    showStandardErrorMessageBox("Sample print failed", e, device);
                }
            }
        } else {
            IDevice device = application.getDevice();
            IPrinter printer = device.getPrinter();
            try {
                printer.print("BoldItalicUnderline NewLine LNT", 1, stringText);
            } catch (ApiPrinterException e) {
                showStandardErrorMessageBox("Sample print failed", e, device);
            }
        }

    }

    public Bitmap generateQR(String genID) {
        QRGEncoder qrgEncoder;

        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        qrgEncoder = new QRGEncoder(
                genID, null,
                QRGContents.Type.TEXT,
                smallerDimension);
        Bitmap bitmap = null;
        try {
            bitmap = qrgEncoder.encodeAsBitmap();


        } catch (WriterException e) {
            Log.e(this + "", e.toString());
        }
        return bitmap;
    }

    private void showProperties() {
        if (application.allDevicesSelected()) {
            showMessageBox("Impossible action",
                    "Setting printer parameters is not allowed for all connected devices at once. Please select one of the devices from the list before proceeding.",
                    "Ok", null, null, null, null);
            return;
        }

        Intent propertiesIntent = new Intent(this, PrinterPropertiesActivity.class);
        startActivity(propertiesIntent);
    }
}