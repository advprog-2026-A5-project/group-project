package id.ac.ui.cs.advprog.mysawit.repository;

import id.ac.ui.cs.advprog.mysawit.enums.StatusPengiriman;
import id.ac.ui.cs.advprog.mysawit.model.Pengiriman;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PengirimanRepository extends JpaRepository<Pengiriman, Long> {
    List<Pengiriman> findByDriverId(Long driverId);
    List<Pengiriman> findByDriverUsernameContainingIgnoreCase(String driverName);
    List<Pengiriman> findByMandorId(Long mandorId);
    List<Pengiriman> findByStatus(StatusPengiriman status);
    List<Pengiriman> findByDriverIdAndStatus(Long driverId, StatusPengiriman status);
    List<Pengiriman> findByDriverIdAndStatusIn(Long driverId, Collection<StatusPengiriman> statuses);
    List<Pengiriman> findByMandorIdAndStatusIn(Long mandorId, Collection<StatusPengiriman> statuses);
    List<Pengiriman> findByMandorIdAndDriverIdAndStatusIn(Long mandorId, Long driverId, Collection<StatusPengiriman> statuses);

        @Query("""
            SELECT p
            FROM Pengiriman p
            WHERE p.driver.id = :driverId
              AND p.status IN :statuses
              AND (:startDate IS NULL OR p.createdAt >= :startDate)
              AND (:endDate IS NULL OR p.createdAt <= :endDate)
            ORDER BY p.createdAt DESC
            """)
        List<Pengiriman> findDriverHistory(
            @Param("driverId") Long driverId,
            @Param("statuses") Collection<StatusPengiriman> statuses,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
        );

        @Query("""
            SELECT p
            FROM Pengiriman p
            WHERE p.status = :status
              AND (:mandorName IS NULL OR LOWER(p.mandor.username) LIKE LOWER(CONCAT('%', :mandorName, '%')))
              AND (:startDate IS NULL OR p.createdAt >= :startDate)
              AND (:endDate IS NULL OR p.createdAt <= :endDate)
            ORDER BY p.createdAt DESC
            """)
        List<Pengiriman> findForAdminApproval(
            @Param("status") StatusPengiriman status,
            @Param("mandorName") String mandorName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
        );

    @Query("""
                    SELECT COUNT(p)
                    FROM Pengiriman p
                    JOIN p.items i
                    WHERE i.harvestId = :harvestId
                    AND p.status IN :statuses """)
    long countActiveShipmentByHarvestId(@Param("harvestId") UUID harvestId, @Param("statuses") Collection<StatusPengiriman> statuses);
}