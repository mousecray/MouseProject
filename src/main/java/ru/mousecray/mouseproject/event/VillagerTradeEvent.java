package ru.mousecray.mouseproject.event;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.village.MerchantRecipe;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class VillagerTradeEvent extends Event {
    private final EntityPlayer   player;
    private final EntityVillager villager;
    private final MerchantRecipe recipe;

    public VillagerTradeEvent(EntityPlayer player, EntityVillager villager, MerchantRecipe recipe) {
        this.player = player;
        this.villager = villager;
        this.recipe = recipe;
    }

    public EntityPlayer getPlayer()     { return player; }
    public EntityVillager getVillager() { return villager; }
    public MerchantRecipe getRecipe()   { return recipe; }
}