package Vista; // Reemplaza 'Vista' por el nombre real de tu paquete

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class VistaClientesProfesional extends javax.swing.JFrame {

    // --- PALETA DE COLORES (Basada en tu logo BONHOMÍA) ---
    private final Color COLOR_COBRIZO = new Color(184, 139, 74); // Marrón Cobrizo #B88B4A
    private final Color COLOR_TEXTO = new Color(73, 59, 42);   // Marrón Oscuro para textos
    private final Color COLOR_BORDE = new Color(210, 190, 160); // Borde suave cobrizo
    private final Font FONT_FORM = new Font("Segoe UI", Font.PLAIN, 14);

    public VistaClientesProfesional() {
        initComponents();
        personalizarVista();
        setTitle("Gestión de Clientes Profesionales - BONHOMÍA");
        setSize(1150, 700); // Tamaño sugerido profesional
        setLocationRelativeTo(null);
    }

    private void personalizarVista() {
        // Estilo Global
        this.getContentPane().setBackground(Color.WHITE);
        
        // Estilos para Paneles con Borde Cobrizo
        TitledBorder borderForm = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true), "Datos del Cliente", TitledBorder.LEFT, TitledBorder.TOP, FONT_FORM, COLOR_COBRIZO);
        panelFormulario.setBorder(borderForm);
        panelFormulario.setBackground(Color.WHITE);

        TitledBorder borderTabla = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1, true), "Listado de Clientes", TitledBorder.LEFT, TitledBorder.TOP, FONT_FORM, COLOR_COBRIZO);
        panelTablaContainer.setBorder(borderTabla);
        panelTablaContainer.setBackground(Color.WHITE);
        
        panelHeader.setBackground(Color.WHITE);
        panelBotonesAccion.setBackground(Color.WHITE);

        // Estilos para la Tabla Profesional
        tableClientes.setFont(FONT_FORM);
        tableClientes.setRowHeight(30);
        tableClientes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableClientes.getTableHeader().setBackground(COLOR_COBRIZO);
        tableClientes.getTableHeader().setForeground(Color.WHITE);
        tableClientes.setSelectionBackground(new Color(230, 215, 190)); // Marrón muy claro al seleccionar

        // Cargar Combos de AFIP (Códigos Numéricos)
        // (Nota: Estos datos los vincularás luego con sus IDs reales de AFIP)
        cbTipoDoc.setModel(new DefaultComboBoxModel<>(new String[]{"DNI", "CUIT", "CUIL"})); // IDs: 96, 80, 86
        cbCondicionIva.setModel(new DefaultComboBoxModel<>(new String[]{"Consumidor Final", "Resp. Inscripto", "Monotributo"})); // IDs: 5, 1, 6
        cbProvincia.setModel(new DefaultComboBoxModel<>(new String[]{"Santa Fe", "Buenos Aires", "Córdoba"})); // IDs: 21, etc.
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        panelHeader = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        txtBusquedaRapida = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        btnNuevo = new javax.swing.JButton();
        panelFormulario = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        cbTipoDoc = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        txtNroDoc = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        cbCondicionIva = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtDomicilio = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtLocalidad = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        cbProvincia = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        txtTelefono = new javax.swing.JTextField();
        panelTablaContainer = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableClientes = new javax.swing.JTable();
        panelBotonesAccion = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
       jLabel11.setForeground(new java.awt.Color(73, 59, 42)); // Color Marrón Oscuro 
        jLabel11.setText("Búsqueda Rápida (DNI/CUIT):");

        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Iconos/buscar.png"))); // NOI18N
        btnBuscar.setText("Buscar");
        btnBuscar.setFocusable(false);

        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Iconos/nuevo.png"))); // NOI18N
        btnNuevo.setText("Nuevo Cliente");
        btnNuevo.setFocusable(false);

        javax.swing.GroupLayout panelHeaderLayout = new javax.swing.GroupLayout(panelHeader);
        panelHeader.setLayout(panelHeaderLayout);
        panelHeaderLayout.setHorizontalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addGap(18, 18, 18)
                .addComponent(txtBusquedaRapida, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelHeaderLayout.setVerticalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtBusquedaRapida, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelFormulario.setLayout(new java.awt.GridLayout(11, 2, 10, 8));

        jLabel1.setFont(FONT_FORM);
        jLabel1.setText("ID (Interno):");
        panelFormulario.add(jLabel1);

        txtId.setEditable(false);
        panelFormulario.add(txtId);

        jLabel2.setFont(FONT_FORM);
        jLabel2.setText("Nombre Completo / Razón Social:");
        panelFormulario.add(jLabel2);
        panelFormulario.add(txtNombre);

        jLabel3.setFont(FONT_FORM);
        jLabel3.setText("Tipo Documento (AFIP):");
        panelFormulario.add(jLabel3);

        cbTipoDoc.setFocusable(false);
        panelFormulario.add(cbTipoDoc);

        jLabel4.setFont(FONT_FORM);
        jLabel4.setText("Nro. Documento:");
        panelFormulario.add(jLabel4);
        panelFormulario.add(txtNroDoc);

        jLabel5.setFont(FONT_FORM);
        jLabel5.setText("Condición IVA:");
        panelFormulario.add(jLabel5);

        cbCondicionIva.setFocusable(false);
        panelFormulario.add(cbCondicionIva);

        jLabel6.setFont(FONT_FORM);
        jLabel6.setText("Email (WooCommerce):");
        panelFormulario.add(jLabel6);
        panelFormulario.add(txtEmail);

        jLabel7.setFont(FONT_FORM);
        jLabel7.setText("Domicilio:");
        panelFormulario.add(jLabel7);
        panelFormulario.add(txtDomicilio);

        jLabel8.setFont(FONT_FORM);
        jLabel8.setText("Localidad:");
        panelFormulario.add(jLabel8);
        panelFormulario.add(txtLocalidad);

        jLabel9.setFont(FONT_FORM);
        jLabel9.setText("Provincia:");
        panelFormulario.add(jLabel9);

        cbProvincia.setFocusable(false);
        panelFormulario.add(cbProvincia);

        jLabel10.setFont(FONT_FORM);
        jLabel10.setText("Teléfono:");
        panelFormulario.add(jLabel10);
        panelFormulario.add(txtTelefono);

        tableClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nombre", "DNI/CUIT", "IVA", "Email", "Localidad"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tableClientes);
        if (tableClientes.getColumnModel().getColumnCount() > 0) {
            tableClientes.getColumnModel().getColumn(0).setMinWidth(40);
            tableClientes.getColumnModel().getColumn(0).setPreferredWidth(40);
            tableClientes.getColumnModel().getColumn(0).setMaxWidth(60);
        }

        javax.swing.GroupLayout panelTablaContainerLayout = new javax.swing.GroupLayout(panelTablaContainer);
        panelTablaContainer.setLayout(panelTablaContainerLayout);
        panelTablaContainerLayout.setHorizontalGroup(
            panelTablaContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        panelTablaContainerLayout.setVerticalGroup(
            panelTablaContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTablaContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelBotonesAccion.setLayout(new java.awt.GridLayout(1, 4, 15, 0));

        btnGuardar.setBackground(COLOR_COBRIZO);
        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Iconos/guardar.png"))); // NOI18N
        btnGuardar.setText("Guardar");
        btnGuardar.setFocusable(false);
        panelBotonesAccion.add(btnGuardar);

        btnEditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Iconos/editar.png"))); // NOI18N
        btnEditar.setText("Editar Selección");
        btnEditar.setFocusable(false);
        panelBotonesAccion.add(btnEditar);

        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Iconos/eliminar.png"))); // NOI18N
        btnEliminar.setText("Eliminar");
        btnEliminar.setFocusable(false);
        panelBotonesAccion.add(btnEliminar);

        btnLimpiar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Iconos/limpiar.png"))); // NOI18N
        btnLimpiar.setText("Limpiar Formulario");
        btnLimpiar.setFocusable(false);
        panelBotonesAccion.add(btnLimpiar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelFormulario, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelTablaContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panelBotonesAccion, javax.swing.GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelTablaContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelBotonesAccion, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelFormulario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>                        

    // Variables declaration - do not modify (Públicas para que el controlador acceda)
    public javax.swing.JButton btnBuscar;
    public javax.swing.JButton btnEditar;
    public javax.swing.JButton btnEliminar;
    public javax.swing.JButton btnGuardar;
    public javax.swing.JButton btnLimpiar;
    public javax.swing.JButton btnNuevo;
    public javax.swing.JComboBox<String> cbCondicionIva;
    public javax.swing.JComboBox<String> cbProvincia;
    public javax.swing.JComboBox<String> cbTipoDoc;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panelBotonesAccion;
    private javax.swing.JPanel panelFormulario;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelTablaContainer;
    public javax.swing.JTable tableClientes;
    public javax.swing.JTextField txtBusquedaRapida;
    public javax.swing.JTextField txtDomicilio;
    public javax.swing.JTextField txtEmail;
    public javax.swing.JTextField txtId;
    public javax.swing.JTextField txtLocalidad;
    public javax.swing.JTextField txtNombre;
    public javax.swing.JTextField txtNroDoc;
    public javax.swing.JTextField txtTelefono;
    // End of variables declaration                   
}