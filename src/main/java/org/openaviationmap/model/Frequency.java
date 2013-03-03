/*
    Open Aviation Map
    Copyright (C) 2012-2013 Ákos Maróy

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openaviationmap.model;

import java.text.DecimalFormat;

/**
 * A class representing frequencies.
 */
public class Frequency {
    /**
     * The number formatter.
     */
    private static final DecimalFormat DF = new DecimalFormat("###.###");

    /**
     * The frequency, that is, beats per second.
     */
    private double frequency;

    /**
     * Default constructor.
     */
    public Frequency() {
        frequency = 0;
    }

    /**
     * Constructor with a value.
     *
     *  @param f the frequency value.
     */
    public Frequency(double f) {
        frequency = f;
    }

    /**
     * Create a frequency object from a string.
     *
     * @param str the string describing a frequency object.
     * @return the frequency object described by the string
     */
    public static Frequency fromString(String str) {
        String s     = str.trim().toLowerCase();
        int    sLen3 = s.length() - 3;

        if (s.endsWith("ghz")) {
            return new Frequency(Double.parseDouble(s.substring(0, sLen3))
                               * 1000000000.0);
        } else if (s.endsWith("mhz")) {
            return new Frequency(Double.parseDouble(s.substring(0, sLen3))
                               * 1000000.0);
        } else if (s.endsWith("khz")) {
            return new Frequency(Double.parseDouble(s.substring(0, sLen3))
                               * 1000.0);
        } else {
            return new Frequency(Double.parseDouble(s));
        }
    }

    /**
     * Create a short string representation of the frequency, that is,
     * in MHz, kHz, etc.
     *
     * @return a string representation of the frequency at hand.
     */
    @Override
    public String toString() {
        if (frequency < 1000.0) {
            return DF.format(frequency).concat("Hz").toString();
        } else if (frequency < 1000000.0) {
            return DF.format(frequency / 1000.0).concat("kHz").toString();
        } else if (frequency < 1000000000.0) {
            return DF.format(frequency / 1000000.0).concat("MHz").toString();
        } else if (frequency < 1000000000000.0) {
            return DF.format(frequency / 1000000000.0).concat("GHz").toString();
        } else {
            return DF.format(frequency).concat("Hz").toString();
        }
    }

    /**
     * @return the frequency
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(frequency);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Frequency other = (Frequency) obj;
        if (Double.doubleToLongBits(frequency) != Double
                .doubleToLongBits(other.frequency)) {
            return false;
        }
        return true;
    }

}
