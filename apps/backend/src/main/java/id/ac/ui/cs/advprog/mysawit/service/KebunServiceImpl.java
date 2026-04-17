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
import java.util.stream.Collectors;
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
        if (request.getKodeKebun() == null || request.getKodeKebun().isBlank()) {
            throw new IllegalArgumentException("Kode kebun wajib diisi");
        }
        if (request.getLuas() == null || request.getLuas() <= 0) {
            throw new IllegalArgumentException("Luas kebun wajib diisi dan lebih dari 0");
        }
        Polygon requestedArea = geometryMapper.createQuadrilateral(request.getKoordinat());

        kebunLock.lock();
        try {
            // --- AREA KRITIS MULAI ---
            // Validasi overlap dijalankan SAAT memiliki Lock
            overlapValidator.validateNoOverlap(requestedArea, null);

            Kebun kebun = new Kebun();
            kebun.setNama(request.getNama());
            kebun.setKodeKebun(request.getKodeKebun());
            kebun.setLuas(request.getLuas());
            kebun.setWktGeometry(requestedArea.toText());

            // Menyimpan ke database. Karena ada @Transactional, data langsung di-flush.
            return kebunRepository.save(kebun);
            // --- AREA KRITIS SELESAI ---
        } finally {
            kebunLock.unlock();
        }
    }

    @Override
    public List<Kebun> findAll(String nama, String kodeKebun) {
        List<Kebun> allKebun = kebunRepository.findAll();
        
        return allKebun.stream()
                .filter(k -> (nama == null || k.getNama().toLowerCase().contains(nama.toLowerCase())))
                .filter(k -> (kodeKebun == null || k.getKodeKebun().toLowerCase().contains(kodeKebun.toLowerCase())))
                .collect(Collectors.toList());
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
            if (request.getLuas() != null && request.getLuas() > 0) {
                existingKebun.setLuas(request.getLuas());
            }
            // Cannot update kodeKebun as per spec
            existingKebun.setWktGeometry(requestedArea.toText());

            return kebunRepository.save(existingKebun);
        } finally {
            kebunLock.unlock();
        }
    }

    @Override
    public void delete(Long id) {
        Kebun kebun = findById(id);
        if (kebun.getMandorName() != null && !kebun.getMandorName().isBlank()) {
            throw new IllegalArgumentException("Tidak dapat menghapus kebun yang masih terikat dengan seorang Mandor");
        }
        kebunRepository.delete(kebun);
    }

    @Override
    public Kebun assignMandor(Long kebunId, String mandorName) {
        Kebun kebun = findById(kebunId);
        if (mandorName == null || mandorName.isBlank()) {
            throw new IllegalArgumentException("Nama mandor tidak boleh kosong");
        }
        kebun.setMandorName(mandorName);
        return kebunRepository.save(kebun);
    }

    @Override
    public Kebun assignSupir(Long kebunId, String supirName) {
        Kebun kebun = findById(kebunId);
        if (supirName == null || supirName.isBlank()) {
            throw new IllegalArgumentException("Nama supir tidak boleh kosong");
        }
        if (kebun.getSupirNames().contains(supirName)) {
            throw new IllegalArgumentException("Supir ini sudah ditugaskan ke kebun ini");
        }
        kebun.getSupirNames().add(supirName);
        return kebunRepository.save(kebun);
    }

    @Override
    public Kebun unassignMandor(Long sourceKebunId, Long targetKebunId) {
        if (sourceKebunId.equals(targetKebunId)) {
            throw new IllegalArgumentException("Kebun asal dan kebun tujuan harus berbeda");
        }
        Kebun source = findById(sourceKebunId);
        if (source.getMandorName() == null || source.getMandorName().isBlank()) {
            throw new IllegalArgumentException("Kebun ini belum memiliki mandor yang di-assign");
        }
        Kebun target = findById(targetKebunId);
        if (target.getMandorName() != null && !target.getMandorName().isBlank()) {
            throw new IllegalArgumentException("Kebun tujuan sudah memiliki mandor");
        }

        String mandorName = source.getMandorName();
        source.setMandorName(null);
        target.setMandorName(mandorName);

        kebunRepository.save(source);
        kebunRepository.save(target);
        return source;
    }

    @Override
    public Kebun unassignSupir(Long sourceKebunId, String supirName, Long targetKebunId) {
        if (sourceKebunId.equals(targetKebunId)) {
            throw new IllegalArgumentException("Kebun asal dan kebun tujuan harus berbeda");
        }
        Kebun source = findById(sourceKebunId);
        if (supirName == null || supirName.isBlank() || !source.getSupirNames().contains(supirName)) {
            throw new IllegalArgumentException("Supir yang ingin dicopot tidak ditemukan di kebun ini");
        }
        Kebun target = findById(targetKebunId);
        if (target.getSupirNames().contains(supirName)) {
            throw new IllegalArgumentException("Supir sudah bekerja di kebun tujuan");
        }

        source.getSupirNames().remove(supirName);
        target.getSupirNames().add(supirName);

        kebunRepository.save(source);
        kebunRepository.save(target);
        return source;
    }
}