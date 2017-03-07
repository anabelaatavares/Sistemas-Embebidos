# SistemasEmbebidos

##Trabalhos de preparação para quem nunca trabalhou com o Arduíno

1.       Fazer o esquema, circuito, algoritmo e programa que permita acender e apagar um led com uma frequência de 1Hz.

a.       Determinar experimentalmente qual é a frequência máxima a que pode acender e apagar o led de forma a que consiga ver a sua cintilação.

2.       Fazer o esquema, circuito, algoritmo e programa que permita acender e apagar, sequencialmente, 1 de 8 leds. A cada momento apenas um led deverá estar aceso. Depois de acender o oitavo led deve voltar ao primeiro.

a.       Altere o circuito e o programa para inserir um botão de pressão que, quando premido, inverta o sentido com que os leds acendem.

b.       Altere o circuito e o programa para controlar através de uma resistência variável o tempo que cada led deverá ficar aceso entre um mínimo de 50ms e um máximo de 3s.

c.       Implemente um novo algoritmo, que não utilize o delay(), e permita que, em tempo real, o circuito reaja a uma alteração do interruptor ou da resistência variável.

d.       Faça uma nova versão em que através de comandos enviados do PC possa controlar a direção com que os leds acendem e o tempo que cada um fica aceso

3.       Fazer o esquema, circuito, algoritmo e programa que permita controlar o brilho de um led a parir de um comando enviado do PC

 

##Trabalhos de preparação para o projeto final a fazer por todos os alunos

1.       Fazer um programa que envie, através da porta série, em formato ASCII, um float e um inteiro para o computador a uma frequência de 10Hz

a.       Fazer um programa para o computador que receba os dois valores, os multiplique, e envie o resultado para o Arduino em formato ASCII.

b.       Refaça o programa do Arduíno para poder receber os valores enviados pelo PC, determinar o tempo entre o envio dos dados e a chegada da resposta e enviar esse tempo, em formato ASSCII, para o PC.

c.       Altere o programa do PC para receber também o tempo de atraso e guardar esse valor num ficheiro que possa ser aberto pelo Excel.

d.       Altere o valor do baudrate e determine experimentalmente de que forma é que o baudrate influencia os tempos de atraso. Utilize o Excel para mostrar os resultados obtidos.

e.       Para dois valores diferentes de baudrate determine qual a frequência máxima de dados que consegue transmitir entre o Arduíno e o PC.

2.       Fazer todo o exercício 1 usando a transmissão de dados em formato binário.

3.       Fazer os exercícios 1 e 2 utilizando o Bluetooth para ligar ao PC e comparar os resultados obtidos com a ligação direta da porta série por USB.

a.       Faça uma versão do programa do PC para Android e efetue as medidas de tempos comparando-as com as obtidas no PC através da ligação Bluetooth