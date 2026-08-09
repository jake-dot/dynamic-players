package ca.jakedot.hideplayers.visibility;

import ca.jakedot.hideplayers.HidePlayers;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RenderUtils {
    private static RenderOverride renderOverride = RenderOverride.NONE;
    private static float targetOpacity = 1.0F;

    private static final Map<Entity, Float> visibilityMap = new HashMap<>();
    private static final Map<BufferBuilder, RenderOverride> bufferOverrideMap = new HashMap<>();

    public static List<Entity> sortByRenderingOrder(List<Entity> list) {
        RenderUtils.visibilityMap.clear();
        Map<Entity, Float> distanceMap = new HashMap<>();
        Iterator<Entity> iter = list.iterator();
        while (iter.hasNext()) {
            Entity entity = iter.next();
            if (!(entity instanceof RemotePlayer)) {
                continue;
            }
            float opacity = RenderUtils.getVisibility((RemotePlayer) entity, distance -> distanceMap.put(entity, distance));
            if (opacity == 1.0F) {
                continue;
            }
            RenderUtils.visibilityMap.put(entity, opacity);
            iter.remove();
        }

        List<Entity> sortedList = new ArrayList<>(list);
        Comparator<Entity> comparator;
        if (!HidePlayers.getConfig().reverseRenderingOrder()) {
            comparator = (o1, o2) -> Float.compare(distanceMap.get(o2), distanceMap.get(o1));
        } else {
            comparator = (o1, o2) -> Float.compare(distanceMap.get(o1), distanceMap.get(o2));
        }
        RenderUtils.visibilityMap.keySet().stream()
                .sorted(comparator)
                .forEach(sortedList::add);
        return sortedList;
    }

    private static float getVisibility(RemotePlayer entity, Consumer<Float> distanceConsumer) {
        String identifier = entity.getGameProfile().getName();
        float targetOpacity = 1.0F;
        double distSqr = Minecraft.getInstance().player.distanceToSqr(entity);
        if (distanceConsumer != null) {
            distanceConsumer.accept((float) distSqr);
        }

        if (!VisibilityManager.isWhitelisted(identifier) || !VisibilityManager.whitelistEnabled()) {
            float minOpacity = (float) HidePlayers.getConfig().allPlayersHiddenOpacity();
            VisibilityTransition transition = VisibilityManager.getTransition(identifier);
            if (transition != null) {
                float rawOpacity = transition.getOpacity();
                targetOpacity = minOpacity + rawOpacity * (1.0F - minOpacity);
            } else if (VisibilityManager.playersHidden()) {
                targetOpacity = minOpacity;
            }
        }
        if (targetOpacity == 0.0F) {
            return targetOpacity;
        }

        if (VisibilityManager.nearPlayersHidden() && (!VisibilityManager.isWhitelisted(identifier) || !VisibilityManager.whitelistEnabled())) {
            AbstractClientPlayer self = Minecraft.getInstance().player;
            float threshold = (float) HidePlayers.getConfig().nearbyPlayersDistance(); // TODO option to auto adjust to player scale
            float transitionRange = 1.0F;
            if (distSqr <= Math.pow(threshold, 2)) {
                float dist = self.distanceTo(entity);
                float minOpacity = (float) HidePlayers.getConfig().nearbyPlayersHiddenOpacity();
                if (dist <= threshold - transitionRange) {
                    targetOpacity = Math.min(targetOpacity, minOpacity);
                } else if (dist < threshold) {
                    float transitionDist = dist - (threshold - transitionRange);
                    float rawOpacity = Math.clamp(transitionDist / transitionRange, 0.0F, 1.0F);
                    float finalOpacity = minOpacity + rawOpacity * (1.0F - minOpacity);
                    targetOpacity = Math.min(targetOpacity, finalOpacity);
                }
            }
        }

        // perhaps run this final opacity value through a separate method to compare multipliers against each other
        return targetOpacity;
    }

    public static void beginRender(RemotePlayer entity) {
        RenderUtils.targetOpacity = RenderUtils.visibilityMap.getOrDefault(entity, 1.0F);
    }

    public static void endRender() {
        RenderUtils.renderOverride = RenderOverride.NONE;
        RenderUtils.targetOpacity = 1.0F;
    }

    public static void reset() {
        RenderUtils.bufferOverrideMap.clear();
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
        if (RenderUtils.targetOpacity == 1.0F) {
            return true;
        }
        return allowTranslucent && RenderUtils.targetOpacity > 0.0F;
    }

    public static boolean shouldRenderTranslucent(boolean ignoreOverride) {
        if (RenderUtils.targetOpacity == 1.0F || RenderUtils.targetOpacity == 0.0F) {
            return false;
        }
        return ignoreOverride || RenderUtils.renderOverride != RenderOverride.NONE;
    }

    public static boolean shouldRenderPartiallyHidden() {
        return RenderUtils.targetOpacity <= 0.5F;
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

    public static void beginEntityTick(RemotePlayer entity) {
        RenderUtils.targetOpacity = RenderUtils.visibilityMap.getOrDefault(entity, 1.0F);
    }

    public static void endEntityTick() {
        RenderUtils.renderOverride = RenderOverride.NONE;
        RenderUtils.targetOpacity = 1.0F;
    }
}
