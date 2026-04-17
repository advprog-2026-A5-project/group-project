package id.ac.ui.cs.advprog.mysawit.validation;

import org.locationtech.jts.geom.Polygon;

public interface OverlapValidator {
    void validateNoOverlap(Polygon newPolygon, Long excludeKebunId);
}