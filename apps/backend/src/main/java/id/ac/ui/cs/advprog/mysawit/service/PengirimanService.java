package id.ac.ui.cs.advprog.mysawit.service;

import id.ac.ui.cs.advprog.mysawit.dto.AssignDriverRequest;
import id.ac.ui.cs.advprog.mysawit.model.Pengiriman;
import id.ac.ui.cs.advprog.mysawit.model.PengirimanStatus;

import java.util.List;
import java.util.UUID;

public interface PengirimanService {
    Pengiriman assignDriver(UUID mandorId, AssignDriverRequest request);
    Pengiriman updatePengirimanStatus(UUID pengirimanId, UUID driverId, PengirimanStatus newStatus);
    List<Pengiriman> getPengirimanByDriver(UUID driverId);
    List<Pengiriman> getOngoingPengiriman(UUID mandorId);
    Pengiriman getPengirimanById(UUID pengirimanId);
}