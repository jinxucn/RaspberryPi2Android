package com.courseproject.raspberryconc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter blue = BluetoothAdapter.getDefaultAdapter();
    private final static int REQUEST_ENBLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private  UUID myuuid = UUID.fromString("a0a08a3b-750a-410f-a78d-c6f3426a7c81");
    private  UUID rauuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    private String servername = "myAndroid";
    private String NexusAddress = "A4:70:D6:B6:48:69";
    private String RaspberryAddress = "B8:27:EB:EA:08:F4";
    private String testSendMas = "hello raspberry";
    private static final String TAG = "MyActivity";
    private TextView tv1;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int arg1=msg.arg1;
            String info= (String) msg.obj;
            if (msg.what==1){
                tv1.setText(getString(R.string.tvRec));
                Log.v(TAG,"arg0");
            }
            if (arg1==0){

                tv1.setText(getString(R.string.tvRec));
                Log.v(TAG,"arg1");
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv1 = findViewById(R.id.tv1);


    }
    public void turnOnBluetooth(View view){
        Toast.makeText(this,"pleas pair Raspberry",Toast.LENGTH_SHORT).show();
        this.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));


    }


    public void sendMessage(View view){

//        Toast.makeText(this,"sendingMesage",Toast.LENGTH_SHORT).show();

        BluetoothDevice raspberry = mBluetoothAdapter.getRemoteDevice(RaspberryAddress);


        ConnectThread connect = new ConnectThread(raspberry);
        connect.run();
        Toast.makeText(this,raspberry.getName(),Toast.LENGTH_SHORT).show();
    }


    public void receiveMessage(View view){

        AcceptThread accept = new AcceptThread();

        accept.run();

    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(servername, rauuid);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();

                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    Log.v(TAG,"connected!!");
                    ConnectedThread new ConnectedThread(socket);
                    try{
                        mmServerSocket.close();
                        Log.v(TAG,"socket closed");
                    } catch (IOException e){}
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(rauuid);
                Log.v(TAG,String.valueOf(tmp.isConnected()));
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            ConnectedThread connected = new ConnectedThread(mmSocket);
            connected.write(testSendMas.getBytes());
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {}
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            Log.v(TAG,"running");
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void sendMsg(BluetoothSocket socket , String msg){
        private final OutputStream mOutStream;
        try {
            mOutStream = socket.getOutputStream();
        } catch (IOException e) { }
        try {
            mOutStream.write(msg.getBytes());
        } catch (IOException e) { }
    }


    private void recMsg(BluetoothSocket socket ){


    }
    private class receiveMsgThread extends Thread {
        public receiveMesThread(BluetoothSocket socket){

        }

    }

}
