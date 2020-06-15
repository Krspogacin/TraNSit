package org.mad.transit.search;

import android.util.Pair;

import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.DepartureTime;
import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.model.Timetable;
import org.mad.transit.model.TimetableDay;
import org.mad.transit.repository.LineRepository;
import org.mad.transit.repository.StopRepository;
import org.mad.transit.repository.TimetableRepository;
import org.mad.transit.util.LocationsUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SearchService {

    private static final int SOLUTION_COUNT = 3;
    private static final int MAX_QUEUE_SIZE = 300000;
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    private LineRepository lineRepository;
    private StopRepository stopRepository;
    private TimetableRepository timetableRepository;
    private List<Line> lines;
    private List<Stop> stops;
    private List<LineStopDirection> linesStopsDirections;
    private Map<Pair<Long, LineDirection>, List<Long>> lineStopsMap;
    private Map<Long, List<Pair<Long, LineDirection>>> stopLinesMap;
    private Map<TimetableDay, List<Timetable>> timetables;
    private Map<Pair<Long, LineDirection>, Map<Long, Long>> timesBetweenStops;
    private RouteSearchProblem problem;

    @Inject
    public SearchService(LineRepository lineRepository, StopRepository stopRepository, TimetableRepository timetableRepository) {
        this.lineRepository = lineRepository;
        this.stopRepository = stopRepository;
        this.timetableRepository = timetableRepository;
    }

    public List<RouteDto> searchRoutes(RouteSearchProblem problem) {
        this.problem = problem;
        lines = lineRepository.findAll();
        stops = stopRepository.findAll();
        linesStopsDirections = stopRepository.findAllLinesStopsDirections();
        timetables = timetableRepository.findAll();

        fillLineStops();
        fillStopLines();
        calculateTimesBetweenStops();

        List<RouteDto> routes = new ArrayList<>();

        PriorityQueue<List<Pair<Action, SearchState>>> queue = new PriorityQueue<>(DEFAULT_INITIAL_CAPACITY, getComparator());
        queue.add(getInitialActionState(problem.getStartState()));

        List<Solution> solutions = new ArrayList<>();

        while (!queue.isEmpty() && solutions.size() < SOLUTION_COUNT) {

            if (queue.size() >= MAX_QUEUE_SIZE) {
                Solution trivialSolution = getTrivialSolution();
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

            for (Pair<Action, SearchState> nextState : getNextStates(currentState, currentPath)) {
                List<Pair<Action, SearchState>> newPath = clonePath(currentPath);
                newPath.add(nextState);
                queue.add(newPath);
            }
        }

        // convert solutions to routes
        for (Solution solution : solutions) {
            routes.add(solution.convertToRoute());
        }

        return routes;
    }

    private void fillLineStops() {
        lineStopsMap = new HashMap<>();
        for (Line line : lines) {
            for (LineDirection direction : LineDirection.values()) {
                List<Long> lineStops = new ArrayList<>();
                for (LineStopDirection lineStopDirection : linesStopsDirections) {
                    if (lineStopDirection.getLineId().equals(line.getId()) && lineStopDirection.getDirection() == direction &&
                            !lineStops.contains(lineStopDirection.getStopId())) {
                        lineStops.add(lineStopDirection.getStopId());
                    }
                }
                if (line.getNumber().equals("4")) {
                    Collections.reverse(lineStops); // for line 4, thanks to genial GSPNS data
                }
                lineStopsMap.put(new Pair<>(line.getId(), direction), lineStops);
            }
        }
    }

    private void fillStopLines() {
        stopLinesMap = new HashMap<>();
        for (Stop stop : stops) {
            List<Pair<Long, LineDirection>> stopLines = new ArrayList<>();
            for (LineStopDirection lineStopDirection : linesStopsDirections) {
                if (lineStopDirection.getStopId().equals(stop.getId())) {
                    stopLines.add(new Pair<>(lineStopDirection.getLineId(), lineStopDirection.getDirection()));
                }
            }
            stopLinesMap.put(stop.getId(), stopLines);
        }
    }

    private void calculateTimesBetweenStops() {
        timesBetweenStops = new HashMap<>();
        for (Line line : lines) {
            for (LineDirection direction : LineDirection.values()) {
                Pair<Long, LineDirection> key = new Pair<>(line.getId(), direction);
                List<Long> lineStops = lineStopsMap.get(key);
                timesBetweenStops.put(key, new HashMap<Long, Long>());
                if (lineStops != null && !lineStops.isEmpty()) {
                    long waitTime = 0;
                    timesBetweenStops.get(key).put(lineStops.get(0), waitTime);
                    for (int i = 1; i < lineStops.size(); i++) {
                        Stop previousStop = getStopById(lineStops.get(i - 1));
                        Stop currentStop = getStopById(lineStops.get(i));
                        if (previousStop != null && currentStop != null) {
                            waitTime += LocationsUtil.calculateDistance(previousStop.getLocation().getLatitude(), previousStop.getLocation().getLongitude(),
                                    currentStop.getLocation().getLatitude(), currentStop.getLocation().getLongitude()) * 90_000;// 3_600_000 / 40 (bus speed)
                            timesBetweenStops.get(key).put(currentStop.getId(), waitTime);
                        }
                    }
                }
            }
        }
    }

    private Comparator<List<Pair<Action, SearchState>>> getComparator() {
        return new Comparator<List<Pair<Action, SearchState>>>() {
            @Override
            public int compare(List<Pair<Action, SearchState>> o1, List<Pair<Action, SearchState>> o2) {
                double firstCost = o1.get(o1.size() - 1).second.aStarCost(problem.getEndLocation());
                double secondCost = o2.get(o2.size() - 1).second.aStarCost(problem.getEndLocation());

                return Double.compare(firstCost, secondCost);
            }
        };
    }

    private List<Pair<Action, SearchState>> getInitialActionState(SearchState startState) {
        List<Pair<Action, SearchState>> list = new ArrayList<>();
        list.add(new Pair<Action, SearchState>(new StartAction(), startState));
        return list;
    }

    private List<Pair<Action, SearchState>> getNextStates(SearchState currentState, List<Pair<Action, SearchState>> currentPath) {
        List<Pair<Action, SearchState>> list = new ArrayList<>();
        getNextWalkStates(list, currentState);
        getNextBusStates(list, currentState, currentPath);
        return list;
    }

    private void getNextWalkStates(List<Pair<Action, SearchState>> list, SearchState currentState) {
        if (currentState.getStop() != null) {
            Action action = WalkAction.builder()
                    .startLocation(currentState.getLocation())
                    .endLocation(problem.getEndLocation())
                    .build();
            list.add(new Pair<>(action, action.execute(currentState)));
            return;
        }
        List<Long> nearestLineStops = findNearestLineStops(currentState.getLocation());
        for (Long stopId : nearestLineStops) {
            Stop stop = getStopById(stopId);
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
                .endLocation(problem.getEndLocation())
                .build();
        list.add(new Pair<>(action, action.execute(currentState)));
    }

    private void getNextBusStates(List<Pair<Action, SearchState>> list, SearchState
            currentState, List<Pair<Action, SearchState>> currentPath) {
        if (currentState.getStop() != null) {
            Map<Pair<Long, LineDirection>, Long> nextLineStops = findNextLineStop(currentState.getStop());
            for (Map.Entry<Pair<Long, LineDirection>, Long> entry : nextLineStops.entrySet()) {
                Stop nextStop = getStopById(entry.getValue());
                if (nextStop != null) {
                    Action action = BusAction.builder()
                            .startLocation(currentState.getLocation())
                            .endLocation(nextStop.getLocation())
                            .line(getLineById(entry.getKey().first))
                            .build();

                    SearchState nextState = action.execute(currentState);
                    nextState.setStop(nextStop);

                    if (isWalkActionPrevious(currentPath) || isChangingLine(currentState, nextState)) {
                        double waitTime = calculateWaitTime(problem.getStartTime() + currentState.getTimeElapsedInMilliseconds(),
                                entry.getKey().first, entry.getKey().second, nextStop.getId());
                        nextState.setTimeElapsed(nextState.getTimeElapsed() + waitTime);
                    }
                    list.add(new Pair<>(action, nextState));
                }
            }
        }
    }

    private boolean isWalkActionPrevious(List<Pair<Action, SearchState>> currentPath) {
        return currentPath.get(currentPath.size() - 1).first instanceof WalkAction;
    }

    private boolean isChangingLine(SearchState currentState, SearchState nextState) {
        return currentState.getLine() != null && !currentState.getLine().getId().equals(nextState.getLine().getId());
    }

    public Map<Pair<Long, LineDirection>, Long> findNextLineStop(Stop stop) {
        Map<Pair<Long, LineDirection>, Long> nextLineStops = new HashMap<>();
        List<Pair<Long, LineDirection>> stopLines = stopLinesMap.get(stop.getId());
        if (stopLines != null) {
            for (Pair<Long, LineDirection> pair : stopLines) {
                List<Long> lineStops = lineStopsMap.get(pair);
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
        for (Line line : lines) {
            for (LineDirection direction : LineDirection.values()) {
                List<Long> lineStops = lineStopsMap.get(new Pair<>(line.getId(), direction));
                if (lineStops != null && !lineStops.isEmpty()) {
                    double minDist = 9999999;
                    int minIndex = 0;
                    for (int i = 0; i < lineStops.size(); i++) {
                        Stop stop = getStopById(lineStops.get(i));
                        if (stop != null) {
                            double dist = LocationsUtil.calculateDistance(startLocation.getLatitude(), startLocation.getLongitude(),
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
        path.add(new Pair<Action, SearchState>(new StartAction(), problem.getStartState()));

        Action action = WalkAction.builder()
                .startLocation(problem.getStartLocation())
                .endLocation(problem.getEndLocation())
                .build();
        SearchState nextState = action.execute(problem.getStartState());
        path.add(new Pair<>(action, nextState));

        return new Solution(path);
    }

    private double calculateWaitTime(long currentTime, long lineId, LineDirection direction,
                                     long stopId) {
        Pair<Long, LineDirection> key = new Pair<>(lineId, direction);
        Timetable lineTimetable = getLineTimetable(lineId, direction);
        if (lineTimetable != null) {
            long stopWaitTime = timesBetweenStops.get(key).get(stopId);
            for (DepartureTime departureTime : lineTimetable.getDepartureTimes()) {
                long departureTimeInMS = getDepartureTimeInMilliseconds(departureTime.getFormattedValue());
                if (currentTime < departureTimeInMS + stopWaitTime) {
                    return (departureTimeInMS + stopWaitTime - currentTime) / 3_600_000D;
                }
            }

            DepartureTime firstDepartureTime = lineTimetable.getDepartureTimes().get(0);
            long departureTimeInMS = getDepartureTimeInMilliseconds(firstDepartureTime.getFormattedValue());
            return (departureTimeInMS + stopWaitTime - currentTime + (24 * 3600 * 1000)) / 3_600_000D; // add full day to calculation at the end
        } else {
            return Double.MAX_VALUE; // increase time for lines which don't have timetable so the algorithm doesn't take them into account
        }
    }

    private Timetable getLineTimetable(long lineId, LineDirection direction) {
        List<Timetable> dayTimetables = timetables.get(getCurrentTimetableDay());
        for (Timetable timetable : dayTimetables) {
            if (lineId == timetable.getLineId() && direction == timetable.getDirection()) {
                return timetable;
            }
        }
        return null;
    }

    private TimetableDay getCurrentTimetableDay() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY:
                return TimetableDay.SUNDAY;
            case Calendar.SATURDAY:
                return TimetableDay.SATURDAY;
            default:
                return TimetableDay.WORKDAY;
        }
    }

    private long getDepartureTimeInMilliseconds(String departureTime) {
        String[] parts = departureTime.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return (60 * hours + minutes) * 60_000L;
    }

    private Line getLineById(Long lineId) {
        for (Line line : lines) {
            if (line.getId().equals(lineId)) {
                return line;
            }
        }
        return null;
    }

    private Stop getStopById(Long stopId) {
        for (Stop stop : stops) {
            if (stop.getId().equals(stopId)) {
                return stop;
            }
        }
        return null;
    }
}
