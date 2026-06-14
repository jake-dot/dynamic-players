package ca.jakedot.eventhelper.mixin;

import ca.jakedot.eventhelper.visibility.RenderOverride;
import ca.jakedot.eventhelper.visibility.RenderUtils;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {
    @WrapMethod(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V")
    private void wrapArmorRender(EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> resourceKey, Model model, ItemStack itemStack, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ResourceLocation resourceLocation, Operation<Void> original) {
        RenderUtils.withRenderOverride(() -> {
            original.call(layerType, resourceKey, model, itemStack, poseStack, multiBufferSource, i, resourceLocation);
        }, RenderOverride.TRANSLUCENT_NO_CULL);
    }
}
