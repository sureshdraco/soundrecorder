package com.maximcedaroff.soundrec;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;


public class RecActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	private ImageView recordButton;
	private TextView recordStatus;
	private boolean startRecording = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rec);
		
		toolbar = (Toolbar) findViewById(R.id.rec_toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.rec_title);
		Typeface myTypeface = Typeface.createFromAsset(getAssets(), "punk kid.ttf");
		toolbarTitle.setTypeface(myTypeface);
		toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		AdView adView = (AdView) findViewById(R.id.rec_adView);
		AdRequest adRequest = new AdRequest.Builder()
			.setRequestAgent("android_studio:ad_template").build();
		adView.loadAd(adRequest);
		recordButton = (ImageView) findViewById(R.id.btnRecord);
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				performRecord();
			}
		});
		recordStatus = (TextView) findViewById(R.id.recording_status_text);
	}
	
	private void performRecord() {
		onRecord(startRecording);
		startRecording = !startRecording;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (!startRecording) {
			performRecord();
		}
	}
	
	// Recording Start/Stop
	//TODO: recording pause
	private void onRecord(boolean start) {
		Intent intent = new Intent(this, RecordingService.class);
		if (start) {
			// start recording
			recordButton.setImageResource(R.drawable.btn_press);
			//mPauseButton.setVisibility(View.VISIBLE);
			Toast.makeText(this, R.string.toast_recording_start, Toast.LENGTH_SHORT).show();
			File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
			if (!folder.exists()) {
				//folder /SoundRecorder doesn't exist, create the folder
				folder.mkdir();
			}
			//start RecordingService
			startService(intent);
			//keep screen on while recording
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			
			recordStatus.setText(getString(R.string.record_in_progress) + ".");
		} else {
			//stop recording
			recordButton.setImageResource(R.drawable.btn_unpress);
			//mPauseButton.setVisibility(View.GONE);
			recordStatus.setText(getString(R.string.record_prompt));
			stopService(intent);
			//allow the screen to turn off again once recording is finished
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
}
