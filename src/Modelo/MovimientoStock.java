package Modelo;

import java.sql.Timestamp;

public class MovimientoStock {
    private int id;
    private int idProducto;
    private String nombreProducto; // Para mostrar en la tabla sin hacer otro JOIN
    private String tipoMovimiento; // "ENTRADA" o "SALIDA"
    private double cantidad;
    private Timestamp fecha;
    private String motivo; // "VENTA", "COMPRA", "ANULACION", "AJUSTE"

    public MovimientoStock() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}