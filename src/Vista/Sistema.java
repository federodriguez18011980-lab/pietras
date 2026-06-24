
package Vista;

import Conexion.ConexionMysql;
import Controlador.ClienteController;
import Modelo.ButtonTabComponent;
import Modelo.ExportarStock;
import Modelo.Caja;
import Modelo.CajaAperturaDao;
import Modelo.CajaDao;
import Modelo.CajaMovimiento;
import Modelo.CajaMovimientoDao;
import Modelo.Categoria;
import Modelo.CategoriaDao;
import Modelo.Cliente;
import Modelo.ClienteDao;
import Modelo.ColorDao;
import Modelo.Config;
import Modelo.ConfigDao;
import Modelo.CuentaCorriente;
import Modelo.CuentaCorrienteDao;
import Modelo.Detalle;
import Modelo.Eventos;
import Modelo.Item;
import Modelo.Loguearse;
import Modelo.Material;
import Modelo.Piedra;
import Modelo.Productos;
import Modelo.ProductosDao;
import Modelo.Proveedor;
import Modelo.ProveedorDao;
import Modelo.TalleDao;
import Modelo.Venta;
import Modelo.VentaDao;
import Modelo.WooOrder;
import Reportes.Excel;
import Servicio.WooCommerce.SincronizadorWooCommerce;
import Servicios.BackupService;
import Servicios.ListadosService;
import Util.GeneradorCodigoBarrasView;
import Util.ListaProveedores;
import Util.ListaUsuariosView;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import java.awt.Desktop;
import java.math.RoundingMode;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JOptionPane;
import java.util.List;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;



public class Sistema extends javax.swing.JFrame {
    VistaClientesProfesional VCP = new VistaClientesProfesional();
    Date fechaVenta = new Date();
    String fechaActual = new SimpleDateFormat("yyyy-MM-dd").format(fechaVenta); 
    Cliente cl = new Cliente();
    ClienteDao cld = new ClienteDao();
    ClienteDao clDao = new ClienteDao();
    DefaultTableModel modelo = new DefaultTableModel();
    DefaultTableModel Tmp = new DefaultTableModel();
    Productos pro = new Productos();
    ProductosDao prodao = new ProductosDao();
    Proveedor pr = new Proveedor();
    ProveedorDao prDao = new ProveedorDao();
    Config conf = new Config();
    ConfigDao ConfDao = new ConfigDao();
    Venta V = new Venta();
    VentaDao Vdao = new VentaDao();
    Detalle Dt = new Detalle();
    Eventos event =new Eventos();
    Caja caja = new Caja();
    CajaDao cajadao = new CajaDao();
    ExportarStock Ex = new ExportarStock();
    CategoriaDao categoriaDao = new CategoriaDao();
    TalleDao talleDao = new TalleDao();
    ColorDao colorDao = new ColorDao();
    ClienteController ctrl = new ClienteController(cl, clDao, VCP);
    SincronizadorWooCommerce Sinc = new SincronizadorWooCommerce();

    int item; 
    private BigDecimal totalpagar = BigDecimal.ZERO;
    private BigDecimal totalEfectivo = BigDecimal.ZERO;
    private BigDecimal totalTransferencia = BigDecimal.ZERO;
    private BigDecimal totalCredito = BigDecimal.ZERO;
    private BigDecimal precioDolar = BigDecimal.ZERO;
    private ConexionMysql cn = new ConexionMysql();
    private java.awt.Color colorPietras = new java.awt.Color(244, 119, 21);
   
    String usuarioLog;
    private final java.util.Set<Integer> pedidosNotificados = new java.util.HashSet<>();
    private javax.swing.JMenuItem jMenuItemPedidosWeb;
    
    public Sistema() {
        initComponents();
        
        jTabbedPane1.setSelectedIndex(7);
        cargarComboMateriales();
        
        
    }
    
    public Sistema (Loguearse priv){
        
        initComponents();
        
        // En el constructor de tu clase Sistema
        
        
        // Suponiendo que tu JTextField se llama txtDolar
        java.text.DecimalFormatSymbols simbolos = java.text.DecimalFormatSymbols.getInstance();
        char separadorDecimal = simbolos.getDecimalSeparator(); // Detecta si el sistema usa punto o coma

        ((javax.swing.text.AbstractDocument) txtPrecioDolar.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                String text = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevoTexto = text.substring(0, offset) + string + text.substring(offset);
                if (esValido(nuevoTexto)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                String documentoText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevoTexto = documentoText.substring(0, offset) + text + documentoText.substring(offset + length);
                if (esValido(nuevoTexto)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            // Expresión regular que permite números enteros o decimales con un solo separador
            private boolean esValido(String texto) {
                if (texto.isEmpty()) {
                    return true;
                }
                // Permite números y acepta tanto puntos como comas según la configuración regional
                return texto.matches("^\\d*[" + separadorDecimal + "]?\\d*$") || texto.matches("^\\d*\\.?\\d*$");
            }
        });
        
        
        
        
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                realizarBackupAutomatico();
            }
        });
        
        
        
        // En el constructor de Sistema.java
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int pregunta = JOptionPane.showConfirmDialog(null,
                        "¿Desea cerrar el sistema?\nSe realizará un backup de seguridad.",
                        "Confirmar Salida", JOptionPane.YES_NO_OPTION);

                if (pregunta == JOptionPane.YES_OPTION) {
                    salirDelSistema(); // <--- Llamamos al método maestro
                }
            }
        });
        
        txtBuscarProducto.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrarTabla(txtBuscarProducto.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarTabla(txtBuscarProducto.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarTabla(txtBuscarProducto.getText());
            }
        });
        
        txtCodigoProVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_F1) {
                    VentaBuscarProducto ventana = new VentaBuscarProducto(Sistema.this);
                    ventana.setVisible(true);
                    Productos p = ventana.getProductoSeleccionado();
                    if (p != null) {
                        txtCodigoProVenta.setText(p.getCodigo());
                        txtDescripcionVenta.setText(p.getDescripcion());
                        txtCantidadVenta.setText("1");
                        txtPrecioVenta.setText(String.valueOf(p.getPrecio()));
                        txtStockVenta.setText(String.valueOf(p.getStock()));

                    } else {
                        JOptionPane.showMessageDialog(null, "Ningun Producto Seleccionado");
                    }
                }
            }
        });

        
         
        bloquearPestañas();
        cargarCategorias();
