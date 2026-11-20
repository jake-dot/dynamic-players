package ca.jakedot.hideplayers.visibility;

public class VisibilityTransition {
    private final long transitionStart;
    private final long duration;
    private final float startOpacity;
    private final float endOpacity;

    public VisibilityTransition(long duration, float startOpacity, float endOpacity) {
        this.transitionStart = System.currentTimeMillis();
        this.duration = duration;
        this.startOpacity = startOpacity;
        this.endOpacity = endOpacity;
    }

    public float getMinOpacity() {
        return Math.min(this.startOpacity, this.endOpacity);
    }

    public boolean isComplete() {
        return this.getTimeElapsed() >= this.duration;
    }

    public float getOpacity() {
        float diff = this.endOpacity - this.startOpacity;
        return this.startOpacity + diff * this.getProgress();
    }

    public VisibilityTransition trim(float opacity) {
        return new VisibilityTransition(this.duration - (long) (this.duration * Math.abs(opacity - this.startOpacity)), opacity, this.endOpacity);
    }

    private long getTimeElapsed() {
        return System.currentTimeMillis() - this.transitionStart;
    }

    private float getProgress() {
        return Math.clamp((float) this.getTimeElapsed() / this.duration, 0.0F, 1.0F);
    }
}
