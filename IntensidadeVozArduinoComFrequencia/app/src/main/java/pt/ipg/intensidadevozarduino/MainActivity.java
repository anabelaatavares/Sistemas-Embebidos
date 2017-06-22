package pt.ipg.intensidadevozarduino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.UUID;

import fftpack.RealDoubleFFT;

import static java.sql.DriverManager.println;

public class MainActivity extends Activity implements View.OnClickListener {

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
    Button btnConnection;
    EditText et_valor_freq;
    TextView tv_modo;

    // Endereço MAC do device conectado via bluetooth
    String deviceMACAddress = null;

    // ID único para a ligação socket.
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    //// ler estado botoes
    InputStream mmInStream = null;
    final int ESPERA32 = 0;
    final int RECEIVE_MESSAGE = 1;
    int contador = 0;
    byte[] bytesRecebidos = new byte[5];
    int estado = ESPERA32;
    int modo = 0;

    // Objeto que permite vincular con las preferencias seleccionadas
    SharedPreferences prefs;
    ////////////////////////////////////////////////////////////////////////

    //Ojeto de tipo WakeLock que permite mantener despierta la aplicacion
    protected PowerManager.WakeLock wakelock;

    RecordAudio recordTask; // proceso de grabacion y analisis
    AudioRecord audioRecord; // objeto de la clase AudioReord que permite captar el sonido

    Button startStopButton; // boton de arranquue y pausa
    boolean started = false; // condicion del boton

    // Configuracion del canal de audio
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    static double RATE = 8000; // frecuencia o tasa de muestreo

    int bufferSize = 0;  // tamaño del buffer segun la configuracion de audio
    int bufferReadResult = 0; // tamaño de la lectura
    int blockSize_buffer = 1024; // valor por defecto para el bloque de lectura de buffer


    // Objeto de la clase que determina la FFT de un vector de muestras
    private RealDoubleFFT transformer;
    int blockSize_fft = 2048; // tamaño de la transformada de Fourier


    // Frecuencias del rango de estudio asociadas al instrumento
    static double MIN_FREQUENCY = 50; // HZ
    static double MAX_FREQUENCY = 3000; // HZ

    // Valores pordefecto para el estudio de los armonicos

    double UMBRAL = 100; // umbral de amplitud valido para tener en cuenta // los armonicos, depende del tamaño de la FFT

    int LONGTRAMA = 20; // tamaño de la ventana de estudio de los armonicos
    // tambien depende del tamaño de la FFT

    int NUM_ARMONICOS = 6; // numero de armonicos a tener en cuenta

    int REL_AMP = 8; // relacion de amplitudes que han de tener los dos primeros armonicos para hallar la nota
    int REL_FREC = 4; // relacion en frecuencia que han de tener los dos primeros armonicos para hallar la nota

    double[] aux3;

    { // declaracion de vector auxiliar para el estudio de la trama
        aux3 = new double[LONGTRAMA];
    } // sera el array que contenga la amplitud de los armonicos

    double[] validos = new double[NUM_ARMONICOS]; // vector que tendra solo los armonicos de interes
    double[] amplitudes = new double[NUM_ARMONICOS]; // vector con las amplitudes de estos armonicos de interes

    double freq_asociada = 0; // valor de la frecuencia obtenida como fundamental tras el estudio de los armonicos


    // Frecuencia de referencia asociada a la nota LA-4
    static double frec_ref = 440;

    // Array con la escala cromatica
    String[] escala = {"F#", "G", "G#", "A", "Bb", "B", "C", "C#", "D", "Eb", "E", "F"};


    // * Aqui tendremos encuenta el rango audible por una posible reutilizacion del codigo
    // aunque en la practica solo estudiemos el espectro en un rango menor, de 50 a 4000 Hz p.ej.
    int n = 66; // indice correspondiente al maximo frecuencial: 440*2^(66/12) = 19912.127 Hz
    static double g = -51, j; // Indice correspondiente al minimo frecuencial: 440*2^(-51/12) = 23.125 Hz
    int fin = n + (int) Math.abs(g); // numero de posiciones desde 'j' hasta 'n'

    // Array con las notas asociadas al array de frecuencias
    String a; //variable de tipo cadena de caracteres para la nota
    String[] G; // conjunto de notas posibles


    double[] F;

