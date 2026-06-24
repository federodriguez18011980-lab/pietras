package Controlador;

import Modelo.Cliente;
import Modelo.ClienteDao;
import Vista.VistaClientesProfesional;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class ClienteController implements ActionListener, MouseListener, java.awt.event.KeyListener {

    private Cliente cl;
    private ClienteDao clDao;
    private VistaClientesProfesional vista;
    DefaultTableModel modelo = new DefaultTableModel();

    public ClienteController(Cliente cl, ClienteDao clDao, VistaClientesProfesional vista) {
        this.cl = cl;
        this.clDao = clDao;
        this.vista = vista;
        
        // Registrar los eventos de los botones
        this.vista.txtBusquedaRapida.addKeyListener(this);
        this.vista.btnGuardar.addActionListener(this);
        this.vista.btnEditar.addActionListener(this);
        this.vista.btnEliminar.addActionListener(this);
        this.vista.btnLimpiar.addActionListener(this);
        this.vista.btnBuscar.addActionListener(this);
        this.vista.btnNuevo.addActionListener(this);
        this.vista.tableClientes.addMouseListener(this);
        
        listarClientes();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.btnGuardar) {
            registrarCliente();
        } else if (e.getSource() == vista.btnEditar) {
            modificarCliente();
        } else if (e.getSource() == vista.btnEliminar) {
            eliminarCliente();
        } else if (e.getSource() == vista.btnLimpiar || e.getSource() == vista.btnNuevo) {
            limpiarCampos();
        } else if (e.getSource() == vista.btnBuscar) {
            // Lógica de búsqueda rápida por DNI
//            if (!vista.txtBusquedaRapida.getText().equals("")) {
//                int dni = Integer.parseInt(vista.txtBusquedaRapida.getText());
//                cl = clDao.BuscarCliente(dni);
//                // Aquí podrías setear los campos si lo encuentra
//            }
            // Dentro de actionPerformed, en la parte de btnBuscar:
            if (e.getSource() == vista.btnBuscar) {
                String criterio = vista.txtBusquedaRapida.getText();
                if (criterio.equals("")) {
                    limpiarTabla();
                    listarClientes(); // Si está vacío, muestra todos
                } else {
                    llenarTablaFiltrada(criterio);
                }
            }
        
        }
    }

    private void registrarCliente() {
        if (validarCampos()) {
            llenarObjetoCliente();
            if (clDao.registrar(cl)) {
                limpiarTabla();
                listarClientes();
                limpiarCampos();
                JOptionPane.showMessageDialog(null, "Cliente Registrado con Éxito");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Por favor, complete los campos obligatorios");
        }
    }

    private void modificarCliente() {
        if (vista.txtId.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Seleccione un cliente de la tabla");
        } else {
            llenarObjetoCliente();
            cl.setId(Integer.parseInt(vista.txtId.getText()));
            if (clDao.ModificarCliente(cl)) {
                limpiarTabla();
                listarClientes();
                limpiarCampos();
                JOptionPane.showMessageDialog(null, "Cliente Modificado");
            }
        }
    }

    private void eliminarCliente() {
        if (!vista.txtId.getText().equals("")) {
            int pregunta = JOptionPane.showConfirmDialog(null, "¿Está seguro de eliminar este cliente?");
            if (pregunta == 0) {
                int id = Integer.parseInt(vista.txtId.getText());
                clDao.EliminarCliente(id);
                limpiarTabla();
                listarClientes();
                limpiarCampos();
            }
        }
    }

    // --- LÓGICA DE APOYO ---

    private void llenarObjetoCliente() {
        cl.setNombre(vista.txtNombre.getText());
        cl.setDni(Integer.parseInt(vista.txtNroDoc.getText()));
        cl.setDomicilio(vista.txtDomicilio.getText());
        cl.setTelefono(vista.txtTelefono.getText());
        cl.setEmail(vista.txtEmail.getText());
        cl.setLocalidad(vista.txtLocalidad.getText());
        cl.setRazon(vista.txtNombre.getText()); // Opcional: usar el mismo nombre para razón social

        // Conversión para ARCA (AFIP)
        // Tipo Doc: DNI=96, CUIT=80, CUIL=86
        String tipoDoc = vista.cbTipoDoc.getSelectedItem().toString();
        cl.setTipoDocumento(tipoDoc.equals("DNI") ? 96 : (tipoDoc.equals("CUIT") ? 80 : 86));

        // Condición IVA: CF=5, RI=1, Monotributo=6
        String iva = vista.cbCondicionIva.getSelectedItem().toString();
        int idIva = 5; // Default CF
        if(iva.equals("Resp. Inscripto")) idIva = 1;
        if(iva.equals("Monotributo")) idIva = 6;
        cl.setIdCondicionIva(idIva);

        cl.setProvinciaId(21); // Default Santa Fe
        cl.setIdWebClienteWoo(0); // Por ahora 0, luego vincularemos con la API
    }

    public void listarClientes() {
        List<Cliente> lista = clDao.ListarClientes();
        modelo = (DefaultTableModel) vista.tableClientes.getModel();
        Object[] ob = new Object[6];
        for (int i = 0; i < lista.size(); i++) {
            ob[0] = lista.get(i).getId();
            ob[1] = lista.get(i).getNombre();
            ob[2] = lista.get(i).getDni();
            ob[3] = lista.get(i).getIdCondicionIva() == 5 ? "Cons. Final" : "Otros";
            ob[4] = lista.get(i).getEmail();
            ob[5] = lista.get(i).getLocalidad();
            modelo.addRow(ob);
        }
        vista.tableClientes.setModel(modelo);
    }

    private void limpiarTabla() {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            modelo.removeRow(i);
            i = i - 1;
        }
    }

    private void limpiarCampos() {
        vista.txtId.setText("");
        vista.txtNombre.setText("");
        vista.txtNroDoc.setText("");
        vista.txtEmail.setText("");
        vista.txtTelefono.setText("");
        vista.txtDomicilio.setText("");
        vista.txtLocalidad.setText("");
        vista.cbTipoDoc.setSelectedIndex(0);
        vista.cbCondicionIva.setSelectedIndex(0);
    }

    private boolean validarCampos() {
        boolean validado = true;

        // Validar Nombre
        if (vista.txtNombre.getText().isEmpty()) {
            vista.txtNombre.setBorder(BorderFactory.createLineBorder(Color.RED));
            validado = false;
        } else {
            vista.txtNombre.setBorder(BorderFactory.createLineBorder(new Color(210, 190, 160))); // Color cobrizo original
        }

        // Validar DNI/CUIT
        if (vista.txtNroDoc.getText().isEmpty()) {
            vista.txtNroDoc.setBorder(BorderFactory.createLineBorder(Color.RED));
            validado = false;
        } else {
            vista.txtNroDoc.setBorder(BorderFactory.createLineBorder(new Color(210, 190, 160)));
        }

        return validado;
    }

