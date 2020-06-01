mkdir out
mkdir out\production

javac -cp lib/jade.jar -d out/production/ -sourcepath src/ src/org/upc/edu/Behaviours/*.java
javac -cp lib/jade.jar -d out/production/ -sourcepath src/ src/org/upc/edu/AIAExamples/communication/*.java
javac -cp lib/jade.jar -d out/production/ -sourcepath src/ src/org/upc/edu/Protocols/*.java
