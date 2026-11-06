package conexiontester.modelo;

import java.util.Arrays;
import java.util.Optional;

/**
 * Motores de bases de datos que reconoce la aplicacion.
 * Cada motor sabe el inicio de su URL JDBC, el nombre de la clase de su driver
 * y un ejemplo de URL para ayudar al usuario.
 */
public enum TipoBaseDatos {

    MYSQL("MySQL", "jdbc:mysql:", "com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://localhost:3306/inventario"),
    MARIADB("MariaDB", "jdbc:mariadb:", "org.mariadb.jdbc.Driver",
            "jdbc:mariadb://localhost:3306/inventario"),
    POSTGRESQL("PostgreSQL", "jdbc:postgresql:", "org.postgresql.Driver",
            "jdbc:postgresql://localhost:5432/inventario"),
    SQL_SERVER("SQL Server", "jdbc:sqlserver:", "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "jdbc:sqlserver://localhost:1433;databaseName=inventario;encrypt=false"),
    SQLITE("SQLite", "jdbc:sqlite:", "org.sqlite.JDBC",
            "jdbc:sqlite:inventario.db");

    private final String nombre;
    private final String prefijoUrl;
    private final String claseDriver;
    private final String urlEjemplo;

    TipoBaseDatos(String nombre, String prefijoUrl, String claseDriver, String urlEjemplo) {
        this.nombre = nombre;
        this.prefijoUrl = prefijoUrl;
        this.claseDriver = claseDriver;
        this.urlEjemplo = urlEjemplo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getClaseDriver() {
        return claseDriver;
    }

    public String getUrlEjemplo() {
        return urlEjemplo;
    }

    /** SQLite trabaja con un archivo y no necesita usuario ni contrasena. */
    public boolean necesitaCredenciales() {
        return this != SQLITE;
    }

    /**
     * Busca el motor segun el inicio de la URL.
     * Ejemplo: "jdbc:mysql://..." devuelve MYSQL.
     */
    public static Optional<TipoBaseDatos> desdeUrl(String url) {
        String urlMinuscula = url.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(tipo -> urlMinuscula.startsWith(tipo.prefijoUrl))
                .findFirst();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
