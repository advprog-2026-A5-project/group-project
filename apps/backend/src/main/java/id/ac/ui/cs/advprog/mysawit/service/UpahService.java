package id.ac.ui.cs.advprog.mysawit.service;

import id.ac.ui.cs.advprog.mysawit.dto.UpahRequestDTO;
import id.ac.ui.cs.advprog.mysawit.model.Upah;

import java.util.List;

public interface UpahService {
    List<Upah> getAll();
    Upah update(UpahRequestDTO request);
}