//    @Override
//    public void mouseClicked(MouseEvent e) {
//        if (e.getSource() == vista.tableClientes) {
//            int fila = vista.tableClientes.rowAtPoint(e.getPoint());
//            vista.txtId.setText(vista.tableClientes.getValueAt(fila, 0).toString());
//            // Aquí podrías disparar una búsqueda al DAO para llenar el resto del formulario
//            // basándote en el ID que acabás de obtener.
//        }
//    }
    
    private void llenarTablaFiltrada(String criterio) {
        List<Cliente> lista = clDao.ListarClientes(); // O mejor, crear un BuscarClientes en el DAO
        limpiarTabla();
        modelo = (DefaultTableModel) vista.tableClientes.getModel();
        Object[] ob = new Object[6];
        for (Cliente c : lista) {
            // Busca coincidencia en Nombre o DNI
            if (c.getNombre().toLowerCase().contains(criterio.toLowerCase())
                    || String.valueOf(c.getDni()).contains(criterio)) {
                ob[0] = c.getId();
                ob[1] = c.getNombre();
                ob[2] = c.getDni();
                ob[3] = (c.getIdCondicionIva() == 5) ? "Cons. Final" : "Otros";
                ob[4] = c.getEmail();
                ob[5] = c.getLocalidad();
                modelo.addRow(ob);
            }
        }
    }

    // Métodos de MouseListener no utilizados pero obligatorios
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == vista.tableClientes) {
            int fila = vista.tableClientes.getSelectedRow();
            int id = Integer.parseInt(vista.tableClientes.getValueAt(fila, 0).toString());

            // Lo mejor es buscar el objeto completo al DAO para tener todos los datos nuevos
            // (TipoDoc, IVA, Localidad, etc.)
            List<Cliente> lista = clDao.ListarClientes();
            for (Cliente c : lista) {
                if (c.getId() == id) {
                    vista.txtId.setText("" + c.getId());
                    vista.txtNombre.setText(c.getNombre());
                    vista.txtNroDoc.setText("" + c.getDni());
                    vista.txtEmail.setText(c.getEmail());
                    vista.txtTelefono.setText(c.getTelefono());
                    vista.txtDomicilio.setText(c.getDomicilio());
                    vista.txtLocalidad.setText(c.getLocalidad());

                    // Setear Combos según los IDs de AFIP
                    if (c.getTipoDocumento() == 96) {
                        vista.cbTipoDoc.setSelectedItem("DNI");
                    } else if (c.getTipoDocumento() == 80) {
                        vista.cbTipoDoc.setSelectedItem("CUIT");
                    }

                    if (c.getIdCondicionIva() == 5) {
                        vista.cbCondicionIva.setSelectedItem("Consumidor Final");
                    } else if (c.getIdCondicionIva() == 1) {
                        vista.cbCondicionIva.setSelectedItem("Resp. Inscripto");
                    } else if (c.getIdCondicionIva() == 6) {
                        vista.cbCondicionIva.setSelectedItem("Monotributo");
                    }

                    break;
                }
            }
        }
    }

    @Override
    public void keyReleased(java.awt.event.KeyEvent e) {
        if (e.getSource() == vista.txtBusquedaRapida) {
            String criterio = vista.txtBusquedaRapida.getText();
            if (criterio.isEmpty()) {
                limpiarTabla();
                listarClientes(); // Si borró todo, muestra la lista completa
            } else {
                llenarTablaFiltrada(criterio); // Filtra mientras escribe
            }
        }
    }

    // Métodos obligatorios de KeyListener (vacíos)
    @Override
    public void keyTyped(java.awt.event.KeyEvent e) {
    }

    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
