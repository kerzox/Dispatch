package mod.kerzox.dispatch.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

public class DispatchRenderTypes extends RenderType {

    public DispatchRenderTypes(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }

    public static final RenderType NO_DEPTH_LINES = create("no_depth_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, false, false,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLineState(new LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setCullState(NO_CULL).createCompositeState(false));


    public static final RenderType SOLID_COLOUR = create(
            "no_texture_quad",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            solidNoDepthState()
    );

    private static CompositeState solidNoDepthState() {
        return CompositeState.builder()
                .setLightmapState(LIGHTMAP)
                .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                .setTextureState(NO_TEXTURE)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .setCullState(NO_CULL)
                .createCompositeState(false);
    }

}
