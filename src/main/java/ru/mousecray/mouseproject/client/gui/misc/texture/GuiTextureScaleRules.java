package ru.mousecray.mouseproject.client.gui.misc.texture;

import java.util.*;

public class GuiTextureScaleRules {
    private final Set<GuiTextureScaleType> scaleTypes  = new HashSet<>();
    private       float                    multiplierX = 1.0f;
    private       float                    multiplierY = 1.0f;

    public GuiTextureScaleRules(GuiTextureScaleType... types) {
        for (GuiTextureScaleType type : types) addType(type);
    }

    private void addType(GuiTextureScaleType newType) {
        List<GuiTextureScaleType> toRemove = new ArrayList<>();
        for (GuiTextureScaleType existing : scaleTypes) {
            if (isIncompatible(existing, newType)) toRemove.add(existing);
        }
        toRemove.forEach(scaleTypes::remove);

        scaleTypes.add(newType);

        if (scaleTypes.contains(GuiTextureScaleType.STRETCH)) {
            scaleTypes.remove(GuiTextureScaleType.STRETCH_HORIZONTAL);
            scaleTypes.remove(GuiTextureScaleType.STRETCH_VERTICAL);
        }
        if (scaleTypes.contains(GuiTextureScaleType.FILL)) {
            scaleTypes.remove(GuiTextureScaleType.FILL_HORIZONTAL);
            scaleTypes.remove(GuiTextureScaleType.FILL_VERTICAL);
        }
    }

    private boolean isIncompatible(GuiTextureScaleType a, GuiTextureScaleType b) {
        GuiTextureScaleType.Category catA = a.getCategory();
        GuiTextureScaleType.Category catB = b.getCategory();

        if (catA == catB && axesOverlap(a, b)) return true;
        return axesOverlap(a, b);
    }

    private boolean axesOverlap(GuiTextureScaleType a, GuiTextureScaleType b) {
        Set<GuiTextureScaleType.Axes> axesA = a.getAxes();
        Set<GuiTextureScaleType.Axes> axesB = b.getAxes();
        axesA.retainAll(axesB);
        return !axesA.isEmpty();
    }

    public GuiTextureScaleRules setMultipliers(float multiplierX, float multiplierY) {
        this.multiplierX = multiplierX;
        this.multiplierY = multiplierY;
        return this;
    }

    public GuiTextureScaleRules setMultiplayerX(float multiplierX) {
        this.multiplierX = multiplierX;
        return this;
    }

    public GuiTextureScaleRules setMultiplayerY(float multiplierY) {
        this.multiplierY = multiplierY;
        return this;
    }

    public GuiTextureScaleRules setMultiplier(float multiplier) {
        return setMultipliers(multiplier, multiplier);
    }

    public float getMultiplierX() { return multiplierX; }
    public float getMultiplierY() { return multiplierY; }

    public ScaleMode getModeX() {
        if (scaleTypes.contains(GuiTextureScaleType.FILL) || scaleTypes.contains(GuiTextureScaleType.FILL_HORIZONTAL))
            return ScaleMode.FILL;
        if (scaleTypes.stream().anyMatch(t -> t.getCategory() == GuiTextureScaleType.Category.SINGLE && t.getAxes().contains(GuiTextureScaleType.Axes.HORIZONTAL)))
            return ScaleMode.SINGLE;
        return ScaleMode.STRETCH;
    }

    public ScaleMode getModeY() {
        if (scaleTypes.contains(GuiTextureScaleType.FILL) || scaleTypes.contains(GuiTextureScaleType.FILL_VERTICAL))
            return ScaleMode.FILL;
        if (scaleTypes.stream().anyMatch(t -> t.getCategory() == GuiTextureScaleType.Category.SINGLE && t.getAxes().contains(GuiTextureScaleType.Axes.VERTICAL)))
            return ScaleMode.SINGLE;
        return ScaleMode.STRETCH;
    }

    public TextureAnchor getAnchorX() {
        if (scaleTypes.contains(GuiTextureScaleType.SINGLE_HORIZONTAL_CENTER)) return TextureAnchor.CENTER;
        if (scaleTypes.contains(GuiTextureScaleType.SINGLE_HORIZONTAL_RIGHT)) return TextureAnchor.MAX;
        return TextureAnchor.MIN;
    }

    public TextureAnchor getAnchorY() {
        if (scaleTypes.contains(GuiTextureScaleType.SINGLE_VERTICAL_CENTER)) return TextureAnchor.CENTER;
        if (scaleTypes.contains(GuiTextureScaleType.SINGLE_VERTICAL_BOTTOM)) return TextureAnchor.MAX;
        return TextureAnchor.MIN;
    }

    public Set<GuiTextureScaleType> getScaleTypes() { return Collections.unmodifiableSet(scaleTypes); }

    public enum ScaleMode {STRETCH, FILL, SINGLE}

    public enum TextureAnchor {MIN, CENTER, MAX}
}