package com.maximcedaroff.soundrec;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
		Typeface myTypeface = Typeface.createFromAsset(getAssets(), "punk kid.ttf");
		toolbarTitle.setTypeface(myTypeface);
		
		
		AdView adView = (AdView) findViewById(R.id.main_adView);
		AdRequest adRequest = new AdRequest.Builder()
			.setRequestAgent("android_studio:ad_template").build();
		adView.loadAd(adRequest);
		toolbar.findViewById(R.id.action_list).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, LisstActivity.class));
			}
		});
		
		toolbar.findViewById(R.id.action_record).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, RecActivity.class));
			}
		});
	}
}
