package ru.mousecray.mouseproject.common.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import ru.mousecray.mouseproject.common.economy.CoinValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MPEventPipeline {
    private static final MPEventPipeline INSTANCE = new MPEventPipeline();

    public CoinDropEvent fireCoinDropEvent(@Nullable EntityPlayer cause, @Nullable EntityLivingBase target, @Nonnull DamageSource causeSource, @Nonnull NonNullList<CoinValue> coins) {
        CoinDropEvent event = new CoinDropEvent(cause, target, causeSource, coins);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static MPEventPipeline instance() { return INSTANCE; }
}
