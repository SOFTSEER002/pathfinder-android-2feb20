package com.fox.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import avd.api.core.IDevice;
import avd.api.core.exceptions.ApiException;


public class SampleAppActivity extends Activity {

	private class ManageableAlertDialog
	{
		public AlertDialog.Builder dialogBuilder;
		public AlertDialog dialog;
		public IDevice     device;
	}

	private class PostedAlertDialog
	{
		public String title;
		public String message;
		public String positiveButtonText;
		public DialogInterface.OnClickListener positiveButtonListener;
		public String negativeButtonText;
		public DialogInterface.OnClickListener negativeButtonListener;
		public String neutralButtonText;
		public DialogInterface.OnClickListener neutralButtonListener;
		public IDevice device;
	}

	private HashMap<IDevice, ArrayList<ManageableAlertDialog>> openAlerts = new HashMap<IDevice, ArrayList<ManageableAlertDialog> >();
	private static ArrayList<PostedAlertDialog> postedAlertsToShow   = new ArrayList<PostedAlertDialog>();
	private static ArrayList<ManageableAlertDialog> openAlertsToShow = new ArrayList<ManageableAlertDialog>();
	private boolean delayAlertShowing = false;

	public IDevice findDeviceAssociatedWithAlert(DialogInterface dialog) {
		for (Map.Entry<IDevice, ArrayList<ManageableAlertDialog>> deviceEntry : openAlerts.entrySet()) {

			for (ManageableAlertDialog alertDialog : deviceEntry.getValue())
				if (alertDialog.dialog == dialog)
					return deviceEntry.getKey();
		}

		return null;
	}

	public void createAndShowMessageBox(final String title, final String message,
										final String positiveButtonText, final DialogInterface.OnClickListener positiveButtonListener,
										final String negativeButtonText, final DialogInterface.OnClickListener negativeButtonListener,
										final String neutralButtonText, final DialogInterface.OnClickListener neutralButtonListener,
										final IDevice device)
	{
		final String dialogMessage = (device != null) ? message + '\n' + "Device is " + device.getSerial() : message;

		if (delayAlertShowing) {
			PostedAlertDialog postedDialog = new PostedAlertDialog();
			postedDialog.title = title;
			postedDialog.message = message;
			postedDialog.positiveButtonText = positiveButtonText;
			postedDialog.positiveButtonListener = positiveButtonListener;
			postedDialog.negativeButtonText = negativeButtonText;
			postedDialog.negativeButtonListener = negativeButtonListener;
			postedDialog.neutralButtonText = neutralButtonText;
			postedDialog.neutralButtonListener = neutralButtonListener;
			postedDialog.device = device;
			postedAlertsToShow.add(postedDialog);

			return;
		}

		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(title)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setMessage(dialogMessage)
				.setPositiveButton(positiveButtonText, positiveButtonListener);
		if (negativeButtonText != null)
			dialogBuilder.setNegativeButton(negativeButtonText, negativeButtonListener);
		if (neutralButtonText != null)
			dialogBuilder.setNeutralButton(neutralButtonText, neutralButtonListener);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!openAlerts.keySet().contains(device))
					openAlerts.put(device, new ArrayList<ManageableAlertDialog>());

				ManageableAlertDialog newDialog = new ManageableAlertDialog();

				newDialog.dialogBuilder = dialogBuilder;
				newDialog.device = device;
				newDialog.dialog = newDialog.dialogBuilder.create();
				newDialog.dialog.setCanceledOnTouchOutside(false);
				newDialog.dialog.show();

