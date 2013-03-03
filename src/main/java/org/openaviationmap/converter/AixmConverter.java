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

import org.openaviationmap.model.Aerodrome;
import org.openaviationmap.model.Airspace;
import org.openaviationmap.model.Boundary;
import org.openaviationmap.model.Circle;
import org.openaviationmap.model.CompoundBoundary;
import org.openaviationmap.model.Distance;
import org.openaviationmap.model.Elevation;
import org.openaviationmap.model.Frequency;
import org.openaviationmap.model.Navaid;
import org.openaviationmap.model.Point;
import org.openaviationmap.model.Ring;
import org.openaviationmap.model.Runway;
import org.openaviationmap.model.UOM;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Vector;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.NamespaceContext;

import net.opengis.gml._3.AbstractRingPropertyType;
import net.opengis.gml._3.CircleByCenterPointType;
import net.opengis.gml._3.CodeWithAuthorityType;
import net.opengis.gml._3.CurvePropertyType;
import net.opengis.gml._3.CurveSegmentArrayPropertyType;
import net.opengis.gml._3.CurveType;
import net.opengis.gml._3.DirectPositionListType;
import net.opengis.gml._3.DirectPositionType;
import net.opengis.gml._3.GeodesicStringType;
import net.opengis.gml._3.LengthType;
import net.opengis.gml._3.LinearRingType;
import net.opengis.gml._3.PolygonPatchType;
import net.opengis.gml._3.RingType;
import net.opengis.gml._3.SurfacePatchArrayPropertyType;
import net.opengis.gml._3.TimeIndeterminateValueType;
import net.opengis.gml._3.TimePeriodType;
import net.opengis.gml._3.TimePositionType;
import net.opengis.gml._3.TimePrimitivePropertyType;

import org.apache.ws.commons.util.NamespaceContextImpl;

import aero.aixm.schema._5.*;
import aero.aixm.schema._5_1.message.AIXMBasicMessageType;
import aero.aixm.schema._5_1.message.BasicMessageMemberAIXMPropertyType;

/**
 * Class to convert aviation data into AIXM.
 */
public final class AixmConverter {
    /**
     * Namespace URI of the XLink namespace.
     */
    public static final String XLINK_NS_URI = "http://www.w3.org/1999/xlink";

    /**
     * Preferred namespace prefix for the XLink namespace.
     */
    public static final String XLINK_NS_PREFIX = "xlink";

    /**
     * Preferred URI of the XSI namespace.
     */
    public static final String XSI_NS_URI =
                                    "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * Preferred namespace prefix for the XSI namespace.
     */
    public static final String XSI_NS_PREFIX = "xsi";

    /**
     * Namespace URI of the GML namespace.
     */
    public static final String GML_NS_URI = "http://www.opengis.net/gml/3.2";

    /**
     * Preferred namespace prefix for the GML namespace.
     */
    public static final String GML_NS_PREFIX = "gml";

    /**
     * Namespace URI of the GCO namespace.
     */
    public static final String GCO_NS_URI =
                                            "http://www.isotc211.org/2005/gco";

    /**
     * Preferred namespace prefix for the GCO namespace.
     */
    public static final String GCO_NS_PREFIX = "gco";

    /**
     * Namespace URI of the GMD namespace.
     */
    public static final String GMD_NS_URI =
                                            "http://www.isotc211.org/2005/gmd";

    /**
     * Preferred namespace prefix for the GMD namespace.
     */
    public static final String GMD_NS_PREFIX = "gmd";

    /**
     * Namespace URI of the GTS namespace.
     */
    public static final String GTS_NS_URI =
                                           "http://www.isotc211.org/2005/gts";

    /**
     * Preferred namespace prefix for the GTS namespace.
     */
    public static final String GTS_NS_PREFIX = "gts";

    /**
     * Namespace URI of the AIXM namespace.
     */
    public static final String AIXM_NS_URI =
                                            "http://www.aixm.aero/schema/5.1";

    /**
     * Preferred namespace prefix for the AIXM namespace.
     */
    public static final String AIXM_NS_PREFIX = "aixm";

    /**
     * Namespace URI of the AIXM Message namespace.
     */
    public static final String AIXM_MESSAGE_NS_URI =
                                    "http://www.aixm.aero/schema/5.1/message";

    /**
     * Preferred namespace prefix for the AIXM Message namespace.
     */
    public static final String AIXM_MESSAGE_NS_PREFIX = "message";

    /**
     * A GML object factory, used to create GML related objects.
     */
    private static net.opengis.gml._3.ObjectFactory gmlFactory =
                                        new net.opengis.gml._3.ObjectFactory();
    /**
     * An AIXM object factory, generated by JAXB, used to create AIXM
     * related objects.
     */
    private static aero.aixm.schema._5.ObjectFactory aixmFactory =
                                        new aero.aixm.schema._5.ObjectFactory();

    /**
     * An AIXM message object factory, generated by JAXB, used to create AIXM
     * related objects.
     */
    private static aero.aixm.schema._5_1.message.ObjectFactory
        aixmMessageFactory = new aero.aixm.schema._5_1.message.ObjectFactory();

    /**
     * The date formatter to format validity dates.
     */
    private static SimpleDateFormat dateFormatter =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    /**
     * Private default constructor.
     */
    private AixmConverter() {
    }

    /**
     * Return a namespace context with the used namespaces and their
     * preferred prefixes.
     *
     * @return a namespace context with namespaces used by AIXM documents.
     */
    public static NamespaceContext getNsCtx() {
        NamespaceContextImpl nsCtx = new NamespaceContextImpl();

        nsCtx.startPrefixMapping(XLINK_NS_PREFIX, XLINK_NS_URI);
        nsCtx.startPrefixMapping(XSI_NS_PREFIX, XSI_NS_URI);
        nsCtx.startPrefixMapping(GML_NS_PREFIX, GML_NS_URI);
        nsCtx.startPrefixMapping(GCO_NS_PREFIX, GCO_NS_URI);
        nsCtx.startPrefixMapping(GMD_NS_PREFIX, GMD_NS_URI);
        nsCtx.startPrefixMapping(GTS_NS_PREFIX, GTS_NS_URI);
        nsCtx.startPrefixMapping(AIXM_NS_PREFIX, AIXM_NS_URI);
        nsCtx.startPrefixMapping(AIXM_MESSAGE_NS_PREFIX, AIXM_MESSAGE_NS_URI);

        return nsCtx;
    }

    /**
     * Convert a set of aerial information into an AIXM message document.
     *
     * @param airspaces the airspaces to convert
     * @param navaids the navigation aids to convert
     * @param aerodromes the aerodromes to convert
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @return an AIXM message containing the supplied aerial resources
     */
    public static JAXBElement<AIXMBasicMessageType>
    convertToAixm(List<Airspace>     airspaces,
                  List<Navaid>       navaids,
                  List<Aerodrome>    aerodromes,
                  GregorianCalendar  validStart,
                  GregorianCalendar  validEnd,
                  String             interpretation,
                  long               sequence,
                  long               correction) {

        AIXMBasicMessageType message =
                aixmMessageFactory.createAIXMBasicMessageType();


        airspacesToAixm(airspaces,
                        validStart,
                        validEnd,
                        interpretation,
                        sequence,
                        correction,
                        message.getHasMember());

        navaidsToAixm(navaids,
                      validStart,
                      validEnd,
                      interpretation,
                      sequence,
                      correction,
                      message.getHasMember());

        aerodromesToAixm(aerodromes,
                         validStart,
                         validEnd,
                         interpretation,
                         sequence,
                         correction,
                         message.getHasMember());

        JAXBElement<AIXMBasicMessageType> m =
                            aixmMessageFactory.createAIXMBasicMessage(message);
        m.getValue().setId("uuid." + UUID.randomUUID().toString());

        return m;
    }

    /**
     * Convert a single airspace into an AIXM airspace.
     *
     * @param airspace the airspace to convert
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the generated airspace properties will be put into
     *        this list
     */
    static void
    airspaceToAixm(Airspace                                 airspace,
                   GregorianCalendar                        validStart,
                   GregorianCalendar                        validEnd,
                   String                                   interpretation,
                   long                                     sequence,
                   long                                     correction,
                   List<BasicMessageMemberAIXMPropertyType> propList) {

        UUID uuid = UUID.randomUUID();

        AirspaceType at = aixmFactory.createAirspaceType();
        at.setId("uuid." + uuid.toString());

        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace("urn:uuid:");
        id.setValue(uuid.toString());
        at.setIdentifier(id);

        AirspaceTimeSlicePropertyType sliceProp =
                            aixmFactory.createAirspaceTimeSlicePropertyType();
        AirspaceTimeSliceType slice = aixmFactory.createAirspaceTimeSliceType();
        initTimeSlice(slice, validStart, validEnd, interpretation,
                      sequence, correction);

        // set the type
        CodeAirspaceType type = aixmFactory.createCodeAirspaceType();
        type.setValue(airspace.getType());
        slice.setType(type);

        // set the designator
        CodeAirspaceDesignatorType designator =
                                aixmFactory.createCodeAirspaceDesignatorType();
        designator.setValue(airspace.getDesignator());
        slice.setDesignator(designator);
        CodeYesNoType yes = aixmFactory.createCodeYesNoType();
        yes.setValue("yes");
        slice.setDesignatorICAO(yes);

        // set the name
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(airspace.getName());
        slice.setAixmName(name);

        airspaceControlActivationToAixm(airspace, slice);

        // set remarks
        NotePropertyType remarks = aixmFactory.createNotePropertyType();
        NoteType note = aixmFactory.createNoteType();

        CodeNotePurposeType purpose = aixmFactory.createCodeNotePurposeType();
        purpose.setValue("REMARK");
        note.setPurpose(purpose);

        LinguisticNotePropertyType enNoteT =
                                aixmFactory.createLinguisticNotePropertyType();
        LinguisticNoteType enNote = aixmFactory.createLinguisticNoteType();
        TextNoteType tNote = aixmFactory.createTextNoteType();
        tNote.setLang("eng");
        tNote.setValue(airspace.getRemarks());
        enNote.setNote(tNote);
        enNoteT.setLinguisticNote(enNote);
        note.getTranslatedNote().add(enNoteT);

        remarks.setNote(note);
        slice.getAnnotation().add(remarks);

        // create & convert the airspace volume itself
        AirspaceVolumeType airspaceVolume =
                                        aixmFactory.createAirspaceVolumeType();

        airspaceLimitsToAixm(airspace, airspaceVolume);

        boundaryToAixm(airspace.getBoundary(), airspaceVolume);

        // package all the stuff into whatever AIXM needs
        AirspaceVolumePropertyType airspaceVolumeProp =
                                aixmFactory.createAirspaceVolumePropertyType();
        airspaceVolumeProp.setAirspaceVolume(airspaceVolume);

        AirspaceGeometryComponentType geometry =
                aixmFactory.createAirspaceGeometryComponentType();
        geometry.setTheAirspaceVolume(airspaceVolumeProp);

        AirspaceGeometryComponentPropertyType geometryProp =
                aixmFactory.createAirspaceGeometryComponentPropertyType();
        geometryProp.setAirspaceGeometryComponent(geometry);

        slice.getGeometryComponent().add(geometryProp);

        // add the time slice to the airspace
        sliceProp.setAirspaceTimeSlice(slice);

        at.getTimeSlice().add(sliceProp);

        // put the airspace into the property list
        JAXBElement<AirspaceType> e = aixmFactory.createAirspace(at);

        BasicMessageMemberAIXMPropertyType p =
            aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        p.setAbstractAIXMFeature(e);

        propList.add(p);
    }

