package ca.jakedot.hideplayers.mixin;

import ca.jakedot.hideplayers.HidePlayers;
import ca.jakedot.hideplayers.visibility.RenderUtils;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
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

    @ModifyVariable(method = "submitItem", at = @At("HEAD"))
    private int[] modifyItemArgb(int[] array) {
        if (array.length > 0) {
            for (int i = 0; i < array.length; i++) {
                array[i] = RenderUtils.recalculateArgb(array[i]);
            }
        } else if (RenderUtils.shouldRenderTranslucent(false)) {
            array = new int[]{ARGB.white(RenderUtils.getTargetOpacity())};
        }
        return array;
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
