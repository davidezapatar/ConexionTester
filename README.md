# ConexionTester – Probador de conexión JDBC con Swing

Aplicación de escritorio en **Java (Swing + JDBC, sin frameworks)** que permite escribir la configuración de una base de datos (URL, usuario y contraseña), probar la conexión y ver el resultado: un mensaje **verde** si la conexión es exitosa o un mensaje **rojo** con el error técnico si falla.

**Actividad:** EA3. Incorporando UI y BD – Programación Orientada a Objetos II
**Institución:** Institución Universitaria Digital de Antioquia
**Autor:** David Esteban Zapata Rodríguez – Grupo 10
**Docente:** Jorge Armando Julio

---

## Qué hace

- Ventana `JFrame` con `JPanel`, `BorderLayout` y `GridLayout`.
- Campos para la URL (`JTextField`), el usuario (`JTextField`) y la contraseña (`JPasswordField`), y una lista para escoger el motor, que llena una URL de ejemplo.
- Botón **"Probar conexión"** con su `ActionListener`.
- Resultado en un `JTextArea` dentro de un `JScrollPane`.
- Conexión con `DriverManager.getConnection()` dentro de un **try-with-resources**, que cierra la conexión siempre.
- Captura de `ClassNotFoundException` (falta el driver) y de `SQLException` (servidor apagado, clave incorrecta, base de datos inexistente...), mostrando `e.getMessage()`, el SQLState y el código de error.
- Cada prueba se guarda en `historial_conexiones.csv`. La contraseña nunca se guarda.

## Cómo ejecutarlo

Requisitos: **JDK 17 o superior** y el **driver JDBC** del motor que se quiera probar (ver `lib/LEEME.txt`).

1. Descargar el driver de MySQL (Connector/J, "Platform Independent") desde https://dev.mysql.com/downloads/connector/j/ y copiar el `.jar` en la carpeta `lib`.
2. **Windows:** doble clic en `ejecutar.bat`. **Linux/Mac:** `./ejecutar.sh`.
3. Escribir, por ejemplo:
   - URL: `jdbc:mysql://localhost:3306/inventario`
   - Usuario: `root`
   - Contraseña: la de su MySQL
4. Presionar **Probar conexión**.

A mano:

```
javac -encoding UTF-8 -d out src/conexiontester/*.java src/conexiontester/*/*.java
java -cp "out;lib/*" conexiontester.ConexionTester      (Windows)
java -cp "out:lib/*" conexiontester.ConexionTester      (Linux/Mac)
```

## Estructura

```
src/conexiontester/
├── ConexionTester.java          Ventana (JFrame) y eventos. Solo interfaz gráfica.
├── modelo/
│   ├── ConfiguracionConexion.java   URL, usuario y contraseña (record)
│   ├── ResultadoPrueba.java         Resultado de la prueba (record)
│   └── TipoBaseDatos.java           Motores reconocidos y su driver (enum)
└── servicio/
    ├── ValidadorConexion.java       Lógica JDBC: DriverManager, try-with-resources, excepciones
    └── HistorialCsv.java            Guarda cada prueba en un archivo CSV
lib/                                 Drivers JDBC (.jar)
docs/                                Informe PDF, historial de pruebas y código fuente en CSV
```
