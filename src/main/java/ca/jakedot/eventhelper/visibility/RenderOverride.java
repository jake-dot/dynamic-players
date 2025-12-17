package ca.jakedot.eventhelper.visibility;

import net.minecraft.client.renderer.RenderType;

import java.util.HashMap;
import java.util.Map;

public enum RenderOverride {
    NONE(false, false),
    TRANSLUCENT(true, false),
    TRANSLUCENT_CULL(true, true);

    private final Map<RenderType, RenderType> renderTypeCache = new HashMap<>();
    private final boolean overrideTransparency;
    private final boolean overrideCull;

    RenderOverride(boolean overrideTransparency, boolean overrideCull) {
        this.overrideTransparency = overrideTransparency;
        this.overrideCull = overrideCull;
    }

    public boolean overridesTransparency() {
        return this.overrideTransparency;
    }

    public boolean overridesCull() {
        return overrideCull;
    }

    public boolean isRenderTypeCached(RenderType renderType) {
        return this.renderTypeCache.containsKey(renderType);
    }

    public RenderType getCachedRenderType(RenderType renderType) {
        RenderType cachedRenderType = this.renderTypeCache.get(renderType);
        if (cachedRenderType == null) {
            return renderType;
        }
        return cachedRenderType;
    }

    public void cacheRenderType(RenderType key, RenderType value) {
        this.renderTypeCache.put(key, value);
    }

    public void clearCache() {
        this.renderTypeCache.clear();
    }
}
