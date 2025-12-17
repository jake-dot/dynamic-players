package ca.jakedot.eventhelper.mixin;

import ca.jakedot.eventhelper.visibility.RenderOverride;
import ca.jakedot.eventhelper.visibility.RenderUtils;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @WrapMethod(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V"
    )
    public void wrapEntireRender(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, EntityRenderer<?, ?> entityRenderer, Operation<Void> original) {
        if (entity instanceof RemotePlayer) {
            RenderUtils.setCurrentRender((RemotePlayer) entity);
        }
        original.call(entity, d, e, f, g, poseStack, multiBufferSource, i, entityRenderer);
        RenderUtils.clearCurrentRender();
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    )
    public void wrapEntityRender(EntityRenderer<?, ?> entityRenderer, EntityRenderState entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Operation<Void> original) {
        if (RenderUtils.shouldRender() || RenderUtils.shouldRenderTranslucent()) {
            RenderUtils.override(() -> original.call(entityRenderer, entityRenderState, poseStack, multiBufferSource, i), RenderOverride.TRANSLUCENT_CULL);
        }
    }
}
