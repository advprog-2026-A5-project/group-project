package id.ac.ui.cs.advprog.mysawit.validation;

import id.ac.ui.cs.advprog.mysawit.model.Kebun;
import id.ac.ui.cs.advprog.mysawit.repository.KebunRepository;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OverlapValidatorImpl implements OverlapValidator {

    private final KebunRepository kebunRepository;

    public OverlapValidatorImpl(KebunRepository kebunRepository) {
        this.kebunRepository = kebunRepository;
    }

    @Override
    public void validateNoOverlap(Polygon newPolygon, Long excludeKebunId) {
        List<Kebun> allKebun = kebunRepository.findAll();
        WKTReader wktReader = new WKTReader();

        for (Kebun existing : allKebun) {
            // Lewati pengecekan jika ID sama
            if (existing.getId().equals(excludeKebunId)) {
                continue;
            }

            try {
                Polygon existingPolygon = (Polygon) wktReader.read(existing.getWktGeometry());
                // Cek irisan
                if (existingPolygon.intersects(newPolygon)) {
                    throw new IllegalArgumentException("Koordinat tumpang tindih dengan kebun: " + existing.getNama());
                }
            } catch (ParseException e) {
                throw new RuntimeException("Gagal membaca data geometri kebun dari database", e);
            }
        }
    }
}