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

import java.util.List;
import java.util.Vector;

/**
 * A class representing an aerodrome.
 */
public class Aerodrome {

    /**
     * Indicates the type of aerodrome, for example Heliport or Seaport.
     */
    private AerodromeType aerodrometype = AerodromeType.AERODROME;

    /**
     * The ICAO code of the aerodrome.
     */
    private String icao;

    /**
     * The IATA code of the aerodrome.
     */
    private String iata;

    /**
     * The name of the aerodrome.
     */
    private String name;

    /**
     * The Airport Reference Point (ARP).
     */
    private Point arp;

    /**
     * The elevation at the ARP.
     */
    private Elevation elevation;

    /**
     * The apron frequency.
     */
    private Frequency apron;

    /**
     * The ATIS frequency.
     */
    private Frequency atis;

    /**
     * The approach frequency.
     */
    private Frequency approach;

    /**
     * The tower frequency.
     */
    private Frequency tower;

    /**
     * The aerodrome flight information service.
     */
    private Frequency afis;

    /**
     * The airspaces related to the aerodrome.
     */
    private List<Airspace> airspaces;

    /**
     * The runways at the aerodrome.
     */
    private List<Runway> runways;

    /**
     * The navigation aids related to the aerodrome.
     */
    private List<Navaid> navaids;

    /**
     * Remarks.
     */
    private String remarks;

    /**
     * Default constructor.
     */
    public Aerodrome() {
        runways   = new Vector<Runway>();
        navaids   = new Vector<Navaid>();
        airspaces = new Vector<Airspace>();
    }


    /**
     * Returns the type of this aerodrome.
     * 
     * @return The aerodrome type.
     */
    public AerodromeType getAerodrometype() {
        return aerodrometype;
    }


    /**
     * Sets the type for this aerodrome.
     * <p>
     * If not set, the default is "AERODROME" which is a standard landbased aerodrome.
     * 
     * @param aerodrometype The Aerodrome type.
     */
    public void setAerodrometype(AerodromeType aerodrometype) {
        this.aerodrometype = aerodrometype;
    }

    /**
     * @return the icao
     */
    public String getIcao() {
        return icao;
    }

    /**
     * @param icao the icao to set
     */
    public void setIcao(String icao) {
        this.icao = icao;
    }

    /**
     * @return the iata
     */
    public String getIata() {
        return iata;
    }

    /**
     * @param iata the iata to set
     */
    public void setIata(String iata) {
        this.iata = iata;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the arp
     */
    public Point getArp() {
        return arp;
    }

    /**
     * @param arp the arp to set
     */
    public void setArp(Point arp) {
        this.arp = arp;
    }

    /**
     * @return the elevation
     */
    public Elevation getElevation() {
        return elevation;
    }

    /**
     * @param elevation the elevation to set
     */
    public void setElevation(Elevation elevation) {
        this.elevation = elevation;
    }

    /**
     * @return the tower
     */
    public Frequency getTower() {
        return tower;
    }

    /**
     * @param tower the tower to set
     */
    public void setTower(Frequency tower) {
        this.tower = tower;
    }

    /**
     * @return the runways
     */
    public List<Runway> getRunways() {
        return runways;
    }

    /**
     * @param runways the runways to set
     */
    public void setRunways(List<Runway> runways) {
        this.runways = runways;
    }

    /**
     * @return the remarks
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * @return the airspaces
     */
    public List<Airspace> getAirspaces() {
        return airspaces;
    }

    /**
     * @param airspaces the airspaces to set
     */
    public void setAirspaces(List<Airspace> airspaces) {
        this.airspaces = airspaces;
    }

    /**
     * @return the afis
     */
    public Frequency getAfis() {
        return afis;
    }

    /**
     * @param afis the afis to set
     */
    public void setAfis(Frequency afis) {
        this.afis = afis;
    }

    /**
     * @return the navaids
     */
    public List<Navaid> getNavaids() {
        return navaids;
    }

    /**
     * @param navaids the navaids to set
     */
    public void setNavaids(List<Navaid> navaids) {
        this.navaids = navaids;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((afis == null) ? 0 : afis.hashCode());
        result = prime * result
                + ((airspaces == null) ? 0 : airspaces.hashCode());
        result = prime * result
                + ((approach == null) ? 0 : approach.hashCode());
        result = prime * result + ((apron == null) ? 0 : apron.hashCode());
        result = prime * result + ((arp == null) ? 0 : arp.hashCode());
        result = prime * result + ((atis == null) ? 0 : atis.hashCode());
        result = prime * result
                + ((elevation == null) ? 0 : elevation.hashCode());
        result = prime * result + ((iata == null) ? 0 : iata.hashCode());
        result = prime * result + ((icao == null) ? 0 : icao.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((navaids == null) ? 0 : navaids.hashCode());
        result = prime * result + ((remarks == null) ? 0 : remarks.hashCode());
        result = prime * result + ((runways == null) ? 0 : runways.hashCode());
        result = prime * result + ((tower == null) ? 0 : tower.hashCode());
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
        Aerodrome other = (Aerodrome) obj;
        if (afis == null) {
            if (other.afis != null) {
                return false;
            }
        } else if (!afis.equals(other.afis)) {
            return false;
        }
        if (airspaces == null) {
            if (other.airspaces != null) {
                return false;
            }
        } else if (!airspaces.equals(other.airspaces)) {
            return false;
        }
        if (approach == null) {
            if (other.approach != null) {
                return false;
            }
        } else if (!approach.equals(other.approach)) {
            return false;
        }
        if (apron == null) {
            if (other.apron != null) {
                return false;
            }
        } else if (!apron.equals(other.apron)) {
            return false;
        }
        if (arp == null) {
            if (other.arp != null) {
                return false;
            }
        } else if (!arp.equals(other.arp)) {
            return false;
        }
        if (atis == null) {
            if (other.atis != null) {
                return false;
            }
        } else if (!atis.equals(other.atis)) {
            return false;
        }
        if (elevation == null) {
            if (other.elevation != null) {
                return false;
            }
        } else if (!elevation.equals(other.elevation)) {
            return false;
        }
        if (iata == null) {
            if (other.iata != null) {
                return false;
            }
        } else if (!iata.equals(other.iata)) {
            return false;
        }
        if (icao == null) {
            if (other.icao != null) {
                return false;
            }
        } else if (!icao.equals(other.icao)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (navaids == null) {
            if (other.navaids != null) {
                return false;
            }
        } else if (!navaids.equals(other.navaids)) {
            return false;
        }
        if (remarks == null) {
            if (other.remarks != null) {
                return false;
            }
        } else if (!remarks.equals(other.remarks)) {
            return false;
        }
        if (runways == null) {
            if (other.runways != null) {
                return false;
            }
        } else if (!runways.equals(other.runways)) {
            return false;
        }
        if (tower == null) {
            if (other.tower != null) {
                return false;
            }
        } else if (!tower.equals(other.tower)) {
            return false;
        }
        return true;
    }

    /**
     * @return the apron
     */
    public Frequency getApron() {
        return apron;
    }

    /**
     * @param apron the apron to set
     */
    public void setApron(Frequency apron) {
        this.apron = apron;
    }

    /**
     * @return the atis
     */
    public Frequency getAtis() {
        return atis;
    }

    /**
     * @param atis the atis to set
     */
    public void setAtis(Frequency atis) {
        this.atis = atis;
    }

    /**
     * @return the approach
     */
    public Frequency getApproach() {
        return approach;
    }

    /**
     * @param approach the approach to set
     */
    public void setApproach(Frequency approach) {
        this.approach = approach;
    }

}
