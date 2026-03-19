package ca.jakedot.eventhelper.visibility;

import ca.jakedot.eventhelper.EventHelper;
import ca.jakedot.eventhelper.PlayerGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class VisibilityManager {
    private final static Set<String> whitelist = new HashSet<>();

    private static boolean allPlayersHidden;
    private static boolean nearPlayersHidden;

    public static void updateWhitelist(List<PlayerGroup> whitelistGroups) {
        VisibilityManager.whitelist.clear();
        whitelistGroups
                .stream()
                .filter(PlayerGroup::enabled)
                .map(PlayerGroup::players)
                .forEach(VisibilityManager.whitelist::addAll);
    }

    public static boolean playersHidden() {
        return VisibilityManager.allPlayersHidden;
    }

    public static boolean nearPlayersHidden() {
        return VisibilityManager.nearPlayersHidden;
    }

    public static void toggleVisibilityAll() {
        VisibilityManager.allPlayersHidden = !VisibilityManager.allPlayersHidden;
        long transitionTime = EventHelper.getConfig().transitionTime();
        if (transitionTime > 0) {
            if (VisibilityManager.globalVisibilityTransition == null) {
                VisibilityManager.globalVisibilityTransition = new VisibilityTransition(transitionTime, VisibilityManager.allPlayersHidden);
            } else {
                VisibilityManager.globalVisibilityTransition = VisibilityManager.globalVisibilityTransition.reverse(); // TODO make this adapt to different durations
            }
        } else {
            VisibilityManager.globalVisibilityTransition = null;
        }
    }

    public static void toggleVisibilityNear() {
        VisibilityManager.nearPlayersHidden = !VisibilityManager.nearPlayersHidden;

        VisibilityManager.nearVisibilityTransition = new VisibilityTransition(150L, VisibilityManager.nearPlayersHidden);
    }

    public static boolean isWhitelisted(String player) {
        return VisibilityManager.whitelist.contains(player); // TODO toggle whitelist
    }

    private static VisibilityTransition globalVisibilityTransition;
    private static VisibilityTransition nearVisibilityTransition;
    private final static Map<String, VisibilityTransition> visibilityTransitionMap = new HashMap<>();

    public static VisibilityTransition getTransition(String player) {
        return VisibilityManager.visibilityTransitionMap.getOrDefault(player, VisibilityManager.globalVisibilityTransition);
    }

    public static void pruneTransitions() {
        if (VisibilityManager.globalVisibilityTransition != null && VisibilityManager.globalVisibilityTransition.isComplete()) {
            VisibilityManager.globalVisibilityTransition = null;
        }
        if (VisibilityManager.nearVisibilityTransition != null && VisibilityManager.nearVisibilityTransition.isComplete()) {
            VisibilityManager.nearVisibilityTransition = null;
        }
        for (Map.Entry<String, VisibilityTransition> entry : VisibilityManager.visibilityTransitionMap.entrySet()) {
            if (entry.getValue().isComplete()) {
                VisibilityManager.visibilityTransitionMap.remove(entry.getKey());
            }
        }
    }
}
