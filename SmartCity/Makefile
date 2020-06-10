#!/bin/sh

compile:
	javac -cp lib/jade.jar -d out/production/ -sourcepath src/ src/org/upc/edu/Behaviours/*.java
	javac -cp lib/jade.jar -d out/production/ -sourcepath src/ src/org/upc/edu/AIAExamples/communication/*.java
	javac -cp lib/jade.jar -d out/production/ -sourcepath src/ src/org/upc/edu/Protocols/*.java

gui:
	java -cp lib/jade.jar:out/production/ jade.Boot -gui -local-host 127.0.0.1

hello-world:
	java -cp lib/jade.jar:out/production/ jade.Boot -local-host 127.0.0.1 -container HelloTio:org.upc.edu.Behaviours.HelloWorldAgent\("Que","pasa","tio"\) -local-host 127.0.0.1

entorno:
	java -cp lib/jade.jar:out/production/ jade.Boot -local-host 127.0.0.1 -container elEntorno:org.upc.edu.Behaviours.EntornoAgent\("Que","pasa","tio"\) -local-host 127.0.0.1

vehiculo:
	java -cp lib/jade.jar:out/production/ jade.Boot -local-host 127.0.0.1 -container elVehiculo:org.upc.edu.Behaviours.VehiculoAgent\("Que","pasa","tio"\) -local-host 127.0.0.1



all: compile

