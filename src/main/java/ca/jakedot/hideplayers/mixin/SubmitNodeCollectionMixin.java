package ca.jakedot.hideplayers.mixin;

import ca.jakedot.hideplayers.visibility.RenderUtils;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin {
    @ModifyVariable(method = "submitShadow", at = @At("HEAD"))
    private List<EntityRenderState.ShadowPiece> modifyShadowAlpha(List<EntityRenderState.ShadowPiece> list) {
        List<EntityRenderState.ShadowPiece> newList = new ArrayList<>();
        for (EntityRenderState.ShadowPiece shadowPiece : list) {
            newList.add(new EntityRenderState.ShadowPiece(shadowPiece.relativeX(), shadowPiece.relativeY(), shadowPiece.relativeZ(),
                    shadowPiece.shapeBelow(), shadowPiece.alpha() * RenderUtils.getTargetOpacity()));
        }
        return newList;
    }

    @ModifyVariable(method = "submitModel", at = @At("HEAD"), ordinal = 2)
    private int modifyModelArgb(int argb) {
        return RenderUtils.recalculateArgb(argb);
    }

    @ModifyVariable(method = "submitModelPart", at = @At("HEAD"), ordinal = 2)
    private int modifyModelPartArgb(int argb) {
        return RenderUtils.recalculateArgb(argb);
    }

    @WrapMethod(method = "submitItem")
    private void modifyItemSubmit(PoseStack poseStack, ItemDisplayContext itemDisplayContext, int i, int j, int k, int[] is, List<BakedQuad> list, RenderType renderType, ItemStackRenderState.FoilType foilType, Operation<Void> original) {
        if (RenderUtils.shouldRenderTranslucent(false)) {
            boolean extraTint = false;
            List<BakedQuad> newList = new ArrayList<>();
            for (BakedQuad quad : list) {
                if (quad.isTinted()) {
                    newList.add(quad);
                } else {
                    newList.add(new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                            quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), list.size(),
                            quad.direction(), quad.sprite(), quad.shade(), quad.lightEmission()));
                    extraTint = true;
                }
            }
            list = newList;
            for (int i0 = 0; i0 < is.length; i0++) {
                is[i0] = RenderUtils.recalculateArgb(is[i0]);
            }
            int listSize = list.size();
            if (extraTint) {
                listSize++;
            }
            if (is.length < listSize) {
                int[] array = Arrays.copyOf(is, listSize);
                int value = ARGB.white(RenderUtils.getTargetOpacity());
                for (int i0 = is.length; i0< array.length; i0++) {
                    array[i0] = value;
                }
                is = array;
            }
        }
        original.call(poseStack, itemDisplayContext, i, j, k, is, list, renderType, foilType);
    }

    @ModifyVariable(method = "submitModel", at = @At("HEAD"))
    private RenderType modifyModelRenderType(RenderType renderType) {
        return RenderUtils.modifyRenderType(renderType);
    }

    @ModifyVariable(method = "submitModelPart", at = @At("HEAD"))
    private RenderType modifyModelPartRenderType(RenderType renderType) {
        return RenderUtils.modifyRenderType(renderType);
    }

    @ModifyVariable(method = "submitItem", at = @At("HEAD"))
    private RenderType modifyItemRenderType(RenderType renderType) {
        return RenderUtils.modifyRenderType(renderType);
    }
}
