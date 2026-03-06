package ru.mousecray.mouseproject.common.capability;

import ru.mousecray.mouseproject.common.inventory.MPInventory;

public interface ICapabilityInventory<T extends ICapabilityInventory<T>> {
    void copyInventory(T inventory);
    MPInventory getInventory();
}