package ru.mousecray.mouseproject.client.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.container.MPGuiScrollPanel;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.impl.MPGuiSlider;
import ru.mousecray.mouseproject.client.gui.impl.container.MPGuiAnchorPanel;
import ru.mousecray.mouseproject.client.gui.misc.GuiRenderHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MPGuiScreen extends GuiScreen {
    protected         ResourceLocation   TEXTURES;
    protected         GuiVector          FULL_TEXTURE_SIZE;
    protected         GuiShape           BACKGROUND_SHAPE;
    private           int                currentElementID     = 0;
    @Nullable private MPGuiButton<?>     lastHoveredButton    = null;
    @Nullable private MPGuiTextField<?>  lastHoveredTextField = null;
    @Nullable private MPGuiLabel<?>      lastHoveredLabel     = null;
    @Nullable private MPGuiTextField<?>  selectedTextField    = null;
    @Nullable private MPGuiLabel<?>      selectedLabel        = null;
    @Nullable private MPGuiTextField<?>  focusedTextField     = null;
    protected         List<GuiTextField> textFieldList        = new ObjectArrayList<>();
    private           int                guiLeft, guiTop;
    protected GuiShape guiShape, guiContentShape;
    protected GuiVector guiBound;
    protected GuiVector guiDefaultSize, guiDefaultBound;

    private       MPGuiAnchorPanel  rootPanel;
    private final MPGuiElementCache elementCache = new MPGuiElementCache();

    protected MPGuiScreen(GuiVector guiDefaultSize, GuiVector guiDefaultBound) {
        this.guiDefaultSize = guiDefaultSize;
        this.guiDefaultBound = guiDefaultBound;
    }

    @Override
    public void initGui() {
        super.initGui();
        guiLeft = (int) guiContentShape.x();
        guiTop = (int) guiContentShape.y();
        rootPanel.calculate(guiDefaultSize, guiContentShape.size(), guiContentShape);
        buttonList.clear();
        labelList.clear();
        textFieldList.clear();
        rootPanel.collectElements();
    }

    public List<GuiButton> getButtonList()    { return buttonList; }
    public List<GuiLabel> getLabelList()      { return labelList; }
    public List<GuiTextField> getFieldsList() { return textFieldList; }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        boolean handled = false;
        if (focusedTextField != null) {
            focusedTextField.onKeyTyped0(typedChar, keyCode);
            handled = true;
        }
        if (!handled) super.keyTyped(typedChar, keyCode);
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

        rootPanel.onDrawBackground(mc, mouseX, mouseY, partialTicks);
        rootPanel.onDrawForeground(mc, mouseX, mouseY, partialTicks);
        rootPanel.onDrawText(mc, mouseX, mouseY, partialTicks);
        rootPanel.onDrawLast(mc, mouseX, mouseY, partialTicks);

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

    protected void setBackground(ResourceLocation textures, GuiVector fullTextureSize, GuiShape backgroundShape) {
        TEXTURES = textures;
        FULL_TEXTURE_SIZE = fullTextureSize;
        BACKGROUND_SHAPE = backgroundShape;
    }

    protected void drawGuiBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (TEXTURES != null && FULL_TEXTURE_SIZE != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(TEXTURES);
            GuiRenderHelper.drawTexture(
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

    protected <T extends MPGuiTextField<T>> T addTextField(T field)        { return addTextField(field, null, null, null); }
    protected <T extends MPGuiTextField<T>> T addTextField(T field, @Nullable GuiMargin margin, @Nullable AnchorPosition anchor, @Nullable GuiVector offset) {
        rootPanel.addChild(field, margin, anchor, offset);
        return field;
    }

    protected <T extends MPGuiLabel<T>> T addLabel(T label) { return addLabel(label, null, null, null); }
    protected <T extends MPGuiLabel<T>> T addLabel(T label, @Nullable GuiMargin margin, @Nullable AnchorPosition anchor, @Nullable GuiVector offset) {
        rootPanel.addChild(label, margin, anchor, offset);
        return label;
    }

    protected <T extends MPGuiSlider<T>> T addSlider(T slider) { return addSlider(slider, null, null, null); }
    protected <T extends MPGuiSlider<T>> T addSlider(T slider, @Nullable GuiMargin margin, @Nullable AnchorPosition anchor, @Nullable GuiVector offset) {
        rootPanel.addChild(slider, margin, anchor, offset);
        return slider;
    }

    protected <T extends MPGuiButton<T>> T addButton(T button) { return addButton(button, null, null, null); }
    protected <T extends MPGuiButton<T>> T addButton(T button, @Nullable GuiMargin margin, @Nullable AnchorPosition anchor, @Nullable GuiVector offset) {
        rootPanel.addChild(button, margin, anchor, offset);
        return button;
    }

    protected <T extends MPGuiPanel<T>> T addPanel(T panel) { return addPanel(panel, null, null, null); }
    protected <T extends MPGuiPanel<T>> T addPanel(T panel, @Nullable GuiMargin margin, @Nullable AnchorPosition anchor, @Nullable GuiVector offset) {
        rootPanel.addChild(panel, margin, anchor, offset);
        return panel;
    }

    protected <T extends MPGuiScrollPanel<T>> T addPanel(T panel) { return addPanel(panel, null, null, null); }
    protected <T extends MPGuiScrollPanel<T>> T addPanel(T panel, @Nullable GuiMargin margin, @Nullable AnchorPosition anchor, @Nullable GuiVector offset) {
        rootPanel.addChild(panel, margin, anchor, offset);
        return panel;
    }

    @SuppressWarnings("NullableProblems") @Override @Nullable
    protected <T extends GuiButton> T addButton(T button) {
        if (button instanceof MPGuiButton) {
            ((MPGuiButton<?>) button).setId(currentElementID++);
            return super.addButton(button);
        }
        MouseProject.LOGGER.error("Button {} isn't MPGuiButton. In will be skipped.", button.getClass());
        return null;
    }

    public int genNextElementID()              { return ++currentElementID; }
    public MPGuiElementCache getElementCache() { return elementCache; }

    protected void resetGui() {
        currentElementID = 0;
        rootPanel = new MPGuiAnchorPanel(new GuiShape(0, 0, guiDefaultSize.x(), guiDefaultSize.y()));
        rootPanel.setScreen(this);
        rootPanel.setScaleRules(new GuiScaleRules(GuiScaleType.FLOW));
        buttonList.clear();
        labelList.clear();
        textFieldList.clear();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        elementCache.clear();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            for (int i = 0; i < buttonList.size(); ++i) {
                MPGuiButton<?> guibutton = (MPGuiButton<?>) buttonList.get(i);
                if (guibutton.mousePressed(mc, mouseX, mouseY)) {
                    net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, buttonList);
                    if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) break;
                    GuiButton button = event.getButton();
                    if (button instanceof MPGuiButton) guibutton = ((MPGuiButton<?>) button);
                    else break;
                    selectedButton = guibutton;
                    guibutton.onMousePressed0(mc, mouseX, mouseY);
                    actionPerformed(guibutton);
                    if (equals(mc.currentScreen))
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this, button, buttonList));
                }
            }
            for (GuiTextField guiTextField : textFieldList) {
                MPGuiTextField<?> gf = (MPGuiTextField<?>) guiTextField;
                if (gf.mousePressed(mc, mouseX, mouseY)) {
                    selectedTextField = gf;
                    gf.onMousePressed0(mc, mouseX, mouseY);
                    onClickTextField(gf);
                }
            }
            for (GuiLabel guiLabel : labelList) {
                MPGuiLabel<?> gl = (MPGuiLabel<?>) guiLabel;
                if (gl.mousePressed(mc, mouseX, mouseY)) {
                    selectedLabel = gl;
                    gl.onMousePressed0(mc, mouseX, mouseY);
                    onClickLabel(gl);
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (selectedButton != null && state == 0) {
            ((MPGuiButton<?>) selectedButton).onMouseReleased0(mc, mouseX, mouseY);
            selectedButton = null;
        }
        if (selectedTextField != null && state == 0) {
            selectedTextField.onMouseReleased0(mc, mouseX, mouseY);
            selectedTextField = null;
        }
        if (selectedLabel != null && state == 0) {
            selectedLabel.onMouseReleased0(mc, mouseX, mouseY);
            selectedLabel = null;
        }

        boolean focusHandled = false;
        for (GuiTextField guiTextField : textFieldList) {
            MPGuiTextField<?> tf = (MPGuiTextField<?>) guiTextField;
            if (tf.mouseHover(mc, mouseX, mouseY)) {
                if (focusedTextField != null && focusedTextField != guiTextField) focusedTextField.setFocused(false);
                focusedTextField = tf;
                focusedTextField.setFocused(true);
                focusHandled = true;
            }
        }

        if (!focusHandled) {
            if (focusedTextField != null) {
                focusedTextField.setFocused(false);
                focusedTextField = null;
            }
        }
    }

    @Override protected final void actionPerformed(GuiButton button) { onClickButton(((MPGuiButton<?>) button)); }

    protected void onClickButton(MPGuiButton<?> button)              { }
    protected void onClickTextField(MPGuiTextField<?> field)         { }
    protected void onClickLabel(MPGuiLabel<?> label)                 { }

    public void bake()                                               { }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        this.mc = mc;
        itemRender = mc.getRenderItem();
        fontRenderer = mc.fontRenderer;
        this.width = width;
        this.height = height;
        int newSizeY = (int) (height / 1.15D);
        int newSizeX = (int) (height / 0.8D);
        guiShape = new GuiShape((width - newSizeX) / 2f, (height - newSizeY - 20) / 2f, newSizeX, newSizeY);
        guiBound = new GuiVector(
                guiShape.width() * guiDefaultBound.x() / guiDefaultSize.x(),
                guiShape.height() * guiDefaultBound.y() / guiDefaultSize.y());
        guiContentShape = new GuiShape(
                guiShape.x() + guiBound.x(), guiShape.y() + guiBound.y(),
                guiShape.width() - guiBound.x() * 2, guiShape.height() - guiBound.y() * 2
        );
        if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Pre(this, buttonList))) {
            resetGui();
            initGui();
            bake();
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Post(this, buttonList));
    }

    @SuppressWarnings("rawtypes") @Override
    public void updateScreen() {
        int       i              = Mouse.getEventX() * width / mc.displayWidth;
        int       j              = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        Minecraft mc             = Minecraft.getMinecraft();
        int       buttonListSize = buttonList.size();

        for (int k = 0; k < buttonListSize; ++k) {
            MPGuiButton rdButton = (MPGuiButton) buttonList.get(k);
            rdButton.onUpdate0(mc, i, j);
        }

        MPGuiButton currentHoveredButton = null;

        for (int k = buttonListSize - 1; k >= 0; --k) {
            MPGuiButton rdButton = (MPGuiButton) buttonList.get(k);
            if (rdButton.mouseHover(mc, i, j)) {
                currentHoveredButton = rdButton;
                break;
            }
        }

        if (currentHoveredButton != lastHoveredButton) {
            if (lastHoveredButton != null) lastHoveredButton.onMouseLeave0(mc, i, j);
            if (currentHoveredButton != null) currentHoveredButton.onMouseEnter0(mc, i, j);
            lastHoveredButton = currentHoveredButton;
        }

        int textFieldListSize = textFieldList.size();
        for (int k = 0; k < textFieldListSize; ++k) {
            MPGuiTextField rdField = (MPGuiTextField) textFieldList.get(k);
            rdField.onUpdate0(mc, i, j);
        }

        MPGuiTextField currentHoveredTextField = null;

        for (int k = textFieldListSize - 1; k >= 0; --k) {
            MPGuiTextField rdField = (MPGuiTextField) textFieldList.get(k);
            if (rdField.mouseHover(mc, i, j)) {
                currentHoveredTextField = rdField;
                break;
            }
        }

        if (currentHoveredTextField != lastHoveredTextField) {
            if (lastHoveredTextField != null) lastHoveredTextField.onMouseLeave0(mc, i, j);
            if (currentHoveredTextField != null) currentHoveredTextField.onMouseEnter0(mc, i, j);
            lastHoveredTextField = currentHoveredTextField;
        }

        int labelListSize = labelList.size();
        for (int k = 0; k < labelListSize; ++k) {
            MPGuiLabel rdLabel = (MPGuiLabel) labelList.get(k);
            rdLabel.onUpdate0(mc, i, j);
        }

        MPGuiLabel currentHoveredLabel = null;

        for (int k = labelListSize - 1; k >= 0; --k) {
            MPGuiLabel rdLabel = (MPGuiLabel) labelList.get(k);
            if (rdLabel.mouseHover(mc, i, j)) {
                currentHoveredLabel = rdLabel;
                break;
            }
        }

        if (currentHoveredLabel != lastHoveredLabel) {
            if (lastHoveredLabel != null) lastHoveredLabel.onMouseLeave0(mc, i, j);
            if (currentHoveredLabel != null) currentHoveredLabel.onMouseEnter0(mc, i, j);
            lastHoveredLabel = currentHoveredLabel;
        }
    }
}