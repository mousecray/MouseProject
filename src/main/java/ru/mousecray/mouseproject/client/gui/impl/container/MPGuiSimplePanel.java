package ru.mousecray.mouseproject.client.gui.impl.container;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;

@SideOnly(Side.CLIENT)
public class MPGuiSimplePanel extends MPGuiPanel<MPGuiSimplePanel> {
    public MPGuiSimplePanel(GuiShape elementShape) {
        super(elementShape);
    }

    @Override
    public MPGuiSimplePanel self() {
        return this;
    }
}
