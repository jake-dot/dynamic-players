package ca.jakedot.eventhelper.mixin;

import ca.jakedot.eventhelper.visibility.RenderUtils;
import ca.jakedot.eventhelper.visibility.VisibilityManager;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "renderEntities", at = @At("HEAD"))
    private void beginRenderingEntities(CallbackInfo ci) {
        VisibilityManager.pruneTransitions();
        RenderUtils.unmarkBuffers();
    }
}
