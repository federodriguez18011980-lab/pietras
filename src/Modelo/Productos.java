package Modelo;

import java.math.BigDecimal;

public class Productos {
    private int id;
    private String codigo;
    private String descripcion;
    private BigDecimal precioCompra; 
    private BigDecimal precio;
    private BigDecimal precioxdolar;
    private BigDecimal stock;
    private String proveedor;
    private Integer idCategoria;
    private String categoriaNombre;
    private String material; // Nuevo campo clave
    private int idWooCommerce;
    private String piedra;
    private Boolean web; 

    public Productos() {}

    // Constructor para Listados y Carga
    public Productos(int id, String codigo, String descripcion, BigDecimal precioCompra, 
                     BigDecimal precio, BigDecimal stock, String proveedor, 
                     Integer idCategoria, String categoriaNombre, String material) {
        this.id = id;
        this.codigo = codigo;this.proveedor = proveedor;
        this.idCategoria = idCategoria;
        this.categoriaNombre = categoriaNombre;
        this.material = material;
    }

    public Boolean getWeb() {
        return web;
    }

    public void setWeb(Boolean web) {
        this.web = web;
    }
    
    
    

    public BigDecimal getPrecioxdolar() {
        return precioxdolar;
    }

    public void setPrecioxdolar(BigDecimal precioxdolar) {
        this.precioxdolar = precioxdolar;
    }
    
    
    
    public String getPiedra() { 
        return piedra; 
    }
    public void setPiedra(String piedra) {
        
        this.piedra = piedra; 
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }
    
    
    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getIdWooCommerce() {
        return idWooCommerce;
    }

    public void setIdWooCommerce(int idWooCommerce) {
        this.idWooCommerce = idWooCommerce;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
    
    /*Desde aca en adelante las categorias, talles, colores y precioCompra*/
    
    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }


    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    
}
