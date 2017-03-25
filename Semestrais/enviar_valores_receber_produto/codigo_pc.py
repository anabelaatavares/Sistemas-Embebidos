import serial
import struct

ser = serial.Serial('COM4', 9600)
print (ser.name)

#draw()
while True:
    if(ser.in_waiting > 0):
        pacote = ser.readline().strip()
        print (pacote)

        posicaoSeparador = pacote.decode().find(';')
        flutuante = float(pacote[0:posicaoSeparador])
        print (flutuante)

        inteiro = int(pacote[posicaoSeparador+1:len(pacote)])
        print (inteiro)

        produto = flutuante * inteiro
        print(produto)

        produtoEmBytes = bytearray(struct.pack("f", produto))
        #print(produtoEmBytes)

        ser.write('a')
