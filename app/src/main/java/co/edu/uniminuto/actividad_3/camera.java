package co.edu.uniminuto.actividad_3;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class camera extends AppCompatActivity {


    private TextView tvCamara;
    private ImageView fotoCamara;
    private Button tomarFt;
    private Button guardarFt;
    private PreviewView PrviaVista;
    private ImageCapture captureImage;
    private ExecutorService ejecutarCamara;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initObjects();
        tomarFt.setOnClickListener(this::tomarFoto);
        guardarFt.setOnClickListener(this::guardarFoto);
        ejecutarCamara = Executors.newSingleThreadExecutor();



    }


    //abrir la camara
    private void openCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                vistaPrevia(cameraProvider);
            } catch (Exception e) {
                Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));

    }

    //visualizarce
    private void vistaPrevia(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        captureImage = new ImageCapture.Builder().build();

        preview.setSurfaceProvider(PrviaVista.getSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, captureImage);
    }

    //Tomar la foto
    private void tomarFoto(View view){
        //fotoCamara.setImageResource(R.drawable.camara);
    }

    //Guardar la foto
    private void guardarFoto(View view){
       // fotoCamara.setImageResource(R.drawable.camara);
    }


    public void initObjects(){
        this.tvCamara = findViewById(R.id.tvCamara);
        this.fotoCamara = findViewById(R.id.fotoCamara);
        this.tomarFt = findViewById(R.id.tomarFt);
        this.guardarFt = findViewById(R.id.guardarFt);

    }
}