package com.ldtteam.armory.weaponry.client.compatibility;

import com.ldtteam.smithscore.client.textures.ITextureController;
import com.ldtteam.smithscore.util.client.color.MinecraftColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class TConTextureController implements ITextureController
{
    @NotNull
    final MaterialRenderInfo renderInfo;
    String identifier;

    public TConTextureController(final @NotNull MaterialRenderInfo renderInfo) {this.renderInfo = renderInfo;}

    @Nonnull
    @Override
    public TextureAtlasSprite getTexture(
      @Nonnull final TextureAtlasSprite textureAtlasSprite, @Nonnull final String s)
    {
        return renderInfo.getTexture(new ResourceLocation(textureAtlasSprite.getIconName()), s);
    }

    @Override
    public boolean isStitched()
    {
        return renderInfo.isStitched();
    }

    @Override
    public boolean useVertexColoring()
    {
        return renderInfo.useVertexColoring();
    }

    @Nonnull
    @Override
    public MinecraftColor getVertexColor()
    {
        return new MinecraftColor(renderInfo.getVertexColor());
    }

    @Nonnull
    @Override
    public MinecraftColor getLiquidColor()
    {
        return getVertexColor();
    }

    @Nullable
    @Override
    public String getTextureSuffix()
    {
        return renderInfo.getTextureSuffix();
    }

    @Nonnull
    @Override
    public ITextureController setTextureSuffix(@Nullable final String s)
    {
        renderInfo.setTextureSuffix(s);
        return this;
    }

    @Nonnull
    @Override
    public String getCreationIdentifier()
    {
        return identifier;
    }

    @Nonnull
    @Override
    public ITextureController setCreationIdentifier(final String s)
    {
        this.identifier = s;
        return this;
    }
}
