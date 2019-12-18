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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {


    private LinearLayout ll_root;
    private ScrollView sv_root;


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
            Log.v("what is",Integer.toString(msg.what));
            if (msg.what == 3){

                Log.v(TAG,"writing");
                mservice.write(testSendMas.getBytes());
                mservice.start();
            }
            if(msg.what == 6){
                String log = (String) msg.obj;

                creatText(log);

            }
            if(msg.what == 2){
                byte[] data = (byte[]) msg.obj;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                creatImg(bitmap);

//                img.setImageBitmap(bitmap);
            }
            if(msg.what == 7){
                sv_root.addView(ll_root);
                setContentView(sv_root);
            }

            return false;
        }
    });




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sv_root = new ScrollView(this);
        ll_root = new LinearLayout(this);
        ll_root.setOrientation(LinearLayout.VERTICAL);

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
//        mservice.stop();
//        img.setImageDrawable(this.getResources().getDrawable(R.drawable.raspberry_pi));

//        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
//
//        startActivity(intent);


        sv_root.addView(ll_root);
        setContentView(sv_root);

    }

    private void creatText(String str) {
        TextView textView = new TextView(this);
        textView.setPadding(10, 10, 10, 10);
        textView.setText(str);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 10, 30, 0);
        ll_root.addView(textView,params);
    }
    private void creatImg(Bitmap bitmap){
        ImageView image = new ImageView(this);
        image.setImageBitmap(bitmap);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,800);
        params.setMargins(30, 20, 30, 20);
        ll_root.addView(image,params);
    }


}
