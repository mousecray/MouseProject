package ru.mousecray.mouseproject.client.gui.impl.wallet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import ru.mousecray.mouseproject.Tags;
import ru.mousecray.mouseproject.client.gui.MPGuiScreen;
import ru.mousecray.mouseproject.client.gui.dim.*;
import ru.mousecray.mouseproject.client.gui.event.MPGuiMouseClickEvent;
import ru.mousecray.mouseproject.client.gui.event.MPGuiTextTypedEvent;
import ru.mousecray.mouseproject.client.gui.impl.*;
import ru.mousecray.mouseproject.client.gui.impl.container.*;
import ru.mousecray.mouseproject.client.gui.misc.MPFontSize;
import ru.mousecray.mouseproject.client.gui.misc.lang.MPGuiString;
import ru.mousecray.mouseproject.client.gui.misc.texture.MPGuiTexturePack;
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
    static final  GuiVector                  TEXTURES_SIZE = new GuiVector(256, 256);
    private final EntityPlayer               player;
    private final ItemStackWalletNBTPipeline walletPipe;
    private final ItemStack                  walletStack;

    public GuiScreenWallet(EntityPlayer player, int slot) {
        super(new GuiVector(230, 200), new GuiVector(4, 4));
        this.player = player;
        walletStack = player.inventory.getStackInSlot(slot).copy();
        walletPipe = !(walletStack.getItem() instanceof IWallet) ? null : MouseProjectNBT.get(walletStack).getWalletPipe();

        setBackground(TEXTURES, TEXTURES_SIZE, new GuiShape(0, 0, 230, 200));
    }

    @Override
    public void initGui() {
        resetGui();
        Keyboard.enableRepeatEvents(true);

        long       maxCoinValue = 1_000_000;
        MPFontSize fontSize     = MPFontSize.NORMAL;

        MPGuiCloseButton closeButton = getElementCache().getOrCreate(
                "close_button", MPGuiCloseButton.class,
                () -> new MPGuiCloseButton(
                        new GuiShape(0, 0, 9, 9),
                        TEXTURES, TEXTURES_SIZE, new GuiShape(95, 200, 9, 9), fontSize,
                        event -> closeGui()
                )
        );
        addButton(closeButton, null, AnchorPosition.TOP_RIGHT, GuiVector.ZERO);

        if (walletPipe == null) return;

        MPGuiStaticLabel titleLabel = getElementCache().getOrCreate(
                "title_label", MPGuiStaticLabel.class,
                () -> new MPGuiStaticLabel(MPGuiString.simple(walletStack.getDisplayName()), fontRenderer,
                        new GuiShape(0, 0, 80, 10),
                        14737632, fontSize
                ),
                t -> t.setGuiString(MPGuiString.simple(walletStack.getDisplayName()))
        );
        addLabel(titleLabel, null, AnchorPosition.TOP_LEFT, GuiVector.ZERO);

        float panelWidth         = 114f;
        float buttonTakePutYSize = 13.0f;
        float buttonTakePutGap   = 10f;

        MPGuiActionButton takeAction = getElementCache().getOrCreate(
                "take_action", MPGuiActionButton.class,
                () -> new MPGuiActionButton(MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.button.take"),
                        new GuiShape(0, 0, panelWidth, 12),
                        TEXTURES, TEXTURES_SIZE, new GuiShape(0, 200, 80, 10), fontSize, event -> { }
                ),
                t -> {
                    t.applyState(GuiButtonPersistentState.DISABLED);
                    t.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_HORIZONTAL));
                }
        );

        MPGuiActionButton putAction = getElementCache().getOrCreate(
                "put_action", MPGuiActionButton.class,
                () -> new MPGuiActionButton(MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.button.put"),
                        new GuiShape(0, 0, panelWidth, 12),
                        TEXTURES, TEXTURES_SIZE, new GuiShape(0, 200, 80, 10), fontSize, event -> { }
                ),
                t -> {
                    t.applyState(GuiButtonPersistentState.DISABLED);
                    t.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_HORIZONTAL));
                }
        );

        Consumer<MPGuiTextTypedEvent<MPGuiNumberField>> fieldEventTake = event -> {
            String newText = event.getNewText();
            if (newText == null || newText.length() > 19 || newText.trim().isEmpty()) {
                event.setCancelled(true);
                takeAction.applyState(GuiButtonPersistentState.DISABLED);
                putAction.applyState(GuiButtonPersistentState.DISABLED);
                return;
            }
            try {
                long l = Long.parseLong(newText);
                if (l <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ignore) {
                event.setCancelled(true);
                takeAction.applyState(GuiButtonPersistentState.DISABLED);
                putAction.applyState(GuiButtonPersistentState.DISABLED);
                return;
            }
            takeAction.applyState(GuiButtonPersistentState.NORMAL);
            putAction.applyState(GuiButtonPersistentState.NORMAL);
        };

        MPGuiNumberField fieldTakePut = getElementCache().getOrCreate(
                "field_take_put", MPGuiNumberField.class,
                () -> new MPGuiNumberField(fontRenderer, MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.text_field.take_put_count"),
                        new GuiShape(0, 0, panelWidth, buttonTakePutYSize * 1.2f),
                        TEXTURES, TEXTURES_SIZE, new GuiShape(104, 200, 80, 10), fontSize, fieldEventTake
                ),
                t -> {
                    t.applyState(GuiButtonPersistentState.DISABLED);
                    t.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_HORIZONTAL));
                }
        );

        class WalletSlider extends MPGuiSlider<WalletSlider> {
            public WalletSlider() {
                super(new GuiShape(0, 0, panelWidth, 11),
                        MPGuiTexturePack.Builder.create(TEXTURES, TEXTURES_SIZE, new GuiVector(0, 200), new GuiVector(80, 10)).build(),
                        MPGuiTexturePack.Builder.create(TEXTURES, TEXTURES_SIZE, new GuiVector(90, 200), new GuiVector(5, 7)).build(),
                        new GuiVector(6.68f, 11), 0, 100, false);
            }
        }
        @SuppressWarnings("Convert2MethodRef")
        WalletSlider slider = getElementCache().getOrCreate(
                "wallet_slider", WalletSlider.class,
                () -> new WalletSlider(),
                t -> {
                    t.onChange(value -> fieldTakePut.setNumberText(value == 0 ? 1 : (long) value * maxCoinValue / 100));
                    t.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_HORIZONTAL));
                }
        );

        MPGuiLinearPanel controls = getElementCache().getOrCreate(
                "controls_panel", MPGuiLinearPanel.class,
                () -> new MPGuiLinearPanel(new GuiShape(0, 0, panelWidth, 67), LinearOrientation.VERTICAL),
                t -> t.getChildren().clear()
        );
        addPanel(controls, null, AnchorPosition.TOP_LEFT, new GuiVector(0, 133));

        MPGuiLinearPanel row1 = getElementCache().getOrCreate(
                "row1_panel", MPGuiLinearPanel.class,
                () -> new MPGuiLinearPanel(new GuiShape(0, 0, panelWidth, buttonTakePutYSize), LinearOrientation.HORIZONTAL),
                t -> t.getChildren().clear()
        );
        row1.addChild(createDynamicButton("btn_+1", "+1", fontSize, e -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() + 1, 1))), null, null);
        row1.addChild(createDynamicButton("btn_+10", "+10", fontSize, e -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() + 10, 1))), null, null);
        row1.addChild(createDynamicButton("btn_+50", "+50", fontSize, e -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() + 50, 1))), null, null);
        row1.addChild(createSpacer("row1_spacer", buttonTakePutGap), null, null);
        row1.addChild(createDynamicButton("btn_-1", "-1", fontSize, e -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() - 1, 1))), null, null);
        row1.addChild(createDynamicButton("btn_-10", "-10", fontSize, e -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() - 10, 1))), null, null);
        row1.addChild(createDynamicButton("btn_-50", "-50", fontSize, e -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() - 50, 1))), null, null);
        controls.addChild(row1, null, null);

        MPGuiLinearPanel row2 = getElementCache().getOrCreate(
                "row2_panel", MPGuiLinearPanel.class,
                () -> new MPGuiLinearPanel(new GuiShape(0, 0, panelWidth, buttonTakePutYSize), LinearOrientation.HORIZONTAL),
                t -> t.getChildren().clear()
        );
        row2.addChild(createDynamicButton("btn_+100", "+100", fontSize, e -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() + 100, 1))), null, null);
        row2.addChild(createDynamicButton("btn_+500", "+500", fontSize, e -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() + 500, 1))), null, null);
        row2.addChild(createDynamicButton("btn_+1K", "+1K", fontSize, e -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() + 1000, 1))), null, null);
        row2.addChild(createSpacer("row2_spacer", buttonTakePutGap), null, null);
        row2.addChild(createDynamicButton("btn_-100", "-100", fontSize, e -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() - 100, 1))), null, null);
        row2.addChild(createDynamicButton("btn_-500", "-500", fontSize, e -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() - 500, 1))), null, null);
        row2.addChild(createDynamicButton("btn_-1K", "-1K", fontSize, e -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() - 1000, 1))), null, null);
        controls.addChild(row2, null, null);

        MPGuiFreePanel fieldSliderStack = getElementCache().getOrCreate(
                "field_slider_stack", MPGuiFreePanel.class,
                () -> new MPGuiFreePanel(new GuiShape(0, 0, panelWidth, 18)),
                t -> t.getChildren().clear()
        );
        controls.addChild(fieldSliderStack, null, null);

        fieldSliderStack.addChild(fieldTakePut, null, null);
        fieldSliderStack.addChild(slider, null, new GuiVector(0, buttonTakePutYSize * 1.2f - 9));

        controls.addChild(takeAction, null, null);
        controls.addChild(putAction, null, null);

        float coinW        = 14.9f;
        float coinH        = 23f;
        int   slot_count_x = 7;
        float COLUMN_GAP   = 12f;
        float CELL_GAP     = 6f;
        float colWidth     = slot_count_x * coinW; //~104.3

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

        MPGuiAnchorPanel coinsAnchorContent = getElementCache().getOrCreate(
                "coins_anchor_content", MPGuiAnchorPanel.class,
                () -> new MPGuiAnchorPanel(new GuiShape(0, 0, 222, 115)),
                t -> t.getChildren().clear()
        );

        MPGuiSimpleScrollPanel coinsContainer = getElementCache().getOrCreate(
                "coins_container", MPGuiSimpleScrollPanel.class,
                () -> new MPGuiSimpleScrollPanel(new GuiShape(0, 0, 222, 115)),
                t -> t.setContent(coinsAnchorContent)
        );
        addPanel(coinsContainer, null, AnchorPosition.TOP_LEFT, new GuiVector(4, 15));

        if (activeGroups.isEmpty()) {
            MPGuiStaticLabel emptyLabel = getElementCache().getOrCreate(
                    "empty_label", MPGuiStaticLabel.class,
                    () -> new MPGuiStaticLabel(MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.label.empty"),
                            fontRenderer, new GuiShape(0, 0, 80, 10), 14737632, fontSize)
            );
            coinsAnchorContent.addChild(emptyLabel, null, AnchorPosition.TOP_LEFT, new GuiVector(2, 0));
        } else {
            List<Map.Entry<Integer, List<CoinValue>>> groupsList = new ArrayList<>(activeGroups.entrySet());

            for (int col = 0; col < 2; col++) {
                float colX = col * (colWidth + COLUMN_GAP);

                MPGuiLinearPanel columnPanel = getElementCache().getOrCreate(
                        "column_panel_" + col, MPGuiLinearPanel.class,
                        () -> new MPGuiLinearPanel(new GuiShape(colX, 0, colWidth, 115), LinearOrientation.VERTICAL),
                        t -> t.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT)),
                        t -> {
                            t.setElementShape(t.getElementShape().withX(colX));
                            t.getChildren().clear();
                        }
                );
                coinsAnchorContent.addChild(columnPanel, null, AnchorPosition.TOP_LEFT, new GuiVector(colX, 0));

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

                    MPGuiLinearPanel groupPanel = getElementCache().getOrCreate(
                            "group_panel_" + idx, MPGuiLinearPanel.class,
                            () -> new MPGuiLinearPanel(new GuiShape(0, 0, colWidth, groupH), LinearOrientation.VERTICAL),
                            t -> {
                                t.setElementShape(t.getElementShape().withHeight(groupH));
                                t.getChildren().clear();
                            }
                    );
                    columnPanel.addChild(groupPanel, new GuiMargin(0, 0, 8f, 8f), null);

                    MPGuiAnchorPanel titlePanel = getElementCache().getOrCreate(
                            "title_panel_" + idx, MPGuiAnchorPanel.class,
                            () -> new MPGuiAnchorPanel(new GuiShape(0, 0, colWidth, 12)),
                            t -> t.getChildren().clear()
                    );
                    groupPanel.addChild(titlePanel, new GuiMargin(0, 3f), null);

                    MPGuiStaticLabel groupTitle = getElementCache().getOrCreate(
                            "group_title_" + idx, MPGuiStaticLabel.class,
                            () -> new MPGuiStaticLabel(MPGuiString.localized(groupLabelKey.$()), fontRenderer,
                                    new GuiShape(0, 0, colWidth - 15, 10), 14737632, fontSize),
                            null, t -> t.setGuiString(MPGuiString.localized(groupLabelKey.$())), null
                    );
                    titlePanel.addChild(groupTitle, null, AnchorPosition.MIDDLE_LEFT, null);

                    MPGuiCheckButton selectAll = getElementCache().getOrCreate(
                            "select_all_" + idx, MPGuiCheckButton.class,
                            () -> new MPGuiCheckButton(
                                    MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.button.select_all"), fontRenderer,
                                    new GuiShape(0, 0, 8, 8), TEXTURES, TEXTURES_SIZE,
                                    new GuiShape(240, 0, 8, 8), fontSize, e -> { }
                            )
                    );
                    titlePanel.addChild(selectAll, null, AnchorPosition.TOP_RIGHT, null);

                    MPGuiGridPanel coinsGrid = getElementCache().getOrCreate(
                            "coins_grid_" + idx, MPGuiGridPanel.class,
                            () -> new MPGuiGridPanel(new GuiShape(0, 0, colWidth, rowsNum.$() * coinH), rowsNum.$(), slot_count_x),
                            t -> t.setGaps(0, CELL_GAP),
                            t -> {
                                t.setGridSize(rowsNum.$(), slot_count_x);
                                t.setElementShape(t.getElementShape().withHeight(rowsNum.$() * coinH));
                                t.getChildren().clear();
                            }
                    );
                    groupPanel.addChild(coinsGrid, new GuiMargin(2, 0, 0, 0), null);

                    int slotIndex = 0;
                    for (CoinValue coinValue : groupSlots.getValue()) {
                        if (slotIndex >= 32) break;
                        int gridRow = slotIndex / slot_count_x;
                        int gridCol = slotIndex % slot_count_x;

                        WalletCoinButton coinBtn = getElementCache().getOrCreate(
                                "coin_btn_" + idx + "_" + slotIndex, WalletCoinButton.class,
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
        super.initGui();
    }

    private MPGuiDefaultButton createDynamicButton(String key, String text, MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<MPGuiDefaultButton>> onClick) {
        return getElementCache().getOrCreate(
                key, MPGuiDefaultButton.class,
                () -> new MPGuiDefaultButton(
                        MPGuiString.simple(text),
                        new GuiShape(0, 0, 10, 13.0f), TEXTURES, TEXTURES_SIZE,
                        new GuiShape(80, 200, 10, 10), fontSize, onClick
                ),
                t -> t.setScaleRules(new GuiScaleRules(GuiScaleType.PARENT_HORIZONTAL))
        );
    }

    private MPGuiFreePanel createSpacer(String key, float width) {
        return getElementCache().getOrCreate(
                key, MPGuiFreePanel.class,
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