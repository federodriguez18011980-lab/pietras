package Modelo;

public class Cliente {
    private int id;
    private int dni; // Podría usarse para CUIT también si es BIGINT, pero lo mantenemos según tu base
    private String telefono;
    private String nombre;
    private String domicilio;
    private String email;
    private String razon;
    
    // --- NUEVOS CAMPOS FISCALES Y WEB ---
    private int tipoDocumento;     // DNI=96, CUIT=80, etc.
    private int idCondicionIva;    // RI=1, Monotributo=6, CF=5
    private String localidad;
    private int provinciaId;       // Santa Fe=21
    private int idWebClienteWoo;   // ID de usuario en WordPress

    public Cliente() {
    }

    // Constructor completo actualizado
    public Cliente(int id, int dni, String telefono, String nombre, String domicilio, String email, String razon, 
                   int tipoDocumento, int idCondicionIva, String localidad, int provinciaId, int idWebClienteWoo) {
        this.id = id;
        this.dni = dni;
        this.telefono = telefono;
        this.nombre = nombre;
        this.domicilio = domicilio;
        this.email = email;
        this.razon = razon;
        this.tipoDocumento = tipoDocumento;
        this.idCondicionIva = idCondicionIva;
        this.localidad = localidad;
        this.provinciaId = provinciaId;
        this.idWebClienteWoo = idWebClienteWoo;
    }

    // Getters y Setters de los campos nuevos
    public int getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(int tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public int getIdCondicionIva() { return idCondicionIva; }
    public void setIdCondicionIva(int idCondicionIva) { this.idCondicionIva = idCondicionIva; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public int getProvinciaId() { return provinciaId; }
    public void setProvinciaId(int provinciaId) { this.provinciaId = provinciaId; }

    public int getIdWebClienteWoo() { return idWebClienteWoo; }
    public void setIdWebClienteWoo(int idWebClienteWoo) { this.idWebClienteWoo = idWebClienteWoo; }

    // (Mantené los getters y setters anteriores que ya tenías...)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDni() { return dni; }
    public void setDni(int dni) { this.dni = dni; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRazon() { return razon; }
    public void setRazon(String razon) { this.razon = razon; }
}