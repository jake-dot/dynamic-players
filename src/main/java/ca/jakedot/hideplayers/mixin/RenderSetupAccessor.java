package ca.jakedot.hideplayers.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RenderSetup.class)
public interface RenderSetupAccessor {
    @Accessor("pipeline")
    RenderPipeline dynamic_players$getRenderPipeline();

    @Accessor("textures")
    Map<String, RenderSetup.TextureBinding> dynamic_players$getTextures();

    @Accessor("outputTarget")
    OutputTarget dynamic_players$getOutputTarget();

    @Accessor("textureTransform")
    TextureTransform dynamic_players$getTextureTransform();

    @Accessor("useLightmap")
    boolean dynamic_players$isUseLightmap();

    @Accessor("useOverlay")
    boolean dynamic_players$isUseOverlay();

    @Accessor("outlineProperty")
    RenderSetup.OutlineProperty dynamic_players$getOutlineProperty();

    @Accessor("layeringTransform")
    LayeringTransform dynamic_players$getLayeringTransform();

    @Accessor("affectsCrumbling")
    boolean dynamic_players$isAffectsCrumbling();

    @Accessor("sortOnUpload")
    boolean dynamic_players$isSortOnUpload();

    @Accessor("bufferSize")
    int dynamic_players$getBufferSize();
}
