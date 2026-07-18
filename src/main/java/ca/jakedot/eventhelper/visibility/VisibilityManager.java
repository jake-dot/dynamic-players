package ca.jakedot.eventhelper.visibility;

import ca.jakedot.eventhelper.EventHelper;
import ca.jakedot.eventhelper.PlayerGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VisibilityManager {
    private final static Set<String> whitelist = new HashSet<>();

    private static boolean whitelistEnabled = true;
    private static boolean allPlayersHidden = false;
    private static boolean nearPlayersHidden = false;

    public static void updateWhitelist(List<PlayerGroup> whitelistGroups) {
        VisibilityManager.whitelist.clear();
        whitelistGroups
                .stream()
                .filter(PlayerGroup::enabled)
                .map(PlayerGroup::players)
                .forEach(VisibilityManager.whitelist::addAll);
    }

    public static boolean whitelistEnabled() {
        return VisibilityManager.whitelistEnabled;
    }

    public static boolean playersHidden() {
        return VisibilityManager.allPlayersHidden;
    }

    public static boolean nearPlayersHidden() {
        return VisibilityManager.nearPlayersHidden;
    }

    public static boolean updateWhitelistState(boolean whitelistEnabled) {
        if (VisibilityManager.whitelistEnabled == whitelistEnabled) {
            return false;
        }
        VisibilityManager.whitelistEnabled = whitelistEnabled;
        return true;
    }

    public static boolean updateVisibilityAll(boolean visibility) {
        if (VisibilityManager.allPlayersHidden != visibility) {
            return false;
        }
        VisibilityManager.allPlayersHidden = !visibility;
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
        return true;
    }

    public static boolean updateVisibilityNear(boolean visibility) {
        if (VisibilityManager.nearPlayersHidden != visibility) {
            return false;
        }
        VisibilityManager.nearPlayersHidden = !visibility;

        VisibilityManager.nearVisibilityTransition = new VisibilityTransition(150L, VisibilityManager.nearPlayersHidden);
        return true;
    }

    public static boolean isWhitelisted(String player) {
        return VisibilityManager.whitelist.contains(player);
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
