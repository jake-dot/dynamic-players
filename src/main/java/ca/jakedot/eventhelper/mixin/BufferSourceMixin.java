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
    private RenderType modifyRenderType(RenderType renderType) {
        return RenderUtils.modifyRenderType(renderType);
    }
}
