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
import ru.mousecray.mouseproject.client.gui.impl.container.MPGuiSimplePanel;
import ru.mousecray.mouseproject.client.gui.impl.container.MPGuiSimpleScrollPanel;
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
        super.initGui();
        resetGui();
        Keyboard.enableRepeatEvents(true);

        long maxCoinValue = 1_000_000;

        MPFontSize fontSize = MPFontSize.NORMAL;

        // Close Button
        MPGuiCloseButton closeButton = getElementCache().get("close_button", MPGuiCloseButton.class);
        if (closeButton == null) {
            closeButton = new MPGuiCloseButton(
                    new GuiShape(0, 0, 9, 9),
                    TEXTURES, TEXTURES_SIZE, new GuiShape(95, 200, 9, 9), fontSize,
                    event -> closeGui()
            );
            getElementCache().put("close_button", closeButton);
        }
        addButton(closeButton, null, AnchorPosition.TOP_RIGHT, GuiVector.ZERO);

        //Заглушка, проверяем, открывать ли гуи
        if (walletPipe == null) return;

        // Title

        MPGuiStaticLabel titleLabel = getElementCache().getOrCreate(
                "title_label", MPGuiStaticLabel.class,
                () -> new MPGuiStaticLabel(MPGuiString.simple(walletStack.getDisplayName()), fontRenderer,
                        new GuiShape(0, 0, 80, 10),
                        14737632, fontSize
                ),
                t -> t.setGuiString(MPGuiString.simple(walletStack.getDisplayName()))
        );
        addLabel(titleLabel, null, AnchorPosition.TOP_LEFT, GuiVector.ZERO);

        float buttonTakePutYSize = 13.0f;
        float buttonTakePutXSize = 17.2f;
        float buttonTakePutGap   = 10f;

        MPGuiActionButton takeAction = getElementCache().getOrCreate(
                "take_action", MPGuiActionButton.class,
                () -> new MPGuiActionButton(MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.button.take"),
                        new GuiShape(0, 0, 113.8f, 12),
                        TEXTURES, TEXTURES_SIZE, new GuiShape(0, 200, 80, 10), fontSize,
                        event -> { }
                ),
                t -> t.applyState(GuiButtonPersistentState.DISABLED)
        );

        MPGuiActionButton putAction = getElementCache().getOrCreate(
                "put_action", MPGuiActionButton.class,
                () -> new MPGuiActionButton(MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.button.put"),
                        new GuiShape(0, 0, 113.8f, 12),
                        TEXTURES, TEXTURES_SIZE, new GuiShape(0, 200, 80, 10), fontSize,
                        event -> { }
                ),
                t -> t.applyState(GuiButtonPersistentState.DISABLED)
        );


        Consumer<MPGuiTextTypedEvent<MPGuiNumberField>> fieldEventTake = event -> {
            String newText = event.getNewText();
            if (newText == null || newText.length() > 19) {
                event.setCancelled(true);
                takeAction.applyState(GuiButtonPersistentState.DISABLED);
                putAction.applyState(GuiButtonPersistentState.DISABLED);
                return;
            }
            if (newText.trim().isEmpty()) {
                takeAction.applyState(GuiButtonPersistentState.DISABLED);
                putAction.applyState(GuiButtonPersistentState.DISABLED);
                return;
            }
            long l;
            try {
                l = Long.parseLong(newText);
            } catch (NumberFormatException ignore) {
                event.setCancelled(true);
                takeAction.applyState(GuiButtonPersistentState.DISABLED);
                putAction.applyState(GuiButtonPersistentState.DISABLED);
                return;
            }
            if (l <= 0) {
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
                        new GuiShape(0, 0, 113.5f, buttonTakePutYSize * 1.2f),
                        TEXTURES, TEXTURES_SIZE, new GuiShape(104, 200, 80, 10), fontSize, fieldEventTake
                ),
                t -> t.applyState(GuiButtonPersistentState.DISABLED)
        );

        // Панель управления (левая часть)
        MPGuiSimplePanel controls = getElementCache().getOrCreate(
                "controls_panel", MPGuiSimplePanel.class,
                () -> new MPGuiSimplePanel(new GuiShape(0, 0, 114, 67)),
                t -> t.setLayoutType(LayoutType.LINEAR_VERTICAL),
                t -> t.getChildren().clear() // Clear old children to rebuild tree
        );
        addPanel(controls, null, AnchorPosition.TOP_LEFT, new GuiVector(0, 133));

        // Ряд кнопок +1, +10, +50, -1, -10, -50
        MPGuiSimplePanel row1 = getElementCache().getOrCreate(
                "row1_panel", MPGuiSimplePanel.class,
                () -> new MPGuiSimplePanel(new GuiShape(0, 0, 114, buttonTakePutYSize)),
                t -> t.setLayoutType(LayoutType.LINEAR_HORIZONTAL),
                t -> {
                    t.getChildren().clear();
                    t.addChild(createSimpleButton("btn_+1", "+1", fontSize, event -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() + 1, 1))), null, null, null);
                    t.addChild(createSimpleButton("btn_+10", "+10", fontSize, event -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() + 10, 1))), null, null, null);
                    t.addChild(createSimpleButton("btn_+50", "+50", fontSize, event -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() + 50, 1))), null, null, null);
                    t.addChild(createSpacer("row1_spacer", buttonTakePutGap), null, null, null);
                    t.addChild(createSimpleButton("btn_-1", "-1", fontSize, event -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() - 1, 1))), null, null, null);
                    t.addChild(createSimpleButton("btn_-10", "-10", fontSize, event -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() - 10, 1))), null, null, null);
                    t.addChild(createSimpleButton("btn_-50", "-50", fontSize, event -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() - 50, 1))), null, null, null);
                }
        );
        controls.addChild(row1, null, null, null);

        // Ряд кнопок +100, +500, +1K, -100, -500, -1K
        MPGuiSimplePanel row2 = getElementCache().getOrCreate(
                "row2_panel", MPGuiSimplePanel.class,
                () -> new MPGuiSimplePanel(new GuiShape(0, 0, 114, buttonTakePutYSize)),
                t -> t.setLayoutType(LayoutType.LINEAR_HORIZONTAL),
                t -> {
                    t.getChildren().clear();
                    t.addChild(createSimpleButton("btn_+100", "+100", fontSize, event -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() + 100, 1))), null, null, null);
                    t.addChild(createSimpleButton("btn_+500", "+500", fontSize, event -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() + 500, 1))), null, null, null);
                    t.addChild(createSimpleButton("btn_+1K", "+1K", fontSize, event -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() + 1000, 1))), null, null, null);
                    t.addChild(createSpacer("row2_spacer", buttonTakePutGap), null, null, null);
                    t.addChild(createSimpleButton("btn_-100", "-100", fontSize, event -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() - 100, 1))), null, null, null);
                    t.addChild(createSimpleButton("btn_-500", "-500", fontSize, event -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() - 500, 1))), null, null, null);
                    t.addChild(createSimpleButton("btn_-1K", "-1K", fontSize, event -> fieldTakePut.setNumberText(Math.max(fieldTakePut.getNumberText() - 1000, 1))), null, null, null);
                }
        );
        controls.addChild(row2, null, null, null);

        // Поле и слайдер
        MPGuiSimplePanel fieldSliderStack = getElementCache().getOrCreate(
                "field_slider_stack", MPGuiSimplePanel.class,
                () -> new MPGuiSimplePanel(new GuiShape(0, 0, 114, 18)),
                t -> t.setLayoutType(LayoutType.FREE),
                t -> t.getChildren().clear()
        );
        controls.addChild(fieldSliderStack, null, null, null);

        fieldSliderStack.addChild(fieldTakePut, null, AnchorPosition.TOP_LEFT, null);

        class WalletSlider extends MPGuiSlider<WalletSlider> {
            public WalletSlider() {
                super(new GuiShape(0, 0, 113.8f, 11),
                        MPGuiTexturePack.Builder.create(TEXTURES, TEXTURES_SIZE, new GuiVector(0, 200), new GuiVector(80, 10)).build(),
                        MPGuiTexturePack.Builder.create(TEXTURES, TEXTURES_SIZE, new GuiVector(90, 200), new GuiVector(5, 7)).build(),
                        new GuiVector(6.68f, 11),
                        0, 100,
                        false);
            }
        }
        WalletSlider slider = getElementCache().getOrCreate(
                "wallet_slider", WalletSlider.class,
                WalletSlider::new,
                t -> t.onChange(value -> fieldTakePut.setNumberText(value == 0 ? 1 : (long) value * maxCoinValue / 100))
        );
        fieldSliderStack.addChild(slider, null, AnchorPosition.TOP_LEFT, new GuiVector(0, buttonTakePutYSize * 1.2f - 9));

        controls.addChild(takeAction, null, null, null);
        controls.addChild(putAction, null, null, null);

        float W            = 14.9f;
        float H            = 23f;
        int   slot_count_x = 7;
        float ROW_WIDTH    = slot_count_x * W;
        float baseX        = 2;
        float baseY        = 12;
        float COLUMN_GAP   = 5f;

        Map<Integer, List<CoinValue>> activeGroups = new HashMap<>();

        CoinValue       bronzeBal   = walletPipe.loadBronzeBalance();
        List<CoinValue> normalSlots = new ArrayList<>();
        if (bronzeBal != null && bronzeBal.getValue() > 0) {
            EnumMap<NormalCoinType, Long> displayCoins = CoinHelper.getDisplayCoins(
                    CoinHelper.getMaxCoin(bronzeBal.getValue()),
                    bronzeBal.getValue()
            );
            normalSlots = displayCoins.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .map(e -> CoinValue.create(e.getValue(), e.getKey()))
                    .collect(Collectors.toList());
        }
        if (!normalSlots.isEmpty()) activeGroups.put(0, normalSlots);

        List<CoinType> coinTypes = walletPipe.loadAllBalanceTypes();
        List<CoinValue> resourceSlots = coinTypes.stream()
                .filter(type -> type instanceof ResourceCoinType)
                .map(walletPipe::loadResourceBalance)
                .filter(cv -> cv != null && cv.getValue() > 0)
                .collect(Collectors.toList());
        if (!resourceSlots.isEmpty()) activeGroups.put(1, resourceSlots);

        List<CoinValue> specificSlots = coinTypes.stream()
                .filter(type -> type instanceof SpecificCoinType)
                .map(walletPipe::loadSpecificBalance)
                .filter(cv -> cv != null && cv.getValue() > 0)
                .collect(Collectors.toList());
        if (!specificSlots.isEmpty()) activeGroups.put(2, specificSlots);

        List<CoinValue> otherSlots = coinTypes.stream()
                .filter(type -> !(type instanceof NormalCoinType) &&
                        !(type instanceof ResourceCoinType) &&
                        !(type instanceof SpecificCoinType))
                .map(walletPipe::loadOtherBalance)
                .filter(cv -> cv != null && cv.getValue() > 0)
                .collect(Collectors.toList());
        if (!otherSlots.isEmpty()) activeGroups.put(3, otherSlots);

        // Контейнер для монет (правая часть)
        MPGuiSimpleScrollPanel coinsContainer = getElementCache().getOrCreate(
                "coins_container", MPGuiSimpleScrollPanel.class,
                () -> new MPGuiSimpleScrollPanel(new GuiShape(0, 0, 115, 188)),
                t -> t.setLayoutType(LayoutType.FREE),
                t -> t.getChildren().clear()
        );
        addPanel(coinsContainer, null, AnchorPosition.TOP_RIGHT, new GuiVector(0, 12));

        if (activeGroups.isEmpty()) {
            MPGuiStaticLabel emptyLabel = getElementCache().getOrCreate(
                    "empty_label", MPGuiStaticLabel.class,
                    () -> new MPGuiStaticLabel(MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.label.empty"),
                            fontRenderer, new GuiShape(0, 0, 80, 10), 14737632, fontSize)
            );
            coinsContainer.addChild(emptyLabel, null, AnchorPosition.TOP_LEFT, new GuiVector(baseX, 0));
        } else {
            List<Map.Entry<Integer, List<CoinValue>>> groupsList = new ArrayList<>(activeGroups.entrySet());

            for (int col = 0; col < 2; col++) {
                String colKey = "col_panel_" + col;
                float  colX   = col * (ROW_WIDTH + COLUMN_GAP + 5);
                MPGuiSimplePanel columnPanel = getElementCache().getOrCreate(
                        colKey, MPGuiSimplePanel.class,
                        () -> new MPGuiSimplePanel(new GuiShape(colX, 0, ROW_WIDTH + 5, 188)),
                        t -> t.setLayoutType(LayoutType.LINEAR_VERTICAL),
                        t -> t.setElementShape(t.getElementShape().withX(colX)),
                        t -> t.getChildren().clear()
                );
                coinsContainer.addChild(columnPanel, null, null, null);

                for (int i = 0; i < 2; i++) {
                    int idx = col * 2 + i;
                    if (idx >= groupsList.size()) break;

                    Map.Entry<Integer, List<CoinValue>> groupSlots = groupsList.get(idx);

                    String groupLabelKey;
                    switch (groupSlots.getKey()) {
                        case 0:
                            groupLabelKey = "gui." + Tags.MOD_ID + ".wallet.label.normal";
                            break;
                        case 1:
                            groupLabelKey = "gui." + Tags.MOD_ID + ".wallet.label.resource";
                            break;
                        case 2:
                            groupLabelKey = "gui." + Tags.MOD_ID + ".wallet.label.specific";
                            break;
                        default:
                            groupLabelKey = "gui." + Tags.MOD_ID + ".wallet.label.other";
                            break;
                    }

                    int   rows   = (int) Math.ceil(groupSlots.getValue().size() / (double) slot_count_x);
                    float groupH = 12 + rows * H;

                    String groupPanelKey = "group_panel_" + idx;
                    MPGuiSimplePanel groupPanel = getElementCache().getOrCreate(
                            groupPanelKey, MPGuiSimplePanel.class,
                            () -> new MPGuiSimplePanel(new GuiShape(0, 0, ROW_WIDTH + 2, groupH)),
                            t -> t.setLayoutType(LayoutType.FREE),
                            t -> t.setElementShape(t.getElementShape().withHeight(groupH)),
                            t -> t.getChildren().clear()
                    );
                    columnPanel.addChild(groupPanel, new GuiMargin(0, 0, 0, 10), null, null);

                    String groupTitleKey = "group_title_" + idx;
                    MPGuiStaticLabel groupTitle = getElementCache().getOrCreate(
                            groupTitleKey, MPGuiStaticLabel.class,
                            () -> new MPGuiStaticLabel(MPGuiString.localized(groupLabelKey), fontRenderer,
                                    new GuiShape(0, 0, ROW_WIDTH - 10, 10), 14737632, fontSize),
                            null,
                            t -> t.setGuiString(MPGuiString.localized(groupLabelKey)),
                            null
                    );
                    groupPanel.addChild(groupTitle, null, AnchorPosition.TOP_LEFT, null);

                    String selectAllKey = "select_all_" + idx;
                    MPGuiCheckButton selectAll = getElementCache().getOrCreate(
                            selectAllKey, MPGuiCheckButton.class,
                            () -> new MPGuiCheckButton(
                                    MPGuiString.localized("gui." + Tags.MOD_ID + ".wallet.button.select_all"), fontRenderer,
                                    new GuiShape(0, 0, 8, 8), TEXTURES, TEXTURES_SIZE,
                                    new GuiShape(240, 0, 8, 8), fontSize,
                                    e -> { }
                            )
                    );
                    groupPanel.addChild(selectAll, null, AnchorPosition.TOP_RIGHT, null);

                    int slotIndex = 0;
                    for (CoinValue coinValue : groupSlots.getValue()) {
                        if (slotIndex >= 32) break;
                        int row      = slotIndex / slot_count_x;
                        int colInRow = slotIndex % slot_count_x;

                        String coinBtnKey = "coin_btn_" + idx + "_" + slotIndex;
                        WalletCoinButton coinBtn = getElementCache().getOrCreate(
                                coinBtnKey, WalletCoinButton.class,
                                () -> new WalletCoinButton(
                                        new GuiShape(colInRow * W, 11 + row * H, W, H),
                                        fontSize, coinValue, e -> { }
                                ),
                                null,
                                t -> {
                                    t.setElementShape(t.getElementShape().withX(colInRow * W).withY(11 + row * H));
                                    t.setCount(coinValue);
                                },
                                null
                        );
                        groupPanel.addChild(coinBtn, null, AnchorPosition.TOP_LEFT, null);
                        slotIndex++;
                    }
                }
            }
        }
    }

    private MPGuiDefaultButton createSimpleButton(String key, String text, MPFontSize fontSize, Consumer<MPGuiMouseClickEvent<MPGuiDefaultButton>> onClick) {
        return getElementCache().getOrCreate(
                key, MPGuiDefaultButton.class,
                () -> new MPGuiDefaultButton(
                        MPGuiString.simple(text),
                        new GuiShape(0, 0, 17.2f, 13.0f), TEXTURES, TEXTURES_SIZE,
                        new GuiShape(80, 200, 10, 10), fontSize, onClick
                )

        );
    }

    private MPGuiSimplePanel createSpacer(String key, float width) {
        return getElementCache().getOrCreate(
                key, MPGuiSimplePanel.class,
                () -> new MPGuiSimplePanel(new GuiShape(0, 0, width, 1))
        );
    }

    private void sendWalletToServer() {

    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }
}
