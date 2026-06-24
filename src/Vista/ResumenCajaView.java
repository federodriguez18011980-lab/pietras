package Vista;

import Modelo.CajaApertura;
import Modelo.CajaAperturaDao;
import Modelo.CajaCierre;
import Modelo.CajaCierreDao;
import Modelo.CajaMovimientoDao;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class ResumenCajaView extends JFrame {

    private JLabel lblDineroFisico, lblDiferenciaCaja;
    private JTextField txtDinero;
    
    // KPI Cards Labels
    private JLabel lblKPISaldoInicial, lblKPIIngresos, lblKPISalidas;
    
    // Desglose Labels
    private JLabel lblVentasEfectivo, lblVentasTransferencia, lblVentasCredito, lblSalidasDetalle;
    private JLabel lblEfectivoEsperado, lblSaldoFinalTotal;

    // Servicios / DAOs
    private final CajaMovimientoDao movDao = new CajaMovimientoDao();
    private final CajaAperturaDao aperturaDao = new CajaAperturaDao();
    private final CajaCierreDao cierreDao = new CajaCierreDao();
    private CajaApertura apertura;
    private final String usuario;

    // Colores Pietras Premium
    private final Color colorPietras = new Color(244, 119, 21); // Naranja
    private final Color colorCobrizo = new Color(184, 139, 74); // Cobrizo/Dorado
    private final Color colorGrisFondo = new Color(245, 246, 250);
    private final Color colorKPIFondo = new Color(242, 244, 244);
    private final Color colorAzulHeader = new Color(44, 62, 80);

    public ResumenCajaView(String usuario) {
        this.usuario = usuario;
        apertura = aperturaDao.cajaAbiertaDelDia();

        if (apertura == null) {
            JOptionPane.showMessageDialog(this, "No hay caja abierta para realizar el arqueo.");
            dispose();
            return;
        }

        setTitle("Pietras - Resumen y Arqueo de Caja");
        setSize(500, 720);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        initComponents();
        cargarResumen();
    }

    private void initComponents() {
        // --- 1. CABECERA PREMIUM (NORTE) ---
        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setBackground(colorPietras);
        pnlCabecera.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("ARQUEO Y CIERRE DE CAJA", JLabel.CENTER);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        pnlCabecera.add(lblTitulo, BorderLayout.CENTER);

        add(pnlCabecera, BorderLayout.NORTH);

        // --- 2. PANEL CENTRAL: CUERPO (KPIs + DESGLOSE + ARQUEO) ---
        JPanel pnlCuerpo = new JPanel();
        pnlCuerpo.setBackground(colorGrisFondo);
        pnlCuerpo.setLayout(new BoxLayout(pnlCuerpo, BoxLayout.Y_AXIS));
        pnlCuerpo.setBorder(new EmptyBorder(15, 20, 15, 20));

        // SECTION 2.1: KPI Cards Superiores
        JPanel pnlKPIs = new JPanel(new GridLayout(1, 3, 10, 0));
        pnlKPIs.setOpaque(false);
        pnlKPIs.setMaximumSize(new Dimension(500, 85));

        lblKPISaldoInicial = new JLabel("$ 0.00", JLabel.CENTER);
        lblKPIIngresos = new JLabel("$ 0.00", JLabel.CENTER);
        lblKPISalidas = new JLabel("$ 0.00", JLabel.CENTER);

        pnlKPIs.add(crearKPICard("SALDO INICIAL 💼", lblKPISaldoInicial, colorCobrizo));
        pnlKPIs.add(crearKPICard("INGRESOS 📈", lblKPIIngresos, new Color(39, 174, 96)));
        pnlKPIs.add(crearKPICard("SALIDAS 📉", lblKPISalidas, new Color(192, 57, 43)));

        pnlCuerpo.add(pnlKPIs);
        pnlCuerpo.add(Box.createVerticalStrut(15));

        // SECTION 2.2: Panel de Desglose de Medios de Pago
        JPanel pnlDesglose = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        pnlDesglose.setBorder(new EmptyBorder(15, 15, 15, 15));
        pnlDesglose.setMaximumSize(new Dimension(500, 220));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título Desglose
        JLabel lblTituloDesglose = new JLabel("Resumen de Ventas y Movimientos");
        lblTituloDesglose.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTituloDesglose.setForeground(colorAzulHeader);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        pnlDesglose.add(lblTituloDesglose, gbc);
        gbc.gridwidth = 1;

        // Fila 1: Ventas Efectivo
        gbc.gridy = 1; gbc.gridx = 0;
        pnlDesglose.add(new JLabel("Ventas Efectivo:"), gbc);
        lblVentasEfectivo = new JLabel("$ 0.00", JLabel.RIGHT);
        lblVentasEfectivo.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 1;
        pnlDesglose.add(lblVentasEfectivo, gbc);

        // Fila 2: Ventas Transferencia
        gbc.gridy = 2; gbc.gridx = 0;
        pnlDesglose.add(new JLabel("Ventas Transferencias:"), gbc);
        lblVentasTransferencia = new JLabel("$ 0.00", JLabel.RIGHT);
        lblVentasTransferencia.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 1;
        pnlDesglose.add(lblVentasTransferencia, gbc);

        // Fila 3: Ventas Tarjeta Crédito
        gbc.gridy = 3; gbc.gridx = 0;
        pnlDesglose.add(new JLabel("Ventas Crédito:"), gbc);
        lblVentasCredito = new JLabel("$ 0.00", JLabel.RIGHT);
        lblVentasCredito.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 1;
        pnlDesglose.add(lblVentasCredito, gbc);

        // Fila 4: Egresos Efectivo
        gbc.gridy = 4; gbc.gridx = 0;
        pnlDesglose.add(new JLabel("Egresos de Efectivo:"), gbc);
        lblSalidasDetalle = new JLabel("$ 0.00", JLabel.RIGHT);
        lblSalidasDetalle.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblSalidasDetalle.setForeground(new Color(192, 57, 43));
        gbc.gridx = 1;
        pnlDesglose.add(lblSalidasDetalle, gbc);

        // Separador
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 220, 220));
        pnlDesglose.add(sep, gbc);
        gbc.gridwidth = 1;

        // Fila 5: Saldo Final Total (Negrita)
        gbc.gridy = 6; gbc.gridx = 0;
        JLabel lblTotalLabel = new JLabel("SALDO TOTAL EN CAJA:");
        lblTotalLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTotalLabel.setForeground(colorCobrizo);
        pnlDesglose.add(lblTotalLabel, gbc);

        lblSaldoFinalTotal = new JLabel("$ 0.00", JLabel.RIGHT);
        lblSaldoFinalTotal.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblSaldoFinalTotal.setForeground(colorCobrizo);
        gbc.gridx = 1;
        pnlDesglose.add(lblSaldoFinalTotal, gbc);

        pnlCuerpo.add(pnlDesglose);
        pnlCuerpo.add(Box.createVerticalStrut(15));

        // SECTION 2.3: Panel de Arqueo y Cuadre de Caja
        JPanel pnlArqueo = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(colorCobrizo);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        pnlArqueo.setBorder(new EmptyBorder(15, 15, 15, 15));
        pnlArqueo.setMaximumSize(new Dimension(500, 190));

        GridBagConstraints gbcA = new GridBagConstraints();
        gbcA.insets = new Insets(5, 5, 5, 5);
        gbcA.fill = GridBagConstraints.HORIZONTAL;

        // Fila 1: Efectivo Esperado
        gbcA.gridx = 0; gbcA.gridy = 0;
        JLabel lblExp = new JLabel("Efectivo Esperado en Caja:");
        lblExp.setFont(new Font("SansSerif", Font.BOLD, 12));
        pnlArqueo.add(lblExp, gbcA);

        lblEfectivoEsperado = new JLabel("$ 0.00", JLabel.RIGHT);
        lblEfectivoEsperado.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblEfectivoEsperado.setForeground(colorAzulHeader);
        gbcA.gridx = 1;
        pnlArqueo.add(lblEfectivoEsperado, gbcA);

        // Fila 2: Efectivo Físico (Input)
        gbcA.gridy = 1; gbcA.gridx = 0;
        lblDineroFisico = new JLabel("Dinero Físico Real:");
        lblDineroFisico.setFont(new Font("SansSerif", Font.BOLD, 12));
        pnlArqueo.add(lblDineroFisico, gbcA);

        txtDinero = new JTextField();
        txtDinero.setFont(new Font("SansSerif", Font.BOLD, 14));
        txtDinero.setHorizontalAlignment(JTextField.CENTER);
        txtDinero.setPreferredSize(new Dimension(140, 32));
        txtDinero.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));

        // Restringir entrada a decimales
        txtDinero.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!(Character.isDigit(c) || c == '.')) {
                    evt.consume();
                }
                if (c == '.' && txtDinero.getText().contains(".")) {
                    evt.consume();
                }
            }
        });

        // Actualizar diferencia en tiempo real
        txtDinero.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarDiferencia(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarDiferencia(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarDiferencia(); }
        });
        gbcA.gridx = 1;
        pnlArqueo.add(txtDinero, gbcA);

        // Fila 3: Diferencia de Caja (Etiqueta dinámica grande)
        gbcA.gridy = 2; gbcA.gridx = 0; gbcA.gridwidth = 2;
        lblDiferenciaCaja = new JLabel("Diferencia: --", JLabel.CENTER);
        lblDiferenciaCaja.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblDiferenciaCaja.setBorder(new EmptyBorder(10, 0, 0, 0));
        pnlArqueo.add(lblDiferenciaCaja, gbcA);

        pnlCuerpo.add(pnlArqueo);

        add(pnlCuerpo, BorderLayout.CENTER);

        // --- 3. PANEL SUR: BOTONES Y ACCIONES PREMIUM ---
        JPanel pnlSur = new JPanel(new GridBagLayout());
        pnlSur.setBackground(Color.WHITE);
        pnlSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        pnlSur.setPreferredSize(new Dimension(500, 160));

        GridBagConstraints gbcS = new GridBagConstraints();
        gbcS.insets = new Insets(6, 15, 6, 15);
        gbcS.fill = GridBagConstraints.HORIZONTAL;
        gbcS.weightx = 1.0;

        // Fila 1: Botones de Retiros e Históricos
        JButton btnRetirar = crearBotonPremium("Retirar Efectivo", "/Img/dinero-24.png", colorCobrizo);
        btnRetirar.addActionListener(e -> new RetiroCajaView(this.usuario).setVisible(true));
        gbcS.gridx = 0; gbcS.gridy = 0;
        pnlSur.add(btnRetirar, gbcS);

        JButton btnVerRetiros = crearBotonPremium("Ver Retiros", "/Img/reporte 24x24.png", colorCobrizo);
        btnVerRetiros.addActionListener(e -> new HistorialRetirosView().setVisible(true));
        gbcS.gridx = 1;
        pnlSur.add(btnVerRetiros, gbcS);

        // Fila 2: Botón Histórico Cierres
        JButton btnHistorico = crearBotonPremium("Histórico de Cierres", "/Img/reporte 24x24.png", colorAzulHeader);
        btnHistorico.addActionListener(e -> new HistoricoCierresView().setVisible(true));
        gbcS.gridx = 0; gbcS.gridy = 1; gbcS.gridwidth = 2;
        pnlSur.add(btnHistorico, gbcS);

        // Fila 3: Botón Cerrar Caja Destacado
        JButton btnCerrar = crearBotonPremium("CERRAR CAJA DEFINITIVO", "/Img/GuardarTodo24.png", new Color(39, 174, 96));
        btnCerrar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnCerrar.setPreferredSize(new Dimension(0, 36));
        btnCerrar.addActionListener(e -> cerrarCaja());
        gbcS.gridy = 2;
        pnlSur.add(btnCerrar, gbcS);

        add(pnlSur, BorderLayout.SOUTH);
    }

    private JPanel crearKPICard(String titulo, JLabel lblValor, Color colorBorde) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colorKPIFondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(colorBorde);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        card.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel lblTit = new JLabel(titulo, JLabel.CENTER);
        lblTit.setFont(new Font("SansSerif", Font.BOLD, 9));
        lblTit.setForeground(colorBorde);

        lblValor.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblValor.setForeground(colorAzulHeader);

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);

        return card;
    }

    private JButton crearBotonPremium(String texto, String rutaIcono, Color colorFondo) {
        JButton boton = new JButton(texto);
        boton.setBackground(colorFondo);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("SansSerif", Font.BOLD, 11));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(150, 32));
        boton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        try {
            ImageIcon icono = new ImageIcon(getClass().getResource(rutaIcono));
            Image img = icono.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            boton.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            System.err.println("Icono no cargado: " + rutaIcono);
        }

        return boton;
    }

    private void cargarResumen() {
        int id = apertura.getId();
        BigDecimal montoInicial = apertura.getMontoInicial();

        BigDecimal efectivo = movDao.totalPorTipo(id, "EFECTIVO");
        BigDecimal transferencia = movDao.totalPorTipo(id, "TRANSFERENCIA");
        BigDecimal credito = movDao.totalPorTipo(id, "CREDITO");

        BigDecimal devoluciones = movDao.totalPorTipo(id, "DEVOLUCION");
        BigDecimal anulaciones = movDao.totalPorTipo(id, "ANULACION");
        BigDecimal retiros = movDao.totalPorTipo(id, "RETIRO");

        // Egresos totales de la caja
        BigDecimal egresos = retiros.add(devoluciones).add(anulaciones);
        BigDecimal ingresos = efectivo.add(transferencia).add(credito);

        // Saldo final sumando fondo inicial
        BigDecimal saldoFinal = montoInicial.add(ingresos).subtract(egresos.abs());
        // Efectivo teórico que debe haber físicamente (Fondo inicial + Ventas Efectivo - Retiros/Devoluciones)
        BigDecimal esperadoEfectivo = montoInicial.add(efectivo).subtract(egresos.abs());

        // Cargar en interfaz
        lblKPISaldoInicial.setText("$ " + String.format(Locale.US, "%,.2f", montoInicial));
        lblKPIIngresos.setText("$ " + String.format(Locale.US, "%,.2f", ingresos));
        lblKPISalidas.setText("$ " + String.format(Locale.US, "%,.2f", egresos.abs()));

        lblVentasEfectivo.setText("$ " + String.format(Locale.US, "%,.2f", efectivo));
        lblVentasTransferencia.setText("$ " + String.format(Locale.US, "%,.2f", transferencia));
        lblVentasCredito.setText("$ " + String.format(Locale.US, "%,.2f", credito));
        lblSalidasDetalle.setText("-$ " + String.format(Locale.US, "%,.2f", egresos.abs()));
        lblSaldoFinalTotal.setText("$ " + String.format(Locale.US, "%,.2f", saldoFinal));
        
        lblEfectivoEsperado.setText("$ " + String.format(Locale.US, "%,.2f", esperadoEfectivo));
        actualizarDiferencia();
    }

    private void actualizarDiferencia() {
        try {
            BigDecimal esperado = obtenerEfectivoEsperado();
            BigDecimal fisico = getMontoFisico();
            BigDecimal diferencia = fisico.subtract(esperado);

            if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
                lblDiferenciaCaja.setForeground(new Color(192, 57, 43)); // Rojo
                lblDiferenciaCaja.setText("⚠️ FALTANTE: -$" + String.format(Locale.US, "%,.2f", diferencia.abs()));
            } else if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
                lblDiferenciaCaja.setForeground(new Color(39, 174, 96)); // Verde
                lblDiferenciaCaja.setText("✅ SOBRANTE: +$" + String.format(Locale.US, "%,.2f", diferencia));
            } else {
                lblDiferenciaCaja.setForeground(colorAzulHeader);
                lblDiferenciaCaja.setText("✨ CAJA PERFECTA (Sin Diferencias)");
            }
        } catch (NumberFormatException e) {
            lblDiferenciaCaja.setText("Diferencia: --");
        }
    }

    private BigDecimal obtenerEfectivoEsperado() {
        BigDecimal montoInicial = apertura.getMontoInicial();
        BigDecimal efectivo = movDao.totalPorTipo(apertura.getId(), "EFECTIVO");
        BigDecimal salidas = movDao.totalPorTipo(apertura.getId(), "RETIRO")
                .add(movDao.totalPorTipo(apertura.getId(), "DEVOLUCION"))
                .add(movDao.totalPorTipo(apertura.getId(), "ANULACION"));
        return montoInicial.add(efectivo).subtract(salidas.abs());
    }

    private BigDecimal getMontoFisico() {
        try {
            String texto = txtDinero.getText().trim();
            return texto.isEmpty() ? BigDecimal.ZERO : new BigDecimal(texto);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private void cerrarCaja() {
        String textoFisico = txtDinero.getText().trim();
        if (textoFisico.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el monto físico real de la caja para cerrar.");
            txtDinero.requestFocus();
            return;
        }

        BigDecimal montoFisico = new BigDecimal(textoFisico);
        CajaCierre cierre = generarObjetoCierre(montoFisico);

        if (cierre.getDiferencia().compareTo(BigDecimal.ZERO) != 0) {
            int confirmar = JOptionPane.showConfirmDialog(this,
                    "Se detectó una diferencia de caja de $" + String.format(Locale.US, "%,.2f", cierre.getDiferencia()) + 
                    ".\n¿Desea registrar y cerrar la caja con esta diferencia?",
                    "Confirmar Diferencia", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmar != JOptionPane.YES_OPTION) {
                return;
            }
        }

        if (cierreDao.ejecutarCierreCompleto(cierre)) {
            StringBuilder reporte = new StringBuilder();
            reporte.append("===============================\n");
            reporte.append("       CIERRE DE CAJA EXITOSO  \n");
            reporte.append("===============================\n");
            reporte.append(String.format("Usuario: %s\n", cierre.getUsuario()));
            reporte.append(String.format("Esperado en Efectivo: $%.2f\n", obtenerEfectivoEsperado()));
            reporte.append(String.format("Ingresado físicamente: $%.2f\n", montoFisico));
            reporte.append(String.format("Diferencia Registrada: $%.2f\n", cierre.getDiferencia()));
            reporte.append("===============================\n");

            JOptionPane.showMessageDialog(this, reporte.toString(), "Resumen de Cierre Pietras", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Ocurrió un error al intentar procesar el cierre en la base de datos.", "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private CajaCierre generarObjetoCierre(BigDecimal montoFisicoReal) {
        int id = apertura.getId();
        BigDecimal montoInicial = apertura.getMontoInicial();
        
        BigDecimal ventasEfe = movDao.totalPorTipo(id, "EFECTIVO");
        BigDecimal ventasTrans = movDao.totalPorTipo(id, "TRANSFERENCIA"); 
        BigDecimal ventasCred = movDao.totalPorTipo(id, "CREDITO"); 
        
        BigDecimal totalSalidas = movDao.totalPorTipo(id, "RETIRO")
                .add(movDao.totalPorTipo(id, "DEVOLUCION"))
                .add(movDao.totalPorTipo(id, "ANULACION"));

        BigDecimal esperadoEfectivo = montoInicial.add(ventasEfe).subtract(totalSalidas.abs());
        BigDecimal saldoFinalTotal = montoInicial.add(ventasEfe).add(ventasTrans).add(ventasCred).subtract(totalSalidas.abs());

        CajaCierre cierre = new CajaCierre();
        cierre.setFecha(java.time.LocalDate.now());
        cierre.setHoraCierre(java.time.LocalTime.now());
        cierre.setMontoFinal(saldoFinalTotal);
        cierre.setTotalEfectivo(ventasEfe);
        cierre.setTotalTransferencia(ventasTrans);
        cierre.setTotalCredito(ventasCred);
        cierre.setTotalSalidas(totalSalidas);
        cierre.setDiferencia(montoFisicoReal.subtract(esperadoEfectivo)); 
        cierre.setUsuario(usuario);
        cierre.setIdApertura(id);
        
        return cierre;
    }
}
