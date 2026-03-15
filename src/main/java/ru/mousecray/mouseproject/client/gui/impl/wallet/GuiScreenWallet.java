/*******************************************************************************
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 ******************************************************************************/

package ru.mousecray.mouseproject.client.gui.impl.wallet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.client.gui.MPGuiScreen;
import ru.mousecray.mouseproject.client.gui.container.MPGuiPanel;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.impl.*;
import ru.mousecray.mouseproject.client.gui.impl.container.*;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.MPGuiElementCache;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.state.GuiButtonPersistentState;
import ru.mousecray.mouseproject.common.economy.CoinHelper;
import ru.mousecray.mouseproject.common.economy.CoinValue;
import ru.mousecray.mouseproject.common.economy.coin.CoinType;
import ru.mousecray.mouseproject.common.economy.coin.NormalCoinType;
import ru.mousecray.mouseproject.common.economy.coin.ResourceCoinType;
import ru.mousecray.mouseproject.common.economy.coin.SpecificCoinType;
import ru.mousecray.mouseproject.common.item.wallet.IWallet;
import ru.mousecray.mouseproject.nbt.ItemStackWalletNBTPipeline;
import ru.mousecray.mouseproject.nbt.MouseProjectNBT;
import ru.mousecray.mouseproject.utils.ref.IntRef;
import ru.mousecray.mouseproject.utils.ref.StringRef;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public class GuiScreenWallet extends MPGuiScreen {
    static final  ResourceLocation           TEXTURES      = new ResourceLocation(Tags.MOD_ID, "textures/gui/wallet.png");
    static final  GuiVector                  TEXTURES_SIZE = GuiVector.of(256);
    private final EntityPlayer               player;
    private final ItemStackWalletNBTPipeline walletPipe;
    private final ItemStack                  walletStack;

    public GuiScreenWallet(EntityPlayer player, int slot) {
        super("wallet_screen", GuiVector.of(230, 200), GuiVector.of(4));
        this.player = player;
        walletStack = player.inventory.getStackInSlot(slot).copy();
        walletPipe = !(walletStack.getItem() instanceof IWallet) ? null : MouseProjectNBT.get(walletStack).getWalletPipe();

        setBackground(TEXTURES, TEXTURES_SIZE, new GuiShape(0, 0, 230, 200));
    }

    @Override
    public void initGui() {
        resetGui();
        Keyboard.enableRepeatEvents(true);
        super.initGui();

        long       maxCoinValue = 1_000_000;
        MPFontSize fontSize     = MPFontSize.NORMAL;

        MPGuiCloseButton closeButton = MPGuiElementCache.INSTANCE.getOrCreate(
                this, "close_button", MPGuiCloseButton.class,
                () -> new MPGuiCloseButton(
                        new GuiShape(0, 0, 9, 9),
                        event -> closeGui()
                )
        );
        addButton(closeButton, GuiMargin.ZERO, AnchorPosition.TOP_RIGHT, GuiVector.ZERO);

        if (walletPipe == null) return;

        MPGuiStaticLabel titleLabel = MPGuiElementCache.INSTANCE.getOrCreate(
                this, "title_label", MPGuiStaticLabel.class,
                () -> new MPGuiStaticLabel(MPGuiString.simple(walletStack.getDisplayName()), fontRenderer,
                        new GuiShape(0, 0, 80, 10),
                        14737632, fontSize
                ),
                null,
                t -> t.setGuiString(MPGuiString.simple(walletStack.getDisplayName()))
        );
        addLabel(titleLabel, null, AnchorPosition.TOP_LEFT, GuiVector.ZERO);

        float panelWidth  = 114f;
        float controlSize = 13.0f;
        float controlGap  = 10f;

        MPGuiActionButton takeAction = MPGuiElementCache.INSTANCE.getOrCreate(
                this, "take_action", MPGuiActionButton.class,
                () -> new MPGuiActionButton(
                        GuiShape.ZERO,
                        MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.button.take"), fontSize,
                        event -> { }
                ),
                t -> {
                    t.applyState(GuiButtonPersistentState.DISABLED);
                    t.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT));
                }
        );

        MPGuiActionButton putAction = MPGuiElementCache.INSTANCE.getOrCreate(
                this, "put_action", MPGuiActionButton.class,
                () -> new MPGuiActionButton(
                        GuiShape.ZERO,
                        MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.button.put"), fontSize,
                        event -> { }
                ),
                t -> {
                    t.applyState(GuiButtonPersistentState.DISABLED);
                    t.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT));
                }
        );

        WalletSliderControl walletSlider = MPGuiElementCache.INSTANCE.getOrCreate(
                this, "wallet_slider_control", WalletSliderControl.class,
                () -> new WalletSliderControl(fontRenderer, fontSize, panelWidth, 16, maxCoinValue),
                t -> {
                    t.onValidityChanged(isValid -> {
                        takeAction.applyState(isValid ? GuiButtonPersistentState.NORMAL : GuiButtonPersistentState.DISABLED);
                        putAction.applyState(isValid ? GuiButtonPersistentState.NORMAL : GuiButtonPersistentState.DISABLED);
                    });
                    t.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_HORIZONTAL));
                }
        );

        MPGuiLinearPanel controls = MPGuiElementCache.INSTANCE.getOrCreate(
                this, "controls_panel", MPGuiLinearPanel.class,
                () -> new MPGuiLinearPanel(new GuiShape(0, 0, panelWidth, 67), LinearPanelOrientation.VERTICAL),
                null,
                MPGuiPanel::removeAllChildren
        );
        addPanel(controls, null, AnchorPosition.TOP_LEFT, GuiVector.of(0, 133));

        MPGuiLinearPanel row1 = MPGuiElementCache.INSTANCE.getOrCreate(
                this, "row1_panel", MPGuiLinearPanel.class,
                () -> new MPGuiLinearPanel(new GuiShape(0, 0, panelWidth, controlSize), LinearPanelOrientation.HORIZONTAL),
                null, MPGuiPanel::removeAllChildren
        );
        row1.addChild(createDynamicButton("btn_+1", "+1", fontSize, e -> walletSlider.addValue(1)), null, null);
        row1.addChild(createDynamicButton("btn_+10", "+10", fontSize, e -> walletSlider.addValue(10)), null, null);
        row1.addChild(createDynamicButton("btn_+50", "+50", fontSize, e -> walletSlider.addValue(50)), null, null);
        row1.addChild(createSpacer("row1_spacer", controlGap), null, null);
        row1.addChild(createDynamicButton("btn_-1", "-1", fontSize, e -> walletSlider.addValue(-1)), null, null);
        row1.addChild(createDynamicButton("btn_-10", "-10", fontSize, e -> walletSlider.addValue(-10)), null, null);
        row1.addChild(createDynamicButton("btn_-50", "-50", fontSize, e -> walletSlider.addValue(-50)), null, null);
        row1.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_VERTICAL));
        controls.addChild(row1, null, null);

        MPGuiLinearPanel row2 = MPGuiElementCache.INSTANCE.getOrCreate(
                this, "row2_panel", MPGuiLinearPanel.class,
                () -> new MPGuiLinearPanel(new GuiShape(0, 0, panelWidth, controlSize), LinearPanelOrientation.HORIZONTAL),
                null, MPGuiPanel::removeAllChildren
        );
        row2.addChild(createDynamicButton("btn_+100", "+100", fontSize, e -> walletSlider.addValue(100)), null, null);
        row2.addChild(createDynamicButton("btn_+500", "+500", fontSize, e -> walletSlider.addValue(500)), null, null);
        row2.addChild(createDynamicButton("btn_+1K", "+1K", fontSize, e -> walletSlider.addValue(1000)), null, null);
        row2.addChild(createSpacer("row2_spacer", controlGap), null, null);
        row2.addChild(createDynamicButton("btn_-100", "-100", fontSize, e -> walletSlider.addValue(-100)), null, null);
        row2.addChild(createDynamicButton("btn_-500", "-500", fontSize, e -> walletSlider.addValue(-500)), null, null);
        row2.addChild(createDynamicButton("btn_-1K", "-1K", fontSize, e -> walletSlider.addValue(-1000)), null, null);
        row2.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_VERTICAL));
        controls.addChild(row2, null, null);

        controls.addChild(walletSlider, new GuiMargin(0, 4f, 0, 2f), null);

        controls.addChild(takeAction, null, null);
        controls.addChild(putAction, null, null);

        float coinW        = 14.9f;
        float coinH        = 23f;
        int   slot_count_x = 7;
        float CELL_GAP     = 6f;
        float colWidth     = slot_count_x * coinW;

        Map<Integer, List<CoinValue>> activeGroups = new HashMap<>();

        CoinValue bronzeBal = walletPipe.loadBronzeBalance();
        if (bronzeBal != null && bronzeBal.getValue() > 0) {
            EnumMap<NormalCoinType, Long> displayCoins = CoinHelper.getDisplayCoins(
                    CoinHelper.getMaxCoin(bronzeBal.getValue()), bronzeBal.getValue()
            );
            List<CoinValue> normalSlots = displayCoins.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .map(e -> CoinValue.create(e.getValue(), e.getKey()))
                    .collect(Collectors.toList());
            if (!normalSlots.isEmpty()) activeGroups.put(0, normalSlots);
        }

        List<CoinType> coinTypes = walletPipe.loadAllBalanceTypes();
        List<CoinValue> resourceSlots = coinTypes.stream()
                .filter(type -> type instanceof ResourceCoinType)
                .map(walletPipe::loadResourceBalance)
                .filter(cv -> cv != null && cv.getValue() > 0).collect(Collectors.toList());
        if (!resourceSlots.isEmpty()) activeGroups.put(1, resourceSlots);

        List<CoinValue> specificSlots = coinTypes.stream()
                .filter(type -> type instanceof SpecificCoinType)
                .map(walletPipe::loadSpecificBalance)
                .filter(cv -> cv != null && cv.getValue() > 0).collect(Collectors.toList());
        if (!specificSlots.isEmpty()) activeGroups.put(2, specificSlots);

        List<CoinValue> otherSlots = coinTypes.stream()
                .filter(type -> !(type instanceof NormalCoinType) && !(type instanceof ResourceCoinType) && !(type instanceof SpecificCoinType))
                .map(walletPipe::loadOtherBalance)
                .filter(cv -> cv != null && cv.getValue() > 0).collect(Collectors.toList());
        if (!otherSlots.isEmpty()) activeGroups.put(3, otherSlots);

        MPGuiLinearPanel coinsPanel = MPGuiElementCache.INSTANCE.getOrCreate(
                this, "coins_anchor_content", MPGuiLinearPanel.class,
                () -> new MPGuiLinearPanel(
                        new GuiShape(0, 0, 222, 400), LinearPanelOrientation.HORIZONTAL
                ),
                t -> t.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_HORIZONTAL)),
                MPGuiPanel::removeAllChildren
        );

        MPGuiSimpleScrollPanel coinsContainer = MPGuiElementCache.INSTANCE.getOrCreate(
                this, "coins_container", MPGuiSimpleScrollPanel.class,
                () -> new MPGuiSimpleScrollPanel(new GuiShape(0, 0, 222, 115)),
                null, t -> t.setContent(coinsPanel)
        );
        addPanel(coinsContainer, null, AnchorPosition.TOP_LEFT, new GuiVector(4, 10));

        if (activeGroups.isEmpty()) {
            MPGuiStaticLabel emptyLabel = MPGuiElementCache.INSTANCE.getOrCreate(
                    this, "empty_label", MPGuiStaticLabel.class,
                    () -> new MPGuiStaticLabel(MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.label.empty"),
                            fontRenderer, new GuiShape(0, 0, 80, 10), 14737632, fontSize)
            );
            coinsPanel.addChild(emptyLabel, null, new GuiVector(2, 0));
        } else {
            List<Map.Entry<Integer, List<CoinValue>>> groupsList = new ArrayList<>(activeGroups.entrySet());

            for (int col = 0; col < 2; col++) {
                MPGuiLinearPanel columnPanel = MPGuiElementCache.INSTANCE.getOrCreate(
                        this, "column_panel_" + col, MPGuiLinearPanel.class,
                        () -> new MPGuiLinearPanel(new GuiShape(0, 0, colWidth, 115), LinearPanelOrientation.VERTICAL),
                        t -> t.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_HORIZONTAL)),
                        MPGuiPanel::removeAllChildren
                );
                coinsPanel.addChild(columnPanel, new GuiMargin(0, 3f, 0, 0), null);

                for (int row = 0; row < 2; row++) {
                    int idx = col * 2 + row;
                    if (idx >= groupsList.size()) break;

                    Map.Entry<Integer, List<CoinValue>> groupSlots = groupsList.get(idx);

                    StringRef groupLabelKey = new StringRef("gui." + Tags.MOD_ID + ".wallet.label.");
                    switch (groupSlots.getKey()) {
                        case 0:
                            groupLabelKey.$A("normal");
                            break;
                        case 1:
                            groupLabelKey.$A("resource");
                            break;
                        case 2:
                            groupLabelKey.$A("specific");
                            break;
                        default:
                            groupLabelKey.$A("other");
                            break;
                    }

                    IntRef rowsNum = new IntRef((int) Math.ceil(groupSlots.getValue().size() / (double) slot_count_x));
                    if (rowsNum.$() == 0) rowsNum.$(1);
                    float groupH = 14 + rowsNum.$() * coinH;

                    MPGuiLinearPanel groupPanel = MPGuiElementCache.INSTANCE.getOrCreate(
                            this, "group_panel_" + idx, MPGuiLinearPanel.class,
                            () -> new MPGuiLinearPanel(new GuiShape(0, 0, colWidth, groupH), LinearPanelOrientation.VERTICAL),
                            null,
                            t -> {
                                t.setElementShape(t.getElementShape().withHeight(groupH));
                                t.removeAllChildren();
                            }
                    );
                    columnPanel.addChild(groupPanel, new GuiMargin(0, 0, 8f, 8f), null);

                    MPGuiAnchorPanel titlePanel = MPGuiElementCache.INSTANCE.getOrCreate(
                            this, "title_panel_" + idx, MPGuiAnchorPanel.class,
                            () -> new MPGuiAnchorPanel(new GuiShape(0, 0, colWidth, 12)),
                            null, MPGuiPanel::removeAllChildren
                    );
                    groupPanel.addChild(titlePanel, new GuiMargin(0, 3f), null);

                    MPGuiStaticLabel groupTitle = MPGuiElementCache.INSTANCE.getOrCreate(
                            this, "group_title_" + idx, MPGuiStaticLabel.class,
                            () -> new MPGuiStaticLabel(MPGuiString.localized(groupLabelKey.$()), fontRenderer,
                                    new GuiShape(0, 0, colWidth - 15, 10), 14737632, fontSize),
                            null, t -> t.setGuiString(MPGuiString.localized(groupLabelKey.$())), null
                    );
                    titlePanel.addChild(groupTitle, null, AnchorPosition.MIDDLE_LEFT, null);

                    MPGuiCheckButton selectAll = MPGuiElementCache.INSTANCE.getOrCreate(
                            this, "select_all_" + idx, MPGuiCheckButton.class,
                            () -> new MPGuiCheckButton(
                                    MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.button.select_all"), fontRenderer,
                                    new GuiShape(0, 0, 8, 8), TEXTURES, TEXTURES_SIZE,
                                    new GuiShape(240, 0, 8, 8), fontSize,
                                    e -> { }
                            )
                    );
                    titlePanel.addChild(selectAll, null, AnchorPosition.TOP_RIGHT, null);

                    MPGuiGridPanel coinsGrid = MPGuiElementCache.INSTANCE.getOrCreate(
                            this, "coins_grid_" + idx, MPGuiGridPanel.class,
                            () -> new MPGuiGridPanel(new GuiShape(0, 0, colWidth, rowsNum.$() * coinH), rowsNum.$(), slot_count_x),
                            t -> t.setGaps(0, CELL_GAP),
                            t -> {
                                t.setGridSize(rowsNum.$(), slot_count_x);
                                t.setElementShape(t.getElementShape().withHeight(rowsNum.$() * coinH));
                                t.removeAllChildren();
                            }
                    );
                    groupPanel.addChild(coinsGrid, new GuiMargin(2, 0, 0, 0), null);

                    int slotIndex = 0;
                    for (CoinValue coinValue : groupSlots.getValue()) {
                        if (slotIndex >= 32) break;
                        int gridRow = slotIndex / slot_count_x;
                        int gridCol = slotIndex % slot_count_x;

                        WalletCoinButton coinBtn = MPGuiElementCache.INSTANCE.getOrCreate(
                                this, "coin_btn_" + idx + "_" + slotIndex, WalletCoinButton.class,
                                () -> new WalletCoinButton(
                                        new GuiShape(0, 0, coinW, coinH),
                                        fontSize, coinValue, e -> { }
                                ),
                                null, t -> t.setCount(coinValue), null
                        );
                        coinsGrid.addChild(coinBtn, null, AnchorPosition.MIDDLE_CENTER, null, new GridPos(gridRow, gridCol));
                        slotIndex++;
                    }
                }
            }
        }
        bake();
    }

    private MPGuiDefaultButton createDynamicButton(String key, String text, MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<MPGuiDefaultButton>> onClick) {
        return MPGuiElementCache.INSTANCE.getOrCreate(
                this, key, MPGuiDefaultButton.class,
                () -> new MPGuiDefaultButton(
                        MPGuiString.simple(text),
                        new GuiShape(0, 0, 10, 13.0f), TEXTURES, TEXTURES_SIZE,
                        new GuiShape(80, 200, 10, 10), fontSize, onClick
                ),
                t -> t.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_HORIZONTAL))
        );
    }

    private MPGuiFreePanel createSpacer(String key, float width) {
        return MPGuiElementCache.INSTANCE.getOrCreate(
                this, key, MPGuiFreePanel.class,
                () -> new MPGuiFreePanel(new GuiShape(0, 0, width, 1))
        );
    }

    private void sendWalletToServer() { }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }
}