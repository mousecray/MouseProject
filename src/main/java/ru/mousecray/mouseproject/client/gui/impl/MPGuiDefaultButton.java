package ru.mousecray.mouseproject.client.gui.impl;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiButton;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;
import ru.mousecray.mouseproject.client.gui.dim.GuiVector;
import ru.mousecray.mouseproject.client.gui.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonActionState;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class MPGuiDefaultButton extends MPGuiButton<MPGuiDefaultButton> {
    private final Consumer<MPGuiMouseClickEvent<MPGuiDefaultButton>> onClick;

    public MPGuiDefaultButton(
            @Nullable MPGuiString text,
            GuiShape elementShape,
            ResourceLocation texture, GuiVector textureSize, GuiShape textureShape,
            MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<MPGuiDefaultButton>> onClick) {
        super(
                text == null ? "" : text.get(), elementShape,
                MPGuiTexturePack.Builder
                        .create(texture, textureSize, textureShape.pos(), textureShape.size())
                        .addTexture(GuiButtonPersistentState.NORMAL, 0)
                        .addTexture(GuiButtonActionState.HOVER, 1)
                        .addTexture(GuiButtonActionState.PRESSED, 2)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, fontSize
        );
        this.onClick = onClick;
        if (text != null) setGuiString(text);
    }

    public MPGuiDefaultButton(
            @Nullable String text,
            GuiShape elementShape,
            ResourceLocation texture, GuiVector textureSize, GuiShape textureShape,
            MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<MPGuiDefaultButton>> onClick) {
        super(
                text, elementShape,
                MPGuiTexturePack.Builder
                        .create(texture, textureSize, textureShape.pos(), textureShape.size())
                        .addTexture(GuiButtonPersistentState.NORMAL, 0)
                        .addTexture(GuiButtonActionState.HOVER, 1)
                        .addTexture(GuiButtonActionState.PRESSED, 2)
                        .build(),
                SoundEvents.UI_BUTTON_CLICK, fontSize
        );
        this.onClick = onClick;
    }

    @Override
    public void onClick(@Nonnull MPGuiMouseClickEvent<MPGuiDefaultButton> event) {
        if (onClick != null) onClick.accept(event);
    }
}
