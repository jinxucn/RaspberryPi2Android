package com.courseproject.raspberryconc;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String RaspberryAddress = "B8:27:EB:EA:08:F4";
    private static final String testSendMas = "pic";
    private static final String TAG = "MyActivity";
    private ImageView img;



    private BluetoothDevice raspberry = mBluetoothAdapter.getRemoteDevice(RaspberryAddress);
    private BluetoothChatService mservice;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what==3){

                Log.v(TAG,"writing");
                mservice.write(testSendMas.getBytes());
                mservice.start();
            }
            if(msg.what == 2){
                byte[] data = (byte[]) msg.obj;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                img.setImageBitmap(bitmap);

            }

            return false;
        }
    });




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = findViewById(R.id.img);
        mservice = new BluetoothChatService(null,mHandler);
    }
    public void turnOnBluetooth(View view){
        Toast.makeText(this,"pleas pair Raspberry",Toast.LENGTH_SHORT).show();
        this.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
    }


    public void takePicture(View view){
        mservice.connect(raspberry);
    }


    public void refresh(View view){
        mservice.stop();
        img.setImageDrawable(this.getResources().getDrawable(R.drawable.raspberry_pi));
    }

}
