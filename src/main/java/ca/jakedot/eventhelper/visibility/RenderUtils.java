package ca.jakedot.eventhelper.visibility;

import ca.jakedot.eventhelper.mixin.CompositeRenderTypeAccessor;
import ca.jakedot.eventhelper.mixin.CompositeStateMixin;
import ca.jakedot.eventhelper.mixin.RenderStateShardAccessor;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;

import java.util.function.Supplier;

// we could assign these methods to variables in order to avoid repeat calls
public class RenderUtils {
    private static RemotePlayer currentRender;

    private static String getCurrentRenderName() {
        return RenderUtils.currentRender != null ? RenderUtils.currentRender.getGameProfile().getName() : "";
    }

    public static boolean shouldRenderTranslucent() {
        if (RenderUtils.currentRender == null) {
            return false;
        }
        if (VisibilityManager.isWhitelisted(getCurrentRenderName())) {
            return false;
        }
        VisibilityTransition transition = VisibilityManager.getTransition(getCurrentRenderName());
        if (transition != null) {
            float opacity = transition.getOpacity();
            return opacity > 0.0F && opacity < 1.0F;
        }
        return false;
    }

    public static boolean shouldRender() {
        if (RenderUtils.currentRender == null) {
            return true;
        }
        return VisibilityManager.isVisible(getCurrentRenderName());
    }

    public static int recalculateArgb(int old) {
        if (shouldRenderTranslucent() && RenderUtils.renderOverride.overridesTransparency()) {
            return ARGB.color(recalculateAlpha(ARGB.alpha(old)), ARGB.red(old), ARGB.green(old), ARGB.blue(old));
        }
        return old;
    }

    public static int recalculateAlpha(int old) {
        if (shouldRenderTranslucent() && RenderUtils.renderOverride.overridesTransparency()) {
            return (int) Math.min(getOpacity() * 255, old);
        }
        return old;
    }

    public static float getOpacity() {
        VisibilityTransition transition = VisibilityManager.getTransition(getCurrentRenderName());
        if (transition != null) {
            return transition.getOpacity();
        }
        return VisibilityManager.isVisible(getCurrentRenderName()) ? 1.0F : 0.0F;
    }

    public static void setCurrentRender(RemotePlayer current) {
        RenderUtils.currentRender = current;
    }

    public static void clearCurrentRender() {
        RenderUtils.currentRender = null;
        RenderUtils.renderOverride = RenderOverride.NONE;
    }

    private static RenderOverride renderOverride = RenderOverride.NONE;

    public static RenderOverride getRenderOverride() {
        return RenderUtils.renderOverride;
    }

    public static <T> T override(Supplier<T> callback, RenderOverride renderOverride) {
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
        if (!shouldRenderTranslucent() || !RenderUtils.renderOverride.overridesTransparency()) {
            return renderType;
        }
        if (!(renderType instanceof RenderType.CompositeRenderType)) {
            return renderType;
        }
        if (RenderUtils.renderOverride.isRenderTypeCached(renderType)) {
            return RenderUtils.renderOverride.getCachedRenderType(renderType);
        }

        RenderType.CompositeState state = ((CompositeRenderTypeAccessor)renderType).event_helper$getState();
        state = overrideState(state);
        if (state == null) {
            RenderUtils.renderOverride.cacheRenderType(renderType, null);
            return renderType;
        }

        RenderType newRenderType = RenderType.create(
                ((RenderStateShardAccessor)renderType).event_helper$getName(),
                renderType.format(),
                renderType.mode(),
                renderType.bufferSize(),
                renderType.affectsCrumbling(),
                renderType.sortOnUpload(),
                state
        );
        RenderUtils.renderOverride.cacheRenderType(renderType, newRenderType);
        return newRenderType;
    }

    private static RenderType.CompositeState overrideState(RenderType.CompositeState state) {
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
