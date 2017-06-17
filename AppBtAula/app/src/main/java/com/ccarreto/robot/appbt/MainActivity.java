package com.ccarreto.robot.appbt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


//Link do site do Android Developers sobre BT
//https://developer.android.com/guide/topics/connectivity/bluetooth.html

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter = null; // Objeto para o bluetooth do Android.
    BluetoothDevice bluetootheDevice = null;  // Objeto para o bluetooth do dispositivo.
    BluetoothSocket bluetoothSocket = null;   // Objeto para o canal de comunicação.
    ConnectedThread connectedThread = null;   // Objeto para o thread de comunicação.

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    private InputStream mmInStream = null;

    // Request codes para os dados devolvidos de outras activities.
    private static final int REQUEST_CODE_ENABLE_BT = 1001;
    private static final int REQUEST_CODE_CONNECTION_BT = 1002;
    public static final int ESPERA32 = 1;
    private static final int LEDADOS = 2;

    // Flag para indicar se o bluetooth está ligado.
    boolean btConnected = false;

    // Objetos para os widgets da interface.
    Button btnConnection, btnLed1, btnLed2;

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
        btnLed1 = (Button) findViewById(R.id.btnLed1);
        btnLed2 = (Button) findViewById(R.id.btnLed2);


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
                        btnConnection.setText("Desconectar");
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
                Toast.makeText(getApplicationContext(), "O bluetooth foi desconectado.", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Ocorreu o erro " + e, Toast.LENGTH_LONG).show();
            }
        } else {
            // É para conectar.
            Intent i = new Intent(MainActivity.this, DeviceList.class);
            startActivityForResult(i, REQUEST_CODE_CONNECTION_BT);
        }
    }

    // Evento onClick no botão LED1.
    public void btnLed1OnClick(View v) {
        if (btConnected) {
            // Evia comando para ligar o LED1.
            connectedThread.send("led1");
        } else {
            Toast.makeText(getApplicationContext(), "O dispositivo não está conectado.", Toast.LENGTH_LONG).show();
        }
    }

    // Evento onClick no botão LED2.
    public void btnLed2OnClick(View v) {
        if (btConnected) {
            // Envia comando para ligar o LED2.
            connectedThread.send("led2");
        } else {
            Toast.makeText(getApplicationContext(), "O dispositivo não está conectado.", Toast.LENGTH_LONG).show();
        }
    }

    // ************************************
    // Classe standard para receber os dados
    // enviados do dispositivo.
    // ************************************
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
//        private final InputStream mmInStream;
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

            beginListenForData();
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // number of bytes returned from read()


//            // Keep listening to the InputStream until an exception occurs
//            while (true) {
//                try {
//                    // Read from the InputStream
//                    bytes = mmInStream.read(buffer);
//                    // Send the obtained bytes to the UI activity
//                    String data = new String(buffer, 0, bytes);
//                    handler.obtainMessage(ESPERA32, bytes, -1, data).sendToTarget();
//                } catch (IOException e) {
//                    break;
//                }
//            }


        }

        /* Call this from the main activity to send data to the remote device */
        //public void write(byte[] bytes) {
        public void send(String dataToSend) {
            byte[] buffer = dataToSend.getBytes();
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
            }
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

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
//                                    final String mData = new String(encodedBytes, "US-ASCII");
                                    final int mData = encodedBytes[0] + (encodedBytes[1] << 8);
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            Log.d("data", "" + mData);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

}
