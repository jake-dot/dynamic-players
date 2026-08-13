package ca.jakedot.hideplayers.mixin;

import ca.jakedot.hideplayers.visibility.RenderUtils;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @WrapMethod(method = "doAddParticle")
    private void wrapAddParticle(ParticleOptions particleOptions, boolean bl, boolean bl2, double d, double e, double f, double g, double h, double i, Operation<Void> original) {
        if (!RenderUtils.shouldRenderPartiallyHidden()) {
            original.call(particleOptions, bl, bl2, d, e, f, g, h, i);
        }
    }
}
