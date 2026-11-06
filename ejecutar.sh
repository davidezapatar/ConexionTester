#!/bin/sh
# Probador de conexion JDBC - compilar y ejecutar en Linux o Mac
cd "$(dirname "$0")"
rm -rf out
javac -encoding UTF-8 -d out $(find src -name "*.java") || exit 1
java -cp "out:lib/*" conexiontester.ConexionTester
