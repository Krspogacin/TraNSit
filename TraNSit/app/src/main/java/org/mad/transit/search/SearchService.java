package org.mad.transit.search;

import android.util.Pair;

import org.mad.transit.dto.ActionDto;
import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.DepartureTime;
import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.PriceList;
import org.mad.transit.model.Stop;
import org.mad.transit.model.Timetable;
import org.mad.transit.model.TimetableDay;
import org.mad.transit.model.Zone;
import org.mad.transit.repository.LineRepository;
import org.mad.transit.repository.LocationRepository;
import org.mad.transit.repository.PriceListRepository;
import org.mad.transit.repository.StopRepository;
import org.mad.transit.repository.TimetableRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.SneakyThrows;

import static org.mad.transit.util.Constants.MILLISECONDS_IN_HOUR;
import static org.mad.transit.util.Constants.MILLISECONDS_IN_MINUTE;
import static org.mad.transit.util.Constants.getCurrentTimetableDay;
import static org.mad.transit.util.Constants.getTimeInMilliseconds;
import static org.mad.transit.util.LocationsUtil.calculateDistance;

@Singleton
public class SearchService {

    private static final int MAX_QUEUE_SIZE = 300000;
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    private static final int MAX_LINES_ALLOWED = 2;
    private final LineRepository lineRepository;
    private final StopRepository stopRepository;
    private final TimetableRepository timetableRepository;
    private final PriceListRepository priceListRepository;
    private final LocationRepository locationRepository;
    private List<PriceList> priceLists;
    private List<Line> lines;
    private List<Stop> stops;
    private List<LineStopDirection> linesStopsDirections;
    private Map<Pair<Long, LineDirection>, List<Long>> lineStopsMap;
    private Map<Long, List<Pair<Long, LineDirection>>> stopLinesMap;
    private Map<TimetableDay, List<Timetable>> timetables;
    private Map<Pair<Long, LineDirection>, Map<Long, Long>> timesBetweenStops;
    private RouteSearchProblem problem;

    @Inject
    public SearchService(LineRepository lineRepository, StopRepository stopRepository, TimetableRepository timetableRepository,
                         PriceListRepository priceListRepository, LocationRepository locationRepository) {
        this.lineRepository = lineRepository;
        this.stopRepository = stopRepository;
        this.timetableRepository = timetableRepository;
        this.priceListRepository = priceListRepository;
        this.locationRepository = locationRepository;
    }

    public List<RouteDto> searchRoutes(RouteSearchProblem problem) {
        this.initData(problem);

        PriorityQueue<List<Pair<Action, SearchState>>> queue = new PriorityQueue<>(DEFAULT_INITIAL_CAPACITY, this.getComparator());
        queue.add(this.getInitialActionState(problem.getStartState()));

        List<Solution> solutions = new ArrayList<>();

        while (!queue.isEmpty() && solutions.size() < problem.getSolutionCount()) {

            if (queue.size() >= MAX_QUEUE_SIZE) {
                Solution trivialSolution = this.getTrivialSolution();
                solutions.add(trivialSolution);
                break;
            }

            List<Pair<Action, SearchState>> currentPath = queue.poll();
            SearchState currentState = currentPath.get(currentPath.size() - 1).second;

            if (problem.isEndState(currentState)) {
                Solution potentialSolution = new Solution(currentPath);
                boolean isDifferent = true;
                for (Solution previousSolution : solutions) {
                    if (!previousSolution.isDifferent(potentialSolution)) {
                        isDifferent = false;
                        break;
                    }
                }
                if (isDifferent) {
                    solutions.add(potentialSolution);
                }
                continue;
            }

            for (Pair<Action, SearchState> nextState : this.getNextStates(currentState, currentPath)) {
                List<Pair<Action, SearchState>> newPath = this.clonePath(currentPath);
                newPath.add(nextState);
                queue.add(newPath);
            }
        }

        return this.convertSolutionsToRoutes(solutions);
    }

    private void initData(RouteSearchProblem problem) {
        this.problem = problem;
        this.lines = this.lineRepository.findAll();
        this.stops = this.stopRepository.findAll();
        this.linesStopsDirections = this.stopRepository.findAllLinesStopsDirections();
        this.timetables = this.timetableRepository.findAll();
        this.priceLists = this.priceListRepository.findAll();

        this.fillLineStops();
        this.fillStopLines();
        this.calculateTimesBetweenStops();
    }

