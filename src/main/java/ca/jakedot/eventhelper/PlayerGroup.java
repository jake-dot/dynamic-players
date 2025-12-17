package ca.jakedot.eventhelper;

import java.util.List;

public record PlayerGroup(String name, List<String> players, boolean enabled) {
    public static PlayerGroup createDefaultGroup(int index) {
        return new PlayerGroup(String.format("Group %d", index), List.of(getExamplePlayer()), true);
    }

    public static String getExamplePlayer() {
        return "Player123";
    }
}