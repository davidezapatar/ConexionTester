package conexiontester.servicio;

import conexiontester.modelo.ConfiguracionConexion;
import conexiontester.modelo.ResultadoPrueba;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Guarda cada prueba de conexion en un archivo CSV (historial_conexiones.csv).
 * La contrasena NUNCA se guarda.
 */
public class HistorialCsv {

    static final String ENCABEZADO = "fecha_hora;url;usuario;resultado;tipo_error;tiempo_ms;mensaje";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path archivo;

    public HistorialCsv(Path archivo) {
        this.archivo = archivo;
    }

    public Path getArchivo() {
        return archivo;
    }

    /**
     * Agrega una fila al CSV. Si el archivo no existe, primero escribe el encabezado.
     *
     * @throws IOException si no se puede escribir el archivo
     */
    public void registrar(ConfiguracionConexion configuracion, ResultadoPrueba resultado) throws IOException {
        if (Files.notExists(archivo)) {
            // El BOM al inicio hace que Excel muestre bien las tildes
            Files.writeString(archivo, "﻿" + ENCABEZADO + System.lineSeparator(), StandardCharsets.UTF_8);
        }
        String fila = String.join(";",
                resultado.fechaHora().format(FORMATO_FECHA),
                limpiar(configuracion.url()),
                limpiar(configuracion.usuario()),
                resultado.exitosa() ? "EXITOSA" : "FALLIDA",
                resultado.tipoError().name(),
                String.valueOf(resultado.milisegundos()),
                limpiar(resultado.detalle().replace("\n\n", "\n").replace("\n", " | ")));
        Files.write(archivo, List.of(fila), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    /** Pone el texto entre comillas si tiene punto y coma, comillas o saltos de linea. */
    static String limpiar(String texto) {
        String sinSaltos = texto.replace("\r", " ").replace("\n", " ");
        if (sinSaltos.contains(";") || sinSaltos.contains("\"")) {
            return "\"" + sinSaltos.replace("\"", "\"\"") + "\"";
        }
        return sinSaltos;
    }
}
