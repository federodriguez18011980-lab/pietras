package Controlador;

import Modelo.MovimientoDao;
import Modelo.MovimientoStock;
import Modelo.Productos; // Asumo que tenés tu modelo Producto
import Modelo.ProductosDao; // Para traer los datos fijos del prod
import Vista.VistaEstadisticasArticulo;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class EstadisticasController implements KeyListener {

    private VistaEstadisticasArticulo vista;
    private MovimientoDao movDao;
    private ProductosDao prodDao;
    private Productos pro;
    private BigDecimal cinco;
    DefaultTableModel modelo = new DefaultTableModel();

    public EstadisticasController(VistaEstadisticasArticulo vista, MovimientoDao movDao, ProductosDao prodDao) {
        this.vista = vista;
        this.movDao = movDao;
        this.prodDao = prodDao;
        
        // Escuchar el buscador por teclado
        this.vista.txtBusquedaArticulo.addKeyListener(this);
        
        // Listener para el botón filtrar
        this.vista.btnFiltrar.addActionListener(e -> {
            String texto = vista.txtBusquedaArticulo.getText();
            if (!texto.isEmpty()) {
                cargarAnalisis(texto);
            }
        });
        
        //--- FILTRO DEL COMBOBOX ---
        this.vista.cbTipoFiltro.addActionListener(e -> {
        String texto = vista.txtBusquedaArticulo.getText();
        if (!texto.isEmpty()) cargarAnalisis(texto);
        });
    }

 private void cargarAnalisis(String criterio) {
        // 1. Buscamos los datos "fijos" del producto (Stock Real y Nombre)
        Productos pro = prodDao.buscarProducto(criterio);
        
        if (pro.getDescripcion() != null) {
            // Actualizamos etiquetas de la vista
            vista.lblNombreProducto.setText(pro.getDescripcion());
            vista.lblCodigoProducto.setText("Cód: " + pro.getCodigo());
            vista.lblStockValor.setText(String.valueOf(pro.getStock()));
            
            // Color de stock crítico
           cinco = new BigDecimal(5);
            if (pro.getStock().compareTo(cinco)== -1) {
                vista.lblStockValor.setForeground(Color.RED);
            } else {
                vista.lblStockValor.setForeground(new Color(184, 139, 74)); // Cobrizo
            }
            
            // CAPTURAMOS LAS FECHAS DE LA VISTA
            java.util.Date fechaInicio = vista.jdDesde.getDate();
            java.util.Date fechaFin = vista.jdHasta.getDate();
            String tipoSeleccionado = vista.cbTipoFiltro.getSelectedItem().toString();
            

            // 2. Buscamos el historial de movimientos
            // LLAMAMOS AL DAO CON LAS FECHAS
            List<MovimientoStock> historial = movDao.consultarHistorial(criterio, fechaInicio, fechaFin);
            //  List<MovimientoStock> historial = movDao.consultarHistorial(criterio);
            
            modelo = (DefaultTableModel) vista.tableHistorial.getModel();
            modelo.setRowCount(0);
            double tVentas = 0, tEntradas = 0, tAjustesEntrada = 0, tAjustesSalida = 0;
            
            for (MovimientoStock m : historial) {
                // FILTRO LÓGICO: Si es "TODOS" o coincide con el motivo
                if (tipoSeleccionado.equals("TODOS") || m.getMotivo().equals(tipoSeleccionado)) {
                    Object[] fila = {m.getFecha(), m.getTipoMovimiento(), m.getCantidad(), m.getMotivo()};
                    modelo.addRow(fila);

                    // Sumar para los totales del Panel Sur
                    if (m.getMotivo().equals("VENTA")) {
                        tVentas += m.getCantidad();
                    } else if (m.getMotivo().equals("ENTRADA") || m.getMotivo().equals("CARGA INICIAL")) {
                        tEntradas += m.getCantidad();
                    } else if (m.getMotivo().equals("AJUSTE DE STOCK")) {
                        
                        
                        if(m.getTipoMovimiento().equalsIgnoreCase("ENTRADA")){
                            
                            //SUMO AJUSTESENTRADAS
                            tAjustesEntrada += m.getCantidad();
                        
                        }else{
                        
                            //SUMO AJUSTESSALIDAS
                            tAjustesSalida += m.getCantidad();
                        }
                        
                        
                        
                        
                    }
                }
            }

            // Actualizar etiquetas del Panel Sur
            vista.lblTotalVentas.setText("Ventas: " + tVentas);
            vista.lblTotalEntradas.setText("Ingresos: " + tEntradas);
            vista.lblTotalAjustesE.setText("Ajustes Entradas: " + tAjustesEntrada);
            vista.lblTotalAjusteS.setText("Ajustes Salidas: " + tAjustesSalida);

            // Actualizar el KPI central de ventas con el total filtrado
            vista.lblVentasValor.setText(String.valueOf(tVentas));
            
            double totalVendido = 0;
//            for (MovimientoStock m : historial) {
//                Object[] fila = {
//                    m.getFecha(),
//                    m.getTipoMovimiento(),
//                    m.getCantidad(),
//                    m.getMotivo()
//                };
//                modelo.addRow(fila);
//                
//                if(m.getMotivo().equals("VENTA")) {
//                    totalVendido += m.getCantidad();
//                }
//            }
//            vista.lblVentasValor.setText(String.valueOf(totalVendido));
            
            if (!historial.isEmpty()) {
                vista.lblUltimaFecha.setText(historial.get(0).getFecha().toString());
            }
        } else {
            // Si no encuentra nada, limpiamos etiquetas
            vista.lblNombreProducto.setText("Producto no encontrado");
            vista.lblStockValor.setText("0");
            modelo.setRowCount(0);
        }
    }

  
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getSource() == vista.txtBusquedaArticulo) {
            String texto = vista.txtBusquedaArticulo.getText();
            if (texto.length() > 2) { // Empezar a buscar después de 3 letras
                cargarAnalisis(texto);
            }
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {}
}
