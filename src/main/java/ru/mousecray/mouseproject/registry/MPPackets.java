/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.registry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.network.handler.HandlerWalletSyncPacket;
import ru.mousecray.mouseproject.network.packet.PacketWalletSync;

public class MPPackets {
    public static final MPPackets            INSTANCE = new MPPackets();
    private             int                  lastID;
    private final       SimpleNetworkWrapper NETWORK  = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);

    private MPPackets() { }

    public void register() {
        registerPacket(HandlerWalletSyncPacket.class, PacketWalletSync.class, Side.SERVER);
    }

    private <REQ extends IMessage, REPLY extends IMessage> void registerPacket(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side) {
        NETWORK.registerMessage(messageHandler, requestMessageType, lastID++, side);
    }

    public void sendToServer(IMessage message)                  { NETWORK.sendToServer(message); }
    public void sendTo(IMessage message, EntityPlayerMP player) { NETWORK.sendTo(message, player); }
}