    private void fillLineStops() {
        this.lineStopsMap = new HashMap<>();
        for (Line line : this.lines) {
            for (LineDirection direction : LineDirection.values()) {
                List<Long> lineStops = new ArrayList<>();
                for (LineStopDirection lineStopDirection : this.linesStopsDirections) {
                    if (lineStopDirection.getLineId().equals(line.getId()) && lineStopDirection.getDirection() == direction &&
                            !lineStops.contains(lineStopDirection.getStopId())) {
                        lineStops.add(lineStopDirection.getStopId());
                    }
                }
                if (line.getNumber().equals("4") && LineDirection.A == direction) {
                    Collections.reverse(lineStops); // for line 4, thanks to genial GSPNS data
                }
                this.lineStopsMap.put(new Pair<>(line.getId(), direction), lineStops);
            }
        }
    }

    private void fillStopLines() {
        this.stopLinesMap = new HashMap<>();
        for (Stop stop : this.stops) {
            List<Pair<Long, LineDirection>> stopLines = new ArrayList<>();
            for (LineStopDirection lineStopDirection : this.linesStopsDirections) {
                if (lineStopDirection.getStopId().equals(stop.getId())) {
                    stopLines.add(new Pair<>(lineStopDirection.getLineId(), lineStopDirection.getDirection()));
                }
            }
            this.stopLinesMap.put(stop.getId(), stopLines);
        }
    }

    private void calculateTimesBetweenStops() {
        this.timesBetweenStops = new HashMap<>();
        for (Line line : this.lines) {
            for (LineDirection direction : LineDirection.values()) {
                Pair<Long, LineDirection> key = new Pair<>(line.getId(), direction);
                List<Long> lineStops = this.lineStopsMap.get(key);
                this.timesBetweenStops.put(key, new HashMap<>());
                if (lineStops != null && !lineStops.isEmpty()) {
                    long waitTime = 0;
                    this.timesBetweenStops.get(key).put(lineStops.get(0), waitTime);
                    for (int i = 1; i < lineStops.size(); i++) {
                        Stop previousStop = this.getStopById(lineStops.get(i - 1));
                        Stop currentStop = this.getStopById(lineStops.get(i));
                        if (previousStop != null && currentStop != null) {
                            waitTime += calculateDistance(previousStop.getLocation().getLatitude(), previousStop.getLocation().getLongitude(),
                                    currentStop.getLocation().getLatitude(), currentStop.getLocation().getLongitude()) * MILLISECONDS_IN_HOUR / 40;// 40 = bus speed
                            this.timesBetweenStops.get(key).put(currentStop.getId(), waitTime);
                        }
                    }
                }
            }
        }
    }

    private Comparator<List<Pair<Action, SearchState>>> getComparator() {
        return (o1, o2) -> {
            double firstCost = o1.get(o1.size() - 1).second.aStarCost(this.problem.getEndLocation());
            double secondCost = o2.get(o2.size() - 1).second.aStarCost(this.problem.getEndLocation());

            return Double.compare(firstCost, secondCost);
        };
    }

    private List<Pair<Action, SearchState>> getInitialActionState(SearchState startState) {
        List<Pair<Action, SearchState>> list = new ArrayList<>();
        list.add(new Pair<>(new StartAction(), startState));
        return list;
    }

    private List<Pair<Action, SearchState>> getNextStates(SearchState currentState, List<Pair<Action, SearchState>> currentPath) {
        List<Pair<Action, SearchState>> list = new ArrayList<>();
        this.getNextWalkStates(list, currentState);
        this.getNextBusStates(list, currentState, currentPath);
        return list;
    }

    private void getNextWalkStates(List<Pair<Action, SearchState>> list, SearchState currentState) {
        if (currentState.getStop() != null) {
            Action action = WalkAction.builder()
                    .startLocation(currentState.getLocation())
                    .endLocation(this.problem.getEndLocation())
                    .build();
            list.add(new Pair<>(action, action.execute(currentState)));
            return;
        }
        List<Long> nearestLineStops = this.findNearestLineStops(currentState.getLocation());
        for (Long stopId : nearestLineStops) {
            Stop stop = this.getStopById(stopId);
            if (stop != null) {
                Action action = WalkAction.builder()
                        .startLocation(currentState.getLocation())
                        .endLocation(stop.getLocation())
                        .build();
                SearchState nextState = action.execute(currentState);
                nextState.setStop(stop);
                list.add(new Pair<>(action, nextState));
            }
        }
        Action action = WalkAction.builder()
                .startLocation(currentState.getLocation())
                .endLocation(this.problem.getEndLocation())
                .build();
        list.add(new Pair<>(action, action.execute(currentState)));
    }

