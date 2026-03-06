package ru.mousecray.mouseproject.client.gui.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.client.gui.MPGuiElement;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

@SideOnly(Side.CLIENT)
public abstract class MPGuiEvent<T extends MPGuiElement<T>> {
    private WeakReference<T> obj;
    private Minecraft        mc;
    private int              mouseX, mouseY;
    private boolean cancelled;

    void setMc(Minecraft mc)                    { this.mc = mc; }
    void setMouseX(int mouseX)                  { this.mouseX = mouseX; }
    void setMouseY(int mouseY)                  { this.mouseY = mouseY; }
    void setObj(T obj)                          { this.obj = new WeakReference<>(obj); }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    public Minecraft getMc()                    { return mc; }
    public int getMouseX()                      { return mouseX; }
    public int getMouseY()                      { return mouseY; }
    public boolean isCancelled()                { return cancelled; }
    @Nullable public T getObj()                 { return obj.get(); }
}