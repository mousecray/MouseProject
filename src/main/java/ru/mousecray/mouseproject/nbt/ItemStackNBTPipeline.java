package ru.mousecray.mouseproject.nbt;

public class ItemStackNBTPipeline {
    static final  String                                   TAG_BASE_KEY = "Base";
    private final MouseProjectNBT.MouseProjectNBTItemStack container;
    private ItemStackNBTPipeline(MouseProjectNBT.MouseProjectNBTItemStack container) { this.container = container; }
    static ItemStackNBTPipeline get(MouseProjectNBT.MouseProjectNBTItemStack base)   { return new ItemStackNBTPipeline(base); }

    public void saveBase(int value)                                                  { container.getBaseTag().setInteger(TAG_BASE_KEY, value); }
}