    private void getNextBusStates(List<Pair<Action, SearchState>> list, SearchState currentState, List<Pair<Action, SearchState>> currentPath) {
        if (currentState.getStop() != null) {
            Map<Pair<Long, LineDirection>, Long> nextLineStops = this.findNextLineStop(currentState.getStop());
            for (Map.Entry<Pair<Long, LineDirection>, Long> entry : nextLineStops.entrySet()) {
                Long lineId = entry.getKey().first;
                LineDirection lineDirection = entry.getKey().second;
                Stop nextStop = this.getStopById(entry.getValue());
                if (nextStop != null && this.shouldUseLine(currentPath, lineId, lineDirection)) {
                    Action action = BusAction.builder()
                            .startLocation(currentState.getLocation())
                            .endLocation(nextStop.getLocation())
                            .line(this.getLineById(lineId))
                            .lineDirection(lineDirection)
                            .build();

                    SearchState nextState = action.execute(currentState);
                    boolean changingLine = this.isChangingLine(currentState, nextState);
                    if (changingLine && !this.problem.isTransfersEnabled()) {
                        continue;
                    }
                    nextState.setStop(nextStop);
                    nextState.setTravelCost(this.getTravelCost(currentPath, currentState, nextState));

                    if (this.isWalkActionPrevious(currentPath) || changingLine) {
                        double waitTime = this.calculateWaitTime(this.problem.getStartTime() + currentState.getTimeElapsedInMilliseconds(),
                                lineId, lineDirection, nextStop.getId());
                        nextState.setTimeElapsed(nextState.getTimeElapsed() + waitTime);
                    }
                    list.add(new Pair<>(action, nextState));
                }
            }
        }
    }

    private boolean shouldUseLine(List<Pair<Action, SearchState>> currentPath, Long newLineId, LineDirection newLineDirection) {
        List<Pair<Long, LineDirection>> alreadyUsedLines = this.getAlreadyUsedLines(currentPath);
        Pair<Long, LineDirection> lineDirectionPair = new Pair<>(newLineId, newLineDirection);

        if (alreadyUsedLines.size() == MAX_LINES_ALLOWED) {
            return false;
        }
        if (alreadyUsedLines.contains(lineDirectionPair)) { // if this line is already used but not last in the list, it should not be used again
            return alreadyUsedLines.indexOf(lineDirectionPair) == alreadyUsedLines.size() - 1;
        }
        return true;
    }

    private List<Pair<Long, LineDirection>> getAlreadyUsedLines(List<Pair<Action, SearchState>> currentPath) {
        List<Pair<Long, LineDirection>> usedLines = new ArrayList<>();
        for (Pair<Action, SearchState> pair : currentPath) {
            if (pair.first instanceof BusAction) {
                Long lineId = pair.second.getLine().getId();
                LineDirection lineDirection = pair.second.getLineDirection();

                Pair<Long, LineDirection> lineDirectionPair = new Pair<>(lineId, lineDirection);
                if (!usedLines.contains(lineDirectionPair)) {
                    usedLines.add(lineDirectionPair);
                }
            }
        }
        return usedLines;
    }

    private int getTravelCost(List<Pair<Action, SearchState>> currentPath, SearchState currentState, SearchState nextState) {
        SearchState startStopState = null;
        for (Pair<Action, SearchState> pair : currentPath) {
            if (pair.second.getStop() != null) {
                startStopState = pair.second;
                break;
            }
        }
        if (startStopState == null) {
            return nextState.getTravelCost();
        }
        Zone startZone = startStopState.getStop().getZone();
        Zone nextZone = nextState.getStop().getZone();

        int cost = this.getCostBetweenZones(startZone.getId(), nextZone.getId());

        if (this.isChangingLine(currentState, nextState)) {
            return nextState.getTravelCost() + cost; // take into account cost for previous line
        } else {
            return cost;
        }
    }

    private int getCostBetweenZones(Long startZoneId, Long endZoneId) {
        for (PriceList priceList : this.priceLists) {
            if (priceList.getStartZoneId().equals(startZoneId) && priceList.getEndZoneId().equals(endZoneId)) {
                return priceList.getPrice();
            }
        }
        return 0;
    }

    private boolean isWalkActionPrevious(List<Pair<Action, SearchState>> currentPath) {
        return currentPath.get(currentPath.size() - 1).first instanceof WalkAction;
    }

