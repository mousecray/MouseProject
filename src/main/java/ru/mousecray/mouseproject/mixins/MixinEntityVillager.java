package ru.mousecray.mouseproject.mixins;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityVillager.class)
public abstract class MixinEntityVillager extends EntityLiving {
    public MixinEntityVillager(World world) {
        super(world);
    }

    @SuppressWarnings({ "UnresolvedMixinReference", "DataFlowIssue", "UnreachableCode" })
    @Inject(method = "initEntityAI()V", at = @At("TAIL"))
    private void injectCustomAI(CallbackInfo ci) {
        EntityVillager villager = (EntityVillager) (Object) this;
//        tasks.addTask(8, new POIVillagerAI(villager));
    }

    @SuppressWarnings({ "UnresolvedMixinReference", "DataFlowIssue", "UnreachableCode" })
    @Inject(method = "useRecipe(Lnet/minecraft/village/MerchantRecipe;)V", at = @At("HEAD"), cancellable = true)
    private void injectUseRecipe(MerchantRecipe recipe, CallbackInfo ci) {
        EntityVillager villager = (EntityVillager) (Object) this;
        EntityPlayer   player   = villager.getCustomer();

        if (player != null && !villager.world.isRemote) {
//            recipe.
        }
    }
}