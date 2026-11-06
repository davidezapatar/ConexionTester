@echo off
REM ============================================================
REM  Probador de conexion JDBC - compilar y ejecutar en Windows
REM  Requisito: JDK 17 o superior.
REM  El driver JDBC (.jar) debe estar en la carpeta lib.
REM ============================================================
cd /d "%~dp0"
if exist out rmdir /s /q out
echo Compilando...
javac -encoding UTF-8 -d out src\conexiontester\*.java src\conexiontester\modelo\*.java src\conexiontester\servicio\*.java
if errorlevel 1 (
  echo Error al compilar. Revise que Java este instalado: javac -version
  pause
  exit /b 1
)
echo Abriendo la ventana...
java -cp "out;lib\*" conexiontester.ConexionTester