    private boolean isChangingLine(SearchState currentState, SearchState nextState) {
        return currentState.getLine() != null &&
                (!currentState.getLine().getId().equals(nextState.getLine().getId()) || currentState.getLineDirection() != nextState.getLineDirection());
    }

    private Map<Pair<Long, LineDirection>, Long> findNextLineStop(Stop stop) {
        Map<Pair<Long, LineDirection>, Long> nextLineStops = new HashMap<>();
        List<Pair<Long, LineDirection>> stopLines = this.stopLinesMap.get(stop.getId());
        if (stopLines != null) {
            for (Pair<Long, LineDirection> pair : stopLines) {
                List<Long> lineStops = this.lineStopsMap.get(pair);
                if (lineStops != null) {
                    int nextStopIndex = lineStops.indexOf(stop.getId()) + 1;
                    if (nextStopIndex < lineStops.size()) {
                        nextLineStops.put(pair, lineStops.get(nextStopIndex));
                    }
                }
            }
        }
        return nextLineStops;
    }

    private List<Long> findNearestLineStops(Location startLocation) {
        List<Long> nearestStops = new ArrayList<>();
        for (Line line : this.lines) {
            for (LineDirection direction : LineDirection.values()) {
                List<Long> lineStops = this.lineStopsMap.get(new Pair<>(line.getId(), direction));
                if (lineStops != null && !lineStops.isEmpty()) {
                    double minDist = 9999999;
                    int minIndex = 0;
                    for (int i = 0; i < lineStops.size(); i++) {
                        Stop stop = this.getStopById(lineStops.get(i));
                        if (stop != null) {
                            double dist = calculateDistance(startLocation.getLatitude(), startLocation.getLongitude(),
                                    stop.getLocation().getLatitude(), stop.getLocation().getLongitude());
                            if (dist < minDist) {
                                minDist = dist;
                                minIndex = i;
                            }
                        }
                    }
                    nearestStops.add(lineStops.get(minIndex));
                }
            }
        }
        return nearestStops;
    }

    private List<Pair<Action, SearchState>> clonePath(List<Pair<Action, SearchState>> currentPath) {
        List<Pair<Action, SearchState>> newPath = new ArrayList<>();
        for (Pair<Action, SearchState> state : currentPath) {
            newPath.add(new Pair<>(state.first, state.second));
        }
        return newPath;
    }

    private Solution getTrivialSolution() {
        List<Pair<Action, SearchState>> path = new ArrayList<>();
        path.add(new Pair<>(new StartAction(), this.problem.getStartState()));

        Action action = WalkAction.builder()
                .startLocation(this.problem.getStartLocation())
                .endLocation(this.problem.getEndLocation())
                .build();
        SearchState nextState = action.execute(this.problem.getStartState());
        path.add(new Pair<>(action, nextState));

        return new Solution(path);
    }

    private double calculateWaitTime(long currentTime, long lineId, LineDirection direction, long stopId) {
        Pair<Long, LineDirection> key = new Pair<>(lineId, direction);
        Timetable lineTimetable = this.getLineTimetable(lineId, direction);
        if (lineTimetable != null) {
            long stopWaitTime = this.timesBetweenStops.get(key).get(stopId);
            for (DepartureTime departureTime : lineTimetable.getDepartureTimes()) {
                long departureTimeInMS = getTimeInMilliseconds(departureTime.getFormattedValue());
                if (currentTime < departureTimeInMS + stopWaitTime) {
                    return (departureTimeInMS + stopWaitTime - currentTime) / MILLISECONDS_IN_HOUR;
                }
            }

            DepartureTime firstDepartureTime = lineTimetable.getDepartureTimes().get(0);
            long departureTimeInMS = getTimeInMilliseconds(firstDepartureTime.getFormattedValue());
            return (departureTimeInMS + stopWaitTime - currentTime + (24 * 3600 * 1000)) / MILLISECONDS_IN_HOUR; // add full day to calculation at the end // TODO check if this is needed
        } else {
            return Double.MAX_VALUE; // increase time for lines which don't have timetable so the algorithm doesn't take them into account
        }
    }

    private Timetable getLineTimetable(long lineId, LineDirection direction) {
        List<Timetable> dayTimetables = this.timetables.get(getCurrentTimetableDay());
        for (Timetable timetable : dayTimetables) {
            if (lineId == timetable.getLineId() && direction == timetable.getDirection()) {
                return timetable;
            }
        }
        return null;
    }

    private Line getLineById(Long lineId) {
        for (Line line : this.lines) {
            if (line.getId().equals(lineId)) {
                return line;
            }
        }
        return null;
    }

