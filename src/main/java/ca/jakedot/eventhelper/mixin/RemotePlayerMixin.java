package ca.jakedot.eventhelper.mixin;

import ca.jakedot.eventhelper.visibility.RenderUtils;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.player.RemotePlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RemotePlayer.class)
public class RemotePlayerMixin {
    @WrapMethod(method = "tick")
    private void wrapTick(Operation<Void> original) {
        RenderUtils.beginEntityTick((RemotePlayer)(Object)this);
        original.call();
        RenderUtils.endEntityTick();
    }
}
