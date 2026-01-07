package ca.jakedot.eventhelper.visibility;

import ca.jakedot.eventhelper.mixin.CompositeRenderTypeAccessor;
import ca.jakedot.eventhelper.mixin.CompositeStateMixin;
import ca.jakedot.eventhelper.mixin.RenderStateShardAccessor;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;

import java.util.function.Supplier;

public class RenderUtils {
    private static RemotePlayer currentRender;
    private static RenderOverride renderOverride = RenderOverride.NONE;
    private static float targetOpacity = 1.0F;

    public static void setCurrentEntityRender(RemotePlayer entity) {
        RenderUtils.currentRender = entity;
        String identifier = RenderUtils.currentRender.getGameProfile().getName();

        if (VisibilityManager.isWhitelisted(identifier)) {
            RenderUtils.targetOpacity = 1.0F;
        } else {
            VisibilityTransition transition = VisibilityManager.getTransition(identifier);
            if (transition != null) {
                RenderUtils.targetOpacity = transition.getOpacity();
            } else {
                RenderUtils.targetOpacity = VisibilityManager.playersHidden() ? 0.0F : 1.0F;
            }
        }
    }

    public static void clearCurrentEntityRender() {
        RenderUtils.currentRender = null;
        RenderUtils.renderOverride = RenderOverride.NONE;
        RenderUtils.targetOpacity = 1.0F;
    }

    public static float getTargetOpacity() {
        return RenderUtils.targetOpacity;
    }

    public static int getTargetAlpha() {
        return (int) (255 * getTargetOpacity());
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

    public static boolean shouldRenderTranslucent() {
        return shouldRenderTranslucent(true);
    }

    private static boolean shouldRenderTranslucent(boolean forceOverride) {
        if (RenderUtils.currentRender == null) {
            return false;
        }
        if (forceOverride || RenderUtils.renderOverride != RenderOverride.NONE) {
            return RenderUtils.targetOpacity > 0.0F && RenderUtils.targetOpacity < 1.0F;
        }
        return false;
    }

    public static void withRenderOverride(Runnable callback, RenderOverride renderOverride) {
        withRenderOverride(() -> {
            callback.run();
            return null;
        }, renderOverride);
    }

    public static <T> T withRenderOverride(Supplier<T> callback, RenderOverride renderOverride) {
        if (shouldRenderTranslucent()) {
            RenderOverride oldValue = RenderUtils.renderOverride;
            RenderUtils.renderOverride = renderOverride;
            T result = callback.get();
            RenderUtils.renderOverride = oldValue;
            return result;
        } else {
            return callback.get();
        }
    }

    public static RenderType modifyRenderType(RenderType renderType) {
        if (!shouldRenderTranslucent(false)) {
            return renderType;
        }
        if (!(renderType instanceof RenderType.CompositeRenderType)) {
            return renderType;
        }
        if (RenderUtils.renderOverride.isRenderTypeCached(renderType)) {
            return RenderUtils.renderOverride.getCachedRenderType(renderType);
        }

        RenderType.CompositeState state = ((CompositeRenderTypeAccessor)renderType).event_helper$getState();
        state = modifyState(state);
        if (state == null) {
            RenderUtils.renderOverride.cacheRenderType(renderType, null);
            return renderType;
        }

        RenderType modifiedRenderType = RenderType.create(
                ((RenderStateShardAccessor)renderType).event_helper$getName(),
                renderType.format(),
                renderType.mode(),
                renderType.bufferSize(),
                renderType.affectsCrumbling(),
                renderType.sortOnUpload(),
                state
        );
        RenderUtils.renderOverride.cacheRenderType(renderType, modifiedRenderType);
        return modifiedRenderType;
    }

    private static RenderType.CompositeState modifyState(RenderType.CompositeState state) {
        CompositeStateMixin stateAccessor = (CompositeStateMixin) (Object) state;

        boolean replaceTransparency = RenderUtils.renderOverride.overridesTransparency() && !stateAccessor.event_helper$getTransparencyState().equals(RenderStateShard.TRANSLUCENT_TRANSPARENCY);
        boolean replaceCull = RenderUtils.renderOverride.overridesCull() && !stateAccessor.event_helper$getCullState().equals(RenderStateShard.CULL);
        if (!replaceTransparency && !replaceCull) {
            return null;
        }

        RenderStateShard.TransparencyStateShard newTransparencyState = stateAccessor.event_helper$getTransparencyState();
        RenderStateShard.CullStateShard newCullState = stateAccessor.event_helper$getCullState();
        if (replaceTransparency) {
            newTransparencyState = RenderStateShard.TRANSLUCENT_TRANSPARENCY;
        }
        if (replaceCull) {
            newCullState = RenderStateShard.CULL;
        }

        return RenderType.CompositeState.builder()
                .setTextureState(stateAccessor.event_helper$getTextureState())
                .setShaderState(stateAccessor.event_helper$getShaderState())
                .setTransparencyState(newTransparencyState)
                .setDepthTestState(stateAccessor.event_helper$getDepthTestState())
                .setCullState(newCullState)
                .setLightmapState(stateAccessor.event_helper$getLightmapState())
                .setOverlayState(stateAccessor.event_helper$getOverlayState())
                .setLayeringState(stateAccessor.event_helper$getLayeringState())
                .setOutputState(stateAccessor.event_helper$getOutputState())
                .setTexturingState(stateAccessor.event_helper$getTexturingState())
                .setWriteMaskState(stateAccessor.event_helper$getWriteMaskState())
                .setLineState(stateAccessor.event_helper$getevent_helper$getLineState())
                .setColorLogicState(stateAccessor.event_helper$getevent_helper$getColorLogicState())
                .createCompositeState(stateAccessor.event_helper$getOutlineProperty());
    }
}
