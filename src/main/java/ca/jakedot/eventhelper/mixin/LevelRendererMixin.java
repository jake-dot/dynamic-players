package ca.jakedot.eventhelper.mixin;

import ca.jakedot.eventhelper.visibility.RenderUtils;
import ca.jakedot.eventhelper.visibility.VisibilityManager;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @WrapMethod(method = "renderEntities")
    private void wrapRenderEntities(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, DeltaTracker deltaTracker, List<Entity> list, Operation<Void> original) {
        VisibilityManager.pruneTransitions();
        original.call(poseStack, bufferSource, camera, deltaTracker, RenderUtils.sortByRenderingOrder(list));
        RenderUtils.reset();
    }

    @WrapMethod(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)V")
    private void wrapAddParticle(ParticleOptions particleOptions, boolean bl, boolean bl2, double d, double e, double f, double g, double h, double i, Operation<Void> original) {
        if (!RenderUtils.shouldRenderPartiallyHidden()) {
            original.call(particleOptions, bl, bl2, d, e, f, g, h, i);
        }
    }
}
