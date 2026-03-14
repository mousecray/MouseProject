/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.common.economy.wallet;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.common.item.coin.ICoin;
import ru.mousecray.mouseproject.nbt.MouseProjectNBT;
import ru.mousecray.mouseproject.registry.MPPotions;
import ru.mousecray.mouseproject.registry.MPSounds;
import ru.mousecray.mouseproject.utils.MPRandomUtils;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MagicWalletController {
    public static void onPutCoin(World world, EntityLivingBase entity) {
        Random rand = entity.getRNG();
        if (MPRandomUtils.accurateChance(rand, 0.5f)) {
            int effectType = rand.nextInt(28);
            switch (effectType) {
                case 0:
                    entity.heal(4.0F);
                    break;
                case 1:
                    attractItems(entity, world, 5.0);
                    break;
                case 2:
                    attractCoins(entity, world, 5.0);
                    break;
                case 3:
                    if (entity instanceof EntityPlayer)
                        ((EntityPlayer) entity).addExperienceLevel(world.rand.nextInt(6) + 5);
                    break;
                case 4:
                    if (entity instanceof EntityPlayer) repairRandomItem(((EntityPlayer) entity), world);
                    break;
                case 5:
                    if (entity instanceof EntityPlayer) ((EntityPlayer) entity).getFoodStats().addStats(2, 0.2F);
                    break;
                case 6:
                    repelMobs(entity, world, 5.0);
                    break;
                case 7:
                    if (entity instanceof EntityPlayer) applyLegendaryEffect(world, ((EntityPlayer) entity), rand);
                    break;
                case 8:
                    if (entity instanceof EntityPlayer) shuffleHotbar(((EntityPlayer) entity), world);
                    break;
                case 9:
                    if (entity instanceof EntityPlayer) dropPhantomItem(((EntityPlayer) entity), world);
                    break;
                case 10:
                    teleportRandomMonster(entity, world, 10);
                    break;
                case 11:
                    if (entity instanceof EntityPlayer) damageRandomItem(((EntityPlayer) entity), world);
                    break;
                case 12:
                    if (entity instanceof EntityPlayer) ((EntityPlayer) entity).getFoodStats().addStats(-2, 0.0F);
                    break;
                case 13:
                    teleportEntityRandomly(entity, world, 5, 10);
                    break;
                case 14:
                    if (entity instanceof EntityPlayer) sendRandomMessageToPlayer(((EntityPlayer) entity), rand);
                    break;
                case 15:
                    glowNearbyMobs(world, entity, 10);
                    break;
                case 16:
                    highlightRandomBlock(world, entity, 10, 10);
                    break;
                case 17:
                    entity.rotationYaw = rand.nextFloat() * 360.0F;
                    break;
                case 18:
                    spawnFakeMob(world, entity, rand, 10, 10);
                    break;
                case 19:
                    entity.motionY = 0.5;
                    break;
                case 20:
                    if (entity instanceof EntityPlayer) scanNearbyMobs(world, ((EntityPlayer) entity), 10);
                    break;
                case 21:
                    sendRandomMessageToAll(world, entity, rand, 100);
                    break;
                case 22:
                    addRandomPotionEffect(entity, rand);
                    break;
                case 23:
                    spawnRandomParticles(world, entity, rand);
                    break;
                case 24:
                    playRandomSound(world, entity, rand);
                    break;
                case 25:
                    if (entity instanceof EntityPlayer)
                        sendRandomMessageActionSelfToPlayer(((EntityPlayer) entity), rand);
                    break;
                case 26:
                    if (entity instanceof EntityPlayer)
                        sendRandomMessageActionOtherToPlayer(world, ((EntityPlayer) entity), rand);
                    break;
                case 27:
                    sendRandomMessageToAllTwice(world, entity, 100f);
                    break;
            }
        }
    }

    private static void dropPhantomItem(EntityPlayer player, World world) {
        ItemStack stack = player.inventory.getCurrentItem();
        if (stack != ItemStack.EMPTY) {
            EntityItem entityItem = new EntityItem(world, player.posX, player.posY + player.getEyeHeight() - 0.3D, player.posZ, stack);
            entityItem.age = 5700;
            entityItem.setInfinitePickupDelay();
            float speed = 0.3F; // Базовая скорость
            entityItem.motionX = -MathHelper.sin(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * speed;
            entityItem.motionY = -MathHelper.sin(player.rotationPitch * 0.017453292F) * speed + 0.2F;
            entityItem.motionZ = MathHelper.cos(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * speed;
            world.spawnEntity(entityItem);
        }
    }

    private static void attractCoins(EntityLivingBase entity, World world, double radius) {
        for (EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, entity.getEntityBoundingBox().grow(radius))) {
            if (!item.cannotPickup() && item.getItem().getItem() instanceof ICoin) {
                double dx       = entity.posX - item.posX;
                double dy       = entity.posY + 0.5 - item.posY;
                double dz       = entity.posZ - item.posZ;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance > 0.5) {
                    item.motionX += dx / distance * 0.1;
                    item.motionY += dy / distance * 0.1;
                    item.motionZ += dz / distance * 0.1;
                }
            }
        }
    }

    private static void attractItems(EntityLivingBase entity, World world, double radius) {
        for (EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, entity.getEntityBoundingBox().grow(radius))) {
            if (!item.cannotPickup()) {
                double dx       = entity.posX - item.posX;
                double dy       = entity.posY + 0.5 - item.posY;
                double dz       = entity.posZ - item.posZ;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance > 0.5) {
                    item.motionX += dx / distance * 0.1;
                    item.motionY += dy / distance * 0.1;
                    item.motionZ += dz / distance * 0.1;
                }
            }
        }
    }

    private static void shuffleHotbar(EntityPlayer player, World world) {
        NonNullList<ItemStack> hotbar = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < 9; ++i) hotbar.set(i, player.inventory.getStackInSlot(i));
        Collections.shuffle(hotbar, world.rand);
        for (int i = 0; i < 9; ++i) player.inventory.setInventorySlotContents(i, hotbar.get(i));
        world.playSound(null, player.getPosition(), MPSounds.WALLET_SHUFFLE_HOTBAR, SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.inventory.markDirty();
    }

    private static void repelMobs(EntityLivingBase entity, World world, double radius) {
        for (EntityLivingBase mob : world.getEntitiesWithinAABB(EntityLivingBase.class, entity.getEntityBoundingBox().grow(radius))) {
            if (mob != entity) {
                double dx       = mob.posX - entity.posX;
                double dz       = mob.posZ - entity.posZ;
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > 0.1) {
                    mob.motionX += dx / distance * 0.5;
                    mob.motionZ += dz / distance * 0.5;
                }
            }
        }
    }

    private static void highlightRandomBlock(World world, EntityLivingBase entity, int radius, int attempt) {
        double                   x           = entity.posX + (world.rand.nextDouble() - 0.5) * 2 * radius;
        double                   z           = entity.posZ + (world.rand.nextDouble() - 0.5) * 2 * radius;
        double                   y           = entity.posY + (world.rand.nextDouble() - 0.5) * 2 * radius;
        BlockPos.MutableBlockPos targetPos   = new BlockPos.MutableBlockPos((int) x, (int) y, (int) z);
        int                      currAttempt = 0;
        while (currAttempt <= attempt) {
            if (world.isAirBlock(targetPos)) {
                BlockPos pos   = entity.getPosition();
                Vec3d    start = new Vec3d(pos.getX() + 0.5, pos.getY() + entity.getEyeHeight(), pos.getZ() + 0.5);
                Vec3d    end   = new Vec3d(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
                if (world.rayTraceBlocks(start, end, false, true, false) == null) {
                    world.spawnParticle(EnumParticleTypes.FLAME, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 0.1, 0.1, 0.1, 10);
                    world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return;
                }
            } else {
                x = entity.posX + (world.rand.nextDouble() - 0.5) * 2 * radius;
                y = entity.posZ + (world.rand.nextDouble() - 0.5) * 2 * radius;
                z = entity.posZ + (world.rand.nextDouble() - 0.5) * 2 * radius;
                targetPos.add(x, y, z);
                currAttempt++;
            }
        }

    }

    private static void teleportRandomMonster(EntityLivingBase entity, World world, int radius) {
        for (EntityLivingBase mob : world.getEntitiesWithinAABB(EntityLivingBase.class, entity.getEntityBoundingBox().grow(radius))) {
            if (mob != entity && mob instanceof IMob) {
                BlockPos pos = entity.getPosition();
                mob.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
            }
        }
    }

    private static void teleportEntityRandomly(EntityLivingBase entity, World world, int radius, int attempt) {
        BlockPos targetPos = findRandomSafePosition(world, entity, radius, attempt);
        if (!entity.getPosition().equals(targetPos)) {
            entity.setPositionAndUpdate(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        }
    }

    private static void scanNearbyMobs(World world, EntityPlayer player, int radius) {
        int entityCount = 0;
        for (EntityLivingBase mob : world.getEntitiesWithinAABB(EntityLivingBase.class, player.getEntityBoundingBox().grow(radius))) {
            if (mob != player) entityCount++;
        }
        player.sendMessage(new TextComponentTranslation("message." + Tags.MOD_ID + ".wallet.nearby_mobs_count", entityCount));
    }

    private static void repairRandomItem(EntityPlayer player, World world) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.isItemDamaged()) {
                stack.setItemDamage(Math.max(0, stack.getItemDamage() - stack.getMaxDamage() / 10));
                world.playSound(null, player.getPosition(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                player.inventory.markDirty();
                break;
            }
        }
    }

    private static void damageRandomItem(EntityPlayer player, World world) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.isItemDamaged()) {
                stack.setItemDamage(stack.getItemDamage() + stack.getMaxDamage() / 10);
                world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
                player.inventory.markDirty();
                break;
            }
        }
    }

    private static void glowNearbyMobs(World world, EntityLivingBase entity, int radius) {
        for (EntityLivingBase mob : world.getEntitiesWithinAABB(EntityLivingBase.class, entity.getEntityBoundingBox().grow(radius))) {
            if (mob != entity) mob.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 200, 0));
        }
    }

    private static BlockPos findRandomSafePosition(World world, EntityLivingBase entity, int maxDistance, int maxAttempt) {
        double                   x           = entity.posX + (world.rand.nextDouble() - 0.5) * 2 * maxDistance;
        double                   z           = entity.posZ + (world.rand.nextDouble() - 0.5) * 2 * maxDistance;
        double                   y           = entity.posY + (world.rand.nextDouble() - 0.5) * 2 * maxDistance;
        BlockPos.MutableBlockPos targetPos   = new BlockPos.MutableBlockPos((int) x, (int) y, (int) z);
        int                      currAttempt = 0;
        boolean                  notFound    = true;
        while (currAttempt <= maxAttempt) {
            if (!world.isAirBlock(targetPos) || !world.isAirBlock(targetPos.up()) || !world.isAirBlock(targetPos.up(1)) || world.isAirBlock(targetPos.down())) {
                x = entity.posX + (world.rand.nextDouble() - 0.5) * 2 * maxDistance;
                y = entity.posZ + (world.rand.nextDouble() - 0.5) * 2 * maxDistance;
                z = entity.posZ + (world.rand.nextDouble() - 0.5) * 2 * maxDistance;
                targetPos.add(x, y, z);
                currAttempt++;
            } else {
                notFound = false;
                break;
            }
        }

        return notFound ? entity.getPosition() : targetPos.toImmutable();
    }

    private static void spawnFakeMob(World world, EntityLivingBase entity, Random rand, int radius, int attempt) {
        int mobType = rand.nextInt(3);
        switch (mobType) {
            case 0: {
                EntityZombie fakeMob   = new EntityZombie(world);
                BlockPos     targetPos = findRandomSafePosition(world, entity, radius, attempt);
                fakeMob.setPosition(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                fakeMob.setEntityInvulnerable(true);
                MouseProjectNBT.get(fakeMob).getDefaultPipe().saveFake();
                world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.0F, 1.0F);
                world.spawnEntity(fakeMob);
                break;
            }
            case 1: {
                EntitySkeleton fakeMob   = new EntitySkeleton(world);
                BlockPos       targetPos = findRandomSafePosition(world, entity, 10, 10);
                fakeMob.setPosition(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                MouseProjectNBT.get(fakeMob).getDefaultPipe().saveFake();
                world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.0F, 1.0F);
                world.spawnEntity(fakeMob);
                break;
            }
            case 2: {
                EntityEnderman fakeMob   = new EntityEnderman(world);
                BlockPos       targetPos = findRandomSafePosition(world, entity, 10, 10);
                fakeMob.setPosition(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                MouseProjectNBT.get(fakeMob).getDefaultPipe().saveFake();
                world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.0F, 1.0F);
                world.spawnEntity(fakeMob);
                break;
            }
        }
    }

    private static void addRandomPotionEffect(EntityLivingBase entity, Random rand) {
        int effectType = rand.nextInt(18);
        switch (effectType) {
            case 0:
                entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, 400 + MPRandomUtils.normalPercentFrom(rand, 400, 24, 1), 0));
                break;
            case 1:
                entity.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 200 + MPRandomUtils.normalPercentFrom(rand, 200, 24, 1), 0));
                break;
            case 2:
                entity.addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 100 + MPRandomUtils.normalPercentFrom(rand, 100, 24, 1), 0));
                break;
            case 3:
                entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200 + MPRandomUtils.normalPercentFrom(rand, 200, 24, 1), 0));
                break;
            case 4:
                entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 200 + MPRandomUtils.normalPercentFrom(rand, 200, 24, 1), 0));
                break;
            case 5:
                entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 300 + MPRandomUtils.normalPercentFrom(rand, 300, 24, 1), 0));
                break;
            case 6:
                entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 300 + MPRandomUtils.normalPercentFrom(rand, 300, 24, 1), 0));
                break;
            case 7:
                entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 300 + MPRandomUtils.normalPercentFrom(rand, 300, 24, 1), 0));
                break;
            case 8:
                entity.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 400 + MPRandomUtils.normalPercentFrom(rand, 400, 24, 1), 0));
                break;
            case 9:
                entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200 + MPRandomUtils.normalPercentFrom(rand, 200, 24, 1), 0));
                break;
            case 10:
                entity.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 400 + MPRandomUtils.normalPercentFrom(rand, 400, 24, 1), 0));
                break;
            case 11:
                entity.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 400 + MPRandomUtils.normalPercentFrom(rand, 400, 24, 1), 0));
                break;
            case 12:
                entity.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 300 + MPRandomUtils.normalPercentFrom(rand, 300, 24, 1), 0));
                break;
            case 13:
                entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 200 + MPRandomUtils.normalPercentFrom(rand, 200, 24, 1), 0));
                break;
            case 14:
                entity.addPotionEffect(new PotionEffect(MobEffects.HASTE, 300 + MPRandomUtils.normalPercentFrom(rand, 300, 24, 1), 0));
                break;
            case 15:
                entity.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 300 + MPRandomUtils.normalPercentFrom(rand, 300, 24, 1), 0));
                break;
            case 16:
                entity.addPotionEffect(new PotionEffect(MobEffects.LUCK, 600 + MPRandomUtils.normalPercentFrom(rand, 600, 24, 1), 0));
                break;
            case 17:
                entity.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 300 + MPRandomUtils.normalPercentFrom(rand, 300, 24, 1), 0));
                break;
        }
    }

    private static void spawnRandomParticles(World world, EntityLivingBase entity, Random rand) {
        int particleType = rand.nextInt(3);
        switch (particleType) {
            case 0:
                world.spawnParticle(EnumParticleTypes.PORTAL, entity.posX, entity.posY + 1, entity.posZ, 0.1, 0.1, 0.1, 10);
                break;
            case 1:
                world.spawnParticle(EnumParticleTypes.FLAME, entity.posX, entity.posY + 1, entity.posZ, 0.1, 0.1, 0.1, 10);
                break;
            case 2:
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, entity.posX, entity.posY + 1, entity.posZ, 0.1, 0.1, 0.1, 10);
                break;
        }
    }

    private static void playRandomSound(World world, EntityLivingBase entity, Random rand) {
        int soundType = rand.nextInt(7);
        switch (soundType) {
            case 0:
                world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_LIGHTNING_IMPACT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                break;
            case 1:
                world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_CREEPER_PRIMED, SoundCategory.PLAYERS, 1.0F, 1.0F);
                break;
            case 2:
                world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_GHAST_SCREAM, SoundCategory.PLAYERS, 1.0F, 1.0F);
                break;
            case 3:
                world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_ENDERMEN_SCREAM, SoundCategory.PLAYERS, 1.0F, 1.0F);
                break;
            case 4:
                world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                break;
            case 5:
                world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_WOODEN_DOOR_OPEN, SoundCategory.PLAYERS, 1.0F, 1.0F);
                break;
            case 6:
                world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                break;
        }
    }

    private static void sendRandomMessageToPlayer(EntityPlayer player, Random rand) {
        int messageType = rand.nextInt(26) + 1;
        player.sendMessage(new TextComponentTranslation("message." + Tags.MOD_ID + ".wallet.whisper." + messageType));
    }

    private static void sendRandomMessageActionSelfToPlayer(EntityPlayer player, Random rand) {
        int messageType = rand.nextInt(3) + 1;
        player.sendMessage(new TextComponentTranslation("message." + Tags.MOD_ID + ".wallet.whisper_action_self." + messageType, player.getDisplayName()));
    }

    private static void sendRandomMessageActionOtherToPlayer(World world, EntityPlayer player, Random rand) {
        int                messageType = rand.nextInt(5) + 1;
        List<EntityPlayer> list        = world.getPlayers(EntityPlayer.class, p -> true);
        ITextComponent     target      = list.get(rand.nextInt(list.size())).getDisplayName();
        player.sendMessage(new TextComponentTranslation("message." + Tags.MOD_ID + ".wallet.whisper_action_other." + messageType, target));
    }

    private static void sendRandomMessageToAll(World world, EntityLivingBase entity, Random rand, float radius) {
        int                messageType = rand.nextInt(29) + 1;
        List<EntityPlayer> list        = world.getPlayers(EntityPlayer.class, p -> p.getDistanceSq(entity.posX, entity.posY, entity.posZ) <= radius * radius);
        for (EntityPlayer player : list) {
            if (player != entity) {
                player.sendMessage(new TextComponentTranslation("message." + Tags.MOD_ID + ".wallet.whisper_broadcast." + messageType, entity.getDisplayName()));
            }
        }
    }

    private static void sendRandomMessageToAllTwice(World world, EntityLivingBase entity, float radius) {
        List<EntityPlayer> list = world.getPlayers(EntityPlayer.class, p -> p.getDistanceSq(entity.posX, entity.posY, entity.posZ) <= radius * radius);
        for (EntityPlayer player : list) {
            if (player != entity) {
                player.sendMessage(new TextComponentTranslation("message." + Tags.MOD_ID + ".wallet.whisper_broadcast.30", entity.getDisplayName(), entity.getDisplayName()));
            }
        }
    }

    private static void applyLegendaryEffect(World world, EntityPlayer player, Random rand) {
        int effectType = rand.nextInt(5);
        int amplifier  = 0;
        switch (effectType) {
            case 0:
                amplifier = MPRandomUtils.normalChance(rand, 50) ? MPRandomUtils.normalChance(rand, 50) ? 2 : 1 : 0;
                player.addPotionEffect(new PotionEffect(MPPotions.DOUBLE_CRAFT, 240 + MPRandomUtils.normalPercentFrom(rand, 300, 24, 1), amplifier));
                break;
            case 1:
                amplifier = MPRandomUtils.normalChance(rand, 50) ? MPRandomUtils.normalChance(rand, 50) ? 2 : 1 : 0;
                player.addPotionEffect(new PotionEffect(MPPotions.DOUBLE_FISHING, 240 + MPRandomUtils.normalPercentFrom(rand, 300, 24, 1), amplifier));
                break;
            case 2:
                amplifier = MPRandomUtils.normalChance(rand, 50) ? MPRandomUtils.normalChance(rand, 50) ? 2 : 1 : 0;
                player.addPotionEffect(new PotionEffect(MPPotions.DOUBLE_MYTHIC, 240 + MPRandomUtils.normalPercentFrom(rand, 300, 24, 1), amplifier));
                break;
            case 3:
                amplifier = MPRandomUtils.normalChance(rand, 50) ? MPRandomUtils.normalChance(rand, 50) ? 2 : 1 : 0;
                player.addPotionEffect(new PotionEffect(MPPotions.DOUBLE_FARM_HARVEST, 240 + MPRandomUtils.normalPercentFrom(rand, 300, 24, 1), amplifier));
                break;
            case 4:
                player.addPotionEffect(new PotionEffect(MPPotions.IMMORTALITY, 240 + MPRandomUtils.normalPercentFrom(rand, 300, 24, 1), 0));
                break;
        }
    }
}