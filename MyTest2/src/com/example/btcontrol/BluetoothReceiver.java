package com.example.btcontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothReceiver {
	public String serialData = "empty";
	BluetoothAdapter mBluetoothAdapter;
	BluetoothSocket mmSocket;
	BluetoothDevice mmDevice;
	OutputStream mmOutputStream;
	InputStream mmInputStream;
	Thread workerThread;
	byte[] readBuffer;
	int readBufferPosition;
	int counter;
	volatile boolean workerStopped;
	String send_msg = "hello";
	Timer timer;
	TimerTask task;

	private TextView tv1;
	private MainActivity mainActivity;

	public BluetoothReceiver(MainActivity main_activity) {
		mainActivity = main_activity;
	}

	void findBT() {
		tv1 = mainActivity.tv1;
		tv1.setText("BluetoothReceiver");

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(mainActivity, "No bluetooth adapter available",
					Toast.LENGTH_SHORT).show();
			return;
		}

		// try to enable BT of the device
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBluetooth = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			mainActivity.startActivityForResult(enableBluetooth, 0);
			Toast.makeText(mainActivity, "Try to enable Bluetooth",
					Toast.LENGTH_SHORT).show();
		}

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				// if(device.getName().equals("HC-06") &&
				// device.getAddress().equals("00:14:01:06:16:52"))
				// //20:13:10:30:03:93 30:14:07:31:37:68
				if (device.getName().equals("HC-06")) {
					mmDevice = device;
					Toast.makeText(mainActivity, "Bind the Bluetooth device",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}
		Toast.makeText(mainActivity, "Bluetooth Device Found",
				Toast.LENGTH_SHORT).show();
	}

	void openBT() throws IOException {
		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard
																				// SerialPortService
																				// ID

		try {
			mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
			mmSocket.connect();
			mmOutputStream = mmSocket.getOutputStream();
			mmInputStream = mmSocket.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
			

		Toast.makeText(mainActivity, "Bluetooth Opened", Toast.LENGTH_SHORT)
				.show();
	}

	void listenForData() {
		final Handler handler = new Handler();
		final byte delimiter = 10; // This is the ASCII code for a newline
									// character

		workerStopped = false;
		readBufferPosition = 0;
		readBuffer = new byte[1024];
		workerThread = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted()
						&& !workerStopped) {
					try {
						int bytesAvailable = mmInputStream.available();
						if (bytesAvailable > 0) {
							byte[] packetBytes = new byte[bytesAvailable];
							mmInputStream.read(packetBytes);
							for (int i = 0; i < bytesAvailable; i++) {
								byte b = packetBytes[i];
								if (b == delimiter) {
									byte[] encodedBytes = new byte[readBufferPosition];
									System.arraycopy(readBuffer, 0,
											encodedBytes, 0,
											encodedBytes.length);
									final String data = new String(
											encodedBytes, "US-ASCII");
									readBufferPosition = 0;

									serialData = data;
									System.out.println(serialData.toString());

									handler.post(new Runnable() {
										public void run() {
											tv1.setText(serialData.toString());
										}
									});
								} else {
									readBuffer[readBufferPosition++] = b;
								}
							}
						}
					} catch (IOException ex) {
						workerStopped = true;
					}
				}
			}
		});

		workerThread.start();
	}

	void timelySendData() {
		if(timer == null)
			timer = new Timer(true);	
		
		if(task == null)
		{
			task = new TimerTask() {
				Handler handler = new Handler();
	
				public void run() {
					try {
						sendData();
	
						handler.post(new Runnable() {
							public void run() {
								tv1.setText("Bluetooth data sent: " + send_msg);
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
		}
		
		if(timer != null && task != null)
			timer.schedule(task, 1000, 1000);
	}

	

	// not used at the moment
	void sendData() throws IOException {
		mmOutputStream.write(send_msg.getBytes());
	}

	public void closeBT() throws IOException {
		
		workerStopped = true;
		
		if(timer != null){
			timer.cancel();
			timer = null;
		}
		if(task != null){
			task.cancel();	
			task = null;
		}
		
		if (mmOutputStream != null)
			mmOutputStream.close();
		if (mmInputStream != null)
			mmInputStream.close();
		if (mmSocket != null)
			mmSocket.close();
//		if (mBluetoothAdapter != null)
//			mBluetoothAdapter.disable();

		Toast.makeText(mainActivity, "Bluetooth Closed", Toast.LENGTH_SHORT)
				.show();
	}
}