package ca.jakedot.eventhelper.visibility;

import ca.jakedot.eventhelper.EventHelper;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.util.ARGB;

import java.util.HashMap;
import java.util.Map;

public class RenderUtils {
    private static RemotePlayer currentRender;
    private static RenderOverride renderOverride = RenderOverride.NONE;
    private static float targetOpacity = 1.0F;

    private static final Map<BufferBuilder, RenderOverride> bufferOverrideMap = new HashMap<>();

    public static void setCurrentEntityRender(RemotePlayer entity) {
        RenderUtils.currentRender = entity;
        String identifier = RenderUtils.currentRender.getGameProfile().getName();

        if (VisibilityManager.isWhitelisted(identifier)) {
            RenderUtils.targetOpacity = 1.0F;
        }

        if (VisibilityManager.nearPlayersHidden()) { // && does not bypass via whitelist
            AbstractClientPlayer self = Minecraft.getInstance().player;
            float threshold = (float) EventHelper.getConfig().nearbyPlayerDistance();
            float transitionRange = 1.0F;
            float dist = self.distanceTo(entity); // TODO get distance between bounding box edges
            if (dist <= threshold - transitionRange) {
                RenderUtils.targetOpacity = 0.0F;
            } else if (dist < threshold) {
                float transitionDist = dist - (threshold - transitionRange);
                RenderUtils.targetOpacity = Math.clamp(transitionDist / transitionRange, 0.0F, 1.0F); // TODO add min opacity from config
            }
        }

        if (RenderUtils.targetOpacity == 0.0F) {
            return;
        }

        if (!VisibilityManager.isWhitelisted(identifier)) {
            VisibilityTransition transition = VisibilityManager.getTransition(identifier);
            float newOpacity;
            if (transition != null) {
                newOpacity = transition.getOpacity();
            } else {
                newOpacity = VisibilityManager.playersHidden() ? 0.0F : 1.0F;
            }
            // we want to set to the lowest visibility available
            RenderUtils.targetOpacity = Math.min(RenderUtils.targetOpacity, newOpacity);
        }
    }

    public static void clearCurrentEntityRender() {
        RenderUtils.currentRender = null;
        RenderUtils.renderOverride = RenderOverride.NONE;
        RenderUtils.targetOpacity = 1.0F;
    }

    public static RenderOverride getRenderOverride() {
        return RenderUtils.renderOverride;
    }

    public static float getTargetOpacity() {
        return RenderUtils.targetOpacity;
    }

    public static int getTargetAlpha() {
        return (int) (255 * getTargetOpacity());
    }

    public static void markBuffer(BufferBuilder bufferBuilder) {
        if (RenderUtils.renderOverride != RenderOverride.NONE) {
            RenderOverride existingOverride = RenderUtils.bufferOverrideMap.getOrDefault(bufferBuilder, RenderOverride.NONE);
            if (RenderUtils.renderOverride.ordinal() > existingOverride.ordinal()) {
                RenderUtils.bufferOverrideMap.put(bufferBuilder, RenderUtils.renderOverride);
            }
        }
    }

    public static RenderOverride consumeBufferOverride(BufferBuilder bufferBuilder) {
        RenderOverride bufferOverride = RenderUtils.bufferOverrideMap.getOrDefault(bufferBuilder, RenderOverride.NONE);
        RenderUtils.bufferOverrideMap.remove(bufferBuilder);
        return bufferOverride;
    }

    public static void unmarkBuffers() {
        RenderUtils.bufferOverrideMap.clear();
    }

    public static int recalculateArgb(int oldArgb) {
        if (shouldRenderTranslucent(false)) {
            return ARGB.color(recalculateAlpha(ARGB.alpha(oldArgb)), ARGB.red(oldArgb), ARGB.green(oldArgb), ARGB.blue(oldArgb));
        }
        return oldArgb;
    }

    public static int recalculateAlpha(int oldAlpha) {
        if (shouldRenderTranslucent(false)) {
            return Math.min(getTargetAlpha(), oldAlpha);
        }
        return oldAlpha;
    }

    public static boolean shouldRender(boolean allowTranslucent) {
        if (RenderUtils.currentRender == null) {
            return true;
        }
        if (allowTranslucent) {
            return RenderUtils.targetOpacity > 0.0F;
        } else {
            return RenderUtils.targetOpacity >= 1.0F;
        }
    }

    public static boolean shouldRenderTranslucent(boolean forceOverride) {
        if (RenderUtils.currentRender == null) {
            return false;
        }
        if (forceOverride || RenderUtils.renderOverride != RenderOverride.NONE) {
            return RenderUtils.targetOpacity > 0.0F && RenderUtils.targetOpacity < 1.0F;
        }
        return false;
    }

    public static void withRenderOverride(Runnable callback, RenderOverride renderOverride) {
        if (shouldRenderTranslucent(true)) {
            RenderOverride oldValue = RenderUtils.renderOverride;
            RenderUtils.renderOverride = renderOverride;
            callback.run();
            RenderUtils.renderOverride = oldValue;
        } else {
            callback.run();
        }
    }
}
