
package Modelo;

/**
 * @author Federico FDR DEV WEB
 */

public enum Material {
    
    ORO_18K("Oro 18K","18"),
    ORO_BLANCO("Oro Blanco","OB"),
    ORO_ROSA("Oro Rosa","OR"),
    ORO_AMARILLO("Oro Amarillo","OA"),
    PLATA_925("Plata 925","PL"),
    PLATA_950("Plata 950","PL"),
    ACERO_QUIRURGICO("Acero Quirúrgico","AC"),
    PLATINO("Platino","PT"),
    CUERO("Cuero","CUE"),
    BIJOU("Bijou","BJU"),
    OTRO("Otro","OTR");        

    private final String nombre;
    private final String codigo;

    Material(String nombre, String codigo) {
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }
    public String getCodigo() { 
        return codigo;
    }

    @Override
    public String toString() {
        return nombre; // Esto es lo que verá el usuario en el ComboBox
    }
}
