package ca.jakedot.hideplayers.mixin;

import ca.jakedot.hideplayers.visibility.RenderUtils;
import ca.jakedot.hideplayers.visibility.VisibilityManager;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @WrapMethod(method = "submitEntities")
    private void wrapSubmitEntities(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, Operation<Void> original) {
        VisibilityManager.pruneTransitions();
        RenderUtils.sortByRenderingOrder(levelRenderState.entityRenderStates);
        original.call(poseStack, levelRenderState, submitNodeCollector);
        RenderUtils.reset();
    }
}