    /**
     * Convert two calendar points into an AIXM time period.
     *
     * @param start the start of the period. if null, an unknown indeterminate
     *        time point is created.
     * @param end the end of the period. if null, an unknown indeterminate
     *        time point is created.
     * @return the time period corresponding to the supplied values.
     */
    private static TimePrimitivePropertyType
    convertTimePeriod(GregorianCalendar start, GregorianCalendar end) {

        TimePositionType tStart = calendarToTimePosition(start);

        TimePositionType tEnd = calendarToTimePosition(end);

        TimePeriodType   pTime  = gmlFactory.createTimePeriodType();
        pTime.setBeginPosition(tStart);
        pTime.setEndPosition(tEnd);

        TimePrimitivePropertyType tp =
                                gmlFactory.createTimePrimitivePropertyType();
        tp.setAbstractTimePrimitive(gmlFactory.createTimePeriod(pTime));

        return tp;
    }

    /**
     * Convert a gregorian calendar into an AIXM time position, in UTC.
     *
     * @param time the time to convert. if null, an unknown indeterminate
     *        time value is generated
     * @return the converted time
     */
    private static TimePositionType
    calendarToTimePosition(GregorianCalendar time) {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        TimePositionType timePos = gmlFactory.createTimePositionType();
        if (time != null) {
            timePos.getValue().add(
                                    dateFormatter.format(time.getTime()));
        } else {
            timePos.setIndeterminatePosition(
                                        TimeIndeterminateValueType.UNKNOWN);
        }

        return timePos;
    }

    /**
     * Set the airspace control type & activation type, based on heuristics
     * about the properties of the airspace.
     *
     * @param airspace the airspace to get the information from
     * @param slice the airspace time slice to set the data for
     */
    private static void
    airspaceControlActivationToAixm(Airspace              airspace,
                                    AirspaceTimeSliceType slice) {
        // set the control type
        CodeMilitaryOperationsType controlType =
                            aixmFactory.createCodeMilitaryOperationsType();

        if (airspace.getType() != null) {
            if (airspace.getType().startsWith("M")) {
                controlType.setValue("MIL");
            } else {
                controlType.setValue("CIVIL");
            }
            slice.setControlType(controlType);
        }

        if ("H24".equals(airspace.getActiveTime())) {
            CodeDayType day = aixmFactory.createCodeDayType();
            day.setValue("ANY");

            TimeType start = aixmFactory.createTimeType();
            start.setValue("00:00");

            TimeType end = aixmFactory.createTimeType();
            end.setValue("24:00");

            CodeTimeReferenceType tRef =
                                    aixmFactory.createCodeTimeReferenceType();
            tRef.setValue("UTC");

            TimesheetType ts = aixmFactory.createTimesheetType();
            ts.setDay(day);
            ts.setStartTime(start);
            ts.setEndTime(end);
            ts.setTimeReference(tRef);

            TimesheetPropertyType tsp =
                                    aixmFactory.createTimesheetPropertyType();
            tsp.setTimesheet(ts);

            AirspaceActivationType act =
                                    aixmFactory.createAirspaceActivationType();
            act.getTimeInterval().add(tsp);

            AirspaceActivationPropertyType actProp =
                            aixmFactory.createAirspaceActivationPropertyType();
            actProp.setAirspaceActivation(act);
            slice.getActivation().add(actProp);
        }

        CodeAirspaceActivityType actv =
                                aixmFactory.createCodeAirspaceActivityType();

        if (airspace.getRemarks() != null && !airspace.getRemarks().isEmpty()) {
            String remarks = airspace.getRemarks().toLowerCase();
            if (remarks.contains("nuclear")) {
                actv.setValue("NUCLEAR");
            } else if (remarks.contains("refine")) {
                actv.setValue("REFINERY");
            } else if (remarks.contains("chemic")) {
                actv.setValue("CHEMICAL");
            } else if (remarks.contains("fauna")) {
                actv.setValue("FAUNA");
            } else if (remarks.contains("bird")) {
                actv.setValue("BIRD");
            } else if (remarks.contains("glide")
                    || remarks.contains("gliding")) {
                actv.setValue("GLIDER");
            } else if (remarks.contains("milit")) {
                actv.setValue("MILOPS");
            } else if (remarks.contains("parachut")) {
                actv.setValue("PARACHUTE");
            } else if (remarks.contains("aerobat")) {
                actv.setValue("AEROBATICS");
            }

            if (actv.isSetValue()) {
                AirspaceActivationType act =
                                    aixmFactory.createAirspaceActivationType();
                act.setActivity(actv);

                AirspaceActivationPropertyType actProp =
                        aixmFactory.createAirspaceActivationPropertyType();
                actProp.setAirspaceActivation(act);
                slice.getActivation().add(actProp);
            }
        }
    }

    /**
     * Convert airspace vertical limits into an AIXM airspace volume.
     *
     * @param airspace the airspace to convert the limits for.
     * @param airspaceVolume the airspace volume to put the limits into
     */
    private static void airspaceLimitsToAixm(Airspace          airspace,
                                            AirspaceVolumeType airspaceVolume) {
        // set the lower limit
        airspaceVolume.setLowerLimit(
                                    convertElevation(airspace.getLowerLimit()));

        CodeVerticalReferenceType lowerLimitRef =
                                  aixmFactory.createCodeVerticalReferenceType();
        lowerLimitRef.setValue(
                            airspace.getLowerLimit().getReference().toString());
        airspaceVolume.setLowerLimitReference(lowerLimitRef);


        // set the upper limit
        airspaceVolume.setUpperLimit(
                                    convertElevation(airspace.getUpperLimit()));

        CodeVerticalReferenceType upperLimitRef =
                                  aixmFactory.createCodeVerticalReferenceType();
        upperLimitRef.setValue(
                            airspace.getUpperLimit().getReference().toString());
        airspaceVolume.setUpperLimitReference(upperLimitRef);
    }

    /**
     * Convert an airspace boundary into AIXM, by putting it into an
     * AIXM airspace volume.
     *
     * @param boundary the boundary to convert
     * @param airspaceVolume the airspace volume to put the results of the
     *        conversion into
     */
    private static void boundaryToAixm(Boundary           boundary,
                                       AirspaceVolumeType airspaceVolume) {

        CurveSegmentArrayPropertyType curveSegment =
                            gmlFactory.createCurveSegmentArrayPropertyType();

        // put the appropriate type into a curveSegment
        switch (boundary.getType()) {
        case RING:
            Ring r = (Ring) boundary;
            addRingToCurveSegment(r, curveSegment);
            break;

        case CIRCLE:
            Circle c = (Circle) boundary;
            addCircleToCurveSegment(c, curveSegment);
            break;

        case COMPOUND:
            CompoundBoundary cb = (CompoundBoundary) boundary;
            for (Boundary b : cb.getBoundaryList()) {
                switch (b.getType()) {
                case RING:
                    Ring rr = (Ring) b;
                    addRingToCurveSegment(rr, curveSegment);
                    break;

                case CIRCLE:
                    Circle cc = (Circle) b;
                    addCircleToCurveSegment(cc, curveSegment);
                    break;

                default:
                    // TODO: handle recursive compound boundaries
                }
            }
            break;

        default:
        }

        // put the curve segment into all the structure AIXM requires
        CurveType curve = gmlFactory.createCurveType();
        curve.setSegments(curveSegment);

        CurvePropertyType curveProp = gmlFactory.createCurvePropertyType();
        curveProp.setAbstractCurve(gmlFactory.createCurve(curve));

        RingType ring = gmlFactory.createRingType();
        ring.getCurveMember().add(curveProp);

        AbstractRingPropertyType abstractRingProp =
                                gmlFactory.createAbstractRingPropertyType();
        abstractRingProp.setAbstractRing(gmlFactory.createRing(ring));

        PolygonPatchType polygonPatch = gmlFactory.createPolygonPatchType();
        polygonPatch.setExterior(abstractRingProp);

        SurfacePatchArrayPropertyType patchArray =
                          gmlFactory.createSurfacePatchArrayPropertyType();
        patchArray.getAbstractSurfacePatch().add(
                            gmlFactory.createPolygonPatch(polygonPatch));

        SurfaceType surfaceType = aixmFactory.createSurfaceType();
        surfaceType.setSrsName("urn:ogc:def:crs:EPSG:4326");
        surfaceType.setPatches(gmlFactory.createPatches(patchArray));

        SurfacePropertyType surfacePropType =
                                    aixmFactory.createSurfacePropertyType();
        surfacePropType.setSurface(aixmFactory.createSurface(surfaceType));
        airspaceVolume.setHorizontalProjection(surfacePropType);
    }

    /**
     * Add a circle object to an AIXM curve segment.
     *
     * @param circle the circle to add
     * @param curveSegment the curve segment to add the circle to.
     */
    private static void
    addCircleToCurveSegment(Circle                        circle,
                            CurveSegmentArrayPropertyType curveSegment) {
        DirectPositionType center = convertPoint(circle.getCenter());

        LengthType radius = gmlFactory.createLengthType();
        radius.setUom(convertUom(circle.getRadius().getUom()));
        radius.setValue(circle.getRadius().getDistance());

        CircleByCenterPointType ccp =
                                gmlFactory.createCircleByCenterPointType();
        ccp.setPos(center);
        ccp.setRadius(radius);

        curveSegment.getAbstractCurveSegment().add(
                          gmlFactory.createCircleByCenterPoint(ccp));
    }

    /**
     * Add a ring object to an AIXM curve segment.
     *
     * @param ring the ring to add
     * @param curveSegment the curve segment to add the ring to.
     */
    private static void
    addRingToCurveSegment(Ring                          ring,
                          CurveSegmentArrayPropertyType curveSegment) {
        DirectPositionListType directPosList =
                                gmlFactory.createDirectPositionListType();
        for (Point p : ring.getPointList()) {
            directPosList.getValue().add(p.getLatitude());
            directPosList.getValue().add(p.getLongitude());
        }

        GeodesicStringType geoString =
                                    gmlFactory.createGeodesicStringType();
        geoString.setPosList(directPosList);

        curveSegment.getAbstractCurveSegment().add(
                        gmlFactory.createGeodesicString(geoString));
    }

    /**
     * Convert a list of airspaces into a list of AIXM Airspaces.
     *
     * @param airspaces the airspaces to convert
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted airspaces will be put into
     *        this list
     */
    public static void
    airspacesToAixm(List<Airspace>                           airspaces,
                    GregorianCalendar                        validStart,
                    GregorianCalendar                        validEnd,
                    String                                   interpretation,
                    long                                     sequence,
                    long                                     correction,
                    List<BasicMessageMemberAIXMPropertyType> propList) {

        for (Airspace ap : airspaces) {
            airspaceToAixm(ap,
                           validStart,
                           validEnd,
                           interpretation,
                           sequence,
                           correction,
                           propList);
        }
    }

