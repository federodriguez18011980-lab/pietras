package Vista;

import Modelo.*;
import Servicio.WooCommerce.SincronizadorWooCommerce;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class PedidosWebView extends JFrame {

    private JTable tablaPedidos;
    private DefaultTableModel modelPedidos;
    private JTextPane txtDetallePedido;
    private JButton btnAprobar, btnVerWeb, btnActualizar;

    private final SincronizadorWooCommerce sincro = new SincronizadorWooCommerce();
    private final ProductosDao proDao = new ProductosDao();

    private List<WooOrder> pedidosActivos = new ArrayList<>();
    private WooOrder pedidoSeleccionado = null;

    // Colores Pietras Premium
    private final java.awt.Color colorPietras = new java.awt.Color(244, 119, 21); // Naranja
    private final java.awt.Color colorCobrizo = new java.awt.Color(184, 139, 74); // Cobrizo
    private final java.awt.Color colorGrisFondo = new java.awt.Color(245, 246, 250);
    private final java.awt.Color colorAzulHeader = new java.awt.Color(44, 62, 80);

    public PedidosWebView() {
        setTitle("Pietras - Panel de Pedidos WooCommerce");
        setSize(950, 550);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initComponents();
        cargarPedidosAsync();
    }

    private void initComponents() {
        // --- 1. PANEL SUPERIOR (NORTE) ---
        JPanel pnlCabecera = new JPanel(new BorderLayout());
        pnlCabecera.setBackground(colorPietras);
        pnlCabecera.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblTitulo = new JLabel("PEDIDOS PENDIENTES DE LA TIENDA WEB", JLabel.CENTER);
        lblTitulo.setForeground(java.awt.Color.WHITE);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        pnlCabecera.add(lblTitulo, BorderLayout.CENTER);

        add(pnlCabecera, BorderLayout.NORTH);

        // --- 2. PANEL CENTRAL (TABLA + DETALLE) ---
        JPanel pnlCuerpo = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlCuerpo.setBackground(colorGrisFondo);
        pnlCuerpo.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 2.1 Panel Tabla (Izquierda)
        JPanel pnlTabla = new JPanel(new BorderLayout());
        pnlTabla.setBackground(java.awt.Color.WHITE);
        pnlTabla.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220), 1, true),
                " Pedidos Pendientes de Pago / Procesamiento ",
                0, 0, new Font("SansSerif", Font.BOLD, 12), colorAzulHeader));

        modelPedidos = new DefaultTableModel(new String[]{"ID Pedido", "Fecha", "Cliente", "Monto Total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPedidos = new JTable(modelPedidos);
        configurarTabla();

        tablaPedidos.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int fila = tablaPedidos.getSelectedRow();
                if (fila != -1) {
                    pedidoSeleccionado = pedidosActivos.get(fila);
                    mostrarDetallePedido(pedidoSeleccionado);
                    btnAprobar.setEnabled(true);
                    btnVerWeb.setEnabled(true);
                }
            }
        });

        pnlTabla.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);
        pnlCuerpo.add(pnlTabla);

        // 2.2 Panel Detalle (Derecha)
        JPanel pnlDetalle = new JPanel(new BorderLayout(0, 10));
        pnlDetalle.setBackground(java.awt.Color.WHITE);
        pnlDetalle.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220), 1, true),
                " Desglose del Pedido Seleccionado ",
                0, 0, new Font("SansSerif", Font.BOLD, 12), colorAzulHeader));

        txtDetallePedido = new JTextPane();
        txtDetallePedido.setEditable(false);
        txtDetallePedido.setContentType("text/html");
        txtDetallePedido.setText("<html><body style='font-family:sans-serif; font-size:11px; padding:10px; color:#555;'><center><i>Seleccione un pedido del listado para ver su desglose...</i></center></body></html>");
        
        pnlDetalle.add(new JScrollPane(txtDetallePedido), BorderLayout.CENTER);
        pnlCuerpo.add(pnlDetalle);

        add(pnlCuerpo, BorderLayout.CENTER);

        // --- 3. PANEL INFERIOR: BOTONES (SUR) ---
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlSur.setBackground(java.awt.Color.WHITE);
        pnlSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(220, 220, 220)));

        btnActualizar = new JButton("Actualizar Lista");
        estiloBoton(btnActualizar, colorAzulHeader);
        btnActualizar.addActionListener(e -> cargarPedidosAsync());

        btnVerWeb = new JButton("Ver en WooCommerce");
        estiloBoton(btnVerWeb, colorCobrizo);
        btnVerWeb.setEnabled(false);
        btnVerWeb.addActionListener(e -> abrirEnNavegador());

        btnAprobar = new JButton("APROBAR Y DESCONTAR STOCK");
        estiloBoton(btnAprobar, new java.awt.Color(39, 174, 96)); // Verde
        btnAprobar.setEnabled(false);
        btnAprobar.addActionListener(e -> procesarPedidoSeleccionado());

        pnlSur.add(btnActualizar);
        pnlSur.add(btnVerWeb);
        pnlSur.add(btnAprobar);

        add(pnlSur, BorderLayout.SOUTH);
    }

    private void estiloBoton(JButton btn, java.awt.Color colorFondo) {
        btn.setBackground(colorFondo);
        btn.setForeground(java.awt.Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private void configurarTabla() {
        tablaPedidos.setRowHeight(30);
        tablaPedidos.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tablaPedidos.getColumnCount(); i++) {
            tablaPedidos.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }

    // --- CARGAR PEDIDOS DESDE LA API ---
    public void cargarPedidosAsync() {
        modelPedidos.setRowCount(0);
        pedidosActivos.clear();
        btnAprobar.setEnabled(false);
        btnVerWeb.setEnabled(false);
        txtDetallePedido.setText("<html><body style='font-family:sans-serif; font-size:11px; padding:10px; color:#555;'><center><i>Cargando pedidos desde Joyería Pietras Web...</i></center></body></html>");

        new Thread(() -> {
            try {
                List<WooOrder> pedidos = sincro.obtenerPedidosProcesando();
                SwingUtilities.invokeLater(() -> {
                    if (pedidos == null || pedidos.isEmpty()) {
                        txtDetallePedido.setText("<html><body style='font-family:sans-serif; font-size:11px; padding:10px; color:#555;'><center><b>No hay pedidos web pendientes de procesar. ✨</b></center></body></html>");
                        return;
                    }

                    pedidosActivos = pedidos;
                    for (WooOrder o : pedidosActivos) {
                        String cliente = o.billing != null ? o.billing.first_name + " " + o.billing.last_name : "N/A";
                        // Limpiar fecha (Ej: 2026-06-20T14:20:00 -> 2026-06-20 14:20)
                        String fecha = o.date_created != null ? o.date_created.replace("T", " ") : "";
                        if (fecha.length() > 16) {
                            fecha = fecha.substring(0, 16);
                        }
                        modelPedidos.addRow(new Object[]{o.id, fecha, cliente, "$ " + o.total});
                    }
                    txtDetallePedido.setText("<html><body style='font-family:sans-serif; font-size:11px; padding:10px; color:#555;'><center><i>Seleccione un pedido para ver el detalle...</i></center></body></html>");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    txtDetallePedido.setText("<html><body style='font-family:sans-serif; font-size:11px; padding:10px; color:#95a5a6;'><center>❌ Error al conectar con WooCommerce:<br>" + e.getMessage() + "</center></body></html>");
                });
            }
        }).start();
    }

    // --- MOSTRAR DETALLE DEL PEDIDO ---
    private void mostrarDetallePedido(WooOrder order) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:sans-serif; font-size:11px; padding:8px; color:#333;'>");

        // Datos del Cliente
        html.append("<h3 style='color:#B88B4A; margin-bottom:5px;'>DATOS DEL COMPRADOR</h3>");
        if (order.billing != null) {
            html.append("<b>Nombre:</b> ").append(order.billing.first_name).append(" ").append(order.billing.last_name).append("<br>");
            html.append("<b>Email:</b> ").append(order.billing.email).append("<br>");
            html.append("<b>Teléfono:</b> ").append(order.billing.phone).append("<br>");
        } else {
            html.append("No hay datos de facturación disponibles.<br>");
        }
        html.append("<hr style='border:0; border-top:1px solid #ddd; margin:10px 0;'>");

        // Desglose de Artículos
        html.append("<h3 style='color:#F47715; margin-bottom:5px;'>ARTÍCULOS COMPRADOS</h3>");
        html.append("<table width='100%' border='0' cellspacing='0' cellpadding='4' style='font-size:11px;'>");
        html.append("<tr bgcolor='#f2f2f2'><b><td align='left'>Artículo</td><td align='center'>Cant.</td><td align='right'>Precio</td><td align='center'>Stock Fis.</td></b></tr>");

        boolean stockSuficiente = true;

        for (WooOrder.LineItem item : order.line_items) {
            // Buscamos stock local
            Productos prodLocal = proDao.buscarPorIdWooCommerce(item.product_id);
            String stockLocalStr = "❌ No Vinc.";
            String colorStock = "#e74c3c"; // Rojo si no hay stock o no está vinculado
            
            if (prodLocal != null) {
                int stockDispon = prodLocal.getStock().intValue();
                stockLocalStr = String.valueOf(stockDispon);
                if (stockDispon >= item.quantity) {
                    colorStock = "#27ae60"; // Verde
                } else {
                    colorStock = "#e67e22"; // Naranja si falta stock
                    stockSuficiente = false;
                }
            } else {
                stockSuficiente = false;
            }

            html.append("<tr>");
            html.append("<td align='left'>").append(item.name).append(" (SKU: ").append(item.sku != null ? item.sku : "S/S").append(")</td>");
            html.append("<td align='center'>").append(item.quantity).append("</td>");
            html.append("<td align='right'>$ ").append(item.price).append("</td>");
            html.append("<td align='center' style='color:").append(colorStock).append("; font-weight:bold;'>").append(stockLocalStr).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        html.append("<hr style='border:0; border-top:1px solid #ddd; margin:10px 0;'>");

        // Resumen
        html.append("<table width='100%' style='font-size:12px; font-weight:bold;'>");
        html.append("<tr><td>Monto Total:</td><td align='right' style='color:#2c3e50;'>$ ").append(order.total).append("</td></tr>");
        html.append("</table>");

        if (!stockSuficiente) {
            html.append("<div style='margin-top:10px; padding:6px; background-color:#fdf2e9; border:1px solid #e59866; color:#a04000; border-radius:5px; font-size:10px; font-weight:bold;'>");
            html.append("⚠️ Atención: Uno o más productos no disponen de stock suficiente en la joyería física o no están vinculados.");
            html.append("</div>");
        }

        html.append("</body></html>");
        txtDetallePedido.setText(html.toString());
        txtDetallePedido.setCaretPosition(0);
    }

    // --- PROCESAR PEDIDO SELECCIONADO ---
    private void procesarPedidoSeleccionado() {
        if (pedidoSeleccionado == null) return;

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea procesar el pedido #" + pedidoSeleccionado.id + "?\nSe descontará el stock local y se marcará completado en la web.",
                "Confirmar Procesamiento", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirmar != JOptionPane.YES_OPTION) return;

        btnAprobar.setEnabled(false);
        btnActualizar.setEnabled(false);
        txtDetallePedido.setText("<html><body style='font-family:sans-serif; font-size:11px; padding:10px; color:#555;'><center><b>Procesando pedido y actualizando inventarios, por favor espere... ⏳</b></center></body></html>");

        new Thread(() -> {
            boolean stockExitoso = true;
            StringBuilder logExplicativo = new StringBuilder();

            // 1. Descontar stock local en base de datos H2
            for (WooOrder.LineItem item : pedidoSeleccionado.line_items) {
                Productos prodLocal = proDao.buscarPorIdWooCommerce(item.product_id);
                if (prodLocal != null) {
                    int nuevoStock = prodLocal.getStock().intValue() - item.quantity;
                    proDao.actualizarStockFisico(prodLocal.getId(), nuevoStock);

                    // Registrar movimiento de salida
                    MovimientoStock mov = new MovimientoStock();
                    mov.setIdProducto(prodLocal.getId());
                    mov.setTipoMovimiento("SALIDA");
                    mov.setCantidad((double) item.quantity);
                    mov.setMotivo("VENTA WEB PEDIDO #" + pedidoSeleccionado.id);
                    new MovimientoDao().registrarMovimiento(mov);

                    logExplicativo.append("✔ Stock descontado: ").append(prodLocal.getDescripcion()).append(" (Nuevo stock: ").append(nuevoStock).append(")\n");
                } else {
                    stockExitoso = false;
                    logExplicativo.append("⚠️ No se pudo descontar stock local para: ").append(item.name).append(" (No vinculado en H2)\n");
                }
            }

            // 2. Cambiar estado del pedido en WooCommerce a completado
            boolean webExitosa = sincro.completarPedidoEnWeb(pedidoSeleccionado.id);

            SwingUtilities.invokeLater(() -> {
                btnActualizar.setEnabled(true);
                if (webExitosa) {
                    JOptionPane.showMessageDialog(this,
                            "Pedido #" + pedidoSeleccionado.id + " procesado con éxito.\n\n" + logExplicativo,
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarPedidosAsync();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "El stock se descontó localmente pero falló la actualización del estado en WordPress.\n\n" + logExplicativo,
                            "Error de Sincronización Web", JOptionPane.WARNING_MESSAGE);
                    cargarPedidosAsync();
                }
            });
        }).start();
    }

    // --- ABRIR PEDIDO EN NAVEGADOR ---
    private void abrirEnNavegador() {
        if (pedidoSeleccionado == null) return;
        try {
            String url = "https://joyeriapietras.com.ar/wp-admin/post.php?post=" + pedidoSeleccionado.id + "&action=edit";
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el navegador: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
