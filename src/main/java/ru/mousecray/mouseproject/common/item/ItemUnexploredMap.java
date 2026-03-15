/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public class ItemUnexploredMap extends MPDefaultItem {
    private final boolean treasure;

    public ItemUnexploredMap(String name, boolean treasure) {
        super(name);
        this.treasure = treasure;
        setMaxStackSize(1);
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
            player.getCooldownTracker().setCooldown(this, 20);
        }

        if (!world.isRemote) {
            ItemStack map;
            if (treasure) map = createTreasureMap(world, player.getPosition());
            else {
                map = null;
                switch (itemRand.nextInt(4)) {
                    case 0:
                        map = createMap(world, player.getPosition(), MapDecoration.Type.MANSION, "Mansion");
                        break;
                    case 1:
                        map = createMap(world, player.getPosition(), MapDecoration.Type.MONUMENT, "Monument");
                        break;
                    case 2:
                        map = createMap(world, player.getPosition(), MapDecoration.Type.TARGET_POINT, "Mineshaft");
                        break;
                    case 3:
                        map = createMap(world, player.getPosition(), MapDecoration.Type.TARGET_POINT, "Temple");
                        break;
                }

            }
            if (map != null) {
                world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                if (!player.addItemStackToInventory(map)) {
                    InventoryHelper.spawnItemStack(world, player.posX, player.posY, player.posZ, map);
                }
            } else {
                world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_VILLAGER_HURT, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            }
        }


        player.addStat(StatList.getObjectUseStats(this));

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Nullable
    private ItemStack createMap(World world, BlockPos currentPos, MapDecoration.Type decor, String name) {
        BlockPos pos = world.findNearestStructure(name, currentPos, true);
        if (pos == null) return null;

        ItemStack stack = ItemMap.setupNewMap(world, pos.getX(), pos.getZ(), (byte) 2, true, true);
        ItemMap.renderBiomePreviewMap(world, stack);
        MapData.addTargetDecoration(stack, pos, "+", decor);
        stack.setTranslatableName("filled_map." + name.toLowerCase(Locale.ROOT));
        return stack;
    }

    @Nullable
    private ItemStack createTreasureMap(World world, BlockPos currentPos) {
        for (int i = 0; i < 3; ++i) {
            int
                    posX = 10 - itemRand.nextInt(21),
                    posY = 10 - itemRand.nextInt(21),
                    posZ = 10 - itemRand.nextInt(21);
            int absX = Math.abs(posX), absY = Math.abs(posY), absZ = Math.abs(posZ);
            if (absX < 10 && absZ < 10 && absY < 10) {
                int i1 = itemRand.nextInt(3);
                if (i1 == 0) {
                    if (posX < 0) posX -= 10 - absX;
                    else posX += 10 - absX;
                } else if (i1 == 1) {
                    if (posY < 0) posY -= 10 - absY;
                    else posY += 10 - absY;
                } else {
                    if (posZ < 0) posZ -= 10 - absZ;
                    else posZ += 10 - absZ;
                }
            }

            BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(currentPos);
            mPos.add(posX, posY, posZ);

            for (int x = 0; x <= 5; ++x) {
                for (int y = 0; y <= 5; ++y) {
                    for (int z = 0; z <= 5; ++z) {
                        mPos.add(x, y, z);
                        BlockPos.MutableBlockPos mPos2 = new BlockPos.MutableBlockPos(mPos);
                        Block                    block = world.getBlockState(mPos).getBlock();
                        if (block == Blocks.DIRT || block == Blocks.STONE || block == Blocks.SAND || block == Blocks.GRAVEL) {
                            if (
                                    !world.isAirBlock(mPos2.setPos(mPos.getX(), mPos.getY() + 1, mPos.getZ()))
                                            && !world.isAirBlock(mPos2.setPos(mPos.getX(), mPos.getY() - 1, mPos.getZ()))
                                            && !world.isAirBlock(mPos2.setPos(mPos.getX() + 1, mPos.getY(), mPos.getZ()))
                                            && !world.isAirBlock(mPos2.setPos(mPos.getX() - 1, mPos.getY(), mPos.getZ()))
                                            && !world.isAirBlock(mPos2.setPos(mPos.getX(), mPos.getY(), mPos.getZ() + 1))
                                            && !world.isAirBlock(mPos2.setPos(mPos.getX(), mPos.getY(), mPos.getZ() - 1))
                            ) {
                                ItemStack stack = ItemMap.setupNewMap(world, mPos.getX(), mPos.getZ(), (byte) 2, true, true);
                                ItemMap.renderBiomePreviewMap(world, stack);
                                MapData.addTargetDecoration(stack, mPos, "+", MapDecoration.Type.TARGET_POINT);
                                stack.setTranslatableName("filled_map." + "treasure");
                                world.setBlockState(mPos, Blocks.CHEST.getDefaultState(), 2);
                                TileEntity tile = world.getTileEntity(mPos);
                                if (tile instanceof TileEntityChest) {
                                    ((TileEntityChest) tile).setLootTable(
                                            new ResourceLocation("minecraft", "minecraft:chests/village_blacksmith"),
                                            world.rand.nextLong());
                                }
                                return stack;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
}