package id.ac.ui.cs.advprog.mysawit.util;

import id.ac.ui.cs.advprog.mysawit.dto.CoordinateDTO;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeometryMapper {
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public Polygon createQuadrilateral(List<CoordinateDTO> points) {
        if (points == null || points.size() != 4) {
            throw new IllegalArgumentException("Kebun harus memiliki tepat 4 titik koordinat.");
        }

        Coordinate[] coords = new Coordinate[5];
        for (int i = 0; i < 4; i++) {
            coords[i] = new Coordinate(points.get(i).getLongitude(), points.get(i).getLatitude());
        }
        coords[4] = coords[0];

        return geometryFactory.createPolygon(coords);
    }
}