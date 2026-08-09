package ca.jakedot.hideplayers.mixin;

import ca.jakedot.hideplayers.visibility.RenderUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiBufferSource.BufferSource.class)
public class BufferSourceMixin {
    @WrapOperation(
            method = "endBatch(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/BufferBuilder;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;draw(Lcom/mojang/blaze3d/vertex/MeshData;)V")
    )
    private void wrapDraw(RenderType renderType, MeshData meshData, Operation<Void> original, @Local(argsOnly = true) BufferBuilder bufferBuilder) {
        RenderUtils.withRenderOverride(() -> {
            original.call(renderType, meshData);
        }, RenderUtils.consumeBufferOverride(bufferBuilder));
    }
}
