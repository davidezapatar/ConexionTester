package conexiontester;

import conexiontester.modelo.ConfiguracionConexion;
import conexiontester.modelo.ResultadoPrueba;
import conexiontester.modelo.TipoBaseDatos;
import conexiontester.servicio.HistorialCsv;
import conexiontester.servicio.ValidadorConexion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

/**
 * Ventana principal: permite escribir la configuracion de una base de datos
 * y probar si la conexion funciona.
 *
 * Esta clase solo se encarga de la interfaz grafica (Swing). La conexion la
 * hace ValidadorConexion y el historial lo guarda HistorialCsv.
 */
public final class ConexionTester extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color VERDE = new Color(0, 128, 0);
    private static final Color ROJO = new Color(190, 0, 0);
    private static final Color NARANJA = new Color(200, 100, 0);
    private static final Font FUENTE_RESULTADO = new Font(Font.MONOSPACED, Font.PLAIN, 13);
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Componentes de la interfaz
    private final JComboBox<TipoBaseDatos> comboMotor = new JComboBox<>(TipoBaseDatos.values());
    private final JTextField campoUrl = new JTextField(35);
    private final JTextField campoUsuario = new JTextField(20);
    private final JPasswordField campoContrasena = new JPasswordField(20);
    private final JButton botonProbar = new JButton("Probar conexión");
    private final JButton botonLimpiar = new JButton("Limpiar");
    private final JTextArea areaResultado = new JTextArea(12, 60);
    private final JLabel etiquetaEstado = new JLabel();

    // Logica (separada de la interfaz)
    private final transient ValidadorConexion validador;
    private final transient HistorialCsv historial;

    public ConexionTester(ValidadorConexion validador, HistorialCsv historial) {
        super("Probador de conexión JDBC - IU Digital");
        this.validador = validador;
        this.historial = historial;
        construirInterfaz();
        registrarEventos();
        seleccionarMotor(TipoBaseDatos.MYSQL);
    }

    // ------------------------------------------------------------------
    // Construccion de la interfaz
    // ------------------------------------------------------------------

    private void construirInterfaz() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panelPrincipal.add(crearPanelEncabezado(), BorderLayout.NORTH);
        panelPrincipal.add(crearPanelCentral(), BorderLayout.CENTER);
        panelPrincipal.add(crearBarraEstado(), BorderLayout.SOUTH);

        setContentPane(panelPrincipal);
        pack();
        setMinimumSize(new Dimension(640, 520));
        setLocationRelativeTo(null);
    }

    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        JLabel titulo = new JLabel("Probador de conexión a bases de datos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        JLabel subtitulo = new JLabel("Escriba la configuración y presione \"Probar conexión\".");
        panel.add(titulo);
        panel.add(subtitulo);
        return panel;
    }

    /** Formulario arriba y resultado abajo. */
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(crearPanelFormulario(), BorderLayout.NORTH);
        panel.add(crearPanelResultado(), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Formulario: una columna de etiquetas (WEST) y una de campos (CENTER),
     * cada una con GridLayout para que queden alineadas fila por fila.
     */
    private JPanel crearPanelFormulario() {
        JPanel etiquetas = new JPanel(new GridLayout(4, 1, 5, 8));
        JPanel campos = new JPanel(new GridLayout(4, 1, 5, 8));

        agregarFila(etiquetas, campos, "Motor de base de datos:", comboMotor);
        agregarFila(etiquetas, campos, "URL de conexión:", campoUrl);
        agregarFila(etiquetas, campos, "Usuario:", campoUsuario);
        agregarFila(etiquetas, campos, "Contraseña:", campoContrasena);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botonProbar.setFont(botonProbar.getFont().deriveFont(Font.BOLD));
        botones.add(botonLimpiar);
        botones.add(botonProbar);

        JPanel formulario = new JPanel(new BorderLayout(10, 10));
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Configuración de la base de datos"),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        formulario.add(etiquetas, BorderLayout.WEST);
        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(botones, BorderLayout.SOUTH);

        // Nombres para poder ubicar los componentes en las pruebas automaticas
        comboMotor.setName("comboMotor");
        campoUrl.setName("campoUrl");
        campoUsuario.setName("campoUsuario");
        campoContrasena.setName("campoContrasena");
        botonProbar.setName("botonProbar");
        areaResultado.setName("areaResultado");
        return formulario;
    }

    private void agregarFila(JPanel etiquetas, JPanel campos, String texto, java.awt.Component campo) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setLabelFor(campo);
        etiquetas.add(etiqueta);
        campos.add(campo);
    }

    private JScrollPane crearPanelResultado() {
        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        areaResultado.setFont(FUENTE_RESULTADO);
        areaResultado.setMargin(new java.awt.Insets(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(areaResultado);
        scroll.setBorder(BorderFactory.createTitledBorder("Resultado de la prueba"));
        return scroll;
    }

    private JLabel crearBarraEstado() {
        etiquetaEstado.setText("Historial de pruebas: " + historial.getArchivo().getFileName());
        etiquetaEstado.setFont(etiquetaEstado.getFont().deriveFont(11f));
        return etiquetaEstado;
    }

    // ------------------------------------------------------------------
    // Eventos
    // ------------------------------------------------------------------

    private void registrarEventos() {
        // ActionListener del boton "Probar conexion"
        botonProbar.addActionListener(evento -> probarConexion());
        botonLimpiar.addActionListener(evento -> limpiarResultado());
        comboMotor.addActionListener(evento -> seleccionarMotor((TipoBaseDatos) comboMotor.getSelectedItem()));
        // Presionar Enter en cualquier campo tambien prueba la conexion
        getRootPane().setDefaultButton(botonProbar);
    }

    /** Cambia la URL de ejemplo y activa o desactiva usuario y contrasena (SQLite no los usa). */
    private void seleccionarMotor(TipoBaseDatos tipo) {
        if (comboMotor.getSelectedItem() != tipo) {
            comboMotor.setSelectedItem(tipo);
        }
        campoUrl.setText(tipo.getUrlEjemplo());
        boolean usaCredenciales = tipo.necesitaCredenciales();
        campoUsuario.setEnabled(usaCredenciales);
        campoContrasena.setEnabled(usaCredenciales);
        if (!usaCredenciales) {
            campoUsuario.setText("");
            campoContrasena.setText("");
        }
    }

    /** Lee los campos de texto y arma la configuracion (nada esta escrito fijo en el codigo). */
    private ConfiguracionConexion leerConfiguracion() {
        return new ConfiguracionConexion(
                campoUrl.getText(),
                campoUsuario.getText(),
                new String(campoContrasena.getPassword()));
    }

    /**
     * Ejecuta la prueba en segundo plano con SwingWorker, para que la ventana
     * no se congele mientras espera al servidor.
     */
    private void probarConexion() {
        ConfiguracionConexion configuracion = leerConfiguracion();
        botonProbar.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        areaResultado.setForeground(Color.DARK_GRAY);
        areaResultado.setText("Probando conexión con " + configuracion.url() + " ...");

        new SwingWorker<ResultadoPrueba, Void>() {
            @Override
            protected ResultadoPrueba doInBackground() {
                return validador.probarConexion(configuracion);
            }

            @Override
            protected void done() {
                try {
                    ResultadoPrueba resultado = get();
                    mostrarResultado(resultado);
                    guardarEnHistorial(configuracion, resultado);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    mostrarErrorInesperado(e.getCause());
                } finally {
                    botonProbar.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    /** Verde si la conexion funciono, rojo si fallo, naranja si faltan datos. */
    private void mostrarResultado(ResultadoPrueba resultado) {
        Color color = switch (resultado.tipoError()) {
            case NINGUNO -> VERDE;
            case DATOS_INVALIDOS -> NARANJA;
            case DRIVER_NO_ENCONTRADO, FALLO_CONEXION -> ROJO;
        };
        areaResultado.setForeground(color);
        areaResultado.setText("[" + resultado.fechaHora().format(FORMATO_HORA) + "] "
                + resultado.titulo() + "  (" + resultado.milisegundos() + " ms)\n\n"
                + resultado.detalle());
        areaResultado.setCaretPosition(0);
    }

    private void guardarEnHistorial(ConfiguracionConexion configuracion, ResultadoPrueba resultado) {
        try {
            historial.registrar(configuracion, resultado);
            etiquetaEstado.setText("Prueba guardada en: " + historial.getArchivo().getFileName());
        } catch (IOException e) {
            etiquetaEstado.setText("No se pudo guardar el historial: " + e.getMessage());
        }
    }

    private void mostrarErrorInesperado(Throwable causa) {
        areaResultado.setForeground(ROJO);
        areaResultado.setText("ERROR INESPERADO\n\n" + causa);
    }

    private void limpiarResultado() {
        areaResultado.setText("");
        campoContrasena.setText("");
    }

    // ------------------------------------------------------------------
    // Punto de entrada
    // ------------------------------------------------------------------

    public static void main(String[] args) {
        usarAparienciaNimbus();
        SwingUtilities.invokeLater(() -> {
            ConexionTester ventana = new ConexionTester(
                    new ValidadorConexion(),
                    new HistorialCsv(Path.of("historial_conexiones.csv")));
            ventana.setVisible(true);
        });
    }

    /** Usa el estilo "Nimbus" que trae Java; si no esta, deja el estilo por defecto. */
    private static void usarAparienciaNimbus() {
        for (UIManager.LookAndFeelInfo estilo : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(estilo.getName())) {
                try {
                    UIManager.setLookAndFeel(estilo.getClassName());
                } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
                    // Se queda con el estilo por defecto
                }
                return;
            }
        }
    }
}
