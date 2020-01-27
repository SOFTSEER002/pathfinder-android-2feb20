package com.fox.app.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import com.avery.sampleapp.R;


public class CustomProgressBar {
	public Dialog popDialog;
	private Context context;

	public CustomProgressBar(Context context) {

		this.context = context;

	}

	/*
	 * This method display a message or alert for any functionality
	 */
//	show progress bar method
	public void showProgress() {
		popDialog = new Dialog(context);
		popDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		popDialog.setContentView(R.layout.progressbar);
		popDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		popDialog.setCancelable(false);
		popDialog.show();
	}

//	Hide progress bar method
	public void hideProgress() {

		if (popDialog != null) {
			popDialog.dismiss();

		}
	}

}
