package pt.ipg.intensidadevozarduino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter = null; // Objeto para o bluetooth do Android.
    BluetoothDevice bluetootheDevice = null;  // Objeto para o bluetooth do dispositivo.
    BluetoothSocket bluetoothSocket = null;   // Objeto para o canal de comunicação.
    ConnectedThread connectedThread = null;   // Objeto para o thread de comunicação.

    // Request codes para os dados devolvidos de outras activities.
    private static final int REQUEST_CODE_ENABLE_BT = 1001;
    private static final int REQUEST_CODE_CONNECTION_BT = 1002;

    // Flag para indicar se o bluetooth está ligado.
    boolean btConnected = false;

    //Checksum, soma dos bytes do valor inteiro enviado
    int checksum;

    // Objetos para os widgets da interface.
    Button btnConnection, btnEnviarValor;
    EditText et_valor_freq;

    // Endereço MAC do device conectado via bluetooth
    String deviceMACAddress = null;

    // ID único para a ligação socket.
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ligação com os widgets da interface.
        btnConnection = (Button) findViewById(R.id.btnConnection);
        et_valor_freq = (EditText) findViewById(R.id.editText_valor);

        // Criação do bluetooth adapter e activação do mesmo.
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "O dispositivo não possui bluetooth.", Toast.LENGTH_LONG).show();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BT);
        }
    }

    // Função para receber as respostas das activities chamadas via intents.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Resposta da activity do sistema que pede ao utilizador para
            // ativar o bluetooth.
            case REQUEST_CODE_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "O bluetooth foi ativado.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "O bluetooth não foi ativado.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            // Resposta da activity DeviceList onde é selecionado o device
            // ao qual a app se vai ligar.
            case REQUEST_CODE_CONNECTION_BT:
                if (resultCode == Activity.RESULT_OK) {
                    // Obter o MAC address do device selecionado na ListActivity
                    deviceMACAddress = data.getExtras().getString(DeviceList.deviceMACAddress);
                    //Toast.makeText(getApplicationContext(), "Device MAC: " + deviceMACAddress, Toast.LENGTH_LONG).show();

                    // Obter objeto para o device com o deviceMACAddress selecionado.
                    bluetootheDevice = bluetoothAdapter.getRemoteDevice(deviceMACAddress);
                    try {
                        // Obter um socket para establecer uma ligação de dados.
                        bluetoothSocket = bluetootheDevice.createRfcommSocketToServiceRecord(uuid);
                        bluetoothSocket.connect();
                        btConnected = true;
                        btnConnection.setText("Desligar");
                        Toast.makeText(getApplicationContext(), "Ligado a " + deviceMACAddress, Toast.LENGTH_LONG).show();

                        // Criação e ativação da thread the recebe os dados via bluetooth.
                        connectedThread = new ConnectedThread(bluetoothSocket);
                        connectedThread.start();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Ocorreu o erro " + e, Toast.LENGTH_LONG).show();
                        btConnected = false;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Falha ao obter o MAC Address", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    // Evento onClick no botão Connctar.
    public void btnConnectionOnClick(View v) {
        if (btConnected) {
            // É para desconectar.
            try {
                bluetoothSocket.close();
                btConnected = false;
                btnConnection.setText("Conectar");
                Toast.makeText(getApplicationContext(), "O bluetooth foi desligado.", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Ocorreu o erro " + e, Toast.LENGTH_LONG).show();
            }
        } else {
            // É para conectar.
            Intent i = new Intent(MainActivity.this, DeviceList.class);
            startActivityForResult(i, REQUEST_CODE_CONNECTION_BT);
        }
    }

    // Evento onClick no botão enviar valor.
    public void btnEnviarValorClick(View v) {
        if (btConnected) {
            // Envia valor na edittext.
            int valor = Integer.parseInt(et_valor_freq.getText().toString());
            connectedThread.sendInt(valor);
        } else {
            Toast.makeText(getApplicationContext(), "O dispositivo não está ligado.", Toast.LENGTH_LONG).show();
        }
    }

    // ************************************
    // Classe standard para receber os dados
    // enviados do dispositivo.
    // ************************************
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
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // number of bytes returned from read()

            /*
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    String data = new String(buffer, 0, bytes);
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, data)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
            */

        }

        /* Call this from the main activity to sendString data to the remote device */
        //public void write(byte[] bytes) {
        public void sendString(String dataToSend) {
            byte[] buffer = dataToSend.getBytes();
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
            }
        }

        public void sendInt(int dataToSend) {
            byte[] buffer = intToByteArray(dataToSend);
            byte flag = 32;
            try {
                mmOutStream.write(flag);
                mmOutStream.write(buffer);
                mmOutStream.write(checksum);
                //Log.d("chk", "" + checksum);
            } catch (IOException e) {
            }
        }

        private byte[] intToByteArray(int val) {
            byte[] bytesDoInteiro = {
                    (byte) val,
                    (byte)(val >>> 8),
                    (byte)(val >>> 16),
                    (byte)(val >>> 24)
            };
            checksum = 0;

            //byte em java cabem 128
            checksum += bytesDoInteiro[0] & 0xFF;
            checksum += bytesDoInteiro[1];
            checksum += bytesDoInteiro[2];
            checksum += bytesDoInteiro[3];


            return bytesDoInteiro;
        }

        /* Call this from the main activity to shutdown the connection */
        /*
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
        */
    }
}
