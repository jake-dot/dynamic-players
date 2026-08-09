package ca.jakedot.hideplayers.mixin;

import ca.jakedot.hideplayers.visibility.RenderOverride;
import ca.jakedot.hideplayers.visibility.RenderUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;renderNameTag(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    )
    private void wrapNameTagRender(EntityRenderer<?, ?> entityRenderer, EntityRenderState entityRenderState, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Operation<Void> original) {
        if (RenderUtils.shouldRender(true)) {
            RenderUtils.withRenderOverride(() -> {
                original.call(entityRenderer, entityRenderState, component, poseStack, multiBufferSource, i);
            }, RenderOverride.NONE);
        }
    }
}
