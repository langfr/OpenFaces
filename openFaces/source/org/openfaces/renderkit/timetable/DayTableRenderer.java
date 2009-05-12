/*
 * OpenFaces - JSF Component Library 2.0
 * Copyright (C) 2007-2009, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */
package org.openfaces.renderkit.timetable;

import org.openfaces.component.timetable.*;
import org.openfaces.org.json.JSONArray;
import org.openfaces.org.json.JSONException;
import org.openfaces.org.json.JSONObject;
import org.openfaces.renderkit.AjaxPortionRenderer;
import org.openfaces.util.HTML;
import org.openfaces.renderkit.RendererBase;
import org.openfaces.renderkit.TableRenderer;
import org.openfaces.renderkit.TableUtil;
import org.openfaces.renderkit.cssparser.CSSUtil;
import org.openfaces.util.*;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseId;
import java.io.IOException;
import java.util.*;

/**
 * @author Dmitry Pikhulya
 */
public class DayTableRenderer extends RendererBase implements AjaxPortionRenderer {
    public static final String USE_RESOURCE_SEPARATION_MODE_ATTR = "_of_useResourceSeparationMode";

    private static final String START_TIME = "startTime";
    private static final String END_TIME = "endTime";

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (!component.isRendered())
            return;

        DayTable dayTable = (DayTable) component;
        RenderingUtil.registerDateTimeFormatObject(dayTable.getLocale());
        AjaxUtil.prepareComponentForAjax(context, dayTable);

        ResponseWriter writer = context.getResponseWriter();
        String clientId = dayTable.getClientId(context);
        writer.startElement("table", dayTable);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("cellspacing", "0", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("class", StyleUtil.getCSSClass(context,
                dayTable, dayTable.getStyle(), "o_dayTable", dayTable.getStyleClass()), null);
        writeStandardEvents(writer, dayTable);
        writer.startElement("tbody", dayTable);

        if (dayTable.isDaySwitcherVisible())
            renderNavigationRow(context, dayTable, clientId);

        List resources = renderResourceHeadersRow(context, dayTable, clientId);

        writer.startElement("tr", dayTable);
        writer.writeAttribute("class", "o_dayTable_tableRow", null);
        writer.startElement("td", dayTable);
        writer.writeAttribute("style", "height: 100%", null);

        renderContentTable(writer, dayTable, clientId, resources);
        encodeEventEditor(context, dayTable);
        encodeActionBar(context, dayTable);

        writer.endElement("td");
        writer.endElement("tr");
        writer.endElement("tbody");
        writer.endElement("table");
    }

    private void encodeActionBar(FacesContext context, DayTable dayTable) throws IOException {
        EventActionBar bar = dayTable.getEventActionBar();
        if(bar != null) {
            bar.encodeAll(context);
        }
    }

    private void encodeEventPreview(FacesContext context, DayTable dayTable) throws IOException {
        EventPreview preview = dayTable.getEventPreview();
        if(preview != null) {
            preview.encodeAll(context);
        }
    }

    private void encodeEventEditor(FacesContext context, DayTable dayTable) throws IOException {
        UIComponent eventEditor = dayTable.getEventEditor();
        if (eventEditor == null) {
            eventEditor = RenderingUtil.getOrCreateFacet(context, dayTable,
                    EventEditorDialog.COMPONENT_TYPE, "eventEditor", EventEditorDialog.class);
        }
        eventEditor.setId("_eventEditor");
        if (eventEditor instanceof EventEditorDialog)
            ((EventEditorDialog) eventEditor).setVisible(false);
        eventEditor.encodeAll(context);
    }


    private void renderNavigationRow(FacesContext context, DayTable dayTable, String clientId) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("tr", dayTable);
        writer.startElement("td", dayTable);