    { // array con todo el conjunto de frecuencias posibles  detro del rango audible
        F = new double[fin];
        G = new String[fin];
        j = g;

        // Bucle para inicializar tanto el array de frecuencias como el de notas
        for (int i = 0; i < F.length; i++) {
            j = j + 1;
            F[i] = frec_ref * Math.pow(2, j / 12);
            a = escala[i % 12];
            G[i] = a;
        }
    }


    // Calculamos el cociente de la Relacion de Aspecto que usaremos para ubicar
    // todo aquello cuya posicion varie en funcion de un valor determinado

    // Tamaños de texto para los diferentes mensajes y resultados
    int TAM_TEXT = 40;


    TextView statusText; // objeto de la clase TextView para mostrar mensaje

    /// PREFERENCIAS

    boolean AUTODETECCION = false;

    int altura_umbral = 7;

    // Usamos la clase DecimalFormat para establecer el numero de decimales del resultado
    DecimalFormat df1;
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

    {
        symbols.setDecimalSeparator('.');
        df1 = new DecimalFormat("#", symbols);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ligação com os widgets da interface.
        btnConnection = (Button) findViewById(R.id.btnConnection);
        tv_modo = (TextView) findViewById(R.id.textView_modo);

        // Inicializacion de todos los elementos graficos
        statusText = (TextView) this.findViewById(R.id.StatusTextView);
        startStopButton = (Button) this.findViewById(R.id.StartStopButton);
        startStopButton.setOnClickListener(this);

        //evitar que la pantalla se apague
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "etiqueta");
        wakelock.acquire();


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

    // ************************************
    // Classe standard para receber os dados
    // enviados do dispositivo.
    // ************************************
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;

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
                    (byte) (val >>> 8),
                    (byte) (val >>> 16),
                    (byte) (val >>> 24)
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


    protected void onDestroy() {
        super.onDestroy();

        this.wakelock.release();

    }

    // Adicionalmente, se recomienda usar onResume, y onSaveInstanceState, para que,
    // si minimizamos la aplicacion, la pantalla se apague normalmente, de lo
    // contrario, no se apagará la pantalla aunque no tengamos a nuestra aplicación
    // en primer plano.

    protected void onResume() {
        super.onResume();

        wakelock.acquire();

        // Valor que muestra el boton al volver a la actividad
        startStopButton.setText("ON");


        // Cargamos las preferencias por si el usuario ha hecho alguna modificacion
        // en la configuracion de la aplicacion
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Preferencia que permite que la aplicacion se detenga automaticamente
        // al detectar un sonido con un volumen y varianza determinados
        AUTODETECCION = prefs.getBoolean("Default_Option", false);


        // Altura que tendra la linea que representa el umbral de deteccion de armonicos
        altura_umbral = (prefs.getInt("umbralPref", 7));


        //RATE = Integer.parseInt(prefs.getString("list_frec_muest","8000"));
        //blockSize_fft = Integer.parseInt(prefs.getString("list_fft","1024"));


    }

