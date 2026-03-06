package ru.mousecray.mouseproject.nbt;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.mousecray.mouseproject.Tags;

import java.lang.ref.WeakReference;
import java.util.function.Function;

public abstract class MouseProjectNBT {
    static final String TAG_CONTAINER_KEY        = Tags.MOD_ID;
    static final String TAG_CONTAINER_NAME_KEY   = "ContainerName";
    static final String TAG_CONTAINER_NAME_VALUE = Tags.MOD_ID;

    protected NBTTagCompound baseTag, modTag;

    protected static Function<Entity, NBTTagCompound> entityToBase = Entity::getEntityData;

    protected static Function<ItemStack, NBTTagCompound> stackToBase = stack -> {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }
        return nbt;
    };

    protected static Function<NBTTagCompound, NBTTagCompound> baseToMod = nbt -> {
        NBTTagCompound mod = nbt.getCompoundTag(TAG_CONTAINER_KEY);
        if (!mod.hasKey(TAG_CONTAINER_NAME_KEY, 8) || !mod.getString(TAG_CONTAINER_NAME_KEY).equals(TAG_CONTAINER_NAME_VALUE)) {
            mod = new NBTTagCompound();
            mod.setString(TAG_CONTAINER_NAME_KEY, TAG_CONTAINER_NAME_VALUE);
            nbt.setTag(TAG_CONTAINER_KEY, mod);
        }
        return mod;
    };

    public static MouseProjectNBTEntity get(Entity entity)      { return new MouseProjectNBTEntity(entity); }
    public static MouseProjectNBTItemStack get(ItemStack stack) { return new MouseProjectNBTItemStack(stack); }

    protected NBTTagCompound getModTag()                        { return baseToMod.apply(getBaseTag()); }

    protected abstract NBTTagCompound getBaseTag();
    protected abstract boolean hasBaseTag();
    protected abstract void removeBaseTag();

    protected boolean hasModTag() {
        if (!hasBaseTag()) return false;
        NBTTagCompound base = getBaseTag();
        if (!base.hasKey(TAG_CONTAINER_KEY, 10)) return false;
        NBTTagCompound tag = base.getCompoundTag(TAG_CONTAINER_KEY);
        return tag.hasKey(TAG_CONTAINER_NAME_KEY, 8) && tag.getString(TAG_CONTAINER_NAME_KEY).equals(TAG_CONTAINER_NAME_VALUE);
    }

    public void removeModTag() {
        if (hasModTag()) {
            NBTTagCompound base = getBaseTag();
            base.removeTag(TAG_CONTAINER_KEY);
        }
    }

    public void removeAllTag() {
        if (hasModTag()) removeModTag();
        if (hasBaseTag()) removeBaseTag();
    }

    public void removeBaseTagIfEmpty() {
        if (hasBaseTag()) {
            NBTTagCompound base = getBaseTag();
            if (base.isEmpty()) removeBaseTag();
        }
    }

    public void removeModTagIfEmpty() {
        if (hasModTag()) {
            NBTTagCompound mod = getModTag();
            if (mod.getSize() <= 1) removeModTag();
        }
    }

    public void removeAllTagIfEmpty() {
        removeModTagIfEmpty();
        removeBaseTagIfEmpty();
    }

    public static NBTTagCompound getBaseTag(ItemStack stack) { return stackToBase.apply(stack); }
    public static NBTTagCompound getModTag(ItemStack stack)  { return baseToMod.apply(getBaseTag(stack)); }

    public static NBTTagCompound getBaseTag(Entity entity)   { return entityToBase.apply(entity); }
    public static NBTTagCompound getModTag(Entity entity)    { return baseToMod.apply(getBaseTag(entity)); }

    public static class MouseProjectNBTEntity extends MouseProjectNBT {
        private final WeakReference<Entity> entity;
        private MouseProjectNBTEntity(Entity entity)       { this.entity = new WeakReference<>(entity); }
        public EntityVillagerNBTPipeline getVillagerPipe() { return EntityVillagerNBTPipeline.get(this); }
        public EntityNBTPipeline getDefaultPipe()          { return EntityNBTPipeline.get(this); }

        @Override
        protected NBTTagCompound getBaseTag() {
            Entity e = entity.get();
            return e != null ? entityToBase.apply(e) : new NBTTagCompound();
        }

        @Override
        protected boolean hasBaseTag() {
            Entity e = entity.get();
            return e != null && !e.getEntityData().isEmpty();
        }

        @Override
        protected void removeBaseTag() {
            Entity e = entity.get();
            if (e != null) e.getEntityData().getKeySet().clear();
        }
    }

    public static class MouseProjectNBTItemStack extends MouseProjectNBT {
        private final WeakReference<ItemStack> stack;
        private MouseProjectNBTItemStack(ItemStack stack) { this.stack = new WeakReference<>(stack); }
        public ItemStackNBTPipeline getDefaultPipe()      { return ItemStackNBTPipeline.get(this); }
        public ItemStackCoinNBTPipeline getCoinPipe()     { return ItemStackCoinNBTPipeline.get(this); }
        public ItemStackWalletNBTPipeline getWalletPipe() { return ItemStackWalletNBTPipeline.get(this); }

        @Override
        protected NBTTagCompound getBaseTag() {
            ItemStack s = stack.get();
            return s != null ? stackToBase.apply(s) : new NBTTagCompound();
        }

        @Override
        protected boolean hasBaseTag() {
            ItemStack s = stack.get();
            if (s == null || !s.hasTagCompound()) return false;
            assert s.getTagCompound() != null;
            return !s.getTagCompound().isEmpty();
        }

        @Override
        protected void removeBaseTag() {
            ItemStack s = stack.get();
            if (s != null) s.setTagCompound(null);
        }
    }
}