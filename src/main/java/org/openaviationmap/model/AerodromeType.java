package org.openaviationmap.model;

public enum AerodromeType {
    /**
     * Aerodrome - A standard land aerodrome.
     */
    AERODROME,

    /**
     * Heliport - A heliport not associated directly with a hospital
     */
    HELIPORT,

    /**
     * Heliport - A heliport associated directly with a hospital
     */
    HOSPITALPORT,

    /**
     * A Seaplane port which may or may not co-exist with a regular aerodrome.
     */
    SEAPORT,

    /**
     * An abandoned aerodrome.
     */
    ABANDONED;

}
