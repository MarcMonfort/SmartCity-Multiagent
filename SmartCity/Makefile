#!/bin/sh

compile:
	javac -cp lib/jade.jar:lib/JenaLibs/* -d out/production/ -sourcepath src/ src/Agentes/*.java

gui:
	java -cp lib/jade.jar:lib/JenaLibs/*:out/production/ jade.Boot -gui -local-host 127.0.0.1


entorno:
	java -cp lib/jade.jar:lib/JenaLibs/*:out/production/ jade.Boot -local-host 127.0.0.1 -container entorno:Agentes.EntornoAgent\(\) -local-host 127.0.0.1


all: compile

