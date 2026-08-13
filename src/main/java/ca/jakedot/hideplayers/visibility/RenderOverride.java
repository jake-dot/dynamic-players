package ca.jakedot.hideplayers.visibility;

public enum RenderOverride {
    NONE(false, null),
    TRANSLUCENT(true, null),
    TRANSLUCENT_CULL(true, true),
    TRANSLUCENT_NO_CULL(true, false);

    private final boolean overrideTransparency;
    private final Boolean shouldCull;

    RenderOverride(boolean overrideTransparency, Boolean shouldCull) {
        this.overrideTransparency = overrideTransparency;
        this.shouldCull = shouldCull;
    }

    public boolean overridesTransparency() {
        return this.overrideTransparency;
    }

    public Boolean shouldCull() {
        return shouldCull;
    }
}
