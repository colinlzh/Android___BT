package com.example.mytest2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.TextView;

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
	volatile boolean stopWorker;
	
	private TextView tv1;
//	public Activity activity;
	private MainActivity mainActivity;
	
	private static BluetoothReceiver instance;
//	public static BluetoothReceiver getInstance()
//	{
//		if(instance == null)
//		{
//			instance = new BluetoothReceiver();
//		}
//		return instance;
//	}
	
	public BluetoothReceiver(MainActivity main_activity)
	{		
		mainActivity = main_activity;
		findBT();
	}
	
	void findBT()
    {
		tv1 = mainActivity.tv1;
		tv1.setText("BluetoothReceiver");
		
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
        	tv1.setText("No bluetooth adapter available");
        	return;
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mainActivity.startActivityForResult(enableBluetooth, 0);
            tv1.setText("Try to enable Bluetooth");
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                //if(device.getName().equals("HC-06") && device.getAddress().equals("00:14:01:06:16:52"))  //20:13:10:30:03:93 30:14:07:31:37:68 
            	if(device.getName().equals("HC-06"))
                {
                    mmDevice = device;
                    tv1.setText("Bind the Bluetooth device");
                    break;
                }
            }
        }
        tv1.setText("Bluetooth Device Found");
    }

//	public static void OpenBT() throws IOException
//	{
//		if(instance!=null)
//		{
//			instance.openBT();
//		}
//		
//	}
	
    void openBT() throws IOException
    {
    	
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID

        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);     
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        System.out.print("openBT");
        beginListenForData();

//        tv1.setText("Bluetooth Opened");
    }
    
    void beginListenForData()
    {
        //final Handler handler = new Handler(); 
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {                
               while(!Thread.currentThread().isInterrupted() && !stopWorker)
               {
                    try 
                    {
                        int bytesAvailable = mmInputStream.available(); 
                       // System.out.print("bytesAvailable");
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                	byte[] encodedBytes = new byte[readBufferPosition];
                                	System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                	final String data = new String(encodedBytes, "US-ASCII");
                                	readBufferPosition = 0;
                                	
                                	serialData=data;
                                	System.out.println(serialData.toString());
//                                	tv1.setText(serialData.toString());
//                                    handler.post(new Runnable()
//                                    {
//                                        public void run()
//                                        {
//                                        	//serialDataView.setText(data);
//                                        }
//                                    });
                                	
                                	
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                      System.out.println(serialData);
//                      tv1.setText(serialData.toString());
                    }
                    
                    catch (IOException ex) 
                    {
                        stopWorker = true;
                    }
               }
            }
        });

        workerThread.start();
    }

    //not used at the moment
    void sendData() throws IOException
    {
        String msg = " ";// myTextbox.getText().toString();
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
//        tv1.setText("Bluetooth data sent");
    }

    public static void CloseBT() throws IOException
    {
    	if(instance != null)
    	{
    		instance.closeBT();
    	}
    }
    
    public void closeBT() throws IOException
    {
        stopWorker = true;
        if(mmOutputStream != null)
        	mmOutputStream.close();
        if(mmInputStream!=null)
        	mmInputStream.close();
        if(mmSocket!=null)
        	mmSocket.close();
        
//        tv1.setText("Bluetooth Closed");
    }
}