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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstadisticasVentasView extends JFrame {

    private JDateChooser jdDesde, jdHasta;
    private JComboBox<Item> cbCategoria;
    private JTable tablaRotacion;
    private DefaultTableModel modelRotacion;
    
    // KPI Labels
    private JLabel lblMasVendido, lblMenosVendido, lblTotalFacturado;
    
    // Gráfico de Barras
    private GraficoPanel panelGrafico;

    // Servicios / DAOs
    private final CategoriaDao catDao = new CategoriaDao();
    private final ConexionMysql cn = new ConexionMysql();

    // Colores Pietras Premium
    private final Color colorPietras = new Color(244, 119, 21); // Naranja Pietras
    private final Color colorCobrizo = new Color(184, 139, 74); // Marrón Cobrizo
    private final Color colorGrisFondo = new Color(245, 246, 250);
    private final Color colorKPIFondo = new Color(253, 242, 233); // Naranja ultra claro para tarjetas
    private final Color colorAzulHeader = new Color(44, 62, 80);

    public EstadisticasVentasView() {
        setTitle("Pietras - Estadísticas y Rotación de Inventario");
        setSize(1200, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initComponents();
        cargarCategorias();
        ejecutarFiltro(); // Carga datos por defecto
    }

    private void initComponents() {
        // --- 1. PANEL SUPERIOR: FILTROS ---
        JPanel pnlFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pnlFiltros.setBackground(colorPietras);
        pnlFiltros.setBorder(new EmptyBorder(5, 15, 5, 15));

        JLabel lblDesde = new JLabel("Desde:");
        lblDesde.setForeground(Color.WHITE);
        lblDesde.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        jdDesde = new JDateChooser();
        jdDesde.setPreferredSize(new Dimension(130, 28));
        jdDesde.setDate(new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)); // Hace 30 días

        JLabel lblHasta = new JLabel("Hasta:");
        lblHasta.setForeground(Color.WHITE);
        lblHasta.setFont(new Font("SansSerif", Font.BOLD, 12));

        jdHasta = new JDateChooser();
        jdHasta.setPreferredSize(new Dimension(130, 28));
        jdHasta.setDate(new Date()); // Hoy

        JLabel lblCat = new JLabel("Categoría:");
        lblCat.setForeground(Color.WHITE);
        lblCat.setFont(new Font("SansSerif", Font.BOLD, 12));

        cbCategoria = new JComboBox<>();
        cbCategoria.setPreferredSize(new Dimension(160, 28));

        JButton btnFiltrar = new JButton("FILTRAR DATOS");
        estiloBoton(btnFiltrar, new Color(44, 62, 80)); // Gris/Azul oscuro
        btnFiltrar.addActionListener(e -> ejecutarFiltro());

        pnlFiltros.add(lblDesde);
        pnlFiltros.add(jdDesde);
        pnlFiltros.add(lblHasta);
        pnlFiltros.add(jdHasta);
        pnlFiltros.add(lblCat);
        pnlFiltros.add(cbCategoria);
        pnlFiltros.add(btnFiltrar);

        add(pnlFiltros, BorderLayout.NORTH);

        // --- 2. CUERPO PRINCIPAL ---
        JPanel pnlCuerpo = new JPanel(new BorderLayout(15, 15));
        pnlCuerpo.setBackground(colorGrisFondo);
        pnlCuerpo.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Subpanel Superior: Tarjetas KPI
        JPanel pnlKPIs = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlKPIs.setOpaque(false);
        pnlKPIs.setPreferredSize(new Dimension(0, 110));

        lblMasVendido = new JLabel("<html><b>CARGANDO...</b></html>", JLabel.CENTER);
        lblMenosVendido = new JLabel("<html><b>CARGANDO...</b></html>", JLabel.CENTER);
        lblTotalFacturado = new JLabel("<html><b>CARGANDO...</b></html>", JLabel.CENTER);

        pnlKPIs.add(crearKPICard("PRODUCTO MÁS VENDIDO 🏆", lblMasVendido, colorCobrizo));
        pnlKPIs.add(crearKPICard("MENOR ROTACIÓN / SIN VENTAS 📉", lblMenosVendido, new Color(231, 76, 60)));
        pnlKPIs.add(crearKPICard("FACTURACIÓN DEL PERÍODO 💰", lblTotalFacturado, colorPietras));

        pnlCuerpo.add(pnlKPIs, BorderLayout.NORTH);

        // Subpanel Central: Gráfico + Tabla
        JPanel pnlVisual = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlVisual.setOpaque(false);

        // Izquierda: Gráfico
        panelGrafico = new GraficoPanel();
        panelGrafico.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                " Top 5 Artículos Más Vendidos ",
                0, 0, new Font("SansSerif", Font.BOLD, 13), colorAzulHeader));
        pnlVisual.add(panelGrafico);

        // Derecha: Tabla Top 20 Rotación
        JPanel pnlTablaContainer = new JPanel(new BorderLayout());
        pnlTablaContainer.setBackground(Color.WHITE);
        pnlTablaContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                " Artículos con Mayor Rotación (Top 20) ",
                0, 0, new Font("SansSerif", Font.BOLD, 13), colorAzulHeader));

        modelRotacion = new DefaultTableModel(new String[]{"Código", "Descripción", "Categoría", "Cant. Vendida", "Recaudado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaRotacion = new JTable(modelRotacion);
        configurarTabla();
        pnlTablaContainer.add(new JScrollPane(tablaRotacion), BorderLayout.CENTER);

        pnlVisual.add(pnlTablaContainer);
        pnlCuerpo.add(pnlVisual, BorderLayout.CENTER);

        add(pnlCuerpo, BorderLayout.CENTER);

        // --- 3. PANEL SUR: BOTONES DE EXPORTACIÓN ---
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlSur.setBackground(Color.WHITE);
        pnlSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton btnPdf = new JButton("Exportar Reporte (PDF)");
        estiloBoton(btnPdf, new Color(192, 57, 43)); // Rojo PDF
        btnPdf.addActionListener(e -> exportarPDF());

        JButton btnExcel = new JButton("Exportar Datos (Excel)");
        estiloBoton(btnExcel, new Color(30, 132, 73)); // Verde Excel
        btnExcel.addActionListener(e -> exportarExcel());

        pnlSur.add(btnPdf);
        pnlSur.add(btnExcel);

        add(pnlSur, BorderLayout.SOUTH);
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(colorBorde);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        card.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblTitulo = new JLabel(titulo, JLabel.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblTitulo.setForeground(colorBorde);

        lblValor.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblValor.setForeground(colorAzulHeader);

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);

        return card;
    }

    private void configurarTabla() {
        tablaRotacion.setRowHeight(28);
        tablaRotacion.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tablaRotacion.getTableHeader().setBackground(new Color(236, 240, 241));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof BigDecimal) {
                    setText("$ " + String.format(java.util.Locale.US, "%,.2f", value));
                } else {
                    super.setValue(value);
                }
            }
        };
        currencyRenderer.setHorizontalAlignment(JLabel.RIGHT);

        tablaRotacion.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tablaRotacion.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tablaRotacion.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tablaRotacion.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tablaRotacion.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
    }

    private void cargarCategorias() {
        cbCategoria.removeAllItems();
        cbCategoria.addItem(new Item(0, "Todas las Categorías"));
        for (Categoria c : catDao.listar()) {
            cbCategoria.addItem(new Item(c.getId(), c.getNombre()));
        }
    }

    private LocalDate getLocalDate(JDateChooser jd) {
        if (jd.getDate() == null) return null;
        return jd.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // --- CARGA DE ESTADÍSTICAS ---
    private void ejecutarFiltro() {
        LocalDate desde = getLocalDate(jdDesde);
        LocalDate hasta = getLocalDate(jdHasta);
        Item cat = (Item) cbCategoria.getSelectedItem();
        int idCat = (cat != null) ? cat.getId() : 0;

        if (desde == null || hasta == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un rango de fechas válido");
            return;
        }

        modelRotacion.setRowCount(0);

        // Hilo de consulta para no bloquear
        new Thread(() -> {
            cargarTarjetasKPI(desde, hasta, idCat);
            cargarTablaRotacionYGrafico(desde, hasta, idCat);
        }).start();
    }

    private void cargarTarjetasKPI(LocalDate desde, LocalDate hasta, int idCat) {
        BigDecimal totalFacturado = BigDecimal.ZERO;
        String masVendido = "N/A";
        double masVendidoCant = 0;
        String menosVendido = "N/A";
        double menosVendidoCant = 0;

        // 1. Facturación Total
        String sqlFacturacion = "SELECT SUM(total) FROM ventas WHERE estado = 'ACTIVA' AND fecha BETWEEN ? AND ?";
        try (Connection con = cn.conectar(); PreparedStatement ps = con.prepareStatement(sqlFacturacion)) {
            ps.setDate(1, java.sql.Date.valueOf(desde));
            ps.setDate(2, java.sql.Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal sum = rs.getBigDecimal(1);
                    if (sum != null) totalFacturado = sum;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. Producto más vendido (Top 1)
        StringBuilder sqlMasVendido = new StringBuilder(
            "SELECT p.descripcion, SUM(d.cantidad) AS cant " +
            "FROM detalles d " +
            "INNER JOIN ventas v ON d.id_venta = v.id " +
            "INNER JOIN productos p ON d.cod_producto = p.codigo " +
            "WHERE v.estado = 'ACTIVA' AND v.fecha BETWEEN ? AND ? "
        );
        if (idCat > 0) sqlMasVendido.append("AND p.id_categoria = ? ");
        sqlMasVendido.append("GROUP BY p.codigo, p.descripcion ORDER BY cant DESC LIMIT 1");

        try (Connection con = cn.conectar(); PreparedStatement ps = con.prepareStatement(sqlMasVendido.toString())) {
            ps.setDate(1, java.sql.Date.valueOf(desde));
            ps.setDate(2, java.sql.Date.valueOf(hasta));
            if (idCat > 0) ps.setInt(3, idCat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    masVendido = rs.getString("descripcion");
                    masVendidoCant = rs.getDouble("cant");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Producto menos vendido (pero con al menos una venta)
        StringBuilder sqlMenosVendido = new StringBuilder(
            "SELECT p.descripcion, SUM(d.cantidad) AS cant " +
            "FROM detalles d " +
            "INNER JOIN ventas v ON d.id_venta = v.id " +
            "INNER JOIN productos p ON d.cod_producto = p.codigo " +
            "WHERE v.estado = 'ACTIVA' AND v.fecha BETWEEN ? AND ? "
        );
        if (idCat > 0) sqlMenosVendido.append("AND p.id_categoria = ? ");
        sqlMenosVendido.append("GROUP BY p.codigo, p.descripcion ORDER BY cant ASC LIMIT 1");

        try (Connection con = cn.conectar(); PreparedStatement ps = con.prepareStatement(sqlMenosVendido.toString())) {
            ps.setDate(1, java.sql.Date.valueOf(desde));
            ps.setDate(2, java.sql.Date.valueOf(hasta));
            if (idCat > 0) ps.setInt(3, idCat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    menosVendido = rs.getString("descripcion");
                    menosVendidoCant = rs.getDouble("cant");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final BigDecimal fact = totalFacturado;
        final String mvDesc = masVendido;
        final double mvCant = masVendidoCant;
        final String mnvDesc = menosVendido;
        final double mnvCant = menosVendidoCant;

        SwingUtilities.invokeLater(() -> {
            lblTotalFacturado.setText("<html><div style='text-align:center;'><span style='font-size:16px;'><b>$ " + String.format(java.util.Locale.US, "%,.2f", fact) + "</b></span><br><span style='font-size:10px; color:#7f8c8d;'>Ventas Netas Activas</span></div></html>");
            lblMasVendido.setText("<html><div style='text-align:center;'><span style='font-size:12px;'><b>" + mvDesc + "</b></span><br><span style='font-size:11px; color:#27ae60;'><b>" + mvCant + " unidades</b></span></div></html>");
            lblMenosVendido.setText("<html><div style='text-align:center;'><span style='font-size:12px;'><b>" + mnvDesc + "</b></span><br><span style='font-size:11px; color:#c0392b;'><b>" + mnvCant + " unidades</b></span></div></html>");
        });
    }

    private void cargarTablaRotacionYGrafico(LocalDate desde, LocalDate hasta, int idCat) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.codigo, p.descripcion, c.nombre AS categoria, SUM(d.cantidad) AS total_vendido, SUM(d.cantidad * d.precio) AS total_recaudado " +
            "FROM detalles d " +
            "INNER JOIN ventas v ON d.id_venta = v.id " +
            "INNER JOIN productos p ON d.cod_producto = p.codigo " +
            "LEFT JOIN categorias c ON p.id_categoria = c.id " +
            "WHERE v.estado = 'ACTIVA' AND v.fecha BETWEEN ? AND ? "
        );
        if (idCat > 0) sql.append("AND p.id_categoria = ? ");
        sql.append("GROUP BY p.codigo, p.descripcion, c.nombre ORDER BY total_vendido DESC LIMIT 20");

        List<Object[]> datosTabla = new ArrayList<>();
        List<String> nombresGrafico = new ArrayList<>();
        List<Double> valoresGrafico = new ArrayList<>();

        try (Connection con = cn.conectar(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setDate(1, java.sql.Date.valueOf(desde));
            ps.setDate(2, java.sql.Date.valueOf(hasta));
            if (idCat > 0) ps.setInt(3, idCat);
            
            try (ResultSet rs = ps.executeQuery()) {
                int index = 0;
                while (rs.next()) {
                    String cod = rs.getString("codigo");
                    String desc = rs.getString("descripcion");
                    String cat = rs.getString("categoria");
                    BigDecimal cant = rs.getBigDecimal("total_vendido");
                    BigDecimal rec = rs.getBigDecimal("total_recaudado");

                    datosTabla.add(new Object[]{cod, desc, cat, cant, rec});

                    // Guardamos el Top 5 para el gráfico
                    if (index < 5) {
                        nombresGrafico.add(desc.length() > 15 ? desc.substring(0, 12) + "..." : desc);
                        valoresGrafico.add(cant.doubleValue());
                    }
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            for (Object[] fila : datosTabla) {
                modelRotacion.addRow(fila);
            }
            panelGrafico.setDatos(nombresGrafico, valoresGrafico);
        });
    }

    // --- EXPORTACIÓN ---
    private void exportarExcel() {
        if (tablaRotacion.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Top20_Rotacion_Articulos.xlsx"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet hoja = wb.createSheet("Top Rotación");
                
                // Título
                Row filaTitulo = hoja.createRow(0);
                Cell cTitulo = filaTitulo.createCell(0);
                cTitulo.setCellValue("Reporte de Rotación de Artículos - Joyería Pietras");
                
                // Rango Fechas
                Row filaRango = hoja.createRow(1);
                filaRango.createCell(0).setCellValue("Desde: " + jdDesde.getDate() + " | Hasta: " + jdHasta.getDate());

                // Encabezados
                Row encabezado = hoja.createRow(3);
                for (int i = 0; i < tablaRotacion.getColumnCount(); i++) {
                    encabezado.createCell(i).setCellValue(tablaRotacion.getColumnName(i));
                }

                // Datos
                for (int r = 0; r < tablaRotacion.getRowCount(); r++) {
                    Row fila = hoja.createRow(r + 4);
                    for (int c = 0; c < tablaRotacion.getColumnCount(); c++) {
                        Object v = tablaRotacion.getValueAt(r, c);
                        if (v instanceof BigDecimal) {
                            fila.createCell(c).setCellValue(((BigDecimal) v).doubleValue());
                        } else {
                            fila.createCell(c).setCellValue(v != null ? v.toString() : "");
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
        if (tablaRotacion.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Reporte_Estadisticas_Ventas.pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Document doc = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(doc, new FileOutputStream(chooser.getSelectedFile()));
                doc.open();

                com.itextpdf.text.Font fTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 20, com.itextpdf.text.Font.BOLD);
                Paragraph p = new Paragraph("Reporte de Rotación de Artículos (Top 20)", fTitulo);
                p.setAlignment(Paragraph.ALIGN_CENTER);
                p.setSpacingAfter(10);
                doc.add(p);

                Paragraph pFechas = new Paragraph("Período: " + jdDesde.getDate().toString() + " hasta " + jdHasta.getDate().toString());
                pFechas.setAlignment(Paragraph.ALIGN_CENTER);
                pFechas.setSpacingAfter(25);
                doc.add(pFechas);

                PdfPTable pdfTable = new PdfPTable(tablaRotacion.getColumnCount());
                pdfTable.setWidthPercentage(100);

                // Encabezados
                for (int i = 0; i < tablaRotacion.getColumnCount(); i++) {
                    PdfPCell cell = new PdfPCell(new Phrase(tablaRotacion.getColumnName(i)));
                    cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                    cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                    pdfTable.addCell(cell);
                }

                // Celdas
                for (int f = 0; f < tablaRotacion.getRowCount(); f++) {
                    for (int c = 0; c < tablaRotacion.getColumnCount(); c++) {
                        Object valor = tablaRotacion.getValueAt(f, c);
                        String strVal = (valor instanceof BigDecimal) ? "$ " + String.format(java.util.Locale.US, "%,.2f", valor) : (valor != null ? valor.toString() : "");
                        PdfPCell cell = new PdfPCell(new Phrase(strVal));
                        if (c == 4) cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
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

    // ==========================================================
    // PANEL DEL GRÁFICO PERSONALIZADO CON DIBUJO DENTRO DE SWING
    // ==========================================================
    private class GraficoPanel extends JPanel {
        private List<String> nombres = new ArrayList<>();
        private List<Double> valores = new ArrayList<>();
        private int hoveredIdx = -1;
        private Point mousePoint = null;

        public GraficoPanel() {
            setBackground(Color.WHITE);
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mousePoint = e.getPoint();
                    int idx = detectarBarra(e.getX(), e.getY());
                    if (idx != hoveredIdx) {
                        hoveredIdx = idx;
                        if (hoveredIdx != -1) {
                            setCursor(new Cursor(Cursor.HAND_CURSOR));
                        } else {
                            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                        repaint();
                    }
                }
            });
        }

        public void setDatos(List<String> nombres, List<Double> valores) {
            this.nombres = nombres;
            this.valores = valores;
            this.hoveredIdx = -1;
            repaint();
        }

        private int detectarBarra(int mx, int my) {
            if (valores.isEmpty()) return -1;
            
            int width = getWidth();
            int height = getHeight();
            int margin = 50;
            int xStart = margin + 10;
            int graphWidth = width - 2 * margin - 10;
            int graphHeight = height - 2 * margin;

            double maxVal = 0;
            for (double v : valores) {
                if (v > maxVal) maxVal = v;
            }
            if (maxVal == 0) maxVal = 1;

            int barWidth = graphWidth / valores.size() - 20;

            for (int i = 0; i < valores.size(); i++) {
                int x = xStart + i * (graphWidth / valores.size()) + 10;
                int barHeight = (int) ((valores.get(i) / maxVal) * (graphHeight - 20));
                int y = height - margin - barHeight;

                if (mx >= x && mx <= x + barWidth && my >= y && my <= height - margin) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int margin = 50;

            // Dibujar rejilla / Ejes
            g2.setColor(new Color(230, 230, 230));
            g2.setStroke(new BasicStroke(1));
            // Rejilla horizontal (4 líneas de referencia)
            int graphHeight = height - 2 * margin;
            for (int i = 0; i <= 4; i++) {
                int y = height - margin - (i * graphHeight / 4);
                g2.drawLine(margin, y, width - margin, y);
            }

            g2.setColor(colorAzulHeader);
            g2.setStroke(new BasicStroke(2));
            // Eje X
            g2.drawLine(margin, height - margin, width - margin, height - margin);
            // Eje Y
            g2.drawLine(margin, margin, margin, height - margin);

            if (valores.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
                g2.setColor(Color.GRAY);
                g2.drawString("No hay datos de ventas en este período.", width / 2 - 120, height / 2);
                g2.dispose();
                return;
            }

            double maxVal = 0;
            for (double v : valores) {
                if (v > maxVal) maxVal = v;
            }
            if (maxVal == 0) maxVal = 1;

            int graphWidth = width - 2 * margin - 10;
            int xStart = margin + 10;
            int barWidth = graphWidth / valores.size() - 20;

            // Dibujar Barras
            for (int i = 0; i < valores.size(); i++) {
                int x = xStart + i * (graphWidth / valores.size()) + 10;
                int barHeight = (int) ((valores.get(i) / maxVal) * (graphHeight - 20));
                int y = height - margin - barHeight;

                // Degradado
                GradientPaint gp;
                if (i == hoveredIdx) {
                    gp = new GradientPaint(x, y, colorPietras, x, height - margin, colorPietras.brighter());
                } else {
                    gp = new GradientPaint(x, y, colorCobrizo, x, height - margin, colorCobrizo.brighter());
                }
                
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

                // Bordes
                g2.setColor(i == hoveredIdx ? colorPietras.darker() : colorCobrizo.darker());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, y, barWidth - 1, barHeight - 1, 8, 8);

                // Dibujar Cantidad sobre la barra
                g2.setColor(colorAzulHeader);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                String cantStr = String.valueOf(valores.get(i).intValue());
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (barWidth - fm.stringWidth(cantStr)) / 2;
                g2.drawString(cantStr, tx, y - 5);

                // Dibujar etiquetas del Eje X (con rotación leve si es larga)
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                String tag = nombres.get(i);
                
                // Rotar texto 15 grados para que no se superpongan
                Graphics2D g2t = (Graphics2D) g2.create();
                int labelX = x + barWidth / 2;
                int labelY = height - margin + 18;
                g2t.translate(labelX, labelY);
                g2t.rotate(Math.toRadians(12));
                FontMetrics fmTag = g2t.getFontMetrics();
                g2t.drawString(tag, -fmTag.stringWidth(tag) / 2, 0);
                g2t.dispose();
            }

            // Dibujar Tooltip Flotante si hay hover
            if (hoveredIdx != -1 && mousePoint != null) {
                String desc = nombres.get(hoveredIdx);
                String valor = String.valueOf(valores.get(hoveredIdx).intValue()) + " vendidos";
                
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int wTooltip = Math.max(fm.stringWidth(desc), fm.stringWidth(valor)) + 20;
                int hTooltip = 42;

                int tx = mousePoint.x + 15;
                int ty = mousePoint.y - 15;

                // Evitar que el tooltip se salga de la pantalla
                if (tx + wTooltip > width) tx = mousePoint.x - wTooltip - 5;
                if (ty - hTooltip < 0) ty = mousePoint.y + 15;

                // Fondo
                g2.setColor(new Color(44, 62, 80, 240)); // Translúcido oscuro
                g2.fillRoundRect(tx, ty - hTooltip, wTooltip, hTooltip, 10, 10);
                
                g2.setColor(colorPietras);
                g2.drawRoundRect(tx, ty - hTooltip, wTooltip - 1, hTooltip - 1, 10, 10);

                // Textos
                g2.setColor(Color.WHITE);
                g2.drawString(desc, tx + 10, ty - 26);
                g2.setColor(new Color(241, 196, 15)); // Amarillo brillante
                g2.drawString(valor, tx + 10, ty - 10);
            }

            g2.dispose();
        }
    }
}
