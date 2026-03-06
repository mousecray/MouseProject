package ru.mousecray.mouseproject.registry;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import ru.mousecray.mouseproject.MouseProject;
import ru.mousecray.mouseproject.common.economy.capacity.NormalWalletCapacity;
import ru.mousecray.mouseproject.common.economy.capacity.ResourceWalletCapacity;
import ru.mousecray.mouseproject.common.economy.capacity.SpecificWalletCapacity;
import ru.mousecray.mouseproject.common.economy.coin.NormalCoinType;
import ru.mousecray.mouseproject.common.economy.coin.ResourceCoinType;
import ru.mousecray.mouseproject.common.economy.coin.SpecificCoinType;
import ru.mousecray.mouseproject.common.economy.wallet.DefaultWalletType;
import ru.mousecray.mouseproject.common.item.ItemUnexploredMap;
import ru.mousecray.mouseproject.common.item.coin.ItemCoin;
import ru.mousecray.mouseproject.common.item.wallet.*;
import ru.mousecray.mouseproject.registry.constants.ItemNames;
import ru.mousecray.mouseproject.utils.MPReflectionUtils;

@Mod.EventBusSubscriber
public class MPItems {
    public static Item UNEXPLORED_TREASURE_MAP  = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item UNEXPLORED_STRUCTURE_MAP = MPPlugFactory.CREATE_ITEM_PLUG();

    public static Item BRONZE_COIN   = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item SILVER_COIN   = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item GOLD_COIN     = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item DIAMOND_COIN  = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item EMERALD_COIN  = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item RUBY_COIN     = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item AMETHYST_COIN = MPPlugFactory.CREATE_ITEM_PLUG();

    public static Item MYTHIC_COIN = MPPlugFactory.CREATE_ITEM_PLUG();

    public static Item WOOL_COIN      = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item WOOD_COIN      = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item COAL_COIN      = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item STONE_COIN     = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item LAPIS_COIN     = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item REDSTONE_COIN  = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item OBSIDIAN_COIN  = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item NETHERITE_COIN = MPPlugFactory.CREATE_ITEM_PLUG();

    public static Item SMALL_WALLET   = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item LEATHER_WALLET = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item FAT_WALLET     = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item BIG_WALLET     = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item HUGE_WALLET    = MPPlugFactory.CREATE_ITEM_PLUG();

    public static Item MAGIC_WALLET  = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item CHEST_WALLET  = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item FOREST_WALLET = MPPlugFactory.CREATE_ITEM_PLUG();

    public static Item LEAK_SMALL_WALLET   = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item LEAK_LEATHER_WALLET = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item LEAK_FAT_WALLET     = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item LEAK_BIG_WALLET     = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item LEAK_HUGE_WALLET    = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item LEAK_CHEST_WALLET   = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item LEAK_MAGIC_WALLET   = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item LEAK_FOREST_WALLET  = MPPlugFactory.CREATE_ITEM_PLUG();

    public static Item EPIC_WALLET      = MPPlugFactory.CREATE_ITEM_PLUG();
    public static Item LEGENDARY_WALLET = MPPlugFactory.CREATE_ITEM_PLUG();

    public static Item INFINITY_WALLET = MPPlugFactory.CREATE_ITEM_PLUG();

