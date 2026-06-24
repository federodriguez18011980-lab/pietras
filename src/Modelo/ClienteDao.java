package Modelo;

import Conexion.ConexionMysql;
import java.sql.Connection;
import javax.swing.JOptionPane;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

public class ClienteDao {
    
    Conexion.ConexionMysql cn = new ConexionMysql();
    Connection con;
    PreparedStatement ps;
    ResultSet rs;
    
    public boolean registrar(Cliente cl){
        // SQL actualizado con los nuevos campos
        String sql = "INSERT INTO clientes (nombre, dni, domicilio, telefono, email, razon_social, "
                   + "tipo_documento, id_condicion_iva, localidad, provincia_id, id_web_cliente_woo) "
                   + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, cl.getNombre());
            ps.setInt(2, cl.getDni());
            ps.setString(3, cl.getDomicilio());
            ps.setString(4, cl.getTelefono());
            ps.setString(5, cl.getEmail());
            ps.setString(6, cl.getRazon());
            // Nuevos campos
            ps.setInt(7, cl.getTipoDocumento());
            ps.setInt(8, cl.getIdCondicionIva());
            ps.setString(9, cl.getLocalidad());
            ps.setInt(10, cl.getProvinciaId());
            ps.setInt(11, cl.getIdWebClienteWoo());
            
            ps.execute();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al registrar cliente: " + e.toString());
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (SQLException e) { System.out.println(e.toString()); }
        }
    }
    
    public List<Cliente> ListarClientes() {
        List<Cliente> ListaCL = new ArrayList<>();
        String sql = "SELECT * FROM clientes";
        try {
            con = cn.conectar();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Cliente cl = new Cliente();
                cl.setId(rs.getInt("id"));
                cl.setNombre(rs.getString("nombre"));
                cl.setDni(rs.getInt("dni"));
                cl.setDomicilio(rs.getString("domicilio"));
                cl.setTelefono(rs.getString("telefono"));
                cl.setEmail(rs.getString("email"));
                cl.setRazon(rs.getString("razon_social"));
                // Mapeo de nuevos campos
                cl.setTipoDocumento(rs.getInt("tipo_documento"));
                cl.setIdCondicionIva(rs.getInt("id_condicion_iva"));
                cl.setLocalidad(rs.getString("localidad"));
                cl.setProvinciaId(rs.getInt("provincia_id"));
                cl.setIdWebClienteWoo(rs.getInt("id_web_cliente_woo"));
                ListaCL.add(cl);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar: " + e.toString());
        }
        return ListaCL; 
    }

    public boolean ModificarCliente(Cliente cl){
        String sql = "UPDATE clientes SET nombre=?, dni=?, domicilio=?, telefono=?, email=?, razon_social=?, "
                   + "tipo_documento=?, id_condicion_iva=?, localidad=?, provincia_id=?, id_web_cliente_woo=? WHERE id=?";
        try {
            con = cn.conectar(); // Importante reconectar si se cerró
            ps = con.prepareStatement(sql);
            ps.setString(1, cl.getNombre());
            ps.setInt(2, cl.getDni());
            ps.setString(3, cl.getDomicilio());
            ps.setString(4, cl.getTelefono());
            ps.setString(5, cl.getEmail());
            ps.setString(6, cl.getRazon());
            ps.setInt(7, cl.getTipoDocumento());
            ps.setInt(8, cl.getIdCondicionIva());
            ps.setString(9, cl.getLocalidad());
            ps.setInt(10, cl.getProvinciaId());
            ps.setInt(11, cl.getIdWebClienteWoo());
            ps.setInt(12, cl.getId());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.toString());
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (SQLException ex) { System.out.println(ex.toString()); }
        }
    }
    
     public Cliente BuscarCliente(int dni) {

        Cliente cl = new Cliente();
        String sql = "SELECT * FROM clientes WHERE dni=?";

        try {

            con = cn.conectar();
            ps = con.prepareStatement(sql);
            ps.setInt(1, dni);
            rs = ps.executeQuery();

            if (rs.next()) {

                cl.setNombre(rs.getString("nombre"));   
                cl.setDomicilio(rs.getString("Domicilio"));
                cl.setTelefono(rs.getString("telefono"));
                cl.setEmail(rs.getString("email"));
                cl.setRazon(rs.getString("razon_social"));
            }

        } catch (SQLException e) {

            System.out.println(e.toString());
        }
        return cl;

    }
     
        public boolean EliminarCliente(int id){
        String sql = "DELETE FROM clientes WHERE id = ?";

        try {

          ps = con.prepareStatement(sql);
          ps.setInt(1, id);
          ps.execute();
          return true;
          
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;

        }finally{

            try {
                con.close();
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }
    }

    // El método BuscarCliente también debería actualizarse para traer los nuevos campos si los necesitás en la vista.
}
