package id.ac.ui.cs.advprog.mysawit.service;

import id.ac.ui.cs.advprog.mysawit.dto.AssignDriverRequest;
import id.ac.ui.cs.advprog.mysawit.model.*;
import id.ac.ui.cs.advprog.mysawit.repository.PengirimanRepository;
import id.ac.ui.cs.advprog.mysawit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PengirimanServiceImpl implements PengirimanService {

    private static final double MAX_WEIGHT_KG = 400.0;

    private final PengirimanRepository pengirimanRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Pengiriman assignDriver(UUID mandorId, AssignDriverRequest request) {
        User mandor = userRepository.findById(mandorId)
                .orElseThrow(() -> new IllegalArgumentException("Mandor not found"));

        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        // Calculate total weight
        double totalWeight = request.getHarvestItems().stream()
                .mapToDouble(AssignDriverRequest.HarvestItemDto::getWeightKg)
                .sum();

        // Validate max weight constraint
        if (totalWeight > MAX_WEIGHT_KG) {
            throw new IllegalArgumentException(
                    String.format("Total weight %.2f Kg exceeds maximum capacity of %.0f Kg",
                            totalWeight, MAX_WEIGHT_KG));
        }

        if (totalWeight <= 0) {
            throw new IllegalArgumentException("Total weight must be greater than 0");
        }

        Pengiriman pengiriman = Pengiriman.builder()
                .driver(driver)
                .mandor(mandor)
                .status(PengirimanStatus.MEMUAT)
                .totalWeightKg(totalWeight)
                .items(new ArrayList<>())
                .build();

        // Create pengiriman items
        for (AssignDriverRequest.HarvestItemDto item : request.getHarvestItems()) {
            PengirimanItem pengirimanItem = PengirimanItem.builder()
                    .pengiriman(pengiriman)
                    .harvestId(item.getHarvestId())
                    .weightKg(item.getWeightKg())
                    .build();
            pengiriman.getItems().add(pengirimanItem);
        }

        return pengirimanRepository.save(pengiriman);
    }

    @Override
    @Transactional
    public Pengiriman updatePengirimanStatus(UUID pengirimanId, UUID driverId, PengirimanStatus newStatus) {
        Pengiriman pengiriman = pengirimanRepository.findById(pengirimanId)
                .orElseThrow(() -> new IllegalArgumentException("Pengiriman not found"));

        // Verify the driver is assigned to this pengiriman
        if (!pengiriman.getDriver().getId().equals(driverId)) {
            throw new SecurityException("Driver is not assigned to this pengiriman");
        }

        // Validate state machine transition
        PengirimanStatus currentStatus = pengiriman.getStatus();
        if (!currentStatus.canDriverTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus));
        }

        pengiriman.setStatus(newStatus);
        return pengirimanRepository.save(pengiriman);
    }

    @Override
    public List<Pengiriman> getPengirimanByDriver(UUID driverId) {
        return pengirimanRepository.findByDriverId(driverId);
    }

    @Override
    public List<Pengiriman> getOngoingPengiriman(UUID mandorId) {
        return pengirimanRepository.findByMandorId(mandorId).stream()
                .filter(s -> s.getStatus() == PengirimanStatus.MEMUAT
                        || s.getStatus() == PengirimanStatus.MENGIRIM
                        || s.getStatus() == PengirimanStatus.TIBA_DI_TUJUAN)
                .toList();
    }

    @Override
    public Pengiriman getPengirimanById(UUID pengirimanId) {
        return pengirimanRepository.findById(pengirimanId)
                .orElseThrow(() -> new IllegalArgumentException("Pengiriman not found"));
    }
}