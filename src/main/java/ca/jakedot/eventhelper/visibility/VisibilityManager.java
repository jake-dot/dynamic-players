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
    private static boolean playersHidden;

    public static void updateWhitelist(List<PlayerGroup> whitelistGroups) {
        VisibilityManager.whitelist.clear();
        whitelistGroups
                .stream()
                .filter(PlayerGroup::enabled)
                .map(PlayerGroup::players)
                .forEach(VisibilityManager.whitelist::addAll);
    }

    public static boolean arePlayersHidden() {
        return VisibilityManager.playersHidden;
    }

    public static void toggleVisibility() {
        VisibilityManager.playersHidden = !VisibilityManager.playersHidden;
        long transitionTime = EventHelper.getConfig().transitionTime();
        if (transitionTime > 0) {
            if (VisibilityManager.visibilityTransition == null) {
                VisibilityManager.visibilityTransition = new VisibilityTransition(transitionTime, VisibilityManager.playersHidden);
            } else {
                VisibilityManager.visibilityTransition = VisibilityManager.visibilityTransition.reverse(); // TODO make this adapt to different durations
            }
        } else {
            VisibilityManager.visibilityTransition = null;
        }
    }

    public static boolean isWhitelisted(String player) {
        return VisibilityManager.whitelist.contains(player); // TODO toggle whitelist
    }

    public static boolean isVisible(String player) {
        if (!VisibilityManager.playersHidden) {
            return true;
        } else {
            VisibilityTransition transition = getTransition(player);
            if (transition != null && transition.getOpacity() == 1.0F) {
                return true;
            }
            return isWhitelisted(player);
        }
    }

    private static VisibilityTransition visibilityTransition;
    private final static Map<String, VisibilityTransition> visibilityTransitionMap = new HashMap<>();

    public static VisibilityTransition getTransition(String player) {
        return VisibilityManager.visibilityTransitionMap.getOrDefault(player, VisibilityManager.visibilityTransition);
    }

    public static void pruneTransitions() {
        if (VisibilityManager.visibilityTransition != null && VisibilityManager.visibilityTransition.isComplete()) {
            VisibilityManager.visibilityTransition = null;
        }
        for (Map.Entry<String, VisibilityTransition> entry : VisibilityManager.visibilityTransitionMap.entrySet()) {
            if (entry.getValue().isComplete()) {
                VisibilityManager.visibilityTransitionMap.remove(entry.getKey());
            }
        }
    }
}
