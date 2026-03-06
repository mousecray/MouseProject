package ru.mousecray.mouseproject.mixins;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.common.capability.ICapabilityInventory;
import ru.mousecray.mouseproject.common.capability.impl.CapabilityWalletInventory;
import ru.mousecray.mouseproject.common.inventory.slot.WalletSlot;

@Mixin(GuiInventory.class)
public class MixinWalletGuiSurvival {
    @SuppressWarnings({ "UnresolvedMixinReference", "DataFlowIssue", "UnreachableCode" })
    @Inject(method = "drawGuiContainerBackgroundLayer(FII)V", at = @At("TAIL"))
    private void drawCustomSlotBackground(float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        GuiInventory                                    gui             = (GuiInventory) (Object) this;
        ICapabilityInventory<CapabilityWalletInventory> walletInventory = gui.mc.player.getCapability(CapabilityWalletInventory.InventoryProvider.WALLET_INVENTORY, null);

        if (walletInventory != null) {
            Slot slot = gui.inventorySlots.getSlotFromInventory(walletInventory.getInventory(), 0);
            if (slot instanceof WalletSlot) {
                int slotX = gui.getGuiLeft() + slot.xPos - 1;
                int slotY = gui.getGuiTop() + slot.yPos - 1;

                //Рендер фона слота
                gui.mc.getTextureManager().bindTexture(new ResourceLocation(Tags.MOD_ID, "textures/gui/container/slot/wallet_slot_background.png"));
                Gui.drawModalRectWithCustomSizedTexture(slotX, slotY, 0, 0, 18, 18, 18, 18); //Фон слота 18x18

                if (!slot.getHasStack()) {
                    //Рендер пустого слота
                    gui.mc.getTextureManager().bindTexture(new ResourceLocation(Tags.MOD_ID, "textures/gui/container/slot/wallet_slot_empty.png"));
                    Gui.drawModalRectWithCustomSizedTexture(slotX, slotY, 0, 0, 18, 18, 18, 18); //Фон слота 16x16
                }
            }
        }
    }
}