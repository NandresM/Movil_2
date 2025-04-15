package co.edu.uniminuto.actividad_3;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class bluetooth extends AppCompatActivity {

    private Button btnBlOn;
    private Button btnBlOff;
    public TextView state;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> listVinculados;

    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bluetooth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initObjects();
        btnBlOn.setOnClickListener(this::blutuOn);
        btnBlOff.setOnClickListener(this::blutuOff);

        // Inicializar el adaptador Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Verificar si el dispositivo soporta Bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
            state.setText("Bluetooth no soportado");
        }


    }

    //verifica  estado del bluetooh
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {

                state.setText("Bluetooth encendido");
            } else {

                state.setText("Bluetooth apagado");
            }
        }

    }

    //encendemos el bluetooth
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void blutuOn(View view) {


        try {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();  // Esto requiere permisos BLUETOOTH_ADMIN
                Toast.makeText(this, "Encendiendo Bluetooth", Toast.LENGTH_SHORT).show();
            }
            state.setText("Bluetooth encendido");
        } catch (Exception e) {
            Log.e("BLUETOOTH", "Error al encender: " + e.getMessage());
            Toast.makeText(this, "Error al encender Bluetooth", Toast.LENGTH_SHORT).show();
        }


    }

    //apagamos el bluetooth
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void blutuOff(View view) {
        try {
            bluetoothAdapter.disable();  // Apaga el bluetooth directamente
            state.setText("Bluetooth apagado");
        } catch (Exception e) {
            Log.e("BLUETOOTH", "Error al apagar: " + e.getMessage());
        }

    }


    //lista de vinculados
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void listaVinculados(View view) {
        try {

            listVinculados = new ArrayList<>();

        }catch (Exception e){
            Log.e("BLUETOOTH", "Error al listar: " + e.getMessage());
        }
    }







    private void initObjects(){

        this.btnBlOn = findViewById(R.id.btnBlOn);
        this.btnBlOff = findViewById(R.id.btnBlOff);
        this.state = findViewById(R.id.state);


    }

}


    

    
