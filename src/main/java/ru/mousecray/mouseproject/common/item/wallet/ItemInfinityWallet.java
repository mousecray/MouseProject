package ru.mousecray.mouseproject.common.item.wallet;

import mcp.MethodsReturnNonnullByDefault;
import ru.mousecray.mouseproject.common.economy.capacity.NormalWalletCapacity;
import ru.mousecray.mouseproject.common.economy.capacity.ResourceWalletCapacity;
import ru.mousecray.mouseproject.common.economy.capacity.SpecificWalletCapacity;
import ru.mousecray.mouseproject.common.economy.wallet.WalletType;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemInfinityWallet extends ItemWallet {
    public ItemInfinityWallet(WalletType type) {
        super(type, null, null,
                NormalWalletCapacity.createInfinite(),
                SpecificWalletCapacity.createInfinite(),
                ResourceWalletCapacity.createInfinite());
    }
}