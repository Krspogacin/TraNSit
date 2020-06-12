package org.mad.transit.search;

import android.util.Pair;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class Solution {
    private List<Pair<Action, SearchState>> actions;
    private Set<Long> usedLines;

    public Solution(List<Pair<Action, SearchState>> actions) {
        this.actions = actions;
        this.usedLines = new HashSet<>();
        for (Pair<Action, SearchState> pair : actions) {
            if (pair.first instanceof BusAction) {
                BusAction action = (BusAction) pair.first;
                usedLines.add(action.getLine().getId());
            }
        }
    }

    public boolean isDifferent(Solution potentialSolution) {
        return usedLines.isEmpty() ||
                Sets.difference(usedLines, potentialSolution.getUsedLines()).size() != 0 ||
                Sets.difference(potentialSolution.getUsedLines(), usedLines).size() != 0;
    }
}
