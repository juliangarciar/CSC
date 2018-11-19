#!/bin/sh
# This is a comment!
echo 'Compilando ficheros...'
javac ./src/*.java -d ./bin/
echo 'Iniciando aplicacion...'
java -cp ./bin/ Main