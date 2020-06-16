mkdir out
mkdir out\production

javac -cp lib/jade.jar:lib/JenaLibs/* -d out/production/ -sourcepath src/ src/Agentes/*.java
