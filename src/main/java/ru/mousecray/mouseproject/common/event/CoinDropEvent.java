package ru.mousecray.mouseproject.common.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import ru.mousecray.mouseproject.common.economy.CoinValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Cancelable
public class CoinDropEvent extends PlayerEvent {
    private final EntityLivingBase       target;
    private final DamageSource           causeSource;
    private final NonNullList<CoinValue> coins;

    public CoinDropEvent(@Nullable EntityPlayer cause, @Nullable EntityLivingBase target, @Nonnull DamageSource causeSource, @Nonnull NonNullList<CoinValue> coins) {
        super(cause);
        this.target = target;
        this.causeSource = causeSource;
        this.coins = coins;
    }

    public NonNullList<CoinValue> getCoins() { return coins; }
    public EntityLivingBase getTarget()      { return target; }
    public DamageSource getCauseSource()     { return causeSource; }
}