package id.ac.ui.cs.advprog.mysawit.model;

public enum ShipmentStatus {
    MEMUAT,
    MENGIRIM,
    TIBA_DI_TUJUAN,
    APPROVED_MANDOR,
    REJECTED_MANDOR,
    APPROVED_ADMIN,
    REJECTED_ADMIN,
    PARTIALLY_REJECTED_ADMIN;

    public boolean canDriverTransitionTo(StatusPengiriman next) {
        return switch (this) {
            case MEMUAT -> next == MENGIRIM;
            case MENGIRIM -> next == TIBA_DI_TUJUAN;
            default -> false;
        };
    }
}