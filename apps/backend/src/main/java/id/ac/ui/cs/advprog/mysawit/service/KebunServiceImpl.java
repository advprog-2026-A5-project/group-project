package id.ac.ui.cs.advprog.mysawit.service;

import id.ac.ui.cs.advprog.mysawit.dto.KebunRequestDTO;
import id.ac.ui.cs.advprog.mysawit.model.Kebun;
import id.ac.ui.cs.advprog.mysawit.repository.KebunRepository;
import id.ac.ui.cs.advprog.mysawit.util.GeometryMapper;
import id.ac.ui.cs.advprog.mysawit.validation.OverlapValidator;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class KebunServiceImpl implements KebunService {

    private final KebunRepository kebunRepository;
    private final OverlapValidator overlapValidator;
    private final GeometryMapper geometryMapper;

    // Objek Lock (Mutex)
    private final Lock kebunLock = new ReentrantLock();

    public KebunServiceImpl(KebunRepository kebunRepository,
                            OverlapValidator overlapValidator,
                            GeometryMapper geometryMapper) {
        this.kebunRepository = kebunRepository;
        this.overlapValidator = overlapValidator;
        this.geometryMapper = geometryMapper;
    }

    @Override
    @Transactional
    public Kebun create(KebunRequestDTO request) {
        Polygon requestedArea = geometryMapper.createQuadrilateral(request.getKoordinat());

        kebunLock.lock();
        try {
            // --- AREA KRITIS MULAI ---
            // Validasi overlap dijalankan SAAT memiliki Lock
            overlapValidator.validateNoOverlap(requestedArea, null);

            Kebun kebun = new Kebun();
            kebun.setNama(request.getNama());
            kebun.setWktGeometry(requestedArea.toText());

            // Menyimpan ke database. Karena ada @Transactional, data langsung di-flush.
            return kebunRepository.save(kebun);
            // --- AREA KRITIS SELESAI ---
        } finally {
            kebunLock.unlock();
        }
    }

    @Override
    public List<Kebun> findAll() {
        return kebunRepository.findAll();
    }

    @Override
    public Kebun findById(Long id) {
        return kebunRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kebun tidak ditemukan"));
    }

    @Override
    @Transactional
    public Kebun update(Long id, KebunRequestDTO request) {
        Kebun existingKebun = findById(id);
        Polygon requestedArea = geometryMapper.createQuadrilateral(request.getKoordinat());

        // Proses Update juga harus dikunci agar tidak ada yang insert/update bersamaan
        kebunLock.lock();
        try {
            overlapValidator.validateNoOverlap(requestedArea, id);

            existingKebun.setNama(request.getNama());
            existingKebun.setWktGeometry(requestedArea.toText());

            return kebunRepository.save(existingKebun);
        } finally {
            kebunLock.unlock();
        }
    }

    @Override
    public void delete(Long id) {
        Kebun kebun = findById(id);
        kebunRepository.delete(kebun);
    }
}