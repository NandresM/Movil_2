package co.edu.uniminuto.actividad_3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private Activity activity;
    // Version del Android
    private TextView versionAndroid;
    private int versionSDK;
    //Batería
    private ProgressBar pbLevelBaterry;
    private TextView tvLevelBaterry;
    private IntentFilter baterryFilter;
    //conexion
    private TextView tvConexion;
    private ConnectivityManager conexion;
    //Flashlight
    private CameraManager cameraManager;
    private String cameraId;
    private Button onFlash;
    private Button offFlash;
    //file
    private EditText nameFile;
    //private CLFile clFile

    private TextView tvState;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initObjects();
        onFlash.setOnClickListener(this::onLigth);
        offFlash.setOnClickListener(this::offLigth);
        baterryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broReceiver, baterryFilter);





    }


    @Override
    protected void onResume() {
        super.onResume();
        //versionAndroid
        String versionSO = Build.VERSION.RELEASE;
        versionSDK = Build.VERSION.SDK_INT;
        versionAndroid.setText("Version SO: "+versionSO+"/ SDK: "+versionSDK);
        //llamada metodo conexion

        checkConnection();

    }

    private void initObjects(){
        this.context = getApplicationContext();
        this.activity = this;
        this.versionAndroid = findViewById(R.id.tvVersionAndroid);
        this.pbLevelBaterry = findViewById(R.id.pbLevelBattery);
        this.tvLevelBaterry = findViewById(R.id.tvLevelBatteryLB);
        this.tvConexion = findViewById(R.id.tvState);
        this.nameFile = findViewById(R.id.etNameFile);
        this.onFlash = findViewById(R.id.btnOn);
        this.offFlash = findViewById(R.id.btnOff);
        //definir los bluetooh

    }


    //conexion
    private void checkConnection(){
        try {
            conexion = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (conexion != null) {
                NetworkInfo networkInfo = conexion.getActiveNetworkInfo();
                boolean stateNet = networkInfo != null && networkInfo.isConnectedOrConnecting();
                if (stateNet) {
                    tvState.setText("State ON");
;
                }else {
                    tvState.setText("State OFF");

                }
                }
            }catch (Exception e){
                 Log.i("CONEXION", e.getMessage());
        }

    }
    //Encender luz
    private void onLigth(View view){
        try {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);

        }catch ( CameraAccessException e){
            Log.i("LINTERNA", e.getMessage());
        }
    }

    //Apagar luiz
    private void offLigth(View view){
        try {

            cameraManager.setTorchMode(cameraId, false);
        }catch ( CameraAccessException e){
            Log.i("LINTERNA", e.getMessage());

        }
    }
    //Bateria
    BroadcastReceiver broReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int levelBaterry = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            pbLevelBaterry.setProgress(levelBaterry);
            tvLevelBaterry.setText("Nivel de batería: "+String.valueOf(levelBaterry)+"%");
        }
    };







}