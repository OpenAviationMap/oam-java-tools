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
package hu.tyrell.openaviationmap.converter;

import hu.tyrell.openaviationmap.converter.eaip.EAipProcessor;
import hu.tyrell.openaviationmap.converter.eaip.EAipProcessorEnr21;
import hu.tyrell.openaviationmap.converter.eaip.EAipProcessorEnr22;
import hu.tyrell.openaviationmap.converter.eaip.EAipProcessorEnr51;
import hu.tyrell.openaviationmap.converter.eaip.EAipProcessorEnr52;
import hu.tyrell.openaviationmap.converter.eaip.EAipProcessorEnr55;
import hu.tyrell.openaviationmap.converter.eaip.EAipProcessorEnr56;
import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Point;

import java.util.List;

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
     *  @param errors all parsing errors will be written to this list
     */
    public void processEAIP(Node                    eAipNode,
                            List<Point>             borderPoints,
                            List<Airspace>          airspaces,
                            List<ParseException>    errors) {

        String rootName = eAipNode.getOwnerDocument().getDocumentElement()
                                                                .getNodeName();

        EAipProcessor processor;

        if ("e:ENR-2.1".equals(rootName)) {
            processor = new EAipProcessorEnr21();
        } else if ("e:ENR-2.2".equals(rootName)) {
            processor = new EAipProcessorEnr22();
        } else if ("e:ENR-5.1".equals(rootName)) {
            processor = new EAipProcessorEnr51();
        } else if ("e:ENR-5.2".equals(rootName)) {
            processor = new EAipProcessorEnr52();
        } else if ("e:ENR-5.5".equals(rootName)) {
            processor = new EAipProcessorEnr55();
        } else if ("e:ENR-5.6".equals(rootName)) {
            processor = new EAipProcessorEnr56();
        } else {
            // the generic case
            processor = new EAipProcessor();
        }

        processor.processEAIP(eAipNode, borderPoints, airspaces, errors);
    }
}
