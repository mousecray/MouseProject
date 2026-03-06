package ru.mousecray.mouseproject.registry;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import ru.mousecray.mouseproject.common.advancement.trigger.TriggerFullWallet;
import ru.mousecray.mouseproject.common.advancement.trigger.TriggerGetCoin;

public class MPTriggers {
    public static final MPTriggers INSTANCE = new MPTriggers();

    public static final TriggerGetCoin    GET_COIN    = new TriggerGetCoin();
    public static final TriggerFullWallet FULL_WALLET = new TriggerFullWallet();

    public void register() {
        registerCriteria(GET_COIN);
        registerCriteria(FULL_WALLET);
    }

    private void registerCriteria(ICriterionTrigger<? extends AbstractCriterionInstance> criterionTrigger) {
        CriteriaTriggers.register(criterionTrigger);
    }
}