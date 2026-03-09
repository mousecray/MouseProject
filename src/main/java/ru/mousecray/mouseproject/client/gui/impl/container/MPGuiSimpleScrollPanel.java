package ru.mousecray.mouseproject.client.gui.impl.container;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.container.MPGuiScrollPanel;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;

import javax.annotation.ParametersAreNonnullByDefault;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MPGuiSimpleScrollPanel extends MPGuiScrollPanel<MPGuiSimpleScrollPanel> {
    public MPGuiSimpleScrollPanel(GuiShape elementShape) {
        super(elementShape);
    }
}