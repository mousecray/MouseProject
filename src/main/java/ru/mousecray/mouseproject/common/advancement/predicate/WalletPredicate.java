/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.advancement.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import ru.mousecray.mouseproject.common.economy.wallet.WalletType;

import javax.annotation.Nullable;

public class WalletPredicate {
    public static final WalletPredicate ANY = new WalletPredicate(null);
    private final       WalletType      type;

    public WalletPredicate(WalletType type) { this.type = type; }

    public boolean test(@Nullable WalletType type) {
        return this == ANY || this.type == null || this.type == type;
    }

    public static WalletPredicate deserialize(@Nullable JsonElement element) {
        if (element != null && !element.isJsonNull()) {
            JsonObject jsonobject = JsonUtils.getJsonObject(element, "wallet");

            WalletType actualType = null;
            if (jsonobject.has("type")) {
                int type = JsonUtils.getInt(jsonobject, "type");
                actualType = type == -1 ? null : WalletType.fromID(type);
            }

            if (actualType == null) return ANY;
            return new WalletPredicate(actualType);
        } else return ANY;
    }
}