byte inicio = 32;
int modo = 0, modoAtual = 1;
long tempoAtual, tempoInicio;
int botaoGraves = 13;//cabo branco
int botaoMedios = 12;//cabo laranja
int botaoAgudos = 11;//cabo roxo
int estadoBotaoGraves, estadoBotaoMedios, estadoBotaoAgudos;
int ledVerde1 = 8;
int ledVerde2 = 7;
int ledVerde3 = 6;
int ledAmarelo1 = 5;
int ledAmarelo2 = 4;
int ledAmarelo3 = 3;
int ledVermelho1 = 2;
int ledVermelho2 = 9;
long valorRecebido;
#define ESPERA32 0
#define ESPERADADOS 1


void setup() {
  // initialize both serial ports:
  Serial.begin(9600);

  Serial1.begin(9600);
  pinMode(botaoGraves, INPUT);
  pinMode(botaoMedios, INPUT);
  pinMode(botaoAgudos, INPUT);

  pinMode(ledVerde1, OUTPUT);
  pinMode(ledVerde2, OUTPUT);
  pinMode(ledVerde3, OUTPUT);
  pinMode(ledAmarelo1, OUTPUT);
  pinMode(ledAmarelo2, OUTPUT);
  pinMode(ledAmarelo3, OUTPUT);
  pinMode(ledVermelho1, OUTPUT);
  pinMode(ledVermelho2, OUTPUT);
}

void loop() {
  //   read from port 1, send to port 0:
  if (Serial1.available()) {
    int mByte = Serial1.read();
    abreLatas(mByte);
  }
<<<<<<< HEAD

  tempoAtual = millis();

  if (tempoAtual - tempoInicio > 1000) {
    tempoInicio = millis();
    estadoBotaoGraves = digitalRead(botaoGraves);
    estadoBotaoMedios = digitalRead(botaoMedios);
    estadoBotaoAgudos = digitalRead(botaoAgudos);

    //se pressionado (HIGH)
    if ( estadoBotaoGraves == HIGH) {
      modo = 10;
    } else if ( estadoBotaoMedios == HIGH) {
      modo = 20;
    } else if ( estadoBotaoAgudos == HIGH) {
      modo = 30;
    } else {
      modo = 0;
      blackout();
      equalizer(valorRecebido);
    }
=======
  //
  //  // read from port 0, send to port 1:
  //  if (Serial.available()) {
  //    int inByte = Serial.read();
  //    Serial1.write(inByte);
  //  }

  tempoAtual = millis();

  //if (tempoAtual - tempoInicio > 100) {
  //tempoInicio = millis();
  estadoBotaoGraves = digitalRead(botaoGraves);
  estadoBotaoMedios = digitalRead(botaoMedios);
  estadoBotaoAgudos = digitalRead(botaoAgudos);

  modo = modoAtual;

  //se pressionado (HIGH)
  if (estadoBotaoGraves == HIGH) {
    modoAtual = 10;
  } else if ( estadoBotaoMedios == HIGH) {
    modoAtual = 20;
  } else if ( estadoBotaoAgudos == HIGH) {
    modoAtual = 30;
  } else {
    modoAtual = 0;
  }

  if (modo != modoAtual) {
>>>>>>> 48b5bebc80904cc75abb0db757f34f2b94054773
    Serial1.write(inicio);
    Serial1.write((char*) &modoAtual, 2);
  }
  //}
}

void acenderLed(int varLed) {
  digitalWrite(varLed, HIGH);
}

void apagarLed(int varLed) {
  digitalWrite(varLed, LOW);
}

void abreLatas(int mByte) {
  byte bytesRecebidos[20];
<<<<<<< HEAD
  int i = 0;

  Serial.println(mByte);
  if (mByte != 32) {
    bytesRecebidos[i] = mByte;
    i++;
  } else {
    valorRecebido = bytesRecebidos[i];
    Serial.println(valorRecebido);
    bytesRecebidos[i] = 0;
    i = 0;
=======
  static int i = 0;
  static boolean estado = ESPERA32;

  switch (estado) {
    case ESPERA32:
      if (mByte == 32) {
        estado = ESPERADADOS;
        i = 0;
      }
      break;
    case ESPERADADOS:
      bytesRecebidos[i] = mByte;
      i++;
      if (i == 4) {
        valorRecebido = *((long*) (bytesRecebidos));
      } else if (i == 5) {
        byte checksum = bytesRecebidos[4];
        int soma = 0;
        for (int i = 0; i < 4; i++) {
          soma += bytesRecebidos[i];
        }
        if (soma == checksum) {          
          blackout();
          equalizer(valorRecebido);
        }
        estado = ESPERA32;
      }
      break;
>>>>>>> 48b5bebc80904cc75abb0db757f34f2b94054773
  }

}

void blackout() {
  apagarLed(ledVerde1);
  apagarLed(ledVerde2);
  apagarLed(ledVerde3);
  apagarLed(ledAmarelo1);
  apagarLed(ledAmarelo2);
  apagarLed(ledAmarelo3);
  apagarLed(ledVermelho1);
  apagarLed(ledVermelho2);
}

void equalizer(int vr) {
  //todo ajustar
  int  limiteGraves = 50;
  int  limiteMedios = 100;

  if (vr >= 0) {
    acenderLed(ledVerde1);
    if (vr > limiteGraves / 3) {
      acenderLed(ledVerde2);
      if (vr > (limiteGraves / 3) * 2) {
        acenderLed(ledVerde3);
        if (vr > limiteGraves) {
          acenderLed(ledAmarelo1);
          if (vr > limiteMedios / 3) {
            acenderLed(ledAmarelo2);
            if (vr > (limiteMedios / 3) * 2) {
              acenderLed(ledAmarelo3);
              if (vr > limiteMedios) {
                acenderLed(ledVermelho1);
                if (vr > limiteMedios * 2) {
                  acenderLed(ledVermelho2);
                }
              }
            }
          }
        }
      }
    }
  }
}

