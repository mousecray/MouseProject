package ru.mousecray.mouseproject.common.eventhandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ru.mousecray.mouseproject.nbt.MouseProjectNBT;

public class MagicWalletHandler {
    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntityPlayer().world.isRemote) return;

        Entity target = event.getTarget();
        if (target instanceof IMob) {
            if (MouseProjectNBT.get(target).getDefaultPipe().loadFake()) {
                event.setCanceled(true);
                target.setDead();
            }
        }
    }

    @SubscribeEvent
    public void onMobAttack(LivingHurtEvent event) {
        if (event.getEntityLiving().world.isRemote) return;

        if (event.getEntityLiving() instanceof EntityPlayer) {
            Entity target = event.getSource().getTrueSource();
            if (target instanceof IMob) {
                if (MouseProjectNBT.get(target).getDefaultPipe().loadFake()) {
                    event.setCanceled(true);
                    target.setDead();
                    event.setAmount(-1f);
                }
            }
        }
    }
}