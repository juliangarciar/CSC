echo "Compilando servidor..."
mkdir -p out
rm out/*.class
javac -d out *.java
jar cvfm Server.jar manifest.mf out/*.class
echo "Compilado"