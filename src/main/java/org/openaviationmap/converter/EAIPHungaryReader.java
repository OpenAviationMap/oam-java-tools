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
package org.openaviationmap.converter;

import org.openaviationmap.converter.eaip.EAipProcessor;
import org.openaviationmap.converter.eaip.EAipProcessorAd;
import org.openaviationmap.converter.eaip.EAipProcessorAd13;
import org.openaviationmap.converter.eaip.EAipProcessorEnr21;
import org.openaviationmap.converter.eaip.EAipProcessorEnr22;
import org.openaviationmap.converter.eaip.EAipProcessorEnr41;
import org.openaviationmap.converter.eaip.EAipProcessorEnr44;
import org.openaviationmap.converter.eaip.EAipProcessorEnr51;
import org.openaviationmap.converter.eaip.EAipProcessorEnr52;
import org.openaviationmap.converter.eaip.EAipProcessorEnr55;
import org.openaviationmap.converter.eaip.EAipProcessorEnr56;
import org.openaviationmap.model.Aerodrome;
import org.openaviationmap.model.Airspace;
import org.openaviationmap.model.Navaid;
import org.openaviationmap.model.Point;

import java.util.List;
import java.util.Vector;

import org.w3c.dom.Node;

/**
 * A class to read eAIP publications published for Hungary.
 */
public class EAIPHungaryReader {
    /**
     *  Process an eAIP file.
     *
     *  @param eAipNode the document node of an eAIP file
     *  @param borderPoints a list of points repesenting the country border,
     *         which is used for airspaces that reference a country border.
     *         may be null.
     *  @param airspaces all airspaces extracted from the supplied eAIP file
     *         will be inserted into this list.
     *  @param navaids the navaids that are contained in the eAIP file
     *         will be inserted into this list.
     *  @param aerodromes the aerodromes that are contained contained in the
     *         eAIP file will be put into this list
     *  @param errors all parsing errors will be written to this list
     */
    public void processEAIP(Node                    eAipNode,
                            List<Point>             borderPoints,
                            List<Airspace>          airspaces,
                            List<Navaid>            navaids,
                            List<Aerodrome>         aerodromes,
                            List<ParseException>    errors) {

        String rootName = eAipNode.getOwnerDocument().getDocumentElement()
                                                                .getNodeName();

        EAipProcessor processor;

        if ("e:ENR-2.1".equals(rootName)) {
            processor = new EAipProcessorEnr21();
        } else if ("e:ENR-2.2".equals(rootName)) {
            processor = new EAipProcessorEnr22();
        } else if ("e:ENR-4.1".equals(rootName)) {
            processor = new EAipProcessorEnr41();
        } else if ("e:ENR-4.4".equals(rootName)) {
            processor = new EAipProcessorEnr44();
        } else if ("e:ENR-5.1".equals(rootName)) {
            processor = new EAipProcessorEnr51();
        } else if ("e:ENR-5.2".equals(rootName)) {
            processor = new EAipProcessorEnr52();
        } else if ("e:ENR-5.5".equals(rootName)) {
            processor = new EAipProcessorEnr55();
        } else if ("e:ENR-5.6".equals(rootName)) {
            processor = new EAipProcessorEnr56();
        } else if ("e:AD-1.3".equals(rootName)) {
            processor = new EAipProcessorAd13();
        } else if ("e:Aerodrome".equals(rootName)) {
            processor = new EAipProcessorAd();
        } else {
            // the generic case
            processor = new EAipProcessor();
        }

        processor.processEAIP(eAipNode,
                              borderPoints,
                              airspaces,
                              navaids,
                              aerodromes,
                              errors);

        // clean the result of placeholder aerodrome objects, if this
        // is not AD-1.3, which contains this list of aerodrome names
        if (!"e:AD-1.3".equals(rootName) && aerodromes != null) {
            List<Aerodrome>  ads = new Vector<Aerodrome>();

            for (Aerodrome ad : aerodromes) {
                if (ad.getArp() != null) {
                    ads.add(ad);
                }
            }
            aerodromes.retainAll(ads);
        }
    }
}
