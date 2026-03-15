/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.common.advancement.trigger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import ru.mousecray.mouseproject.common.advancement.predicate.CoinPredicate;
import ru.mousecray.mouseproject.common.economy.CoinValue;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TriggerGetCoin implements ICriterionTrigger<TriggerGetCoin.Instance> {
    private static final ResourceLocation                   ID        = new ResourceLocation("${mod_id}", "get_coin");
    private final        Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

    @Override public ResourceLocation getId() { return ID; }

    @Override
    public void addListener(PlayerAdvancements advancements, Listener<Instance> listener) {
        Listeners listeners = this.listeners.computeIfAbsent(advancements, Listeners::new);
        listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements advancements, Listener<Instance> listener) {
        Listeners listeners = this.listeners.get(advancements);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) this.listeners.remove(advancements);
        }
    }

    @Override public void removeAllListeners(PlayerAdvancements advancements) { listeners.remove(advancements); }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(getId(), CoinPredicate.deserialize(json.get("coin.json")));
    }

    public void trigger(EntityPlayerMP player, CoinValue value) {
        Listeners listeners = this.listeners.get(player.getAdvancements());
        if (listeners != null) listeners.trigger(value);
    }

    public static class Instance extends AbstractCriterionInstance {
        private final CoinPredicate coin;

        public Instance(ResourceLocation criterion, CoinPredicate coin) {
            super(criterion);
            this.coin = coin;
        }

        public boolean test(CoinValue coin) { return this.coin.test(coin); }
    }

    public static class Listeners {
        private final PlayerAdvancements      advancements;
        private final Set<Listener<Instance>> listeners = Sets.newHashSet();

        public Listeners(PlayerAdvancements advancements)                 { this.advancements = advancements; }

        public boolean isEmpty()                                          { return listeners.isEmpty(); }
        public void add(ICriterionTrigger.Listener<Instance> listener)    { listeners.add(listener); }
        public void remove(ICriterionTrigger.Listener<Instance> listener) { listeners.remove(listener); }

        public void trigger(CoinValue value) {
            List<Listener<Instance>> list = null;

            for (ICriterionTrigger.Listener<Instance> listener : listeners) {
                if (listener.getCriterionInstance().test(value)) {
                    if (list == null) list = Lists.newArrayList();
                    list.add(listener);
                }
            }

            if (list != null) for (Listener<Instance> listener1 : list) listener1.grantCriterion(advancements);
        }
    }
}
