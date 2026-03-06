package ru.mousecray.mouseproject.client.gui.impl.container;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.container.MPGuiScrollPanel;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;

@SideOnly(Side.CLIENT)
public class MPGuiSimpleScrollPanel extends MPGuiScrollPanel<MPGuiSimpleScrollPanel> {
    public MPGuiSimpleScrollPanel(GuiShape elementShape) {
        super(elementShape);
    }

    @Override
    public MPGuiSimpleScrollPanel self() {
        return this;
    }
}
