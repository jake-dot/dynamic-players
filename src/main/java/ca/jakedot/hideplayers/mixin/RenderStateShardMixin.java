package ca.jakedot.hideplayers.mixin;

import ca.jakedot.hideplayers.visibility.RenderOverride;
import ca.jakedot.hideplayers.visibility.RenderUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderStateShard.class)
public class RenderStateShardMixin {
    @Inject(method = "setupRenderState", at = @At("TAIL"))
    private void setupTransparency(CallbackInfo ci) {
        if (!((Object)this instanceof RenderType)) {
            return;
        }
        if (!RenderUtils.shouldRenderTranslucent(false)) {
            return;
        }
        RenderOverride renderOverride = RenderUtils.getRenderOverride();
        if (renderOverride.overridesTransparency()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );
        }
        if (renderOverride.noCull()) {
            RenderSystem.disableCull();
        } else {
            RenderSystem.enableCull();
        }
    }

    @Inject(method = "clearRenderState", at = @At("TAIL"))
    private void clearTransparency(CallbackInfo ci) {
        if (!((Object)this instanceof RenderType)) {
            return;
        }
        if (!RenderUtils.shouldRenderTranslucent(false)) {
            return;
        }
        RenderOverride renderOverride = RenderUtils.getRenderOverride();
        if (renderOverride.overridesTransparency()) {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
        if (renderOverride.noCull()) {
            RenderSystem.enableCull();
        }
    }
}
