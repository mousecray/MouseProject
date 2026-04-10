/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.core.control.base;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.core.MPGuiTextField;
import ru.mousecray.mouseproject.client.gui.core.components.texture.MPGuiTexturePack;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiShape;
import ru.mousecray.mouseproject.client.gui.core.dim.MPGuiVector;
import ru.mousecray.mouseproject.client.gui.core.event.MPGuiTextTypedEvent;
import ru.mousecray.mouseproject.utils.MPStaticData;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MPGuiBaseTextField<T extends MPGuiBaseTextField<T>> extends MPGuiTextField<T> {
    private Consumer<MPGuiTextTypedEvent<T>> onTextTypedListener;

    public MPGuiBaseTextField(MPGuiShape shape) {
        super(shape);
        setTexturePack(createTexturePack());
    }

    protected MPGuiTexturePack createTexturePack() {
        return MPGuiTexturePack.Builder
                .create(
                        MPStaticData.CONTROLS_TEXTURES, MPStaticData.CONTROLS_TEXTURES_SIZE,
                        MPGuiVector.of(104, 0), MPGuiVector.of(80, 10)
                )
                .addTexture(0)
                .build();
    }

    public T setOnTextTypedListener(@Nullable Consumer<MPGuiTextTypedEvent<T>> listener) {
        onTextTypedListener = listener;
        return self();
    }

    public Consumer<MPGuiTextTypedEvent<T>> getOnTextTypedListener() { return onTextTypedListener; }

    @Override
    protected void onTextTyped(MPGuiTextTypedEvent<T> event) {
        super.onTextTyped(event);
        if (onTextTypedListener != null && !event.isCancelled()) onTextTypedListener.accept(event);
    }
}