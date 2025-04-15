package co.edu.uniminuto.actividad_3;

import android.Manifest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.icu.text.SimpleDateFormat;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;

import androidx.camera.core.Camera;
import java.io.File;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class camera extends AppCompatActivity {
    private TextView tvCamara;
    private Button tomarFt;
    private Button guardarFt;
    public PreviewView fotoCamara;
    private ImageCapture captureImage;
    private ExecutorService ejecutarCamara;
    private ImageView previewImageView;
    private Uri lastCapturedImageUri; // Para guardar la URI de la imagen capturada
    private File photoFile; // Guardar referencia al archivo temporal

    private static final String TAG = "CameraActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar objetos UI
        initObjects();

        // Configuración inicial de botones
        tomarFt.setEnabled(false);
        guardarFt.setEnabled(false); // Inicialmente deshabilitado
        guardarFt.setVisibility(View.GONE); // Oculto hasta que se tome una foto

        // Configurar listeners
        tomarFt.setOnClickListener(this::tomarFoto);
        guardarFt.setOnClickListener(this::guardarFoto);

        // Solicitar permisos inmediatamente al iniciar la actividad
        requestCameraPermissions();
    }

    // Método para solicitar permisos de forma explícita
    private void requestCameraPermissions() {
        if (allPermissionsGranted()) {
            Log.d(TAG, "Todos los permisos concedidos, iniciando cámara");
            setupCameraAndButtons();
        } else {
            Log.d(TAG, "Solicitando permisos de cámara");
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    // Nuevo método para configurar la cámara y habilitar botones una vez que hay permisos
    private void setupCameraAndButtons() {
        openCamera();
        tomarFt.setEnabled(true);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permiso no concedido: " + permission);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Verificar si todos los permisos fueron concedidos
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Log.d(TAG, "Permisos concedidos por el usuario, iniciando cámara");
                setupCameraAndButtons();
            } else {
                Log.d(TAG, "Permisos no concedidos por el usuario");
                Toast.makeText(this, "Se requieren permisos para usar la cámara", Toast.LENGTH_LONG).show();

                // Mostrar un diálogo explicando por qué se necesitan los permisos
                new AlertDialog.Builder(this)
                        .setTitle("Permisos necesarios")
                        .setMessage("Esta aplicación necesita acceso a la cámara y almacenamiento para funcionar correctamente.")
                        .setPositiveButton("Solicitar de nuevo", (dialog, which) -> requestCameraPermissions())
                        .setNegativeButton("Cancelar", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }
        }
    }

    // Abrir la camara
    private void openCamera() {
        ejecutarCamara = Executors.newSingleThreadExecutor();

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                vistaPrevia(cameraProvider);
            } catch (Exception e) {
                Log.e(TAG, "Error al abrir la cámara: " + e.getMessage());
                Toast.makeText(this, "Error al abrir la cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Configurar la vista previa
    private void vistaPrevia(@NonNull ProcessCameraProvider cameraProvider) {
        try {
            // Limpiar vinculaciones anteriores
            cameraProvider.unbindAll();

            // Configurar componentes de la cámara con mejor calidad
            Preview preview = new Preview.Builder().build();

            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();

            // Mejorar la configuración para captura de imagen con mayor calidad
            captureImage = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // Maximizar calidad en lugar de latencia
                    .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()) // Ajustar rotación
                    .setFlashMode(ImageCapture.FLASH_MODE_AUTO) // Flash automático
                    .build();

            // Establecer proveedor de superficie y vincular al ciclo de vida
            preview.setSurfaceProvider(fotoCamara.getSurfaceProvider());

            // Vincular los casos de uso a la cámara
            Camera camera = cameraProvider.bindToLifecycle(
                    this, // LifecycleOwner
                    cameraSelector,
                    preview,
                    captureImage);

            // Habilitar zoom y otras características si están disponibles
            if (camera.getCameraInfo().hasFlashUnit()) {
                // Podrías añadir un botón para controlar el flash aquí
            }

            Log.d(TAG, "Vista previa de cámara configurada correctamente");

        } catch (Exception e) {
            Log.e(TAG, "Error en vista previa: " + e.getMessage());
            Toast.makeText(this, "Error en la vista previa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Tomar la foto - ahora solo la guarda en memoria para previsualizar
    private void tomarFoto(View view) {
        if (captureImage == null) {
            Log.e(TAG, "Error: captureImage es null");
            Toast.makeText(this, "Error: La cámara no está lista", Toast.LENGTH_SHORT).show();
            return;
        }

        // Desactivar botón temporalmente para evitar múltiples capturas
        tomarFt.setEnabled(false);

        // Crear archivo temporal para la imagen
        photoFile = createTempFile();
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Tomar la foto
        captureImage.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Guardar referencia de la imagen
                        lastCapturedImageUri = Uri.fromFile(photoFile);

                        // Mostrar imagen en vista previa
                        mostrarVistaPrevia(photoFile);

                        // Cambiar visibilidad de botones - estilo Instagram
                        tomarFt.setVisibility(View.GONE);
                        guardarFt.setVisibility(View.VISIBLE);
                        guardarFt.setEnabled(true);

                        Log.d(TAG, "Foto tomada correctamente: " + photoFile.getAbsolutePath());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // Error al guardar la foto
                        Log.e(TAG, "Error al tomar la foto: " + exception.getMessage(), exception);
                        Toast.makeText(camera.this, "Error al tomar la foto: " +
                                exception.getMessage(), Toast.LENGTH_SHORT).show();
                        tomarFt.setEnabled(true); // Re-habilitar botón
                    }
                });
    }

    // Mostrar la imagen capturada en vista previa
    private void mostrarVistaPrevia(File photoFile) {
        if (previewImageView != null) {
            // Optimizar carga de imagen usando escala
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Escala la imagen reduciendo a 1/2 el tamaño

            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);

            // Rotar bitmap si es necesario
            bitmap = rotarImagenSiEsNecesario(bitmap, photoFile.getAbsolutePath());

            previewImageView.setImageBitmap(bitmap);
            previewImageView.setVisibility(View.VISIBLE);

            // Ocultar la vista previa de la cámara
            fotoCamara.setVisibility(View.GONE);
        }
    }

    // Rotar imagen según su EXIF orientation
    private Bitmap rotarImagenSiEsNecesario(Bitmap bitmap, String path) {
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotarBitmap(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotarBitmap(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotarBitmap(bitmap, 270);
                default:
                    return bitmap;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al leer EXIF: " + e.getMessage());
            return bitmap;
        }
    }

    private Bitmap rotarBitmap(Bitmap source, float angulo) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angulo);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    // Crear un archivo temporal para la vista previa
    private File createTempFile() {
        // Crear un nombre de archivo único basado en la marca de tiempo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "TEMP_" + timeStamp + ".jpg";

        // Obtener el directorio de almacenamiento de la app
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, fileName);
    }

    // Guardar la foto permanentemente en la galería
    private void guardarFoto(View view) {
        if (lastCapturedImageUri == null || photoFile == null || !photoFile.exists()) {
            Toast.makeText(this, "No hay imagen para guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Desactivar botón mientras se guarda
        guardarFt.setEnabled(false);

        // Guardar la imagen en la galería pública
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()));
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        // Para Android 10 (API 29) y superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        }

        ContentResolver resolver = getContentResolver();
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (imageUri != null) {
            try {
                // Copiar desde archivo temporal al URI de MediaStore
                try (OutputStream outputStream = resolver.openOutputStream(imageUri);
                     InputStream inputStream = new FileInputStream(photoFile)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show();
                }

                // Volver al modo cámara
                volverAModoCamara();

            } catch (IOException e) {
                Log.e(TAG, "Error al guardar en galería: " + e.getMessage());
                Toast.makeText(this, "Error al guardar la imagen: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                guardarFt.setEnabled(true);
            }
        } else {
            Toast.makeText(this, "Error al crear entrada en la galería", Toast.LENGTH_SHORT).show();
            guardarFt.setEnabled(true);
        }
    }

    // Volver al modo cámara después de guardar o cancelar
    private void volverAModoCamara() {
        // Ocultar vista previa y volver a la cámara
        if (previewImageView != null) {
            previewImageView.setVisibility(View.GONE);
        }
        fotoCamara.setVisibility(View.VISIBLE);

        // Restablecer botones
        guardarFt.setVisibility(View.GONE);
        tomarFt.setVisibility(View.VISIBLE);
        tomarFt.setEnabled(true);

        // Eliminar archivo temporal si todavía existe
        if (photoFile != null && photoFile.exists()) {
            photoFile.delete();
        }

        // Limpiar URI
        lastCapturedImageUri = null;
    }

    public void initObjects() {
        this.tvCamara = findViewById(R.id.tvCamara);
        this.tomarFt = findViewById(R.id.tomarFt);
        this.guardarFt = findViewById(R.id.guardarFt);
        this.fotoCamara = findViewById(R.id.fotoCamara);
        this.previewImageView = findViewById(R.id.previewImageView);

        // Si tienes el ImageView en tu layout, asegúrate de que esté inicialmente oculto
        if (this.previewImageView != null) {
            this.previewImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        // Si estamos en modo vista previa, volver a la cámara
        if (previewImageView != null && previewImageView.getVisibility() == View.VISIBLE) {
            volverAModoCamara();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ejecutarCamara != null) {
            ejecutarCamara.shutdown();
        }

        // Limpiar archivos temporales
        if (photoFile != null && photoFile.exists()) {
            photoFile.delete();
        }
    }
}