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

import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Boundary;
import hu.tyrell.openaviationmap.model.Circle;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.Frequency;
import hu.tyrell.openaviationmap.model.Navaid;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.Ring;
import hu.tyrell.openaviationmap.model.UOM;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;

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
import net.opengis.gml._3.PolygonPatchType;
import net.opengis.gml._3.RingType;
import net.opengis.gml._3.SurfacePatchArrayPropertyType;
import net.opengis.gml._3.TimeIndeterminateValueType;
import net.opengis.gml._3.TimePeriodType;
import net.opengis.gml._3.TimePositionType;
import net.opengis.gml._3.TimePrimitivePropertyType;
import aero.aixm.schema._5.AirspaceActivationPropertyType;
import aero.aixm.schema._5.AirspaceActivationType;
import aero.aixm.schema._5.AirspaceGeometryComponentPropertyType;
import aero.aixm.schema._5.AirspaceGeometryComponentType;
import aero.aixm.schema._5.AirspaceTimeSlicePropertyType;
import aero.aixm.schema._5.AirspaceTimeSliceType;
import aero.aixm.schema._5.AirspaceType;
import aero.aixm.schema._5.AirspaceVolumePropertyType;
import aero.aixm.schema._5.AirspaceVolumeType;
import aero.aixm.schema._5.CodeAirspaceActivityType;
import aero.aixm.schema._5.CodeAirspaceDesignatorType;
import aero.aixm.schema._5.CodeAirspaceType;
import aero.aixm.schema._5.CodeDMEChannelType;
import aero.aixm.schema._5.CodeDMEType;
import aero.aixm.schema._5.CodeDayType;
import aero.aixm.schema._5.CodeDesignatedPointDesignatorType;
import aero.aixm.schema._5.CodeDesignatedPointType;
import aero.aixm.schema._5.CodeMilitaryOperationsType;
import aero.aixm.schema._5.CodeNavaidDesignatorType;
import aero.aixm.schema._5.CodeNavaidServiceType;
import aero.aixm.schema._5.CodeNotePurposeType;
import aero.aixm.schema._5.CodeTimeReferenceType;
import aero.aixm.schema._5.CodeVORType;
import aero.aixm.schema._5.CodeVerticalReferenceType;
import aero.aixm.schema._5.CodeYesNoType;
import aero.aixm.schema._5.DMETimeSlicePropertyType;
import aero.aixm.schema._5.DMETimeSliceType;
import aero.aixm.schema._5.DMEType;
import aero.aixm.schema._5.DateYearType;
import aero.aixm.schema._5.DesignatedPointTimeSlicePropertyType;
import aero.aixm.schema._5.DesignatedPointTimeSliceType;
import aero.aixm.schema._5.DesignatedPointType;
import aero.aixm.schema._5.ElevatedPointPropertyType;
import aero.aixm.schema._5.ElevatedPointType;
import aero.aixm.schema._5.LinguisticNotePropertyType;
import aero.aixm.schema._5.LinguisticNoteType;
import aero.aixm.schema._5.NDBTimeSlicePropertyType;
import aero.aixm.schema._5.NDBTimeSliceType;
import aero.aixm.schema._5.NDBType;
import aero.aixm.schema._5.NavaidComponentPropertyType;
import aero.aixm.schema._5.NavaidComponentType;
import aero.aixm.schema._5.NavaidEquipmentPropertyType;
import aero.aixm.schema._5.NavaidTimeSlicePropertyType;
import aero.aixm.schema._5.NavaidTimeSliceType;
import aero.aixm.schema._5.NavaidType;
import aero.aixm.schema._5.NotePropertyType;
import aero.aixm.schema._5.NoteType;
import aero.aixm.schema._5.PointPropertyType;
import aero.aixm.schema._5.PointType;
import aero.aixm.schema._5.SurfacePropertyType;
import aero.aixm.schema._5.SurfaceType;
import aero.aixm.schema._5.TextNameType;
import aero.aixm.schema._5.TextNoteType;
import aero.aixm.schema._5.TimeType;
import aero.aixm.schema._5.TimesheetPropertyType;
import aero.aixm.schema._5.TimesheetType;
import aero.aixm.schema._5.VORTimeSlicePropertyType;
import aero.aixm.schema._5.VORTimeSliceType;
import aero.aixm.schema._5.VORType;
import aero.aixm.schema._5.ValDistanceSignedType;
import aero.aixm.schema._5.ValDistanceVerticalType;
import aero.aixm.schema._5.ValFrequencyType;
import aero.aixm.schema._5.ValMagneticVariationType;
import aero.aixm.schema._5_1.message.AIXMBasicMessageType;
import aero.aixm.schema._5_1.message.BasicMessageMemberAIXMPropertyType;