//        cargarTalles();
//        cargarColores();
       
        jTabbedPane1.setSelectedIndex(7);
        this.setLocationRelativeTo(null);
        txtIdCliente.setVisible(false);
        txtIdProdNV.setVisible(false);
        txtIdProveedor.setVisible(false);
        txtIdProd.setVisible(false);
        
        txtIdConfig.setVisible(false);
        txtIdVentas.setVisible(false);
        txtRazonCV.setVisible(false);
        txtDireccionCV.setVisible(false);
        txtTelefonoCV.setVisible(false);
        AutoCompleteDecorator.decorate(bxProveedor);
        prodao.ConsultarProveedor(bxProveedor);
        AutoCompleteDecorator.decorate(cbxUsuarios);
        Vdao.ConsultarUsuario(cbxUsuarios);
        cbxUsuarios.setSelectedItem(null);
        buttonGroup1.add(radioCredito);
        buttonGroup1.add(radioDebito);
        buttonGroup1.add(radioEfectivo);
        grupoFiltrar.add(radioUsuarios);
        grupoFiltrar.add(radioVentas);
        String totalCaja = String.valueOf(cajadao.CargarTxtMontoCaja());
        txtEfectivoCaja.setText(totalCaja);
        //btnCategorias.setEnabled(false);
        //btnExportar.setEnabled(false);
        usuarioLog = priv.getNombre();
        DecimalFormat df = new DecimalFormat("#,##0.00");
        txtPrecioDolar.setText(df.format(prodao.obtenerUltimoValorDolar()));
        
        ListarConfig();
        
        if(priv.getRol().equals("Vendedor")){
            
            
//            btnProductos.setEnabled(false);
//            btnproveedor.setEnabled(false);
           // btnRegistrar.setEnabled(false);
            btnRetirarEfectivo.setEnabled(false);
            jMenuItemProductosCargar.setEnabled(false);
            txtRetirarEfectivo.setEnabled(false);
            jLabelVendedor.setText(priv.getNombre());
            jMenuItemParametros.setEnabled(false);
            
            
            for (int i = 0; i < jTabbedPane1.getTabCount(); i++) {
                jTabbedPane1.setEnabledAt(i, false);
            }
        
        }else{jLabelVendedor.setText(priv.getNombre());
            
        }
        new Servicios.ActualizadorService(this).verificarVersion();
        
        // Inicializar jMenuItemPedidosWeb programáticamente
        jMenuItemPedidosWeb = new javax.swing.JMenuItem();
        jMenuItemPedidosWeb.setText("Pedidos WooCommerce");
        try {
            jMenuItemPedidosWeb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Carrito-de-compras.png")));
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono del carrito: " + e.getMessage());
        }
        jMenuItemPedidosWeb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new PedidosWebView().setVisible(true);
            }
        });
        jMenuVentas.add(jMenuItemPedidosWeb);

        iniciarMonitorPedidosWeb();
    }
    
    public void ListarCliente() {
        List<Cliente> ListarCL = cld.ListarClientes();
        modelo = (DefaultTableModel) tablaClientes.getModel();
        Object[] obj = new Object[7];

        for (int i = 0; i < ListarCL.size(); i++) {
            obj[0] = ListarCL.get(i).getId();
            obj[1] = ListarCL.get(i).getNombre();
            obj[2] = ListarCL.get(i).getDni();
            obj[3] = ListarCL.get(i).getDomicilio();
            obj[4] = ListarCL.get(i).getTelefono();
            obj[5] = ListarCL.get(i).getEmail();
            obj[6] = ListarCL.get(i).getRazon();
            modelo.addRow(obj);
        }

        tablaClientes.setModel(modelo);

    }

    public void ListarProveedor() {
        List<Proveedor> ListaPr = prDao.ListarProvedor();
        modelo = (DefaultTableModel) tablaProveedores.getModel();
        Object[] obj = new Object[6];

        for (int i = 0; i < ListaPr.size(); i++) {
            obj[0] = ListaPr.get(i).getId();
            obj[1] = ListaPr.get(i).getCuit();
            obj[2] = ListaPr.get(i).getNombre();
            obj[3] = ListaPr.get(i).getTelefono();
            obj[4] = ListaPr.get(i).getDomicilio();
            obj[5] = ListaPr.get(i).getEmail();

            modelo.addRow(obj);
        }

        tablaProveedores.setModel(modelo);

    }

    
    public void ListarProductos() {
    List<Productos> ListaProd = prodao.ListarProductos();

    modelo = (DefaultTableModel) tablaProductos.getModel();
    modelo.setRowCount(0); // limpiar tabla

    Object[] obj = new Object[12];

    for (int i = 0; i < ListaProd.size(); i++) {
        obj[0] = ListaProd.get(i).getId();
        obj[1] = ListaProd.get(i).getCodigo();
        obj[2] = ListaProd.get(i).getDescripcion();
        obj[3] = ListaProd.get(i).getStock();
        obj[4] = ListaProd.get(i).getCategoriaNombre();        // ← nombre
        obj[5] = ListaProd.get(i).getPrecioCompra();
        obj[6] = ListaProd.get(i).getPrecio();
        obj[7] = ListaProd.get(i).getProveedor();
        obj[8] = ListaProd.get(i).getMaterial();
        obj[9] = ListaProd.get(i).getPiedra();
        obj[10]= ListaProd.get(i).getPrecioxdolar();
        obj[11]= ListaProd.get(i).getWeb();
        modelo.addRow(obj);
    }

    tablaProductos.setModel(modelo);
}

    
    public void ListarCajasSistema() {
        List<Caja> ListacajasSistema = cajadao.ListarCaja();

        modelo = (DefaultTableModel) tablaCaja.getModel();
        Object[] obj = new Object[6];

        for (int i = 0; i < ListacajasSistema.size(); i++) {
            obj[0] = ListacajasSistema.get(i).getId();
            obj[1] = ListacajasSistema.get(i).getEntrada();
            obj[2] = ListacajasSistema.get(i).getSalida();
            obj[3] = ListacajasSistema.get(i).getMonto();
            obj[4] = ListacajasSistema.get(i).getFecha();
            obj[5] = ListacajasSistema.get(i).getUsuario();

            modelo.addRow(obj);
        }

        tablaCaja.setModel(modelo);

    }

    public void ListarConfig() {
    conf = prodao.BuscarDatos();

    txtIdConfig.setText(String.valueOf(conf.getId()));
    txtNombreCongig.setText(conf.getNombre());
    txtCuitConfig.setText(conf.getCuit());
    txtTelefonoConfig.setText(conf.getTelefono());
    txtDomicilioConfig.setText(conf.getDireccion());
    txtDescuentoEfectivo.setText(conf.getEfectivo().setScale(2, RoundingMode.HALF_UP).toPlainString());
    txtDescuentoTransferencia.setText(conf.getTransferencia().setScale(2, RoundingMode.HALF_UP).toPlainString());
    txtDescuentoCredito.setText(conf.getCredito().setScale(2, RoundingMode.HALF_UP).toPlainString());
}


    public void LimpiarTabla() {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            modelo.removeRow(i);
            i = i - 1;
        }
    }

    public void ListarVentas() {
        List<Venta> ListaVenta = Vdao.ListarVentas();

        modelo = (DefaultTableModel) tablaVentas.getModel();
        Object[] obj = new Object[6];

        for (int i = 0; i < ListaVenta.size(); i++) {
            obj[0] = ListaVenta.get(i).getId();
            obj[1] = ListaVenta.get(i).getCliente();
            obj[2] = ListaVenta.get(i).getVendedor();
            obj[3] = ListaVenta.get(i).getTotal();
            obj[4] = ListaVenta.get(i).getFecha();
            obj[5] = ListaVenta.get(i).getPago();
            modelo.addRow(obj);
        }

        tablaVentas.setModel(modelo);

    }

    public void FiltrarVentas(String valor) {
        LimpiarTabla();
        Connection con;
        Conexion.ConexionMysql cn;
        cn = new ConexionMysql();
        PreparedStatement ps;
        ResultSet rs;

        List<Venta> ListaVentas = new ArrayList();

        String sql = "SELECT * FROM ventas WHERE vendedor LIKE '%" + valor + "%'";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Venta ven = new Venta();

                ven.setId(rs.getInt("id"));
                ven.setCliente(rs.getString("cliente"));
                ven.setVendedor(rs.getString("vendedor"));
                ven.setTotal(rs.getBigDecimal("total"));
                ven.setFecha(rs.getString("fecha"));

                ListaVentas.add(ven);

            }

            //List <Venta> ListaVenta = Vdao.ListarVentas();
            modelo = (DefaultTableModel) tablaVentas.getModel();
            Object[] obj = new Object[5];
            //double totalFiltro =0;
            for (int i = 0; i < ListaVentas.size(); i++) {
                obj[0] = ListaVentas.get(i).getId();
                obj[1] = ListaVentas.get(i).getCliente();
                obj[2] = ListaVentas.get(i).getVendedor();
                obj[3] = ListaVentas.get(i).getTotal();
                //totalFiltro = totalFiltro+ ListaVentas.get(i).getTotal();
                obj[4] = ListaVentas.get(i).getFecha();

                modelo.addRow(obj);
            }

            // labelTotalFiltros.setText(String.valueOf(totalpagar));
            tablaVentas.setModel(modelo);

        } catch (SQLException e) {
            System.out.println(e.toString());
        }

        //return ListaVentas;
    }

    
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        grupoFiltrar = new javax.swing.ButtonGroup();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        productos = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tablaProductos = new javax.swing.JTable();
        jPanelCargaProductos = new javax.swing.JPanel();
        txtBuscarProducto = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        txtCodigoProducto = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        txtDescripcionProd = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        txtCantidadProducto = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        txtPrecioProd = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        bxProveedor = new javax.swing.JComboBox<>();
        cbxCategoria = new javax.swing.JComboBox<Item>();
        btnCrearCategoria = new javax.swing.JButton();
        panelBtnsProductos = new javax.swing.JPanel();
        btnNuevoProd = new javax.swing.JButton();
        btnGuardarProd = new javax.swing.JButton();
        btnEditarProd = new javax.swing.JButton();
        btnExcelProd = new javax.swing.JButton();
        sincroWOO = new javax.swing.JButton();
        btnEliminarProd = new javax.swing.JButton();
        btnReCategoria = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtPrecioCompra = new javax.swing.JTextField();
        comboMateriales = new javax.swing.JComboBox<>();
        btnGenerarSKU = new javax.swing.JButton();
        comboPiedras = new javax.swing.JComboBox<>();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        chkWeb = new javax.swing.JCheckBox();
        txtIdProd = new javax.swing.JTextField();
        configuracion = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        txtNombreCongig = new javax.swing.JTextField();
        txtDomicilioConfig = new javax.swing.JTextField();
        txtTelefonoConfig = new javax.swing.JTextField();
        txtCuitConfig = new javax.swing.JTextField();
        btnActualizarConfig = new javax.swing.JButton();
        txtIdConfig = new javax.swing.JTextField();
        jPanelParametros = new javax.swing.JPanel();
        jLabel45 = new javax.swing.JLabel();
        txtSalidaJson = new javax.swing.JTextField();
        jLabel46 = new javax.swing.JLabel();
        txtEntradaJson = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        txtDescuentoEfectivo = new javax.swing.JTextField();
        txtDescuentoTransferencia = new javax.swing.JTextField();
        txtDescuentoCredito = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        txtRazon = new javax.swing.JTextField();
        PrecioDolar = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        txtPrecioDolar = new javax.swing.JTextField();
        btnDolar = new javax.swing.JButton();
        proveedor = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        txtCuitProv = new javax.swing.JTextField();
        txtNomProv = new javax.swing.JTextField();
        txtTelProv = new javax.swing.JTextField();
        txtDomicilioProv = new javax.swing.JTextField();
        txtEmailProv = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        tablaProveedores = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnGuardarProv = new javax.swing.JButton();
        btnNuevoProv = new javax.swing.JButton();
        btnActualizarProv = new javax.swing.JButton();
        btnEliminarProv = new javax.swing.JButton();
        btnPdfProv = new javax.swing.JButton();
        txtIdProveedor = new javax.swing.JTextField();
        jLabel49 = new javax.swing.JLabel();
        Efectivo = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        txtEfectivoCaja = new javax.swing.JTextField();
        jPanelTituloCaja = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        txtRetirarEfectivo = new javax.swing.JTextField();
        btnRetirarEfectivo = new javax.swing.JButton();
        btnIngresarEfectivo = new javax.swing.JButton();
        txtIngresarEfectivo = new javax.swing.JTextField();
        jScrollPane6 = new javax.swing.JScrollPane();
        tablaCaja = new javax.swing.JTable();
        jPanelBotoneaCaja = new javax.swing.JPanel();
        ventas = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tablaVentas = new javax.swing.JTable();
        btnPdfVentas = new javax.swing.JButton();
        txtIdVentas = new javax.swing.JTextField();
        labelTotalFiltros = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        btnFiltrarNomYFecha = new javax.swing.JButton();
        cbxUsuarios = new javax.swing.JComboBox<>();
        jLabel34 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        labelEfectivoAcum = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        labeltransferenciaFiltro = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        labelCreditoFiltro = new javax.swing.JLabel();
        radioUsuarios = new javax.swing.JRadioButton();
        radioVentas = new javax.swing.JRadioButton();
        jLabel43 = new javax.swing.JLabel();
        miFechaBuscar = new com.toedter.calendar.JDateChooser();
        miFechaBuscar2 = new com.toedter.calendar.JDateChooser();
        clientes = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtClienteNombre = new javax.swing.JTextField();
        txtClienteDni = new javax.swing.JTextField();
        txtClienteDomicilio = new javax.swing.JTextField();
        txtClienteTelefono = new javax.swing.JTextField();
        txtClienteEmail = new javax.swing.JTextField();
        txtClienteRSocial = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaClientes = new javax.swing.JTable();
        panelBotones = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnActualizarClientes = new javax.swing.JButton();
        btnNuevo = new javax.swing.JButton();
        btnEliminarCliente = new javax.swing.JButton();
        btnPdfClientes = new javax.swing.JButton();
        txtIdCliente = new javax.swing.JTextField();
        jLabel52 = new javax.swing.JLabel();
        nuevaVenta = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnEliminarNVenta = new javax.swing.JButton();
        txtCodigoProVenta = new javax.swing.JTextField();
        txtDescripcionVenta = new javax.swing.JTextField();
        txtCantidadVenta = new javax.swing.JTextField();
        txtPrecioVenta = new javax.swing.JTextField();
        txtStockVenta = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaNuevaVenta = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtDni = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        labelTotal = new javax.swing.JLabel();
        labelTotalAPagar = new javax.swing.JLabel();
        btnImprimir = new javax.swing.JButton();
        txtTelefonoCV = new javax.swing.JTextField();
        txtDireccionCV = new javax.swing.JTextField();
        txtRazonCV = new javax.swing.JTextField();
        txtIdProdNV = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtDomicilioClienteNV = new javax.swing.JTextField();
        jLabelTotal = new javax.swing.JLabel();
        LabelSubtotal = new javax.swing.JLabel();
        lblSeleccionFdePago = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jPanelFormaDePago = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        radioEfectivo = new javax.swing.JRadioButton();
        radioDebito = new javax.swing.JRadioButton();
        radioCredito = new javax.swing.JRadioButton();
        radioDebito1 = new javax.swing.JRadioButton();
        radioCuenta = new javax.swing.JRadioButton();
        inicio = new javax.swing.JPanel();
        jLabel56 = new javax.swing.JLabel();
        jLabelVendedor = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuVentas = new javax.swing.JMenu();
        jMenuItemNuevaVenta = new javax.swing.JMenuItem();
        jMenuItemDevoluciones = new javax.swing.JMenuItem();
        jMenuAnularVenta = new javax.swing.JMenuItem();
        menuResumenCaja = new javax.swing.JMenuItem();
        jMenuItemAbrirCaja = new javax.swing.JMenuItem();
        jMenuUsuarios = new javax.swing.JMenu();
        jMenuRegistroUsuario = new javax.swing.JMenuItem();
        listUsuarios = new javax.swing.JMenuItem();
        jmenuProductos = new javax.swing.JMenu();
        jMenuItemProductosCargar = new javax.swing.JMenuItem();
        jMenuItemCatProducto = new javax.swing.JMenuItem();
        menuImpExp = new javax.swing.JMenuItem();
        menuCodBarras = new javax.swing.JMenuItem();
        jMenuConfiguracion = new javax.swing.JMenu();
        jMenuItemParametros = new javax.swing.JMenuItem();
        itemBackup = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuReportes = new javax.swing.JMenu();
        jMenuIReportesVarios = new javax.swing.JMenuItem();
        jMenuItemInformes = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuProveedores = new javax.swing.JMenu();
        jMenuItemProveedores = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuClientes = new javax.swing.JMenu();
        jMenuItemClientes = new javax.swing.JMenuItem();

        jMenuItem8.setText("jMenuItem8");

        jMenu1.setText("File");
        jMenuBar2.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar2.add(jMenu2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAutoRequestFocus(false);
        setBackground(new java.awt.Color(204, 204, 204));
        setMinimumSize(new java.awt.Dimension(1220, 650));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));
        jTabbedPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTabbedPane1MouseClicked(evt);
            }
        });

        productos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                productosMouseClicked(evt);
            }
        });

        tablaProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "id", "CODIGO", "DESCRIPCION", "STOCK", "CATEGORIA", "COMPRA U$D", "VENTA U$D", "PROVEEDOR", "MATERIAL", "PIEDRA", "PRECIO EN PESOS", "WEB"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaProductosMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tablaProductos);
        if (tablaProductos.getColumnModel().getColumnCount() > 0) {
            tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(5);
            tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(15);
            tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(50);
            tablaProductos.getColumnModel().getColumn(3).setPreferredWidth(10);
            tablaProductos.getColumnModel().getColumn(4).setPreferredWidth(15);
            tablaProductos.getColumnModel().getColumn(5).setPreferredWidth(20);
            tablaProductos.getColumnModel().getColumn(6).setPreferredWidth(20);
            tablaProductos.getColumnModel().getColumn(7).setPreferredWidth(30);
            tablaProductos.getColumnModel().getColumn(8).setPreferredWidth(20);
            tablaProductos.getColumnModel().getColumn(9).setPreferredWidth(20);
        }

        txtBuscarProducto.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtBuscarProducto.setMinimumSize(new java.awt.Dimension(64, 22));
        txtBuscarProducto.setPreferredSize(new java.awt.Dimension(64, 22));
        txtBuscarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarProductoActionPerformed(evt);
            }
        });
        txtBuscarProducto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtBuscarProductoKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtBuscarProductoKeyTyped(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel24.setText("Código");

        txtCodigoProducto.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtCodigoProducto.setMinimumSize(new java.awt.Dimension(64, 22));
        txtCodigoProducto.setPreferredSize(new java.awt.Dimension(64, 22));
        txtCodigoProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoProductoActionPerformed(evt);
            }
        });
        txtCodigoProducto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCodigoProductoKeyTyped(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel26.setText("Descripción");

        txtDescripcionProd.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDescripcionProd.setMaximumSize(new java.awt.Dimension(141, 30));
        txtDescripcionProd.setMinimumSize(new java.awt.Dimension(64, 22));
        txtDescripcionProd.setPreferredSize(new java.awt.Dimension(64, 22));

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel25.setText("Stock");

        txtCantidadProducto.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtCantidadProducto.setMaximumSize(new java.awt.Dimension(141, 30));
        txtCantidadProducto.setMinimumSize(new java.awt.Dimension(64, 22));
        txtCantidadProducto.setPreferredSize(new java.awt.Dimension(64, 22));
        txtCantidadProducto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCantidadProductoKeyTyped(evt);
            }
        });

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel27.setText("Precio Venta en U$D");

        txtPrecioProd.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtPrecioProd.setMaximumSize(new java.awt.Dimension(141, 30));
        txtPrecioProd.setMinimumSize(new java.awt.Dimension(64, 22));
        txtPrecioProd.setPreferredSize(new java.awt.Dimension(64, 22));
        txtPrecioProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPrecioProdActionPerformed(evt);
            }
        });
        txtPrecioProd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPrecioProdKeyTyped(evt);
            }
        });

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel28.setText("Proveedor");

        bxProveedor.setEditable(true);
        bxProveedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bxProveedorMouseClicked(evt);
            }
        });
        bxProveedor.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                bxProveedorComponentHidden(evt);
            }
        });
        bxProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bxProveedorActionPerformed(evt);
            }
        });

        cbxCategoria.setModel(new javax.swing.DefaultComboBoxModel<>());
        cbxCategoria.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxCategoriaItemStateChanged(evt);
            }
        });
        cbxCategoria.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cbxCategoriaMouseClicked(evt);
            }
        });
        cbxCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxCategoriaActionPerformed(evt);
            }
        });

        btnCrearCategoria.setText("Crear");
        btnCrearCategoria.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCrearCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearCategoriaActionPerformed(evt);
            }
        });

        btnNuevoProd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/nuevo.png"))); // NOI18N
        btnNuevoProd.setText("Limpiar");
        btnNuevoProd.setMaximumSize(new java.awt.Dimension(111, 35));
        btnNuevoProd.setMinimumSize(new java.awt.Dimension(111, 35));
        btnNuevoProd.setPreferredSize(new java.awt.Dimension(118, 35));
        btnNuevoProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoProdActionPerformed(evt);
            }
        });
        panelBtnsProductos.add(btnNuevoProd);

        btnGuardarProd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/GuardarTodo.png"))); // NOI18N
        btnGuardarProd.setText("Guardar");
        btnGuardarProd.setMaximumSize(new java.awt.Dimension(111, 35));
        btnGuardarProd.setMinimumSize(new java.awt.Dimension(111, 35));
        btnGuardarProd.setPreferredSize(new java.awt.Dimension(118, 35));
        btnGuardarProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarProdActionPerformed(evt);
            }
        });
        panelBtnsProductos.add(btnGuardarProd);

        btnEditarProd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/refresh24.png"))); // NOI18N
        btnEditarProd.setText("Actualizar");
        btnEditarProd.setMaximumSize(new java.awt.Dimension(111, 35));
        btnEditarProd.setMinimumSize(new java.awt.Dimension(111, 35));
        btnEditarProd.setPreferredSize(new java.awt.Dimension(118, 35));
        btnEditarProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarProdActionPerformed(evt);
            }
        });
        panelBtnsProductos.add(btnEditarProd);

        btnExcelProd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/excel.png"))); // NOI18N
        btnExcelProd.setText("Migrar E");
        btnExcelProd.setMaximumSize(new java.awt.Dimension(111, 35));
        btnExcelProd.setMinimumSize(new java.awt.Dimension(111, 35));
        btnExcelProd.setPreferredSize(new java.awt.Dimension(118, 35));
        btnExcelProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcelProdActionPerformed(evt);
            }
        });
        panelBtnsProductos.add(btnExcelProd);

        sincroWOO.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Logo-wordpress-boton.png"))); // NOI18N
        sincroWOO.setText("SINC/WOO");
        sincroWOO.setMaximumSize(new java.awt.Dimension(118, 35));
        sincroWOO.setMinimumSize(new java.awt.Dimension(118, 35));
        sincroWOO.setPreferredSize(new java.awt.Dimension(118, 35));
        sincroWOO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sincroWOOActionPerformed(evt);
            }
        });
        panelBtnsProductos.add(sincroWOO);

        btnEliminarProd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarProd.setText("Eliminar");
        btnEliminarProd.setMaximumSize(new java.awt.Dimension(111, 35));
        btnEliminarProd.setMinimumSize(new java.awt.Dimension(111, 35));
        btnEliminarProd.setPreferredSize(new java.awt.Dimension(118, 35));
        btnEliminarProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarProdActionPerformed(evt);
            }
        });
        panelBtnsProductos.add(btnEliminarProd);

        btnReCategoria.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/actualizar24.png"))); // NOI18N
        btnReCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReCategoriaActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Precio Compra en U$D");

        comboMateriales.setModel(new javax.swing.DefaultComboBoxModel<>(Material.values()));

        btnGenerarSKU.setBackground(new java.awt.Color(244, 119, 21));
        btnGenerarSKU.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnGenerarSKU.setText("GENERAR SKU/CÓDIGO");
        btnGenerarSKU.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        btnGenerarSKU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarSKUActionPerformed(evt);
            }
        });

        comboPiedras.setModel(new javax.swing.DefaultComboBoxModel<>(Piedra.values()));

        jLabel53.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel53.setText("Material");

        jLabel54.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel54.setText("Piedras");

        jLabel44.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel44.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/lupa20x20.png"))); // NOI18N
        jLabel44.setText("Buscar");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("Producto Web");

        chkWeb.setText("Sí");

        javax.swing.GroupLayout jPanelCargaProductosLayout = new javax.swing.GroupLayout(jPanelCargaProductos);
        jPanelCargaProductos.setLayout(jPanelCargaProductosLayout);
        jPanelCargaProductosLayout.setHorizontalGroup(
            jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCargaProductosLayout.createSequentialGroup()
                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanelCargaProductosLayout.createSequentialGroup()
                            .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(bxProveedor, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanelCargaProductosLayout.createSequentialGroup()
                            .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE))
                            .addGap(18, 18, Short.MAX_VALUE)
                            .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtCantidadProducto, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                                .addComponent(txtDescripcionProd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(jPanelCargaProductosLayout.createSequentialGroup()
                            .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                            .addComponent(txtBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanelCargaProductosLayout.createSequentialGroup()
                            .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtPrecioCompra, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                                .addComponent(txtPrecioProd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(jPanelCargaProductosLayout.createSequentialGroup()
                            .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel53, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                                    .addComponent(jLabel54, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(jPanelCargaProductosLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanelCargaProductosLayout.createSequentialGroup()
                                    .addComponent(btnCrearCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnReCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(8, 8, 8)
                            .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtCodigoProducto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(comboMateriales, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(comboPiedras, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbxCategoria, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addComponent(panelBtnsProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanelCargaProductosLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(chkWeb, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelCargaProductosLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(btnGenerarSKU, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        jPanelCargaProductosLayout.setVerticalGroup(
            jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCargaProductosLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel44, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDescripcionProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(txtCantidadProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtPrecioCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(txtPrecioProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(bxProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel53)
                    .addComponent(comboMateriales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel54)
                    .addComponent(comboPiedras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnCrearCategoria, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbxCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(chkWeb))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelCargaProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCodigoProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnGenerarSKU, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelBtnsProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );

        javax.swing.GroupLayout productosLayout = new javax.swing.GroupLayout(productos);
        productos.setLayout(productosLayout);
        productosLayout.setHorizontalGroup(
            productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productosLayout.createSequentialGroup()
                .addGroup(productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(productosLayout.createSequentialGroup()
                        .addGap(246, 246, 246)
                        .addComponent(txtIdProd, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanelCargaProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 964, Short.MAX_VALUE)
                .addContainerGap())
        );
        productosLayout.setVerticalGroup(
            productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(productosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(productosLayout.createSequentialGroup()
                        .addComponent(txtIdProd, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelCargaProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 610, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 595, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Productos", productos);

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel29.setText("NOMBRE");

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel31.setText("DOMICILIO");

        jLabel32.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel32.setText("TELEFONO");

        jLabel33.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel33.setText("CUIT");

        btnActualizarConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/refresh24.png"))); // NOI18N
        btnActualizarConfig.setText("Actualizar");
        btnActualizarConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarConfigActionPerformed(evt);
            }
        });

        jPanelParametros.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Parametros", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.ABOVE_TOP));

        jLabel45.setText("Path Salida");

        jLabel46.setText("Path Entrada");

        jLabel37.setText("Desc. Efect");

        jLabel47.setText("Desc Transf");

        jLabel48.setText("Desc. Tarjeta");

        txtDescuentoEfectivo.setMinimumSize(new java.awt.Dimension(150, 22));
        txtDescuentoEfectivo.setPreferredSize(new java.awt.Dimension(150, 22));
        txtDescuentoEfectivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDescuentoEfectivoActionPerformed(evt);
            }
        });

        txtDescuentoTransferencia.setMinimumSize(new java.awt.Dimension(150, 22));
        txtDescuentoTransferencia.setPreferredSize(new java.awt.Dimension(150, 22));

        txtDescuentoCredito.setMinimumSize(new java.awt.Dimension(150, 22));
        txtDescuentoCredito.setPreferredSize(new java.awt.Dimension(150, 22));

        javax.swing.GroupLayout jPanelParametrosLayout = new javax.swing.GroupLayout(jPanelParametros);
        jPanelParametros.setLayout(jPanelParametrosLayout);
        jPanelParametrosLayout.setHorizontalGroup(
            jPanelParametrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParametrosLayout.createSequentialGroup()
                .addGroup(jPanelParametrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelParametrosLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel45)
                        .addGap(32, 32, 32)
                        .addComponent(txtSalidaJson, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(jLabel37))
                    .addGroup(jPanelParametrosLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel46)
                        .addGap(23, 23, 23)
                        .addComponent(txtEntradaJson, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(jLabel47))
                    .addGroup(jPanelParametrosLayout.createSequentialGroup()
                        .addGap(425, 425, 425)
                        .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanelParametrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDescuentoTransferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDescuentoCredito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDescuentoEfectivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(123, Short.MAX_VALUE))
        );
        jPanelParametrosLayout.setVerticalGroup(
            jPanelParametrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParametrosLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanelParametrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelParametrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel37)
                        .addComponent(txtDescuentoEfectivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelParametrosLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanelParametrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel45)
                            .addComponent(txtSalidaJson, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(5, 5, 5)
                .addGroup(jPanelParametrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelParametrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel47)
                        .addComponent(txtDescuentoTransferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelParametrosLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanelParametrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel46)
                            .addComponent(txtEntradaJson, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(8, 8, 8)
                .addGroup(jPanelParametrosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDescuentoCredito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel30.setText("RAZON SOCIAL");

        PrecioDolar.setName("CARGAR PRECIO DEL DOLAR"); // NOI18N

        jLabel12.setText("PRECIO DEL DOLAR");

        btnDolar.setBackground(new java.awt.Color(133, 187, 101));
        btnDolar.setText("ACTUALIZAR DOLAR");
        btnDolar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 112, 192)));
        btnDolar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDolarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PrecioDolarLayout = new javax.swing.GroupLayout(PrecioDolar);
        PrecioDolar.setLayout(PrecioDolarLayout);
        PrecioDolarLayout.setHorizontalGroup(
            PrecioDolarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PrecioDolarLayout.createSequentialGroup()
                .addGroup(PrecioDolarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PrecioDolarLayout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addGroup(PrecioDolarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPrecioDolar, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(PrecioDolarLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(btnDolar, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(59, Short.MAX_VALUE))
        );
        PrecioDolarLayout.setVerticalGroup(
            PrecioDolarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PrecioDolarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPrecioDolar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnDolar)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout configuracionLayout = new javax.swing.GroupLayout(configuracion);
        configuracion.setLayout(configuracionLayout);
        configuracionLayout.setHorizontalGroup(
            configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configuracionLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnActualizarConfig)
                    .addGroup(configuracionLayout.createSequentialGroup()
                        .addComponent(jPanelParametros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(PrecioDolar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(configuracionLayout.createSequentialGroup()
                        .addGroup(configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtIdConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(configuracionLayout.createSequentialGroup()
                                .addGroup(configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel29)
                                    .addComponent(jLabel33))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtNombreCongig, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtCuitConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(41, 41, 41)
                        .addGroup(configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(configuracionLayout.createSequentialGroup()
                                .addComponent(jLabel31)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDomicilioConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(jLabel30)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtRazon, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(configuracionLayout.createSequentialGroup()
                                .addComponent(jLabel32)
                                .addGap(18, 18, 18)
                                .addComponent(txtTelefonoConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(158, Short.MAX_VALUE))
        );
        configuracionLayout.setVerticalGroup(
            configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configuracionLayout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addComponent(txtIdConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(configuracionLayout.createSequentialGroup()
                        .addGroup(configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel29)
                            .addComponent(txtNombreCongig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel33)
                            .addComponent(txtCuitConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(configuracionLayout.createSequentialGroup()
                        .addGroup(configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel31)
                            .addComponent(txtDomicilioConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel30)
                            .addComponent(txtRazon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel32)
                            .addComponent(txtTelefonoConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(57, 57, 57)
                .addGroup(configuracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelParametros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PrecioDolar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addComponent(btnActualizarConfig)
                .addContainerGap(215, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Configuracion", configuracion);

        proveedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                proveedorMouseClicked(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel19.setText("CUIT");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel20.setText("Nombre");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel21.setText("Telefono");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel22.setText("Domicilio");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel23.setText("Web o instagram");

        txtCuitProv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCuitProvActionPerformed(evt);
            }
        });

        txtDomicilioProv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDomicilioProvActionPerformed(evt);
            }
        });

        tablaProveedores.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "id", "CUIT", "Nombre", "Telefono", "Domimcilio", " Web o instagram"
            }
        ));
        tablaProveedores.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaProveedoresMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tablaProveedores);
        if (tablaProveedores.getColumnModel().getColumnCount() > 0) {
            tablaProveedores.getColumnModel().getColumn(0).setPreferredWidth(10);
            tablaProveedores.getColumnModel().getColumn(1).setPreferredWidth(30);
            tablaProveedores.getColumnModel().getColumn(2).setPreferredWidth(80);
            tablaProveedores.getColumnModel().getColumn(3).setPreferredWidth(30);
            tablaProveedores.getColumnModel().getColumn(4).setPreferredWidth(80);
            tablaProveedores.getColumnModel().getColumn(5).setPreferredWidth(30);
        }

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        btnGuardarProv.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/GuardarTodo.png"))); // NOI18N
        btnGuardarProv.setText("Guardar");
        btnGuardarProv.setPreferredSize(new java.awt.Dimension(111, 35));
        btnGuardarProv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarProvActionPerformed(evt);
            }
        });
        jPanel2.add(btnGuardarProv);

        btnNuevoProv.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/nuevo.png"))); // NOI18N
        btnNuevoProv.setText("Nuevo");
        btnNuevoProv.setPreferredSize(new java.awt.Dimension(111, 35));
        btnNuevoProv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoProvActionPerformed(evt);
            }
        });
        jPanel2.add(btnNuevoProv);

        btnActualizarProv.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/refresh24.png"))); // NOI18N
        btnActualizarProv.setText("Actualizar");
        btnActualizarProv.setPreferredSize(new java.awt.Dimension(111, 35));
        btnActualizarProv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarProvActionPerformed(evt);
            }
        });
        jPanel2.add(btnActualizarProv);

        btnEliminarProv.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarProv.setText("Eliminar");
        btnEliminarProv.setPreferredSize(new java.awt.Dimension(111, 35));
        btnEliminarProv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarProvActionPerformed(evt);
            }
        });
        jPanel2.add(btnEliminarProv);

        btnPdfProv.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/pdf24.png"))); // NOI18N
        btnPdfProv.setText("Imprimir");
        btnPdfProv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPdfProvActionPerformed(evt);
            }
        });
        jPanel2.add(btnPdfProv);

        jLabel49.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel49.setText("PROVEEDORES");

        javax.swing.GroupLayout proveedorLayout = new javax.swing.GroupLayout(proveedor);
        proveedor.setLayout(proveedorLayout);
        proveedorLayout.setHorizontalGroup(
            proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proveedorLayout.createSequentialGroup()
                .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(proveedorLayout.createSequentialGroup()
                        .addGap(96, 96, 96)
                        .addComponent(jLabel49))
                    .addGroup(proveedorLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtIdProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(proveedorLayout.createSequentialGroup()
                                    .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel22)
                                        .addComponent(jLabel21)
                                        .addComponent(jLabel23))
                                    .addGap(18, 18, 18)
                                    .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(txtEmailProv, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtDomicilioProv, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtTelProv, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(proveedorLayout.createSequentialGroup()
                                    .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel20)
                                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtNomProv, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtCuitProv, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 964, Short.MAX_VALUE)
                .addContainerGap())
        );
        proveedorLayout.setVerticalGroup(
            proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, proveedorLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, proveedorLayout.createSequentialGroup()
                        .addComponent(jLabel49)
                        .addGap(46, 46, 46)
                        .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel19)
                            .addComponent(txtCuitProv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(txtNomProv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel21)
                            .addComponent(txtTelProv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(21, 21, 21)
                        .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel22)
                            .addComponent(txtDomicilioProv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(proveedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(txtEmailProv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtIdProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(68, 68, 68))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, proveedorLayout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15))))
        );

        jTabbedPane1.addTab("Proveedor", proveedor);

        Efectivo.setBorder(javax.swing.BorderFactory.createTitledBorder("Efectivo en caja"));
        Efectivo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                EfectivoMouseClicked(evt);
            }
        });

        jLabel39.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel39.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/dinero-24.png"))); // NOI18N
        jLabel39.setText("Efectivo");

        txtEfectivoCaja.setEditable(false);

        jLabel41.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel41.setText("Caja Diaria");
        jPanelTituloCaja.add(jLabel41);

        txtRetirarEfectivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRetirarEfectivoActionPerformed(evt);
            }
        });

        btnRetirarEfectivo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRetirarEfectivo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/bolsa-de-dinero-24.png"))); // NOI18N
        btnRetirarEfectivo.setText("Retirar");
        btnRetirarEfectivo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnRetirarEfectivo.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnRetirarEfectivo.setMaximumSize(new java.awt.Dimension(104, 25));
        btnRetirarEfectivo.setMinimumSize(new java.awt.Dimension(104, 25));
        btnRetirarEfectivo.setPreferredSize(new java.awt.Dimension(104, 25));
        btnRetirarEfectivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRetirarEfectivoActionPerformed(evt);
            }
        });

        btnIngresarEfectivo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnIngresarEfectivo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/dinero-24.png"))); // NOI18N
        btnIngresarEfectivo.setText("Ingresar\n");
        btnIngresarEfectivo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnIngresarEfectivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIngresarEfectivoActionPerformed(evt);
            }
        });

        txtIngresarEfectivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIngresarEfectivoActionPerformed(evt);
            }
        });

        tablaCaja.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID APERTURA", "FECHA", "HORA", "TIPO", "MONTO", "DESCRIPCION", "USUARIO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(tablaCaja);
        if (tablaCaja.getColumnModel().getColumnCount() > 0) {
            tablaCaja.getColumnModel().getColumn(0).setPreferredWidth(10);
            tablaCaja.getColumnModel().getColumn(1).setPreferredWidth(20);
            tablaCaja.getColumnModel().getColumn(2).setPreferredWidth(20);
            tablaCaja.getColumnModel().getColumn(3).setPreferredWidth(20);
            tablaCaja.getColumnModel().getColumn(4).setPreferredWidth(20);
            tablaCaja.getColumnModel().getColumn(6).setPreferredWidth(25);
        }

        javax.swing.GroupLayout jPanelBotoneaCajaLayout = new javax.swing.GroupLayout(jPanelBotoneaCaja);
        jPanelBotoneaCaja.setLayout(jPanelBotoneaCajaLayout);
        jPanelBotoneaCajaLayout.setHorizontalGroup(
            jPanelBotoneaCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelBotoneaCajaLayout.setVerticalGroup(
            jPanelBotoneaCajaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 217, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout EfectivoLayout = new javax.swing.GroupLayout(Efectivo);
        Efectivo.setLayout(EfectivoLayout);
        EfectivoLayout.setHorizontalGroup(
            EfectivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EfectivoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(EfectivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(EfectivoLayout.createSequentialGroup()
                        .addGroup(EfectivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnIngresarEfectivo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel39, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnRetirarEfectivo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(41, 41, 41)
                        .addGroup(EfectivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtEfectivoCaja)
                            .addComponent(txtIngresarEfectivo, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtRetirarEfectivo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanelBotoneaCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 940, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanelTituloCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        EfectivoLayout.setVerticalGroup(
            EfectivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EfectivoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelTituloCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EfectivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EfectivoLayout.createSequentialGroup()
                        .addGroup(EfectivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtEfectivoCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(EfectivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRetirarEfectivo, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtRetirarEfectivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(EfectivoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnIngresarEfectivo, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtIngresarEfectivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(37, 37, 37)
                        .addComponent(jPanelBotoneaCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 376, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(185, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Caja", Efectivo);

        ventas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ventasMouseClicked(evt);
            }
        });

        tablaVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "CLIENTE", "VENDEDOR", "TOTAL", "FECHA", "FORMA DE PAGO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaVentasMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tablaVentas);
        if (tablaVentas.getColumnModel().getColumnCount() > 0) {
            tablaVentas.getColumnModel().getColumn(0).setPreferredWidth(10);
            tablaVentas.getColumnModel().getColumn(1).setPreferredWidth(80);
            tablaVentas.getColumnModel().getColumn(2).setPreferredWidth(60);
            tablaVentas.getColumnModel().getColumn(3).setPreferredWidth(40);
            tablaVentas.getColumnModel().getColumn(4).setPreferredWidth(10);
            tablaVentas.getColumnModel().getColumn(5).setPreferredWidth(20);
        }

        btnPdfVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/pdf.png"))); // NOI18N
        btnPdfVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPdfVentasActionPerformed(evt);
            }
        });

        labelTotalFiltros.setText("--------------");

        jLabel35.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel35.setText("TOTAL $");

        btnFiltrarNomYFecha.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnFiltrarNomYFecha.setForeground(new java.awt.Color(255, 153, 153));
        btnFiltrarNomYFecha.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/lupa.png"))); // NOI18N
        btnFiltrarNomYFecha.setText("Filtro");
        btnFiltrarNomYFecha.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnFiltrarNomYFechaMouseClicked(evt);
            }
        });
        btnFiltrarNomYFecha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFiltrarNomYFechaActionPerformed(evt);
            }
        });

        jLabel34.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel34.setText("Usuario");

        jLabel38.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel38.setText("Efectivo $");

        labelEfectivoAcum.setText("-------------");

        jLabel40.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel40.setText("Transferencias $");

        labeltransferenciaFiltro.setText("--------------");

        jLabel42.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel42.setText("Crédito/Débito $");

        labelCreditoFiltro.setText("---------------");

        radioUsuarios.setText("Usuario");
        radioUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioUsuariosActionPerformed(evt);
            }
        });

        radioVentas.setText("Ventas");

        jLabel43.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel43.setText("Filtrar Por");

        javax.swing.GroupLayout ventasLayout = new javax.swing.GroupLayout(ventas);
        ventas.setLayout(ventasLayout);
        ventasLayout.setHorizontalGroup(
            ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ventasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ventasLayout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(labelEfectivoAcum)
                        .addGap(94, 94, 94)
                        .addComponent(jLabel40)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labeltransferenciaFiltro)
                        .addGap(102, 102, 102)
                        .addComponent(jLabel42)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelCreditoFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelTotalFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(ventasLayout.createSequentialGroup()
                        .addComponent(txtIdVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(ventasLayout.createSequentialGroup()
                                .addComponent(btnPdfVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 177, Short.MAX_VALUE)
                                .addComponent(jLabel43)
                                .addGap(18, 18, 18)
                                .addComponent(radioUsuarios)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioVentas)
                                .addGap(69, 69, 69)
                                .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(cbxUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(58, 58, 58)
                                .addComponent(miFechaBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnFiltrarNomYFecha)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(miFechaBuscar2, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(52, 52, 52))
                            .addComponent(jScrollPane5))))
                .addContainerGap())
        );
        ventasLayout.setVerticalGroup(
            ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ventasLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radioUsuarios)
                            .addComponent(radioVentas)
                            .addComponent(jLabel43))
                        .addComponent(cbxUsuarios, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnFiltrarNomYFecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(miFechaBuscar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(miFechaBuscar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(ventasLayout.createSequentialGroup()
                        .addComponent(btnPdfVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)))
                .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(ventasLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtIdVentas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(538, Short.MAX_VALUE))
                    .addGroup(ventasLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                        .addGap(26, 26, 26)
                        .addGroup(ventasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel40)
                            .addComponent(jLabel42)
                            .addComponent(jLabel35)
                            .addComponent(jLabel38)
                            .addComponent(labelEfectivoAcum, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labeltransferenciaFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelCreditoFiltro)
                            .addComponent(labelTotalFiltros))
                        .addGap(19, 19, 19))))
        );

        jTabbedPane1.addTab("Informes", ventas);

        clientes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        clientes.setPreferredSize(new java.awt.Dimension(1010, 501));
        clientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clientesMouseClicked(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel13.setText("Nombre");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel14.setText("DNI");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel15.setText("Domicilio");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel16.setText("Telefono");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel17.setText("Email");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel18.setText("Razon Social");

        tablaClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nombre", "DNI", "Domicilio", "Telefono", "Email", "Razon Social"
            }
        ));
        tablaClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaClientesMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tablaClientes);
        if (tablaClientes.getColumnModel().getColumnCount() > 0) {
            tablaClientes.getColumnModel().getColumn(0).setPreferredWidth(5);
            tablaClientes.getColumnModel().getColumn(1).setPreferredWidth(80);
            tablaClientes.getColumnModel().getColumn(2).setPreferredWidth(30);
            tablaClientes.getColumnModel().getColumn(3).setPreferredWidth(80);
            tablaClientes.getColumnModel().getColumn(4).setPreferredWidth(30);
            tablaClientes.getColumnModel().getColumn(5).setPreferredWidth(60);
            tablaClientes.getColumnModel().getColumn(6).setPreferredWidth(80);
        }

        panelBotones.setBackground(new java.awt.Color(204, 204, 204));

        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/GuardarTodo.png"))); // NOI18N
        btnGuardar.setText("Guardar");
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setPreferredSize(new java.awt.Dimension(111, 35));
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });
        panelBotones.add(btnGuardar);

        btnActualizarClientes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/refresh32.png"))); // NOI18N
        btnActualizarClientes.setText("Actualizar");
        btnActualizarClientes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnActualizarClientes.setPreferredSize(new java.awt.Dimension(111, 35));
        btnActualizarClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnActualizarClientesMouseReleased(evt);
            }
        });
        btnActualizarClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarClientesActionPerformed(evt);
            }
        });
        panelBotones.add(btnActualizarClientes);

        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/nuevo.png"))); // NOI18N
        btnNuevo.setText("Nuevo");
        btnNuevo.setPreferredSize(new java.awt.Dimension(111, 35));
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });
        panelBotones.add(btnNuevo);

        btnEliminarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarCliente.setText("Eliminar");
        btnEliminarCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminarCliente.setPreferredSize(new java.awt.Dimension(111, 35));
        btnEliminarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarClienteActionPerformed(evt);
            }
        });
        panelBotones.add(btnEliminarCliente);

        btnPdfClientes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/PDF24x24.png"))); // NOI18N
        btnPdfClientes.setText("Imprimir");
        btnPdfClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPdfClientesActionPerformed(evt);
            }
        });
        panelBotones.add(btnPdfClientes);

        jLabel52.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel52.setText("CLIENTES");

        javax.swing.GroupLayout clientesLayout = new javax.swing.GroupLayout(clientes);
        clientes.setLayout(clientesLayout);
        clientesLayout.setHorizontalGroup(
            clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clientesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(clientesLayout.createSequentialGroup()
                        .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(clientesLayout.createSequentialGroup()
                                .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel16)
                                    .addComponent(jLabel17)
                                    .addComponent(jLabel18))
                                .addGap(18, 18, 18)
                                .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtClienteDni, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtClienteNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtClienteDomicilio, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtClienteTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtClienteEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(txtClienteRSocial, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(panelBotones, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, clientesLayout.createSequentialGroup()
                        .addComponent(jLabel52)
                        .addGap(113, 113, 113)))
                .addComponent(txtIdCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 900, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(772, 772, 772))
        );
        clientesLayout.setVerticalGroup(
            clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clientesLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(clientesLayout.createSequentialGroup()
                        .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtIdCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel52))
                        .addGap(28, 28, 28)
                        .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(txtClienteNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(txtClienteDni, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel15)
                            .addComponent(txtClienteDomicilio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(txtClienteTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(txtClienteEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(21, 21, 21)
                        .addGroup(clientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18)
                            .addComponent(txtClienteRSocial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelBotones, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 571, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Clientes", clientes);

        nuevaVenta.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Código F1 para buscar");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Descripcion");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Cantidad");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Precio");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Stock");

        btnEliminarNVenta.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEliminarNVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/eliminar.png"))); // NOI18N
        btnEliminarNVenta.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEliminarNVenta.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnEliminarNVenta.setLabel("Quitar Prod");
        btnEliminarNVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarNVentaActionPerformed(evt);
            }
        });

        txtCodigoProVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoProVentaActionPerformed(evt);
            }
        });
        txtCodigoProVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCodigoProVentaKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCodigoProVentaKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCodigoProVentaKeyTyped(evt);
            }
        });

        txtDescripcionVenta.setEditable(false);

        txtCantidadVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCantidadVentaActionPerformed(evt);
            }
        });
        txtCantidadVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCantidadVentaKeyPressed(evt);
            }
        });

        txtPrecioVenta.setEditable(false);
        txtPrecioVenta.setBackground(new java.awt.Color(255, 255, 255));
        txtPrecioVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPrecioVentaActionPerformed(evt);
            }
        });

        txtStockVenta.setEditable(false);

        tablaNuevaVenta.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Descripcion", "Cantidad", "Precio", "Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaNuevaVenta);
        if (tablaNuevaVenta.getColumnModel().getColumnCount() > 0) {
            tablaNuevaVenta.getColumnModel().getColumn(0).setPreferredWidth(25);
            tablaNuevaVenta.getColumnModel().getColumn(1).setPreferredWidth(100);
            tablaNuevaVenta.getColumnModel().getColumn(2).setPreferredWidth(10);
            tablaNuevaVenta.getColumnModel().getColumn(3).setPreferredWidth(15);
            tablaNuevaVenta.getColumnModel().getColumn(4).setPreferredWidth(30);
        }

        jLabel8.setText("DNI");

        jLabel9.setText("Nombre");

        txtDni.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtDniKeyPressed(evt);
            }
        });

        txtNombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNombreActionPerformed(evt);
            }
        });

        jLabel10.setText("Email");

        labelTotal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/money.png"))); // NOI18N
        labelTotal.setText("Total a Pagar");

        labelTotalAPagar.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        labelTotalAPagar.setText("----------");

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/carretilla.png"))); // NOI18N
        btnImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimirActionPerformed(evt);
            }
        });

        txtDireccionCV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDireccionCVActionPerformed(evt);
            }
        });

        jLabel11.setText("Domicilio");

        jLabelTotal.setText("Total");

        LabelSubtotal.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        LabelSubtotal.setText("------------");

        lblSeleccionFdePago.setText("--------------");

        jLabel50.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel50.setText("Datos del Cliente");

        jLabel51.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel51.setText("Confirmar Venta");

        jLabel36.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel36.setText("Forma de pago");

        radioEfectivo.setText("Efectivo");
        radioEfectivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioEfectivoActionPerformed(evt);
            }
        });

        radioDebito.setText("Transferencia");
        radioDebito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioDebitoActionPerformed(evt);
            }
        });

        radioCredito.setText("Crédito");
        radioCredito.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioCreditoActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioDebito1);
        radioDebito1.setText("Débito");

        buttonGroup1.add(radioCuenta);
        radioCuenta.setText("Cuenta");

        javax.swing.GroupLayout jPanelFormaDePagoLayout = new javax.swing.GroupLayout(jPanelFormaDePago);
        jPanelFormaDePago.setLayout(jPanelFormaDePagoLayout);
        jPanelFormaDePagoLayout.setHorizontalGroup(
            jPanelFormaDePagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFormaDePagoLayout.createSequentialGroup()
                .addComponent(jLabel36)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanelFormaDePagoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelFormaDePagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelFormaDePagoLayout.createSequentialGroup()
                        .addComponent(radioEfectivo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioDebito))
                    .addGroup(jPanelFormaDePagoLayout.createSequentialGroup()
                        .addComponent(radioDebito1)
                        .addGap(18, 18, 18)
                        .addComponent(radioCredito))
                    .addComponent(radioCuenta))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        jPanelFormaDePagoLayout.setVerticalGroup(
            jPanelFormaDePagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFormaDePagoLayout.createSequentialGroup()
                .addComponent(jLabel36)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelFormaDePagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioEfectivo)
                    .addComponent(radioDebito))
                .addGap(18, 18, 18)
                .addGroup(jPanelFormaDePagoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioDebito1)
                    .addComponent(radioCredito))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioCuenta)
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout nuevaVentaLayout = new javax.swing.GroupLayout(nuevaVenta);
        nuevaVenta.setLayout(nuevaVentaLayout);
        nuevaVentaLayout.setHorizontalGroup(
            nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nuevaVentaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(nuevaVentaLayout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())
                    .addGroup(nuevaVentaLayout.createSequentialGroup()
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCodigoProVenta)
                            .addGroup(nuevaVentaLayout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 279, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(nuevaVentaLayout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(69, 69, 69)
                                .addComponent(txtIdProdNV, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtDescripcionVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(nuevaVentaLayout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(txtCantidadVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nuevaVentaLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)))
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nuevaVentaLayout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(35, 35, 35))
                            .addComponent(txtPrecioVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtStockVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addComponent(btnEliminarNVenta)
                        .addGap(192, 192, 192))
                    .addGroup(nuevaVentaLayout.createSequentialGroup()
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(nuevaVentaLayout.createSequentialGroup()
                                .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(nuevaVentaLayout.createSequentialGroup()
                                        .addComponent(txtDni, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(nuevaVentaLayout.createSequentialGroup()
                                        .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(32, 32, 32)
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtDomicilioClienteNV, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(nuevaVentaLayout.createSequentialGroup()
                                .addComponent(jLabel50)
                                .addGap(20, 20, 20)
                                .addComponent(txtDireccionCV, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtTelefonoCV, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtRazonCV, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelFormaDePago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel51)
                            .addGroup(nuevaVentaLayout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblSeleccionFdePago)
                            .addGroup(nuevaVentaLayout.createSequentialGroup()
                                .addComponent(labelTotal)
                                .addGap(27, 27, 27)
                                .addComponent(labelTotalAPagar))
                            .addGroup(nuevaVentaLayout.createSequentialGroup()
                                .addComponent(jLabelTotal)
                                .addGap(46, 46, 46)
                                .addComponent(LabelSubtotal)))
                        .addGap(75, 75, 75))))
        );
        nuevaVentaLayout.setVerticalGroup(
            nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nuevaVentaLayout.createSequentialGroup()
                .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(nuevaVentaLayout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEliminarNVenta))
                    .addGroup(nuevaVentaLayout.createSequentialGroup()
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(nuevaVentaLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtIdProdNV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCodigoProVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtPrecioVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtCantidadVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtDescripcionVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtStockVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(nuevaVentaLayout.createSequentialGroup()
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel50)
                            .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtDireccionCV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtTelefonoCV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtRazonCV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel9)
                                .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel8)
                                .addComponent(txtDni, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDomicilioClienteNV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11)))
                    .addGroup(nuevaVentaLayout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(LabelSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelTotal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSeleccionFdePago, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelTotalAPagar))
                    .addGroup(nuevaVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(labelTotal)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, nuevaVentaLayout.createSequentialGroup()
                            .addGap(72, 72, 72)
                            .addComponent(jLabel51)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnImprimir)))
                    .addComponent(jPanelFormaDePago, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(175, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Nueva Venta", nuevaVenta);

        inicio.setBackground(new java.awt.Color(255, 255, 255));
        inicio.setLayout(new java.awt.GridBagLayout());

        jLabel56.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Logo PNG chgpt 700x700.png"))); // NOI18N
        inicio.add(jLabel56, new java.awt.GridBagConstraints());

        jTabbedPane1.addTab("Inicio", inicio);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 1250, 660));

        jLabelVendedor.setBackground(new java.awt.Color(204, 204, 204));
        jLabelVendedor.setText("fgrgwerg");
        getContentPane().add(jLabelVendedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 690, -1, -1));

        jMenuBar1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jMenuVentas.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 15, 1, 15));
        jMenuVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/ventas 24.png"))); // NOI18N
        jMenuVentas.setText("Ventas");
        jMenuVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuVentasActionPerformed(evt);
            }
        });

        jMenuItemNuevaVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/ventas 24.png"))); // NOI18N
        jMenuItemNuevaVenta.setText("Nueva Venta");
        jMenuItemNuevaVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNuevaVentaActionPerformed(evt);
            }
        });
        jMenuVentas.add(jMenuItemNuevaVenta);

        jMenuItemDevoluciones.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/actualizar24.png"))); // NOI18N
        jMenuItemDevoluciones.setText("Devoluciones");
        jMenuItemDevoluciones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDevolucionesActionPerformed(evt);
            }
        });
        jMenuVentas.add(jMenuItemDevoluciones);

        jMenuAnularVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/quitar 20x20.png"))); // NOI18N
        jMenuAnularVenta.setText("Anular Venta");
        jMenuAnularVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAnularVentaActionPerformed(evt);
            }
        });
        jMenuVentas.add(jMenuAnularVenta);

        menuResumenCaja.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/caja registradora 24x24.png"))); // NOI18N
        menuResumenCaja.setText("Caja");
        menuResumenCaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuResumenCajaActionPerformed(evt);
            }
        });
        jMenuVentas.add(menuResumenCaja);

        jMenuItemAbrirCaja.setText("Abrir Caja");
        jMenuItemAbrirCaja.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAbrirCajaActionPerformed(evt);
            }
        });
        jMenuVentas.add(jMenuItemAbrirCaja);

        jMenuBar1.add(jMenuVentas);

        jMenuUsuarios.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 15, 1, 15));
        jMenuUsuarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Usuarios24.png"))); // NOI18N
        jMenuUsuarios.setText("Usuarios");
        jMenuUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuUsuariosActionPerformed(evt);
            }
        });

        jMenuRegistroUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/agregarusuario20x20.png"))); // NOI18N
        jMenuRegistroUsuario.setText("Nuevo Registro");
        jMenuRegistroUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuRegistroUsuarioActionPerformed(evt);
            }
        });
        jMenuUsuarios.add(jMenuRegistroUsuario);

        listUsuarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/listado20x20.png"))); // NOI18N
        listUsuarios.setText("Listado de Usuarios");
        listUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listUsuariosActionPerformed(evt);
            }
        });
        jMenuUsuarios.add(listUsuarios);

        jMenuBar1.add(jMenuUsuarios);

        jmenuProductos.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 15, 1, 15));
        jmenuProductos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/producto24.png"))); // NOI18N
        jmenuProductos.setText("Productos");
        jmenuProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jmenuProductosMouseClicked(evt);
            }
        });
        jmenuProductos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmenuProductosActionPerformed(evt);
            }
        });

        jMenuItemProductosCargar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/editar 24x24.png"))); // NOI18N
        jMenuItemProductosCargar.setText("Cargar/Editar");
        jMenuItemProductosCargar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemProductosCargarActionPerformed(evt);
            }
        });
        jmenuProductos.add(jMenuItemProductosCargar);

        jMenuItemCatProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/categorias24x24.png"))); // NOI18N
        jMenuItemCatProducto.setText("Categorias");
        jMenuItemCatProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCatProductoActionPerformed(evt);
            }
        });
        jmenuProductos.add(jMenuItemCatProducto);

        menuImpExp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/impExp 24x24.png"))); // NOI18N
        menuImpExp.setText("Imp/Export");
        menuImpExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuImpExpActionPerformed(evt);
            }
        });
        jmenuProductos.add(menuImpExp);

        menuCodBarras.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/códigobarras24x24.png"))); // NOI18N
        menuCodBarras.setText("Codigos de Barras");
        menuCodBarras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuCodBarrasActionPerformed(evt);
            }
        });
        jmenuProductos.add(menuCodBarras);

        jMenuBar1.add(jmenuProductos);

        jMenuConfiguracion.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 15, 1, 15));
        jMenuConfiguracion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/config24.png"))); // NOI18N
        jMenuConfiguracion.setText("Configuracion");

        jMenuItemParametros.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/config24.png"))); // NOI18N
        jMenuItemParametros.setText("Parametros");
        jMenuItemParametros.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemParametrosActionPerformed(evt);
            }
        });
        jMenuConfiguracion.add(jMenuItemParametros);

        itemBackup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/backup (1).png"))); // NOI18N
        itemBackup.setText("Backup");
        itemBackup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemBackupActionPerformed(evt);
            }
        });
        jMenuConfiguracion.add(itemBackup);

        jMenuItem2.setText("Sinc Web");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenuConfiguracion.add(jMenuItem2);

        jMenuItem3.setText("Pedidos Web");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenuConfiguracion.add(jMenuItem3);

        jMenuBar1.add(jMenuConfiguracion);

        jMenuReportes.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 15, 1, 15));
        jMenuReportes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Iconos/analisis (1).png"))); // NOI18N
        jMenuReportes.setText("Reportes");

        jMenuIReportesVarios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/report.png"))); // NOI18N
        jMenuIReportesVarios.setText("Reportes Varios");
        jMenuIReportesVarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuIReportesVariosActionPerformed(evt);
            }
        });
        jMenuReportes.add(jMenuIReportesVarios);

        jMenuItemInformes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/compras.png"))); // NOI18N
        jMenuItemInformes.setText("Informes de Ventas");
        jMenuItemInformes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemInformesActionPerformed(evt);
            }
        });
        jMenuReportes.add(jMenuItemInformes);

        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Iconos/analisis (1).png"))); // NOI18N
        jMenuItem1.setText("Estadisticas");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenuReportes.add(jMenuItem1);

        jMenuBar1.add(jMenuReportes);

        jMenuProveedores.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/proveedor24.png"))); // NOI18N
        jMenuProveedores.setText("Proveedores");
        jMenuProveedores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuProveedoresActionPerformed(evt);
            }
        });

        jMenuItemProveedores.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/proveedor24.png"))); // NOI18N
        jMenuItemProveedores.setText("Ver Editar Proveedores");
        jMenuItemProveedores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemProveedoresActionPerformed(evt);
            }
        });
        jMenuProveedores.add(jMenuItemProveedores);

        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/listado20x20.png"))); // NOI18N
        jMenuItem4.setText("Listado de Proveedores");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenuProveedores.add(jMenuItem4);

        jMenuBar1.add(jMenuProveedores);

        jMenuClientes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Clientes24.png"))); // NOI18N
        jMenuClientes.setText("Clientes");
        jMenuClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuClientesActionPerformed(evt);
            }
        });

        jMenuItemClientes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Clientes24.png"))); // NOI18N
        jMenuItemClientes.setText("Ver editar Clientes");
        jMenuItemClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClientesActionPerformed(evt);
            }
        });
        jMenuClientes.add(jMenuItemClientes);

        jMenuBar1.add(jMenuClientes);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtCodigoProVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoProVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoProVentaActionPerformed

    private void btnActualizarClientesMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnActualizarClientesMouseReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_btnActualizarClientesMouseReleased

    private void txtDomicilioProvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDomicilioProvActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDomicilioProvActionPerformed

    private void txtCuitProvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCuitProvActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCuitProvActionPerformed

    private void btnExcelProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelProdActionPerformed
        // TODO add your handling code here:
       Excel.reporte();
    }//GEN-LAST:event_btnExcelProdActionPerformed

    private void txtDireccionCVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDireccionCVActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDireccionCVActionPerformed

    private void txtPrecioVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPrecioVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPrecioVentaActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
       
        if( !"".equals(txtClienteNombre.getText()) || !"".equals(txtClienteDni.getText()) || !"".equals(txtClienteDomicilio.getText()) || !"".equals(txtClienteTelefono.getText()) || !"".equals(txtClienteEmail.getText()) ){
       
            cl.setNombre(txtClienteNombre.getText());
            cl.setDni(Integer.parseInt(txtClienteDni.getText()));
            cl.setDomicilio(txtClienteDomicilio.getText());
            cl.setTelefono(txtClienteTelefono.getText());
            cl.setEmail(txtClienteEmail.getText());
            cl.setRazon(txtClienteRSocial.getText());
            cld.registrar(cl);
            LimpiarTabla();
            LimpiarCliente();
            ListarCliente();
            JOptionPane.showMessageDialog(null,"Cliente Registrado");
       
       }else{
        JOptionPane.showMessageDialog(null,"Los Campos están incompletos");
        }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void tablaClientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaClientesMouseClicked
        int fila = tablaClientes.rowAtPoint(evt.getPoint());/*esta linea captura el click sobre la tabla especificamente sobre la fila que se selecciona*/
        txtIdCliente.setText(tablaClientes.getValueAt(fila, 0).toString());
        txtClienteNombre.setText(tablaClientes.getValueAt(fila, 1).toString());
        txtClienteDni.setText(tablaClientes.getValueAt(fila, 2).toString());
        txtClienteDomicilio.setText(tablaClientes.getValueAt(fila, 3).toString());
        txtClienteTelefono.setText(tablaClientes.getValueAt(fila, 4).toString());
        txtClienteEmail.setText(tablaClientes.getValueAt(fila, 5).toString());
        txtClienteRSocial.setText(tablaClientes.getValueAt(fila, 6).toString());
    }//GEN-LAST:event_tablaClientesMouseClicked

    private void btnEliminarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarClienteActionPerformed
        // TODO add your handling code here:
        if(!"".equals(txtIdCliente.getText())){
            int pregunta = JOptionPane.showConfirmDialog(null,"Esta Seguro de eliminar al cliente? " +txtClienteNombre.getText());
            if(pregunta == 0){
                int id = Integer.parseInt( txtIdCliente.getText());
                cld.EliminarCliente(id);
                LimpiarTabla();
                LimpiarCliente();
                ListarCliente();
            }
        
        }
    }//GEN-LAST:event_btnEliminarClienteActionPerformed

    private void btnActualizarClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarClientesActionPerformed
        // TODO add your handling code here:
        if("".equals(txtIdCliente.getText())){
            JOptionPane.showMessageDialog(null, "Seleccione una fila");
        
        }else{
            if(!"".equals(txtClienteNombre.getText()) || !"".equals(txtClienteDni.getText()) || !"".equals(txtClienteDomicilio.getText()) || !"".equals(txtClienteTelefono.getText()) || !"".equals(txtClienteEmail.getText()))
            {
            cl.setNombre(txtClienteNombre.getText());
            cl.setDni(Integer.parseInt(txtClienteDni.getText()));
            cl.setDomicilio(txtClienteDomicilio.getText());
            cl.setTelefono(txtClienteTelefono.getText());
            cl.setEmail(txtClienteEmail.getText());
            cl.setRazon(txtClienteRSocial.getText());
            cl.setId(Integer.parseInt(txtIdCliente.getText()));
            cld.ModificarCliente(cl);
            LimpiarTabla();
            LimpiarCliente();
            ListarCliente();
            
            }else{
            JOptionPane.showMessageDialog(null,"Todos los campos deben completarse");
            
            }
        }
    }//GEN-LAST:event_btnActualizarClientesActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        // TODO add your handling code here:
        LimpiarCliente();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnGuardarProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarProdActionPerformed
        // TODO add your handling code here:

        // Verificación rápida de campos obligatorios
        if (txtCodigoProducto.getText().isEmpty() || txtDescripcionProd.getText().isEmpty()
                || txtPrecioProd.getText().isEmpty() || txtCantidadProducto.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Los campos obligatorios están vacíos", "Advertencia", JOptionPane.WARNING_MESSAGE);
        } else {
            guardarProducto();
        }


    }//GEN-LAST:event_btnGuardarProdActionPerformed

    private void btnGuardarProvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarProvActionPerformed
        // TODO add your handling code here:
        if(!"".equals(txtCuitProv.getText())|| 
           !"".equals(txtNomProv.getText())||
           !"".equals(txtTelProv.getText())||
           !"".equals(txtDomicilioProv.getText())||
           !"".equals(txtEmailProv.getText()))
        {
        pr.setCuit(txtCuitProv.getText());
        pr.setNombre(txtNomProv.getText());
        pr.setTelefono(txtTelProv.getText());
        pr.setDomicilio(txtDomicilioProv.getText());
        pr.setEmail(txtEmailProv.getText());
        prDao.RegistrarProveedor(pr);
        LimpiarTabla();
        ListarProveedor();
        LimpiarProveedor();
        }else{
        JOptionPane.showMessageDialog(null, "Los campos no deben estar vacios");
        }
    }//GEN-LAST:event_btnGuardarProvActionPerformed

    private void tablaProveedoresMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaProveedoresMouseClicked
        // TODO add your handling code here:
        int fila =tablaProveedores.rowAtPoint(evt.getPoint());
        txtIdProveedor.setText(tablaProveedores.getValueAt(fila,0).toString());
        txtCuitProv.setText(tablaProveedores.getValueAt(fila,1).toString());
        txtNomProv.setText(tablaProveedores.getValueAt(fila,2).toString());
        txtTelProv.setText(tablaProveedores.getValueAt(fila, 3).toString());
        txtDomicilioProv.setText(tablaProveedores.getValueAt(fila,4).toString());
        txtEmailProv.setText(tablaProveedores.getValueAt(fila,5).toString());
    }//GEN-LAST:event_tablaProveedoresMouseClicked

    private void btnEliminarProvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarProvActionPerformed
        // TODO add your handling code here:
        if(!"".equals(txtIdProveedor.getText())){
            int pregunta = JOptionPane.showConfirmDialog(null,"Está seguro que quiere borrar al Proveedor? " + txtNomProv.getText());
                if(pregunta==0){
                    int id = Integer.parseInt(txtIdProveedor.getText());
                    prDao.EliminarProveedor(id);
                    LimpiarTabla();
                    ListarProveedor();
                    LimpiarProveedor();
                }
        }else{
                
                    JOptionPane.showMessageDialog(null, "Los Campos están vacios, seleccione una FILA");
                
                }
    }//GEN-LAST:event_btnEliminarProvActionPerformed

    private void btnActualizarProvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarProvActionPerformed
        // TODO add your handling code here:
        if("".equals(txtIdProveedor.getText())){
        
                JOptionPane.showMessageDialog(null, "Seleccione una fila");
        
        }else{
            if(!"".equals(txtNomProv.getText())||
               !"".equals(txtDomicilioProv.getText())||
               !"".equals(txtTelProv.getText())||     
               !"".equals(txtEmailProv.getText())||
               !"".equals(txtCuitProv.getText()))
            {
                pr.setCuit(txtCuitProv.getText());
                pr.setNombre(txtNomProv.getText());
                pr.setDomicilio(txtDomicilioProv.getText());
                pr.setTelefono(txtTelProv.getText());
                pr.setEmail(txtEmailProv.getText());
                pr.setId(Integer.parseInt(txtIdProveedor.getText()));
                
                prDao.ModificarProveedor(pr);
                LimpiarTabla();
                ListarProveedor();
                LimpiarProveedor();
                
                
            }
        
        
        }
    }//GEN-LAST:event_btnActualizarProvActionPerformed

    private void btnNuevoProvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoProvActionPerformed
        // TODO add your handling code here:
        LimpiarProveedor();
    }//GEN-LAST:event_btnNuevoProvActionPerformed

    private void tablaProductosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaProductosMouseClicked

    int fila = tablaProductos.rowAtPoint(evt.getPoint());
    if (fila == -1) return; // Seguridad por si se hace clic fuera de las filas

    // Carga de campos de texto básicos
    txtIdProd.setText(tablaProductos.getValueAt(fila, 0).toString());
    txtCodigoProducto.setText(tablaProductos.getValueAt(fila, 1).toString());
    txtDescripcionProd.setText(tablaProductos.getValueAt(fila, 2).toString());
    txtCantidadProducto.setText(tablaProductos.getValueAt(fila, 3).toString());
    txtPrecioProd.setText(tablaProductos.getValueAt(fila, 6).toString());
    txtPrecioCompra.setText(tablaProductos.getValueAt(fila, 5).toString());
    bxProveedor.setSelectedItem(tablaProductos.getValueAt(fila, 7).toString());
    
        // Suponiendo que en tu tabla mostrás el estado 'Apto Web' (true/false) en alguna columna, por ejemplo la 5:
        Object valorWeb = tablaProductos.getValueAt(fila, 11);
        if (valorWeb instanceof Boolean) {
            chkWeb.setSelected((Boolean) valorWeb);
        } else {
            // Si en la tabla lo mostrás como texto "Sí"/"No" o "1"/"0", lo evaluás:
            chkWeb.setSelected(valorWeb.toString().equals("true") || valorWeb.toString().equals("1"));
        }

    // 🟢 OBTENER EL ID del producto
    int id = Integer.parseInt(txtIdProd.getText());

    // 🟢 CARGAR EL PRODUCTO COMPLETO DESDE LA BD
    // Asegúrate de que prodao.BuscarProductoPorId traiga material y piedra
    Productos pro = prodao.BuscarProductoPorId(id);

    if (pro != null) {
        // 1. Cargar Categoría (Por ID)
        seleccionarItemPorId(cbxCategoria, pro.getIdCategoria());

        // 2. Cargar Material (Enum)
        if (pro.getMaterial() != null) {
            try {
                // valueOf convierte el String "ORO_18K" al objeto Material.ORO_18K
                Material mat = Material.valueOf(pro.getMaterial());
                comboMateriales.setSelectedItem(mat);
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Material no encontrado en Enum: " + pro.getMaterial());
            }
        }

        // 3. Cargar Piedra (Enum)
        if (pro.getPiedra() != null) {
            try {
                Piedra pie = Piedra.valueOf(pro.getPiedra());
                comboPiedras.setSelectedItem(pie);
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Piedra no encontrada en Enum: " + pro.getPiedra());
                comboPiedras.setSelectedItem(Piedra.NINGUNA);
            }
        }
    }  
    }//GEN-LAST:event_tablaProductosMouseClicked

    private void btnEliminarProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarProdActionPerformed
        // TODO add your handling code here:
         if(!"".equals(txtIdProd.getText())){
            int pregunta = JOptionPane.showConfirmDialog(null,"Esta Seguro de eliminar el producto? "+ txtDescripcionProd.getText());
            if(pregunta == 0){
                int id = Integer.parseInt( txtIdProd.getText());
                prodao.EliminarProducto(id);
                //ExportarStock.exportar(); // Actualiza el Json Salida
                LimpiarTabla();
                LimpiarProductos();
                ListarProductos();
               
            }
        
        }else{
             JOptionPane.showMessageDialog(null,"Debe seleccionar al menos un (1) Producto");
         
         }
    }//GEN-LAST:event_btnEliminarProdActionPerformed

    private void btnNuevoProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoProdActionPerformed
        // TODO add your handling code here:
        LimpiarTabla();
        LimpiarProductos();
        ListarProductos();
    }//GEN-LAST:event_btnNuevoProdActionPerformed

    private void btnEditarProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarProdActionPerformed
        
       // 1. Validaciones iniciales
    if (txtIdProd.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Seleccione una fila de la tabla");
        return;
    }

    if (txtCodigoProducto.getText().isEmpty() || txtDescripcionProd.getText().isEmpty() || 
        txtCantidadProducto.getText().isEmpty() || txtPrecioProd.getText().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Los campos principales son obligatorios");
        return;
    }

    try {
        // 2. Capturar estado anterior desde la Base de Datos
        Productos proAnterior = prodao.BuscarProducto(txtCodigoProducto.getText());
        BigDecimal stockAnterior = (proAnterior != null) ? proAnterior.getStock() : BigDecimal.ZERO;
        
        // 🔥 CRUCIAL: Guardamos el ID de WooCommerce que ya tenía el producto en la DB 
        // para que no se pise con 0 al modificar
        int idWcExistente = (proAnterior != null) ? proAnterior.getIdWooCommerce() : 0;

        // 3. Llenar objeto con nuevos datos
        pro.setId(Integer.parseInt(txtIdProd.getText()));
        pro.setCodigo(txtCodigoProducto.getText());
        pro.setDescripcion(txtDescripcionProd.getText());
        pro.setProveedor(bxProveedor.getSelectedItem().toString());
        
        // Manejo de decimales seguro
        pro.setStock(new BigDecimal(txtCantidadProducto.getText().replace(",", ".")));
        pro.setPrecio(new BigDecimal(txtPrecioProd.getText().replace(",", ".")));
        
        BigDecimal pCompra = txtPrecioCompra.getText().isEmpty() ? 
                             BigDecimal.ZERO : new BigDecimal(txtPrecioCompra.getText().replace(",", "."));
        pro.setPrecioCompra(pCompra);

        // Categoría, Material y Piedra
        Item catItem = (Item) cbxCategoria.getSelectedItem();
        Material mat = (Material) comboMateriales.getSelectedItem();
        Piedra pie = (Piedra) comboPiedras.getSelectedItem(); // Ajustá según tu paquete

        if (catItem == null || catItem.getId() == 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoría válida");
            return;
        }
        
        pro.setIdCategoria(catItem.getId());
        pro.setMaterial(mat.name());
        pro.setPiedra(pie.name());
        
        // Asignamos los estados de la Web correctamente
        pro.setWeb(chkWeb.isSelected());
        pro.setIdWooCommerce(idWcExistente); // <- Le devolvemos su ID real para no perderlo

        // 4. Ejecutar Actualización Local
        if (prodao.ModificarProductos(pro)) {
            
            // --- Lógica de Movimientos de Stock ---
            BigDecimal stockNuevo = pro.getStock();
            BigDecimal diferencia = stockNuevo.subtract(stockAnterior);

            if (diferencia.compareTo(BigDecimal.ZERO) != 0) {
                Modelo.MovimientoStock mov = new Modelo.MovimientoStock();
                mov.setIdProducto(pro.getId());
                mov.setTipoMovimiento(diferencia.compareTo(BigDecimal.ZERO) > 0 ? "ENTRADA" : "SALIDA");
                mov.setCantidad(Math.abs(diferencia.doubleValue()));
                mov.setMotivo("AJUSTE DE STOCK MANUAL");
                new Modelo.MovimientoDao().registrarMovimiento(mov);
            }

            JOptionPane.showMessageDialog(this, "Producto actualizado correctamente");
            
            // --- HILO DE SINCRONIZACIÓN WEB INTELIGENTE ---
            SincronizadorWooCommerce sincro = new SincronizadorWooCommerce();
            Productos proFresco = prodao.BuscarProducto(pro.getCodigo());

            if (proFresco != null) {
                // CASO A: Si activó el checkbox de la web y el producto NUNCA estuvo vinculado antes
                if (chkWeb.isSelected() && proFresco.getIdWooCommerce() <= 0) {
                    sincro.vincularProductoIndividualConWeb(proFresco);
                } 
                // CASO B: Si el checkbox está activo y el producto YA está vinculado
                else if (chkWeb.isSelected() && proFresco.getIdWooCommerce() > 0) {
                    sincro.sincronizarProducto(proFresco);
                }
            }
            
            // Refrescar vista
            LimpiarTabla();
            LimpiarProductos();
            ListarProductos();
        } else {
            JOptionPane.showMessageDialog(this, "Error al modificar en la base de datos");
        }

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Error en el formato de los números");
    }

    }//GEN-LAST:event_btnEditarProdActionPerformed

    private void txtCodigoProVentaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoProVentaKeyPressed
       
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {

        if (!"".equals(txtCodigoProVenta.getText())) {

            String cod = txtCodigoProVenta.getText();

            pro = prodao.BuscarProducto(cod);

            if (pro.getDescripcion() != null) {

                txtDescripcionVenta.setText(pro.getDescripcion());

                // Precio en BigDecimal
                BigDecimal precio = pro.getPrecio();

                // Redondear hacia arriba (equivalente a Math.ceil)
                BigDecimal precioRedondeado = precio.setScale(0, RoundingMode.CEILING);

                txtPrecioVenta.setText(precioRedondeado.toString());
                txtStockVenta.setText(pro.getStock().toString());

                txtCantidadVenta.requestFocus();

            } else {

                LimpiarVenta();
                txtCodigoProVenta.requestFocus();

            }

        } else {
            JOptionPane.showMessageDialog(null, "Ingrese el código del producto");
            txtCodigoProVenta.requestFocus();
        }
    
}


    }//GEN-LAST:event_txtCodigoProVentaKeyPressed

    private void txtCantidadVentaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadVentaKeyPressed
        
     if (evt.getKeyCode() == KeyEvent.VK_ENTER) {

        if (!"".equals(txtCantidadVenta.getText())) {

            try {
                String cod = txtCodigoProVenta.getText();
                String descripcion = txtDescripcionVenta.getText();

                // Cantidad como BigDecimal (PERMITE DECIMALES)
                BigDecimal cant = new BigDecimal(
                        txtCantidadVenta.getText().replace(",", ".")
                );

                // Precio como BigDecimal
                BigDecimal precio = new BigDecimal(
                        txtPrecioVenta.getText().replace(",", ".")
                ).setScale(2, RoundingMode.HALF_UP);

                // total = precio * cantidad
                BigDecimal total = precio.multiply(cant);

                // descuentos
                if (radioEfectivo.isSelected()) {
                    BigDecimal desc = new BigDecimal(
                            txtDescuentoEfectivo.getText().replace(",", ".")
                    ).divide(BigDecimal.valueOf(100));

                    total = total.multiply(BigDecimal.ONE.subtract(desc));

                } else if (radioDebito.isSelected()) {

                    BigDecimal desc = new BigDecimal(
                            txtDescuentoTransferencia.getText().replace(",", ".")
                    ).divide(BigDecimal.valueOf(100));

                    total = total.multiply(BigDecimal.ONE.subtract(desc));
                }

                // Stock también en BigDecimal
                BigDecimal stock = new BigDecimal(
                        txtStockVenta.getText().replace(",", ".")
                );

                if (stock.compareTo(cant) >= 0) {

                    item++;
                    Tmp = (DefaultTableModel) tablaNuevaVenta.getModel();

                    // evitar duplicados
                    for (int i = 0; i < Tmp.getRowCount(); i++) {
                        if (Tmp.getValueAt(i, 1).equals(descripcion)) {
                            JOptionPane.showMessageDialog(null, "El producto ya está registrado");
                            LimpiarVenta();
                            txtCodigoProVenta.requestFocus();
                            return;
                        }
                    }

                    Object[] fila = new Object[5];
                    fila[0] = cod;
                    fila[1] = descripcion;
                    fila[2] = cant.toPlainString();    // conserva decimales exactos
                    fila[3] = precio.toPlainString();
                    fila[4] = total.setScale(2, RoundingMode.HALF_UP).toPlainString();

                    Tmp.addRow(fila);
                    tablaNuevaVenta.setModel(Tmp);

                    TotalPagar();
                    LimpiarVenta();
                    txtCodigoProVenta.requestFocus();

                } else {
                    JOptionPane.showMessageDialog(null, "Stock no disponible");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error en los datos numéricos: " + ex.getMessage());
            }

        } else {
            JOptionPane.showMessageDialog(null, "Ingrese cantidad");
        }
    
}




    }//GEN-LAST:event_txtCantidadVentaKeyPressed

    private void btnEliminarNVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarNVentaActionPerformed
        // TODO add your handling code here:
        if (tablaNuevaVenta.getSelectedRow() == -1) {

            JOptionPane.showInternalMessageDialog(null, "Debe seleccionar un articulo para eliminar", "ELIMINAR", 0);

        } else {
            modelo = (DefaultTableModel) tablaNuevaVenta.getModel();
            modelo.removeRow(tablaNuevaVenta.getSelectedRow());
            TotalPagar();
            txtCodigoProVenta.requestFocus();
            aplicarDescuento();
        }
        
        
    }//GEN-LAST:event_btnEliminarNVentaActionPerformed

    private void txtDniKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDniKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            
            if (!"".equals(txtDni.getText())) {
                
                cl = cld.BuscarCliente(Integer.parseInt(txtDni.getText()));
                if (cl.getNombre() != null) {
                    
                    txtNombre.setText("" + cl.getNombre());
                    txtEmail.setText("" + cl.getEmail());
                    txtDireccionCV.setText("" + cl.getDomicilio());
                    txtDomicilioClienteNV.setText("" + cl.getDomicilio());
                    txtTelefonoCV.setText("" + cl.getTelefono());
                    txtRazonCV.setText("" + cl.getRazon());
                    
                } else {
                    txtDni.setText("");
                    JOptionPane.showMessageDialog(null, "El Cliente No Existe");
                    
                }
            }
            
        }
    }//GEN-LAST:event_txtDniKeyPressed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed

        if (radioCredito.isSelected() == false && radioDebito.isSelected() == false && radioEfectivo.isSelected() == false) {

            JOptionPane.showMessageDialog(null, "DEBE SELECCIONAR FORMA DE PAGO", "FORMA DE PAGO",0);

        } else {

            if ((tablaNuevaVenta.getRowCount() > 0)) {
                if (!"".equals(txtNombre.getText())) {

                    if (radioDebito.isSelected()) { //Venta Con DEbito Transferencia
                       // RegistrarVentaTransferencia();
                       
                        RegistrarDetalleTransferencia();
                        RegistrarVentaDebitoTransferencia("Transferencia");

                    }

                    if (radioEfectivo.isSelected()) {// Venta en Efectivo
                        RegistrarVentaEfectivo();
                        RegistrarDetalleEfectivo();
                        RegistrarCaja();
                        String totalCargaCaja = String.valueOf(cajadao.CargarTxtMontoCaja());
                        txtEfectivoCaja.setText(totalCargaCaja);

                    }

                    if (radioCredito.isSelected()) { //Venta con tarjeta de crédito 

                        RegistrarVenta();
                        RegistrarDetalle();

                    }
                    
                    if(radioCuenta.isSelected()){
                        int idVenta;
                        idVenta = Vdao.IdVenta();
                        RegistrarVenta();
                        RegistrarDetalle();
                        CuentaCorriente Cc = new CuentaCorriente();
                        Cc.setIdVenta(idVenta);
                        Cc.setCliente(txtClienteNombre.getText());
                        Cc.setTotal(V.getTotal());
                        Cc.setSaldo(V.getTotal());
                        Cc.setFecha(LocalDate.now().toString());
                        
                        new CuentaCorrienteDao().registrarCuenta(Cc);
                        
                    
                    }

                    ActualizarStock(); 
                    //ExportarStock.exportar(); // Actualiza el Json Salida
                    pdf();
                    LimpiarTablaVenta();
                    LimpiarDatosCliente();
                    labelTotalAPagar.setText("------------");
                    LabelSubtotal.setText("------------");
                } else {
                   // JOptionPane.showMessageDialog(null, "Debe ingresar el Nombre del Cliente");
                   txtDni.setText("00");
                   txtNombre.setText("Consumidor Final");
                   txtEmail.setText("email@gmail.com");
                   txtDomicilioClienteNV.setText("Calle S/N");
                   txtRazonCV.setText("");
                   txtDireccionCV.setText("Calle S/N");
                   txtTelefonoCV.setText("S/N");
                   
                    if (radioDebito.isSelected()) { //Venta Con DEbito Transferencia
                        //RegistrarVentaTransferencia();
                        
                        RegistrarDetalleTransferencia();
                        RegistrarVentaDebitoTransferencia("Transferencia");
                    }

                    if (radioEfectivo.isSelected()) {// Venta en Efectivo
                        RegistrarVentaEfectivo();
                        RegistrarDetalleEfectivo();
                        RegistrarCaja();
                        String totalCargaCaja = String.valueOf(cajadao.CargarTxtMontoCaja());
                        txtEfectivoCaja.setText(totalCargaCaja);

                    }

                    if (radioCredito.isSelected()) { //Venta con tarjeta de crédito 

                        RegistrarVenta();
                        RegistrarDetalle();

                    }

                    ActualizarStock(); 
                   // ExportarStock.exportar(); // Actualiza el Json Salida
                    pdf();
                   
                    LimpiarTablaVenta();
                    LimpiarDatosCliente();
                    labelTotalAPagar.setText("------------");
                    LabelSubtotal.setText("------------");
                }

            } else {
                JOptionPane.showMessageDialog(null, "Debe agregar un poducto");
            }

        }


    }//GEN-LAST:event_btnImprimirActionPerformed

    private void txtCantidadVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCantidadVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantidadVentaActionPerformed

    private void txtCodigoProVentaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoProVentaKeyTyped
        // TODO add your handling code here:
        //event.numberKeyPress(evt);
    }//GEN-LAST:event_txtCodigoProVentaKeyTyped

    private void txtCodigoProductoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoProductoKeyTyped
        // TODO add your handling code here:
        //event.numberKeyPress(evt);
    }//GEN-LAST:event_txtCodigoProductoKeyTyped

    private void txtCantidadProductoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadProductoKeyTyped
       
        event.numberKeyPress(evt);

    }//GEN-LAST:event_txtCantidadProductoKeyTyped

    private void txtPrecioProdKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPrecioProdKeyTyped
        // TODO add your handling code here:
        event.numberDecimalKeyPress(evt,txtPrecioProd);
    }//GEN-LAST:event_txtPrecioProdKeyTyped

    private void btnActualizarConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarConfigActionPerformed
        // TODO add your handling code here:
        if (!"".equals(txtCuitConfig.getText())
                || !"".equals(txtDomicilioConfig.getText())
                || !"".equals(txtNombreCongig.getText())
                || !"".equals(txtTelefonoConfig.getText())
                || !"".equals(txtEntradaJson.getText())
                || !"".equals(txtSalidaJson.getText())
                || !"".equals(txtDescuentoEfectivo.getText())
                || !"".equals(txtDescuentoTransferencia.getText())
                || !"".equals(txtDescuentoCredito.getText())) {

            conf.setCuit(txtCuitConfig.getText());
            conf.setRazon(txtRazon.getText());
            conf.setNombre(txtNombreCongig.getText());
            conf.setDireccion(txtDomicilioConfig.getText());
            conf.setTelefono(txtTelefonoConfig.getText());
            conf.setId(Integer.parseInt(txtIdConfig.getText()));
            conf.setEntrada(txtEntradaJson.getText());
            conf.setSalida(txtSalidaJson.getText());
            conf.setEfectivo(new BigDecimal(txtDescuentoEfectivo.getText().replace(",", ".")));
            conf.setTransferencia(new BigDecimal(txtDescuentoTransferencia.getText().replace(",", ".")));
            conf.setCredito(new BigDecimal(txtDescuentoCredito.getText().replace(",", ".")));


            ConfDao.ModificarDatos(conf);

            JOptionPane.showMessageDialog(null, "Datos Actualizados");
            ListarConfig();
        } else {
            JOptionPane.showMessageDialog(null, "Todos los campos deben completarse");

        }

    }//GEN-LAST:event_btnActualizarConfigActionPerformed

    private void tablaVentasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaVentasMouseClicked
        // TODO add your handling code here:
        int fila = tablaVentas.rowAtPoint(evt.getPoint());
        txtIdVentas.setText(tablaVentas.getValueAt(fila,0).toString());
        
        
    }//GEN-LAST:event_tablaVentasMouseClicked

    private void btnPdfVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPdfVentasActionPerformed
        // TODO add your handling code here:
        try {
            int id = Integer.parseInt(txtIdVentas.getText());
            String userHome = System.getProperty("user.home");
            File file = new File(userHome + "/Documents/Canarias/Comprobantes/venta" + id + ".pdf");

            
            Desktop.getDesktop().open(file);
            
        } catch (IOException | NumberFormatException e) {
        }
        
    }//GEN-LAST:event_btnPdfVentasActionPerformed

    private void btnFiltrarNomYFechaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFiltrarNomYFechaMouseClicked

    }//GEN-LAST:event_btnFiltrarNomYFechaMouseClicked

    private void btnFiltrarNomYFechaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFiltrarNomYFechaActionPerformed
  
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
    // Validar selección de tipo de filtro
    if (!radioUsuarios.isSelected() && !radioVentas.isSelected()) {
        JOptionPane.showMessageDialog(null, "Debe seleccionar USUARIO o VENTAS", "FILTRO", JOptionPane.WARNING_MESSAGE);
        return;
    }
