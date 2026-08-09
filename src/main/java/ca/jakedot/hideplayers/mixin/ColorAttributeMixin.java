package ca.jakedot.hideplayers.mixin;

import ca.jakedot.hideplayers.visibility.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute")
public class ColorAttributeMixin {
    @ModifyVariable(method = "set", at = @At("HEAD"))
    private static int modifyColor(int argb) {
        return RenderUtils.recalculateArgb(argb);
    }
}
