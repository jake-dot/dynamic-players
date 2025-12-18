package ca.jakedot.eventhelper.mixin;

import ca.jakedot.eventhelper.visibility.RenderUtils;
import com.mojang.blaze3d.vertex.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin {
    @ModifyVariable(method = "setColor(IIII)Lcom/mojang/blaze3d/vertex/VertexConsumer;", at = @At(value = "HEAD"), ordinal = 3)
    public int modifyAlpha(int alpha) {
        return RenderUtils.recalculateAlpha(alpha);
    }

    @ModifyVariable(method = "putRgba(JI)V", at = @At(value = "HEAD"))
    private static int modifyArgb(int argb) {
        return RenderUtils.recalculateArgb(argb);
    }
}