//     JOptionPane.showMessageDialog(null, miFechaBuscar.getDate() +""+ miFechaBuscar2.getDate());
    // Validar fechas
    if (miFechaBuscar.getDate()== null || miFechaBuscar2.getDate()== null) {
        JOptionPane.showMessageDialog(null, "Debe seleccionar ambas fechas", "FILTRO", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    
    String fechaIni = sdf.format(miFechaBuscar.getDate());
    String fechaFinal = sdf.format(miFechaBuscar2.getDate());

    // Limpiar la tabla antes de filtrar
    LimpiarTabla();

    // Filtrar por usuario
    if (radioUsuarios.isSelected()) {
        if (cbxUsuarios.getSelectedItem() == null || cbxUsuarios.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar un usuario", "FILTRO", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = cbxUsuarios.getSelectedItem().toString().trim();
        FiltrarEntreYFechasNombre(fechaIni, fechaFinal, nombre);
        TotalPagarFiltro();
        TotalEfectivoFiltro();
    }

    // Filtrar por ventas sin usuario
    if (radioVentas.isSelected()) {
        FiltrarFecha(fechaIni, fechaFinal);
        TotalPagarFiltro();
        TotalEfectivoFiltro();

    }//GEN-LAST:event_btnFiltrarNomYFechaActionPerformed
    }
    private void bxProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bxProveedorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bxProveedorActionPerformed

    private void bxProveedorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bxProveedorMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_bxProveedorMouseClicked

    private void bxProveedorComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_bxProveedorComponentHidden
        // TODO add your handling code here:
    }//GEN-LAST:event_bxProveedorComponentHidden

    private void radioCreditoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioCreditoActionPerformed
        // TODO add your handling code here:
        aplicarDescuento();
        //totalPagarCredito();
        lblSeleccionFdePago.setText("Debito-Credito");
    }//GEN-LAST:event_radioCreditoActionPerformed

    private void radioEfectivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioEfectivoActionPerformed
        // TODO add your handling code here:
        aplicarDescuento();
        //TotalPagarEfectivo();
        lblSeleccionFdePago.setText("Efectivo");
    }//GEN-LAST:event_radioEfectivoActionPerformed

    private void radioDebitoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioDebitoActionPerformed
        // TODO add your handling code here:
        aplicarDescuento();
       // TotalPagarDEbito();
        lblSeleccionFdePago.setText("Transferencia");
    }//GEN-LAST:event_radioDebitoActionPerformed

    private void txtRetirarEfectivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRetirarEfectivoActionPerformed
        // TODO add your handling code here:
    
    }//GEN-LAST:event_txtRetirarEfectivoActionPerformed

    private void btnIngresarEfectivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIngresarEfectivoActionPerformed
                                                        
    if (txtIngresarEfectivo.getText().trim().isEmpty()) {

        JOptionPane.showMessageDialog(null, "DEBE INDICAR UN MONTO PARA INGRESAR");

    } else {

        int id = cajadao.idCaja();

        // Obtener monto actual con BigDecimal
        BigDecimal montoActual = cajadao.obtenerMontoActual(id);

        // Convertir lo que ingresó el usuario a BigDecimal
        BigDecimal montoCargado = new BigDecimal(txtIngresarEfectivo.getText().replace(",", "."));

        // No hay salida en este caso
        BigDecimal salida = BigDecimal.ZERO;

        // Sumar el monto
        BigDecimal nuevoMonto = montoActual.add(montoCargado);

        caja.setEntrada(montoCargado);
        caja.setSalida(salida);
        caja.setFecha(fechaActual);
        caja.setMonto(nuevoMonto);

        cajadao.RegistrarMovimiento(caja);

        JOptionPane.showMessageDialog(null, "Se ingresaron con éxito $" + montoCargado);

        txtIngresarEfectivo.setText("");
        txtIngresarEfectivo.requestFocus();

        // Mostrar el nuevo monto en la caja
        txtEfectivoCaja.setText(nuevoMonto.toString());

        LimpiarTabla();
        ListarCajasSistema();
    }

    }//GEN-LAST:event_btnIngresarEfectivoActionPerformed

    private void txtIngresarEfectivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIngresarEfectivoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIngresarEfectivoActionPerformed

    private void radioUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioUsuariosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_radioUsuariosActionPerformed

    private void btnRetirarEfectivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRetirarEfectivoActionPerformed
                                                         

    if ("".equals(txtRetirarEfectivo.getText())) {
        JOptionPane.showMessageDialog(null, "Debe ingresar un monto");
        return;
    }

    try {

        // Convertimos correctamente a BigDecimal
        BigDecimal retiro = new BigDecimal(txtRetirarEfectivo.getText().replace(",", "."));
        int id = cajadao.idCaja();

        BigDecimal montoActual = cajadao.obtenerMontoActual(id);
        if (montoActual == null) montoActual = BigDecimal.ZERO;

        int pregunta = JOptionPane.showConfirmDialog(
                null, "Vas a retirar: $" + retiro.toPlainString()
        );

        if (pregunta == 0) {

            // Nuevo monto
            BigDecimal nuevoMonto = montoActual.subtract(retiro);

            // Set de datos en la clase Caja
            caja.setEntrada(BigDecimal.ZERO);
            caja.setSalida(retiro);
            caja.setFecha(fechaActual);
            caja.setMonto(nuevoMonto);

            cajadao.RegistrarMovimiento(caja);

            JOptionPane.showMessageDialog(null, 
                "Se retiraron $" + retiro.toPlainString());

            txtRetirarEfectivo.setText("");
            txtIngresarEfectivo.setText("");
            txtIngresarEfectivo.requestFocus();

            // Vuelvo a cargar el monto en caja
            BigDecimal totalCaja = cajadao.CargarTxtMontoCaja();
            txtEfectivoCaja.setText(totalCaja.toPlainString());

            LimpiarTabla();
            ListarCajasSistema();
        }

    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(null, 
            "Monto inválido. Solo números (usa punto para decimales).");
    }


    }//GEN-LAST:event_btnRetirarEfectivoActionPerformed

    private void txtNombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNombreActionPerformed

    private void txtBuscarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarProductoActionPerformed
     


    }//GEN-LAST:event_txtBuscarProductoActionPerformed

    private void txtBuscarProductoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarProductoKeyPressed
   

    }//GEN-LAST:event_txtBuscarProductoKeyPressed

    private void txtDescuentoEfectivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescuentoEfectivoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDescuentoEfectivoActionPerformed

    private void jTabbedPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPane1MouseClicked
        // TODO add your handling code here:
       
    }//GEN-LAST:event_jTabbedPane1MouseClicked

    private void productosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_productosMouseClicked
        // TODO add your handling code here:
        LimpiarTabla();
        ListarProductos();
    }//GEN-LAST:event_productosMouseClicked

    private void proveedorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_proveedorMouseClicked
        // TODO add your handling code here:
        LimpiarTabla();
        ListarProveedor();
    }//GEN-LAST:event_proveedorMouseClicked

    private void EfectivoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_EfectivoMouseClicked
        // TODO add your handling code here:
         LimpiarTabla();
         ListarCajasSistema();
      
    }//GEN-LAST:event_EfectivoMouseClicked

    private void ventasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ventasMouseClicked
        // TODO add your handling code here:
        LimpiarTabla();
        ListarVentas();
        TotalPagarFiltro();
    }//GEN-LAST:event_ventasMouseClicked

    private void clientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clientesMouseClicked
        // TODO add your handling code here:
        LimpiarTabla();
        ListarCliente();
    }//GEN-LAST:event_clientesMouseClicked

    private void txtCodigoProVentaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoProVentaKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoProVentaKeyReleased

    private void txtBuscarProductoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarProductoKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBuscarProductoKeyTyped

    private void txtPrecioProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPrecioProdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPrecioProdActionPerformed

    private void btnCrearCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearCategoriaActionPerformed
        // TODO add your handling code here:
        CategoriasView cv = new CategoriasView();
        cv.setLocationRelativeTo(null); // centrar pantalla
        cv.setVisible(true);

    }//GEN-LAST:event_btnCrearCategoriaActionPerformed

  
    
    
    private void cbxCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxCategoriaActionPerformed

