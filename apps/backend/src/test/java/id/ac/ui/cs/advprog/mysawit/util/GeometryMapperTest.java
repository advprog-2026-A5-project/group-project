package id.ac.ui.cs.advprog.mysawit.util;

import id.ac.ui.cs.advprog.mysawit.dto.CoordinateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeometryMapperTest {

    private GeometryMapper geometryMapper;

    @BeforeEach
    void setUp() {
        geometryMapper = new GeometryMapper();
    }

    @Test
    void createQuadrilateral_validFourPoints_returnsPolygon() {
        List<CoordinateDTO> points = Arrays.asList(
                new CoordinateDTO(-6.0, 106.0),
                new CoordinateDTO(-6.0, 107.0),
                new CoordinateDTO(-7.0, 107.0),
                new CoordinateDTO(-7.0, 106.0)
        );

        Polygon result = geometryMapper.createQuadrilateral(points);

        assertNotNull(result);
        assertTrue(result.isValid());
        // Polygon should have 5 coordinates (4 vertices + closing point)
        assertEquals(5, result.getCoordinates().length);
    }

    @Test
    void createQuadrilateral_polygonIsClosed() {
        List<CoordinateDTO> points = Arrays.asList(
                new CoordinateDTO(-6.0, 106.0),
                new CoordinateDTO(-6.0, 107.0),
                new CoordinateDTO(-7.0, 107.0),
                new CoordinateDTO(-7.0, 106.0)
        );

        Polygon result = geometryMapper.createQuadrilateral(points);
        Coordinate[] coords = result.getCoordinates();

        // First and last coordinate must be the same (closed ring)
        assertEquals(coords[0].x, coords[4].x);
        assertEquals(coords[0].y, coords[4].y);
    }

    @Test
    void createQuadrilateral_coordinateOrderIsLongitudeLatitude() {
        List<CoordinateDTO> points = Arrays.asList(
                new CoordinateDTO(-6.0, 106.0),
                new CoordinateDTO(-6.0, 107.0),
                new CoordinateDTO(-7.0, 107.0),
                new CoordinateDTO(-7.0, 106.0)
        );

        Polygon result = geometryMapper.createQuadrilateral(points);
        Coordinate firstCoord = result.getCoordinates()[0];

        // JTS uses x=longitude, y=latitude
        assertEquals(106.0, firstCoord.x);
        assertEquals(-6.0, firstCoord.y);
    }

    @Test
    void createQuadrilateral_nullPoints_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                geometryMapper.createQuadrilateral(null));
    }

    @Test
    void createQuadrilateral_emptyList_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                geometryMapper.createQuadrilateral(Collections.emptyList()));
    }

    @Test
    void createQuadrilateral_threePoints_throwsException() {
        List<CoordinateDTO> points = Arrays.asList(
                new CoordinateDTO(-6.0, 106.0),
                new CoordinateDTO(-6.0, 107.0),
                new CoordinateDTO(-7.0, 107.0)
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                geometryMapper.createQuadrilateral(points));
        assertTrue(ex.getMessage().contains("4 titik koordinat"));
    }

    @Test
    void createQuadrilateral_fivePoints_throwsException() {
        List<CoordinateDTO> points = Arrays.asList(
                new CoordinateDTO(-6.0, 106.0),
                new CoordinateDTO(-6.0, 107.0),
                new CoordinateDTO(-7.0, 107.0),
                new CoordinateDTO(-7.0, 106.0),
                new CoordinateDTO(-6.5, 106.5)
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                geometryMapper.createQuadrilateral(points));
        assertTrue(ex.getMessage().contains("4 titik koordinat"));
    }

    @Test
    void createQuadrilateral_onePoint_throwsException() {
        List<CoordinateDTO> points = Collections.singletonList(
                new CoordinateDTO(-6.0, 106.0)
        );

        assertThrows(IllegalArgumentException.class, () ->
                geometryMapper.createQuadrilateral(points));
    }

    @Test
    void createQuadrilateral_producesValidWkt() {
        List<CoordinateDTO> points = Arrays.asList(
                new CoordinateDTO(0.0, 0.0),
                new CoordinateDTO(0.0, 10.0),
                new CoordinateDTO(10.0, 10.0),
                new CoordinateDTO(10.0, 0.0)
        );

        Polygon result = geometryMapper.createQuadrilateral(points);
        String wkt = result.toText();

        assertTrue(wkt.startsWith("POLYGON"));
    }
}
