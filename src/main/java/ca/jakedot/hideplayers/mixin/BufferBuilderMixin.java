package ca.jakedot.hideplayers.mixin;

import ca.jakedot.hideplayers.visibility.RenderUtils;
import com.mojang.blaze3d.vertex.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin {
    @ModifyVariable(method = "setColor(IIII)Lcom/mojang/blaze3d/vertex/VertexConsumer;", at = @At(value = "HEAD"), ordinal = 3)
    private int modifyAlpha(int alpha) {
        return RenderUtils.recalculateAlpha(alpha);
    }

    @ModifyVariable(method = "putRgba(JI)V", at = @At(value = "HEAD"))
    private static int modifyArgb(int argb) {
        return RenderUtils.recalculateArgb(argb);
    }

    @Inject(method = "addVertex(FFF)V", at = @At("HEAD"))
    private void markBuffer1(float f, float g, float h, CallbackInfoReturnable<Void> ci) { // unsure why this errored with CallbackInfo
        RenderUtils.markBuffer((BufferBuilder)(Object)this);
    }

    @Inject(method = "addVertex(FFFIFFIIFFF)V", at = @At("HEAD"))
    private void markBuffer2(float f, float g, float h, int i, float j, float k, int l, int m, float n, float o, float p, CallbackInfo ci) {
        RenderUtils.markBuffer((BufferBuilder)(Object)this);
    }
}
