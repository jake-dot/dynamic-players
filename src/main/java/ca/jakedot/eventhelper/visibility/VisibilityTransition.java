package ca.jakedot.eventhelper.visibility;

public class VisibilityTransition {
    private final long duration;
    private final long start;
    private final boolean startVisible;

    public VisibilityTransition(long duration, boolean startVisible) {
        this(duration, System.currentTimeMillis(), startVisible);
    }

    public VisibilityTransition(long duration, long start, boolean startVisible) {
        this.duration = duration;
        this.start = start;
        this.startVisible = startVisible;
    }

    public boolean isComplete() {
        return this.getTimeElapsed() >= this.duration;
    }

    public float getOpacity() {
        float rawOpacity = this.startVisible ? 1.0F - this.getProgress() : this.getProgress();
        if (rawOpacity == 0.0F || rawOpacity == 1.0F) {
            return rawOpacity;
        }
        return 0.05F + rawOpacity * 0.9F;
    }

    public VisibilityTransition reverse() {
        long remaining = this.duration - this.getTimeElapsed();
        long inverseStart = System.currentTimeMillis() - remaining;
        return new VisibilityTransition(duration, inverseStart, !this.startVisible);
    }

    private long getTimeElapsed() {
        return System.currentTimeMillis() - this.start;
    }

    private float getProgress() {
        return Math.clamp((float) this.getTimeElapsed() / this.duration, 0.0F, 1.0F);
    }
}
