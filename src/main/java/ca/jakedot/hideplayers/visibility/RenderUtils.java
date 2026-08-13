package ca.jakedot.hideplayers.visibility;

import ca.jakedot.hideplayers.HidePlayers;
import ca.jakedot.hideplayers.mixin.RenderSetupAccessor;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuSampler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RenderUtils {
    private static RenderOverride renderOverride = RenderOverride.NONE;
    private static float targetOpacity = 1.0F;

    private static final Map<EntityRenderState, Entity> entityMap = new HashMap<>();
    private static final Map<Entity, Float> visibilityMap = new HashMap<>();

    public static void storeEntity(Entity entity, EntityRenderState entityRenderState) {
        RenderUtils.entityMap.put(entityRenderState, entity);
    }

    public static Entity getEntity(EntityRenderState entityRenderState) {
        return RenderUtils.entityMap.get(entityRenderState);
    }

    public static void sortByRenderingOrder(List<EntityRenderState> list) {
        RenderUtils.visibilityMap.clear();
        Map<EntityRenderState, Float> distanceMap = new HashMap<>();
        Iterator<EntityRenderState> iter = list.iterator();
        while (iter.hasNext()) {
            EntityRenderState entityRenderState = iter.next();
            Entity entity = RenderUtils.entityMap.get(entityRenderState);
            if (!(entity instanceof RemotePlayer)) {
                continue;
            }
            float opacity = RenderUtils.getVisibility((RemotePlayer) entity, distance -> distanceMap.put(entityRenderState, distance));
            if (opacity == 1.0F) {
                continue;
            }
            RenderUtils.visibilityMap.put(entity, opacity);
            iter.remove();
        }

        Consumer<EntityRenderState> addConsumer;
        if (!HidePlayers.getConfig().reverseRenderingOrder()) {
            addConsumer = list::add;
        } else {
            addConsumer = list::addFirst;
        }
        distanceMap.keySet().stream()
                .filter(entityRenderState -> RenderUtils.entityMap.containsKey(entityRenderState) && RenderUtils.visibilityMap.containsKey(RenderUtils.entityMap.get(entityRenderState)))
                .filter(entityRenderState -> RenderUtils.visibilityMap.get(RenderUtils.entityMap.get(entityRenderState)) != 1.0F)
                .sorted((o1, o2) -> Float.compare(distanceMap.get(o2), distanceMap.get(o1)))
                .forEach(addConsumer);
    }

    private static float getVisibility(RemotePlayer entity, Consumer<Float> distanceConsumer) {
        String identifier = entity.getGameProfile().name();
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
        RenderUtils.entityMap.clear();
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

    public static RenderType modifyRenderType(RenderType renderType) {
        if (!RenderUtils.shouldRenderTranslucent(false)) {
            return renderType;
        }
        String name;
        RenderSetup renderSetup;
        try {
            Field field = renderType.getClass().getDeclaredField("field_64011");
            field.setAccessible(true);
            name = (String) field.get(renderType);

            field = renderType.getClass().getDeclaredField("field_64013");
            field.setAccessible(true);
            renderSetup = (RenderSetup) field.get(renderType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        RenderSetup newRenderSetup = RenderUtils.modifyRenderSetup(renderSetup, RenderUtils.getRenderOverride());
        return RenderType.create(HidePlayers.MOD_ID + "$" + name, newRenderSetup);
    }

    private static RenderSetup modifyRenderSetup(RenderSetup oldRenderSetup, RenderOverride renderOverride) {
        RenderSetupAccessor oldSetupAccessor = (RenderSetupAccessor) (Object) oldRenderSetup;
        RenderPipeline newPipeline = RenderUtils.modifyRenderPipeline(oldSetupAccessor.dynamic_players$getRenderPipeline(), renderOverride);
        RenderSetup.RenderSetupBuilder builder = RenderSetup.builder(newPipeline)
                .setOutputTarget(oldSetupAccessor.dynamic_players$getOutputTarget())
                .setTextureTransform(oldSetupAccessor.dynamic_players$getTextureTransform())
                .setOutline(oldSetupAccessor.dynamic_players$getOutlineProperty())
                .setLayeringTransform(oldSetupAccessor.dynamic_players$getLayeringTransform())
                .bufferSize(oldSetupAccessor.dynamic_players$getBufferSize());

        if (oldSetupAccessor.dynamic_players$isUseLightmap()) {
            builder.useLightmap();
        }
        if (oldSetupAccessor.dynamic_players$isUseOverlay()) {
            builder.useOverlay();
        }
        if (oldSetupAccessor.dynamic_players$isAffectsCrumbling()) {
            builder.affectsCrumbling();
        }
        if (oldSetupAccessor.dynamic_players$isSortOnUpload()) {
            builder.sortOnUpload();
        }
        for (Map.Entry<String, RenderSetup.TextureBinding> entry : oldSetupAccessor.dynamic_players$getTextures().entrySet()) {
            builder.withTexture(entry.getKey(), entry.getValue().location(), entry.getValue().sampler());
        }
        return builder.createRenderSetup();
    }

    private static RenderPipeline modifyRenderPipeline(RenderPipeline oldPipeline, RenderOverride renderOverride) {
        Map<String, String> shaderDefineValues = oldPipeline.getShaderDefines().values();
        Set<String> shaderDefineFlags = oldPipeline.getShaderDefines().flags();

        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(oldPipeline.getLocation())
                .withVertexShader(oldPipeline.getVertexShader())
                .withFragmentShader(oldPipeline.getFragmentShader());
        shaderDefineValues.entrySet().stream()
                .map(entry -> {
                    try {
                        return new AbstractMap.SimpleEntry<>(entry.getKey(), Float.parseFloat(entry.getValue()));
                    } catch (NumberFormatException ignored) {}
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(entry -> builder.withShaderDefine(entry.getKey(), entry.getValue()));
        shaderDefineFlags.forEach(builder::withShaderDefine);
        oldPipeline.getSamplers().forEach(builder::withSampler);
        oldPipeline.getUniforms().forEach(uniform -> {
            if (uniform.textureFormat() == null) {
                builder.withUniform(uniform.name(), uniform.type());
            } else {
                builder.withUniform(uniform.name(), uniform.type(), uniform.textureFormat());
            }
        });
        builder
                .withDepthTestFunction(oldPipeline.getDepthTestFunction())
                .withPolygonMode(oldPipeline.getPolygonMode())
                .withColorWrite(oldPipeline.isWriteColor(), oldPipeline.isWriteAlpha())
                .withDepthWrite(oldPipeline.isWriteDepth())
                .withColorLogic(oldPipeline.getColorLogic())
                .withVertexFormat(oldPipeline.getVertexFormat(), oldPipeline.getVertexFormatMode())
                .withDepthBias(oldPipeline.getDepthBiasScaleFactor(), oldPipeline.getDepthBiasConstant());
        if (renderOverride.overridesTransparency()) {
            builder.withBlend(BlendFunction.TRANSLUCENT);
        }
        if (renderOverride.shouldCull() != null) {
            builder.withCull(renderOverride.shouldCull());
        } else {
            builder.withCull(oldPipeline.isCull());
        }
        return builder.build();
    }
}
