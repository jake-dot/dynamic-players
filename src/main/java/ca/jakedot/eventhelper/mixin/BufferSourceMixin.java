package ca.jakedot.eventhelper.mixin;

import ca.jakedot.eventhelper.visibility.RenderUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MultiBufferSource.BufferSource.class)
public class BufferSourceMixin {
    @ModifyVariable(method = "getBuffer", at = @At("HEAD"))
    private RenderType modifyRenderType1(RenderType renderType) {
        return RenderUtils.modifyRenderType(renderType);
    }

    @ModifyVariable(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V", at = @At("HEAD"))
    private RenderType modifyRenderType2(RenderType renderType) {
        return RenderUtils.modifyRenderType(renderType);
    }

    @ModifyVariable(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/BufferBuilder;)V", at = @At("HEAD"))
    private RenderType modifyRenderType3(RenderType renderType) {
        return RenderUtils.modifyRenderType(renderType);
    }
}
