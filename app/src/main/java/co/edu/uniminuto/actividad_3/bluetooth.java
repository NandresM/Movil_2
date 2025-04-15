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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.Set;

public class bluetooth extends AppCompatActivity {

    private Button btnBlOn;
    private Button btnBlOff;
    private Button btnListVinculados;
    public TextView state;
    private BluetoothAdapter bluetoothAdapter;
    private ListView listVinculados;
    private ArrayAdapter<String> deviceListAdapter;
    private ArrayList<String> deviceList;
    private ArrayList<BluetoothDevice> deviceObjects;

    
    
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
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
        btnListVinculados.setOnClickListener(this::listaVinculados);

        setupListVinculados();

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
            // Limpiar listas anteriores
            deviceList.clear();
            deviceObjects.clear();

            // Verificar si el Bluetooth está activado
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "El Bluetooth está apagado. Enciéndalo primero.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Obtener dispositivos vinculados
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();
                    String deviceInfo = deviceName + " - " + deviceAddress;

                    deviceList.add(deviceInfo);
                    deviceObjects.add(device);
                }
                deviceListAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Se encontraron " + pairedDevices.size() + " dispositivos vinculados", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No hay dispositivos vinculados", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("BLUETOOTH", "Error al mostrar dispositivos vinculados: " + e.getMessage());
            Toast.makeText(this, "Error al obtener dispositivos vinculados", Toast.LENGTH_SHORT).show();
        }

    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void setupListVinculados() {
        // Configurar listener para la selección de dispositivos
        listVinculados.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < deviceObjects.size()) {
                BluetoothDevice selectedDevice = deviceObjects.get(position);
                Toast.makeText(this, "Dispositivo seleccionado: " + selectedDevice.getName(), Toast.LENGTH_SHORT).show();
                // Aquí puedes agregar código para conectarte al dispositivo
            }
        });
    }






    private void initObjects(){

        this.btnBlOn = findViewById(R.id.btnBlOn);
        this.btnBlOff = findViewById(R.id.btnBlOff);
        this.btnListVinculados = findViewById(R.id.btnListVinculados);
        this.state = findViewById(R.id.state);
        this.listVinculados = findViewById(R.id.listVinculados);
        this.deviceList = new ArrayList<>();
        this.deviceObjects = new ArrayList<>();
        this.deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        this.listVinculados.setAdapter(deviceListAdapter);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    }

}


    

    
