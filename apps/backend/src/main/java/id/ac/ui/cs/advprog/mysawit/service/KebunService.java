package id.ac.ui.cs.advprog.mysawit.service;

import id.ac.ui.cs.advprog.mysawit.dto.KebunRequestDTO;
import id.ac.ui.cs.advprog.mysawit.model.Kebun;

import java.util.List;

public interface KebunService {
    Kebun create(KebunRequestDTO request);
    List<Kebun> findAll();
    Kebun findById(Long id);
    Kebun update(Long id, KebunRequestDTO request);
    void delete(Long id);
}