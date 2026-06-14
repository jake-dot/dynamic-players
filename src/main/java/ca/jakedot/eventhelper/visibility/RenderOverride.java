package ca.jakedot.eventhelper.visibility;

public enum RenderOverride {
    NONE(false, true),
    TRANSLUCENT(true, false),
    TRANSLUCENT_NO_CULL(true, true);

    private final boolean overrideTransparency;
    private final boolean noCull;

    RenderOverride(boolean overrideTransparency, boolean noCull) {
        this.overrideTransparency = overrideTransparency;
        this.noCull = noCull;
    }

    public boolean overridesTransparency() {
        return this.overrideTransparency;
    }

    public boolean noCull() {
        return noCull;
    }
}
