package Modelo;

public enum TipoPago {
    EFECTIVO,
    TRANSFERENCIA,
    CREDITO,
    DEVOLUCION;

    @Override
    public String toString() {
        switch (this) {
            case EFECTIVO: return "Efectivo";
            case TRANSFERENCIA: return "Transferencia";
            case CREDITO: return "Crédito";
            case DEVOLUCION: return "Devoluvion";
            default: return name();
        }
    }
}