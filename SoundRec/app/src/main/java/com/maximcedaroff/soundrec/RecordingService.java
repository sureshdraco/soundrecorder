package com.maximcedaroff.soundrec;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Daniel on 12/28/2014.
 */
public class RecordingService extends Service {
	
	private static final String LOG_TAG = "RecordingService";
	
	private String fileName = null;
	private String filePath = null;
	
	private MediaRecorder recorder = null;
	
	private DBHelper database;
	
	private long startingTimeMillis = 0;
	private long elapsedMillis = 0;
	private int elapsedSeconds = 0;
	private OnTimerChangedListener onTimerChangedListener = null;
	private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
	
	private Timer timer = null;
	private TimerTask incrementTimerTask = null;
	private File file;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public interface OnTimerChangedListener {
		void onTimerChanged(int seconds);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		database = new DBHelper(getApplicationContext());
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startRecording();
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		if (recorder != null) {
			stopRecording();
		}
		
		super.onDestroy();
	}
	
	public void startRecording() {
		setFileNameAndPath();
		
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setOutputFile(filePath);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		recorder.setAudioChannels(1);
		//high quality
		recorder.setAudioSamplingRate(44100);
		recorder.setAudioEncodingBitRate(192000);
		
		try {
			recorder.prepare();
			recorder.start();
			startingTimeMillis = System.currentTimeMillis();
			
			//startTimer();
			//startForeground(1, createNotification());
			
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}
	
	public void setFileNameAndPath() {
		int count = 0;
		
		do {
			count++;
			
			fileName = getString(R.string.default_file_name)
				+ "_" + (database.getCount() + count) + ".mp4";
			filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
			filePath += "/SoundRecorder/" + fileName;
			
			file = new File(filePath);
		} while (file.exists() && !file.isDirectory());
	}
	
	public void stopRecording() {
		recorder.stop();
		elapsedMillis = (System.currentTimeMillis() - startingTimeMillis);
		recorder.release();
		Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + filePath, Toast.LENGTH_LONG).show();
		
		//remove notification
		if (incrementTimerTask != null) {
			incrementTimerTask.cancel();
			incrementTimerTask = null;
		}
		
		recorder = null;
		
		try {
			database.addRecording(fileName, filePath, elapsedMillis, file.length() / 1024);
			
		} catch (Exception e) {
			Log.e(LOG_TAG, "exception", e);
		}
	}
	
	private void startTimer() {
		timer = new Timer();
		incrementTimerTask = new TimerTask() {
			@Override
			public void run() {
				elapsedSeconds++;
				if (onTimerChangedListener != null)
					onTimerChangedListener.onTimerChanged(elapsedSeconds);
				NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				mgr.notify(1, createNotification());
			}
		};
		timer.scheduleAtFixedRate(incrementTimerTask, 1000, 1000);
	}
	
	//TODO:
	private Notification createNotification() {
		NotificationCompat.Builder mBuilder =
			new NotificationCompat.Builder(getApplicationContext())
				.setSmallIcon(R.drawable.ic_mic_white_36dp)
				.setContentTitle(getString(R.string.notification_recording))
				.setContentText(mTimerFormat.format(elapsedSeconds * 1000))
				.setOngoing(true);
		
		mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
			new Intent[]{new Intent(getApplicationContext(), MainActivity.class)}, 0));
		
		return mBuilder.build();
	}
}
