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
}
