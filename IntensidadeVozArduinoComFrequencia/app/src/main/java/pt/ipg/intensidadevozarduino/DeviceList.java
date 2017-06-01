package pt.ipg.intensidadevozarduino;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by RT on 27/05/2017.
 * Classe para gerir a lista de dispositivos emparelhados.
 */

public class DeviceList extends ListActivity {

    BluetoothAdapter bluetoothAdapter = null;
    static String deviceMACAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Estrutura de dados com os dados a mostrar na ListActivity.
        ArrayAdapter<String> btDeviceData = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        // Objeto para gerir o bluetooth do dispositivo.
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Obter dados dos dispositivos emparelhados.
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // Se existirem dispositivos emparelhados.
        if (pairedDevices.size() > 0) {
            // Adicionar dados que interessam à estrutura de dados da ListActivity.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceMAC = device.getAddress();
                btDeviceData.add(deviceName + "\n" + deviceMAC);
            }
        }

        // Ativar a lista de dados da ListActivity.
        setListAdapter(btDeviceData);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Obter informação do device selecionado.
        String deviceInfo = ((TextView) v).getText().toString();
        //Toast.makeText(getApplicationContext(), "Device info.: " + deviceInfo, Toast.LENGTH_LONG).show();

        // Obter o MAC Address do device selecionado.
        String macAddress = deviceInfo.substring(deviceInfo.length() - 17);
        //Toast.makeText(getApplicationContext(), "MAC: " + macAddress, Toast.LENGTH_LONG).show();

        // Criar um Intent para devolver o MAC Address do device selecionado.
        Intent getMACIntent = new Intent();
        getMACIntent.putExtra(deviceMACAddress, macAddress);

        // Correu tudo bem, então definir um RESULT_OK para o intent e
        // terminar a activity.
        setResult(RESULT_OK, getMACIntent);
        finish();
    }
}
