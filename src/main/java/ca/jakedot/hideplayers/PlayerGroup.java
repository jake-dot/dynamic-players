package ca.jakedot.hideplayers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public record PlayerGroup(String name, List<String> players, boolean enabled) {
    private static final String[] EXAMPLE_PLAYER_NAMES = new String[]{
            "Player1",
            "Player2",
            "Player3",
            "Player4",
            "Player5",
            "Player6",
            "Player8",
            "Player9",
            "Player123",
            "Player123",
            "Player123",
            "Player456",
            "Player789",
            "Steve",
            "Steve",
            "Alex",
            "Alex",
            "Herobrine"
    };

    static {
        Collections.shuffle(Arrays.asList(EXAMPLE_PLAYER_NAMES));
    }

    public static PlayerGroup createDefaultGroup(int index) {
        return new PlayerGroup(String.format("Group %d", index), List.of(getExamplePlayer(0)), true);
    }

    public static String getExamplePlayer(int index) {
        index = index % EXAMPLE_PLAYER_NAMES.length;
        return EXAMPLE_PLAYER_NAMES[index];
    }
}