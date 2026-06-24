
package Modelo;

/**
 * @author FDR Desarrollos
 */

public enum Piedra {
    NINGUNA("Sin Piedra", "SP"),
    DIAMANTE("Diamante", "DIA"),
    RUBI("Rubí", "RUB"),
    ESMERALDA("Esmeralda", "ESM"),
    ZAFIRO("Zafiro", "ZAF"),
    CIRCONITA("Circonita", "CZ"),
    PERLA("Perla", "PER"),
    AMATISTA("Amatista", "AMA"),
    TOPOASIO("Topasio", "TOp"),
    CITRINO("Citrino", "CIT"),
    TUIRMALINA("Turmalina", "TUR"),
    GRANATE("Granate", "GRA"),
    OTRA("Otra", "OTR");

    private final String nombre;
    private final String abreviatura;

    Piedra(String nombre, String abreviatura) {
        this.nombre = nombre;
        this.abreviatura = abreviatura;
    }

    public String getAbreviatura() { return abreviatura; }
    
    @Override
    public String toString() { return nombre; }
}