    private static void onInit() {
        UNEXPLORED_TREASURE_MAP = new ItemUnexploredMap(ItemNames.UNEXPLORED_TREASURE_MAP_NAME, true);
        UNEXPLORED_STRUCTURE_MAP = new ItemUnexploredMap(ItemNames.UNEXPLORED_STRUCTURE_MAP_NAME, false);

        BRONZE_COIN = new ItemCoin(NormalCoinType.BRONZE);
        SILVER_COIN = new ItemCoin(NormalCoinType.SILVER);
        GOLD_COIN = new ItemCoin(NormalCoinType.GOLD);
        DIAMOND_COIN = new ItemCoin(NormalCoinType.DIAMOND);
        EMERALD_COIN = new ItemCoin(NormalCoinType.EMERALD);
        RUBY_COIN = new ItemCoin(NormalCoinType.RUBY);
        AMETHYST_COIN = new ItemCoin(NormalCoinType.AMETHYST);

        MYTHIC_COIN = new ItemCoin(SpecificCoinType.MYTHIC);

        WOOL_COIN = new ItemCoin(ResourceCoinType.WOOL);
        WOOD_COIN = new ItemCoin(ResourceCoinType.WOOD);
        STONE_COIN = new ItemCoin(ResourceCoinType.STONE);
        COAL_COIN = new ItemCoin(ResourceCoinType.COAL);
        LAPIS_COIN = new ItemCoin(ResourceCoinType.LAPIS);
        REDSTONE_COIN = new ItemCoin(ResourceCoinType.REDSTONE);
        OBSIDIAN_COIN = new ItemCoin(ResourceCoinType.OBSIDIAN);
        NETHERITE_COIN = new ItemCoin(ResourceCoinType.NETHERITE);

        SMALL_WALLET = new ItemNormalWallet(
                DefaultWalletType.SMALL,
                DefaultWalletType.LEAK_SMALL,
                NormalWalletCapacity.create(4_096L),
                ResourceWalletCapacity.create(
                        25, 25, 25,
                        25, 0, 0,
                        0, 0
                ),
                SpecificWalletCapacity.create(1)
        );
        LEATHER_WALLET = new ItemNormalWallet(
                DefaultWalletType.LEATHER,
                DefaultWalletType.LEAK_LEATHER,
                NormalWalletCapacity.create(40_960L),
                ResourceWalletCapacity.createSingle(50),
                SpecificWalletCapacity.create(5)
        );
        FAT_WALLET = new ItemNormalWallet(
                DefaultWalletType.FAT,
                DefaultWalletType.LEAK_FAT,
                NormalWalletCapacity.create(2_621_440L),
                ResourceWalletCapacity.createSingle(100),
                SpecificWalletCapacity.create(20)
        );
        BIG_WALLET = new ItemNormalWallet(
                DefaultWalletType.BIG,
                DefaultWalletType.LEAK_BIG,
                NormalWalletCapacity.create(167_772_160L),
                ResourceWalletCapacity.createSingle(250),
                SpecificWalletCapacity.create(50)
        );
        HUGE_WALLET = new ItemNormalWallet(
                DefaultWalletType.HUGE,
                DefaultWalletType.LEAK_HUGE,
                NormalWalletCapacity.create(10_737_418_240L),
                ResourceWalletCapacity.createSingle(500),
                SpecificWalletCapacity.create(100)
        );

        MAGIC_WALLET = new ItemMagicWallet(
                DefaultWalletType.MAGIC,
                DefaultWalletType.LEAK_MAGIC,
                NormalWalletCapacity.create(2_621_440L),
                ResourceWalletCapacity.create(
                        100, 100, 100,
                        100, 10000,
                        10000, 10000,
                        10000
                ),
                SpecificWalletCapacity.create(20)
        );
        CHEST_WALLET = new ItemChestWallet(
                DefaultWalletType.CHEST,
                DefaultWalletType.LEAK_CHEST
        );
        FOREST_WALLET = new ItemForestWallet(
                DefaultWalletType.FOREST,
                DefaultWalletType.LEAK_FOREST,
                NormalWalletCapacity.create(2_621_440L),
                ResourceWalletCapacity.create(
                        10000, 10000,
                        10000, 10000,
                        100, 100,
                        100, 100
                ),
                SpecificWalletCapacity.create(20)
        );

        LEAK_SMALL_WALLET = new ItemLeakNormalWallet(
                DefaultWalletType.LEAK_SMALL,
                DefaultWalletType.SMALL,
                NormalWalletCapacity.create(4_096L),
                ResourceWalletCapacity.create(
                        25, 25, 25,
                        25, 0, 0,
                        0, 0
                ),
                SpecificWalletCapacity.create(1)
        );
        LEAK_LEATHER_WALLET = new ItemLeakNormalWallet(
                DefaultWalletType.LEAK_LEATHER,
                DefaultWalletType.LEATHER,
                NormalWalletCapacity.create(40_960L),
                ResourceWalletCapacity.createSingle(50),
                SpecificWalletCapacity.create(5)
        );
        LEAK_FAT_WALLET = new ItemLeakNormalWallet(
                DefaultWalletType.LEAK_FAT,
                DefaultWalletType.FAT,
                NormalWalletCapacity.create(2_621_440L),
                ResourceWalletCapacity.createSingle(100),
                SpecificWalletCapacity.create(20)
        );
        LEAK_BIG_WALLET = new ItemLeakNormalWallet(
                DefaultWalletType.LEAK_BIG,
                DefaultWalletType.BIG,
                NormalWalletCapacity.create(167_772_160L),
                ResourceWalletCapacity.createSingle(250),
                SpecificWalletCapacity.create(50)
        );
        LEAK_HUGE_WALLET = new ItemLeakNormalWallet(
                DefaultWalletType.LEAK_HUGE,
                DefaultWalletType.HUGE,
                NormalWalletCapacity.create(10_737_418_240L),
                ResourceWalletCapacity.createSingle(500),
                SpecificWalletCapacity.create(100)
        );

        LEAK_MAGIC_WALLET = new ItemLeakMagicWallet(
                DefaultWalletType.LEAK_MAGIC,
                DefaultWalletType.MAGIC,
                NormalWalletCapacity.create(2_621_440L),
                ResourceWalletCapacity.create(
                        100, 100, 100,
                        100, 10000,
                        10000, 10000,
                        10000
                ),
                SpecificWalletCapacity.create(20)
        );
        LEAK_CHEST_WALLET = new ItemLeakChestWallet(
                DefaultWalletType.LEAK_CHEST,
                DefaultWalletType.CHEST
        );
        LEAK_FOREST_WALLET = new ItemLeakForestWallet(
                DefaultWalletType.LEAK_FOREST,
                DefaultWalletType.FOREST,
                NormalWalletCapacity.create(2_621_440L),
                ResourceWalletCapacity.create(
                        10000, 10000,
                        10000, 10000,
                        100, 100,
                        100, 100
                ),
                SpecificWalletCapacity.create(20)
        );

        EPIC_WALLET = new ItemEpicWallet(
                DefaultWalletType.EPIC,
                NormalWalletCapacity.create(687_194_767_360L),
                ResourceWalletCapacity.createSingle(500),
                SpecificWalletCapacity.create(500)
        );
        LEGENDARY_WALLET = new ItemLegendaryWallet(
                DefaultWalletType.LEGENDARY,
                NormalWalletCapacity.create(6_871_947_673_600L),
                ResourceWalletCapacity.createSingle(1000),
                SpecificWalletCapacity.create(1000)
        );

        INFINITY_WALLET = new ItemInfinityWallet(
                DefaultWalletType.INFINITY
        );
    }

    @SubscribeEvent
    public static void onRegistryItem(RegistryEvent.Register<Item> e) {
        onInit();
        MouseProject.LOGGER.info("Initialized items");
        IForgeRegistry<Item> registry = e.getRegistry();
        MPReflectionUtils.prepare(MPItems.class).<Item>getPublicStaticFields().forEach(registry::register);
        MouseProject.LOGGER.info("Registered items");
    }

    @SubscribeEvent @SideOnly(Side.CLIENT)
    public static void onRegistryModel(ModelRegistryEvent e) {
        MPReflectionUtils.prepare(MPItems.class).<Item>getPublicStaticFields().forEach(MPItems::registerModel);
        MouseProject.LOGGER.info("Registered item models");
    }

    @SideOnly(Side.CLIENT)
    private static void registerModel(Item item) {
        ResourceLocation regName = item.getRegistryName();
        if (regName != null) {
            ModelResourceLocation mrl = new ModelResourceLocation(regName, "inventory");
            ModelBakery.registerItemVariants(item, mrl);
            ModelLoader.setCustomModelResourceLocation(item, 0, mrl);
        } else MouseProject.LOGGER.warn("{} didn't contains registry constants", item.getTranslationKey());
    }
}