        HtmlCommandButton prevBtn = ComponentUtil.createButtonFacet(context, dayTable, "prev", "Previous");
        prevBtn.setId("_prev");
        prevBtn.encodeAll(context);
        writer.write(HTML.NBSP_ENTITY);
        HtmlCommandButton nextBtn = ComponentUtil.createButtonFacet(context, dayTable, "next", "Next");
        nextBtn.setId("_next");
        nextBtn.encodeAll(context);
        writer.write(HTML.NBSP_ENTITY);
        HtmlCommandButton todayBtn = ComponentUtil.createButtonFacet(context, dayTable, "today", "Today");
        todayBtn.setId("_today");
        todayBtn.encodeAll(context);
        writer.write(HTML.NBSP_ENTITY);
        writer.startElement("span", dayTable);
        writer.writeAttribute("white-space", "nowrap", null);
        writer.writeAttribute("id", clientId + RenderingUtil.CLIENT_ID_SUFFIX_SEPARATOR + "dayText", null);
        writer.endElement("span");

        writer.endElement("td");
        writer.endElement("tr");
    }

    private List renderResourceHeadersRow(FacesContext context, final DayTable dayTable, String clientId) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        ValueExpression resourcesExpression = dayTable.getResourcesValueExpression();
        final List resources = resourcesExpression != null
                ? DataUtil.readDataModelExpressionAsList(context, resourcesExpression)
                : Collections.EMPTY_LIST;
        if (resources.size() > 0) {
            final int colCount = 1 + (resources.size() > 0 ? resources.size() : 1);
            writer.startElement("tr", dayTable);
            writer.startElement("td", dayTable);
            new TableRenderer(clientId + RenderingUtil.CLIENT_ID_SUFFIX_SEPARATOR + "resourceHeaders", 0, 0, 0, "o_resourceHeadersTable") {
                protected void encodeCellContents(FacesContext context, ResponseWriter writer, UIComponent component,
                                                  int rowIndex, int colIndex) throws IOException {
                    if (colIndex == 0 || colIndex == colCount)
                        return;
                    TimetableResource resource = (TimetableResource) resources.get(colIndex - 1);
                    UIComponent resourceHeader = dayTable.getResourceHeader();
                    if (resourceHeader != null) {
                        Object prevValue = ComponentUtil.setRequestMapValue("resource", resource);
                        resourceHeader.encodeAll(context);
                        ComponentUtil.setRequestMapValue("resource", prevValue);
                    } else {
                        writer.write(resource.getName());
                    }
                }
            }.render(dayTable, true, 1, colCount + 1);
            writer.endElement("td");
            writer.endElement("tr");
        }
        return resources;
    }

    private void encodeInitScript(FacesContext context, DayTable dayTable, List resources) throws IOException {
        Map<String, TimeZone> timeZoneParam = getTimeZoneParamForJSONConverter(dayTable);
        JSONArray resourcesJsArray = DataUtil.listToJSONArray(resources, timeZoneParam);
        dayTable.getAttributes().put(USE_RESOURCE_SEPARATION_MODE_ATTR, resourcesJsArray.length() > 0);

        String clientId = dayTable.getClientId(context);

        JSONObject editingOptionsObj = getEditingOptionsObj(dayTable);
        JSONObject stylingParams = getStylingParamsObj(context, dayTable);

        StyleUtil.renderStyleClasses(context, dayTable);
        TimeZone timeZone = (dayTable.getTimeZone() != null)
                ? dayTable.getTimeZone()
                : TimeZone.getDefault();
        boolean editable = dayTable.isEditable();
        boolean thereIsChangeListener = dayTable.getTimetableChangeListener() != null ||
                dayTable.getTimetableChangeListeners().length > 0;
        if (!thereIsChangeListener) {
            Log.log(context, "The DayTable with clientID=[" + clientId + "] is set to be editable, but is not configured to accept the changes. " +
                    "You should either make it read-only explicitly (using editable=\"false\" attribute), or define timetableChangeListener attriubte to accept the changes (see DayTable reference).");
            editable = false;
        }
        List<AbstractTimetableEvent> events = new ArrayList<AbstractTimetableEvent>();
        JSONObject eventParams = composeEventParams(context, dayTable, events);
        Map<String, AbstractTimetableEvent> loadedEvents = dayTable.getLoadedEvents();
        loadedEvents.clear();
        for (AbstractTimetableEvent event : events) {
            loadedEvents.put(event.getId(), event);
        }

        JSONArray areaSettings = encodeEventAreas(context, dayTable, events);

        List<UITimetableEvent> uiEvents = ComponentUtil.findChildrenWithClass(dayTable, UITimetableEvent.class);
        if (uiEvents.size() > 1)
            throw new FacesException("There should be only one <o:timetableEvent> tag inside of <o:dayTable> tag. " +
                    "DayTable clientId = " + dayTable.getClientId(context));
        UITimetableEvent uiEvent = uiEvents.size() > 0 ? uiEvents.get(0) : null;

        try {
            RenderingUtil.renderInitScript(context, new ScriptBuilder().initScript(context, dayTable, "O$._initDayTable",
                    DataUtil.formatDateTimeForJs(dayTable.getDay(), timeZone),
                    dayTable.getLocale(),
                    "MMMM, dd yyyy",
                    dayTable.getStartTime(),
                    dayTable.getEndTime(),
                    eventParams,
                    resourcesJsArray,
                    areaSettings,
                    editable,
                    dayTable.getOnchange(),
                    editingOptionsObj,
                    dayTable.isDaySwitcherVisible(),
                    stylingParams,
                    uiEvent != null ? uiEvent.toJSONObject(null) : null,
                    dayTable.getTimePattern(),
                    dayTable.getTimeSuffixPattern(),
                    dayTable.getMajorTimeInterval(),
                    dayTable.getMinorTimeInterval(),
                    dayTable.getShowTimeForMinorIntervals()).semicolon()
                    , new String[]{
                            ResourceUtil.getUtilJsURL(context),
                            ResourceUtil.getJsonJsURL(context),
                            TableUtil.getTableUtilJsURL(context),
                            ResourceUtil.getInternalResourceURL(context, DayTableRenderer.class, "dayTable.js")
                    });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    private JSONArray encodeEventAreas(FacesContext context, DayTable dayTable, List<AbstractTimetableEvent> events) throws IOException {
        List<EventArea> eventAreas = dayTable.getEventAreas();
        if (eventAreas.size() > 0) {
            String eventVarName = dayTable.getEventVar();
            Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
            String prevEventVarValue = (String) requestMap.get(eventVarName);
            for (AbstractTimetableEvent event : events) {
                dayTable.setEvent(event);
                for (EventArea eventArea : eventAreas) {
                    eventArea.encodeAll(context);
                }

            }
            dayTable.setEvent(null);
            requestMap.put(eventVarName, prevEventVarValue);
        }

        JSONArray areaSettings = new JSONArray();
        try {
            for (int i = 0; i < eventAreas.size(); i++) {
                EventArea eventArea = eventAreas.get(i);
                JSONObject thisAreaSettings = new JSONObject();
                thisAreaSettings.put("id", eventArea.getId());
                thisAreaSettings.put("horizontalAlignment", eventArea.getHorizontalAlignment());
                thisAreaSettings.put("verticalAlignment", eventArea.getVerticalAlignment());
                areaSettings.put(i, thisAreaSettings);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return areaSettings;
    }

    private Date getDayTimeAtTimeZone(Date day, String timeStr, TimeZone timeZone) {
        Calendar c = new GregorianCalendar(timeZone);
        c.setTime(day);
        String[] timeFields = timeStr.split(":");
        int hours = Integer.parseInt(timeFields[0]);
        int minutes = Integer.parseInt(timeFields[1]);
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private JSONObject composeEventParams(FacesContext context, DayTable dayTable, List putEventsHere) {
        PreloadedEvents preloadedEvents = dayTable.getPreloadedEvents();
        Map<String, TimeZone> timeZoneParam = getTimeZoneParamForJSONConverter(dayTable);
        TimeZone timeZone = timeZoneParam.get("timeZone");

        Date startTime = null;
        Date endTime = null;
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        Object prevStartTimeValue = null;
        Object prevEndTimeValue = null;
        if (preloadedEvents == PreloadedEvents.NONE) {
            String dayStartTime = dayTable.getStartTime();
            String dayEndTime = dayTable.getEndTime();
            startTime = getDayTimeAtTimeZone(dayTable.getDay(), dayStartTime != null ? dayStartTime : "00:00", timeZone);
            endTime = getDayTimeAtTimeZone(dayTable.getDay(), dayEndTime != null ? dayEndTime : "24:00", timeZone);
            prevStartTimeValue = requestMap.put(START_TIME, startTime);
            prevEndTimeValue = requestMap.put(END_TIME, endTime);
        }

        ValueExpression eventsExpression = dayTable.getEventsValueExpression();
        List events = eventsExpression == null ? Collections.EMPTY_LIST : DataUtil.readDataModelExpressionAsList(context, eventsExpression);
        if (putEventsHere != null) {
            putEventsHere.clear();
            putEventsHere.addAll(events);
        }

        if (preloadedEvents == PreloadedEvents.NONE) {
            requestMap.put(START_TIME, prevStartTimeValue);
            requestMap.put(END_TIME, prevEndTimeValue);
        }

        JSONArray eventsJsArray = DataUtil.listToJSONArray(events, timeZoneParam);

        JSONObject eventParams = new JSONObject();
        try {
            eventParams.put("events", eventsJsArray);
            eventParams.put("from", DataUtil.formatDateTimeForJs(startTime, timeZone));
            eventParams.put("to", DataUtil.formatDateTimeForJs(endTime, timeZone));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return eventParams;
    }

    private Map<String, TimeZone> getTimeZoneParamForJSONConverter(DayTable dayTable) {
        TimeZone timeZone = (dayTable.getTimeZone() != null)
                ? dayTable.getTimeZone()
                : TimeZone.getDefault();
        Map<String, TimeZone> timeZoneParam = new HashMap<String, TimeZone>();
        timeZoneParam.put("timeZone", timeZone);
        return timeZoneParam;
    }

    private JSONObject getEditingOptionsObj(DayTable dayTable) {
        JSONObject editingOptionsObj = new JSONObject();
        TimetableEditingOptions editingOptions = dayTable.getEditingOptions();
        RenderingUtil.addJsonParam(editingOptionsObj, "autoSaveChanges", editingOptions.getAutoSaveChanges(), true);
        RenderingUtil.addJsonParam(editingOptionsObj, "overlappedEventsAllowed", editingOptions.getOverlappedEventsAllowed(), true);
        RenderingUtil.addJsonParam(editingOptionsObj, "eventResourceEditable", editingOptions.isEventResourceEditable(), true);
        RenderingUtil.addJsonParam(editingOptionsObj, "eventDurationEditable", editingOptions.isEventDurationEditable(), true);
        RenderingUtil.addJsonParam(editingOptionsObj, "defaultEventDuration", editingOptions.getDefaultEventDuration(), 30);
        return editingOptionsObj;
    }

    private JSONObject getStylingParamsObj(FacesContext context, DayTable dayTable) {
        JSONObject stylingParams = new JSONObject();


        StyleUtil.addStyleJsonParam(context, dayTable, stylingParams, "rolloverClass",
                dayTable.getRolloverStyle(), dayTable.getRolloverClass());
        StyleUtil.addStyleJsonParam(context, dayTable, stylingParams, "resourceHeadersRowClass",
                dayTable.getResourceHeadersRowStyle(), dayTable.getResourceHeadersRowClass());
        StyleUtil.addStyleJsonParam(context, dayTable, stylingParams, "rowClass",
                dayTable.getRowStyle(), dayTable.getRowClass());

        RenderingUtil.addJsonParam(stylingParams, "timeTextPosition",
                dayTable.getTimeTextPosition(), TimeTextPosition.UNDER_MARK);

        StyleUtil.addStyleJsonParam(context, dayTable, stylingParams, "timeColumnClass",
                dayTable.getTimeColumnStyle(), dayTable.getTimeColumnClass());
        StyleUtil.addStyleJsonParam(context, dayTable, stylingParams, "majorTimeClass",
                dayTable.getMajorTimeStyle(), dayTable.getMajorTimeClass());
        StyleUtil.addStyleJsonParam(context, dayTable, stylingParams, "minorTimeClass",
                dayTable.getMinorTimeStyle(), dayTable.getMinorTimeClass());
        StyleUtil.addStyleJsonParam(context, dayTable, stylingParams, "timeSuffixClass",
                dayTable.getTimeSuffixStyle(), dayTable.getTimeSuffixClass(), StyleGroup.regularStyleGroup(1));

        RenderingUtil.addJsonParam(stylingParams, "defaultEventColor", dayTable.getDefaultEventColor());
        RenderingUtil.addJsonParam(stylingParams, "reservedTimeEventColor", dayTable.getDefaultEventColor());
        StyleUtil.addStyleJsonParam(context, dayTable, stylingParams, "reservedTimeEventClass",
                dayTable.getReservedTimeEventStyle(), dayTable.getReservedTimeEventClass());
        RenderingUtil.addJsonParam(stylingParams, "eventBackgroundTransparencyLevel", dayTable.getEventBackgroundTransparencyLevel(), 0.2);
        RenderingUtil.addJsonParam(stylingParams, "eventBackgroundIntensityLevel", dayTable.getEventBackgroundIntensityLevel(), 0.25);
        RenderingUtil.addJsonParam(stylingParams, "dragAndDropTransitionPeriod", dayTable.getDragAndDropTransitionPeriod(), 70);
        RenderingUtil.addJsonParam(stylingParams, "dragAndDropCancelingPeriod", dayTable.getDragAndDropCancelingPeriod(), 200);
        RenderingUtil.addJsonParam(stylingParams, "undroppableStateTransitionPeriod", dayTable.getUndroppableStateTransitionPeriod(), 250);
        RenderingUtil.addJsonParam(stylingParams, "undroppableEventTransparency", dayTable.getUndroppableEventTransparency(), 0.5);

        RenderingUtil.addJsonParam(stylingParams, "resourceHeadersRowSeparator", dayTable.getResourceHeadersRowSeparator());
        RenderingUtil.addJsonParam(stylingParams, "resourceColumnSeparator", dayTable.getResourceColumnSeparator());
        RenderingUtil.addJsonParam(stylingParams, "timeColumnSeparator", dayTable.getTimeColumnSeparator());
        RenderingUtil.addJsonParam(stylingParams, "primaryRowSeparator", dayTable.getPrimaryRowSeparator());
        RenderingUtil.addJsonParam(stylingParams, "secondaryRowSeparator", dayTable.getSecondaryRowSeparator());
        RenderingUtil.addJsonParam(stylingParams, "timeColumnPrimaryRowSeparator", dayTable.getTimeColumnPrimaryRowSeparator());
        RenderingUtil.addJsonParam(stylingParams, "timeColumnSecondaryRowSeparator", dayTable.getTimeColumnSecondaryRowSeparator());
        return stylingParams;
    }

    private void renderContentTable(
            ResponseWriter writer,
            final DayTable dayTable,
            final String clientId,
            final List resources) throws IOException {
        writer.startElement("div", dayTable);
        writer.writeAttribute("id", clientId + "::scroller", null);
        writer.writeAttribute("class", "o_dayTable_scroller", null);

        int colCount = 1 + (resources.size() > 0 ? resources.size() : 1);
        new TableRenderer(clientId + RenderingUtil.CLIENT_ID_SUFFIX_SEPARATOR + "table", 0, 0, 0, "o_dayTable_table") {

            protected void encodeTFoot(ResponseWriter writer, UIComponent component) throws IOException {
                writer.startElement("tfoot", component);
                writer.writeAttribute("id", clientId + "::hiddenArea", null);
                writer.writeAttribute("style", "display: none", null);
                writer.startElement("tr", component);
                writer.startElement("td", component);

                FacesContext context = FacesContext.getCurrentInstance();
                encodeEventPreview(context, dayTable);
                encodeInitScript(context, dayTable, resources);

                writer.endElement("td");
                writer.endElement("tr");
                writer.endElement("tfoot");
            }
        }.render(dayTable, true, 0, colCount);

        writer.endElement("div");
    }

    public boolean getRendersChildren() {
        return true;
    }

    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
    }

    public void decode(FacesContext context, UIComponent component) {
        super.decode(context, component);
        DayTable dayTable = (DayTable) component;
        Map<String, String> requestParams = context.getExternalContext().getRequestParameterMap();

        String changesKey = dayTable.getClientId(context) + RenderingUtil.CLIENT_ID_SUFFIX_SEPARATOR + "timetableChanges";
        String timetableChangesStr = requestParams.get(changesKey);
        if (timetableChangesStr == null)
            return;

        TimetableEvent[] addedEvents;
        TimetableEvent[] editedEvents;
        String[] removedEventIds;
        try {
            JSONObject timetableChanges = new JSONObject(timetableChangesStr);
            Object addedEventsArray = timetableChanges.get("addedEvents");
            Object editedEventsObj = timetableChanges.get("editedEvents");
            Object removedEventIdsObj = timetableChanges.get("removedEventIds");

            TimeZone timeZone = (dayTable.getTimeZone() != null)
                    ? dayTable.getTimeZone()
                    : TimeZone.getDefault();

            addedEvents = eventsFromJSONArray(timeZone, addedEventsArray);
            editedEvents = eventsFromJSONArray(timeZone, editedEventsObj);
            JSONArray removedEventIdsArray = JSONObject.NULL.equals(removedEventIdsObj) ? null : (JSONArray) removedEventIdsObj;
            int removedEventCount = removedEventIdsArray != null ? removedEventIdsArray.length() : 0;
            removedEventIds = new String[removedEventCount];
            for (int i = 0; i < removedEventCount; i++) {
                removedEventIds[i] = removedEventIdsArray.getString(i);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        TimetableChangeEvent event = new TimetableChangeEvent(dayTable, addedEvents, editedEvents, removedEventIds);
        event.setPhaseId(PhaseId.UPDATE_MODEL_VALUES);
        if (addedEvents.length > 0 || editedEvents.length > 0 || removedEventIds.length > 0)
            dayTable.queueEvent(event);
        context.getExternalContext().getRequestMap().put(getTimetableChangeEventKey(context, dayTable), event);
    }

    private String getTimetableChangeEventKey(FacesContext context, DayTable dayTable) {
        return "OF__TimetableChangeEvent for " + dayTable.getClientId(context);
    }

    private TimetableEvent[] eventsFromJSONArray(TimeZone timeZone, Object arrayObj) throws JSONException {
        JSONArray array = JSONObject.NULL.equals(arrayObj) ? null : (JSONArray) arrayObj;
        int arrayLength = array != null ? array.length() : 0;
        TimetableEvent[] targetArray = new TimetableEvent[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            JSONObject eventObj = (JSONObject) array.get(i);

            TimetableEvent event = new TimetableEvent();
            event.setId((String) eventObj.opt("id"));
            event.setName(eventObj.getString("name"));
            event.setDescription(eventObj.getString("description"));
            if (!eventObj.isNull("resourceId"))
                event.setResourceId(eventObj.getString("resourceId"));
            String startStr = eventObj.getString("start");
            event.setStart(DataUtil.parseDateTimeFromJs(startStr, timeZone));
            String endStr = eventObj.getString("end");
            event.setEnd(DataUtil.parseDateTimeFromJs(endStr, timeZone));
            String colorStr = (String) eventObj.opt("color");
            event.setColor(CSSUtil.parseColor(colorStr));

            targetArray[i] = event;
        }
        return targetArray;
    }

    public JSONObject encodeAjaxPortion(FacesContext context,
                                        UIComponent component,
                                        String portionName,
                                        JSONObject jsonParam) throws IOException, JSONException {
        DayTable dayTable = (DayTable) component;
        JSONObject responseData = new JSONObject();
        if (portionName.equals("saveEventChanges")) {
            encodeSaveEventChangesPortion(context, dayTable, responseData, jsonParam);
        } else if (portionName.equals("loadEvents")) {
            encodeLoadEventsPortion(context, dayTable, responseData, jsonParam);
        } else {
            throw new IllegalArgumentException("Unknown portion name: " + portionName);
        }
        return responseData;
    }

    private void encodeLoadEventsPortion(
            FacesContext context,
            DayTable dayTable,
            JSONObject responseData,
            JSONObject jsonParam) throws JSONException, IOException {
        Map<String, AbstractTimetableEvent> loadedEvents = dayTable.getLoadedEvents();
        JSONArray eventsJsArray = encodeRequestedEventsArray(context, dayTable, jsonParam, loadedEvents);
        responseData.put("events", eventsJsArray);
    }

    private void encodeSaveEventChangesPortion(
            FacesContext context,
            DayTable dayTable,
            JSONObject responseData,
            JSONObject jsonParam) throws JSONException, IOException {
        boolean reloadAllEvents = jsonParam.getBoolean("reloadAllEvents");
        Map<String, TimeZone> timeZoneParam = getTimeZoneParamForJSONConverter(dayTable);

        TimetableChangeEvent changeEvent = (TimetableChangeEvent) context.getExternalContext().getRequestMap().get(
                getTimetableChangeEventKey(context, dayTable));
        reloadAllEvents |= changeEvent.getReloadAllEvents();

        Map<String, AbstractTimetableEvent> loadedEvents = dayTable.getLoadedEvents();
        if (!reloadAllEvents) {
            TimetableEvent[] addedEvents = changeEvent.getAddedEvents();
            TimetableEvent[] changedEvents = changeEvent.getChangedEvents();
            String[] removedEventIds = changeEvent.getRemovedEventIds();
            responseData.put("addedEvents", DataUtil.arrayToJSONArray(addedEvents, timeZoneParam));
            responseData.put("editedEvents", DataUtil.arrayToJSONArray(changedEvents, timeZoneParam));
            List<AbstractTimetableEvent> events = new ArrayList<AbstractTimetableEvent>();
            events.addAll(Arrays.asList(addedEvents));
            events.addAll(Arrays.asList(changedEvents));
            encodeEventAreas(context, dayTable, events);
            for (TimetableEvent event : addedEvents) {
                String eventId = event.getId();
                if (eventId == null)
                    throw new IllegalStateException("You must assign id field for all added events inside of TimetableChangeEvent. See \"timetableChangeListener\" attribute description in DayTable reference.");
                loadedEvents.put(eventId, event);
            }
            for (TimetableEvent event : changedEvents) {
                loadedEvents.put(event.getId(), event);
            }
            for (String eventId : removedEventIds) {
                loadedEvents.remove(eventId);
            }

        } else {
            loadedEvents.clear();
            JSONArray requestedEvents = encodeRequestedEventsArray(context, dayTable, jsonParam, loadedEvents);
            responseData.put("reloadedEvents", requestedEvents);
        }

    }

    private JSONArray encodeRequestedEventsArray(
            FacesContext context,
            DayTable dayTable,
            JSONObject jsonParam,
            Map<String, AbstractTimetableEvent> putLoadedEventsHere) throws JSONException, IOException {
        Map<String, TimeZone> timeZoneParam = getTimeZoneParamForJSONConverter(dayTable);
        TimeZone timeZone = timeZoneParam.get("timeZone");
        String startTimeStr = jsonParam.getString(START_TIME);
        String endTimeStr = jsonParam.getString(END_TIME);
        Date startTime = DataUtil.parseDateTimeFromJs(startTimeStr, timeZone);
        Date endTime = DataUtil.parseDateTimeFromJs(endTimeStr, timeZone);

        PreloadedEvents preloadedEvents = dayTable.getPreloadedEvents();
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();

        Object prevStartTimeValue = null;
        Object prevEndTimeValue = null;
        if (PreloadedEvents.NONE.equals(preloadedEvents)) {
            prevStartTimeValue = requestMap.put(START_TIME, startTime);
            prevEndTimeValue = requestMap.put(END_TIME, endTime);
        }
        ValueExpression eventsExpression = dayTable.getEventsValueExpression();
        List<AbstractTimetableEvent> events = eventsExpression == null
                ? Collections.emptyList()
                : DataUtil.readDataModelExpressionAsList(context, eventsExpression);
        if (PreloadedEvents.NONE.equals(preloadedEvents)) {
            requestMap.put(START_TIME, prevStartTimeValue);
            requestMap.put(END_TIME, prevEndTimeValue);
        }

        if (putLoadedEventsHere != null)
            for (AbstractTimetableEvent event : events) {
                putLoadedEventsHere.put(event.getId(), event);
            }

        encodeEventAreas(context, dayTable, events);
        return DataUtil.listToJSONArray(events, timeZoneParam);
    }


}
