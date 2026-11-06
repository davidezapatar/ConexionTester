package conexiontester.modelo;

import java.time.LocalDateTime;

/**
 * Resultado de una prueba de conexion.
 *
 * @param exitosa      true si la conexion se pudo abrir
 * @param tipoError    NINGUNO, DATOS_INVALIDOS, DRIVER_NO_ENCONTRADO o FALLO_CONEXION
 * @param titulo       mensaje corto ("Conexion exitosa", "Fallo de conexion"...)
 * @param detalle      explicacion y mensaje tecnico del error (e.getMessage())
 * @param milisegundos tiempo que tardo la prueba
 * @param fechaHora    momento en que se hizo la prueba
 */
public record ResultadoPrueba(boolean exitosa,
                              TipoError tipoError,
                              String titulo,
                              String detalle,
                              long milisegundos,
                              LocalDateTime fechaHora) {

    /** Tipos de resultado posibles. */
    public enum TipoError {
        NINGUNO,
        DATOS_INVALIDOS,
        DRIVER_NO_ENCONTRADO,
        FALLO_CONEXION
    }

    public static ResultadoPrueba exito(String detalle, long milisegundos) {
        return new ResultadoPrueba(true, TipoError.NINGUNO, "CONEXIÓN EXITOSA", detalle,
                milisegundos, LocalDateTime.now());
    }

    public static ResultadoPrueba error(TipoError tipo, String titulo, String detalle, long milisegundos) {
        return new ResultadoPrueba(false, tipo, titulo, detalle, milisegundos, LocalDateTime.now());
    }
}
