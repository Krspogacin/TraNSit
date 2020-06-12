package org.mad.transit.search;

import android.util.Log;
import android.util.Pair;

import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.repository.LineRepository;
import org.mad.transit.repository.StopRepository;
import org.mad.transit.util.LocationsUtil;

import java.util.ArrayList;
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
    private List<Line> lines;
    private List<Stop> stops;
    private List<LineStopDirection> linesStopsDirections;
    private RouteSearchProblem problem;

    @Inject
    public SearchService(LineRepository lineRepository, StopRepository stopRepository) {
        this.lineRepository = lineRepository;
        this.stopRepository = stopRepository;
    }

    public List<Route> searchRoutes(RouteSearchProblem problem) {
        this.problem = problem;
        lines = lineRepository.findAll();
        stops = stopRepository.findAll();
        linesStopsDirections = stopRepository.findAllLinesStopsDirections();

        List<Route> routes = new ArrayList<>();

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
            Log.i("SOLUTION", "******************* TIME ELAPSED: " + solution.getActions().get(solution.getActions().size() - 1).second.getTimeElapsed() * 60);
            for (Pair<Action, SearchState> pair : solution.getActions()) {
                if (pair.first instanceof BusAction) {
                    BusAction busAction = (BusAction) pair.first;
                    Log.i("BUS ACTION", "FROM: " + busAction.getStartLocation() + ", TO: " + busAction.getEndLocation() +
                            ", LINE: " + busAction.getLine().getNumber() + ", STOP: " + pair.second.getStop().getTitle());
                } else if (pair.first instanceof WalkAction) {
                    Log.i("WALK ACTION", "FROM: " + pair.first.getStartLocation() + ", TO: " + pair.first.getEndLocation());
                } else {
                    Log.i("START ACTION", "FROM: " + pair.first.getStartLocation() + ", TO: " + pair.first.getEndLocation());
                }
            }
        }

        return routes;
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
        List<Stop> nearestLineStops = findNearestLineStops(currentState.getLocation());
        for (Stop stop : nearestLineStops) {
            Action action = WalkAction.builder()
                    .startLocation(currentState.getLocation())
                    .endLocation(stop.getLocation())
                    .build();
            SearchState nextState = action.execute(currentState);
            nextState.setStop(stop);
            list.add(new Pair<>(action, nextState));
        }
        Action action = WalkAction.builder()
                .startLocation(currentState.getLocation())
                .endLocation(problem.getEndLocation())
                .build();
        list.add(new Pair<>(action, action.execute(currentState)));
    }

    private void getNextBusStates(List<Pair<Action, SearchState>> list, SearchState currentState, List<Pair<Action, SearchState>> currentPath) {
        if (currentState.getStop() != null) {
            Map<Pair<Line, LineDirection>, Stop> nextLineStops = findNextLineStop(currentState.getStop());
            for (Map.Entry<Pair<Line, LineDirection>, Stop> entry : nextLineStops.entrySet()) {
                Stop nextStop = entry.getValue();
                Action action = BusAction.builder()
                        .startLocation(currentState.getLocation())
                        .endLocation(nextStop.getLocation())
                        .line(entry.getKey().first)
                        .build();

                SearchState nextState = action.execute(currentState);
                nextState.setStop(nextStop);
                if (currentPath.get(currentPath.size() - 1).first instanceof WalkAction) {
                    //TODO add wait time for the chosen line
                }
                list.add(new Pair<>(action, nextState));
            }
        }
    }

    public Map<Pair<Line, LineDirection>, Stop> findNextLineStop(Stop stop) {
        Map<Pair<Line, LineDirection>, Stop> nextLineStops = new HashMap<>();
        Map<Line, LineDirection> stopLines = getStopLines(stop);
        for (Map.Entry<Line, LineDirection> entry : stopLines.entrySet()) {
            List<Stop> lineStops = getLineStops(entry.getKey().getId(), entry.getValue());
            int nextStopIndex = lineStops.indexOf(stop) + 1;
            if (nextStopIndex < lineStops.size()) {
                nextLineStops.put(new Pair<>(entry.getKey(), entry.getValue()), lineStops.get(nextStopIndex));
            }
        }
        return nextLineStops;
    }

    private List<Stop> findNearestLineStops(Location startLocation) {
        List<Stop> nearestStops = new ArrayList<>();
        for (Line line : lines) {
            for (LineDirection direction : LineDirection.values()) {
                List<Stop> lineStops = getLineStops(line.getId(), direction);
                if (!lineStops.isEmpty()) {
                    double minDist = 9999999;
                    int minIndex = 0;
                    for (int i = 0; i < lineStops.size(); i++) {
                        Stop stop = lineStops.get(i);
                        double dist = LocationsUtil.calculateDistance(startLocation.getLatitude(), startLocation.getLongitude(),
                                stop.getLocation().getLatitude(), stop.getLocation().getLongitude());
                        if (dist < minDist) {
                            minDist = dist;
                            minIndex = i;
                        }
                    }
                    nearestStops.add(lineStops.get(minIndex));
                }
            }
        }
        return nearestStops;
    }

    private List<Stop> getLineStops(Long lineId, LineDirection direction) {
        List<Stop> lineStops = new ArrayList<>();
        List<Long> stopIds = new ArrayList<>();
        for (LineStopDirection lineStopDirection : linesStopsDirections) {
            if (lineStopDirection.getLineId().equals(lineId) && direction == lineStopDirection.getDirection()) {
                stopIds.add(lineStopDirection.getStopId());
            }
        }
        for (Long stopId : stopIds) {
            for (Stop stop : stops) {
                if (stop.getId().equals(stopId)) {
                    lineStops.add(stop);
                    break;
                }
            }
        }
        return lineStops;
    }

    private Map<Line, LineDirection> getStopLines(Stop stop) {
        Map<Line, LineDirection> stopLinesDirection = new HashMap<>();
        Map<Long, LineDirection> lineDirectionIds = new HashMap<>();
        for (LineStopDirection lineStopDirection : linesStopsDirections) {
            if (lineStopDirection.getStopId().equals(stop.getId())) {
                lineDirectionIds.put(lineStopDirection.getLineId(), lineStopDirection.getDirection());
            }
        }
        for (Map.Entry<Long, LineDirection> entry : lineDirectionIds.entrySet()) {
            for (Line line : lines) {
                if (line.getId().equals(entry.getKey())) {
                    stopLinesDirection.put(line, entry.getValue());
                    break;
                }
            }
        }
        return stopLinesDirection;
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
}
