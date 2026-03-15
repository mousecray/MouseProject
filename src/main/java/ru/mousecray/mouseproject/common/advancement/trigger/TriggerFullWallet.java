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
import ru.mousecray.mouseproject.common.advancement.predicate.WalletPredicate;
import ru.mousecray.mouseproject.common.economy.wallet.WalletType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TriggerFullWallet implements ICriterionTrigger<TriggerFullWallet.Instance> {
    private static final ResourceLocation                   ID        = new ResourceLocation("${mod_id}", "full_wallet");
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
        return new Instance(getId(), WalletPredicate.deserialize(json.get("wallet")));
    }

    public void trigger(EntityPlayerMP player, WalletType type) {
        Listeners listeners = this.listeners.get(player.getAdvancements());
        if (listeners != null) listeners.trigger(type);
    }

    public static class Instance extends AbstractCriterionInstance {
        private final WalletPredicate wallet;

        public Instance(ResourceLocation criterion, WalletPredicate wallet) {
            super(criterion);
            this.wallet = wallet;
        }

        public boolean test(WalletType wallet) { return this.wallet.test(wallet); }
    }

    public static class Listeners {
        private final PlayerAdvancements      advancements;
        private final Set<Listener<Instance>> listeners = Sets.newHashSet();

        public Listeners(PlayerAdvancements advancements) { this.advancements = advancements; }

        public boolean isEmpty()                          { return listeners.isEmpty(); }
        public void add(Listener<Instance> listener)      { listeners.add(listener); }
        public void remove(Listener<Instance> listener)   { listeners.remove(listener); }

        public void trigger(WalletType value) {
            List<Listener<Instance>> list = null;

            for (Listener<Instance> listener : listeners) {
                if (listener.getCriterionInstance().test(value)) {
                    if (list == null) list = Lists.newArrayList();
                    list.add(listener);
                }
            }

            if (list != null) for (Listener<Instance> listener1 : list) listener1.grantCriterion(advancements);
        }
    }
}
