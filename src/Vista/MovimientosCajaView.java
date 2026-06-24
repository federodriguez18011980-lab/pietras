package Vista;

import Modelo.*;
import Conexion.ConexionMysql;
import com.toedter.calendar.JDateChooser;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MovimientosCajaView extends JFrame {

    private JDateChooser jdDesde, jdHasta;
    private JComboBox<String> cbTipoPago;
    private JComboBox<String> cbUsuario;
    private JTextField txtBuscar;
    private JTable tablaMovimientos;
    private DefaultTableModel modelMovimientos;
    
    // KPI Labels
    private JLabel lblTotalIngresos, lblTotalEgresos, lblBalanceNeto;

    // Servicios / DAOs
    private final ConexionMysql cn = new ConexionMysql();
    private final VentaDao ventaDao = new VentaDao();

    // Colores Pietras Premium
    private final Color colorPietras = new Color(244, 119, 21); // Naranja
    private final Color colorCobrizo = new Color(184, 139, 74); // Dorado/Cobrizo
    private final Color colorGrisFondo = new Color(245, 246, 250);
    private final Color colorKPIFondo = new Color(242, 244, 244);
    private final Color colorAzulHeader = new Color(44, 62, 80);

    public MovimientosCajaView() {
        setTitle("Pietras - Auditoría e Historial de Movimientos de Caja");
        setSize(1200, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initComponents();
        cargarUsuarios();
        ejecutarFiltro(); // Carga por defecto
    }

    private void initComponents() {
        // --- 1. PANEL SUPERIOR: FILTROS AVANZADOS ---
        JPanel pnlFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        pnlFiltros.setBackground(colorPietras);
        pnlFiltros.setBorder(new EmptyBorder(5, 10, 5, 10));

        pnlFiltros.add(crearEtiqueta("Desde:"));
        jdDesde = new JDateChooser();
        jdDesde.setPreferredSize(new Dimension(110, 26));
        jdDesde.setDate(new Date());

        pnlFiltros.add(jdDesde);

        pnlFiltros.add(crearEtiqueta("Hasta:"));
        jdHasta = new JDateChooser();
        jdHasta.setPreferredSize(new Dimension(110, 26));
        jdHasta.setDate(new Date());
        pnlFiltros.add(jdHasta);

        pnlFiltros.add(crearEtiqueta("Tipo:"));
        cbTipoPago = new JComboBox<>(new String[]{
            "TODOS", "EFECTIVO", "TRANSFERENCIA", "CREDITO", "RETIRO", "DEVOLUCION", "ANULACION", "ENTRADA"
        });
        cbTipoPago.setPreferredSize(new Dimension(130, 26));
        pnlFiltros.add(cbTipoPago);

        pnlFiltros.add(crearEtiqueta("Usuario:"));
        cbUsuario = new JComboBox<>();
        cbUsuario.setPreferredSize(new Dimension(130, 26));
        pnlFiltros.add(cbUsuario);

        pnlFiltros.add(crearEtiqueta("Descripción:"));
        txtBuscar = new JTextField();
        txtBuscar.setPreferredSize(new Dimension(130, 26));
        pnlFiltros.add(txtBuscar);

        JButton btnFiltrar = new JButton("BUSCAR");
        estiloBoton(btnFiltrar, new Color(44, 62, 80));
        btnFiltrar.addActionListener(e -> ejecutarFiltro());
        pnlFiltros.add(btnFiltrar);

        add(pnlFiltros, BorderLayout.NORTH);

        // --- 2. PANEL CENTRAL: DETALLES ---
        JPanel pnlCuerpo = new JPanel(new BorderLayout(15, 15));
        pnlCuerpo.setBackground(colorGrisFondo);
        pnlCuerpo.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Subpanel Superior: Tarjetas KPI de Caja
        JPanel pnlKPIs = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlKPIs.setOpaque(false);
        pnlKPIs.setPreferredSize(new Dimension(0, 100));

        lblTotalIngresos = new JLabel("<html><b>$0.00</b></html>", JLabel.CENTER);
        lblTotalEgresos = new JLabel("<html><b>$0.00</b></html>", JLabel.CENTER);
        lblBalanceNeto = new JLabel("<html><b>$0.00</b></html>", JLabel.CENTER);

        pnlKPIs.add(crearKPICard("INGRESOS TOTALES (VENTAS / ENTRADAS) ➕", lblTotalIngresos, new Color(39, 174, 96)));
        pnlKPIs.add(crearKPICard("EGRESOS TOTALES (RETIROS / SALIDAS) ➖", lblTotalEgresos, new Color(192, 57, 43)));
        pnlKPIs.add(crearKPICard("BALANCE DE FLUJO NETO ⚖️", lblBalanceNeto, colorCobrizo));

        pnlCuerpo.add(pnlKPIs, BorderLayout.NORTH);

        // Subpanel Central: Tabla de Movimientos
        JPanel pnlTablaContainer = new JPanel(new BorderLayout());
        pnlTablaContainer.setBackground(Color.WHITE);
        pnlTablaContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                " Historial de Entradas y Salidas de Caja ",
                0, 0, new Font("SansSerif", Font.BOLD, 13), colorAzulHeader));

        modelMovimientos = new DefaultTableModel(
            new String[]{"ID", "Fecha", "Hora", "Tipo Movimiento", "Monto", "Descripción", "Usuario", "Saldo Acumulado"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaMovimientos = new JTable(modelMovimientos);
        configurarTabla();

        pnlTablaContainer.add(new JScrollPane(tablaMovimientos), BorderLayout.CENTER);
        pnlCuerpo.add(pnlTablaContainer, BorderLayout.CENTER);

        add(pnlCuerpo, BorderLayout.CENTER);

        // --- 3. PANEL SUR: BOTONES DE EXPORTACIÓN ---
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlSur.setBackground(Color.WHITE);
        pnlSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton btnPdf = new JButton("Exportar Auditoría (PDF)");
        estiloBoton(btnPdf, new Color(192, 57, 43));
        btnPdf.addActionListener(e -> exportarPDF());

        JButton btnExcel = new JButton("Exportar Auditoría (Excel)");
        estiloBoton(btnExcel, new Color(30, 132, 73));
        btnExcel.addActionListener(e -> exportarExcel());

        pnlSur.add(btnPdf);
        pnlSur.add(btnExcel);

        add(pnlSur, BorderLayout.SOUTH);
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        return l;
    }

    private void estiloBoton(JButton btn, Color colorFondo) {
        btn.setBackground(colorFondo);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private JPanel crearKPICard(String titulo, JLabel lblValor, Color colorBorde) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colorKPIFondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(colorBorde);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel lblTitulo = new JLabel(titulo, JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblTitulo.setForeground(colorBorde);

        lblValor.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblValor.setForeground(colorAzulHeader);

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);

        return card;
    }

    private void configurarTabla() {
        tablaMovimientos.setRowHeight(28);
        tablaMovimientos.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof BigDecimal) {
                    BigDecimal val = (BigDecimal) value;
                    if (val.compareTo(BigDecimal.ZERO) < 0) {
                        setForeground(Color.RED);
                        setText("-$ " + String.format(java.util.Locale.US, "%,.2f", val.abs()));
                    } else {
                        setForeground(new Color(39, 174, 96));
                        setText("+$ " + String.format(java.util.Locale.US, "%,.2f", val));
                    }
                } else {
                    super.setValue(value);
                }
            }
        };
        currencyRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        DefaultTableCellRenderer accumRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof BigDecimal) {
                    setText("$ " + String.format(java.util.Locale.US, "%,.2f", value));
                } else {
                    super.setValue(value);
                }
            }
        };
        accumRenderer.setHorizontalAlignment(JLabel.RIGHT);

        tablaMovimientos.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tablaMovimientos.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tablaMovimientos.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tablaMovimientos.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tablaMovimientos.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
        tablaMovimientos.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tablaMovimientos.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        tablaMovimientos.getColumnModel().getColumn(7).setCellRenderer(accumRenderer);
    }

    private void cargarUsuarios() {
        cbUsuario.removeAllItems();
        cbUsuario.addItem("TODOS");
        ventaDao.ConsultarUsuario(cbUsuario);
    }

    private LocalDate getLocalDate(JDateChooser jd) {
        if (jd.getDate() == null) return null;
        return jd.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // --- FILTRAR DATOS ---
    private void ejecutarFiltro() {
        LocalDate desde = getLocalDate(jdDesde);
        LocalDate hasta = getLocalDate(jdHasta);
        String tipo = cbTipoPago.getSelectedItem().toString();
        String usuario = cbUsuario.getSelectedItem().toString();
        String desc = txtBuscar.getText().trim();

        if (desde == null || hasta == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un rango de fechas válido");
            return;
        }

        modelMovimientos.setRowCount(0);

        new Thread(() -> {
            cargarDatosCaja(desde, hasta, tipo, usuario, desc);
        }).start();
    }

    private void cargarDatosCaja(LocalDate desde, LocalDate hasta, String tipo, String usuario, String desc) {
        List<Object[]> filas = new ArrayList<>();
        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal egresos = BigDecimal.ZERO;

        StringBuilder sql = new StringBuilder(
            "SELECT cm.id, cm.fecha, cm.hora, cm.tipo, cm.monto, cm.descripcion, cm.usuario, " +
            "SUM(CASE WHEN cm.tipo IN ('EFECTIVO', 'TRANSFERENCIA', 'CREDITO', 'ENTRADA') THEN cm.monto ELSE -ABS(cm.monto) END) " +
            "OVER (ORDER BY cm.fecha, cm.hora, cm.id) AS acumulado " +
            "FROM caja_movimientos cm " +
            "WHERE cm.fecha BETWEEN ? AND ? "
        );

        if (!"TODOS".equals(tipo)) {
            sql.append("AND cm.tipo = ? ");
        }
        if (!"TODOS".equals(usuario)) {
            sql.append("AND cm.usuario = ? ");
        }
        if (!desc.isEmpty()) {
            sql.append("AND LOWER(cm.descripcion) LIKE ? ");
        }
        sql.append("ORDER BY cm.fecha ASC, cm.hora ASC, cm.id ASC");

        try (Connection con = cn.conectar(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setDate(idx++, java.sql.Date.valueOf(desde));
            ps.setDate(idx++, java.sql.Date.valueOf(hasta));

            if (!"TODOS".equals(tipo)) {
                ps.setString(idx++, tipo);
            }
            if (!"TODOS".equals(usuario)) {
                ps.setString(idx++, usuario);
            }
            if (!desc.isEmpty()) {
                ps.setString(idx++, "%" + desc.toLowerCase() + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Date fecha = rs.getDate("fecha");
                    Date hora = rs.getTime("hora");
                    String tMov = rs.getString("tipo");
                    BigDecimal monto = rs.getBigDecimal("monto");
                    String descripcion = rs.getString("descripcion");
                    String user = rs.getString("usuario");
                    BigDecimal acumulado = rs.getBigDecimal("acumulado");

                    filas.add(new Object[]{id, fecha, hora, tMov, monto, descripcion, user, acumulado});

                    // Calcular KPI
                    if (tMov.equalsIgnoreCase("EFECTIVO") || tMov.equalsIgnoreCase("TRANSFERENCIA") || 
                        tMov.equalsIgnoreCase("CREDITO") || tMov.equalsIgnoreCase("ENTRADA")) {
                        ingresos = ingresos.add(monto);
                    } else {
                        egresos = egresos.add(monto.abs());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final BigDecimal ing = ingresos;
        final BigDecimal egr = egresos;
        final BigDecimal bal = ingresos.subtract(egresos);

        SwingUtilities.invokeLater(() -> {
            for (Object[] fila : filas) {
                modelMovimientos.addRow(fila);
            }
            lblTotalIngresos.setText("<html><div style='text-align:center;'><span style='font-size:15px; color:#27ae60;'><b>+$ " + String.format(java.util.Locale.US, "%,.2f", ing) + "</b></span></div></html>");
            lblTotalEgresos.setText("<html><div style='text-align:center;'><span style='font-size:15px; color:#c0392b;'><b>-$ " + String.format(java.util.Locale.US, "%,.2f", egr) + "</b></span></div></html>");
            lblBalanceNeto.setText("<html><div style='text-align:center;'><span style='font-size:15px; color:#1a5276;'><b>" + (bal.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "-") + "$ " + String.format(java.util.Locale.US, "%,.2f", bal.abs()) + "</b></span></div></html>");
        });
    }

    // --- EXPORTACIÓN ---
    private void exportarExcel() {
        if (tablaMovimientos.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Auditoria_Movimientos_Caja.xlsx"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("Movimientos");
                
                Row titleRow = sheet.createRow(0);
                titleRow.createCell(0).setCellValue("Auditoría Histórica de Caja - Joyería Pietras");

                Row filterRow = sheet.createRow(1);
                filterRow.createCell(0).setCellValue("Desde: " + jdDesde.getDate() + " | Hasta: " + jdHasta.getDate() + " | Filtro Tipo: " + cbTipoPago.getSelectedItem());

                Row headerRow = sheet.createRow(3);
                for (int i = 0; i < tablaMovimientos.getColumnCount(); i++) {
                    headerRow.createCell(i).setCellValue(tablaMovimientos.getColumnName(i));
                }

                for (int r = 0; r < tablaMovimientos.getRowCount(); r++) {
                    Row row = sheet.createRow(r + 4);
                    for (int c = 0; c < tablaMovimientos.getColumnCount(); c++) {
                        Object val = tablaMovimientos.getValueAt(r, c);
                        if (val instanceof BigDecimal) {
                            row.createCell(c).setCellValue(((BigDecimal) val).doubleValue());
                        } else {
                            row.createCell(c).setCellValue(val != null ? val.toString() : "");
                        }
                    }
                }

                try (FileOutputStream out = new FileOutputStream(chooser.getSelectedFile())) {
                    wb.write(out);
                    JOptionPane.showMessageDialog(this, "¡Archivo Excel guardado con éxito!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al generar el Excel: " + e.getMessage());
            }
        }
    }

    private void exportarPDF() {
        if (tablaMovimientos.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Auditoria_Caja.pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Document doc = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(doc, new FileOutputStream(chooser.getSelectedFile()));
                doc.open();

                com.itextpdf.text.Font fTitle = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                Paragraph p = new Paragraph("Auditoría de Movimientos e Historial de Caja", fTitle);
                p.setAlignment(Paragraph.ALIGN_CENTER);
                p.setSpacingAfter(10);
                doc.add(p);

                Paragraph pFechas = new Paragraph("Filtros: Desde " + jdDesde.getDate() + " hasta " + jdHasta.getDate() + " | Tipo: " + cbTipoPago.getSelectedItem());
                pFechas.setAlignment(Paragraph.ALIGN_CENTER);
                pFechas.setSpacingAfter(25);
                doc.add(pFechas);

                PdfPTable pdfTable = new PdfPTable(tablaMovimientos.getColumnCount());
                pdfTable.setWidthPercentage(100);

                // Encabezados
                for (int i = 0; i < tablaMovimientos.getColumnCount(); i++) {
                    PdfPCell cell = new PdfPCell(new Phrase(tablaMovimientos.getColumnName(i)));
                    cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                    cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                    pdfTable.addCell(cell);
                }

                // Celdas
                for (int f = 0; f < tablaMovimientos.getRowCount(); f++) {
                    for (int c = 0; c < tablaMovimientos.getColumnCount(); c++) {
                        Object valor = tablaMovimientos.getValueAt(f, c);
                        String strVal;
                        if (valor instanceof BigDecimal) {
                            BigDecimal val = (BigDecimal) valor;
                            strVal = (c == 4 && val.compareTo(BigDecimal.ZERO) < 0 ? "-" : "") + "$ " + String.format(java.util.Locale.US, "%,.2f", val.abs());
                        } else {
                            strVal = (valor != null ? valor.toString() : "");
                        }

                        PdfPCell cell = new PdfPCell(new Phrase(strVal));
                        if (c == 4 || c == 7) cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                        else cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        pdfTable.addCell(cell);
                    }
                }

                doc.add(pdfTable);
                doc.close();
                JOptionPane.showMessageDialog(this, "¡Archivo PDF guardado con éxito!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al generar el PDF: " + e.getMessage());
            }
        }
    }
}
