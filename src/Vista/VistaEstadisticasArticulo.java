package Vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;

public class VistaEstadisticasArticulo extends javax.swing.JFrame {

    // Paleta Bonhomía
    private java.awt.Color colorPietras = new java.awt.Color(244, 119, 21);
    private final Color COLOR_COBRIZO = new Color(244, 119, 21);
    private final Color COLOR_TEXTO = new Color(73, 59, 42);
    private final Color COLOR_FONDO_TARJETA = new Color(250, 248, 245);
    private final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 16);
    private final Font FONT_VALOR = new Font("Segoe UI", Font.BOLD, 22);

    public VistaEstadisticasArticulo() {
        initComponents(); // 1. Crea los objetos
        personalizarDiseño(); // 2. Les da el estilo Bonhomía
        setTitle("Análisis 360 de Artículos");
        setSize(1350, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        // Inicialización de componentes (Para que no sean NULL)
        txtBusquedaArticulo = new javax.swing.JTextField();
        lblStockValor = new javax.swing.JLabel("0");
        lblVentasValor = new javax.swing.JLabel("0");
        lblUltimaFecha = new javax.swing.JLabel("-");
        lblNombreProducto = new javax.swing.JLabel("Seleccione un producto");
        lblCodigoProducto = new javax.swing.JLabel("Cód: ---");
        
        tableHistorial = new javax.swing.JTable();
        tableHistorial.setModel(new DefaultTableModel(
            new Object [][] {},
            new String [] {"Fecha", "Movimiento", "Cant.", "Motivo"}
        ));
        JScrollPane scrollTable = new JScrollPane(tableHistorial);

        panelStock = new javax.swing.JPanel();
        panelVentas = new javax.swing.JPanel();
        panelUltimoMov = new javax.swing.JPanel();
        panelDetalleProd = new javax.swing.JPanel();
        
        // Layout Principal
        setLayout(new BorderLayout(10, 10));

        // --- PANEL NORTE (Buscador) ---
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panelNorte.setBackground(Color.WHITE);
        panelNorte.add(new JLabel("Buscar Producto (Nombre/Cód/ID):"));
        txtBusquedaArticulo.setPreferredSize(new Dimension(150, 30));
        
         //--- FILTRO POR FECHAS ---
        jdDesde = new JDateChooser();
        jdDesde.setPreferredSize(new Dimension(150, 30));
        jdHasta = new JDateChooser();
        jdHasta.setPreferredSize(new Dimension(150, 30));
        btnFiltrar = new JButton("Filtrar");
        cbTipoFiltro = new JComboBox<>(new String[]{"TODOS", "VENTA", "CARGA INICIAL", "AJUSTE DE STOCK"});
        panelNorte.add(new JLabel("Tipo:"));
        panelNorte.add(cbTipoFiltro);
        
        panelNorte.add(txtBusquedaArticulo);
        panelNorte.add(lblNombreProducto);
        panelNorte.add(new JLabel("Desde:"));
        panelNorte.add(jdDesde);
        panelNorte.add(new JLabel("Hasta:"));
        panelNorte.add(jdHasta);
        panelNorte.add(btnFiltrar);
       
        
        add(panelNorte, BorderLayout.NORTH);

        // --- PANEL CENTRAL (KPIs y Tabla) ---
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBackground(Color.WHITE);

        // Subpanel de Tarjetas (KPIs)
        JPanel panelKPIs = new JPanel(new GridLayout(1, 3, 15, 0));
        panelKPIs.setBackground(Color.WHITE);
        
        panelKPIs.add(crearTarjeta(panelStock, "STOCK ACTUAL", lblStockValor));
        panelKPIs.add(crearTarjeta(panelVentas, "TOTAL VENDIDO", lblVentasValor));
        panelKPIs.add(crearTarjeta(panelUltimoMov, "ÚLTIMO MOV.", lblUltimaFecha));
        
        panelCentral.add(panelKPIs, BorderLayout.NORTH);
        panelCentral.add(scrollTable, BorderLayout.CENTER);
        
        add(panelCentral, BorderLayout.CENTER);

        // --- PANEL OESTE (Detalle lateral) ---
        panelDetalleProd.setPreferredSize(new Dimension(250, 0));
        panelDetalleProd.setLayout(new GridLayout(10, 1, 5, 5));
        panelDetalleProd.add(new JLabel("Detalles Técnicos:"));
        panelDetalleProd.add(lblCodigoProducto);
        
        add(panelDetalleProd, BorderLayout.WEST);
        
        // --- PANEL SUR (Totales) ---
        JPanel panelSur = new JPanel(new GridLayout(1, 3, 20, 0));
        panelSur.setBackground(COLOR_FONDO_TARJETA);
        panelSur.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        lblTotalVentas = new JLabel("Ventas: 0");
        lblTotalEntradas = new JLabel("Ingresos: 0");
        lblTotalAjustesE = new JLabel("Ajustes: 0");
        lblTotalAjusteS = new JLabel("Ajustes: 0");

        // Estilo para que resalten
        Font fTotales = new Font("Segoe UI", Font.BOLD, 16);
        lblTotalVentas.setFont(fTotales);
        lblTotalEntradas.setFont(fTotales);
        lblTotalAjustesE.setFont(fTotales);
        lblTotalAjusteS.setFont(fTotales);

        panelSur.add(lblTotalVentas);
        panelSur.add(lblTotalEntradas);
        panelSur.add(lblTotalAjustesE);
        panelSur.add(lblTotalAjusteS);

        add(panelSur, BorderLayout.SOUTH);
    }

    private JPanel crearTarjeta(JPanel panel, String titulo, JLabel valor) {
        panel.setLayout(new BorderLayout());
        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valor.setFont(FONT_VALOR);
        valor.setHorizontalAlignment(SwingConstants.CENTER);
        valor.setForeground(COLOR_COBRIZO);
        
        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(valor, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        return panel;
    }

    private void personalizarDiseño() {
        this.getContentPane().setBackground(Color.WHITE);
        
        panelStock.setBackground(COLOR_FONDO_TAR_VERIFICA());
        panelVentas.setBackground(COLOR_FONDO_TAR_VERIFICA());
        panelUltimoMov.setBackground(COLOR_FONDO_TAR_VERIFICA());
        
        panelDetalleProd.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_COBRIZO, 1, true), 
                "Información", 0, 0, FONT_TITULO, COLOR_COBRIZO));
        panelDetalleProd.setBackground(Color.WHITE);

        tableHistorial.setRowHeight(35);
        tableHistorial.getTableHeader().setBackground(COLOR_COBRIZO);
        tableHistorial.getTableHeader().setForeground(Color.WHITE);
        tableHistorial.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHistorial.setSelectionBackground(new Color(245, 240, 230));
    }

    private Color COLOR_FONDO_TAR_VERIFICA() {
        return new Color(250, 248, 245);
    }

    // Variables Públicas para el Controlador
    public javax.swing.JTextField txtBusquedaArticulo;
    public javax.swing.JLabel lblStockValor;
    public javax.swing.JLabel lblVentasValor;
    public javax.swing.JLabel lblUltimaFecha;
    public javax.swing.JLabel lblNombreProducto;
    public javax.swing.JLabel lblCodigoProducto;
    public javax.swing.JTable tableHistorial;
    public JDateChooser jdDesde;
    public JDateChooser jdHasta;
    public JButton btnFiltrar;
    public JComboBox<String> cbTipoFiltro;
    public JLabel lblTotalVentas, lblTotalEntradas, lblTotalAjustesE, lblTotalAjusteS;

    // Paneles privados
    private javax.swing.JPanel panelStock;
    private javax.swing.JPanel panelVentas;
    private javax.swing.JPanel panelUltimoMov;
    private javax.swing.JPanel panelDetalleProd;
}