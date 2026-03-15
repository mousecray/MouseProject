/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.ai;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.common.values.DayValue;
import ru.mousecray.mouseproject.common.values.Poi;
import ru.mousecray.mouseproject.common.values.TradeTable;
import ru.mousecray.mouseproject.nbt.EntityVillagerNBTPipeline;
import ru.mousecray.mouseproject.nbt.MouseProjectNBT;
import ru.mousecray.mouseproject.registry.MPVillagerRegistry;

import javax.annotation.Nullable;

public class POIVillagerAI extends EntityAIBase {
    private final EntityVillager            villager;
    private final EntityVillagerNBTPipeline tag;

    public POIVillagerAI(EntityVillager villager) {
        this.villager = villager;
        setMutexBits(3);
        tag = MouseProjectNBT.get(villager).getVillagerPipe();
    }


    @Override
    public void startExecuting() {
        tag.savePoiFollow(true);
        World world   = villager.getEntityWorld();
        int   visited = tag.loadPoiVisitedPerDay();
        if (visited < 0) visited = 0;
        DayValue day     = tag.loadDayValue();
        int      newDay  = (int) (world.getWorldTime() / 24000);
        int      newTime = (int) (world.getWorldTime() % 24000);
        if (day == null) day = new DayValue(newDay, newTime);

        boolean flagDay  = newDay - day.getDay() >= 1;
        boolean flagTime = Math.abs(newTime - day.getValue()) >= 5000;

        if (flagDay && flagTime && visited <= 2) {
            day.setDay(newDay);
            day.setValue(newTime);
            Poi poi = tag.loadPoi();
            if (poi != null && poi.isPOIExist(villager.getEntityWorld())) {
                tag.savePoiVisitedPerDay(visited + 1);
                BlockPos pos = poi.getPos();
                villager.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 0.6D);
            } else {
                poi = createPOI(world, villager.getPosition(), 10);
                if (poi != null) {
                    if (tag.savePoi(poi)) {
                        VillagerRegistry.VillagerProfession prof = poi.getProfession();
                        if (prof != null) {
                            villager.buyingList = null;
                            villager.careerLevel = 0;
                            villager.populateBuyingList();
                            tag.saveTradeTable(new TradeTable());
                            villager.setProfession(prof);
                            tag.savePoiVisitedPerDay(visited + 1);
                            BlockPos pos = poi.getPos();
                            villager.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 0.6D);
                        }
                    } else {
                        MouseProject.LOGGER.error("Cannot save POI for villager at {}", villager.getPosition());
                    }
                } else {
                    MouseProject.LOGGER.debug("No POI found near villager at {}", villager.getPosition());
                }
            }
        } else {
            tag.savePoiVisitedPerDay(0);
            tag.savePoiFollow(false);
        }
        tag.saveDayValue(day);
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (tag.loadPoiFollow()) {
            if (!villager.getNavigator().noPath()) return true;
            else {
                tag.savePoiFollow(false);
                Poi poi = tag.loadPoi();
                if (poi != null && villager.getDistanceSq(poi.getPos()) < 4.0) {
                    VillagerRegistry.VillagerProfession prof = poi.getProfession();
                    if (prof != null) {
                        ResourceLocation name = prof.getRegistryName();
                        if (name != null && name.getNamespace().equals(Tags.MOD_ID)) {
//                            for (int i = 0; i < villager.buyingList.size())
//                                    VillagerRegistry.VillagerCareer career = prof.getCareer(0);
//                            if (career != null) {
//                                List<EntityVillager.ITradeList> recipes = career.getTrades(0);
//                                if (recipes != null) buyingList.addAll(recipes);
//                                recipes = career.getTrades(1);
//                                if (recipes != null) buyingList.addAll(recipes);
//                            }
//                                villager.setRecipes(buyingList);
                        }
                    }
                }
                tag.savePoiFollow(false);
            }
        } else {
            if (!villager.getNavigator().noPath()) villager.getNavigator().clearPath();
        }
        return false;
    }

    @Override
    public boolean shouldExecute() {
        return villager.isEntityAlive()
                && !villager.isChild()
                && villager.onGround
                && !villager.velocityChanged;
    }

    @Override public void resetTask() { tag.savePoiFollow(false); }

    @SuppressWarnings("SameParameterValue") @Nullable
    private Poi createPOI(World world, BlockPos position, int scanRadius) {
        BlockPos.MutableBlockPos pos     = new BlockPos.MutableBlockPos(position);
        int                      halfRad = scanRadius / 2;
        for (int y = -1; y < 2; ++y) {
            for (int x = -halfRad; x < halfRad; ++x) {
                for (int z = -halfRad; z < halfRad; ++z) {
                    pos.setPos(position.getX() + x, position.getY() + y, position.getZ() + z);
                    Block                               block = world.getBlockState(pos).getBlock();
                    VillagerRegistry.VillagerProfession prof  = MPVillagerRegistry.getProfessionForBlock(block);
                    if (prof != null) return new Poi(block, position);
                }
            }
        }
        return null;
    }
}