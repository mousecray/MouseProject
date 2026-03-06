package ru.mousecray.mouseproject.client.gui.misc;

public enum MPFontSize {
    NORMAL(1.0f), SMALL(1.0f), LARGE(1.5f), EXTRA_LARGE(2.0f);

    private final float vanillaScale;
    MPFontSize(float vanillaScale) { this.vanillaScale = vanillaScale; }
    public float getScale()        { return vanillaScale; }
}