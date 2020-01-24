package com.fox.app.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import com.avery.sampleapp.R;
import com.fox.app.Activities.MainActivity;
import com.fox.app.SampleApplication;

public class DeviceService extends Service {
	
	private static final int NOTIFICATION_ID = 1;
	
	private static final String ACTION_SET_FOREGROUND_ACTIVITY = "avd.api.action_set_foreground";
	
	private static final String BUNDLE_KEY_FOREGROUND_ACTIVITY_CLASS = "avd.api.foreground_activity_class";
	
	private static final Logger logger = LoggerFactory.getLogger(DeviceService.class.getSimpleName());
	
	public static final String WAKE_LOCK_NAME = "avd.api.sampleapp.wakelock";
	
	private PowerManager.WakeLock wakeLock = null;
	
	public static void setForegroundActivity(Activity activity){
		Intent intent = new Intent(activity, DeviceService.class);
		intent.setAction(ACTION_SET_FOREGROUND_ACTIVITY);
		intent.putExtra(BUNDLE_KEY_FOREGROUND_ACTIVITY_CLASS, activity.getClass());
		activity.startService(intent);
	}
	
	public static void removeForegroundActivity(Activity activity){
		Intent intent = new Intent(activity, DeviceService.class);
		intent.setAction(ACTION_SET_FOREGROUND_ACTIVITY);
		activity.startService(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(wakeLock != null && wakeLock.isHeld()){
			wakeLock.release();
			logger.debug("Release wake lock");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Class <? extends Activity> clazz = MainActivity.class;
		
		//No connection. Received intent by mistake 
		if(((SampleApplication)getApplication()).getDevice() == null ){
			stopSelf();
			return START_NOT_STICKY;
		}
		
		if(intent.getAction() == ACTION_SET_FOREGROUND_ACTIVITY){
			
			if(intent.hasExtra(BUNDLE_KEY_FOREGROUND_ACTIVITY_CLASS)){
				clazz = (Class <? extends Activity>) intent.getSerializableExtra(BUNDLE_KEY_FOREGROUND_ACTIVITY_CLASS);	
			}
		} 
		
		if(wakeLock == null){
			
			 logger.debug("Obtain wake lock for DeviceService");	
			
			 PowerManager mgr = (PowerManager)getSystemService(POWER_SERVICE);
			 wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME);
			 wakeLock.acquire();
		}
		
		registerForegorund(clazz);
	
		return START_REDELIVER_INTENT;
	}
	Intent intent;

	private void registerForegorund(Class<? extends Activity> foregroundActivityClass) {

		intent = new Intent(this, foregroundActivityClass);

		startMyOwnForeground();
	}

	@TargetApi(Build.VERSION_CODES.O)
	private void startMyOwnForeground(){
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
		String NOTIFICATION_CHANNEL_ID = "DeviceService";
		String channelName = "bgchannel";
		NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
		chan.setLightColor(Color.BLUE);
		chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		assert manager != null;
		manager.createNotificationChannel(chan);


		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
		Notification notification = notificationBuilder
				.setOngoing(true)
				.setSmallIcon(R.drawable.sample_app_active)
				.setContentTitle("Pathfinder 6140")
				.setContentText("Active")
				.setContentIntent(pi)
				.setPriority(NotificationManager.IMPORTANCE_DEFAULT)
				.setCategory(Notification.CATEGORY_SERVICE)
				.build();
		startForeground(1, notification);
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	

}
