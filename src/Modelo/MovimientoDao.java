package Modelo;

import Conexion.ConexionMysql;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDao {
    ConexionMysql cn = new ConexionMysql();
    Connection con;
    PreparedStatement ps;
    ResultSet rs;

    // 1. REGISTRAR MOVIMIENTO
    public boolean registrarMovimiento(MovimientoStock mov) {
        // Mantenemos solo el ID_PRODUCTO por integridad referencial
        String sql = "INSERT INTO movimientos_stock (id_producto, tipo_movimiento, cantidad, motivo) VALUES (?,?,?,?)";
        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setInt(1, mov.getIdProducto());
            ps.setString(2, mov.getTipoMovimiento());
            ps.setDouble(3, mov.getCantidad());
            ps.setString(4, mov.getMotivo());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error al registrar movimiento: " + e.toString());
            return false;
        } finally {
            closeResources();
        }
    }

public List<MovimientoStock> consultarHistorial(String criterio, java.util.Date inicio, java.util.Date fin) {
    List<MovimientoStock> lista = new ArrayList<>();
    
    // SQL con filtro de fechas usando BETWEEN
    String sql = "SELECT m.ID_MOVIMIENTO, m.ID_PRODUCTO, m.TIPO_MOVIMIENTO, m.CANTIDAD, m.FECHA, m.MOTIVO, " +
                 "p.codigo as prod_codigo, p.descripcion as prod_nombre " +
                 "FROM movimientos_stock m " +
                 "INNER JOIN productos p ON m.id_producto = p.id " +
                 "WHERE (p.codigo LIKE ? OR p.descripcion LIKE ? OR CAST(p.id AS CHAR) = ?) ";
    
    // Si hay fechas, agregamos la condición al SQL
    if (inicio != null && fin != null) {
        sql += " AND CAST(m.fecha AS DATE) BETWEEN ? AND ? ";
    }
    
    sql += " ORDER BY m.fecha DESC";

    try {
        con = cn.conectar();
        ps = con.prepareStatement(sql);
        String filtro = "%" + criterio + "%";
        ps.setString(1, filtro);
        ps.setString(2, filtro);
        ps.setString(3, criterio);

        if (inicio != null && fin != null) {
            ps.setDate(4, new java.sql.Date(inicio.getTime()));
            ps.setDate(5, new java.sql.Date(fin.getTime()));
        }

        rs = ps.executeQuery();
        while (rs.next()) {
            MovimientoStock m = new MovimientoStock();
            m.setId(rs.getInt("ID_MOVIMIENTO"));
            m.setNombreProducto(rs.getString("prod_nombre"));
            m.setTipoMovimiento(rs.getString("TIPO_MOVIMIENTO"));
            m.setCantidad(rs.getDouble("CANTIDAD"));
            m.setFecha(rs.getTimestamp("FECHA"));
            m.setMotivo(rs.getString("MOTIVO"));
            lista.add(m);
        }
    } catch (SQLException e) {
        System.out.println("Error en consulta filtrada: " + e.toString());
    } finally {
        closeResources();
    }
    return lista;
}

    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            System.out.println("Error cerrando recursos: " + e.toString());
        }
    }
}