    private Stop getStopById(Long stopId) {
        for (Stop stop : this.stops) {
            if (stop.getId().equals(stopId)) {
                return stop;
            }
        }
        return null;
    }

    private List<RouteDto> convertSolutionsToRoutes(List<Solution> solutions) {
        List<RouteDto> routes = new ArrayList<>();

        for (Solution solution : solutions) {
            RouteDto route = solution.convertToRoute();
            List<ActionDto> busActions = route.getBusActions();
            if (!busActions.isEmpty()) {
                ActionDto firstBusAction = busActions.get(0);
                int index = route.getActions().indexOf(firstBusAction);
                ActionDto previousWalkAction = route.getActions().get(index - 1);
                route.setNextDeparture(this.getRouteNextDeparture(firstBusAction, previousWalkAction));
            }
            Map<Pair<Long, LineDirection>, Pair<Location, Location>> busActionsByLine = route.getLineBoundaryLocations();
            Map<Pair<Long, LineDirection>, List<Location>> routePath = new LinkedHashMap<>();
            for (Map.Entry<Pair<Long, LineDirection>, Pair<Location, Location>> entry : busActionsByLine.entrySet()) {
                Long lineId = entry.getKey().first;
                LineDirection lineDirection = entry.getKey().second;
                Location startLocation = entry.getValue().first;
                Location endLocation = entry.getValue().second;
                routePath.put(entry.getKey(), this.getRouteLinePath(lineId, lineDirection, startLocation, endLocation));
            }
            route.setPath(routePath);
            routes.add(route);
        }

        return routes;
    }

    @SneakyThrows
    private String getRouteNextDeparture(ActionDto firstBusAction, ActionDto previousWalkAction) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.forLanguageTag("sr-RS"));
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Timetable lineTimetable = this.getLineTimetable(firstBusAction.getLine().getId(), firstBusAction.getLineDirection());

        if (lineTimetable != null) {
            Pair<Long, LineDirection> key = new Pair<>(firstBusAction.getLine().getId(), firstBusAction.getLineDirection());
            long stopWaitTime = this.timesBetweenStops.get(key).get(firstBusAction.getStop().getId());

            for (DepartureTime departureTime : lineTimetable.getDepartureTimes()) {
                long departureTimeInMS = getTimeInMilliseconds(departureTime.getFormattedValue());
                if (this.problem.getStartTime() + previousWalkAction.getDuration() * MILLISECONDS_IN_MINUTE < departureTimeInMS + stopWaitTime) {
                    return dateFormat.format(new Date(departureTimeInMS + stopWaitTime)); // take into account time for the bus to arrive at the given station
                }
            }
        }
        return null;
    }

    private List<Location> getRouteLinePath(Long lineId, LineDirection lineDirection, Location startLocation, Location endLocation) {
        List<Location> lineLocations = this.locationRepository.findAllByLineIdAndLineDirection(lineId, lineDirection);

        if (this.getLineById(lineId).getNumber().equals("4") && LineDirection.A == lineDirection) {
            Collections.reverse(lineLocations); // for line 4, thanks to genial GSPNS data
        }

        Comparator<Location> startLocationComparator = (o1, o2) -> {
            double distance1 = calculateDistance(o1.getLatitude(), o1.getLongitude(), startLocation.getLatitude(), startLocation.getLongitude());
            double distance2 = calculateDistance(o2.getLatitude(), o2.getLongitude(), startLocation.getLatitude(), startLocation.getLongitude());
            return Double.compare(distance1, distance2);
        };
        Location nearestStartLocation = Collections.min(lineLocations, startLocationComparator);

        Comparator<Location> endLocationComparator = (o1, o2) -> {
            double distance1 = calculateDistance(o1.getLatitude(), o1.getLongitude(), endLocation.getLatitude(), endLocation.getLongitude());
            double distance2 = calculateDistance(o2.getLatitude(), o2.getLongitude(), endLocation.getLatitude(), endLocation.getLongitude());
            return Double.compare(distance1, distance2);
        };
        Location nearestEndLocation = Collections.min(lineLocations, endLocationComparator);

        int startIndex = lineLocations.indexOf(nearestStartLocation);
        int endIndex = lineLocations.indexOf(nearestEndLocation);

        List<Location> path = new ArrayList<>();
        if (startIndex != -1 && endIndex != -1 && startIndex <= endIndex) {
            path.addAll(lineLocations.subList(startIndex, endIndex + 1));
        }

        return path;
    }
}