    // Si se sale de la actividad de manera inesperada
    @Override
    protected void onPause() {
        super.onPause();
        if (started) {

            started = false;
        }
    }

    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        this.wakelock.release();

    }

    // PROCESO O TAREA ASINCRONA QUE SENCARGARA DE RECOGER Y ANALIZAR LA SEÑAL DE AUDIO DE ENTRADA
    class RecordAudio extends AsyncTask<Void, short[], Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {

                // estimacion del tamaño del buffer en funcion de la configuracion de audio
                bufferSize = AudioRecord.getMinBufferSize((int) RATE,
                        channelConfiguration, audioEncoding);

                // inicializacion del objeto de la clase AudioRecord
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, (int) RATE,
                        channelConfiguration, audioEncoding, bufferSize);

                // declaracion del vector que almacenara primero los datos recogidos del microfono
                short[] audio_data = new short[blockSize_buffer]; // tipo de dato short (2^15 = 32768)

                audioRecord.startRecording(); // empieza a grabar

                while (started) { // mientras no se pulse de nuevo el boton

                    // tamañod de la lectura
                    bufferReadResult = audioRecord.read(audio_data, 0, blockSize_buffer);


                    // se mandan las muestras recogidas para su procesado
                    publishProgress(audio_data);

                }

                audioRecord.stop(); // para la grabacion momentaneamente

            } catch (Throwable t) { // en caso de error, p.ej. captura de audio ya activa
                Log.e("AudioRecord", "Recording Failed");
            }

            return null;
        }

        protected void onProgressUpdate(short[]... toTransform) {


            double maximo = 0, promedio = 0, varianza = 0;

            // Arrays con las muestras de audio en tiempo y frecuencia en formato double
            double[] trama, trama_espectro;
            trama = new double[blockSize_fft];

            // inicializamos el vector que contendra la FFT de
            transformer = new RealDoubleFFT(blockSize_fft);


            for (int i = 0; i < bufferReadResult; i++) {

                trama[i * 2] = (double) toTransform[0][i];
                trama[i * 2 + 1] = 0; // aumentaremos la resolucion en frecuencia de la transformada interpolando ceros

            }

            maximo = max(trama, 0, trama.length).valor;

            //promedio = promedio(trama);

            // normalizamos la trama de sonido dividiendo todas las muestra por la de mayor valor
            normaliza(trama);

            varianza = varianza(trama);

            /////////////////////////////////////////////////////////////////////////////////////
            // AUTODETECCION
            if (AUTODETECCION) {
                if (validos[1] != 0) { // Si han aprecido armonicos
                    if ((maximo >= 800) && (varianza > 0.04)) {

                        started = false;
                        startStopButton.setText("ON");
                        recordTask.cancel(true);

                    }
                }
            }

            // Conseguimos precision con el enventanado
            // Filtra los armonicos en el espectro
            // Destaca y realza los fundamentales

            trama = aplicaHamming(trama);


            // Dominio transformado. Realiza la FFT de la trama
            transformer.ft(trama);

            statusText.setTextSize(TAM_TEXT); // definimos el tamaño para el texto


            DevuelveNota(trama); // escribe por pantalla la nota resultante

            if (freq_asociada > MIN_FREQUENCY) {

                int position = CalculaIndice(freq_asociada);
                statusText.setText(df1.format(freq_asociada));
                int valor = Integer.parseInt(df1.format(freq_asociada));
                new CospeFreqPorBT().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, valor);
                new ChupaValorBT().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }

        }

    }

    // Algortimo que una vez detecta los armonicos establece cuales son los validos
    // para determinar la nota fundamental de la trama que se pasa como entrada
    public String DevuelveNota(double[] trama) {

        freq_asociada = 0;

        String nota_final; // cadena que contendra el valor final de la nota

        double[] armonicos = devuelveArmonicos(trama); // vector con los armonicos detectados

        String[] notas = new String[NUM_ARMONICOS]; // vector con las notas correspondientes a los armonicos validos

        // vector de numeros enteros con la correspondecia a la posicion dentro del array "escala" con las notas
        // de los armonicos validos
        int[] indices = new int[NUM_ARMONICOS];

        // Inicializamos a 0 el vector de armonicos validos
        for (int k = 0; k < NUM_ARMONICOS; k++) {
            validos[k] = 0;
        }

        int m = 0, n = 0; // indices para recorrer el array armonicos
        // Recorremos el vector de armonicos buscando los candidatos
        while ((m < NUM_ARMONICOS) && (n < armonicos.length - 1)) {

            // Evitamos que se repita mas de una vez un mismo armonico y que aparezcan dos muy proximos
            // Desventaja: puede que de dos muy proximos no tomemos el de mayor amplitud

            // Si lo que viene luego en la posicion correspondiente al vector armonicos es distinto a lo que hay ahora
            if ((armonicos[n + 1] != armonicos[n])) {

                // Si la diferencia de distancia en frecuencia entre lo que viene luego y lo que tengo ahora
                // es menor que la LONGITUD de TRAMA mejor quedate con lo que viene luego
                //if(Math.abs(armonicos[n+1]-armonicos[n])<LONGTRAMA/2){
                if (Math.abs(armonicos[n + 1] - armonicos[n]) < LONGTRAMA) {

                    validos[m] = armonicos[n + 1];

                    amplitudes[m] = aux3[n + 1];

                    notas[m] = BuscaNota(CalculaIndice(armonicos[n + 1])); // calcula la nota en funcion de la frecuencia

                    // devuelve el indice correspondiente a la posicion que ocupa la nota en el array "escala"
                    indices[m] = DevuelvePosicion(BuscaNota(CalculaIndice(armonicos[n + 1])));

                    m = m + 1; // avanzamos una posicion en "validos"
                    n = n + 2; // avanzamos dos en "armonicos"

                }

                // Si lo que viene luego en el vector armonicos supera el rango de LONGTRAMA
                else {
                    validos[m] = armonicos[n];

                    amplitudes[m] = aux3[n];

                    notas[m] = BuscaNota(CalculaIndice(armonicos[n])); // calcula la nota en funcion de la frecuencia

                    // devuelve el indice correspondiente a la posicion que ocupa la nota en el array "escala"
                    indices[m] = DevuelvePosicion(BuscaNota(CalculaIndice(armonicos[n])));

                    m++;
                    n++;

                }

            } else {
                n++;
            }
        }


        // Variables que contendran los valores
        // maximo y minimo del espectro asi como su relacion.
        double Min, Max, May, Men, relacion_amp, relacion_frec;


        // Asigna amplitud menor y mayor
        if (amplitudes[1] > amplitudes[0]) {
            Min = amplitudes[0];
            Max = amplitudes[1];
        } else {
            Min = amplitudes[1];
            Max = amplitudes[0];
        }

        // Asigna frecuencia menor y mayor
        if (validos[1] > validos[0]) {
            Men = validos[0];
            May = validos[1];
        } else {
            Men = validos[1];
            May = validos[0];
        }

        relacion_amp = Max / Min; // relacion entre las amplitudes

        relacion_frec = May / Men; // relacion entre las frecuencias

        // Si la relacion entre las amplitudes y entre las frecuencias es muy grande
        // despreciamos los armonicos y calculamos directamente la nota como
        // la correspondiente al armonico de mayor amplitud
        if ((relacion_amp > REL_AMP) && (relacion_frec > REL_FREC)) {

            freq_asociada = devuelvePitch(trama);
            nota_final = BuscaNota(CalculaIndice(freq_asociada));

            //nota_final = DevuelveNota(CalculaIndice(devuelvePitch(trama)));
        } else {

            // La diferencia entre los indices de ambos armonicos siempre ha de guardar
            // una cantidad de 3 unidades
            // Comprobamos que validos[1]!=0,es decir no hay armonico, para no confundir la nota
            // Fa# (escala[0]) presente en acordes como B = 5+9+0 ó D = 8+3+0
            if ((indices[1] >= 0) && (Math.abs(indices[0] - indices[1]) >= 3) && (validos[1] != 0)) {

                int menor, mayor; //

                // comprueba que indice es menor y cual mayor
                // para pasarselos como entrada al algoritmo
                // que estima la nota en funcion de las componentes
                if (indices[1] > indices[0]) {
                    menor = indices[0];
                    mayor = indices[1];
                } else {
                    menor = indices[1];
                    mayor = indices[0];
                }

                // Tenemos dos indices de armonicos que guardan una distancia suficiente para formar una nota
                nota_final = DeterminaNota(menor, mayor, indices[0]);
                /// Determinar freq_asociada
                // Si la nota esta en el acorde
                if (nota_final == escala[indices[0]]) {
                    freq_asociada = validos[1] / 3;
                } else if (nota_final == escala[indices[1]]) {
                    freq_asociada = validos[0] / 3;
                } else {
                    freq_asociada = validos[0] / 3;
                }
                // coge la frecuencia de la otra nota y la divide entre 3
                // Si no coge validos[0] y lo divide entre 3

            } else if (indices[0] - indices[1] == 0) {// si tenemos dos veces la misma nota
                nota_final = notas[0]; // sera esta la que prevalezca
                freq_asociada = validos[0];
            } else { // si no cumple ninguno de estos requisitos suponemos que es la de mayor amplitud

                freq_asociada = devuelvePitch(trama);
                nota_final = BuscaNota(CalculaIndice(freq_asociada));

                //nota_final = DevuelveNota(CalculaIndice(devuelvePitch(trama)));
            }


        }

        return nota_final;
    }

    // Metodo que nos devuelve la nota en funcion de la presencia de las componentes del acorde mayor de esa nota
    public String DeterminaNota(int menor, int mayor, int defecto) {

        // Escala cromatica y los valores numericos de las notas como acordes
        /* F# = [0,4,7]  = [F#,Bb,C#]
         * G  = [1,5,8]  = [G,B,D]
    	 * G# = [2,6,9]  = [G#,C,Eb]
    	 * A  = [3,7,10] = [A,C#,E]
    	 * Bb = [4,8,11] = [Bb,D,F]
    	 * B  = [5,9,0]  = [B,Eb,F#]
    	 * C  = [6,10,1] = [C,E,G]
    	 * C# = [7,11,2] = [C#,F,G#]
    	 * D  = [8,0,3]  = [D,F#,A]
    	 * Eb = [9,1,4]  = [Eb,G,Bb]
    	 * E  = [10,2,5] = [E,G#,B]
    	 * F  = [11,3,6] = [F,A,C] */

        // Cadena que devolvera como nota estimada
        String nota = escala[defecto]; // por defecto es la que esta primera en el array de armonicos

        // Iremos descartando posibilidades teniendo ordenados de menor a mayor los indices
        // Empezamos por el menor que es el 0, conforme mayor sea el indice menor, menos probabilidades
        // habra de que guarde una relacion de 3 unidades con el mayor indice

        if (menor == 0) {

            if ((mayor == 4) || (mayor == 7)) {

                nota = escala[0]; // es LA
            } else if ((mayor == 5) || (mayor == 9)) {
                nota = escala[5];
            } else if ((mayor == 3) || (mayor == 8)) {
                nota = escala[8]; // es RE
            }


        } else if (menor == 1) {


            if ((mayor == 5) || (mayor == 8)) {

                nota = escala[1]; // es SOL
            } else if ((mayor == 6) || (mayor == 10)) {

                nota = escala[6];
            } else if ((mayor == 4) || (mayor == 9)) {

                nota = escala[9]; // es MIb

                //freq_asociada = validos[0]/3;
            }

        } else if (menor == 2) {

            if ((mayor == 6) || (mayor == 9)) {

                nota = escala[2]; // es 2
            } else if ((mayor == 7) || (mayor == 11)) {
                nota = escala[7];
            } else if ((mayor == 5) || (mayor == 10)) {

                nota = escala[10];// es MI

                //freq_asociada = validos[0]/3;
            }


        } else if (menor == 3) {

            if ((mayor == 7) || (mayor == 10)) {

                nota = escala[3]; // es LA
            } else if ((mayor == 6) || (mayor == 11)) {

                nota = escala[11]; // es FA
            } else if (mayor == 8) {

                nota = escala[8];// es RE

                //freq_asociada = validos[1]/3;
            }

        } else if (menor == 4) {

            if ((mayor == 8) || (mayor == 11)) {

                nota = escala[4]; // es 4
            } else if (mayor == 7) {

                nota = escala[0];

            } else if (mayor == 9) {

                nota = escala[9]; // es MIb

                //freq_asociada = validos[1]/3;
            }
        } else if (menor == 5) {

            if (mayor == 8) {
                nota = escala[1];
            } else if (mayor == 9) {
                nota = escala[5]; // es 5
            } else if (mayor == 10) {

                nota = escala[10]; // es MI

                //freq_asociada = validos[1]/3;
            }
        } else if (menor == 6) {

            if (mayor == 9) {
                nota = escala[2];
            } else if (mayor == 10) {
                nota = escala[6]; // es 6
            } else if (mayor == 11) {
                nota = escala[11];
            }


        } else if (menor == 7) {

            if (mayor == 10) {
                nota = escala[3];
            } else if (mayor == 11) {
                nota = escala[7]; // es 7
            }

        } else if (menor == 8) {
            if (mayor == 11) {
                nota = escala[4];
            }
        } else {

            nota = "FALLO";

        }
        return nota;
    }

    // Función encargada de devolver la posicion que ocupa
    // en los arrays de frecuencias y notas
    public int CalculaIndice(double pitch) {

        double num; // indice correspondiente a la posicion respecto al LA4
        // (sera el valor que redondearemos para obtener 'indice')

        int indice; // valor redondeado de num


        // La siguiente operacion devuelve el indice correspondiente a la frecuencia
        // detectada, es la operacion inversa a la utilizada para calcular la teorica
        num = 12 * Math.log10(pitch / frec_ref) / Math.log10(2) + 51;

        indice = (int) Math.round(num); // convierte el indice a entero

        return indice;

    }

    // Función encargada de devolver la nota correspondiente
    public String BuscaNota(int indice) {

        // la nota sera el valor correspondiente a la posicion
        // 'indice' en el array 'G' del rango de notas posibles
        return G[indice];

    }


    // Función encargada de devolver el indice
    // o posición de la nota en el array "escala"
    public int DevuelvePosicion(String nota_draw) {


        int posicion = 0;
        boolean cumple = false;

        while ((!cumple) && (posicion < escala.length)) {
            if (nota_draw == escala[posicion]) {
                cumple = true;
            } else {
                posicion++;
            }

        }

        return posicion;

    }

    // Algoritmo que recibe como parametro de entrada una trama de frecuencias y determina cual es el
    // principal armonico, es decir el de mayor amplitud
    public double devuelvePitch(double[] data) {


        // indice o poscion en el que empezaremos a leer en la trama del espectro
        // que se corresponde con la frecuencia mínima del rango de deteccion
        final int min_frequency_fft = (int) Math.round(MIN_FREQUENCY
                * blockSize_buffer / RATE);
        // indice o poscion en el que acabaremos de leer en la trama del espectro
        // que se corresponde con la frecuencia máxima del rango de deteccion
        final int max_frequency_fft = (int) Math.round(MAX_FREQUENCY
                * blockSize_buffer / RATE);
        double best_frequency = min_frequency_fft; // inicializamos la frecuencia candidata en el minimo
        double best_amplitude = 0; // inicializamos a 0 la amplitud que al final sera la maxima de la trama

        // recorremos la trama
        for (int i = min_frequency_fft; i <= max_frequency_fft; i++) {

            // calcula la frecuncia actual restaurando el valor del indice o posicon
            final double current_frequency = i * 1.0 * RATE
                    / (blockSize_buffer);


            //final double normalized_amplitude = Math.abs(data[i]);

            // calcula la amplitud actual
            final double current_amplitude = Math.pow(data[i * 2], 2)
                    + Math.pow(data[i * 2 + 1], 2);

            // normaliza la amplitud actual
            final double normalized_amplitude = current_amplitude
                    * Math.pow(MIN_FREQUENCY * MAX_FREQUENCY, 0.5)
                    / current_frequency;

            // si es mayor que la anterior es candidata a ser la maxima
            if (normalized_amplitude > best_amplitude) {
                best_frequency = current_frequency;
                best_amplitude = normalized_amplitude;
            }
        }
        return best_frequency;

    }


    // Algoritmo que recibe como parametro de entrada una trama de frecuencias y determina cuales son los
    // principales armonicos que componen la señal en funcion del criterio de comparacion con un UMBRAL
    public double[] devuelveArmonicos(double[] data) {

        int r = 0; // indice para el recorrido del vector con los armonicos

        int umbral = (int) UMBRAL; // condicion necesaria para considerarse armonico
        int longtrama = LONGTRAMA; // longitud de la trama para el estudio de los armonicos


        // indice o poscion en el que empezaremos a leer en la trama del espectro
        // que se corresponde con la frecuencia mínima del rango de deteccion
        final int min_frequency_fft = (int) Math.round(MIN_FREQUENCY
                * blockSize_buffer / RATE);

        // indice o poscion en el que acabaremos de leer en la trama del espectro
        // que se corresponde con la frecuencia máxima del rango de deteccion
        final int max_frequency_fft = (int) Math.round(MAX_FREQUENCY
                * blockSize_buffer / RATE);
        double best_frequency = min_frequency_fft; // inicializamos la frecuencia candidata en el minimo

        double best_amplitude = 0; // inicializamos la amplitud a comparar con el umbral a 0

        double[] aux2; // declaracion de vector auxiliar para el estudio de la trama
        aux2 = new double[longtrama]; // sera el array que contenga los armonicos


        // bucle que recorre hasta la posicion del maximo del rango [MIN_FREQUENCY,MAX_FREQUENCY]
        for (int i = min_frequency_fft; i < max_frequency_fft; i = i + longtrama) {

            best_amplitude = 0; // restauramos el valor de la amplitud maxima una vez leida la trama

            // bucle que recorre la trama
            for (int j = 0; j < longtrama; j++) {


                final double current_frequency = (i + j) * 1.0 * RATE
                        / (blockSize_buffer);

                //final double normalized_amplitude = Math.abs(data[i+j]);

                final double current_amplitude = Math.pow(data[(i + j) * 2], 2)
                        + Math.pow(data[(i + j) * 2 + 1], 2);
                final double normalized_amplitude = current_amplitude
                        * Math.pow(MIN_FREQUENCY * MAX_FREQUENCY, 0.5)
                        / current_frequency;

                if (normalized_amplitude > best_amplitude) {
                    best_frequency = current_frequency;
                    best_amplitude = normalized_amplitude;
                }
            }

            if (best_amplitude > umbral) {
                // almacena en aux2 la posicion frecuencia
                // que cumple el requisito 'umbral'
                // y en aux3 la amplitud correspondiente

                aux3[r] = best_amplitude;
                aux2[r] = best_frequency;
                r = r + 1;


            }

        }
        return aux2; // devuelve el vector con las frecuencias de los armonicos

    }


    // Metodo para el calculo de la media de un vector de muestras.
    private static double media(double[] datos) {
        // Computo de la media.
        int N = datos.length;
        double med = 0;
        for (int k = 0; k < N; k++) {

            med += datos[k];
        }
        med = med / N;
        return med;
    }

    // Metodo para el calculo de la varianza de un vector de muestras.
    private static double varianza(double[] datos) {
        // Computo de la media.
        int N = datos.length;
        double med = media(datos);
        // Computo de la varianza.
        double varianza = 0;
        for (int k = 0; k < N; k++) {
            varianza += Math.pow(datos[k] - med, 2);
        }
        varianza = varianza / (N - 1);
        return varianza;
    }

    // Metodo para la normalizacion de un vector de muestras.
    private static double[] normaliza(double[] datos) {

        double maximo = 0;
        for (int k = 0; k < datos.length; k++) {
            if (Math.abs(datos[k]) > maximo) {
                maximo = Math.abs(datos[k]);
            }
        }
        for (int k = 0; k < datos.length; k++) {
            datos[k] = datos[k] / maximo;
        }
        return datos;
    }


    // Metodo para enventanar Hamming un vector de muestras.
    private static double[] aplicaHamming(double[] datos) {
        double A0 = 0.53836;
        double A1 = 0.46164;
        int Nbf = datos.length;
        for (int k = 0; k < Nbf; k++) {
            datos[k] = datos[k] * (A0 - A1 * Math.cos(2 * Math.PI * k / (Nbf - 1)));
        }
        return datos;
    }

    public Maximo max(double[] x, int ini, int fin) {

        Maximo miMaximo;
        miMaximo = new Maximo();

        for (int i = ini; i < fin; i++) {
            if (Math.abs(x[i]) >= miMaximo.valor) {
                miMaximo.valor = Math.abs(x[i]);
                miMaximo.pos = i;
            }

        }

        return miMaximo;

    }

    // Definicion de la clase del objeto Maximo
    class Maximo {
        int pos;// posicion
        double valor;
    }

    public void onClick(View v) {
        if (started) {

            started = false;
            startStopButton.setText("ON");
            recordTask.cancel(true);
            validos[1] = 0;

        } else {
            started = true;
            startStopButton.setText("OFF");
            recordTask = new RecordAudio();
            recordTask.execute();

        }
    }

    private class CospeFreqPorBT extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            int freq = params[0];
            int  limiteGraves = 150;
            int  limiteMedios = 250;

            if (modo == 0) {
                connectedThread.sendInt(freq);
            } else if (modo == 10) {
                if (freq < limiteGraves) connectedThread.sendInt(freq);
            } else if (modo == 20) {
                if (freq > limiteGraves && freq < limiteMedios) connectedThread.sendInt(freq);
            } else if (modo == 30) {
                if (freq > limiteMedios) connectedThread.sendInt(freq);
            }

            return null;
        }
    }

    private class ChupaValorBT extends AsyncTask<Void, Void, Integer> {

        byte[] bytesTemp = new byte[100];

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                if (mmInStream.available() > 0)
                    mmInStream.read(bytesTemp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bytesTemp != null) {

                for (int i = 0; i < bytesTemp.length; i++) {

                    switch (estado) {
                        case ESPERA32:
                            if (bytesTemp[i] == 32) {
                                estado = RECEIVE_MESSAGE;
                                contador = 0;
                            }
                            break;
                        case RECEIVE_MESSAGE:
                            bytesRecebidos[contador++] = bytesTemp[i];
                            if (contador == 2) {
                                estado = ESPERA32;
                                int inte = bytesRecebidos[0] + (bytesRecebidos[1] << 8);
                                return Integer.valueOf(inte);
                            }
                            break;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer != null) {
                tv_modo.setText(String.valueOf(integer));
                modo = integer;
            }
        }
    }


}
