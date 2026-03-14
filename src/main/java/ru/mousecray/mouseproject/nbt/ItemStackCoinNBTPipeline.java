/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.nbt;

public class ItemStackCoinNBTPipeline {
    static final  String                                   TAG_IS_NEW_KEY = "IsNew";
    private final MouseProjectNBT.MouseProjectNBTItemStack container;
    private ItemStackCoinNBTPipeline(MouseProjectNBT.MouseProjectNBTItemStack container) { this.container = container; }
    static ItemStackCoinNBTPipeline get(MouseProjectNBT.MouseProjectNBTItemStack base)   { return new ItemStackCoinNBTPipeline(base); }

    public void removeIsNew() {
        if (container.hasModTag()) container.getModTag().removeTag(TAG_IS_NEW_KEY);
        container.removeAllTagIfEmpty();
    }

    public void setIsNew() { container.getModTag().setBoolean(TAG_IS_NEW_KEY, true); }

    public boolean loadIsNew() {
        return container.hasModTag() && container.getModTag().hasKey(TAG_IS_NEW_KEY) && container.getModTag().getBoolean(TAG_IS_NEW_KEY);
    }
}