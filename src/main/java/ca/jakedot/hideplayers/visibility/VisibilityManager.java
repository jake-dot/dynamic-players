package ca.jakedot.hideplayers.visibility;

import ca.jakedot.hideplayers.HidePlayers;
import ca.jakedot.hideplayers.PlayerGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class VisibilityManager {
    private final static Set<String> whitelist = new HashSet<>();

    private static boolean whitelistEnabled = true;
    private static boolean visibilityAll = true;
    private static boolean visibilityNear = true;

    public static void updateWhitelist(List<PlayerGroup> whitelistGroups) {
        VisibilityManager.whitelist.clear();
        whitelistGroups.stream()
                .filter(PlayerGroup::enabled)
                .map(PlayerGroup::players)
                .forEach(group -> {
                    group.forEach(player -> whitelist.add(player.toLowerCase(Locale.ROOT)));
                });
    }

    public static boolean whitelistEnabled() {
        return VisibilityManager.whitelistEnabled;
    }

    public static boolean visibilityAll() {
        return VisibilityManager.visibilityAll;
    }

    public static boolean visibilityNear() {
        return VisibilityManager.visibilityNear;
    }

    public static boolean updateWhitelistState(boolean whitelistEnabled) {
        if (VisibilityManager.whitelistEnabled == whitelistEnabled) {
            return false;
        }
        VisibilityManager.whitelistEnabled = whitelistEnabled;
        return true;
    }

    public static void updateVisibilityAll(boolean visibility) {
        if (VisibilityManager.visibilityAll == visibility) {
            return;
        }
        VisibilityManager.visibilityAll = visibility;
        long transitionTime = HidePlayers.getConfig().transitionTime();
        if (transitionTime > 0) {
            if (VisibilityManager.globalVisibilityTransition == null) {
                VisibilityManager.globalVisibilityTransition = new VisibilityTransition(transitionTime, !visibility);
            } else {
                VisibilityManager.globalVisibilityTransition = VisibilityManager.globalVisibilityTransition.reverse(); // TODO make this adapt to different durations
            }
        } else {
            VisibilityManager.globalVisibilityTransition = null;
        }
    }

    public static boolean updateVisibilityNear(boolean visibility) {
        if (VisibilityManager.visibilityNear == visibility) {
            return false;
        }
        VisibilityManager.visibilityNear = visibility;

        VisibilityManager.nearVisibilityTransition = new VisibilityTransition(150L, !visibility);
        return true;
    }

    public static boolean isWhitelisted(String player) {
        player = player.toLowerCase(Locale.ROOT);
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