				openAlerts.get(device).add(newDialog);
			}
		});
	}

	public void showMessageBox(String title, String message,
							   String positiveButtonText, DialogInterface.OnClickListener positiveButtonListener,
							   String negativeButtonText, DialogInterface.OnClickListener negativeButtonListener,
							   IDevice device)
	{
		createAndShowMessageBox(title, message,
				positiveButtonText, positiveButtonListener,
				negativeButtonText, negativeButtonListener,
				null, null, device);
	}


	public void showErrorMessageBox(String title, ApiException e,
									String positiveButtonText, DialogInterface.OnClickListener positiveButtonListener,
									String negativeButtonText, DialogInterface.OnClickListener negativeButtonListener,
									IDevice device)
	{
		showMessageBox(title, String.format("Error code is %d.", e.getErrorCode()), positiveButtonText, positiveButtonListener, negativeButtonText, negativeButtonListener, device);
	}

	public void showStandardErrorMessageBox(String title, ApiException e, IDevice device)
	{
		showErrorMessageBox(title, e, "Abort", ((SampleApplication) getApplication()).abortPrinterError, "Ignore", null, device);
	}

	public void showNonAbortableErrorMessageBox(String title, ApiException e, IDevice device)
	{
		showErrorMessageBox(title, e, "Ok", null, null, null, device);
	}

	public void dismissAllAlerts(boolean isDismissalTemporary)
	{
		for (IDevice device : openAlerts.keySet())
			dismissAllAlertsForDevice(device, isDismissalTemporary);

		// All open alerts are either transferred to the list of open alerts waiting to be shown
		// or destroyed altogether. No need to contain the mapping anymore.
		openAlerts.clear();
	}

	public void dismissAllAlertsForDevice(IDevice device, boolean isDismissalTemporary)
	{
		ArrayList<ManageableAlertDialog> openAlertsForDevice = openAlerts.get(device);
		if (isDismissalTemporary)
		{
			// If no alerts are open, we don't need to do anything else.
			if ((openAlertsForDevice == null) || openAlertsForDevice.isEmpty())
				return;

			for (ManageableAlertDialog manageableDialog : openAlertsForDevice)
			{
				if ((manageableDialog.dialog != null) && manageableDialog.dialog.isShowing())
				{
					manageableDialog.dialog.cancel();
					manageableDialog.dialog = null;
					openAlertsToShow.add(manageableDialog);
				}
			}
		}
		else
		{
			// Removing all open alerts waiting to be shown associated with the device.
			for (Iterator<ManageableAlertDialog> iterator = openAlertsToShow.iterator(); iterator.hasNext(); )
			{
				ManageableAlertDialog alertDialog = iterator.next();
				if (alertDialog.device == device)
					iterator.remove();
			}

			// Removing all posted alerts waiting to be shown associated with the device.
			for (Iterator<PostedAlertDialog> iterator = postedAlertsToShow.iterator(); iterator.hasNext(); )
			{
				PostedAlertDialog alertDialog = iterator.next();
				if (alertDialog.device == device)
					iterator.remove();
			}

			// If no alerts are open, we don't need to do anything else.
			if ((openAlertsForDevice == null) || openAlertsForDevice.isEmpty())
				return;

			for (ManageableAlertDialog manageableDialog : openAlertsForDevice)
			{
				if ((manageableDialog.dialog != null) && manageableDialog.dialog.isShowing())
					manageableDialog.dialog.cancel();
				manageableDialog.dialog = null;
				manageableDialog.device = null;
			}
		}
	}

	@Override
	public void onBackPressed() {
		DeviceService.removeForegroundActivity(this);
		delayAlertShowing = true;
		super.onBackPressed();
	}

	@Override
	public void onStop() {
		dismissAllAlerts(true);

		delayAlertShowing = true;
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		delayAlertShowing = false;
		DeviceService.setForegroundActivity(this);
		SampleApplication application = ((SampleApplication) getApplication());
		application.currentActivity = this;
		application.isButtonPressAllowed = true;
		sendBroadcast(new Intent(SampleApplication.INTENT_ACTION_UPDATE_SCREEN_CONTROLS));
		// Demonstrate previously closed alerts
		for (ManageableAlertDialog manageableDialog : openAlertsToShow)
		{
			manageableDialog.dialog = manageableDialog.dialogBuilder.create();
			manageableDialog.dialog.setCanceledOnTouchOutside(false);
			manageableDialog.dialog.show();
			if (!openAlerts.keySet().contains(manageableDialog.device))
				openAlerts.put(manageableDialog.device, new ArrayList<ManageableAlertDialog>());
			openAlerts.get(manageableDialog.device).add(manageableDialog);
		}
		openAlertsToShow.clear();

		// Demonstrate posted alerts
		for (PostedAlertDialog postedAlert : postedAlertsToShow)
		{
			createAndShowMessageBox(postedAlert.title, postedAlert.message,
					postedAlert.positiveButtonText, postedAlert.positiveButtonListener,
					postedAlert.negativeButtonText, postedAlert.negativeButtonListener,
					postedAlert.neutralButtonText, postedAlert.neutralButtonListener,
					postedAlert.device);
		}
		postedAlertsToShow.clear();
	}

}
