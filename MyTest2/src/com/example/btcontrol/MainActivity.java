package com.example.btcontrol;

import java.io.IOException;

import com.example.mytest2.R;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView tv1;
	Button stopBtn, restartBtn, exitBtn;
	BluetoothReceiver btActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tv1 = (TextView) findViewById(R.id.textView1);
		stopBtn = (Button) findViewById(R.id.stopBtn);
		restartBtn = (Button) findViewById(R.id.restartBtn);
		exitBtn = (Button) findViewById(R.id.exitBtn);
		
		btActivity = new BluetoothReceiver(this);

		stopBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				System.out.println("try stopBT()");
				try {
					btActivity.stopBT();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		exitBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				System.out.println("try to close all!");
				try {
					btActivity.stopBT();
				} catch (IOException e) {
					e.printStackTrace();
				}
				MainActivity.this.finish();
			}
		});
		
		restartBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// should not execute heavy task in main UI thread?
				Handler h1 = new Handler();
				h1.post(new Runnable() {
					@Override
					public void run() {
						BTHandler();						
					}});
			}
		});

		BTHandler();		
	}
	
	public void BTHandler()
	{
		try {
			btActivity.findBT();
			btActivity.openBT();
			btActivity.listenForData();
			btActivity.timelySendData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
