package conexiontester.servicio;

import conexiontester.modelo.ConfiguracionConexion;
import conexiontester.modelo.ResultadoPrueba;
import conexiontester.modelo.ResultadoPrueba.TipoError;
import conexiontester.modelo.TipoBaseDatos;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Logica de conexion a la base de datos, separada de la interfaz grafica.
 *
 * Esta clase no sabe nada de Swing: recibe una ConfiguracionConexion y
 * devuelve un ResultadoPrueba. Por eso se puede usar desde la ventana,
 * desde la consola o desde una prueba.
 */
public class ValidadorConexion {

    /** Segundos maximos que se espera al servidor antes de rendirse. */
    private static final int TIEMPO_ESPERA_SEGUNDOS = 5;

    /**
     * Intenta abrir una conexion con los datos recibidos y la cierra enseguida.
     *
     * @param configuracion URL, usuario y contrasena escritos por el usuario
     * @return el resultado de la prueba (exito o el tipo de error con su mensaje)
     */
    public ResultadoPrueba probarConexion(ConfiguracionConexion configuracion) {
        long inicio = System.currentTimeMillis();

        // 1. Revisar que los datos tengan sentido antes de intentar conectar
        Optional<String> errorDatos = validarDatos(configuracion);
        if (errorDatos.isPresent()) {
            return ResultadoPrueba.error(TipoError.DATOS_INVALIDOS, "DATOS INCOMPLETOS",
                    errorDatos.get(), calcularTiempo(inicio));
        }

        Optional<TipoBaseDatos> tipo = TipoBaseDatos.desdeUrl(configuracion.url());

        try {
            // 2. Cargar el driver JDBC. Si el .jar no esta en la carpeta lib,
            //    Java lanza ClassNotFoundException.
            if (tipo.isPresent()) {
                Class.forName(tipo.get().getClaseDriver());
            }

            // 3. Abrir la conexion con try-with-resources: Java la cierra
            //    automaticamente al salir del bloque, salga bien o salga mal.
            DriverManager.setLoginTimeout(TIEMPO_ESPERA_SEGUNDOS);
            try (Connection conexion = abrirConexion(configuracion)) {
                String detalle = describirConexion(conexion, tipo);
                return ResultadoPrueba.exito(detalle, calcularTiempo(inicio));
            }

        } catch (ClassNotFoundException e) {
            String motor = tipo.map(TipoBaseDatos::getNombre).orElse("desconocido");
            String detalle = "No se encontró el driver JDBC de " + motor + ".\n"
                    + "Copie el archivo .jar del driver en la carpeta 'lib' del proyecto y vuelva a ejecutar el programa.\n\n"
                    + "Mensaje técnico: " + e.getMessage();
            return ResultadoPrueba.error(TipoError.DRIVER_NO_ENCONTRADO, "FALLO DE CONEXIÓN: DRIVER NO ENCONTRADO",
                    detalle, calcularTiempo(inicio));

        } catch (SQLException e) {
            String detalle = explicarError(e) + "\n\n"
                    + "Mensaje técnico: " + e.getMessage() + "\n"
                    + "SQLState: " + e.getSQLState() + " | Código de error: " + e.getErrorCode();
            return ResultadoPrueba.error(TipoError.FALLO_CONEXION, "FALLO DE CONEXIÓN",
                    detalle, calcularTiempo(inicio));
        }
    }

    /** Revisa los datos escritos. Devuelve el problema encontrado, o vacio si todo esta bien. */
    Optional<String> validarDatos(ConfiguracionConexion configuracion) {
        if (configuracion.url().isEmpty()) {
            return Optional.of("Escriba la URL de conexión. Ejemplo: jdbc:mysql://localhost:3306/inventario");
        }
        if (!configuracion.url().toLowerCase().startsWith("jdbc:")) {
            return Optional.of("La URL debe empezar por 'jdbc:'. Ejemplo: jdbc:mysql://localhost:3306/inventario");
        }
        return Optional.empty();
    }

    /** SQLite no usa usuario: en ese caso se llama a getConnection solo con la URL. */
    private Connection abrirConexion(ConfiguracionConexion configuracion) throws SQLException {
        if (configuracion.tieneUsuario()) {
            return DriverManager.getConnection(configuracion.url(), configuracion.usuario(),
                    configuracion.contrasena());
        }
        return DriverManager.getConnection(configuracion.url());
    }

    /** Arma el texto con la informacion del servidor al que se conecto. */
    private String describirConexion(Connection conexion, Optional<TipoBaseDatos> tipo) throws SQLException {
        DatabaseMetaData datos = conexion.getMetaData();
        return "Motor detectado: " + tipo.map(TipoBaseDatos::getNombre).orElse("otro") + "\n"
                + "Servidor: " + datos.getDatabaseProductName() + " " + datos.getDatabaseProductVersion() + "\n"
                + "Driver: " + datos.getDriverName() + " " + datos.getDriverVersion() + "\n"
                + "Usuario conectado: " + valorOGuion(datos.getUserName()) + "\n"
                + "Conexión válida: " + (conexion.isValid(TIEMPO_ESPERA_SEGUNDOS) ? "sí" : "no") + "\n"
                + "La conexión se cerró automáticamente (try-with-resources).";
    }

    /** Traduce los errores mas comunes a un mensaje facil de entender. */
    String explicarError(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String mensaje = e.getMessage() == null ? "" : e.getMessage().toLowerCase();

        if (mensaje.contains("no suitable driver")) {
            return "La URL no corresponde a ningún driver cargado. Revise que esté bien escrita.";
        }
        if (e.getErrorCode() == 1049 || mensaje.contains("unknown database")) {
            return "El servidor respondió, pero la base de datos no existe.";
        }
        if (e.getErrorCode() == 1044) {
            return "El usuario y la contraseña son correctos, pero ese usuario no tiene permiso "
                    + "sobre esa base de datos (o la base de datos no existe).";
        }
        if (estado.startsWith("28") || e.getErrorCode() == 1045 || mensaje.contains("access denied")) {
            return "El usuario o la contraseña son incorrectos.";
        }
        if (estado.startsWith("08") || mensaje.contains("communications link failure")
                || mensaje.contains("connection refused") || mensaje.contains("could not connect")) {
            return "No se pudo llegar al servidor. Revise que esté encendido y que el host y el puerto sean correctos.";
        }
        return "La base de datos rechazó la conexión.";
    }

    private static String valorOGuion(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    private static long calcularTiempo(long inicio) {
        return System.currentTimeMillis() - inicio;
    }
}
