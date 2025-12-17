package ca.jakedot.eventhelper.mixin;

import ca.jakedot.eventhelper.visibility.RenderOverride;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderType.CompositeState.class)
public interface CompositeStateMixin {
    @Accessor("textureState")
    RenderStateShard.EmptyTextureStateShard event_helper$getTextureState();
    @Accessor("shaderState")
    RenderStateShard.ShaderStateShard event_helper$getShaderState();
    @Accessor("transparencyState")
    RenderStateShard.TransparencyStateShard event_helper$getTransparencyState();
    @Accessor("depthTestState")
    RenderStateShard.DepthTestStateShard event_helper$getDepthTestState();
    @Accessor("cullState")
    RenderStateShard.CullStateShard event_helper$getCullState();
    @Accessor("lightmapState")
    RenderStateShard.LightmapStateShard event_helper$getLightmapState();
    @Accessor("overlayState")
    RenderStateShard.OverlayStateShard event_helper$getOverlayState();
    @Accessor("layeringState")
    RenderStateShard.LayeringStateShard event_helper$getLayeringState();
    @Accessor("outputState")
    RenderStateShard.OutputStateShard event_helper$getOutputState();
    @Accessor("texturingState")
    RenderStateShard.TexturingStateShard event_helper$getTexturingState();
    @Accessor("writeMaskState")
    RenderStateShard.WriteMaskStateShard event_helper$getWriteMaskState();
    @Accessor("lineState")
    RenderStateShard.LineStateShard event_helper$getevent_helper$getLineState();
    @Accessor("colorLogicState")
    RenderStateShard.ColorLogicStateShard event_helper$getevent_helper$getColorLogicState();
    @Accessor("outlineProperty")
    RenderType.OutlineProperty event_helper$getOutlineProperty();
}