//        // 1. Validar selección
//        Item item = (Item) cbxCategoria.getSelectedItem();
//        if (item == null || item.getId() == 0) {
//            return;
//        }
//
//        if (txtIdProd.getText().isEmpty()) {
//            String nuevoCod = prodao.generarCodigoConPrefijo(item.getId(), item.getNombre());
//            txtCodigoProducto.setText(nuevoCod);
//        }
//
//        if (cbxCategoria.getSelectedItem() == null) {
//            return;
//        }
//
//        // Si es un item vacío o placeholder
//        if (item.getId() == 0) {
//            return;
//        }
//
//        int idCategoria = item.getId();  // ← ESTE ES EL ID REAL

    }//GEN-LAST:event_cbxCategoriaActionPerformed

    private void jMenuItemProductosCargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemProductosCargarActionPerformed
        // TODO add your handling code here:
        LimpiarTabla();
        ListarProductos();
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jMenuItemProductosCargarActionPerformed

    private void jmenuProductosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmenuProductosActionPerformed
        // TODO add your handling code here:
//        CategoriasView cv = new CategoriasView();
//        cv.setLocationRelativeTo(null); // centrar pantalla
//        cv.setVisible(true);
    
    }//GEN-LAST:event_jmenuProductosActionPerformed

    private void jMenuItemNuevaVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNuevaVentaActionPerformed
        // TODO add your handling code here:
        abrirNuevaVenta();
         //jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jMenuItemNuevaVentaActionPerformed

    private void jMenuItemDevolucionesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDevolucionesActionPerformed
        // TODO add your handling code here:
        String usuarioLogueado;
        usuarioLogueado = jLabelVendedor.getText();
        new DevolucionesView(usuarioLogueado).setVisible(true);
    }//GEN-LAST:event_jMenuItemDevolucionesActionPerformed

    private void jMenuVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuVentasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuVentasActionPerformed

    private void jMenuAnularVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAnularVentaActionPerformed
        // TODO add your handling code here:
        String usuarioLogueado;
        usuarioLogueado = jLabelVendedor.getText();
        new AnularVentaView(usuarioLogueado).setVisible(true);
    }//GEN-LAST:event_jMenuAnularVentaActionPerformed

    private void jMenuItemCatProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCatProductoActionPerformed
        // TODO add your handling code here:
        CategoriasView cv = new CategoriasView();
        cv.setLocationRelativeTo(null); // centrar pantalla
        cv.setVisible(true);
    }//GEN-LAST:event_jMenuItemCatProductoActionPerformed

    private void jMenuItemParametrosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemParametrosActionPerformed
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jMenuItemParametrosActionPerformed

    private void jMenuRegistroUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuRegistroUsuarioActionPerformed
        
        Registro rg = new Registro();
        rg.setVisible(true);
    }//GEN-LAST:event_jMenuRegistroUsuarioActionPerformed

    private void jMenuIReportesVariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuIReportesVariosActionPerformed
        // TODO add your handling code here:
        //String usuarioLogueado;
        //usuarioLogueado = jLabelVendedor.getText();
        new ReportesView().setVisible(true);
    }//GEN-LAST:event_jMenuIReportesVariosActionPerformed
    private ResumenCajaView resumenCaja;
    
    private void menuResumenCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuResumenCajaActionPerformed
        // TODO add your handling code here:
        String usuarioLogueado;
        usuarioLogueado = jLabelVendedor.getText();
        if (resumenCaja == null || !resumenCaja.isVisible()) {
            resumenCaja = new ResumenCajaView(usuarioLogueado);
            resumenCaja.setLocationRelativeTo(null);
            resumenCaja.setVisible(true);
        } else {
            resumenCaja.toFront();
            resumenCaja.requestFocus();
        }

    }//GEN-LAST:event_menuResumenCajaActionPerformed

    private void menuImpExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuImpExpActionPerformed
        
    abrirEditarProductos();

    }//GEN-LAST:event_menuImpExpActionPerformed

    private void cbxCategoriaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxCategoriaItemStateChanged
        // TODO add your handling code here:
        
    }//GEN-LAST:event_cbxCategoriaItemStateChanged

    
    
    private void menuCodBarrasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuCodBarrasActionPerformed
        // TODO add your handling code here:
        GeneradorCodigoBarrasView CView = new GeneradorCodigoBarrasView();
        CView.setVisible(true);
       
    }//GEN-LAST:event_menuCodBarrasActionPerformed

    private void jmenuProductosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jmenuProductosMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jmenuProductosMouseClicked

    private void btnPdfClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPdfClientesActionPerformed
        // TODO add your handling code here:
        ClienteDao clDao = new ClienteDao();
        List<Cliente> lista = clDao.ListarClientes(); //  método que trae todo de la DB
        ListadosService service = new ListadosService();
        try {
            service.generarReporteClientes(lista);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnPdfClientesActionPerformed

    private void listUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listUsuariosActionPerformed
        
        new ListaUsuariosView().setVisible(true);
    }//GEN-LAST:event_listUsuariosActionPerformed

    private void btnPdfProvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPdfProvActionPerformed
        
         new ListaProveedores().setVisible(true);
        
    }//GEN-LAST:event_btnPdfProvActionPerformed

    private void jMenuUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuUsuariosActionPerformed
        
    }//GEN-LAST:event_jMenuUsuariosActionPerformed

    private void cbxCategoriaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cbxCategoriaMouseClicked
        
    }//GEN-LAST:event_cbxCategoriaMouseClicked

    private void btnReCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReCategoriaActionPerformed
      
         cargarCategorias();
     
    }//GEN-LAST:event_btnReCategoriaActionPerformed

    private void itemBackupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemBackupActionPerformed
        // TODO add your handling code here:
        
    
        BackupService bs = new BackupService();
        bs.generarBackupManual();

    }//GEN-LAST:event_itemBackupActionPerformed

    private void btnDolarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDolarActionPerformed
        
        BigDecimal nuevoValorDolar = obtenerPrecioDolarFromTextField();

        // Si el valor es menor o igual a 1, asumimos que no es un valor de dólar real válido o hubo error
        if (nuevoValorDolar.compareTo(java.math.BigDecimal.ONE) <= 0) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un valor de dólar válido mayor a 1.", "Atención", JOptionPane.WARNING_MESSAGE);
            return; // Frenamos la ejecución antes de romper los precios de la base de datos
        }

        if (prodao.registrarTipoDeCambio(nuevoValorDolar)) {
            if (prodao.actualizarPrecioPorDolar(nuevoValorDolar)) {
                JOptionPane.showMessageDialog(this, "Precios actualizados por cambio de divisa localmente.");
                ListarProductos(); 
                
                SincronizadorWooCommerce sincro = new SincronizadorWooCommerce();
                sincro.sincronizarPreciosPostDolar();
            }
        }

    }//GEN-LAST:event_btnDolarActionPerformed

    private void txtCodigoProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoProductoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoProductoActionPerformed

    private void btnGenerarSKUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarSKUActionPerformed
   
        Item cat = (Item) cbxCategoria.getSelectedItem();
        Material mat = (Material) comboMateriales.getSelectedItem();
        Piedra pie = (Piedra) comboPiedras.getSelectedItem(); // Nuevo combo
        
        // 1. Validar que tengamos los datos necesarios
        if (cat == null || cat.getId() == 0) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una Categoría primero.");
            cbxCategoria.requestFocus();
            return;
        }

        if (mat == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Material.");
            comboMateriales.requestFocus();
            return;
        }
        
        if(pie == null){
             JOptionPane.showMessageDialog(this, "Debe seleccionar si tien o no Piedras.");
            comboPiedras.requestFocus();
            return;
        }

        // 2. Solo generar si el campo de código está vacío o es un producto nuevo
        // (Opcional: puedes quitar esta condición si quieres que el botón siempre lo regenere)
        if (txtCodigoProducto.getText().trim().isEmpty() || txtIdProd.getText().isEmpty()) {
            String sku = prodao.generarCodigoConPrefijo(cat.getId(), cat.getNombre(), mat, pie);
            txtCodigoProducto.setText(sku);
        } else {
            int confirmar = JOptionPane.showConfirmDialog(this,
                    "¿Desea sobreescribir el código actual?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirmar == JOptionPane.YES_OPTION) {
                String sku = prodao.generarCodigoConPrefijo(cat.getId(), cat.getNombre(), mat, pie);
                txtCodigoProducto.setText(sku);
            }
        }

   
    }//GEN-LAST:event_btnGenerarSKUActionPerformed

    private void jMenuProveedoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuProveedoresActionPerformed
       
        //jTabbedPane1.setSelectedIndex(2);
        //LimpiarTabla();
        //ListarProveedor();
        //jTabbedPane1.setSelectedIndex(2);
        
    }//GEN-LAST:event_jMenuProveedoresActionPerformed

    private void jMenuItemInformesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInformesActionPerformed
      
        
        jTabbedPane1.setSelectedIndex(4);
        LimpiarTabla();
        ListarVentas();
        TotalPagarFiltro();
    }//GEN-LAST:event_jMenuItemInformesActionPerformed

    private void jMenuClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuClientesActionPerformed
        LimpiarTabla();
        ListarCliente();
        VCP.setVisible(true);
        VCP.setLocationRelativeTo(null);
    }//GEN-LAST:event_jMenuClientesActionPerformed

    private void jMenuItemAbrirCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAbrirCajaActionPerformed
        String usuarioLogueado;
        usuarioLogueado = jLabelVendedor.getText();

        CajaView cv = new CajaView(usuarioLogueado);
        cv.setVisible(true);
    }//GEN-LAST:event_jMenuItemAbrirCajaActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
         // 1. Instanciamos la Vista profesional
        Vista.VistaEstadisticasArticulo vistaEst = new Vista.VistaEstadisticasArticulo();
        
        // 2. Instanciamos los DAOs necesarios
        Modelo.MovimientoDao movDao = new Modelo.MovimientoDao();
        Modelo.ProductosDao prodDao = new Modelo.ProductosDao();
        
        // 3. El Controlador toma el mando y vincula la vista con los datos
        // (Asegurate de que el constructor de EstadisticasController reciba estos 3 parámetros)
        Controlador.EstadisticasController ctrl = new Controlador.EstadisticasController(vistaEst, movDao, prodDao);
        // 4. Hacemos visible la ventana
        vistaEst.setVisible(true);
        
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        SincronizadorWooCommerce sincro = new SincronizadorWooCommerce();
        sincro.vincularProductosLocalesConWeb();

    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        SincronizadorWooCommerce sincro = new SincronizadorWooCommerce();
        sincro.descargarPedidosWeb();
        
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItemProveedoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemProveedoresActionPerformed
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(2);
        LimpiarTabla();
        ListarProveedor();
    }//GEN-LAST:event_jMenuItemProveedoresActionPerformed

    private void jMenuItemClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClientesActionPerformed
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(5);
        LimpiarTabla();
        ListarCliente();
    }//GEN-LAST:event_jMenuItemClientesActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        // TODO add your handling code here:
        new ListaProveedores().setVisible(true);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void sincroWOOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sincroWOOActionPerformed
        // TODO add your handling code here:
        Sinc.vincularProductosLocalesConWeb();
    }//GEN-LAST:event_sincroWOOActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Sistema.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Sistema.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Sistema.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Sistema.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Sistema().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Efectivo;
    private javax.swing.JLabel LabelSubtotal;
    private javax.swing.JPanel PrecioDolar;
    private javax.swing.JButton btnActualizarClientes;
    private javax.swing.JButton btnActualizarConfig;
    private javax.swing.JButton btnActualizarProv;
    private javax.swing.JButton btnCrearCategoria;
    private javax.swing.JButton btnDolar;
    private javax.swing.JButton btnEditarProd;
    private javax.swing.JButton btnEliminarCliente;
    private javax.swing.JButton btnEliminarNVenta;
    private javax.swing.JButton btnEliminarProd;
    private javax.swing.JButton btnEliminarProv;
    private javax.swing.JButton btnExcelProd;
    private javax.swing.JButton btnFiltrarNomYFecha;
    private javax.swing.JButton btnGenerarSKU;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnGuardarProd;
    private javax.swing.JButton btnGuardarProv;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIngresarEfectivo;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnNuevoProd;
    private javax.swing.JButton btnNuevoProv;
    private javax.swing.JButton btnPdfClientes;
    private javax.swing.JButton btnPdfProv;
    private javax.swing.JButton btnPdfVentas;
    private javax.swing.JButton btnReCategoria;
    private javax.swing.JButton btnRetirarEfectivo;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> bxProveedor;
    private javax.swing.JComboBox<Item> cbxCategoria;
    private javax.swing.JComboBox<String> cbxUsuarios;
    private javax.swing.JCheckBox chkWeb;
    private javax.swing.JPanel clientes;
    private javax.swing.JComboBox<Material> comboMateriales;
    private javax.swing.JComboBox<Piedra> comboPiedras;
    private javax.swing.JPanel configuracion;
    private javax.swing.ButtonGroup grupoFiltrar;
    private javax.swing.JPanel inicio;
    private javax.swing.JMenuItem itemBackup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelTotal;
    private javax.swing.JLabel jLabelVendedor;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuAnularVenta;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenu jMenuClientes;
    private javax.swing.JMenu jMenuConfiguracion;
    private javax.swing.JMenuItem jMenuIReportesVarios;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItemAbrirCaja;
    private javax.swing.JMenuItem jMenuItemCatProducto;
    private javax.swing.JMenuItem jMenuItemClientes;
    private javax.swing.JMenuItem jMenuItemDevoluciones;
    private javax.swing.JMenuItem jMenuItemInformes;
    private javax.swing.JMenuItem jMenuItemNuevaVenta;
    private javax.swing.JMenuItem jMenuItemParametros;
    private javax.swing.JMenuItem jMenuItemProductosCargar;
    private javax.swing.JMenuItem jMenuItemProveedores;
    private javax.swing.JMenu jMenuProveedores;
    private javax.swing.JMenuItem jMenuRegistroUsuario;
    private javax.swing.JMenu jMenuReportes;
    private javax.swing.JMenu jMenuUsuarios;
    private javax.swing.JMenu jMenuVentas;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelBotoneaCaja;
    private javax.swing.JPanel jPanelCargaProductos;
    private javax.swing.JPanel jPanelFormaDePago;
    private javax.swing.JPanel jPanelParametros;
    private javax.swing.JPanel jPanelTituloCaja;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JMenu jmenuProductos;
    private javax.swing.JLabel labelCreditoFiltro;
    private javax.swing.JLabel labelEfectivoAcum;
    private javax.swing.JLabel labelTotal;
    private javax.swing.JLabel labelTotalAPagar;
    private javax.swing.JLabel labelTotalFiltros;
    private javax.swing.JLabel labeltransferenciaFiltro;
    private javax.swing.JLabel lblSeleccionFdePago;
    private javax.swing.JMenuItem listUsuarios;
    private javax.swing.JMenuItem menuCodBarras;
    private javax.swing.JMenuItem menuImpExp;
    private javax.swing.JMenuItem menuResumenCaja;
    private com.toedter.calendar.JDateChooser miFechaBuscar;
    private com.toedter.calendar.JDateChooser miFechaBuscar2;
    private javax.swing.JPanel nuevaVenta;
    private javax.swing.JPanel panelBotones;
    private javax.swing.JPanel panelBtnsProductos;
    private javax.swing.JPanel productos;
    private javax.swing.JPanel proveedor;
    private javax.swing.JRadioButton radioCredito;
    private javax.swing.JRadioButton radioCuenta;
    private javax.swing.JRadioButton radioDebito;
    private javax.swing.JRadioButton radioDebito1;
    private javax.swing.JRadioButton radioEfectivo;
    private javax.swing.JRadioButton radioUsuarios;
    private javax.swing.JRadioButton radioVentas;
    private javax.swing.JButton sincroWOO;
    private javax.swing.JTable tablaCaja;
    private javax.swing.JTable tablaClientes;
    private javax.swing.JTable tablaNuevaVenta;
    private javax.swing.JTable tablaProductos;
    private javax.swing.JTable tablaProveedores;
    private javax.swing.JTable tablaVentas;
    private javax.swing.JTextField txtBuscarProducto;
    private javax.swing.JTextField txtCantidadProducto;
    private javax.swing.JTextField txtCantidadVenta;
    private javax.swing.JTextField txtClienteDni;
    private javax.swing.JTextField txtClienteDomicilio;
    private javax.swing.JTextField txtClienteEmail;
    private javax.swing.JTextField txtClienteNombre;
    private javax.swing.JTextField txtClienteRSocial;
    private javax.swing.JTextField txtClienteTelefono;
    private javax.swing.JTextField txtCodigoProVenta;
    private javax.swing.JTextField txtCodigoProducto;
    private javax.swing.JTextField txtCuitConfig;
    private javax.swing.JTextField txtCuitProv;
    private javax.swing.JTextField txtDescripcionProd;
    private javax.swing.JTextField txtDescripcionVenta;
    private javax.swing.JTextField txtDescuentoCredito;
    private javax.swing.JTextField txtDescuentoEfectivo;
    private javax.swing.JTextField txtDescuentoTransferencia;
    private javax.swing.JTextField txtDireccionCV;
    private javax.swing.JTextField txtDni;
    private javax.swing.JTextField txtDomicilioClienteNV;
    private javax.swing.JTextField txtDomicilioConfig;
    private javax.swing.JTextField txtDomicilioProv;
    private javax.swing.JTextField txtEfectivoCaja;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtEmailProv;
    private javax.swing.JTextField txtEntradaJson;
    private javax.swing.JTextField txtIdCliente;
    private javax.swing.JTextField txtIdConfig;
    private javax.swing.JTextField txtIdProd;
    private javax.swing.JTextField txtIdProdNV;
    private javax.swing.JTextField txtIdProveedor;
    private javax.swing.JTextField txtIdVentas;
    private javax.swing.JTextField txtIngresarEfectivo;
    private javax.swing.JTextField txtNomProv;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtNombreCongig;
    private javax.swing.JTextField txtPrecioCompra;
    private javax.swing.JTextField txtPrecioDolar;
    private javax.swing.JTextField txtPrecioProd;
    private javax.swing.JTextField txtPrecioVenta;
    private javax.swing.JTextField txtRazon;
    private javax.swing.JTextField txtRazonCV;
    private javax.swing.JTextField txtRetirarEfectivo;
    private javax.swing.JTextField txtSalidaJson;
    private javax.swing.JTextField txtStockVenta;
    private javax.swing.JTextField txtTelProv;
    private javax.swing.JTextField txtTelefonoCV;
    private javax.swing.JTextField txtTelefonoConfig;
    private javax.swing.JPanel ventas;
    // End of variables declaration//GEN-END:variables
   int contadorVentas = 1;

    private void LimpiarCliente() {
        txtIdCliente.setText("");
        txtClienteDni.setText("");
        txtClienteNombre.setText("");
        txtClienteDomicilio.setText("");
        txtClienteTelefono.setText("");
        txtClienteEmail.setText("");
        txtClienteRSocial.setText("");

    }

    private void LimpiarProveedor() {
        txtIdProveedor.setText("");
        txtCuitProv.setText("");
        txtNomProv.setText("");
        txtDomicilioProv.setText("");
        txtTelProv.setText("");
        txtEmailProv.setText("");

    }

    private void LimpiarProductos() {
        txtIdProd.setText("");
        txtCodigoProducto.setText("");
        txtDescripcionProd.setText("");
        txtCantidadProducto.setText("");
        txtPrecioProd.setText("");
        bxProveedor.setSelectedItem(null);
        txtPrecioCompra.setText("");

    }

    private void TotalPagar() {

        BigDecimal totalPagar = BigDecimal.ZERO;

        int numFila = tablaNuevaVenta.getRowCount();

        for (int i = 0; i < numFila; i++) {

            Object valorCelda = tablaNuevaVenta.getModel().getValueAt(i, 4);

            if (valorCelda == null) {
                continue;
            }

            String valorTexto = valorCelda.toString().replace(",", ".");

            try {

                BigDecimal valor = new BigDecimal(valorTexto);
                totalPagar = totalPagar.add(valor);

            } catch (Exception e) {
                System.out.println("Error al convertir valor de fila " + i + ": " + valorTexto);
            }
        }

        // ----> ESTA LÍNEA ES NECESARIA Y FALTABA <----
        totalpagar = totalPagar.setScale(2, RoundingMode.HALF_UP);

        // Mostramos el total
        LabelSubtotal.setText(totalpagar.toString());
        labelTotalAPagar.setText(totalpagar.toString());
    }

    private void TotalPagarFiltro() {
        totalpagar = BigDecimal.ZERO;   // reiniciamos con BigDecimal

        int numFila = tablaVentas.getRowCount();

        for (int i = 0; i < numFila; i++) {

            // Obtenemos el valor de la tabla (columna 3)
            String valorTexto = String.valueOf(
                    tablaVentas.getModel().getValueAt(i, 3)
            ).replace(",", ".");

            try {
                BigDecimal cal = new BigDecimal(valorTexto);

                totalpagar = totalpagar.add(cal);

            } catch (NumberFormatException e) {
                System.out.println("Error al convertir fila " + i + ": " + valorTexto);
            }
        }

        // Mostrar con 2 decimales
        labelTotalFiltros.setText(
                totalpagar.setScale(2, RoundingMode.HALF_UP).toString()
        );
    }

    private void TotalEfectivoFiltro() {

        totalEfectivo = BigDecimal.ZERO;
        totalTransferencia = BigDecimal.ZERO;
        totalCredito = BigDecimal.ZERO;

        int numFila = tablaVentas.getRowCount();

        for (int i = 0; i < numFila; i++) {

            String tipo = String.valueOf(tablaVentas.getModel().getValueAt(i, 5));
            String valorTexto = String.valueOf(tablaVentas.getModel().getValueAt(i, 3))
                    .replace(",", ".");

            try {
                BigDecimal cal = new BigDecimal(valorTexto);

                switch (tipo) {

                    case "Efectivo":
                        totalEfectivo = totalEfectivo.add(cal);
                        break;

                    case "Transferencia":
                        totalTransferencia = totalTransferencia.add(cal);
                        break;

                    case "Debito-Credito":
                        totalCredito = totalCredito.add(cal);
                        break;
                }

            } catch (NumberFormatException e) {
                System.out.println("Error al convertir fila " + i + ": " + valorTexto);
            }
        }

        // Mostrar valores con 2 decimales
        labelCreditoFiltro.setText(totalCredito.setScale(2, RoundingMode.HALF_UP).toString());
        labelEfectivoAcum.setText(totalEfectivo.setScale(2, RoundingMode.HALF_UP).toString());
        labeltransferenciaFiltro.setText(totalTransferencia.setScale(2, RoundingMode.HALF_UP).toString());
    }

    private void LimpiarVenta() {
        txtCodigoProVenta.setText("");
        txtCantidadVenta.setText("");
        txtDescripcionVenta.setText("");
        txtPrecioVenta.setText("");
        txtStockVenta.setText("");
        txtIdProdNV.setText("");

    }

    private void RegistrarVenta() {

        String cliente = txtNombre.getText();
        String vendedor = jLabelVendedor.getText();
        String fPago = lblSeleccionFdePago.getText();

        // totalpagar YA es BigDecimal, solo ajustamos a 2 decimales si hace falta
        BigDecimal total = totalpagar.setScale(2, RoundingMode.HALF_UP);

        V.setCliente(cliente);
        V.setVendedor(vendedor);
        V.setTotal(total);
        V.setFecha(fechaActual);
        V.setPago(fPago);
        V.setEstado("ACTIVA");

        Vdao.RegistrarVenta(V);
    }

