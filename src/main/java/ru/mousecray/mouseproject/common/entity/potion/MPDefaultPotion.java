/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.entity.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.nbt.EntityNBTPipeline;
import ru.mousecray.mouseproject.nbt.MouseProjectNBT;

import javax.annotation.Nonnull;

public class MPDefaultPotion extends Potion {
    protected MPDefaultPotion(String name, boolean isBadEffect, int liquidColor) {
        super(isBadEffect, liquidColor);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, name));
        setPotionName("effect." + Tags.MOD_ID + "." + name);
    }

    protected void onAddEffectToEntity(@Nonnull World world, @Nonnull EntityLivingBase entity, PotionEffect effect)      { }
    protected void onRemoveEffectFromEntity(@Nonnull World world, @Nonnull EntityLivingBase entity, PotionEffect effect) { }
    protected void onEffectPerformed(@Nonnull World world, @Nonnull EntityLivingBase entity, PotionEffect effect)        { }

    @Override
    public final void performEffect(@Nonnull EntityLivingBase entity, int amplifier) {
        PotionEffect      effect = entity.getActivePotionEffect(this);
        EntityNBTPipeline pipe   = MouseProjectNBT.get(entity).getDefaultPipe();

        if (!pipe.loadPotionFirstTick(this)) {
            onAddEffectToEntity(entity.world, entity, effect);
            pipe.savePotionFirstTick(this);
        }

        onEffectPerformed(entity.world, entity, effect);

        if (effect == null || effect.getDuration() <= 1) {
            onRemoveEffectFromEntity(entity.world, entity, effect);
            pipe.removePotionFirstTick(this);
        }
    }

    @Override public boolean isReady(int duration, int amplifier)   { return duration > 0; }
    @Override @SideOnly(Side.CLIENT) public boolean hasStatusIcon() { return false; }
}