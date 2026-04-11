/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import ru.mousecray.mouseproject.client.gui.core.components.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.control.base.MPGuiBaseTextField;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiTextTypedEvent;
import ru.mousecray.mouseproject.client.gui.core.misc.MPNumberMode;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MPGuiNumberField extends MPGuiBaseTextField<MPGuiNumberField> {
    private MPNumberMode numberMode;

    public MPGuiNumberField(MPGuiShape shape, MPGuiString placeholder, MPNumberMode numberMode) {
        super(shape, placeholder);
        this.numberMode = Objects.requireNonNull(numberMode);
        setTexturePack(MPGuiTexturePack.Builder
                .create(
                        MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                        MPGuiVector.of(104, 0), MPGuiVector.of(80, 10)
                )
                .addTexture(0)
                .build());
    }

    public void setNumberMode(MPNumberMode numberMode) { this.numberMode = Objects.requireNonNull(numberMode); }
    public MPNumberMode getNumberMode()                { return numberMode; }

    @Override
    protected void onTextTyped(MPGuiTextTypedEvent<MPGuiNumberField> event) {
        String newText = event.getNewText();

        if (newText == null || newText.isEmpty()) {
            super.onTextTyped(event);
            return;
        }

        if (newText.equals("-")) {
            if (numberMode == MPNumberMode.POSITIVE || numberMode == MPNumberMode.POSITIVE_OR_ZERO) event.setCancelled(true);
            else super.onTextTyped(event);
            return;
        }

        if (!newText.matches("-?\\d+")) {
            event.setCancelled(true);
            return;
        }

        try {
            long val = Long.parseLong(newText);

            if (val > 0 && (numberMode == MPNumberMode.NEGATIVE || numberMode == MPNumberMode.NEGATIVE_OR_ZERO))
                event.setCancelled(true);
            if (val < 0 && (numberMode == MPNumberMode.POSITIVE || numberMode == MPNumberMode.POSITIVE_OR_ZERO))
                event.setCancelled(true);
            if (val == 0 && (numberMode == MPNumberMode.POSITIVE || numberMode == MPNumberMode.NEGATIVE)) event.setCancelled(true);

        } catch (NumberFormatException ex) {
            event.setCancelled(true);
            return;
        }

        if (!event.isCancelled()) super.onTextTyped(event);
    }

    public long getNumberText() {
        String text = getText();
        if (StringUtils.isEmpty(text) || text.equals("-")) return 0;
        return Long.parseLong(text);
    }

    public void setNumberText(long value) { setText(String.valueOf(value)); }
}