package ca.jakedot.hideplayers.mixin;

import ca.jakedot.hideplayers.visibility.RenderOverride;
import ca.jakedot.hideplayers.visibility.RenderUtils;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @WrapMethod(method = "submit")
    private void wrapEntireRender(EntityRenderState entityRenderState, CameraRenderState cameraRenderState, double d, double e, double f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Operation<Void> original) {
        Entity entity = RenderUtils.getEntity(entityRenderState);
        boolean playerRender = entity instanceof RemotePlayer;
        if (playerRender) {
            RenderUtils.beginRender((RemotePlayer) entity);
        }
        original.call(entityRenderState, cameraRenderState, d, e, f, poseStack, submitNodeCollector);
        if (playerRender) {
            RenderUtils.endRender();
        }
    }

    @WrapOperation(
            method = "submit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
    )
    private void wrapEntityRender(EntityRenderer<?, ?> entityRenderer, EntityRenderState entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, Operation<Void> original) {
        if (RenderUtils.shouldRender(true)) {
            RenderUtils.withRenderOverride(() -> {
                original.call(entityRenderer, entityRenderState, poseStack, submitNodeCollector, cameraRenderState);
            }, RenderOverride.TRANSLUCENT_CULL);
        }
    }

    @WrapMethod(method = "extractEntity")
    private EntityRenderState captureEntity(Entity entity, float f, Operation<EntityRenderState> original) {
        EntityRenderState entityRenderState = original.call(entity, f);
        RenderUtils.storeEntity(entity, entityRenderState);
        return entityRenderState;
    }
}
