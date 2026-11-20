package ca.jakedot.hideplayers.visibility;

import ca.jakedot.hideplayers.HidePlayers;
import ca.jakedot.hideplayers.PlayerGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class VisibilityManager {
    private final static Set<String> whitelist = new HashSet<>();

    private static boolean whitelistEnabled = true;
    private static boolean visibilityAll = true;
    private static boolean visibilityNear = true;
    private static boolean translucentAll;
    private static boolean translucentNear;

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

    public static boolean translucentAll() {
        return VisibilityManager.translucentAll;
    }

    public static boolean translucentNear() {
        return VisibilityManager.translucentNear;
    }

    public static void toggleWhitelist() {
        VisibilityManager.whitelistEnabled = !VisibilityManager.whitelistEnabled;
    }

    public static void toggleVisibilityAll() {
        VisibilityManager.visibilityAll = !VisibilityManager.visibilityAll;

        if (HidePlayers.getConfig().transitionTime() > 0) {
            float startOpacity = VisibilityManager.visibilityAll ? 0.0F : 1.0F;
            float endOpacity = VisibilityManager.visibilityAll ? 1.0F : 0.0F;
            VisibilityTransition transition = new VisibilityTransition(HidePlayers.getConfig().transitionTime(), startOpacity, endOpacity);
            if (VisibilityManager.globalVisibilityTransition != null) {
                transition = transition.trim(VisibilityManager.globalVisibilityTransition.getOpacity());
            }
            VisibilityManager.globalVisibilityTransition = transition;
        } else {
            VisibilityManager.globalVisibilityTransition = null;
        }
    }

    public static void toggleVisibilityNear() {
        VisibilityManager.visibilityNear = !VisibilityManager.visibilityNear;

        float startOpacity = VisibilityManager.visibilityNear ? 0.0F : 1.0F;
        float endOpacity = VisibilityManager.visibilityNear ? 1.0F : 0.0F;
        VisibilityTransition transition = new VisibilityTransition(150L, startOpacity, endOpacity);
        if (VisibilityManager.nearVisibilityTransition != null) {
            transition = transition.trim(VisibilityManager.nearVisibilityTransition.getOpacity());
        }
        VisibilityManager.nearVisibilityTransition = transition;
    }

    public static void toggleTranslucentAll() {
        VisibilityManager.translucentAll = !VisibilityManager.translucentAll;

        if (HidePlayers.getConfig().transitionTime() > 0) {
            float translucentOpacity = (float) HidePlayers.getConfig().allPlayersHiddenOpacity();
            float regularOpacity = VisibilityManager.visibilityAll ? 1.0F : 0.0F;
            float startOpacity = VisibilityManager.translucentAll ? regularOpacity : translucentOpacity;
            float endOpacity = VisibilityManager.translucentAll ? translucentOpacity : regularOpacity;
            VisibilityTransition transition = new VisibilityTransition(HidePlayers.getConfig().transitionTime(), startOpacity, endOpacity);
            if (VisibilityManager.globalVisibilityTransition != null) {
                transition = transition.trim(VisibilityManager.globalVisibilityTransition.getOpacity());
            }
            VisibilityManager.globalVisibilityTransition = transition;
        } else {
            VisibilityManager.globalVisibilityTransition = null;
        }
    }

    public static void toggleTranslucentNear() {
        VisibilityManager.translucentNear = !VisibilityManager.translucentNear;

        float translucentOpacity = (float) HidePlayers.getConfig().nearbyPlayersHiddenOpacity();
        float regularOpacity = VisibilityManager.visibilityNear ? 1.0F : 0.0F;
        float startOpacity = VisibilityManager.translucentNear ? regularOpacity : translucentOpacity;
        float endOpacity = VisibilityManager.translucentNear ? translucentOpacity : regularOpacity;
        VisibilityTransition transition = new VisibilityTransition(150L, startOpacity, endOpacity);
        if (VisibilityManager.nearVisibilityTransition != null) {
            transition = transition.trim(VisibilityManager.nearVisibilityTransition.getOpacity());
        }
        VisibilityManager.nearVisibilityTransition = transition;
    }

    public static boolean isWhitelisted(String player) {
        player = player.toLowerCase(Locale.ROOT);
        return VisibilityManager.whitelist.contains(player);
    }

    private static VisibilityTransition globalVisibilityTransition;
    private static VisibilityTransition nearVisibilityTransition;


    public static VisibilityTransition getGlobalTransition() {
        return VisibilityManager.globalVisibilityTransition;
    }

    public static VisibilityTransition getNearTransition() {
        return VisibilityManager.nearVisibilityTransition;
    }

    public static void pruneTransitions() {
        if (VisibilityManager.globalVisibilityTransition != null && VisibilityManager.globalVisibilityTransition.isComplete()) {
            VisibilityManager.globalVisibilityTransition = null;
        }
        if (VisibilityManager.nearVisibilityTransition != null && VisibilityManager.nearVisibilityTransition.isComplete()) {
            VisibilityManager.nearVisibilityTransition = null;
        }
    }
}
