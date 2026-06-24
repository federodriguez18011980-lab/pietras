package Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import Conexion.ConexionMysql;
import java.math.RoundingMode;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class ProductosDao {

    Connection con;
    PreparedStatement ps;
    ResultSet rs;
    ConexionMysql cn = new ConexionMysql();
    
    
    // MÉTODO 1: VALIDACIÓN (Solo consulta, no inserta)
    
    public boolean existeCodigo(String codigo) {
        String sql = "SELECT COUNT(*) FROM productos WHERE codigo = ?";
        try (Connection conValidar = cn.conectar(); 
             PreparedStatement psCheck = conValidar.prepareStatement(sql)) {
            psCheck.setString(1, codigo);
            rs = psCheck.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error existeCodigo: " + e.getMessage());
        }
        return false;
    }
    
    // MÉTODO 2: CARGA (Recibe el objeto listo)
  public boolean RegistrarProductos(Productos pro) {
    // 1. Obtenemos el valor del dólar actual antes de insertar
    BigDecimal valorDolar = obtenerUltimoValorDolar();
    // 2. Calculamos el precio en pesos (Precio Dolar * Valor del Dolar hoy)
    BigDecimal precioEnPesos = pro.getPrecio().multiply(valorDolar).setScale(2, RoundingMode.HALF_UP);

    String sql = "INSERT INTO productos (codigo, descripcion, precio, stock, proveedor,"
            + " id_categoria, precio_compra, material, piedra, precioxdolar, apto_web) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    
    try {
        con = cn.conectar();
        ps = con.prepareStatement(sql);
        ps.setString(1, pro.getCodigo());
        ps.setString(2, pro.getDescripcion());
        ps.setBigDecimal(3, pro.getPrecio());
        ps.setBigDecimal(4, pro.getStock());
        ps.setString(5, pro.getProveedor());
        ps.setInt(6, pro.getIdCategoria());
        ps.setBigDecimal(7, pro.getPrecioCompra());
        ps.setString(8, pro.getMaterial());
        ps.setString(9, pro.getPiedra());
        ps.setBigDecimal(10, precioEnPesos);// <--- CARGA AUTOMÁTICA
        ps.setBoolean(11,pro.getWeb());

        ps.execute();
        return true;
    } catch (SQLException e) {
        System.err.println(e.getMessage());
        return false;
    }
}
    
    // Método auxiliar para recuperar el ID recién creado para el movimiento
    public int obtenerIdPorCodigo(String codigo) {
        String sql = "SELECT id FROM productos WHERE codigo = ?";
        try (Connection c = cn.conectar(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, codigo);
            rs = p.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return -1;
    }


    public List<Productos> ListarProductos() {
        List<Productos> lista = new ArrayList<>();

        String sql = "SELECT p.id, p.codigo, p.descripcion, p.stock, p.precio, "
                + "c.nombre AS categoria,"
                + "p.proveedor, p.precio_compra, p.material, p.piedra, p.precioxdolar, p.apto_web "
                + "FROM productos p "
                + "LEFT JOIN categorias c ON p.id_categoria = c.id "
                + "ORDER BY p.descripcion ASC";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Productos pro = new Productos();

                pro.setId(rs.getInt("id"));
                pro.setCodigo(rs.getString("codigo"));
                pro.setDescripcion(rs.getString("descripcion"));
                pro.setStock(rs.getBigDecimal("stock"));
                pro.setPrecio(rs.getBigDecimal("precio"));

                // NUEVOS: nombres de tablas relacionadas
                pro.setCategoriaNombre(rs.getString("categoria"));

                pro.setProveedor(rs.getString("proveedor"));
                pro.setPrecioCompra(rs.getBigDecimal("precio_compra"));
                pro.setMaterial(rs.getString("material"));
                pro.setPiedra(rs.getString("piedra"));
                pro.setPrecioxdolar(rs.getBigDecimal("precioxdolar"));
                pro.setWeb(rs.getBoolean("apto_web"));

                lista.add(pro);
            }
        } catch (SQLException e) {
            System.out.println("Error ListarProductos: " + e.getMessage());
        }

        return lista;
    }

    public Productos BuscarProducto(String cod) {
        Productos pro = new Productos();
        String sql = "SELECT * FROM productos WHERE codigo=? LIMIT 1";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, cod);
            rs = ps.executeQuery();

            if (rs.next()) {
                pro.setId(rs.getInt("id"));
                pro.setCodigo(rs.getString("codigo"));
                pro.setDescripcion(rs.getString("descripcion"));
                pro.setPrecio(rs.getBigDecimal("precio"));
                pro.setStock(rs.getBigDecimal("stock"));
                pro.setProveedor(rs.getString("proveedor"));
                pro.setIdCategoria(rs.getInt("id_categoria"));
                pro.setPrecioCompra(rs.getBigDecimal("precio_compra"));
                pro.setMaterial(rs.getString("material"));
                pro.setPiedra(rs.getString("piedra"));
                pro.setIdWooCommerce(rs.getInt("id_woocommerce"));
                pro.setPrecioxdolar(rs.getBigDecimal("precioxdolar"));
                pro.setWeb(rs.getBoolean("apto_web"));
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }

        return pro;
    }

    public boolean ActualizarProductos(Productos pro) {
        String sql = "UPDATE productos SET descripcion=?, stock=?, proveedor=? WHERE codigo=?";
        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, pro.getDescripcion());
            ps.setBigDecimal(2, pro.getPrecio());
            ps.setBigDecimal(3, pro.getStock());
            ps.setString(4, pro.getProveedor());
            ps.setString(5, pro.getCodigo());
            ps.setInt(6, pro.getIdCategoria());
            ps.setBigDecimal(9, pro.getPrecioCompra());
            ps.setBoolean(10,pro.getWeb());
            pro.setIdWooCommerce(rs.getInt("id_woocommerce"));

            ps.execute();
            return true;

        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public boolean EliminarProducto(String codigo) {
        String sql = "DELETE FROM productos WHERE codigo=?";
        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, codigo);
            ps.execute();
            return true;

        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public boolean ActualizarStock(BigDecimal nuevoStock, String codigo) {
        String sql = "UPDATE productos SET stock=? WHERE codigo=?";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setBigDecimal(1, nuevoStock);
            ps.setString(2, codigo);
            ps.execute();
            return true;

        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public Config BuscarDatos() {
        Config conf = new Config();
        String sql = "SELECT * FROM config";

        try {

            con = cn.conectar();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                conf.setId(rs.getInt("id"));
                conf.setNombre(rs.getString("nombre"));
                conf.setCuit(rs.getString("cuit"));
                conf.setTelefono(rs.getString("telefono"));
                conf.setDireccion(rs.getString("direccion"));
                conf.setRazon(rs.getString("razon"));
                conf.setEntrada(rs.getString("entrada"));
                conf.setSalida(rs.getString("salida"));
                conf.setEfectivo(rs.getBigDecimal("efectivo"));
                conf.setTransferencia(rs.getBigDecimal("transferencia"));
                conf.setCredito(rs.getBigDecimal("credito"));
            } else {
                JOptionPane.showMessageDialog(null, "Código no encontrado");
            }

        } catch (SQLException e) {
            System.out.println(e.toString());
        }

        return conf;
    }

    public List<Productos> buscarPorDescripcion(String descripcion) {
        List<Productos> lista = new ArrayList<>();
        if (descripcion == null) {
            descripcion = "";
        }

        String sql = "SELECT * FROM productos WHERE LOWER(descripcion) LIKE LOWER(?)";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, "%" + descripcion + "%");
            rs = ps.executeQuery();

            while (rs.next()) {
                Productos p = new Productos();
                p.setId(rs.getInt("id"));
                p.setCodigo(rs.getString("codigo"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setStock(rs.getBigDecimal("stock"));
                p.setPrecio(rs.getBigDecimal("precio"));
                p.setIdCategoria(rs.getInt("id_categoria"));
                p.setPrecioCompra(rs.getBigDecimal("precio_compra"));
                p.setMaterial(rs.getString("material"));
                p.setPiedra(rs.getString("piedra"));
                p.setPrecioxdolar(rs.getBigDecimal("precioxdolar"));
                p.setIdWooCommerce(rs.getInt("id_woocommerce"));
                p.setWeb(rs.getBoolean("apto_web"));
                lista.add(p);
            }

            rs.close();
        } catch (SQLException e) {
            System.out.println("Error buscarPorDescripcion: " + e.toString());
        }

        return lista;
    }

    public void ConsultarProveedor(JComboBox proveedor) {
        String sql = "SELECT nombre FROM proveedor";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                proveedor.addItem(rs.getString("nombre"));
            }

        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    public boolean ModificarProductos(Productos pro) {
        BigDecimal valorDolar = obtenerUltimoValorDolar();
        BigDecimal precioEnPesos = pro.getPrecio().multiply(valorDolar).setScale(2, RoundingMode.HALF_UP);

        String sql = "UPDATE productos SET codigo=?, descripcion=?, precio=?, stock=?, proveedor=?, "
                + "id_categoria=?, precio_compra=?, material=?, piedra=?, precioxdolar=?, apto_web=? WHERE id=?";
        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, pro.getCodigo());
            ps.setString(2, pro.getDescripcion());
            ps.setBigDecimal(3, pro.getPrecio());
            ps.setBigDecimal(4, pro.getStock());
            ps.setString(5, pro.getProveedor());
            ps.setInt(6, pro.getIdCategoria());
            ps.setBigDecimal(7, pro.getPrecioCompra());
            ps.setString(8, pro.getMaterial());
            ps.setString(9, pro.getPiedra());
            ps.setBigDecimal(10, precioEnPesos); // <--- ACTUALIZACIÓN AUTOMÁTICA
            ps.setBoolean(11, pro.getWeb());
            ps.setInt(12, pro.getId());

            ps.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error ModificarProductos: " + e.toString());
            return false;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
            }
        }
    }

    public boolean actualizarPrecioPorDolar(BigDecimal valorDolar) {
        String sql = "UPDATE productos SET precioxdolar = precio * ?";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setBigDecimal(1, valorDolar);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error actualizarPrecioPorDolar: " + e.toString());
            return false;
        }
    }
    

    public BigDecimal obtenerUltimoValorDolar() {
        String sql = "SELECT valor_peso_argentino FROM tipodecambio ORDER BY id DESC LIMIT 1";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            System.out.println("Error obtenerUltimoValorDolar: " + e.getMessage());
        }
        return BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean registrarTipoDeCambio(BigDecimal valorPesoArg) {
        String sql = "INSERT INTO tipodecambio (fecha, valor_peso_argentino) VALUES (NOW(), ?)";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setBigDecimal(1, valorPesoArg);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error registrarTipoDeCambio: " + e.toString());
            return false;
        }
    }

    public boolean EliminarProducto(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.execute();
            return true;

        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public Productos BuscarProductoPorId(int idProd) {
        Productos pro = new Productos();
        String sql = "SELECT * FROM productos WHERE id = ?";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setInt(1, idProd);
            rs = ps.executeQuery();

            if (rs.next()) {
                pro.setId(rs.getInt("id"));
                pro.setCodigo(rs.getString("codigo"));
                pro.setDescripcion(rs.getString("descripcion"));
                pro.setPrecio(rs.getBigDecimal("precio"));
                pro.setStock(rs.getBigDecimal("stock"));
                pro.setProveedor(rs.getString("proveedor"));
                pro.setIdCategoria(rs.getInt("id_categoria"));
                pro.setPrecioCompra(rs.getBigDecimal("precio_compra"));
                pro.setMaterial(rs.getString("material"));
                pro.setPiedra(rs.getString("piedra"));
                pro.setWeb(rs.getBoolean("apto_web"));
            }

        } catch (SQLException e) {
            System.out.println("Error BuscarProductoPorId: " + e.toString());
        }

        return pro;
    }

    public List<Productos> buscarPorCodigoDescripcion(String filtro) {
        List<Productos> lista = new ArrayList<>();

        String sql = "SELECT * FROM productos "
                + "WHERE LOWER(codigo) LIKE ? OR LOWER(descripcion) LIKE ? "
                + "ORDER BY descripcion ASC";

        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, "%" + filtro + "%");
            ps.setString(2, "%" + filtro + "%");
            rs = ps.executeQuery();

            while (rs.next()) {
                Productos p = new Productos();

                p.setId(rs.getInt("id"));
                p.setCodigo(rs.getString("codigo"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setStock(rs.getBigDecimal("stock"));
                p.setPrecio(rs.getBigDecimal("precio"));
                p.setProveedor(rs.getString("proveedor"));
                p.setMaterial(rs.getString("material"));
                p.setPiedra(rs.getString("piedra"));
                p.setWeb(rs.getBoolean("apto_web"));

                lista.add(p);
            }

        } catch (Exception e) {
            System.out.println("Error en buscarPorCodigoDescripcion: " + e.getMessage());
        }

        return lista;
    }

    public boolean sumarStock(String codigo, BigDecimal cantidad) {

        String sql = "UPDATE productos SET stock = stock + ? WHERE codigo = ?";

        try (Connection con = cn.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, cantidad);
            ps.setString(2, codigo);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error sumarStock: " + e.getMessage());
            return false;
        }
    }


    
    public String generarCodigoConPrefijo(int idCategoria, String nombreCat, Material material, Piedra piedra) {
    String prefijo = (nombreCat.length() >= 3) ? nombreCat.substring(0, 3).toUpperCase() : nombreCat.toUpperCase();
    String sufijoMat = material.getCodigo(); // O el método que definas en tu Enum
    // Lógica condicional para la piedra
    String sufijoPiedra = (piedra != Piedra.NINGUNA) ? "-" + piedra.getAbreviatura() : "";

    String sql = "SELECT codigo FROM productos WHERE id_categoria = ? ORDER BY id DESC LIMIT 1";
    int correlativo = 1;

    try (Connection con = cn.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idCategoria);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String ultimoCodigo = rs.getString("codigo"); 
            String[] partes = ultimoCodigo.split("-");
            
            // Intentamos obtener el número de la parte central (Ej: ANI-005-PL -> 005)
            if (partes.length >= 2) {
                try {
                    correlativo = Integer.parseInt(partes[1]) + 1;
                } catch (NumberFormatException e) {
                    correlativo = 1;
                }
            }
        }
    } catch (Exception e) {
        System.err.println("Error correlativo: " + e.getMessage());
    }

    return String.format("%s-%03d-%s%s", prefijo, correlativo, sufijoMat, sufijoPiedra);
}
    

    
    public Productos buscarProducto(String criterio) {
        String sql = "SELECT * FROM productos WHERE codigo = ? OR descripcion LIKE ? OR id = ?";
        Productos pro = new Productos();
        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, criterio);
            ps.setString(2, "%" + criterio + "%");
            // Evitar error si el criterio no es numérico para el ID
            int id = 0;
            try {
                id = Integer.parseInt(criterio);
            } catch (NumberFormatException e) {
                id = -1;
            }
            ps.setInt(3, id);

            rs = ps.executeQuery();
            if (rs.next()) {
                pro.setId(rs.getInt("id"));
                pro.setCodigo(rs.getString("codigo"));
                pro.setDescripcion(rs.getString("descripcion"));
                pro.setStock(rs.getBigDecimal("stock"));
                // Agregá aquí otros campos si los necesitás (precio, etc.)
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return pro;
    }
    
    public boolean actualizarIdWooCommerce(int idLocal, int idWC) {
        String sql = "UPDATE productos SET id_woocommerce = ? WHERE id = ?";
        try (Connection con = cn.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idWC);
            ps.setInt(2, idLocal);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
   



    public void actualizarStockFisico(int idProducto, int stockWeb) {
        // Sentencia SQL para actualizar el stock físico en la base de datos
        String sql = "UPDATE productos SET stock = ? WHERE id = ?";

        try (Connection con = cn.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            // Establecer los parámetros en la sentencia
            stmt.setInt(1, stockWeb); // Seteamos el nuevo stock
            stmt.setInt(2, idProducto); // Seteamos el id del producto que queremos actualizar

            // Ejecutar la actualización
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Stock actualizado exitosamente para el producto ID: " + idProducto);
            } else {
                System.out.println("No se encontró el producto con ID: " + idProducto);
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
        }
    }
    
    public Productos buscarPorIdWooCommerce(int idWC) {
        String sql = "SELECT * FROM productos WHERE id_woocommerce = ?";
        Productos pro = null;
        try (Connection con = cn.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idWC);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pro = new Productos();
                    pro.setId(rs.getInt("id"));
                    pro.setDescripcion(rs.getString("descripcion"));
                    pro.setStock(rs.getBigDecimal("stock"));
                    pro.setIdWooCommerce(rs.getInt("id_woocommerce"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pro;
    }
    
 public List<Productos> ListarProductosWebVinculados() {
    List<Productos> listaPro = new ArrayList<>();
    // Nos aseguramos de traer todas las columnas necesarias
    String sql = "SELECT * FROM productos WHERE apto_web = true AND id_woocommerce > 0";
    try {
        con = cn.conectar(); // Tu método de conexión habitual
        ps = con.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            Productos pro = new Productos();
            pro.setId(rs.getInt("id"));
            pro.setCodigo(rs.getString("codigo"));
            pro.setDescripcion(rs.getString("descripcion"));
            pro.setStock(rs.getBigDecimal("stock"));
            
            // 🔥 CONTROLÁ ESTA LÍNEA: Asegurate de que el string coincida con tu columna en la DB
            pro.setPrecioxdolar(rs.getBigDecimal("precioxdolar")); 
            
            pro.setIdWooCommerce(rs.getInt("id_woocommerce"));
            pro.setWeb(rs.getBoolean("apto_web"));
            
            listaPro.add(pro);
        }
    } catch (SQLException e) {
        System.err.println("Error al listar productos web: " + e.getMessage());
    }
    return listaPro;
}


}

