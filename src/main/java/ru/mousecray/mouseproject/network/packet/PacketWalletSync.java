/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketWalletSync implements IMessage {
    private ItemStack stack;
    public PacketWalletSync()                    { }
    public PacketWalletSync(ItemStack stack)     { this.stack = stack; }
    @Override public void fromBytes(ByteBuf buf) { stack = ByteBufUtils.readItemStack(buf); }
    @Override public void toBytes(ByteBuf buf)   { ByteBufUtils.writeItemStack(buf, stack); }
    public ItemStack getStack()                  { return stack; }
}