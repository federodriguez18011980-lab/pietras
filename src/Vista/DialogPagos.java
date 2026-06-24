package Vista;

import Modelo.*;
import static Modelo.TipoPago.CREDITO;
import static Modelo.TipoPago.EFECTIVO;
import static Modelo.TipoPago.TRANSFERENCIA;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class DialogPagos extends javax.swing.JDialog {

    private BigDecimal totalVenta;
    private BigDecimal totalPagado = BigDecimal.ZERO;
    private BigDecimal montoBruto = BigDecimal.ZERO;
    private BigDecimal saldo;
    private List<Pago> pagos = new ArrayList<>();
    private ConfigDao confDao = new ConfigDao();
    private BigDecimal montoDevolucion = BigDecimal.ZERO; // Nueva variable
    

    // Colores Bonhomia System
   // private final Color COLOR_COBRIZO = new Color(184, 139, 74); // Marrón Cobrizo #B88B4A
    private java.awt.Color azulOscuro = new java.awt.Color(184, 139, 74);//COLOR_COBRIZO
    private java.awt.Color verdeDinero = new java.awt.Color(39, 174, 96);
    private java.awt.Color rojoBoton = new java.awt.Color(231, 76, 60);
    private java.awt.Color grisFondo = new java.awt.Color(245, 246, 250);
    private java.awt.Color colorPietras = new java.awt.Color(244, 119, 21);
    private java.awt.Color colorHueso = new java.awt.Color(242, 240, 235);

    public DialogPagos(java.awt.Frame parent, BigDecimal totalVenta, BigDecimal montoDevolucion) {
        super(parent, true);
        this.totalVenta = totalVenta;
        this.montoDevolucion = montoDevolucion;
        this.montoDevolucion.setScale(2, RoundingMode.HALF_UP);
        this.saldo = totalVenta;
        
        initComponents();
        this.getContentPane().setBackground(java.awt.Color.WHITE);
        this.setLocationRelativeTo(null);
        
        if (montoDevolucion.compareTo(BigDecimal.ZERO) > 0) {
            aplicarPagoDevolucionAutomatica();
        }
        
        cmbTipoPago.setModel(new DefaultComboBoxModel<>(TipoPago.values()));
        cargarTiposPago();
        actualizarTotales();
    }

    private void initComponents() {
        setTitle("Finalizar Venta - Gestión de Cobro");
        setSize(800, 600);
        setLayout(new BorderLayout());

        // --- PANEL SUPERIOR: TARJETA DE TOTAL ---
        JPanel pnlHeader = new JPanel(new GridLayout(1, 1));
        pnlHeader.setBackground(colorPietras);
        pnlHeader.setBorder(new EmptyBorder(20, 25, 20, 25));

        lblTotalVenta = new JLabel("TOTAL A COBRAR: $" + totalVenta);
        lblTotalVenta.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTotalVenta.setForeground(java.awt.Color.WHITE);
        lblTotalVenta.setHorizontalAlignment(SwingConstants.CENTER);
        pnlHeader.add(lblTotalVenta);
        add(pnlHeader, BorderLayout.NORTH);

        // --- PANEL CENTRAL: ENTRADA DE DATOS Y TABLA ---
        JPanel pnlCentral = new JPanel();
        pnlCentral.setLayout(new BoxLayout(pnlCentral, BoxLayout.Y_AXIS));
        pnlCentral.setBackground(java.awt.Color.WHITE);
        pnlCentral.setBorder(new EmptyBorder(20, 25, 10, 25));

        // Subpanel: Inputs
        JPanel pnlInputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlInputs.setOpaque(false);

        cmbTipoPago = new JComboBox<>();
        cmbTipoPago.setPreferredSize(new Dimension(150, 35));
        
        txtMonto = new JTextField();
        txtMonto.setPreferredSize(new Dimension(120, 35));
        txtMonto.setFont(new Font("SansSerif", Font.BOLD, 16));
        txtMonto.setHorizontalAlignment(JTextField.CENTER);

        btnAgregarPago = new JButton("AGREGAR");
        estiloBoton(btnAgregarPago, verdeDinero);
        
        btnEliminar = new JButton("QUITAR");
        estiloBoton(btnEliminar, rojoBoton);
        
        lblCreditoDisponible = new JLabel("Crédito: $" + montoDevolucion);
        lblCreditoDisponible.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblCreditoDisponible.setForeground(new java.awt.Color(52, 152, 219)); // Azul suave
        

        pnlInputs.add(new JLabel("Método:"));
        pnlInputs.add(cmbTipoPago);
        pnlInputs.add(new JLabel("Monto:"));
        pnlInputs.add(txtMonto);
        pnlInputs.add(lblCreditoDisponible);
        pnlInputs.add(btnAgregarPago);
        pnlInputs.add(btnEliminar);

        pnlCentral.add(pnlInputs);
       pnlCentral.add(Box.createVerticalStrut(20)); // Espacio vertical de 20 píxeles

        // Tabla
        tblPagos = new JTable();
        tblPagos.setModel(new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"TIPO", "BRUTO", "DESC.", "A COBRAR"}
        ) { boolean[] canEdit = new boolean[]{false, false, false, false};
            @Override public boolean isCellEditable(int r, int c) { return canEdit[c]; }
        });
        configurarTabla();
        
        JScrollPane scroll = new JScrollPane(tblPagos);
        scroll.setPreferredSize(new Dimension(650, 150));
        pnlCentral.add(scroll);
        add(pnlCentral, BorderLayout.CENTER);

        // --- PANEL SUR: RESUMEN Y FINALIZAR ---
        JPanel pnlSur = new JPanel(new BorderLayout());
        pnlSur.setBackground(grisFondo);
        pnlSur.setBorder(new EmptyBorder(15, 25, 15, 25));

        JPanel pnlResumen = new JPanel(new GridLayout(3, 1, 5, 5));
        pnlResumen.setOpaque(false);
        
        lblTotalPagado = new JLabel("Total Bruto: $0.00");
        lblTotalPagado.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        lblTotalCancelado = new JLabel("Total con Descuento: $0.00");
        lblTotalCancelado.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTotalCancelado.setForeground(verdeDinero);
        
        lblSaldo = new JLabel("SALDO PENDIENTE: $0.00");
        lblSaldo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblSaldo.setForeground(rojoBoton);

        pnlResumen.add(lblTotalPagado);
        pnlResumen.add(lblTotalCancelado);
        pnlResumen.add(lblSaldo);

        btnFinalizar = new JButton("FINALIZAR Y FACTURAR");
        estiloBoton(btnFinalizar, colorPietras);
        btnFinalizar.setPreferredSize(new Dimension(200, 45));
        btnFinalizar.setFont(new Font("SansSerif", Font.BOLD, 14));

        pnlSur.add(pnlResumen, BorderLayout.WEST);
        pnlSur.add(btnFinalizar, BorderLayout.EAST);
        add(pnlSur, BorderLayout.SOUTH);

        // Eventos
        btnAgregarPago.addActionListener(e -> btnAgregarPagoActionPerformed(null));
        btnEliminar.addActionListener(e -> btnEliminarActionPerformed(null));
        btnFinalizar.addActionListener(e -> btnFinalizarActionPerformed(null));
        txtMonto.addActionListener(e -> btnAgregarPagoActionPerformed(null));
    }

    private void estiloBoton(JButton btn, java.awt.Color color) {
        btn.setBackground(color);
        btn.setForeground(java.awt.Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void configurarTabla() {
        tblPagos.setRowHeight(30);
        JTableHeader h = tblPagos.getTableHeader();
        h.setFont(new Font("SansSerif", Font.BOLD, 12));
        h.setBackground(new java.awt.Color(230, 230, 230));
        
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<4; i++) tblPagos.getColumnModel().getColumn(i).setCellRenderer(center);
    }

    // --- LÓGICA MANTENIDA ---

    private void btnAgregarPagoActionPerformed(java.awt.event.ActionEvent evt) {
        if (txtMonto.getText().trim().isEmpty()) return;
        try {
            BigDecimal monto = new BigDecimal(txtMonto.getText());
            TipoPago tipo = (TipoPago) cmbTipoPago.getSelectedItem();
            
            // --- BLINDAJE PARA DEVOLUCIONES ---
            if (tipo == TipoPago.DEVOLUCION) {
                if (monto.compareTo(montoDevolucion) > 0) {
                    JOptionPane.showMessageDialog(this,
                            "¡ERROR DE SEGURIDAD!\n"
                            + "El monto ingresado ($" + monto + ") supera el crédito real del cliente ($" + montoDevolucion + ").",
                            "Monto Excedido", JOptionPane.ERROR_MESSAGE);
                    return; // Bloqueamos la operación
                }
            }
            // ---------------------------------
            
            
            
            if (monto.compareTo(BigDecimal.ZERO) <= 0) return;
            if (monto.compareTo(saldo) > 0) {
                JOptionPane.showMessageDialog(this, "El monto supera el saldo pendiente");
                return;
            }

            
            Pago pago = crearPago(tipo, monto);
            pagos.add(pago);

            montoBruto = montoBruto.add(pago.getMontoBruto());
            totalPagado = totalPagado.add(pago.getMontoFinal());
            saldo = totalVenta.subtract(montoBruto);

            agregarPagoATabla(pago);
            actualizarTotales();
            cargarTiposPago();
            txtMonto.setText("");
            txtMonto.requestFocus();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Monto inválido");
        }
    }

    private void btnFinalizarActionPerformed(java.awt.event.ActionEvent evt) {
        if (pagos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar al menos un pago");
            return;
        }
        if (montoBruto.compareTo(totalVenta) < 0) {
            JOptionPane.showMessageDialog(this, "El monto pagado no cubre el total");
            return;
        }
        this.dispose();
    }

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {
        int fila = tblPagos.getSelectedRow();
        if (fila == -1) return;
        Pago p = pagos.get(fila);
        montoBruto = montoBruto.subtract(p.getMontoBruto());
        totalPagado = totalPagado.subtract(p.getMontoFinal());
        pagos.remove(fila);
        ((DefaultTableModel) tblPagos.getModel()).removeRow(fila);
        actualizarTotales();
        cargarTiposPago();
    }

    private void actualizarTotales() {
        if (totalVenta == null) totalVenta = BigDecimal.ZERO;
        saldo = totalVenta.subtract(montoBruto);
        lblTotalVenta.setText("TOTAL A COBRAR: $" + totalVenta);
        lblTotalPagado.setText("Total Bruto: $" + montoBruto);
        lblTotalCancelado.setText("Total Real (con desc.): $" + totalPagado);
        lblSaldo.setText("SALDO PENDIENTE: $" + saldo);
        
        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            lblSaldo.setForeground(verdeDinero);
            lblSaldo.setText("PAGO COMPLETADO ✔");
        } else {
            lblSaldo.setForeground(rojoBoton);
        }
        
        boolean yaSeUsoDevolucion = pagos.stream().anyMatch(p -> p.getTipo() == TipoPago.DEVOLUCION);
    
        if (yaSeUsoDevolucion) {
            lblCreditoDisponible.setText("Crédito: APLICADO");
            lblCreditoDisponible.setForeground(verdeDinero);
        } else {
            lblCreditoDisponible.setText("Crédito Disp: $" + montoDevolucion);
            lblCreditoDisponible.setForeground(new java.awt.Color(52, 152, 219));
        }
        
    }

    private Pago crearPago(TipoPago tipo, BigDecimal montoBruto) {
        BigDecimal porcentaje = obtenerPorcentajeDescuento(tipo);
        BigDecimal descuento = montoBruto.multiply(porcentaje).divide(new BigDecimal("100"));
        BigDecimal montoFinal = montoBruto.subtract(descuento);

        Pago p = new Pago();
        p.setTipo(tipo);
        p.setMontoBruto(montoBruto);
        p.setDescuento(descuento);
        p.setMontoFinal(montoFinal);
        return p;
    }

    private BigDecimal obtenerPorcentajeDescuento(TipoPago tipo) {
        Config conf = confDao.obtenerConfig();
        switch (tipo) {
            case EFECTIVO: return conf.getEfectivo();
            case TRANSFERENCIA: return conf.getTransferencia();
            case CREDITO: return conf.getCredito();
            default: return BigDecimal.ZERO;
        }
    }
    
          /*=====ZONA DE GETTERS=====*/
    
    public List<Pago> getPagos() {
        return pagos;
    }
    
    public BigDecimal getTotalPagado() {
        return totalPagado;
    }

    public BigDecimal getTotalVenta() {
        return totalVenta;
    }

    public BigDecimal getMontoBruto() {
        return montoBruto;
    }

    private void cargarTiposPago() {
        cmbTipoPago.removeAllItems();
        cmbTipoPago.addItem(TipoPago.EFECTIVO);
        cmbTipoPago.addItem(TipoPago.TRANSFERENCIA);
        cmbTipoPago.addItem(TipoPago.CREDITO);

        // Solo habilitamos la opción DEVOLUCION si:
        // 1. El saldo a favor que viene de la vista Devoluciones es > 0
        // 2. Y todavía NO está en nuestra lista de pagos actual
        boolean yaSeUsoDevolucion = pagos.stream()
                .anyMatch(p -> p.getTipo() == TipoPago.DEVOLUCION);

        if (montoDevolucion.compareTo(BigDecimal.ZERO) > 0 && !yaSeUsoDevolucion) {
            cmbTipoPago.addItem(TipoPago.DEVOLUCION);
        }
    }

    private void agregarPagoATabla(Pago p) {
        DefaultTableModel mod = (DefaultTableModel) tblPagos.getModel();
        mod.addRow(new Object[]{p.getTipo(), p.getMontoBruto(), p.getDescuento(), p.getMontoFinal()});
    }
    
    private void aplicarPagoDevolucionAutomatica() {
    // Esto hace que apenas abra la ventana, ya figure el "descuento" por el cambio
    Pago pDev = new Pago();
    pDev.setTipo(TipoPago.DEVOLUCION);
    pDev.setMontoBruto(montoDevolucion);
    pDev.setDescuento(BigDecimal.ZERO);
    pDev.setMontoFinal(montoDevolucion);
    
    pagos.add(pDev);
    montoBruto = montoBruto.add(montoDevolucion);
    totalPagado = totalPagado.add(montoDevolucion);
    agregarPagoATabla(pDev);
    actualizarTotales();
}

    // Declaración de variables UI necesarias
    private javax.swing.JButton btnAgregarPago, btnEliminar, btnFinalizar;
    private javax.swing.JComboBox<TipoPago> cmbTipoPago;
    private javax.swing.JLabel lblSaldo, lblTotalCancelado, lblTotalPagado, lblTotalVenta, lblCreditoDisponible;
    private javax.swing.JTable tblPagos;
    private javax.swing.JTextField txtMonto;
    
}