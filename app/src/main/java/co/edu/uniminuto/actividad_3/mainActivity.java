package co.edu.uniminuto.actividad_3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class mainActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 1001;
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
    private Button blutu;
    private Button cam;



   private Button btnSaveFile;

    private String studentName;
   private int levelBaterry;
    private String androidVersion;



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


    openMetodo();

    }

    private void openMetodo(){
        initObjects();
        onFlash.setOnClickListener(this::onLigth);
        offFlash.setOnClickListener(this::offLigth);
        baterryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broReceiver, baterryFilter);
        blutu.setOnClickListener(this::abrirBt);
        cam.setOnClickListener(this::abrirCam);
        btnSaveFile.setOnClickListener(this::createFile);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createFile(findViewById(android.R.id.content));
            } else {
                Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //versionAndroid
        String versionSO = Build.VERSION.RELEASE;
        androidVersion = versionSO;
        versionSDK = Build.VERSION.SDK_INT;
        versionAndroid.setText("Version SO: "+versionSO+"/ SDK: "+versionSDK);
        //llamada metodo conexion

        checkConnection();
        //validar permisos almacenimiento


    }

    private void initObjects(){
        this.context = getApplicationContext();
        this.activity = this;
        this.versionAndroid = findViewById(R.id.tvVersionAndroid);
        this.pbLevelBaterry = findViewById(R.id.pbLevelBattery);
        this.tvLevelBaterry = findViewById(R.id.tvLevelBatteryLB);
        this.nameFile = findViewById(R.id.nameFile);
        this.onFlash = findViewById(R.id.btnOn);
        this.offFlash = findViewById(R.id.btnOff);
        this.tvState = findViewById(R.id.tvState);
        //definir los bluetooh
        this.blutu = findViewById(R.id.bluetooth);
        this.cam = findViewById(R.id.camara);
        this.btnSaveFile = findViewById(R.id.btnSaveFile);



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
             levelBaterry = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

            pbLevelBaterry.setProgress(levelBaterry);
            tvLevelBaterry.setText("Nivel de batería: "+String.valueOf(levelBaterry)+"%");
        }
    };

    //Crear .txt

    public void crearArchivoEnDescargas(View view) {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
            return;
        }

        createFile(view);
    }



    public void createFile(View view) {
        String fileName = nameFile.getText().toString().trim();
        if (fileName.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa un nombre para el archivo.", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder fileContentBuilder = new StringBuilder();
        fileContentBuilder.append("Nombres estudiantes: ").append("Andres Mora, Alejandra Sarmiento").append("\n")
                .append("Nivel de batería: ").append(levelBaterry).append("%\n")
                .append("Versión de Android: ").append(androidVersion);

        String fileContent = fileContentBuilder.toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createFileWithMediaStore(fileName, fileContent);
        } else {

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void createFileWithMediaStore(String fileName, String content) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + ".txt");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver resolver = getContentResolver();
        Uri uri = null;

        try {
            uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                    if (outputStream != null) {
                        outputStream.write(content.getBytes());
                        Toast.makeText(this, "Archivo creado en Descargas: " + fileName + ".txt",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al crear el archivo: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }





    public void abrirBt(View view) {
        Intent intent = new Intent(this, bluetooth.class);
        startActivity(intent);

    }

    public void abrirCam(View view) {
        Intent intent = new Intent(this, camera.class);
        startActivity(intent);
    }





}