package ru.mousecray.mouseproject.client.gui.impl;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiLabel;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;
import ru.mousecray.mouseproject.client.gui.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class MPGuiStaticLabel extends MPGuiLabel<MPGuiStaticLabel> {
    public MPGuiStaticLabel(MPGuiString text, FontRenderer fontRenderer, GuiShape elementShape, int color, MPFontSize fontSize, @Nullable SoundEvent soundClick) {
        super(text.get(), fontRenderer, elementShape, color, fontSize, soundClick);
        setGuiString(text);
    }

    public MPGuiStaticLabel(String text, FontRenderer fontRenderer, GuiShape elementShape, int color, MPFontSize fontSize, @Nullable SoundEvent soundClick) {
        super(text, fontRenderer, elementShape, color, fontSize, soundClick);
    }

    public MPGuiStaticLabel(MPGuiString text, FontRenderer fontRenderer, GuiShape elementShape, int color, MPFontSize fontSize) {
        this(text, fontRenderer, elementShape, color, fontSize, null);
    }

    public MPGuiStaticLabel(String text, FontRenderer fontRenderer, GuiShape elementShape, int color, MPFontSize fontSize) {
        this(text, fontRenderer, elementShape, color, fontSize, null);
    }

    @Override public void onClick(MPGuiMouseClickEvent<MPGuiStaticLabel> event) { }
}