//    private void RegistrarVentaTransferencia() {
//
//        String cliente = txtNombre.getText();
//        String vendedor = jLabelVendedor.getText();
//        String fPago = lblSeleccionFdePago.getText();
//        BigDecimal total = totalpagar.setScale(2, RoundingMode.HALF_UP);
//        V.setCliente(cliente);
//        V.setVendedor(vendedor);
//        V.setTotal(total);
//        V.setFecha(fechaActual);
//        V.setPago(fPago);
//        Vdao.RegistrarVenta(V);
//    }

    private void RegistrarVentaDebitoTransferencia(String tipoPago) {

        // tipoPago debe ser: "DEBITO" o "TRANSFERENCIA"
        String cliente = txtNombre.getText();
        String vendedor = jLabelVendedor.getText();
        BigDecimal total = totalpagar.setScale(2, RoundingMode.HALF_UP);

        Venta v = new Venta();
        v.setCliente(cliente);
        v.setVendedor(vendedor);
        v.setTotal(total);
        v.setFecha(fechaActual);
        v.setPago(tipoPago);

        VentaDao vdao = new VentaDao();
        vdao.RegistrarVenta(v);

        int idVenta = vdao.IdVenta();

        JOptionPane.showMessageDialog(null,
                "Venta registrada (" + tipoPago + ") correctamente",
                "Venta OK",
                JOptionPane.INFORMATION_MESSAGE
        );

    }

    private void RegistrarVentaEfectivo() {

        // 1) Registrar venta normalmente
        String cliente = txtNombre.getText();
        String vendedor = jLabelVendedor.getText();
        String fPago = "EFECTIVO";
        BigDecimal total = totalpagar.setScale(2, RoundingMode.HALF_UP);

        V.setCliente(cliente);
        V.setVendedor(vendedor);
        V.setTotal(total);
        V.setFecha(fechaActual);
        V.setPago(fPago);

        Vdao.RegistrarVenta(V);

        // obtener ID de la venta recién generada
        int idVenta = Vdao.IdVenta();

        // 2) Registrar movimiento en caja
        CajaAperturaDao aperturaDao = new CajaAperturaDao();
        Integer idApertura = aperturaDao.obtenerIdAperturaActiva();

        if (idApertura == null) {
            JOptionPane.showMessageDialog(null,
                    "⚠ No hay una caja abierta. Esta venta NO se registrará en la caja.",
                    "Caja no abierta",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Crear movimiento
        CajaMovimiento mov = new CajaMovimiento();
        mov.setIdApertura(idApertura);
        mov.setFecha(java.time.LocalDate.now());
        mov.setHora(java.time.LocalTime.now());
        mov.setTipo("ENTRADA");
        mov.setMonto(total);   // total ya tiene el descuento aplicado
        mov.setDescripcion("Venta en efectivo - ID Venta " + idVenta);
        mov.setUsuario(vendedor);

        CajaMovimientoDao movDao = new CajaMovimientoDao();
        movDao.registrarMovimiento(mov);
    }

    private void RegistrarCaja() {
        int id = cajadao.idCaja();

        // montoActual viene como BigDecimal desde la BD
        BigDecimal montoActual = cajadao.obtenerMontoActual(id);

        // totalpagar YA es BigDecimal
        BigDecimal entradas = totalpagar // 90%
                .setScale(2, RoundingMode.HALF_UP);    // redondeo seguro
        //.multiply(new BigDecimal("0.90"))      // 90%
        BigDecimal salida = BigDecimal.ZERO;
        //registrar nombre del vendedor
        String usuario = jLabelVendedor.getText();

        // nuevo monto en caja
        montoActual = montoActual.add(entradas);

        caja.setEntrada(entradas);
        caja.setSalida(salida);
        caja.setFecha(fechaActual);
        caja.setMonto(montoActual);
        caja.setUsuario(usuario);

        cajadao.RegistrarMovimiento(caja);
    }

    private void RegistrarDetalle() {
        int id = Vdao.IdVenta();

        for (int i = 0; i < tablaNuevaVenta.getRowCount(); i++) {

            String cod = tablaNuevaVenta.getValueAt(i, 0).toString();

            BigDecimal cant = new BigDecimal(
                    tablaNuevaVenta.getValueAt(i, 2).toString().replace(",", ".")
            );

            BigDecimal precio = new BigDecimal(
                    tablaNuevaVenta.getValueAt(i, 3).toString().replace(",", ".")
            );

            Dt.setCod_pro(cod);
            Dt.setCant(cant);
            Dt.setPrecio(precio);
            Dt.setIdVenta(id);

            Vdao.RegistrarDetalle(Dt);
        }
    }

    private void RegistrarDetalleEfectivo() {
        int id = Vdao.IdVenta();

        for (int i = 0; i < tablaNuevaVenta.getRowCount(); i++) {

            String cod = tablaNuevaVenta.getValueAt(i, 0).toString();

            BigDecimal cant = new BigDecimal(
                    tablaNuevaVenta.getValueAt(i, 2).toString().replace(",", ".")
            );

            BigDecimal precio = new BigDecimal(tablaNuevaVenta.getValueAt(i, 3).toString().replace(",", "."));

            // ✔ aplica 10% de descuento (multiplica por 0.90)
            BigDecimal descuento = new BigDecimal("0.90");
            BigDecimal precioConDescuento = precio.multiply(descuento).setScale(2, RoundingMode.HALF_UP);

            Dt.setCod_pro(cod);
            Dt.setCant(cant);
            Dt.setPrecio(precioConDescuento);
            Dt.setIdVenta(id);

            Vdao.RegistrarDetalle(Dt);
        }
    }

    private void RegistrarDetalleTransferencia() {
        int id = Vdao.IdVenta();
        for (int i = 0; i < tablaNuevaVenta.getRowCount(); i++) {

            String cod = tablaNuevaVenta.getValueAt(i, 0).toString();

            BigDecimal cant = new BigDecimal(
                    tablaNuevaVenta.getValueAt(i, 2).toString().replace(",", "."));

            BigDecimal precio = new BigDecimal(
                    tablaNuevaVenta.getValueAt(i, 3).toString().replace(",", "."));

            BigDecimal descuento = new BigDecimal("0.90");
            BigDecimal precioConDescuento = precio.multiply(descuento).setScale(2, RoundingMode.HALF_UP);

            //precio = (precio/100)*95;
            Dt.setCod_pro(cod);
            Dt.setCant(cant);
            Dt.setPrecio(precioConDescuento);
            Dt.setIdVenta(id);
            Vdao.RegistrarDetalle(Dt);
        }

    }

    private void ActualizarStock() {

        for (int i = 0; i < tablaNuevaVenta.getRowCount(); i++) {

            String cod = tablaNuevaVenta.getValueAt(i, 0).toString();

            BigDecimal cant = new BigDecimal(tablaNuevaVenta.getValueAt(i, 2).toString());

            Productos prod = prodao.BuscarProducto(cod);

            BigDecimal stockActual = prod.getStock().subtract(cant);

            if (prodao.ActualizarStock(stockActual, cod)) {
                // Si el producto está configurado para la web y está vinculado, actualizamos stock en WooCommerce
                if (prod.getWeb() != null && prod.getWeb() && prod.getIdWooCommerce() > 0) {
                    SincronizadorWooCommerce sincro = new SincronizadorWooCommerce();
                    sincro.subirStockAWeb(prod.getIdWooCommerce(), stockActual.intValue());
                }
            }
        }
    }

    private void LimpiarTablaVenta() {
        Tmp = (DefaultTableModel) tablaNuevaVenta.getModel();
        int fila = tablaNuevaVenta.getRowCount();

        for (int i = 0; i < fila; i++) {

            Tmp.removeRow(0);

        }

    }

    private void LimpiarDatosCliente() {
        txtDni.setText("");
        txtDomicilioClienteNV.setText("");
        txtNombre.setText("");
        txtEmail.setText("");
        txtTelefonoCV.setText("");
        txtDireccionCV.setText("");
        txtRazonCV.setText("");
    }

    private void pdf() {
        try {
            int id = Vdao.IdVenta();

            // ===== CREAR DIRECTORIO SI NO EXISTE =====
            String userHome = System.getProperty("user.home");
            String rutaCarpeta = userHome + "/Documents/Canarias/Comprobantes";
            File carpetaPDF = new File(rutaCarpeta);

            if (!carpetaPDF.exists()) {
                carpetaPDF.mkdirs();  // Crea la carpeta completa
            }

            // ===== ARCHIVO PDF =====
            File file = new File(rutaCarpeta + "/venta" + id + ".pdf");
            FileOutputStream archivo = new FileOutputStream(file);

            Document doc = new Document();
            PdfWriter.getInstance(doc, archivo);
            doc.open();

            // ===== IMAGEN DEL LOGO =====
            // Ruta absoluta donde está instalado el programa
            String basePath = System.getProperty("user.dir");
            String rutaImagen = basePath + File.separator + "img" + File.separator + "Logo ByG gemini 200x200.png";

            Image img = Image.getInstance(rutaImagen);
            img.scaleToFit(100, 100);

            // ===== ENCABEZADO =====
            Paragraph fecha = new Paragraph();
            Font negrita = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLUE);
            fecha.add(Chunk.NEWLINE);
            fecha.add("Factura: " + id + "\n"
                    + "Fecha: " + new SimpleDateFormat("dd-MM-yyyy HH-mm").format(new Date()));

            PdfPTable Encabezado = new PdfPTable(4);
            Encabezado.setWidthPercentage(100);
            Encabezado.getDefaultCell().setBorder(0);
            Encabezado.setWidths(new float[]{20f, 30f, 70f, 40f});
            Encabezado.setHorizontalAlignment(Element.ALIGN_LEFT);

            // Imagen
            PdfPCell imgCell = new PdfPCell(img);
            imgCell.setBorder(0);
            imgCell.setRowspan(3);
            Encabezado.addCell(imgCell);

            // Datos empresa
            Encabezado.addCell("");
            Encabezado.addCell("Cuit: " + txtCuitConfig.getText()
                    + "\nNombre: " + txtNombreCongig.getText()
                    + "\nTelefono: " + txtTelefonoConfig.getText()
                    + "\nDomicilio: " + txtDomicilioConfig.getText());
            Encabezado.addCell(fecha);

            doc.add(Encabezado);

            // ===== DATOS DEL CLIENTE =====
            Paragraph Cli = new Paragraph();
            Cli.add(Chunk.NEWLINE);
            Cli.add("Datos del Cliente\n");
            doc.add(Cli);

            PdfPTable tablaCli = new PdfPTable(4);
            tablaCli.setWidthPercentage(100);
            tablaCli.getDefaultCell().setBorder(0);
            tablaCli.setWidths(new float[]{20f, 50f, 30f, 40f});
            tablaCli.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell cl1 = new PdfPCell(new Phrase("DNI/CUIT", negrita));
            PdfPCell cl2 = new PdfPCell(new Phrase("Nombre", negrita));
            PdfPCell cl3 = new PdfPCell(new Phrase("Telefono", negrita));
            PdfPCell cl4 = new PdfPCell(new Phrase("Direccion", negrita));

            cl1.setBorder(0);
            cl2.setBorder(0);
            cl3.setBorder(0);
            cl4.setBorder(0);

            tablaCli.addCell(cl1);
            tablaCli.addCell(cl2);
            tablaCli.addCell(cl3);
            tablaCli.addCell(cl4);

            tablaCli.addCell(txtDni.getText());
            tablaCli.addCell(txtNombre.getText());
            tablaCli.addCell(txtTelefonoCV.getText());
            tablaCli.addCell(txtDireccionCV.getText());

            doc.add(tablaCli);

            // ===== ESPACIO =====
            Paragraph espacio = new Paragraph();
            espacio.add(Chunk.NEWLINE);
            doc.add(espacio);

            // ===== DESCUENTO SEGÚN PAGO =====
            String etiquetaDescuento = conf.getCredito() + " %";
            if (radioEfectivo.isSelected()) {
                etiquetaDescuento = conf.getEfectivo() + " %";
            } else if (radioDebito.isSelected()) {
                etiquetaDescuento = conf.getTransferencia() + " %";
            }

            // ===== TABLA PRODUCTOS =====
            PdfPTable tablaprod = new PdfPTable(6);
            tablaprod.setWidthPercentage(100);
            tablaprod.getDefaultCell().setBorder(0);
            tablaprod.setWidths(new float[]{10f, 10f, 38f, 15f, 12f, 15f});

            String[] headers = {"Cantidad", "Código", "Descripcion", "Precio U", "Desc.", "Total"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, negrita));
                cell.setBorder(0);
                cell.setBackgroundColor(BaseColor.GRAY);
                tablaprod.addCell(cell);
            }

            for (int i = 0; i < tablaNuevaVenta.getRowCount(); i++) {
                tablaprod.addCell(tablaNuevaVenta.getValueAt(i, 2).toString());
                tablaprod.addCell(tablaNuevaVenta.getValueAt(i, 0).toString());
                tablaprod.addCell(tablaNuevaVenta.getValueAt(i, 1).toString());
                tablaprod.addCell(tablaNuevaVenta.getValueAt(i, 3).toString());
                tablaprod.addCell(etiquetaDescuento);
                tablaprod.addCell(tablaNuevaVenta.getValueAt(i, 4).toString());
            }

            doc.add(tablaprod);

            // ===== INFORMACIÓN FINAL =====
            Paragraph info = new Paragraph();
            info.add(Chunk.NEWLINE);
            info.add("Total a Pagar: " + labelTotalAPagar.getText());
            info.add("\n\nF. de Pago: " + lblSeleccionFdePago.getText());
            info.setAlignment(Element.ALIGN_RIGHT);
            doc.add(info);

            Paragraph mensaje = new Paragraph();
            mensaje.add(Chunk.NEWLINE);
            mensaje.add("ByG Refrigeración Agradece su preferencia!");
            mensaje.setAlignment(Element.ALIGN_CENTER);
            doc.add(mensaje);

            doc.close();
            archivo.close();

            // ===== CONFIRMAR APERTURA =====
            int respuesta = JOptionPane.showConfirmDialog(
                    null,
                    "Archivo PDF Generado en:\n" + file.getAbsolutePath()
                    + "\n\n¿Desea abrirlo ahora?",
                    "PDF guardado",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (respuesta == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(file);
            }

        } catch (DocumentException | IOException e) {
            System.out.println(e.toString());
        }
    }

    public void FiltrarFecha(String fecha1, String fecha2) {
        LimpiarTabla();
        Connection con;
        Conexion.ConexionMysql cn = new ConexionMysql();
        PreparedStatement ps;
        ResultSet rs;

        List<Venta> ListaVentas = new ArrayList<>();

        String sql = "SELECT * FROM ventas WHERE fecha BETWEEN ? AND ?";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(fecha1));
            ps.setDate(2, java.sql.Date.valueOf(fecha2));
            rs = ps.executeQuery();

            while (rs.next()) {
                Venta ven = new Venta();
                ven.setId(rs.getInt("id"));
                ven.setCliente(rs.getString("cliente"));
                ven.setVendedor(rs.getString("vendedor"));
                ven.setTotal(rs.getBigDecimal("total"));
                ven.setFecha(rs.getString("fecha"));
                ven.setPago(rs.getString("pago"));
                ven.setEstado(rs.getString("estado"));
                ListaVentas.add(ven);
            }

            modelo = (DefaultTableModel) tablaVentas.getModel();
            Object[] obj = new Object[6];

            for (Venta v : ListaVentas) {
                obj[0] = v.getId();
                obj[1] = v.getCliente();
                obj[2] = v.getVendedor();
                obj[3] = v.getTotal();
                obj[4] = v.getFecha();
                obj[5] = v.getPago();

                modelo.addRow(obj);
            }

            tablaVentas.setModel(modelo);

        } catch (SQLException e) {
            System.out.println("Error al cargar la tabla " + e.toString());
        }
    }

    public void FiltrarEntreYFechasNombre(String fecha1, String fecha2, String nombre) {
        Connection con;
        Conexion.ConexionMysql cn = new ConexionMysql();
        PreparedStatement ps;
        ResultSet rs;

        List<Venta> ListaVentas = new ArrayList<>();

        String sql = "SELECT * FROM ventas WHERE vendedor LIKE ? AND fecha BETWEEN ? AND ?";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, "%" + nombre + "%");
            ps.setDate(2, java.sql.Date.valueOf(fecha1));
            ps.setDate(3, java.sql.Date.valueOf(fecha2));
            rs = ps.executeQuery();

            while (rs.next()) {
                Venta ven = new Venta();
                ven.setId(rs.getInt("id"));
                ven.setCliente(rs.getString("cliente"));
                ven.setVendedor(rs.getString("vendedor"));
                ven.setTotal(rs.getBigDecimal("total"));
                ven.setFecha(rs.getString("fecha"));
                ven.setPago(rs.getString("pago"));
                ven.setEstado(rs.getString("estado"));
                ListaVentas.add(ven);
            }

            modelo = (DefaultTableModel) tablaVentas.getModel();
            Object[] obj = new Object[6];

            for (Venta v : ListaVentas) {
                obj[0] = v.getId();
                obj[1] = v.getCliente();
                obj[2] = v.getVendedor();
                obj[3] = v.getTotal();
                obj[4] = v.getFecha();
                obj[5] = v.getPago();
                modelo.addRow(obj);
            }

            tablaVentas.setModel(modelo);

        } catch (SQLException e) {
            System.out.println("Error al cargar la tabla " + e.toString());
        }
    }
    
    
    //*** METODO PARA CREAR EL PRODUCTO ***//
    
    public void guardarProducto() {
    String codigo = txtCodigoProducto.getText().trim();

    // 1. Validar duplicado usando el DAO
    if (prodao.existeCodigo(codigo)) {
        JOptionPane.showMessageDialog(this, "El código ya existe. Use el botón de actualizar.", "Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // 2. Mapear datos de la vista al objeto
    Productos nuevoPro = new Productos();
    nuevoPro.setCodigo(codigo);
    nuevoPro.setDescripcion(txtDescripcionProd.getText());
    nuevoPro.setProveedor(bxProveedor.getSelectedItem().toString());
    
    try {
        nuevoPro.setStock(new BigDecimal(txtCantidadProducto.getText()));
        nuevoPro.setPrecio(new BigDecimal(txtPrecioProd.getText().replace(",", ".")));
        nuevoPro.setPrecioCompra(new BigDecimal(txtPrecioCompra.getText().replace(",", ".")));
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Verifique los valores numéricos", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    Item cat = (Item) cbxCategoria.getSelectedItem();
    Material mat = (Material) comboMateriales.getSelectedItem();
    Piedra pie = (Piedra) comboPiedras.getSelectedItem();
    
    if (cat == null || mat == null) {
        JOptionPane.showMessageDialog(this, "Seleccione Categoría y Material");
        return;
    }
    
    nuevoPro.setIdCategoria(cat.getId());
    nuevoPro.setMaterial(mat.name());
    nuevoPro.setPiedra(pie.name());
    // Capturamos el estado del checkbox (devuelve true si está marcado, false si no)
    nuevoPro.setWeb(chkWeb.isSelected());
    
    // 3. Ejecutar el registro
    if (prodao.RegistrarProductos(nuevoPro)) {
        // --- Registrar Movimiento inicial ---
        int id = prodao.obtenerIdPorCodigo(codigo);
        if (id != -1) {
            Modelo.MovimientoStock mov = new Modelo.MovimientoStock();
            mov.setIdProducto(id);
            mov.setTipoMovimiento("ENTRADA");
            mov.setCantidad(nuevoPro.getStock().doubleValue());
            mov.setMotivo("CARGA INICIAL");
            new Modelo.MovimientoDao().registrarMovimiento(mov);
        }
        
      
        JOptionPane.showMessageDialog(this, "Producto Registrado con Éxito");
        
        // Si el usuario marcó que va a la web, intentamos vincularlo al instante
        if (chkWeb.isSelected()) {
            Productos proFresco = prodao.BuscarProducto(codigo);
            if (proFresco != null && proFresco.getId() > 0) {
                Sinc.vincularProductoIndividualConWeb(proFresco);
            }
        }
        

        LimpiarTabla();
        LimpiarProductos();
        ListarProductos();
    } else {
        JOptionPane.showMessageDialog(this, "Error al registrar en la base de datos");
    }
}
    


    public Productos BuscarProductoB(String codigo) {

        Productos producto = new Productos();
        String sql = "SELECT * FROM productos WHERE codigo=?";
        Connection con;
        ConexionMysql cn = new ConexionMysql();
        PreparedStatement ps;
        ResultSet rs;

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, codigo);
            rs = ps.executeQuery();

            if (rs.next()) {
                producto.setId(rs.getInt("id"));
                producto.setCodigo(rs.getString("codigo"));
                producto.setDescripcion(rs.getString("descripcion"));
                producto.setPrecio(rs.getBigDecimal("precio"));
                producto.setStock(rs.getBigDecimal("stock"));

                txtIdProd.setText(Integer.toString(producto.getId()));
                //txtCantidadProducto.setText(Integer.toString(producto.getStock()));
                txtCantidadProducto.setText(producto.getStock().toPlainString());

                txtDescripcionProd.setText(producto.getDescripcion());
                txtCodigoProducto.setText(producto.getCodigo());
                //txtPrecioProd.setText(Double.toString(producto.getPrecio()));
                txtPrecioProd.setText(producto.getPrecio().setScale(2).toPlainString());

                //Seleccionar la fila en la JTable
                seleccionarFilaEnTabla(tablaProductos, rs.getInt("id"));

            } else {
                JOptionPane.showMessageDialog(null, "Cóigo no encontrado");
            }
        } catch (SQLException e) {

            System.out.println(e.toString());

        }

        return producto;
    }

    private void seleccionarFilaEnTabla(JTable tabla, int id) {
        for (int i = 0; i < tabla.getRowCount(); i++) {
            if (Integer.parseInt(tabla.getValueAt(i, 0).toString()) == id) { // Suponiendo que la columna "id" está en la posición 0
                tabla.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    private void aplicarDescuento() {

        // Obtener porcentaje de descuento en BigDecimal (conf debe dar BigDecimal o String)
        BigDecimal descuento;

        try {
            descuento = new BigDecimal(String.valueOf(conf.getEfectivo()));
        } catch (Exception e) {
            descuento = BigDecimal.ZERO;
        }

        if (radioEfectivo.isSelected()) {
            descuento = new BigDecimal(String.valueOf(conf.getEfectivo()));

        } else if (radioDebito.isSelected()) {
            descuento = new BigDecimal(String.valueOf(conf.getTransferencia()));

        } else if (radioCredito.isSelected()) {
            descuento = new BigDecimal(String.valueOf(conf.getCredito()));

        } else {
            return;
        }

        // convertir a porcentaje: 15 → 0.15
        descuento = descuento.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        DefaultTableModel model = (DefaultTableModel) tablaNuevaVenta.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {

            // cantidad
            BigDecimal cantidad = new BigDecimal(model.getValueAt(i, 2).toString().replace(",", "."));

            // precio unitario
            BigDecimal precio = new BigDecimal(model.getValueAt(i, 3).toString().replace(",", "."));

            // subtotal
            BigDecimal subtotal = precio.multiply(cantidad);

            // aplicar descuento
            BigDecimal totalConDescuento
                    = subtotal.multiply(BigDecimal.ONE.subtract(descuento))
                            .setScale(2, RoundingMode.HALF_UP);

            model.setValueAt(totalConDescuento.toPlainString(), i, 4);
        }

        TotalPagar();
    }

    private void filtrarTabla(String texto) {

        // Limpiar tabla antes de cargar
        DefaultTableModel modelo = (DefaultTableModel) tablaProductos.getModel();
        modelo.setRowCount(0);

        List<Productos> todos = prodao.ListarProductos();

        for (Productos p : todos) {
            if (texto.isEmpty()
                    || p.getCodigo().toLowerCase().contains(texto.toLowerCase())
                    || p.getDescripcion().toLowerCase().contains(texto.toLowerCase())
                    || p.getProveedor().toLowerCase().contains(texto.toLowerCase())
                    || (p.getCategoriaNombre() != null && p.getCategoriaNombre().toLowerCase().contains(texto.toLowerCase()))) {

                modelo.addRow(new Object[]{
                    p.getId(),
                    p.getCodigo(),
                    p.getDescripcion(),
                    p.getStock(),
                    p.getCategoriaNombre(),
                    p.getPrecioCompra(),
                    p.getPrecio(),
                    p.getProveedor(),
                    p.getMaterial(),
                    p.getPiedra(),
                    p.getPrecioxdolar(),
                    p.getWeb()
                        
                    
                });
            }
        }
    }

    public void cargarCategorias() {
        cbxCategoria.removeAllItems();//borramos todo
        // Agregamos el placeholder
        cbxCategoria.addItem(new Item(0, "Categoría")); 
        
        for (Categoria c : categoriaDao.listar()) {
            cbxCategoria.addItem(new Item(c.getId(), c.getNombre()));
        }
    }
    

    
    private void cargarComboMateriales() {
    // Limpiamos ítems previos por si acaso
    comboMateriales.removeAllItems();
    
    // Recorremos los valores del ENUM Material
    for (Material m : Material.values()) {
        comboMateriales.addItem(m);
    }
}

    private void seleccionarItemPorId(JComboBox<Item> combo, int idBuscado) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Item item = combo.getItemAt(i);
            if (item.getId() == idBuscado) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    public void bloquearPestañas() {
        int totalPestanas = jTabbedPane1.getTabCount();
        for (int i = 0; i < totalPestanas; i++) {
            jTabbedPane1.setEnabledAt(i, false);
        }
    }


    private void abrirNuevaVenta() {
    // 1. Creamos la instancia de tu panel personalizado
    PanelVenta pv = new PanelVenta(usuarioLog);

    // 2. Añadimos la pestaña con el título dinámico
    String titulo = "Venta " + contadorVentas;
    jTabbedPane1.addTab(titulo, pv);

    // 3. Obtenemos el índice de la pestaña que acabamos de añadir
    // Siempre será el último, así que usamos getTabCount() - 1
    int index = jTabbedPane1.getTabCount() - 1;

    // 4. Aplicamos el componente con el botón "X" a ese índice
    jTabbedPane1.setTabComponentAt(index, new ButtonTabComponent(jTabbedPane1));

    // 5. Seleccionamos la nueva pestaña para que el usuario la vea de inmediato
    jTabbedPane1.setSelectedIndex(index);

    contadorVentas++;
}

    private void abrirEditarProductos() {
        EditarProductosView vista = new EditarProductosView();
        vista.setVisible(true);
    }
    
    private void realizarBackupAutomatico() {
    try {
        // Creamos una carpeta 'backups' si no existe
        File folder = new File("backups");
        if (!folder.exists()) folder.mkdir();
        
        Servicios.BackupService bs = new Servicios.BackupService();
        // Lo guarda en la carpeta local sin preguntar al usuario
        bs.ejecutarRespaldo(folder.getAbsolutePath());
        
        System.out.println("Backup automático realizado.");
    } catch (Exception e) {
        System.err.println("Error en backup automático: " + e.getMessage());
    }
}
    
    
    private void confirmarSalidaSegura() {
        int pregunta = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cerrar el sistema?\nSe realizará un backup automático.",
                "Confirmar Salida", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (pregunta == JOptionPane.YES_OPTION) {
            // 1. Crear carpeta si no existe
            File folder = new File("backups_automaticos");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // 2. Ejecutar backup (Síncrono)
            BackupService bs = new BackupService();
            bs.ejecutarRespaldo(folder.getAbsolutePath());

            // 3. Cerrar el programa definitivamente
            System.exit(0);
        }
    }
    
    public void salirDelSistema() {
        System.out.println("Iniciando proceso de salida controlado...");

        // 1. Ejecutar el backup
        try {
            File folder = new File("backups_automaticos");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            Servicios.BackupService bs = new Servicios.BackupService();
            // Usamos el método que devuelve boolean para confirmar en consola
            boolean ok = bs.ejecutarRespaldo(folder.getAbsolutePath());
            if (ok) {
                System.out.println("Backup de seguridad completado.");
            }
        } catch (Exception e) {
            System.err.println("Error en backup de salida: " + e.getMessage());
        }

        // 2. Cerrar la conexión a H2 explícitamente
        // Esto es vital para que el archivo .mv.db se cierre bien
        try {
            // cn es tu objeto de Conexion. Asegúrate que cerrar() o close() funcione.
            if (cn.conectar() != null && !cn.conectar().isClosed()) {
                cn.conectar().close();
                System.out.println("Conexión a H2 cerrada.");
            }
        } catch (Exception e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }

        // 3. Fin del proceso
        System.exit(0);
    }
    

    
    public java.math.BigDecimal obtenerPrecioDolarFromTextField() {
        String texto = txtPrecioDolar.getText().trim();

        // Si está vacío o es solo un punto/coma, devolvemos un valor seguro por defecto (ej: 1) para que no rompa
        if (texto.isEmpty() || texto.equals(".") || texto.equals(",")) {
            return java.math.BigDecimal.ONE.setScale(2, java.math.RoundingMode.HALF_UP);
        }

        try {
            // Normalizamos: Reemplazamos la coma por punto para que el constructor de BigDecimal no falle
            texto = texto.replace(",", ".");

            // Creamos el BigDecimal de forma segura
            java.math.BigDecimal valor = new java.math.BigDecimal(texto);

            // Lo limitamos a 2 decimales con redondeo matemático estándar
            return valor.setScale(2, java.math.RoundingMode.HALF_UP);

        } catch (NumberFormatException e) {
            System.err.println("❌ Formato numérico inválido detectado: " + e.getMessage());
            // En caso de un desastre impredecible, devolvemos 1 para no romper cálculos con NULL
            return java.math.BigDecimal.ONE.setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

    private void iniciarMonitorPedidosWeb() {
        java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Buscamos pedidos procesando (nuevos pedidos pagados)
                List<WooOrder> nuevosPedidos = Sinc.obtenerPedidosProcesando();
                if (nuevosPedidos != null && !nuevosPedidos.isEmpty()) {
                    for (WooOrder pedido : nuevosPedidos) {
                        if (!pedidosNotificados.contains(pedido.id)) {
                            pedidosNotificados.add(pedido.id);
                            
                            // Mostrar notificación visual Toast en un hilo de Swing
                            SwingUtilities.invokeLater(() -> {
                                String cliente = pedido.billing != null ? pedido.billing.first_name + " " + pedido.billing.last_name : "Cliente Web";
                                ToastNotification toast = new ToastNotification(
                                    "🛍️ ¡NUEVO PEDIDO WEB!",
                                    "Pedido #" + pedido.id + " de " + cliente + " por $" + pedido.total
                                );
                                toast.mostrar();
                                
                                // Reproducir pitido de alerta
                                java.awt.Toolkit.getDefaultToolkit().beep();
                            });
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error en el monitor de pedidos WooCommerce: " + e.getMessage());
            }
        }, 15, 90, java.util.concurrent.TimeUnit.SECONDS); // Corre por primera vez a los 15s y luego cada 90s (1.5 minutos)
    }
    
}