    /**
     * Convert a single navaid into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     */
    static void
    navaidToAixm(Navaid                                   navaid,
                 GregorianCalendar                        validStart,
                 GregorianCalendar                        validEnd,
                 String                                   interpretation,
                 long                                     sequence,
                 long                                     correction,
                 List<BasicMessageMemberAIXMPropertyType> propList) {

        List<AbstractNavaidEquipmentType> equipmentList =
                                    new Vector<AbstractNavaidEquipmentType>();

        String navaidType;

        switch (navaid.getType()) {
        case VORDME:
            vordmeToAixm(navaid, validStart, validEnd, interpretation,
                         sequence, correction, propList, equipmentList);
            navaidType = "VOR_DME";
            break;

        case VOR:
        case VOT:
            vorToAixm(navaid, validStart, validEnd, interpretation,
                      sequence, correction, propList, equipmentList);
            navaidType = "VOR";
            break;

        case DME:
            dmeToAixm(navaid, validStart, validEnd, interpretation,
                      sequence, correction, propList, equipmentList);
            navaidType = "DME";
            break;

        case NDB:
            ndbToAixm(navaid, validStart, validEnd, interpretation,
                      sequence, correction, propList, equipmentList);
            navaidType = "NDB";
            break;

        case MARKER:
            markerToAixm(navaid, validStart, validEnd, interpretation,
                         sequence, correction, propList, equipmentList);
            navaidType = "MKR";
          break;

        case LOC:
            localizerToAixm(navaid, validStart, validEnd, interpretation,
                            sequence, correction, propList, equipmentList);
            navaidType = "LOC";
          break;

        case GP:
            glidepathToAixm(navaid, validStart, validEnd, interpretation,
                            sequence, correction, propList, equipmentList);
            navaidType = "ILS";
          break;

        case DESIGNATED:
            designatedToAixm(navaid, validStart, validEnd, interpretation,
                             sequence, correction, propList);
            navaidType = null;
          break;

        default:
            throw new RuntimeException("unrecognized navaid type "
                                      + navaid.getType());
        }

        if (navaidType != null) {
            navaidForNavaidEquipment(navaid, navaidType, equipmentList,
                                     validStart, validEnd, interpretation,
                                     sequence, correction, propList);
        }

    }

    /**
     * Convert a VOR into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     * @param equipmentList the generated navaid equipment will be put into
     *         this list
     */
    static void
    vorToAixm(Navaid                                   navaid,
              GregorianCalendar                        validStart,
              GregorianCalendar                        validEnd,
              String                                   interpretation,
              long                                     sequence,
              long                                     correction,
              List<BasicMessageMemberAIXMPropertyType> propList,
              List<AbstractNavaidEquipmentType>        equipmentList) {

        // the location
        ElevatedPointType ep = convertPointElevation(navaid,
                                                     navaid.getElevation());
        ElevatedPointPropertyType epp =
                                aixmFactory.createElevatedPointPropertyType();
        epp.setElevatedPoint(ep);

        // magnetic variation
        ValMagneticVariationType var     = null;
        DateYearType             varDate = null;
        if (navaid.getVariation() != null) {
            var = aixmFactory.createValMagneticVariationType();
            var.setValue(doubleToBigDecimal(
                                        navaid.getVariation().getVariation()));

            varDate = aixmFactory.createDateYearType();
            varDate.setValue(Integer.toString(navaid.getVariation().getYear()));
        }

        // the declination
        // magnetic variation
        ValMagneticVariationType decl     = null;
        if (navaid.getDeclination() != 0) {
            decl = aixmFactory.createValMagneticVariationType();
            decl.setValue(doubleToBigDecimal(navaid.getDeclination()));
        }

        // the designator
        CodeNavaidDesignatorType des =
                                aixmFactory.createCodeNavaidDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace("urn:uuid:");
        id.setValue(UUID.randomUUID().toString());

        // the type
        CodeVORType type = aixmFactory.createCodeVORType();
        switch (navaid.getType()) {
        // TODO: set DVOR when applicable
        case VOR:
        case VORDME:
            type.setValue("VOR");
            break;

        case VOT:
            type.setValue("VOT");
            break;

        default:
            throw new RuntimeException("unrecognzed VOR type '"
                                      + navaid.getType() + "'");
        }

        // the name
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(navaid.getName());

        // the frequency
        ValFrequencyType freq = convertFrequency(navaid.getFrequency());

        // package the things together
        VORTimeSliceType slice = aixmFactory.createVORTimeSliceType();
        initTimeSlice(slice, validStart, validEnd, interpretation,
                      sequence, correction);
        slice.setLocation(epp);
        slice.setMagneticVariation(var);
        slice.setDateMagneticVariation(varDate);
        slice.setDeclination(decl);
        slice.setDesignator(des);
        slice.setType(type);
        slice.setAixmName(name);
        slice.setFrequency(freq);

        VORTimeSlicePropertyType sliceProp =
                                  aixmFactory.createVORTimeSlicePropertyType();
        sliceProp.setVORTimeSlice(slice);

        VORType vor = aixmFactory.createVORType();
        vor.getTimeSlice().add(sliceProp);
        vor.setIdentifier(id);
        vor.setId("uuid." + id.getValue());


        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createVOR(vor));

        propList.add(prop);

