package ca.jakedot.eventhelper.mixin;

import ca.jakedot.eventhelper.visibility.RenderUtils;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin {
    @ModifyVariable(method = "setColor(IIII)Lcom/mojang/blaze3d/vertex/VertexConsumer;", at = @At(value = "HEAD"), ordinal = 3)
    public int modifyAlpha(int alpha) {
        if (RenderUtils.shouldRenderTranslucent() && RenderUtils.getRenderOverride().overridesTransparency()) {
            replaceAlpha(alpha);
        }
        return alpha;
    }

    @ModifyVariable(method = "putRgba(JI)V", at = @At(value = "HEAD"))
    private static int modifyArgb(int argb) {
        if (RenderUtils.shouldRenderTranslucent() && RenderUtils.getRenderOverride().overridesTransparency()) {
            return replaceArgb(argb);
        }
        return argb;
    }

    private static int replaceAlpha(int old) {
        return Math.min((int) (RenderUtils.getOpacity() * 255), old);
    }

    private static int replaceArgb(int old) {
        return ARGB.color(replaceAlpha(ARGB.alpha(old)), ARGB.red(old), ARGB.green(old), ARGB.blue(old));
    }
}
