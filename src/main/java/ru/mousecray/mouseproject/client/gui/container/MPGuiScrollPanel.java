package ru.mousecray.mouseproject.client.gui.container;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;
import ru.mousecray.mouseproject.client.gui.dim.GuiShape;
import ru.mousecray.mouseproject.client.gui.dim.IGuiShape;
import ru.mousecray.mouseproject.client.gui.dim.IGuiVector;
import ru.mousecray.mouseproject.client.gui.dim.MutableGuiShape;

@SideOnly(Side.CLIENT)
public abstract class MPGuiScrollPanel<T extends MPGuiScrollPanel<T>> extends MPGuiPanel<T> {
    protected float   scrollY       = 0;
    protected float   contentHeight = 0;
    protected boolean scrollEnabled = true;

    public MPGuiScrollPanel(GuiShape elementShape) {
        super(elementShape);
    }

    @Override
    public void calculate(IGuiVector parentDefaultSize, IGuiVector parentContentSize, IGuiShape available) {
        super.calculate(parentDefaultSize, parentContentSize, available);
        updateContentHeight();
        applyScroll(0);
    }

    protected void updateContentHeight() {
        float maxBottom = 0;
        float myY       = getCalculatedElementShape().y();
        for (MPGuiElement<?> child : children) {
            MutableGuiShape childShape = child.getCalculatedElementShape();
            float           bottom     = childShape.y() + childShape.height();
            if (bottom > maxBottom) maxBottom = bottom;
        }
        contentHeight = Math.max(0, maxBottom - myY);
    }

    @Override
    public void onUpdate0(Minecraft mc, int mouseX, int mouseY) {
        super.onUpdate0(mc, mouseX, mouseY);
        if (scrollEnabled && mouseHover(mc, mouseX, mouseY)) {
            int dWheel = Mouse.getDWheel();
            if (dWheel != 0) {
                applyScroll(-dWheel / 10f);
            }
        }
    }

    public void applyScroll(float amount) {
        float oldScroll = scrollY;
        scrollY += amount;
        float maxScroll = Math.max(0, contentHeight - getCalculatedElementShape().height());
        if (scrollY < 0) scrollY = 0;
        if (scrollY > maxScroll) scrollY = maxScroll;

        float diff = scrollY - oldScroll;
        if (diff != 0) {
            offsetChildren(0, -diff);
        }
    }

    protected void offsetChildren(float dx, float dy) {
        for (MPGuiElement<?> child : children) {
            child.offsetCalculatedShape(dx, dy);
        }
    }

    @Override
    public void onDrawBackground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        drawPanelBackground(mc, mouseX, mouseY, partialTicks);

        if (children.isEmpty()) return;

        setupScissor(mc);
        for (MPGuiElement<?> child : children) child.onDrawBackground(mc, mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (contentHeight > getCalculatedElementShape().height()) {
            drawScrollBar(mc);
        }
    }

    @Override
    public void onDrawForeground(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (children.isEmpty()) return;
        setupScissor(mc);
        for (MPGuiElement<?> child : children) child.onDrawForeground(mc, mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void onDrawText(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (children.isEmpty()) return;
        setupScissor(mc);
        for (MPGuiElement<?> child : children) child.onDrawText(mc, mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected void setupScissor(Minecraft mc) {
        int             scale = new ScaledResolution(mc).getScaleFactor();
        MutableGuiShape shape = getCalculatedElementShape();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (shape.x() * scale),
                (int) (mc.displayHeight - (shape.y() + shape.height()) * scale),
                (int) (shape.width() * scale),
                (int) (shape.height() * scale));
    }

    protected void drawScrollBar(Minecraft mc) {
        // TODO: Отрисовка слайдера (заглушка)
        // Пока просто комментарий, так как текстур нет
    }
}