        equipmentList.add(vor);
    }

    /**
     * Convert a DME into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     * @param equipmentList the generated navaid equipment will be put into
     *         this list
     */
    static void
    dmeToAixm(Navaid                                   navaid,
              GregorianCalendar                        validStart,
              GregorianCalendar                        validEnd,
              String                                   interpretation,
              long                                     sequence,
              long                                     correction,
              List<BasicMessageMemberAIXMPropertyType> propList,
              List<AbstractNavaidEquipmentType>        equipmentList) {

        // the location
        ElevatedPointType ep = convertPointElevation(navaid,
                                                     navaid.getElevation());
        ElevatedPointPropertyType epp =
                                aixmFactory.createElevatedPointPropertyType();
        epp.setElevatedPoint(ep);

        // magnetic variation
        ValMagneticVariationType var     = null;
        DateYearType             varDate = null;
        if (navaid.getVariation() != null) {
            var = aixmFactory.createValMagneticVariationType();
            var.setValue(doubleToBigDecimal(
                                        navaid.getVariation().getVariation()));

            varDate = aixmFactory.createDateYearType();
            varDate.setValue(Integer.toString(navaid.getVariation().getYear()));
        }

        // the designator
        CodeNavaidDesignatorType des =
                                aixmFactory.createCodeNavaidDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace("urn:uuid:");
        id.setValue(UUID.randomUUID().toString());

        // the type
        CodeDMEType type = aixmFactory.createCodeDMEType();
        type.setValue("DME");

        // the name
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(navaid.getName());

        // the frequency
        CodeDMEChannelType channel = aixmFactory.createCodeDMEChannelType();
        channel.setValue(navaid.getDmeChannel());

        // package the things together
        DMETimeSliceType slice = aixmFactory.createDMETimeSliceType();
        initTimeSlice(slice, validStart, validEnd, interpretation,
                      sequence, correction);
        slice.setLocation(epp);
        slice.setMagneticVariation(var);
        slice.setDateMagneticVariation(varDate);
        slice.setDesignator(des);
        slice.setType(type);
        slice.setAixmName(name);
        slice.setChannel(channel);

        DMETimeSlicePropertyType sliceProp =
                                  aixmFactory.createDMETimeSlicePropertyType();
        sliceProp.setDMETimeSlice(slice);

        DMEType dme = aixmFactory.createDMEType();
        dme.getTimeSlice().add(sliceProp);
        dme.setIdentifier(id);
        dme.setId("uuid." + id.getValue());


        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createDME(dme));

        propList.add(prop);

        equipmentList.add(dme);
    }

    /**
     * Convert an NDB into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     * @param equipmentList the generated navaid equipment will be put into
     *         this list
     */
    static void
    ndbToAixm(Navaid                                   navaid,
              GregorianCalendar                        validStart,
              GregorianCalendar                        validEnd,
              String                                   interpretation,
              long                                     sequence,
              long                                     correction,
              List<BasicMessageMemberAIXMPropertyType> propList,
              List<AbstractNavaidEquipmentType>        equipmentList) {

        // the location
        ElevatedPointType ep = convertPointElevation(navaid,
                                                     navaid.getElevation());
        ElevatedPointPropertyType epp =
                                aixmFactory.createElevatedPointPropertyType();
        epp.setElevatedPoint(ep);

        // magnetic variation
        ValMagneticVariationType var     = null;
        DateYearType             varDate = null;
        if (navaid.getVariation() != null) {
            var = aixmFactory.createValMagneticVariationType();
            var.setValue(doubleToBigDecimal(
                                        navaid.getVariation().getVariation()));

            varDate = aixmFactory.createDateYearType();
            varDate.setValue(Integer.toString(navaid.getVariation().getYear()));
        }

        // the designator
        CodeNavaidDesignatorType des =
                                aixmFactory.createCodeNavaidDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace("urn:uuid:");
        id.setValue(UUID.randomUUID().toString());

        // the name
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(navaid.getName());

        // the frequency
        ValFrequencyType freq = convertFrequency(navaid.getFrequency());

        // package the things together
        NDBTimeSliceType slice = aixmFactory.createNDBTimeSliceType();
        initTimeSlice(slice, validStart, validEnd, interpretation,
                      sequence, correction);
        slice.setLocation(epp);
        slice.setMagneticVariation(var);
        slice.setDateMagneticVariation(varDate);
        slice.setDesignator(des);
        slice.setAixmName(name);
        slice.setFrequency(freq);

        NDBTimeSlicePropertyType sliceProp =
                                  aixmFactory.createNDBTimeSlicePropertyType();
        sliceProp.setNDBTimeSlice(slice);

        NDBType ndb = aixmFactory.createNDBType();
        ndb.getTimeSlice().add(sliceProp);
        ndb.setIdentifier(id);
        ndb.setId("uuid." + id.getValue());


        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createNDB(ndb));

        propList.add(prop);

        equipmentList.add(ndb);
    }

    /**
     * Convert a marker beakon into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     * @param equipmentList the generated navaid equipment will be put into
     *         this list
     */
    static void
    markerToAixm(Navaid                                   navaid,
                 GregorianCalendar                        validStart,
                 GregorianCalendar                        validEnd,
                 String                                   interpretation,
                 long                                     sequence,
                 long                                     correction,
                 List<BasicMessageMemberAIXMPropertyType> propList,
                 List<AbstractNavaidEquipmentType>        equipmentList) {

        // the location
        ElevatedPointType ep = convertPointElevation(navaid,
                                                     navaid.getElevation());
        ElevatedPointPropertyType epp =
                                aixmFactory.createElevatedPointPropertyType();
        epp.setElevatedPoint(ep);

        // magnetic variation
        ValMagneticVariationType var     = null;
        DateYearType             varDate = null;
        if (navaid.getVariation() != null) {
            var = aixmFactory.createValMagneticVariationType();
            var.setValue(doubleToBigDecimal(
                                        navaid.getVariation().getVariation()));

            varDate = aixmFactory.createDateYearType();
            varDate.setValue(Integer.toString(navaid.getVariation().getYear()));
        }

        // the designator
        CodeNavaidDesignatorType des =
                                aixmFactory.createCodeNavaidDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace("urn:uuid:");
        id.setValue(UUID.randomUUID().toString());

        // the name
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(navaid.getName());

        // the frequency
        ValFrequencyType freq = convertFrequency(navaid.getFrequency());

        // the aural code
        CodeAuralMorseType auralCode = aixmFactory.createCodeAuralMorseType();
        String auralStr = navaid.getIdent().replace("-", "")
                                           .replace("Dashes", "-")
                                           .replace("Dots", ".");
        auralCode.setValue(auralStr);

        // package the things together
        MarkerBeaconTimeSliceType slice =
                                aixmFactory.createMarkerBeaconTimeSliceType();
        initTimeSlice(slice, validStart, validEnd, interpretation,
                      sequence, correction);
        slice.setLocation(epp);
        slice.setMagneticVariation(var);
        slice.setDateMagneticVariation(varDate);
        slice.setDesignator(des);
        slice.setAixmName(name);
        slice.setFrequency(freq);
        slice.setAuralMorseCode(auralCode);

        MarkerBeaconTimeSlicePropertyType sliceProp =
                          aixmFactory.createMarkerBeaconTimeSlicePropertyType();
        sliceProp.setMarkerBeaconTimeSlice(slice);

        MarkerBeaconType marker = aixmFactory.createMarkerBeaconType();
        marker.getTimeSlice().add(sliceProp);
        marker.setIdentifier(id);
        marker.setId("uuid." + id.getValue());


        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createMarkerBeacon(marker));

        propList.add(prop);

        equipmentList.add(marker);
    }

    /**
     * Convert a designated point into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     */
    static void
    designatedToAixm(Navaid                                   navaid,
                     GregorianCalendar                        validStart,
                     GregorianCalendar                        validEnd,
                     String                                   interpretation,
                     long                                     sequence,
                     long                                     correction,
                     List<BasicMessageMemberAIXMPropertyType> propList) {

        // the location
        DirectPositionType p = convertPoint(navaid);

        PointType pt = aixmFactory.createPointType();
        pt.setSrsName("urn:ogc:def:crs:EPSG:4326");
        pt.setPos(p);
        pt.setId("uuid." + UUID.randomUUID().toString());

        PointPropertyType pp = aixmFactory.createPointPropertyType();
        pp.setPoint(aixmFactory.createPoint(pt));

        // the designator
        CodeDesignatedPointDesignatorType des =
                          aixmFactory.createCodeDesignatedPointDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace("urn:uuid:");
        id.setValue(UUID.randomUUID().toString());

        // the type
        CodeDesignatedPointType type =
                                    aixmFactory.createCodeDesignatedPointType();
        type.setValue("ICAO");

        // the name
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(navaid.getName());

        // package the things together
        DesignatedPointTimeSliceType slice =
                            aixmFactory.createDesignatedPointTimeSliceType();
        initTimeSlice(slice, validStart, validEnd, interpretation,
                      sequence, correction);
        slice.setLocation(pp);
        slice.setDesignator(des);
        slice.setType(type);
        slice.setAixmName(name);

        DesignatedPointTimeSlicePropertyType sliceProp =
                       aixmFactory.createDesignatedPointTimeSlicePropertyType();
        sliceProp.setDesignatedPointTimeSlice(slice);

        DesignatedPointType dpt = aixmFactory.createDesignatedPointType();
        dpt.getTimeSlice().add(sliceProp);
        dpt.setIdentifier(id);
        dpt.setId("uuid." + id.getValue());


        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createDesignatedPoint(dpt));

        propList.add(prop);
    }

    /**
     * Convert a localizer into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     * @param equipmentList the generated navaid equipment will be put into
     *         this list
     */
    static void
    localizerToAixm(Navaid                                   navaid,
                    GregorianCalendar                        validStart,
                    GregorianCalendar                        validEnd,
                    String                                   interpretation,
                    long                                     sequence,
                    long                                     correction,
                    List<BasicMessageMemberAIXMPropertyType> propList,
                    List<AbstractNavaidEquipmentType>        equipmentList) {

        // the location
        ElevatedPointType ep = convertPointElevation(navaid,
                                                     navaid.getElevation());
        ElevatedPointPropertyType epp =
                                aixmFactory.createElevatedPointPropertyType();
        epp.setElevatedPoint(ep);

        // the designator
        CodeNavaidDesignatorType des =
                                  aixmFactory.createCodeNavaidDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace("urn:uuid:");
        id.setValue(UUID.randomUUID().toString());

        // the name
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(navaid.getName());

        // the frequency
        ValFrequencyType freq = convertFrequency(navaid.getFrequency());

        // magnetic bearing
        ValBearingType bearing = aixmFactory.createValBearingType();
        bearing.setValue(doubleToBigDecimal(navaid.getAngle()));

        // get the decliation
        ValMagneticVariationType declination =
                                  aixmFactory.createValMagneticVariationType();
        declination.setValue(doubleToBigDecimal(navaid.getDeclination()));

        // get the magnetic variation
        ValMagneticVariationType variation =
                                aixmFactory.createValMagneticVariationType();
        DateYearType variationDate = aixmFactory.createDateYearType();
        if (navaid.getVariation() != null) {
            variation.setValue(doubleToBigDecimal(
                                        navaid.getVariation().getVariation()));
            variationDate.setValue(
                            Integer.toString(navaid.getVariation().getYear()));
        }

        // package the things together
        LocalizerTimeSliceType slice =
                                    aixmFactory.createLocalizerTimeSliceType();
        initTimeSlice(slice, validStart, validEnd, interpretation,
                      sequence, correction);
        slice.setLocation(epp);
        slice.setDesignator(des);
        slice.setAixmName(name);
        slice.setFrequency(freq);
        slice.setMagneticBearing(bearing);
        slice.setDeclination(declination);
        if (navaid.getVariation() != null) {
            slice.setMagneticVariation(variation);
            slice.setDateMagneticVariation(variationDate);
        }

        LocalizerTimeSlicePropertyType sliceProp =
                           aixmFactory.createLocalizerTimeSlicePropertyType();
        sliceProp.setLocalizerTimeSlice(slice);

        LocalizerType l = aixmFactory.createLocalizerType();
        l.getTimeSlice().add(sliceProp);
        l.setIdentifier(id);
        l.setId("uuid." + id.getValue());

        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createLocalizer(l));

        propList.add(prop);

        equipmentList.add(l);
    }

    /**
     * Convert a glidepath (ILS) into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     * @param equipmentList the generated navaid equipment will be put into
     *         this list
     */
    static void
    glidepathToAixm(Navaid                                   navaid,
                    GregorianCalendar                        validStart,
                    GregorianCalendar                        validEnd,
                    String                                   interpretation,
                    long                                     sequence,
                    long                                     correction,
                    List<BasicMessageMemberAIXMPropertyType> propList,
                    List<AbstractNavaidEquipmentType>        equipmentList) {

        // the location
        ElevatedPointType ep = convertPointElevation(navaid,
                                                     navaid.getElevation());
        ElevatedPointPropertyType epp =
                                aixmFactory.createElevatedPointPropertyType();
        epp.setElevatedPoint(ep);

        ValDistanceVerticalType elevation =
                                    convertElevation(navaid.getElevation());

        // the designator
        CodeNavaidDesignatorType des =
                                  aixmFactory.createCodeNavaidDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace("urn:uuid:");
        id.setValue(UUID.randomUUID().toString());

        // the name
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(navaid.getName());

        // the frequency
        ValFrequencyType freq = convertFrequency(navaid.getFrequency());

        // get the magnetic variation
        ValMagneticVariationType variation =
                                aixmFactory.createValMagneticVariationType();
        DateYearType variationDate = aixmFactory.createDateYearType();
        if (navaid.getVariation() != null) {
            variation.setValue(doubleToBigDecimal(
                                        navaid.getVariation().getVariation()));
            variationDate.setValue(
                            Integer.toString(navaid.getVariation().getYear()));
        }

        // package the things together
        GlidepathTimeSliceType slice =
                                    aixmFactory.createGlidepathTimeSliceType();
        initTimeSlice(slice, validStart, validEnd, interpretation,
                      sequence, correction);
        slice.setLocation(epp);
        slice.setRdh(elevation);
        slice.setDesignator(des);
        slice.setAixmName(name);
        slice.setFrequency(freq);
        if (navaid.getVariation() != null) {
            slice.setMagneticVariation(variation);
            slice.setDateMagneticVariation(variationDate);
        }

        GlidepathTimeSlicePropertyType sliceProp =
                           aixmFactory.createGlidepathTimeSlicePropertyType();
        sliceProp.setGlidepathTimeSlice(slice);

        GlidepathType g = aixmFactory.createGlidepathType();
        g.getTimeSlice().add(sliceProp);
        g.setIdentifier(id);
        g.setId("uuid." + id.getValue());

        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createGlidepath(g));

        propList.add(prop);

        equipmentList.add(g);
    }

    /**
     * Convert a single aerodrome into an AIXM AirportHeliport.
     *
     * @param aerodrome the aerodrome to convert
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     */
    static void
    aerodromeToAixm(Aerodrome                                aerodrome,
                    GregorianCalendar                        validStart,
                    GregorianCalendar                        validEnd,
                    String                                   interpretation,
                    long                                     sequence,
                    long                                     correction,
                    List<BasicMessageMemberAIXMPropertyType> propList) {

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace("urn:uuid:");
        id.setValue(UUID.randomUUID().toString());


        aerodromeNavaidsToAixm(aerodrome,
                               id.getValue(),
                               validStart,
                               validEnd,
                               interpretation,
                               sequence,
                               correction,
                               propList);

        airspacesToAixm(aerodrome.getAirspaces(),
                        validStart,
                        validEnd,
                        interpretation,
                        sequence,
                        correction,
                        propList);

        aerodromeFrequenciesToAixm(aerodrome,
                                   id.getValue(),
                                   validStart,
                                   validEnd,
                                   interpretation,
                                   sequence,
                                   correction,
                                   propList);

        aerodromeRunwaysToAixm(aerodrome,
                               id.getValue(),
                               validStart,
                               validEnd,
                               interpretation,
                               sequence,
                               correction,
                               propList);


        // set the aerodrome properties
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(aerodrome.getName());

        CodeAirportHeliportDesignatorType designator =
                        aixmFactory.createCodeAirportHeliportDesignatorType();
        designator.setValue(aerodrome.getIcao());

        CodeICAOType icao = aixmFactory.createCodeICAOType();
        icao.setValue(aerodrome.getIcao());

        CodeIATAType iata = aixmFactory.createCodeIATAType();
        iata.setValue(aerodrome.getIata());

        CodeAirportHeliportType type =
                                    aixmFactory.createCodeAirportHeliportType();
        type.setValue("AD");

        CodeYesNoType certifiedIcao = aixmFactory.createCodeYesNoType();
        certifiedIcao.setValue("yes");

        ValDistanceVerticalType elevation =
                                    convertElevation(aerodrome.getElevation());

        ElevatedPointType arp = convertPointElevation(aerodrome.getArp(),
                                                      aerodrome.getElevation());
        ElevatedPointPropertyType arpProp =
                                aixmFactory.createElevatedPointPropertyType();
        arpProp.setElevatedPoint(arp);

        // get the remarks as a note
        CodeNotePurposeType remarkPurpose =
                                        aixmFactory.createCodeNotePurposeType();
        remarkPurpose.setValue("REMARK");

        TextNoteType tn = aixmFactory.createTextNoteType();
        tn.setValue(aerodrome.getRemarks());

        LinguisticNoteType ln = aixmFactory.createLinguisticNoteType();
        ln.setNote(tn);

        LinguisticNotePropertyType lnp =
                                aixmFactory.createLinguisticNotePropertyType();
        lnp.setLinguisticNote(ln);

        NoteType remarks = aixmFactory.createNoteType();
        remarks.setPurpose(remarkPurpose);
        remarks.getTranslatedNote().add(lnp);

        NotePropertyType remarksProp = aixmFactory.createNotePropertyType();
        remarksProp.setNote(remarks);

        // package the things together
        AirportHeliportTimeSliceType slice =
                            aixmFactory.createAirportHeliportTimeSliceType();
        initTimeSlice(slice, validStart, validEnd, interpretation,
                      sequence, correction);
        slice.setAixmName(name);
        slice.setDesignator(designator);
        slice.setLocationIndicatorICAO(icao);
        slice.setType(type);
        slice.setCertifiedICAO(certifiedIcao);
        slice.setFieldElevation(elevation);
        slice.setARP(arpProp);
        slice.getAnnotation().add(remarksProp);

        AirportHeliportTimeSlicePropertyType sliceProp =
                       aixmFactory.createAirportHeliportTimeSlicePropertyType();
        sliceProp.setAirportHeliportTimeSlice(slice);


        AirportHeliportType ah = aixmFactory.createAirportHeliportType();
        ah.setIdentifier(id);
        ah.setId("uuid." + id.getValue());
        ah.getTimeSlice().add(sliceProp);

        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createAirportHeliport(ah));

        propList.add(prop);
    }

    /**
     * Convert the frequencies of an aerodrome into AIXM, creating ATCS
     * objects and linking the frequencies to the airport.
     *
     * @param aerodrome the aerodrome to convert the frequencies for
     * @param aerodromeId the id of the aerodrome
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     */
    private static void
    aerodromeFrequenciesToAixm(Aerodrome            aerodrome,
                               String               aerodromeId,
                               GregorianCalendar    validStart,
                               GregorianCalendar    validEnd,
                               String               interpretation,
                               long                sequence,
                               long                correction,
                           List<BasicMessageMemberAIXMPropertyType> propList) {
        // add the frequencies, if aplicable
        if (aerodrome.getAfis() != null) {
            commFrequencyToAixm(aerodrome.getAfis(),
                                aerodromeId,
                                aerodrome.getName() + " Info",
                                "AFIS",
                                validStart,
                                validEnd,
                                interpretation,
                                sequence,
                                correction,
                                propList);
        }

        if (aerodrome.getTower() != null) {
            commFrequencyToAixm(aerodrome.getTower(),
                                aerodromeId,
                                aerodrome.getName() + " Tower",
                                "TWR",
                                validStart,
                                validEnd,
                                interpretation,
                                sequence,
                                correction,
                                propList);
        }

        if (aerodrome.getAtis() != null) {
            commFrequencyToAixm(aerodrome.getAtis(),
                                aerodromeId,
                                aerodrome.getName() + " ATIS",
                                "ATIS",
                                validStart,
                                validEnd,
                                interpretation,
                                sequence,
                                correction,
                                propList);
        }

        if (aerodrome.getApproach() != null) {
            commFrequencyToAixm(aerodrome.getApproach(),
                                aerodromeId,
                                aerodrome.getName() + " Approach",
                                "APP",
                                validStart,
                                validEnd,
                                interpretation,
                                sequence,
                                correction,
                                propList);
        }

        if (aerodrome.getApron() != null) {
            commFrequencyToAixm(aerodrome.getApron(),
                                aerodromeId,
                                aerodrome.getName() + " Ground",
                                "GND",
                                validStart,
                                validEnd,
                                interpretation,
                                sequence,
                                correction,
                                propList);
        }
    }

    /**
     * Convert the navaids of an airodrome into AIXM, linking the navaids
     * to the aerodrome.
     *
     * @param aerodrome the aerodrome to convert the navaids from
     * @param aerodromeId the id of the aerodrome
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     */
    private static void
    aerodromeNavaidsToAixm(Aerodrome                aerodrome,
                           String                   aerodromeId,
                           GregorianCalendar        validStart,
                           GregorianCalendar        validEnd,
                           String                   interpretation,
                           long                    sequence,
                           long                    correction,
                           List<BasicMessageMemberAIXMPropertyType> propList) {

        // process the navaids associated with this aerodrome
        List<BasicMessageMemberAIXMPropertyType> navaids =
                            new Vector<BasicMessageMemberAIXMPropertyType>();

        navaidsToAixm(aerodrome.getNavaids(),
                      validStart,
                      validEnd,
                      interpretation,
                      sequence,
                      correction,
                      navaids);

        for (BasicMessageMemberAIXMPropertyType prop : navaids) {

            // set the 'servedAirport' property for each navaid
            if (prop.getAbstractAIXMFeature().getDeclaredType().
                                                   equals(NavaidType.class)) {
                AirportHeliportPropertyType ahp =
                        aixmFactory.createAirportHeliportPropertyType();
                ahp.setHref("#uuid." + aerodromeId);

                NavaidType n = (NavaidType)
                        prop.getAbstractAIXMFeature().getValue();

                for (NavaidTimeSlicePropertyType nSlice : n.getTimeSlice()) {
                    nSlice.getNavaidTimeSlice().getServedAirport().add(ahp);
                }
            }

            // associate for each designated point
            if (prop.getAbstractAIXMFeature().getDeclaredType().
                                           equals(DesignatedPointType.class)) {

                AirportHeliportPropertyType ahp =
                                aixmFactory.createAirportHeliportPropertyType();
                ahp.setHref("#uuid." + aerodromeId);

                DesignatedPointType d = (DesignatedPointType)
                                    prop.getAbstractAIXMFeature().getValue();

                for (DesignatedPointTimeSlicePropertyType dSlice
                                                          : d.getTimeSlice()) {
                    dSlice.getDesignatedPointTimeSlice().
                                                        setAirportHeliport(ahp);
                }
            }

            // add the property to the real propList
            propList.add(prop);
        }
    }

    /**
     * Convert a communication frequency into AIXM structures.
     *
     * @param frequenct the frequency that is the bases of the ATC
     * @param aerodromeId the id of the aerodrome this comm frequency serves
     * @param frequencyName the name of the frequency
     * @param frequencyType the type of the frequency (AFIS, TWR, etc)
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the generated airspace properties will be put into
     *        this list
     */
    private static void
    commFrequencyToAixm(Frequency                               frequency,
                        String                                  aerodromeId,
                        String                                  frequencyName,
                        String                                  frequencyType,
                        GregorianCalendar                       validStart,
                        GregorianCalendar                       validEnd,
                        String                                  interpretation,
                        long                                   sequence,
                        long                                   correction,
                        List<BasicMessageMemberAIXMPropertyType> propList) {

        RadioCommunicationChannelTimeSliceType commSlice =
                aixmFactory.createRadioCommunicationChannelTimeSliceType();

        initTimeSlice(commSlice, validStart, validEnd, interpretation,
                      sequence, correction);
        commSlice.setFrequencyReception(convertFrequency(frequency));
        commSlice.setFrequencyTransmission(convertFrequency(frequency));

        RadioCommunicationChannelTimeSlicePropertyType commSliceProp =
         aixmFactory.createRadioCommunicationChannelTimeSlicePropertyType();

        commSliceProp.setRadioCommunicationChannelTimeSlice(commSlice);

        CodeWithAuthorityType commId =
                                gmlFactory.createCodeWithAuthorityType();
        commId.setCodeSpace("urn:uuid:");
        commId.setValue(UUID.randomUUID().toString());

        RadioCommunicationChannelType comm =
                        aixmFactory.createRadioCommunicationChannelType();
        comm.setIdentifier(commId);
        comm.setId("uuid." + commId.getValue());
        comm.getTimeSlice().add(commSliceProp);

        BasicMessageMemberAIXMPropertyType prop =
              aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(
                        aixmFactory.createRadioCommunicationChannel(comm));

        propList.add(prop);


        // create the ATC object
        TextNameType fName = aixmFactory.createTextNameType();
        fName.setValue(frequencyName);

        CodeServiceATCType fType = aixmFactory.createCodeServiceATCType();
        fType.setValue(frequencyType);

        TextNameType callsignName = aixmFactory.createTextNameType();
        callsignName.setValue(frequencyName);
        CallsignDetailType callsign =
                                    aixmFactory.createCallsignDetailType();
        callsign.setCallSign(callsignName);

        CallsignDetailPropertyType callsignProp =
                            aixmFactory.createCallsignDetailPropertyType();
        callsignProp.setCallsignDetail(callsign);

        RadioCommunicationChannelPropertyType commProp =
                aixmFactory.createRadioCommunicationChannelPropertyType();
        commProp.setHref("#uuid." + commId.getValue());

        AirportHeliportPropertyType ahp =
                            aixmFactory.createAirportHeliportPropertyType();
        ahp.setHref("#uuid." + aerodromeId);

        AirTrafficControlServiceTimeSliceType atcsSlice =
                aixmFactory.createAirTrafficControlServiceTimeSliceType();

        initTimeSlice(atcsSlice, validStart, validEnd, interpretation,
                      sequence, correction);
        atcsSlice.setAixmName(fName);
        atcsSlice.getCallSign().add(callsignProp);
        atcsSlice.setType(fType);
        atcsSlice.getRadioCommunication().add(commProp);
        atcsSlice.getClientAirport().add(ahp);

        AirTrafficControlServiceTimeSlicePropertyType atcsSliceProp =
          aixmFactory.createAirTrafficControlServiceTimeSlicePropertyType();
        atcsSliceProp.setAirTrafficControlServiceTimeSlice(atcsSlice);

        AirTrafficControlServiceType atcs =
                          aixmFactory.createAirTrafficControlServiceType();
        atcs.getTimeSlice().add(atcsSliceProp);

        prop =
              aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(
                        aixmFactory.createAirTrafficControlService(atcs));

        propList.add(prop);
    }

    /**
     * Convert the runways of an aerodrome into AIXM.
     *
     * @param aerodrome the aerodrome to convert the frequencies for
     * @param aerodromeId the id of the aerodrome
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     */
    private static void
    aerodromeRunwaysToAixm(Aerodrome            aerodrome,
                           String               aerodromeId,
                           GregorianCalendar    validStart,
                           GregorianCalendar    validEnd,
                           String               interpretation,
                           long                sequence,
                           long                correction,
                           List<BasicMessageMemberAIXMPropertyType> propList) {

        // collect 'runways' into 2-packs of runway directions
        Map<String, Runway[]> rwys = runwayDirectionMap(aerodrome);

        // generate the AIXM runway elements, and get a map that points
        // each runway direction to the related AIXM runway
        Map<String, String> rwyDirRwyIdMap = new HashMap<String, String>();

        runwayToAixm(aerodromeId, rwys, rwyDirRwyIdMap,
                     validStart, validEnd, interpretation, sequence,
                     correction, propList);

        for (Runway runway : aerodrome.getRunways()) {

            // the unique id
            CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
            id.setCodeSpace("urn:uuid:");
            id.setValue(UUID.randomUUID().toString());

            // create the threshold & the end point
            runwayThresholdToAixm(runway, id.getValue(),
                                  validStart, validEnd, interpretation,
                                  sequence, correction, propList);
            runwayEndToAixm(runway, id.getValue(),
                            validStart, validEnd, interpretation,
                            sequence, correction, propList);

            // create the runway element
            String rwyEId = runwayElementToAixm(runway,
                                    rwyDirRwyIdMap.get(runway.getDesignator()),
                                    validStart, validEnd,
                                    interpretation, sequence,
                                    correction, propList);

            // set the runway direction properties
            TextDesignatorType designator =
                                        aixmFactory.createTextDesignatorType();
            designator.setValue(runway.getDesignator());

            ValBearingType mBearing = aixmFactory.createValBearingType();
            mBearing.setValue(doubleToBigDecimal(runway.getBearing()));

            ValSlopeType slope = aixmFactory.createValSlopeType();
            slope.setValue(doubleToBigDecimal(runway.getSlope()));

            ValDistanceVerticalType elevation =
                                    convertElevation(runway.getElevation());

            RunwayElementPropertyType rwyEProp =
                                aixmFactory.createRunwayElementPropertyType();
            rwyEProp.setHref("#uuid." + rwyEId);

            RunwayPropertyType rwyProp = aixmFactory.createRunwayPropertyType();
            rwyProp.setHref("#uuid."
                          + rwyDirRwyIdMap.get(runway.getDesignator()));

            RunwayDirectionTimeSliceType rdSlice =
                              aixmFactory.createRunwayDirectionTimeSliceType();

            initTimeSlice(rdSlice, validStart, validEnd, interpretation,
                          sequence, correction);

            rdSlice.setDesignator(designator);
            rdSlice.setMagneticBearing(mBearing);
            rdSlice.setSlopeTDZ(slope);
            rdSlice.setElevationTDZ(elevation);
            rdSlice.setStartingElement(rwyEProp);
            rdSlice.setUsedRunway(rwyProp);

            RunwayDirectionTimeSlicePropertyType rdSliceProp =
                    aixmFactory.createRunwayDirectionTimeSlicePropertyType();
            rdSliceProp.setRunwayDirectionTimeSlice(rdSlice);

            RunwayDirectionType rd = aixmFactory.createRunwayDirectionType();
            rd.getTimeSlice().add(rdSliceProp);
            rd.setId("uuid." + id.getValue());
            rd.setIdentifier(id);

            BasicMessageMemberAIXMPropertyType prop =
                  aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
            prop.setAbstractAIXMFeature(aixmFactory.createRunwayDirection(rd));

            propList.add(prop);
        }
    }

    /**
     * Convert a set of runways into an AIXM runway construct.
     *
     * @param aerodromeId the id of the aerodrome
     * @param rwys a map of runway identifiers and a 2-long array of
     *         related Runway objects
     * @param rwyDirRwyIdMap populate this map with a mapping from
     *         runway direction identifiers to related runway AIXM unique
     *         identifiers
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     */
    private static void
    runwayToAixm(String aerodromeId,
                 Map<String, Runway[]>                    rwys,
                 Map<String, String>                      rwyDirRwyIdMap,
                 GregorianCalendar                        validStart,
                 GregorianCalendar                        validEnd,
                 String                                   interpretation,
                 long                                     sequence,
                 long                                     correction,
                 List<BasicMessageMemberAIXMPropertyType> propList) {

        for (String rwyDesignator : rwys.keySet()) {
            // assume that both runway directions have the same parameters
            Runway rwy = rwys.get(rwyDesignator)[0];

            // the unique id
            CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
            id.setCodeSpace("urn:uuid:");
            id.setValue(UUID.randomUUID().toString());

            TextDesignatorType designator =
                                        aixmFactory.createTextDesignatorType();
            designator.setValue(rwyDesignator);

            CodeRunwayType type = aixmFactory.createCodeRunwayType();
            type.setValue("RWY");

            ValDistanceType length = convertDistance(rwy.getLength());
            ValDistanceType width  = convertDistance(rwy.getWidth());

            // handle the surface
            SurfaceCharacteristicsPropertyType surfaceProp = null;

            CodeSurfaceCompositionType sc = null;
            switch (rwy.getSurface()) {
            case ASPHALT:
                sc = aixmFactory.createCodeSurfaceCompositionType();
                sc.setValue("ASPTH");
                break;

            case GRASS:
                sc = aixmFactory.createCodeSurfaceCompositionType();
                sc.setValue("GRASS");

            default:
            }

            if (sc != null) {
                SurfaceCharacteristicsType surface =
                                aixmFactory.createSurfaceCharacteristicsType();
                surface.setComposition(sc);

                surfaceProp =
                        aixmFactory.createSurfaceCharacteristicsPropertyType();
                surfaceProp.setSurfaceCharacteristics(surface);
            }

            AirportHeliportPropertyType ahp =
                                aixmFactory.createAirportHeliportPropertyType();
            ahp.setHref("#uuid." + aerodromeId);

            RunwayTimeSliceType rSlice =
                                        aixmFactory.createRunwayTimeSliceType();

            initTimeSlice(rSlice, validStart, validEnd, interpretation,
                          sequence, correction);

            rSlice.setDesignator(designator);
            rSlice.setType(type);
            rSlice.setNominalLength(length);
            rSlice.setNominalWidth(width);

            RunwayTimeSlicePropertyType rSliceProp =
                              aixmFactory.createRunwayTimeSlicePropertyType();
            rSliceProp.setRunwayTimeSlice(rSlice);

            RunwayType r = aixmFactory.createRunwayType();
            r.getTimeSlice().add(rSliceProp);
            r.setId("uuid." + id.getValue());
            r.setIdentifier(id);

            BasicMessageMemberAIXMPropertyType prop =
                  aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
            prop.setAbstractAIXMFeature(aixmFactory.createRunway(r));

            propList.add(prop);

            // store the runway id in the map, to be used later
            rwyDirRwyIdMap.put(rwys.get(rwyDesignator)[0].getDesignator(),
                               id.getValue());
            rwyDirRwyIdMap.put(rwys.get(rwyDesignator)[1].getDesignator(),
                               id.getValue());
        }
    }

    /**
     * Identified AIXM runways from what are really runway directions.
     * Thus, from two 'runways', say 14 and 32, generate runway 14/32.
     * NOTE: this is ugly, and the original data model should be updated
     * instead
     *
     * @param aerodrome the aerodrome to identify the runways for
     * @return a map, with the identified runways as keys, and the two
     *          'runway directions' as values in a 2-member array
     */
    private static Map<String, Runway[]>
    runwayDirectionMap(Aerodrome aerodrome) {
        Map<String, Runway[]> rwys = new HashMap<String, Runway[]>();

        // collect 'runways' into 2-packs of runway directions
        for (Runway runway : aerodrome.getRunways()) {
            // NOTE: this assumes that all runways have both directions
            String  designator = runway.getDesignator();
            int     lastIx      = designator.length() - 1;
            char    lastChar    = designator.charAt(lastIx);
            boolean postfix    = Character.isLetter(lastChar);
            int     direction  = postfix
                             ? Integer.parseInt(designator.substring(0, lastIx))
                             : Integer.parseInt(designator);

            int    oppositeDirection = (direction + 18) % 36;
            if (oppositeDirection == 0) {
                oppositeDirection = 36;
            }
            char   oppositeLastChar  = ' ';
            if (postfix) {
                if ('L' == lastChar) {
                    oppositeLastChar = 'R';
                } else if ('R' == lastChar) {
                    oppositeLastChar = 'L';
                } else if ('C' == lastChar) {
                    oppositeLastChar = 'C';
                } else {
                    throw new RuntimeException("unrecognized runway postfix '"
                                              + lastChar + "'");
                }
            }

            String rwyDesignator;
            DecimalFormat fmt = new DecimalFormat("00");
            if (direction < oppositeDirection) {
                if (postfix) {
                    rwyDesignator = fmt.format(direction) + lastChar
                                  + "/"
                                  + fmt.format(oppositeDirection)
                                  + oppositeLastChar;
                } else {
                    rwyDesignator = fmt.format(direction)
                                  + "/"
                                  + fmt.format(oppositeDirection);
                }
            } else {
                if (postfix) {
                    rwyDesignator = fmt.format(oppositeDirection)
                                  + oppositeLastChar
                                  + "/"
                                  + fmt.format(direction) + lastChar;
                } else {
                    rwyDesignator = fmt.format(oppositeDirection)
                                  + "/"
                                  + fmt.format(direction);
                }
            }

            if (!rwys.containsKey(rwyDesignator)) {
                Runway[] relatedRwys = new Runway[2];
                rwys.put(rwyDesignator, relatedRwys);
            }

            if (direction < oppositeDirection) {
                rwys.get(rwyDesignator)[0] = runway;
            } else {
                rwys.get(rwyDesignator)[1] = runway;
            }
        }

        return rwys;
    }

    /**
     * Create a runway element AIXM construct for a runway.
     *
     * @param runway the runway to create the runway AIXM construct for.
     * @param runwayId the id of the AIXM runway that this element is part of
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     * @return the unique id of the created runway element
     */
    private static String
    runwayElementToAixm(Runway                                  runway,
                        String                                  runwayId,
                        GregorianCalendar                       validStart,
                        GregorianCalendar                       validEnd,
                        String                                  interpretation,
                        long                                    sequence,
                        long                                    correction,
                        List<BasicMessageMemberAIXMPropertyType> propList) {

        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace("urn:uuid:");
        id.setValue(UUID.randomUUID().toString());

        CodeRunwayElementType rwyEType =
                                    aixmFactory.createCodeRunwayElementType();
        rwyEType.setValue("NORMAL");

        ValDistanceType length = convertDistance(runway.getLength());
        ValDistanceType width  = convertDistance(runway.getWidth());

        SurfaceCharacteristicsPropertyType surfaceProp =
                                        convertSurface(runway.getSurface());

        RunwayPropertyType rwyProp = aixmFactory.createRunwayPropertyType();
        rwyProp.setHref("#uuid." + runwayId);

        // create the elevated surface that describes the runway
        ElevatedSurfacePropertyType esProp = runwayToElevatedSurface(runway);

        RunwayElementTimeSliceType rwyESlice =
                            aixmFactory.createRunwayElementTimeSliceType();
        initTimeSlice(rwyESlice, validStart, validEnd, interpretation,
                      sequence, correction);
        rwyESlice.setType(rwyEType);
        rwyESlice.setLength(length);
        rwyESlice.setWidth(width);
        rwyESlice.getAssociatedRunway().add(rwyProp);
        rwyESlice.setSurfaceProperties(surfaceProp);
        rwyESlice.setExtent(esProp);

        RunwayElementTimeSlicePropertyType rwyESliceProp =
                    aixmFactory.createRunwayElementTimeSlicePropertyType();
        rwyESliceProp.setRunwayElementTimeSlice(rwyESlice);

        RunwayElementType rwyE = aixmFactory.createRunwayElementType();
        rwyE.getTimeSlice().add(rwyESliceProp);
        rwyE.setIdentifier(id);
        rwyE.setId("uuid." + id.getValue());

        BasicMessageMemberAIXMPropertyType prop =
              aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createRunwayElement(rwyE));

        propList.add(prop);

        return id.getValue();
    }

    /**
     * Convert a runway to an AIXM elevated surface, that is, to the
     * runway surface area. This is done by calculating the coordinates
     * based on the runway threshold, width and length.
     *
     * @param runway the runway to convert
     * @return the surface area corresponding to the supplied runway.
     */
    private static ElevatedSurfacePropertyType
    runwayToElevatedSurface(Runway runway) {

        ValDistanceVerticalType elevation =
                                    convertElevation(runway.getElevation());

        DirectPositionListType directPosList =
                                    gmlFactory.createDirectPositionListType();

        // calculate the runway polygon
        runwayCalcPolygon(runway, directPosList);

        // put everything into a suitable AIXM construct
        LinearRingType ring = gmlFactory.createLinearRingType();
        ring.setPosList(directPosList);

        AbstractRingPropertyType abstractRingProp =
                                gmlFactory.createAbstractRingPropertyType();
        abstractRingProp.setAbstractRing(gmlFactory.createLinearRing(ring));

        PolygonPatchType polygonPatch = gmlFactory.createPolygonPatchType();
        polygonPatch.setExterior(abstractRingProp);

        SurfacePatchArrayPropertyType patchArray =
                          gmlFactory.createSurfacePatchArrayPropertyType();
        patchArray.getAbstractSurfacePatch().add(
                            gmlFactory.createPolygonPatch(polygonPatch));

        ValDistanceSignedType u = aixmFactory.createValDistanceSignedType();
        u.setValue(new BigDecimal(0));

        ElevatedSurfaceType es = aixmFactory.createElevatedSurfaceType();
        es.setSrsName("urn:ogc:def:crs:EPSG:4326");
        es.setGeoidUndulation(u);
        es.setElevation(elevation);
        es.setPatches(gmlFactory.createPatches(patchArray));
        es.setId("uuid." + UUID.randomUUID().toString());

        ElevatedSurfacePropertyType esProp =
                            aixmFactory.createElevatedSurfacePropertyType();
        esProp.setElevatedSurface(es);

        return esProp;
    }

    /**
     * Calculate the polygon of a runway, based on the threshold, width and
     * length.
     *
     * @param runway the runway to calculate the polygon for.
     * @param directPosList the position list to put the polygon into.
     */
    private static void
    runwayCalcPolygon(Runway                 runway,
                      DirectPositionListType directPosList) {

        // determine the real bearing & distance between threshold & end
        Point   thr  = runway.getThreshold();
        Point   end  = runway.getEnd();
        double dLat = thr.getLatitude() - end.getLatitude();
        double dLon = thr.getLongitude() - end.getLongitude();

        double dist  = Math.sqrt(dLat * dLat + dLon * dLon);
        double theta = 0;

        if (dLat == 0) {
            // the vertical difference is 0 - this is either 90 or 270 degrees
            theta = dLon < 0.0 ? Math.PI * 0.5 : Math.PI * 1.5;
        } else if (dLat > 0) {
            theta = Math.atan(dLon / dLat) + Math.PI;
        } else {
            theta = Math.atan(dLon / dLat);
        }

        // calculate the four edges of the runway in a north-south
        // orientation, like this, where T is the threshold:
        //
        //    b-----c
        //    |     |
        //    |     |
        //    a--T--d
        //
        double widthInNm   = runway.getWidth().inUom(UOM.NM).getDistance();
        double widthInDeg  = widthInNm / 60.0;

        Point a   = new Point();
        Point b   = new Point();
        Point c   = new Point();
        Point d   = new Point();

        a.setLatitude(thr.getLatitude());
        a.setLongitude(thr.getLongitude() - widthInDeg / 2.0);

        b.setLatitude(thr.getLatitude() + dist);
        b.setLongitude(thr.getLongitude() - widthInDeg / 2.0);

        c.setLatitude(thr.getLatitude() + dist);
        c.setLongitude(thr.getLongitude() + widthInDeg / 2.0);

        d.setLatitude(thr.getLatitude());
        d.setLongitude(thr.getLongitude() + widthInDeg / 2.0);

        // rotate the values according to the calculated bearing
        a = rotate(a, thr, theta);
        b = rotate(b, thr, theta);
        c = rotate(c, thr, theta);
        d = rotate(d, thr, theta);

        // add the values to the supplied position list
        directPosList.getValue().add(a.getLatitude());
        directPosList.getValue().add(a.getLongitude());

        directPosList.getValue().add(b.getLatitude());
        directPosList.getValue().add(b.getLongitude());

        directPosList.getValue().add(c.getLatitude());
        directPosList.getValue().add(c.getLongitude());

        directPosList.getValue().add(d.getLatitude());
        directPosList.getValue().add(d.getLongitude());

        directPosList.getValue().add(a.getLatitude());
        directPosList.getValue().add(a.getLongitude());
    }

    /**
     * Rotate a point around another point, and a specified number of degrees.
     *
     * @param p the point to rotate
     * @param o the point to rotate around
     * @param deg the number of degrees to rotate
     * @return the rotated point.
     */
    private static Point rotate(Point p, Point o, double deg) {
        Point a = new Point();

        // first, translate according to o
        a.setLatitude(p.getLatitude() - o.getLatitude());
        a.setLongitude(p.getLongitude() - o.getLongitude());

        // now, rotate
        double r    = deg; //Math.toRadians(deg);
        double cosR = Math.cos(r);
        double sinR = Math.sin(r);

        Point   b    = new Point();
        b.setLatitude(a.getLatitude() * cosR - a.getLongitude() * sinR);
        b.setLongitude(a.getLatitude() * sinR + a.getLongitude() * cosR);

        // lastly, translate back according to o
        a.setLatitude(b.getLatitude() + o.getLatitude());
        a.setLongitude(b.getLongitude() + o.getLongitude());

        return a;
    }

    /**
     * Convert the surface of a runway into an AIXM construct.
     *
     * @param surface the surface to convert
     * @return the AIXM surface characteristic property corresponding to the
     *          supplied surface
     */
    private static SurfaceCharacteristicsPropertyType
    convertSurface(org.openaviationmap.model.SurfaceType surface) {
        // handle the surface
        SurfaceCharacteristicsPropertyType surfaceProp = null;

        CodeSurfaceCompositionType sc = null;
        switch (surface) {
        case ASPHALT:
            sc = aixmFactory.createCodeSurfaceCompositionType();
            sc.setValue("ASPH");
            break;

        case GRASS:
            sc = aixmFactory.createCodeSurfaceCompositionType();
            sc.setValue("GRASS");

        default:
        }

        if (sc != null) {
            SurfaceCharacteristicsType sfc =
                            aixmFactory.createSurfaceCharacteristicsType();
            sfc.setComposition(sc);

            surfaceProp =
                    aixmFactory.createSurfaceCharacteristicsPropertyType();
            surfaceProp.setSurfaceCharacteristics(sfc);
        }

        return surfaceProp;
    }

    /**
     * Convert a runway threshold, with related information like TORA, TODA,
     * etc. into an AIXM construct.
     *
     * @param runway the runway to convert the threshold for
     * @param id the AIXM id of the runway
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     */
    private static void
    runwayThresholdToAixm(Runway                runway,
                          String                id,
                          GregorianCalendar     validStart,
                          GregorianCalendar     validEnd,
                          String                interpretation,
                          long                  sequence,
                          long                  correction,
                          List<BasicMessageMemberAIXMPropertyType> propList) {
        CodeRunwayPointRoleType role =
                            aixmFactory.createCodeRunwayPointRoleType();
        role.setValue("THR");

        ElevatedPointType loc = convertPointElevation(runway.getThreshold(),
                                                     runway.getElevation());
        ElevatedPointPropertyType locProp =
                            aixmFactory.createElevatedPointPropertyType();
        locProp.setElevatedPoint(loc);

        RunwayDirectionPropertyType rdp =
                            aixmFactory.createRunwayDirectionPropertyType();
        rdp.setHref("#uuid." + id);

        RunwayCentrelinePointTimeSliceType thresholdSlice =
                aixmFactory.createRunwayCentrelinePointTimeSliceType();
        initTimeSlice(thresholdSlice, validStart, validEnd, interpretation,
                      sequence, correction);
        thresholdSlice.setRole(role);
        thresholdSlice.setLocation(locProp);
        thresholdSlice.setOnRunway(rdp);

        if (runway.getAsda() != null) {
            RunwayDeclaredDistancePropertyType asdaProp =
                        convertDeclaredDistance(runway.getAsda(), "ASDA");

            thresholdSlice.getAssociatedDeclaredDistance().add(asdaProp);
        }

        if (runway.getLda() != null) {
            RunwayDeclaredDistancePropertyType asdaProp =
                        convertDeclaredDistance(runway.getLda(), "LDA");

            thresholdSlice.getAssociatedDeclaredDistance().add(asdaProp);
        }

        if (runway.getToda() != null) {
            RunwayDeclaredDistancePropertyType asdaProp =
                        convertDeclaredDistance(runway.getToda(), "TODA");

            thresholdSlice.getAssociatedDeclaredDistance().add(asdaProp);
        }

        if (runway.getTora() != null) {
            RunwayDeclaredDistancePropertyType asdaProp =
                        convertDeclaredDistance(runway.getTora(), "TORA");

            thresholdSlice.getAssociatedDeclaredDistance().add(asdaProp);
        }

        RunwayCentrelinePointTimeSlicePropertyType thrSliceProp =
             aixmFactory.createRunwayCentrelinePointTimeSlicePropertyType();
        thrSliceProp.setRunwayCentrelinePointTimeSlice(thresholdSlice);

        RunwayCentrelinePointType rct =
                            aixmFactory.createRunwayCentrelinePointType();
        rct.getTimeSlice().add(thrSliceProp);

        BasicMessageMemberAIXMPropertyType prop =
              aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(
                            aixmFactory.createRunwayCentrelinePoint(rct));

        propList.add(prop);
    }

    /**
     * Convert a runway endpont into an AIXM construct.
     *
     * @param runway the runway to convert the threshold for
     * @param id the AIXM id of the runway
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     */
    private static void
    runwayEndToAixm(Runway                runway,
                    String                id,
                    GregorianCalendar     validStart,
                    GregorianCalendar     validEnd,
                    String                interpretation,
                    long                  sequence,
                    long                  correction,
                    List<BasicMessageMemberAIXMPropertyType> propList) {
        CodeRunwayPointRoleType role =
                            aixmFactory.createCodeRunwayPointRoleType();
        role.setValue("END");

        ElevatedPointType loc = convertPointElevation(runway.getThreshold(),
                                                     runway.getElevation());
        ElevatedPointPropertyType locProp =
                            aixmFactory.createElevatedPointPropertyType();
        locProp.setElevatedPoint(loc);

        RunwayDirectionPropertyType rdp =
                            aixmFactory.createRunwayDirectionPropertyType();
        rdp.setHref("#uuid." + id);

        RunwayCentrelinePointTimeSliceType endSlice =
                aixmFactory.createRunwayCentrelinePointTimeSliceType();
        initTimeSlice(endSlice, validStart, validEnd, interpretation,
                      sequence, correction);
        endSlice.setRole(role);
        endSlice.setLocation(locProp);
        endSlice.setOnRunway(rdp);

        RunwayCentrelinePointTimeSlicePropertyType thrSliceProp =
             aixmFactory.createRunwayCentrelinePointTimeSlicePropertyType();
        thrSliceProp.setRunwayCentrelinePointTimeSlice(endSlice);

        RunwayCentrelinePointType rct =
                            aixmFactory.createRunwayCentrelinePointType();
        rct.getTimeSlice().add(thrSliceProp);

        BasicMessageMemberAIXMPropertyType prop =
              aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(
                            aixmFactory.createRunwayCentrelinePoint(rct));

        propList.add(prop);
    }

    /**
     * Convert a Distance object into an AIXM declared distance object.
     *
     * @param distance the distance to convert
     * @param type the type of declared distance to convert to
     * @return a declared distance corresponding to the supplied distance
     */
    private static RunwayDeclaredDistancePropertyType
    convertDeclaredDistance(Distance distance, String type) {
        CodeDeclaredDistanceType t =
                                aixmFactory.createCodeDeclaredDistanceType();
        t.setValue(type);

        ValDistanceType d = convertDistance(distance);

        RunwayDeclaredDistanceValueType dist =
                            aixmFactory.createRunwayDeclaredDistanceValueType();
        dist.setDistance(d);

        RunwayDeclaredDistanceValuePropertyType distProp =
                    aixmFactory.createRunwayDeclaredDistanceValuePropertyType();
        distProp.setRunwayDeclaredDistanceValue(dist);

        RunwayDeclaredDistanceType dd =
                                aixmFactory.createRunwayDeclaredDistanceType();
        dd.setType(t);
        dd.getDeclaredValue().add(distProp);

        RunwayDeclaredDistancePropertyType ddProp =
                        aixmFactory.createRunwayDeclaredDistancePropertyType();
        ddProp.setRunwayDeclaredDistance(dd);

        return ddProp;
    }

    /**
     * Initialize a time slice property.
     *
     * @param slice the time slice to initialize
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     */
    private static void
    initTimeSlice(AbstractAIXMTimeSliceType slice,
                  GregorianCalendar         validStart,
                  GregorianCalendar         validEnd,
                  String                    interpretation,
                  long                      sequence,
                  long                      correction) {

        slice.setInterpretation(interpretation);
        slice.setSequenceNumber(sequence);
        slice.setCorrectionNumber(correction);
        slice.setValidTime(convertTimePeriod(validStart, validEnd));
        slice.setId("uuid." + UUID.randomUUID().toString());
    }

    /**
     * Convert a distance value into an AIXM ValDistnaceType.
     *
     * @param distance the distance to convert
     * @return the ValDistanceType corresponding to the supplied distance.
     */
    private static ValDistanceType
    convertDistance(Distance distance) {
        ValDistanceType dist = aixmFactory.createValDistanceType();

        dist.setValue(doubleToBigDecimal(distance.getDistance()));
        dist.setUom(convertUom(distance.getUom()));

        return dist;
    }

    /**
     * Convert an UOM object to a correct AIXM string representation.
     *
     * @param uom the uom object to convert
     * @return a correct AIXM string representation.
     */
    private static String convertUom(UOM uom) {
        switch (uom) {
        case FL:
            return "FL";
        case FT:
            return "FT";
        case M:
            return "M";
        case NM:
            return "NM";
        default:
            return "";
        }
    }

    /**
     * Convert a Point to a GML DirectPositionType.
     *
     * @param point the point to convert
     * @return a direct position type corresponding to the supplied point
     */
    private static DirectPositionType
    convertPoint(Point point) {
        DirectPositionType pos = gmlFactory.createDirectPositionType();
        pos.getValue().add(point.getLatitude());
        pos.getValue().add(point.getLongitude());

        return pos;
    }

    /**
     * Convert an Elevation object into an AIXM vertical distance type.
     *
     * @param elevation the elevation object to convert
     * @return a corresponding vertical distance type
     */
    private static ValDistanceVerticalType
    convertElevation(Elevation elevation) {
        if (elevation == null) {
            return null;
        }

        ValDistanceVerticalType elev =
                                aixmFactory.createValDistanceVerticalType();
        elev.setValue(Double.toString(elevation.getElevation()));
        elev.setUom(convertUom(elevation.getUom()));

        return elev;
    }

    /**
     * Convert a point and an elevation value into an AIXM elevated point.
     *
     * @param point the coordinates of the elevated point
     * @param elevation the elevation of the elevated point
     * @return an AIXM elevated point corresponding to the supplied
     *          parameters.
     */
    private static ElevatedPointType
    convertPointElevation(Point point, Elevation elevation) {
        DirectPositionType p = convertPoint(point);

        ValDistanceVerticalType elev = null;
        ValDistanceSignedType   u    = null;

        if (elevation != null) {
            elev = convertElevation(elevation);

            u = aixmFactory.createValDistanceSignedType();
            u.setValue(new BigDecimal(0));
            u.setUom(elev.getUom());
        }

        ElevatedPointType ep = aixmFactory.createElevatedPointType();
        ep.setSrsName("urn:ogc:def:crs:EPSG:4326");
        ep.setPos(p);
        ep.setElevation(elev);
        ep.setGeoidUndulation(u);
        ep.setId("uuid." + UUID.randomUUID().toString());

        return ep;
    }

    /**
     * Convert a double to a big decimal.
     *
     * @param d the double value to convert.
     * @return a big decimal, representing the supplied double.
     */
    private static BigDecimal
    doubleToBigDecimal(double d) {
        return new BigDecimal(d, MathContext.DECIMAL64);
    }

    /**
     * Convert a frequency to an appropriate AIXM representation.
     *
     * @param frequency the frequency to convert
     * @return the AIXM representation of the frequency
     */
    private static ValFrequencyType convertFrequency(Frequency frequency) {
        ValFrequencyType freq      = aixmFactory.createValFrequencyType();
        double           f         = frequency.getFrequency();

        if (f < 1000.0) {
            freq.setValue(doubleToBigDecimal(f));
            freq.setUom("Hz");
        } else if (f < 1000000.0) {
            freq.setValue(doubleToBigDecimal(f / 1000.0));
            freq.setUom("kHz");
        } else if (f < 1000000000.0) {
            freq.setValue(doubleToBigDecimal(f / 1000000.0));
            freq.setUom("MHz");
        } else if (f < 1000000000000.0) {
            freq.setValue(doubleToBigDecimal(f / 1000000000.0));
            freq.setUom("GHz");
        } else {
            freq.setValue(doubleToBigDecimal(f));
            freq.setUom("Hz");
        }

        return freq;
    }

    /**
     * Convert a VOR/DME into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     */
    static void
    vordmeToAixm(Navaid                                   navaid,
                 GregorianCalendar                        validStart,
                 GregorianCalendar                        validEnd,
                 String                                   interpretation,
                 long                                     sequence,
                 long                                     correction,
                 List<BasicMessageMemberAIXMPropertyType> propList,
                 List<AbstractNavaidEquipmentType>        equipmentList) {

        vorToAixm(navaid,
                  validStart,
                  validEnd,
                  interpretation,
                  sequence,
                  correction,
                  propList,
                  equipmentList);

        dmeToAixm(navaid,
                  validStart,
                  validEnd,
                  interpretation,
                  sequence,
                  correction,
                  propList,
                  equipmentList);
    }

    /**
     * Create an AIXM navaid construct for a number navaid and a number of
     * navaid equipment objects.
     *
     * @param navaid the navigation aid to convert
     * @param type the AIXM navaid object type
     * @param equipmentList the navaid equipments associated with this navaid
     * @param validStart the beginning of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerospace features. if null, this is unknown.
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted AIXM elements will be put into this list
     */
    private static void
    navaidForNavaidEquipment(Navaid                             navaid,
                             String                             type,
                             List<AbstractNavaidEquipmentType>  equipmentList,
                             GregorianCalendar                  validStart,
                             GregorianCalendar                  validEnd,
                             String                             interpretation,
                             long                               sequence,
                             long                               correction,
                            List<BasicMessageMemberAIXMPropertyType> propList) {
        // now create a navaid object that refers to the VOR & DME
        // the location
        ElevatedPointType ep = convertPointElevation(navaid,
                                                     navaid.getElevation());
        ElevatedPointPropertyType epp =
                                aixmFactory.createElevatedPointPropertyType();
        epp.setElevatedPoint(ep);

        // the designator
        CodeNavaidDesignatorType des =
                                aixmFactory.createCodeNavaidDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace("urn:uuid:");
        id.setValue(UUID.randomUUID().toString());

        // the type
        CodeNavaidServiceType t = aixmFactory.createCodeNavaidServiceType();
        t.setValue(type);

        // the name
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(navaid.getName());

        // package the things together
        NavaidTimeSliceType slice = aixmFactory.createNavaidTimeSliceType();
        initTimeSlice(slice, validStart, validEnd, interpretation,
                      sequence, correction);
        slice.setLocation(epp);
        slice.setDesignator(des);
        slice.setType(t);
        slice.setAixmName(name);

        // the add references to the related navaid equipment
        for (AbstractNavaidEquipmentType eq : equipmentList) {
            NavaidEquipmentPropertyType pe =
                                aixmFactory.createNavaidEquipmentPropertyType();
            pe.setHref("#uuid." + eq.getIdentifier().getValue());

            NavaidComponentType nt = aixmFactory.createNavaidComponentType();
            nt.setTheNavaidEquipment(pe);

            NavaidComponentPropertyType p =
                            aixmFactory.createNavaidComponentPropertyType();
            p.setNavaidComponent(nt);

            slice.getNavaidEquipment().add(p);
        }

        NavaidTimeSlicePropertyType sliceProp =
                                aixmFactory.createNavaidTimeSlicePropertyType();
        sliceProp.setNavaidTimeSlice(slice);

        NavaidType n = aixmFactory.createNavaidType();
        n.getTimeSlice().add(sliceProp);
        n.setIdentifier(id);
        n.setId("uuid." + id.getValue());


        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createNavaid(n));

        propList.add(prop);
    }

    /**
     * Convert a list of navaids into a list of AIXM abstract features.
     *
     * @param navaids the navigation aids to convert
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param validStart the beginning of the validity period for the
     *        navaid features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        navaid features. if null, this is unknown.
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted navigation aids will be put into
     *        this list
     */
    public static void
    navaidsToAixm(List<Navaid>                             navaids,
                  GregorianCalendar                        validStart,
                  GregorianCalendar                        validEnd,
                  String                                   interpretation,
                  long                                     sequence,
                  long                                     correction,
                  List<BasicMessageMemberAIXMPropertyType> propList) {

        for (Navaid navaid : navaids) {
            navaidToAixm(navaid,
                         validStart,
                         validEnd,
                         interpretation,
                         sequence,
                         correction,
                         propList);
        }
    }

    /**
     * Convert a list of aerodromes into a list of AIXM abstract features.
     *
     * @param aerodromes the aerodromes to convert
     * @param interpretation the AIXM time slice interpretation to set.
     *        if in doubt, use "BASELINE"
     * @param validStart the beginning of the validity period for the
     *        aerodrome features. if null, this is unknown.
     * @param validEnd the end of the validity period for the
     *        aerodrome features. if null, this is unknown.
     * @param sequence the AIXM time slice sequence number,
     *        if in doubt, specify 1
     * @param correction the AIXM time slice correction number,
     *        if in doubt, use  0
     * @param propList the converted aerodromes will be put into
     *        this list
     */
    public static void
    aerodromesToAixm(List<Aerodrome>                          aerodromes,
                     GregorianCalendar                        validStart,
                     GregorianCalendar                        validEnd,
                     String                                   interpretation,
                     long                                     sequence,
                     long                                     correction,
                     List<BasicMessageMemberAIXMPropertyType> propList) {

        for (Aerodrome aerodrome : aerodromes) {
            aerodromeToAixm(aerodrome,
                            validStart,
                            validEnd,
                            interpretation,
                            sequence,
                            correction,
                            propList);
        }
    }
}
