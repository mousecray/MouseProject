/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.client.gui.core.components.MPGuiRenderHelper;
import ru.mousecray.mouseproject.client.gui.core.components.state.MPGuiElementState;
import ru.mousecray.mouseproject.client.gui.core.container.MPGuiAnchorPanel;
import ru.mousecray.mouseproject.client.gui.core.control.base.MPGuiBaseSlider;
import ru.mousecray.mouseproject.client.gui.core.dim.*;
import ru.mousecray.mouseproject.client.gui.core.misc.MPFontSize;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MPGuiScreen extends GuiScreen {
    protected ResourceLocation TEXTURES;
    protected MPGuiVector      FULL_TEXTURE_SIZE;
    protected MPGuiShape       BACKGROUND_SHAPE;
    private   int              currentElementID = 0;

    protected       List<GuiTextField>     textFieldList  = new ObjectArrayList<>();
    protected final List<MPGuiElement<?>>  focusOrderList = new ObjectArrayList<>();
    private final   Map<Integer, Runnable> globalKeybinds = new HashMap<>();

    private int guiLeft, guiTop;
    protected MPGuiShape guiShape, guiContentShape;
    protected     MPGuiVector guiBound;
    private final String      screenName;
    protected     MPGuiVector guiDefaultSize, guiDefaultBound;

    @Nullable private MPGuiAnchorPanel rootPanel;
    private           int              currentFocusIndex = -1;

    protected MPFontSize fontSize = MPFontSize.NORMAL;

    protected MPGuiScreen(String screenName, MPGuiVector guiDefaultSize, MPGuiVector guiDefaultBound) {
        this.screenName = screenName;
        this.guiDefaultSize = guiDefaultSize;
        this.guiDefaultBound = guiDefaultBound;
    }

    public List<GuiButton> getButtonList()                 { return buttonList; }
    public List<GuiLabel> getLabelList()                   { return labelList; }
    public List<GuiTextField> getFieldsList()              { return textFieldList; }
    public String getScreenName()                          { return screenName; }
    public FontRenderer getFontRenderer()                  { return fontRenderer; }
    public MPFontSize getFontSize()                        { return fontSize; }
    public void setFontSize(MPFontSize fontSize)           { this.fontSize = Objects.requireNonNull(fontSize); }
    public void setFontRenderer(FontRenderer fontRenderer) { this.fontRenderer = Objects.requireNonNull(fontRenderer); }
    public int genNextElementID()                          { return ++currentElementID; }

    @Override
    public void initGui() {
        super.initGui();
        guiLeft = (int) guiContentShape.x();
        guiTop = (int) guiContentShape.y();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int i = guiLeft;
        int j = guiTop;
        drawDefaultBackground();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        drawGuiBackgroundLayer(partialTicks, mouseX, mouseY);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        if (rootPanel != null) rootPanel.dispatchDraw(mc, mouseX, mouseY, partialTicks);

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) i, (float) j, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawContent(partialTicks, mouseX, mouseY);
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
    }

    protected void setBackground(ResourceLocation textures, MPGuiVector fullTextureSize, MPGuiShape backgroundShape) {
        TEXTURES = textures;
        FULL_TEXTURE_SIZE = fullTextureSize;
        BACKGROUND_SHAPE = backgroundShape;
    }

    protected void drawGuiBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (TEXTURES != null && FULL_TEXTURE_SIZE != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(TEXTURES);
            MPGuiRenderHelper.drawTexture(
                    guiShape.x(), guiShape.y(),
                    BACKGROUND_SHAPE.x(), BACKGROUND_SHAPE.y(), BACKGROUND_SHAPE.width(), BACKGROUND_SHAPE.height(),
                    guiShape.width(), guiShape.height(),
                    FULL_TEXTURE_SIZE.x(), FULL_TEXTURE_SIZE.y()
            );
        }
    }

    protected void closeGui() {
        mc.displayGuiScreen(null);
        if (mc.currentScreen == null) mc.setIngameFocus();
    }

    protected void drawContent(float partialTicks, int mouseX, int mouseY) { }

    public <T extends MPGuiTextField<T>> T addTextField(T field)           { return addTextField(field, null, null, null); }
    public <T extends MPGuiTextField<T>> T addTextField(T field, @Nullable MPGuiMargin margin, @Nullable MPAnchorPos anchor, @Nullable MPGuiVector offset) {
        rootPanel.addChild(field, margin, anchor, offset);
        return field;
    }

    public <T extends MPGuiLabel<T>> T addLabel(T label) { return addLabel(label, null, null, null); }
    public <T extends MPGuiLabel<T>> T addLabel(T label, @Nullable MPGuiMargin margin, @Nullable MPAnchorPos anchor, @Nullable MPGuiVector offset) {
        rootPanel.addChild(label, margin, anchor, offset);
        return label;
    }

    public <T extends MPGuiBaseSlider<T>> T addSlider(T slider) { return addSlider(slider, null, null, null); }
    public <T extends MPGuiBaseSlider<T>> T addSlider(T slider, @Nullable MPGuiMargin margin, @Nullable MPAnchorPos anchor, @Nullable MPGuiVector offset) {
        rootPanel.addChild(slider, margin, anchor, offset);
        return slider;
    }

    public <T extends MPGuiButton<T>> T addButton(T button) { return addButton(button, null, null, null); }
    public <T extends MPGuiButton<T>> T addButton(T button, @Nullable MPGuiMargin margin, @Nullable MPAnchorPos anchor, @Nullable MPGuiVector offset) {
        rootPanel.addChild(button, margin, anchor, offset);
        return button;
    }

    public <T extends MPGuiPanel<T>> T addPanel(T panel) { return addPanel(panel, null, null, null); }
    public <T extends MPGuiPanel<T>> T addPanel(T panel, @Nullable MPGuiMargin margin, @Nullable MPAnchorPos anchor, @Nullable MPGuiVector offset) {
        rootPanel.addChild(panel, margin, anchor, offset);
        return panel;
    }

    public <T extends MPGuiScrollPanel<T>> T addPanel(T panel) { return addPanel(panel, null, null, null); }
    public <T extends MPGuiScrollPanel<T>> T addPanel(T panel, @Nullable MPGuiMargin margin, @Nullable MPAnchorPos anchor, @Nullable MPGuiVector offset) {
        rootPanel.addChild(panel, margin, anchor, offset);
        return panel;
    }

    @SuppressWarnings({ "NullableProblems", "unchecked", "rawtypes" }) @Override @Nullable
    protected <T extends GuiButton> T addButton(T button) {
        if (button instanceof MPGuiButton) return (T) addButton(((MPGuiButton) button), null, null, null);
        MouseProject.LOGGER.error("Button {} isn't MPGuiButton. In will be skipped.", button.getClass());
        return null;
    }

    protected void resetGui() {
        currentElementID = 0;
        rootPanel = new MPGuiAnchorPanel(new MPGuiShape(0, 0, guiDefaultSize.x(), guiDefaultSize.y()));
        rootPanel.setScreen(this);
        rootPanel.setScaleRules(new MPGuiScaleRules(MPGuiScaleType.FLOW));
        buttonList.clear();
        labelList.clear();
        textFieldList.clear();
    }

    public void addGlobalKeybind(int keyCode, Runnable action) {
        globalKeybinds.put(keyCode, action);
    }

    public void bake() {
        if (rootPanel != null) {
            rootPanel.calculate(guiDefaultSize, guiContentShape.size(), guiContentShape);

            buttonList.clear();
            labelList.clear();
            textFieldList.clear();
            focusOrderList.clear();
            currentFocusIndex = -1;

            rootPanel.collectElements();
            collectFocusableElements(rootPanel);
        }
    }

    private void collectFocusableElements(MPGuiPanel<?> panel) {
        for (MPGuiElement<?> child : panel.getChildren()) {
            if (!child.getStateManager().isForbidden(MPGuiElementState.FOCUSED)) focusOrderList.add(child);
            if (child instanceof MPGuiPanel) collectFocusableElements((MPGuiPanel<?>) child);
        }
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        this.mc = mc;
        itemRender = mc.getRenderItem();
        fontRenderer = mc.fontRenderer;
        this.width = width;
        this.height = height;
        int newSizeY = (int) (height / 1.15D);
        int newSizeX = (int) Math.min(width, height / 0.8D);
        guiShape = new MPGuiShape((width - newSizeX) / 2f, (height - newSizeY - 20) / 2f, newSizeX, newSizeY);
        guiBound = MPGuiVector.of(
                guiShape.width() * guiDefaultBound.x() / guiDefaultSize.x(),
                guiShape.height() * guiDefaultBound.y() / guiDefaultSize.y());
        guiContentShape = new MPGuiShape(
                guiShape.x() + guiBound.x(), guiShape.y() + guiBound.y(),
                guiShape.width() - guiBound.x() * 2, guiShape.height() - guiBound.y() * 2
        );
        if (!MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Pre(this, buttonList))) {
            resetGui();
            initGui();
            bake();
        }
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Post(this, buttonList));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (rootPanel != null) {
            int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

            rootPanel.dispatchProcessHover(mc, mouseX, mouseY);

            rootPanel.dispatchUpdate(mc, mouseX, mouseY, mc.getRenderPartialTicks());
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        boolean handled = false;

        if (rootPanel != null) {
            handled = rootPanel.dispatchMousePressed(mc, mouseX, mouseY, mouseButton);

            if (handled) {
                MPGuiElement<?> clicked = rootPanel.getLastSelectedElementRecursively();
                if (clicked != null) setFocusTo(clicked);
            }
        }

        if (!handled) super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (rootPanel != null) rootPanel.dispatchMouseReleased(mc, mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0 && rootPanel != null) {
            int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            rootPanel.dispatchMouseScrolled(mc, mouseX, mouseY, scroll);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (globalKeybinds.containsKey(keyCode)) {
            globalKeybinds.get(keyCode).run();
            return;
        }

        if (keyCode == Keyboard.KEY_TAB && !focusOrderList.isEmpty()) {
            boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

            if (currentFocusIndex >= 0 && currentFocusIndex < focusOrderList.size()) {
                focusOrderList.get(currentFocusIndex).getStateManager().remove(MPGuiElementState.FOCUSED);
            }

            if (shift) {
                currentFocusIndex = currentFocusIndex - 1 < 0 ? focusOrderList.size() - 1 : currentFocusIndex - 1;
            } else {
                currentFocusIndex = (currentFocusIndex + 1) % focusOrderList.size();
            }

            focusOrderList.get(currentFocusIndex).getStateManager().add(MPGuiElementState.FOCUSED);
            return;
        }

        boolean handled = false;
        if (rootPanel != null) {
            int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            handled = rootPanel.dispatchKeyTyped(mc, mouseX, mouseY, typedChar, keyCode);
        }

        if (!handled) super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected final void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof MPGuiElement) {
            ((MPGuiElement<?>) button).performClickFromVanilla();
            onClickButton(((MPGuiButton<?>) button));
        } else super.actionPerformed(button);
    }

    public void rebuildFocusList() {
        focusOrderList.clear();
        currentFocusIndex = -1;
        if (rootPanel != null) collectFocusableElements(rootPanel);
    }

    private void setFocusTo(MPGuiElement<?> element) {
        if (element.getStateManager().isForbidden(MPGuiElementState.FOCUSED)) return;

        if (currentFocusIndex >= 0 && currentFocusIndex < focusOrderList.size()) {
            focusOrderList.get(currentFocusIndex).getStateManager().remove(MPGuiElementState.FOCUSED);
        }

        int index = focusOrderList.indexOf(element);
        if (index != -1) {
            currentFocusIndex = index;
            element.getStateManager().add(MPGuiElementState.FOCUSED);
        }
    }

    @Override public void onGuiClosed()                      { super.onGuiClosed(); }

    protected void onClickButton(MPGuiButton<?> button)      { }
    protected void onClickTextField(MPGuiTextField<?> field) { }
    protected void onClickLabel(MPGuiLabel<?> label)         { }
}