/**
 * Class to convert aviation data into AIXM.
 */
public final class AixmConverter {
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
     * Convert a set of aerial information into an AIXM message document.
     *
     * @param airspaces the airspaces to convert
     * @param navaids the navigation aids to convert
     * @param messageId the unique id of the generated AIXM message
     * @param codeSpace the code space to use when generating ids.
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
                  String             messageId,
                  String             codeSpace,
                  GregorianCalendar  validStart,
                  GregorianCalendar  validEnd,
                  String             interpretation,
                  long               sequence,
                  long               correction) {

        AIXMBasicMessageType message =
                aixmMessageFactory.createAIXMBasicMessageType();


        airspacesToAixm(airspaces,
                        codeSpace,
                        validStart,
                        validEnd,
                        interpretation,
                        sequence,
                        correction,
                        message.getHasMember());

        navaidsToAixm(navaids,
                      codeSpace,
                      validStart,
                      validEnd,
                      interpretation,
                      sequence,
                      correction,
                      message.getHasMember());

        JAXBElement<AIXMBasicMessageType> m =
                            aixmMessageFactory.createAIXMBasicMessage(message);
        m.getValue().setId(messageId);

        return m;
    }

    /**
     * Convert a single airspace into an AIXM airspace.
     *
     * @param airspace the airspace to convert
     * @param codeSpace the code space to use when generating ids.
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
                   String                                   codeSpace,
                   GregorianCalendar                        validStart,
                   GregorianCalendar                        validEnd,
                   String                                   interpretation,
                   long                                     sequence,
                   long                                     correction,
                   List<BasicMessageMemberAIXMPropertyType> propList) {

        String idBase = airspace.getDesignator() != null
                      ? codeSpace + ":" + airspace.getDesignator() + ":"
                      : codeSpace + ":"
                                  + airspace.getName().replaceAll(" ", "")
                                  + ":";
        int    idIx   = 1;

        AirspaceType at = aixmFactory.createAirspaceType();
        at.setId(idBase + Integer.toString(idIx++));

        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace(codeSpace);
        id.setValue(airspace.getDesignator() != null
                                              ? airspace.getDesignator()
                                              : airspace.getName());
        at.setIdentifier(id);

        AirspaceTimeSlicePropertyType sliceProp =
                            aixmFactory.createAirspaceTimeSlicePropertyType();
        AirspaceTimeSliceType slice = aixmFactory.createAirspaceTimeSliceType();
        slice.setId(idBase + Integer.toString(idIx++));

        // set the validity time period
        TimePrimitivePropertyType validTP =
                                        convertTimePeriod(validStart, validEnd);

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

        slice.setInterpretation(interpretation);
        slice.setSequenceNumber(sequence);
        slice.setCorrectionNumber(correction);

        // add the time slice to the airspace
        slice.setValidTime(validTP);
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

            DirectPositionListType directPosList =
                                    gmlFactory.createDirectPositionListType();
            for (Point p : r.getPointList()) {
                directPosList.getValue().add(p.getLatitude());
                directPosList.getValue().add(p.getLongitude());
            }

            GeodesicStringType geoString =
                                        gmlFactory.createGeodesicStringType();
            geoString.setPosList(directPosList);

            curveSegment.getAbstractCurveSegment().add(
                            gmlFactory.createGeodesicString(geoString));
            break;

        case CIRCLE:
            Circle c = (Circle) boundary;

            DirectPositionType center = gmlFactory.createDirectPositionType();
            center.getValue().add(c.getCenter().getLatitude());
            center.getValue().add(c.getCenter().getLongitude());

            LengthType radius = gmlFactory.createLengthType();
            radius.setUom(convertUom(c.getRadius().getUom()));
            radius.setValue(c.getRadius().getDistance());

            CircleByCenterPointType ccp =
                                    gmlFactory.createCircleByCenterPointType();
            ccp.setPos(center);
            ccp.setRadius(radius);

            curveSegment.getAbstractCurveSegment().add(
                              gmlFactory.createCircleByCenterPoint(ccp));
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
     * Convert a list of airspaces into a list of AIXM Airspaces.
     *
     * @param airspaces the airspaces to convert
     * @param codeSpace the code space to use when generating ids.
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
                    String                                   codeSpace,
                    GregorianCalendar                        validStart,
                    GregorianCalendar                        validEnd,
                    String                                   interpretation,
                    long                                     sequence,
                    long                                     correction,
                    List<BasicMessageMemberAIXMPropertyType> propList) {

        for (Airspace ap : airspaces) {
            airspaceToAixm(ap,
                           codeSpace,
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
     * @param codeSpace the code space to use when generating ids.
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
                 String                                   codeSpace,
                 GregorianCalendar                        validStart,
                 GregorianCalendar                        validEnd,
                 String                                   interpretation,
                 long                                     sequence,
                 long                                     correction,
                 List<BasicMessageMemberAIXMPropertyType> propList) {

        switch (navaid.getType()) {
        case VORDME:
            vordmeToAixm(navaid,
                         codeSpace,
                         validStart,
                         validEnd,
                         interpretation,
                         sequence,
                         correction,
                         propList);
            break;

        case VOR:
            vorToAixm(navaid,
                      codeSpace,
                      validStart,
                      validEnd,
                      interpretation,
                      sequence,
                      correction,
                      propList);
            break;

        case DME:
            dmeToAixm(navaid,
                      codeSpace,
                      validStart,
                      validEnd,
                      interpretation,
                      sequence,
                      correction,
                      propList);
            break;

        case NDB:
            ndbToAixm(navaid,
                      codeSpace,
                      validStart,
                      validEnd,
                      interpretation,
                      sequence,
                      correction,
                      propList);
            break;

        case DESIGNATED:
            designatedToAixm(navaid,
                             codeSpace,
                             validStart,
                             validEnd,
                             interpretation,
                             sequence,
                             correction,
                             propList);
          break;

        default:
        }
    }

    /**
     * Convert a VOR into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param codeSpace the code space to use when generating ids.
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
    vorToAixm(Navaid                                   navaid,
              String                                   codeSpace,
              GregorianCalendar                        validStart,
              GregorianCalendar                        validEnd,
              String                                   interpretation,
              long                                     sequence,
              long                                     correction,
              List<BasicMessageMemberAIXMPropertyType> propList) {

        String idBase = codeSpace + ":VOR:" + navaid.getIdent() + ":";
        int    idIx   = 1;

        // the location
        DirectPositionType p = gmlFactory.createDirectPositionType();
        p.getValue().add(navaid.getLatitude());
        p.getValue().add(navaid.getLongitude());

        ValDistanceVerticalType elev = convertElevation(navaid.getElevation());

        ValDistanceSignedType u = aixmFactory.createValDistanceSignedType();
        u.setValue(new BigDecimal(0));
        u.setUom(elev.getUom());

        ElevatedPointType ep = aixmFactory.createElevatedPointType();
        ep.setSrsName("urn:ogc:def:crs:EPSG:4326");
        ep.setPos(p);
        ep.setElevation(elev);
        ep.setGeoidUndulation(u);
        ep.setId(idBase + Integer.toString(idIx++));

        ElevatedPointPropertyType epp =
                                aixmFactory.createElevatedPointPropertyType();
        epp.setElevatedPoint(ep);

        // magnetic variation
        ValMagneticVariationType var     = null;
        DateYearType             varDate = null;
        if (navaid.getVariation() != null) {
            var = aixmFactory.createValMagneticVariationType();
            var.setValue(new BigDecimal(navaid.getVariation().getVariation(),
                                        MathContext.DECIMAL64));

            varDate = aixmFactory.createDateYearType();
            varDate.setValue(Integer.toString(navaid.getVariation().getYear()));
        }

        // the declination
        // magnetic variation
        ValMagneticVariationType decl     = null;
        if (navaid.getDeclination() != 0) {
            decl = aixmFactory.createValMagneticVariationType();
            decl.setValue(new BigDecimal(navaid.getDeclination(),
                                         MathContext.DECIMAL64));
        }

        // the designator
        CodeNavaidDesignatorType des =
                                aixmFactory.createCodeNavaidDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace(codeSpace);
        id.setValue("VOR:" + navaid.getIdent());

        // the type
        CodeVORType type = aixmFactory.createCodeVORType();
        // TODO: set DVOR or VOT if applicable
        type.setValue("VOR");

        // the name
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(navaid.getName());

        // the frequency
        ValFrequencyType freq = convertFrequency(navaid.getFrequency());

        // package the things together
        VORTimeSliceType slice = aixmFactory.createVORTimeSliceType();
        slice.setLocation(epp);
        slice.setMagneticVariation(var);
        slice.setDateMagneticVariation(varDate);
        slice.setDeclination(decl);
        slice.setDesignator(des);
        slice.setInterpretation(interpretation);
        slice.setSequenceNumber(sequence);
        slice.setCorrectionNumber(correction);
        slice.setValidTime(convertTimePeriod(validStart, validEnd));
        slice.setType(type);
        slice.setAixmName(name);
        slice.setFrequency(freq);
        slice.setId(idBase + Integer.toString(idIx++));

        VORTimeSlicePropertyType sliceProp =
                                  aixmFactory.createVORTimeSlicePropertyType();
        sliceProp.setVORTimeSlice(slice);

        VORType vor = aixmFactory.createVORType();
        vor.getTimeSlice().add(sliceProp);
        vor.setIdentifier(id);
        vor.setId(idBase);


        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createVOR(vor));

        propList.add(prop);
    }

    /**
     * Convert a DME into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param codeSpace the code space to use when generating ids.
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
    dmeToAixm(Navaid                                   navaid,
              String                                   codeSpace,
              GregorianCalendar                        validStart,
              GregorianCalendar                        validEnd,
              String                                   interpretation,
              long                                     sequence,
              long                                     correction,
              List<BasicMessageMemberAIXMPropertyType> propList) {

        String idBase = codeSpace + ":DME:" + navaid.getIdent() + ":";
        int    idIx   = 1;

        // the location
        DirectPositionType p = gmlFactory.createDirectPositionType();
        p.getValue().add(navaid.getLatitude());
        p.getValue().add(navaid.getLongitude());

        ValDistanceVerticalType elev = convertElevation(navaid.getElevation());

        ValDistanceSignedType u = aixmFactory.createValDistanceSignedType();
        u.setValue(new BigDecimal(0));
        u.setUom(elev.getUom());

        ElevatedPointType ep = aixmFactory.createElevatedPointType();
        ep.setSrsName("urn:ogc:def:crs:EPSG:4326");
        ep.setPos(p);
        ep.setElevation(elev);
        ep.setGeoidUndulation(u);
        ep.setId(idBase + Integer.toString(idIx++));

        ElevatedPointPropertyType epp =
                                aixmFactory.createElevatedPointPropertyType();
        epp.setElevatedPoint(ep);

        // magnetic variation
        ValMagneticVariationType var     = null;
        DateYearType             varDate = null;
        if (navaid.getVariation() != null) {
            var = aixmFactory.createValMagneticVariationType();
            var.setValue(new BigDecimal(navaid.getVariation().getVariation(),
                                        MathContext.DECIMAL64));

            varDate = aixmFactory.createDateYearType();
            varDate.setValue(Integer.toString(navaid.getVariation().getYear()));
        }

        // the designator
        CodeNavaidDesignatorType des =
                                aixmFactory.createCodeNavaidDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace(codeSpace);
        id.setValue("DME:" + navaid.getIdent());

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
        slice.setLocation(epp);
        slice.setMagneticVariation(var);
        slice.setDateMagneticVariation(varDate);
        slice.setDesignator(des);
        slice.setInterpretation(interpretation);
        slice.setSequenceNumber(sequence);
        slice.setCorrectionNumber(correction);
        slice.setValidTime(convertTimePeriod(validStart, validEnd));
        slice.setType(type);
        slice.setAixmName(name);
        slice.setChannel(channel);
        slice.setId(idBase + Integer.toString(idIx++));

        DMETimeSlicePropertyType sliceProp =
                                  aixmFactory.createDMETimeSlicePropertyType();
        sliceProp.setDMETimeSlice(slice);

        DMEType dme = aixmFactory.createDMEType();
        dme.getTimeSlice().add(sliceProp);
        dme.setIdentifier(id);
        dme.setId(idBase);


        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createDME(dme));

        propList.add(prop);
    }

    /**
     * Convert an NDB into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param codeSpace the code space to use when generating ids.
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
    ndbToAixm(Navaid                                   navaid,
              String                                   codeSpace,
              GregorianCalendar                        validStart,
              GregorianCalendar                        validEnd,
              String                                   interpretation,
              long                                     sequence,
              long                                     correction,
              List<BasicMessageMemberAIXMPropertyType> propList) {

        String idBase = codeSpace + ":NDB:" + navaid.getIdent() + ":";
        int    idIx   = 1;

        // the location
        DirectPositionType p = gmlFactory.createDirectPositionType();
        p.getValue().add(navaid.getLatitude());
        p.getValue().add(navaid.getLongitude());

        ValDistanceVerticalType elev = null;
        ValDistanceSignedType   u    = null;
        if (navaid.getElevation() != null) {
            elev = convertElevation(navaid.getElevation());

            u = aixmFactory.createValDistanceSignedType();
            u.setValue(new BigDecimal(0));
            u.setUom(elev.getUom());
        }

        ElevatedPointType ep = aixmFactory.createElevatedPointType();
        ep.setSrsName("urn:ogc:def:crs:EPSG:4326");
        ep.setPos(p);
        ep.setElevation(elev);
        ep.setGeoidUndulation(u);
        ep.setId(idBase + Integer.toString(idIx++));

        ElevatedPointPropertyType epp =
                                aixmFactory.createElevatedPointPropertyType();
        epp.setElevatedPoint(ep);

        // magnetic variation
        ValMagneticVariationType var     = null;
        DateYearType             varDate = null;
        if (navaid.getVariation() != null) {
            var = aixmFactory.createValMagneticVariationType();
            var.setValue(new BigDecimal(navaid.getVariation().getVariation(),
                                        MathContext.DECIMAL64));

            varDate = aixmFactory.createDateYearType();
            varDate.setValue(Integer.toString(navaid.getVariation().getYear()));
        }

        // the designator
        CodeNavaidDesignatorType des =
                                aixmFactory.createCodeNavaidDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace(codeSpace);
        id.setValue("NDB:" + navaid.getIdent());

        // the name
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(navaid.getName());

        // the frequency
        ValFrequencyType freq = convertFrequency(navaid.getFrequency());

        // package the things together
        NDBTimeSliceType slice = aixmFactory.createNDBTimeSliceType();
        slice.setLocation(epp);
        slice.setMagneticVariation(var);
        slice.setDateMagneticVariation(varDate);
        slice.setDesignator(des);
        slice.setInterpretation(interpretation);
        slice.setSequenceNumber(sequence);
        slice.setCorrectionNumber(correction);
        slice.setValidTime(convertTimePeriod(validStart, validEnd));
        slice.setAixmName(name);
        slice.setFrequency(freq);
        slice.setId(idBase + Integer.toString(idIx++));

        NDBTimeSlicePropertyType sliceProp =
                                  aixmFactory.createNDBTimeSlicePropertyType();
        sliceProp.setNDBTimeSlice(slice);

        NDBType ndb = aixmFactory.createNDBType();
        ndb.getTimeSlice().add(sliceProp);
        ndb.setIdentifier(id);
        ndb.setId(idBase);


        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createNDB(ndb));

        propList.add(prop);
    }

    /**
     * Convert a designated point into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param codeSpace the code space to use when generating ids.
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
                     String                                   codeSpace,
                     GregorianCalendar                        validStart,
                     GregorianCalendar                        validEnd,
                     String                                   interpretation,
                     long                                     sequence,
                     long                                     correction,
                     List<BasicMessageMemberAIXMPropertyType> propList) {

        String idBase = codeSpace + ":DESIGNATED:" + navaid.getIdent() + ":";
        int    idIx   = 1;

        // the location
        DirectPositionType p = gmlFactory.createDirectPositionType();
        p.getValue().add(navaid.getLatitude());
        p.getValue().add(navaid.getLongitude());

        PointType pt = aixmFactory.createPointType();
        pt.setSrsName("urn:ogc:def:crs:EPSG:4326");
        pt.setPos(p);
        pt.setId(idBase + Integer.toString(idIx++));

        PointPropertyType pp = aixmFactory.createPointPropertyType();
        pp.setPoint(aixmFactory.createPoint(pt));

        // the designator
        CodeDesignatedPointDesignatorType des =
                          aixmFactory.createCodeDesignatedPointDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace(codeSpace);
        id.setValue("VOR:" + navaid.getIdent());

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
        slice.setLocation(pp);
        slice.setDesignator(des);
        slice.setInterpretation(interpretation);
        slice.setSequenceNumber(sequence);
        slice.setCorrectionNumber(correction);
        slice.setValidTime(convertTimePeriod(validStart, validEnd));
        slice.setType(type);
        slice.setAixmName(name);
        slice.setId(idBase + Integer.toString(idIx++));

        DesignatedPointTimeSlicePropertyType sliceProp =
                       aixmFactory.createDesignatedPointTimeSlicePropertyType();
        sliceProp.setDesignatedPointTimeSlice(slice);

        DesignatedPointType dpt = aixmFactory.createDesignatedPointType();
        dpt.getTimeSlice().add(sliceProp);
        dpt.setIdentifier(id);
        dpt.setId(idBase);


        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createDesignatedPoint(dpt));

        propList.add(prop);
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
     * Convert an Elevation object into an AIXM vertical distance type.
     *
     * @param elevation the elevation object to convert
     * @return a corresponding vertical distance type
     */
    private static ValDistanceVerticalType
    convertElevation(Elevation elevation) {
        ValDistanceVerticalType elev =
                            aixmFactory.createValDistanceVerticalType();
        elev.setValue(Double.toString(elevation.getElevation()));
        elev.setUom(convertUom(elevation.getUom()));

        return elev;
    }

    /**
     * Convert a frequenct to an appropriate AIXM representation.
     *
     * @param frequency the frequency to convert
     * @return the AIXM representation of the frequency
     */
    private static ValFrequencyType convertFrequency(Frequency frequency) {
        ValFrequencyType freq      = aixmFactory.createValFrequencyType();
        double           f         = frequency.getFrequency();

        if (f < 1000.0) {
            freq.setValue(new BigDecimal(f, MathContext.DECIMAL64));
            freq.setUom("Hz");
        } else if (f < 1000000.0) {
            freq.setValue(new BigDecimal(f / 1000.0,
                                         MathContext.DECIMAL64));
            freq.setUom("kHz");
        } else if (f < 1000000000.0) {
            freq.setValue(new BigDecimal(f / 1000000.0,
                                         MathContext.DECIMAL64));
            freq.setUom("MHz");
        } else if (f < 1000000000000.0) {
            freq.setValue(new BigDecimal(f / 1000000000.0,
                                         MathContext.DECIMAL64));
            freq.setUom("GHz");
        } else {
            freq.setValue(new BigDecimal(f, MathContext.DECIMAL64));
            freq.setUom("Hz");
        }

        return freq;
    }

    /**
     * Convert a VOR/DME into an AIXM navaid.
     *
     * @param navaid the navigation aid to convert
     * @param codeSpace the code space to use when generating ids.
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
                 String                                   codeSpace,
                 GregorianCalendar                        validStart,
                 GregorianCalendar                        validEnd,
                 String                                   interpretation,
                 long                                     sequence,
                 long                                     correction,
                 List<BasicMessageMemberAIXMPropertyType> propList) {

        vorToAixm(navaid,
                  codeSpace,
                  validStart,
                  validEnd,
                  interpretation,
                  sequence,
                  correction,
                  propList);

        VORType vor = (VORType) propList.get(propList.size() - 1).
                                        getAbstractAIXMFeature().getValue();

        dmeToAixm(navaid,
                codeSpace,
                validStart,
                validEnd,
                interpretation,
                sequence,
                correction,
                propList);

        DMEType dme = (DMEType) propList.get(propList.size() - 1).
                                        getAbstractAIXMFeature().getValue();


        // now create a navaid object that refers to the VOR & DME
        String idBase = codeSpace + ":VOR_DME:" + navaid.getIdent() + ":";
        int    idIx   = 1;

        // the location
        DirectPositionType p = gmlFactory.createDirectPositionType();
        p.getValue().add(navaid.getLatitude());
        p.getValue().add(navaid.getLongitude());

        ValDistanceVerticalType elev = convertElevation(navaid.getElevation());

        ValDistanceSignedType u = aixmFactory.createValDistanceSignedType();
        u.setValue(new BigDecimal(0));
        u.setUom(elev.getUom());

        ElevatedPointType ep = aixmFactory.createElevatedPointType();
        ep.setSrsName("urn:ogc:def:crs:EPSG:4326");
        ep.setPos(p);
        ep.setElevation(elev);
        ep.setGeoidUndulation(u);
        ep.setId(idBase + Integer.toString(idIx++));

        ElevatedPointPropertyType epp =
                                aixmFactory.createElevatedPointPropertyType();
        epp.setElevatedPoint(ep);

        // the designator
        CodeNavaidDesignatorType des =
                                aixmFactory.createCodeNavaidDesignatorType();
        des.setValue(navaid.getIdent());

        // the unique id
        CodeWithAuthorityType id = gmlFactory.createCodeWithAuthorityType();
        id.setCodeSpace(codeSpace);
        id.setValue("VOR:" + navaid.getIdent());

        // the type
        CodeNavaidServiceType type = aixmFactory.createCodeNavaidServiceType();
        type.setValue("VOR_DME");

        // the name
        TextNameType name = aixmFactory.createTextNameType();
        name.setValue(navaid.getName());

        // the related VOR
        NavaidEquipmentPropertyType peVor =
                            aixmFactory.createNavaidEquipmentPropertyType();
        peVor.setHref(vor.getIdentifier().getValue());

        NavaidComponentType tVor = aixmFactory.createNavaidComponentType();
        tVor.setTheNavaidEquipment(peVor);

        NavaidComponentPropertyType pVor =
                                aixmFactory.createNavaidComponentPropertyType();
        pVor.setNavaidComponent(tVor);

        // the related DME
        NavaidEquipmentPropertyType peDme =
                            aixmFactory.createNavaidEquipmentPropertyType();
        peDme.setHref(dme.getIdentifier().getValue());

        NavaidComponentType tDme = aixmFactory.createNavaidComponentType();
        tDme.setTheNavaidEquipment(peDme);

        NavaidComponentPropertyType pDme =
                                aixmFactory.createNavaidComponentPropertyType();
        pDme.setNavaidComponent(tDme);


        // package the things together
        NavaidTimeSliceType slice = aixmFactory.createNavaidTimeSliceType();
        slice.setLocation(epp);
        slice.setDesignator(des);
        slice.setInterpretation(interpretation);
        slice.setSequenceNumber(sequence);
        slice.setCorrectionNumber(correction);
        slice.setValidTime(convertTimePeriod(validStart, validEnd));
        slice.setType(type);
        slice.setAixmName(name);
        slice.setId(idBase + Integer.toString(idIx++));

        slice.getNavaidEquipment().add(pVor);
        slice.getNavaidEquipment().add(pDme);

        NavaidTimeSlicePropertyType sliceProp =
                                aixmFactory.createNavaidTimeSlicePropertyType();
        sliceProp.setNavaidTimeSlice(slice);

        NavaidType n = aixmFactory.createNavaidType();
        n.getTimeSlice().add(sliceProp);
        n.setIdentifier(id);
        n.setId(idBase);


        BasicMessageMemberAIXMPropertyType prop =
                aixmMessageFactory.createBasicMessageMemberAIXMPropertyType();
        prop.setAbstractAIXMFeature(aixmFactory.createNavaid(n));

        propList.add(prop);
    }

    /**
     * Convert a list of navaidsinto a list of AIXM abstract features.
     *
     * @param navaids the navigation aids to convert
     * @param codeSpace the code space to use when generating ids.
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
     * @param propList the converted navigation aids will be put into
     *        this list
     */
    public static void
    navaidsToAixm(List<Navaid>                             navaids,
                  String                                   codeSpace,
                  GregorianCalendar                        validStart,
                  GregorianCalendar                        validEnd,
                  String                                   interpretation,
                  long                                     sequence,
                  long                                     correction,
                  List<BasicMessageMemberAIXMPropertyType> propList) {

        for (Navaid navaid : navaids) {
            navaidToAixm(navaid,
                         codeSpace,
                         validStart,
                         validEnd,
                         interpretation,
                         sequence,
                         correction,
                         propList);
        }
    }
}
