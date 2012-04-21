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
package hu.tyrell.openaviationmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.opengis.gml._3.AbstractSurfacePatchType;
import net.opengis.gml._3.DirectPositionListType;
import net.opengis.gml._3.LinearRingType;
import net.opengis.gml._3.PolygonPatchType;
import net.opengis.gml._3.SurfacePatchArrayPropertyType;
import net.opengis.gml._3.TimePeriodType;
import net.opengis.gml._3.TimePrimitivePropertyType;

import org.junit.Test;

import aero.aixm.schema._5.AirspaceGeometryComponentPropertyType;
import aero.aixm.schema._5.AirspaceGeometryComponentType;
import aero.aixm.schema._5.AirspaceTimeSlicePropertyType;
import aero.aixm.schema._5.AirspaceTimeSliceType;
import aero.aixm.schema._5.AirspaceType;
import aero.aixm.schema._5.AirspaceVolumeType;
import aero.aixm.schema._5.CodeAirspaceType;
import aero.aixm.schema._5.SurfaceType;
import aero.aixm.schema._5_1.message.AIXMBasicMessageType;
import aero.aixm.schema._5_1.message.BasicMessageMemberAIXMPropertyType;


/**
 * Unit test for simple App.
 */
public class AixmTest {
    /**
     * A unit test for a simple AIXM file / feature.
     *
     * @throws JAXBException on JAXB issues.
     * @throws FileNotFoundException on I/O errors
     */
    @Test
    public void simpleAixmTest() throws JAXBException, FileNotFoundException {
        JAXBContext  ctx = JAXBContext.newInstance(
                                               "aero.aixm.schema._5_1.message");
        Unmarshaller unm = ctx.createUnmarshaller();
        JAXBElement<AIXMBasicMessageType>  root =
            (JAXBElement<AIXMBasicMessageType>)
            unm.unmarshal(new FileInputStream("var/test.aixm"));

        assertNotNull(root);
        assertEquals(AIXMBasicMessageType.class, root.getDeclaredType());

        AIXMBasicMessageType message = root.getValue();
        assertNotNull(message);
        List<BasicMessageMemberAIXMPropertyType> members =
                                                    message.getHasMember();
        assertEquals(1, members.size());

        BasicMessageMemberAIXMPropertyType m = members.get(0);
        assertEquals(AirspaceType.class,
                     m.getAbstractAIXMFeature().getDeclaredType());
        AirspaceType as = (AirspaceType) m.getAbstractAIXMFeature().getValue();
        assertEquals("4fd9f4be-8c65-43f6-b083-3ced9a4b2a7f",
                     as.getIdentifier().getValue());

        List<AirspaceTimeSlicePropertyType> slices = as.getTimeSlice();
        assertEquals(1, slices.size());
        AirspaceTimeSliceType asts = slices.get(0).getAirspaceTimeSlice();

        TimePrimitivePropertyType validTime = asts.getValidTime();
        assertEquals(TimePeriodType.class,
                     validTime.getAbstractTimePrimitive().getDeclaredType());
        TimePeriodType tp = (TimePeriodType)
                             validTime.getAbstractTimePrimitive().getValue();
        assertEquals(1, tp.getBeginPosition().getValue().size());
        assertEquals("2009-01-01T00:00:00.000",
                     tp.getBeginPosition().getValue().get(0));
        assertEquals(0, tp.getEndPosition().getValue().size());
        assertNotNull("UNKNOWN",
                      tp.getEndPosition().getIndeterminatePosition());

        assertEquals("BASELINE", asts.getInterpretation());
        assertEquals(1, asts.getSequenceNumber().longValue());

        TimePrimitivePropertyType featureTime = asts.getFeatureLifetime();
        assertEquals(TimePeriodType.class,
                     featureTime.getAbstractTimePrimitive().getDeclaredType());
        tp = (TimePeriodType) featureTime.getAbstractTimePrimitive().getValue();
        assertEquals(1, tp.getBeginPosition().getValue().size());
        assertEquals("2009-01-01T00:00:00.000",
                     tp.getBeginPosition().getValue().get(0));
        assertEquals(0, tp.getEndPosition().getValue().size());
        assertNotNull("UNKNOWN",
                      tp.getEndPosition().getIndeterminatePosition());

        CodeAirspaceType type = asts.getType().getValue();
        assertEquals("R", type.getValue());

        assertEquals("LHR1", asts.getDesignator().getValue().getValue());

        List<AirspaceGeometryComponentPropertyType> gl =
                                                asts.getGeometryComponent();
        assertEquals(1, gl.size());

        AirspaceGeometryComponentPropertyType g = gl.get(0);
        AirspaceGeometryComponentType ag = g.getAirspaceGeometryComponent();
        AirspaceVolumeType av = ag.getTheAirspaceVolume().getAirspaceVolume();
        SurfaceType surface =
                av.getHorizontalProjection().getValue().getSurface().getValue();
        assertNotNull(surface);

        SurfacePatchArrayPropertyType patches = surface.getPatches().getValue();
        assertEquals(1, patches.getAbstractSurfacePatch().size());
        JAXBElement<? extends AbstractSurfacePatchType> patch =
                                      patches.getAbstractSurfacePatch().get(0);
        assertEquals(PolygonPatchType.class, patch.getDeclaredType());
        PolygonPatchType p = (PolygonPatchType) patch.getValue();
        LinearRingType ring =
                (LinearRingType) p.getExterior().getAbstractRing().getValue();
        DirectPositionListType posList = ring.getPosList();
        assertEquals(24, posList.getValue().size());

        assertEquals(47.3059, posList.getValue().get(0).doubleValue(), 0.001);
        assertEquals(18.5828, posList.getValue().get(1).doubleValue(), 0.001);
        assertEquals(47.3055, posList.getValue().get(2).doubleValue(), 0.001);
        assertEquals(19.0118, posList.getValue().get(3).doubleValue(), 0.001);
        assertEquals(47.3059, posList.getValue().get(22).doubleValue(), 0.001);
        assertEquals(18.5828, posList.getValue().get(23).doubleValue(), 0.001);
    }
}
