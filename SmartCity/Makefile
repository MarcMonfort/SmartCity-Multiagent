#!/bin/sh

compile:
	javac -cp lib/jade.jar:lib/JenaLibs/* -d out/production/ -sourcepath src/ src/org/upc/edu/Behaviours/*.java
	javac -cp lib/jade.jar -d out/production/ -sourcepath src/ src/org/upc/edu/AIAExamples/communication/*.java
	javac -cp lib/jade.jar -d out/production/ -sourcepath src/ src/org/upc/edu/Protocols/*.java

gui:
	java -cp lib/jade.jar:lib/JenaLibs/*:out/production/ jade.Boot -gui -local-host 127.0.0.1

hello-world:
	java -cp lib/jade.jar:out/production/ jade.Boot -local-host 127.0.0.1 -container HelloTio:org.upc.edu.Behaviours.HelloWorldAgent\("Que","pasa","tio"\) -local-host 127.0.0.1

entorno:
	java -cp lib/jade.jar:lib/JenaLibs/*:out/production/ jade.Boot -local-host 127.0.0.1 -container entornooo:org.upc.edu.Behaviours.EntornoAgent\("Que","pasa","tio"\) -local-host 127.0.0.1

vehiculo:
	java -cp lib/jade.jar:out/production/ jade.Boot -local-host 127.0.0.1 -container elVehiculo:org.upc.edu.Behaviours.VehiculoAgent\("0"\) -local-host 127.0.0.1
vehiculo2:
	java -cp lib/jade.jar:out/production/ jade.Boot -local-host 127.0.0.1 -container elVehiculo_2:org.upc.edu.Behaviours.VehiculoAgent\("1"\) -local-host 127.0.0.1


all: compile

