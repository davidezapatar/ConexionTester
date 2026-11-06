package conexiontester.modelo;

/**
 * Datos que escribe el usuario en la ventana para conectarse.
 * Es un record: sus valores no se pueden cambiar despues de creado.
 *
 * @param url        URL JDBC, por ejemplo jdbc:mysql://localhost:3306/inventario
 * @param usuario    usuario de la base de datos (vacio en SQLite)
 * @param contrasena contrasena del usuario (vacia en SQLite)
 */
public record ConfiguracionConexion(String url, String usuario, String contrasena) {

    public ConfiguracionConexion {
        url = url == null ? "" : url.trim();
        usuario = usuario == null ? "" : usuario.trim();
        contrasena = contrasena == null ? "" : contrasena;
    }

    public boolean tieneUsuario() {
        return !usuario.isEmpty();
    }

    /** Evita que la contrasena aparezca si alguien imprime el objeto. */
    @Override
    public String toString() {
        return "ConfiguracionConexion[url=" + url + ", usuario=" + usuario + ", contrasena=****]";
    }
}
