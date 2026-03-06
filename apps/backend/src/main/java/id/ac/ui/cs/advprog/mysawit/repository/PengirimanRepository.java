package id.ac.ui.cs.advprog.mysawit.repository;

import id.ac.ui.cs.advprog.mysawit.model.Shipment;
import id.ac.ui.cs.advprog.mysawit.model.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PengirimanRepository extends JpaRepository<Pengiriman, UUID> {
    List<Pengiriman> findByDriverId(UUID driverId);
    List<Pengiriman> findByMandorId(UUID mandorId);
    List<Pengiriman> findByStatus(PengirimanStatus status);
    List<Pengiriman> findByDriverIdAndStatus(UUID driverId, PengirimanStatus status);
}