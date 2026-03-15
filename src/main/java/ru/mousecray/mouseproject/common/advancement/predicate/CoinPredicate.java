/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.advancement.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import ru.mousecray.mouseproject.common.economy.CoinValue;
import ru.mousecray.mouseproject.common.economy.coin.CoinType;
import ru.mousecray.mouseproject.utils.MPJsonUtils;

import javax.annotation.Nullable;

public class CoinPredicate {
    public static final CoinPredicate ANY = new CoinPredicate(CoinValue.create(-1, null));
    private final       CoinValue     value;

    public CoinPredicate(CoinValue value) { this.value = value; }

    public boolean test(@Nullable CoinValue value) {
        if (this == ANY || this.value == null) return true;
        else {
            boolean hasTypeCriteria  = this.value.getType() != null;
            boolean hasValueCriteria = this.value.getValue() >= 0;
            if (hasTypeCriteria && hasValueCriteria) {
                return value != null && this.value.getType() == value.getType() && this.value.getValue() == value.getValue();
            } else if (hasTypeCriteria) return value != null && this.value.getType() == value.getType();
            else if (hasValueCriteria) return value != null && this.value.getValue() == value.getValue();
            else return true;
        }
    }

    public static CoinPredicate deserialize(@Nullable JsonElement element) {
        if (element != null && !element.isJsonNull()) {
            JsonObject jsonobject = JsonUtils.getJsonObject(element, "coin.json");

            CoinType actualType = null;
            long     value      = -1;
            if (jsonobject.has("type")) {
                int type = JsonUtils.getInt(jsonobject, "type");
                actualType = type == -1 ? null : CoinType.fromID(type);
            }

            if (jsonobject.has("value")) {
                value = MPJsonUtils.getLong(jsonobject, "value");
            }

            if (actualType == null && value <= 0) return ANY;
            return new CoinPredicate(CoinValue.create(value, actualType));
        } else return ANY;
    }
}