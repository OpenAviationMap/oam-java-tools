/*
    Open Aviation Map
    Copyright (C) 2012 Ákos Maróy

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
package hu.tyrell.openaviationmap.model;

import java.util.List;
import java.util.Vector;

/**
 * A class representing an aerodrome.
 */
public class Aerodrome {
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
     * The ground frequency.
     */
    private Frequency ground;

    /**
     * The tower frequency.
     */
    private Frequency tower;

    /**
     * The aerodrome flight information service.
     */
    private Frequency afis;

    /**
     * The airspace related to the aerodrome.
     */
    private Airspace airspace;

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
        runways = new Vector<Runway>();
        navaids = new Vector<Navaid>();
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
     * @return the ground
     */
    public Frequency getGround() {
        return ground;
    }

    /**
     * @param ground the ground to set
     */
    public void setGround(Frequency ground) {
        this.ground = ground;
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
     * @return the airspace
     */
    public Airspace getAirspace() {
        return airspace;
    }

    /**
     * @param airspace the airspace to set
     */
    public void setAirspace(Airspace airspace) {
        this.airspace = airspace;
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
                + ((airspace == null) ? 0 : airspace.hashCode());
        result = prime * result + ((arp == null) ? 0 : arp.hashCode());
        result = prime * result
                + ((elevation == null) ? 0 : elevation.hashCode());
        result = prime * result + ((ground == null) ? 0 : ground.hashCode());
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
        if (airspace == null) {
            if (other.airspace != null) {
                return false;
            }
        } else if (!airspace.equals(other.airspace)) {
            return false;
        }
        if (arp == null) {
            if (other.arp != null) {
                return false;
            }
        } else if (!arp.equals(other.arp)) {
            return false;
        }
        if (elevation == null) {
            if (other.elevation != null) {
                return false;
            }
        } else if (!elevation.equals(other.elevation)) {
            return false;
        }
        if (ground == null) {
            if (other.ground != null) {
                return false;
            }
        } else if (!ground.equals(other.ground)) {
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

}
