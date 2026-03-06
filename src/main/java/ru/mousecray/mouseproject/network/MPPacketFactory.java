package ru.mousecray.mouseproject.network;

import net.minecraft.item.ItemStack;
import ru.mousecray.mouseproject.network.packet.PacketWalletSync;

public class MPPacketFactory {
    public static PacketWalletSync WALLET_SYNC(ItemStack stack) { return new PacketWalletSync(stack); }
}