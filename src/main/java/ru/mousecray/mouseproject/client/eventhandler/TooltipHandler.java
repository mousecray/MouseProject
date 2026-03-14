/*
 * Copyright © 2026 mousecray
 * Licensed under the GNU Lesser General Public License, Version 3.0
 */

package ru.mousecray.mouseproject.client.eventhandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.common.item.wallet.IWallet;

import java.util.*;

public class TooltipHandler {

    //Сохраняем индексы и оригинальные строки
    private static final Map<Integer, String> savedTooltipLines = new HashMap<>();

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTooltipPre(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof IWallet)) return;

        //Сохраняем строки с плейсхолдерами и заменяем их на пустые
        savedTooltipLines.clear();
        List<String> lines = event.getToolTip();
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            if (line.contains("@")) {
                savedTooltipLines.put(i, line); //Сохраняем оригинал
                lines.set(i, "·"); //Заменяем на маркер
                lines.add(i + 1, ""); //Заменяем на пустую строку
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTooltipPost(RenderTooltipEvent.PostText event) {
        Minecraft mc    = Minecraft.getMinecraft();
        ItemStack stack = event.getStack();
        if (!(stack.getItem() instanceof IWallet) || savedTooltipLines.isEmpty()) return;

        //Актуальные строки после разбиения
        List<String> currentLines = event.getLines();
        int          baseY        = event.getY() + 2;
        int          lineHeight   = event.getFontRenderer().FONT_HEIGHT; //Базовая высота строки
        int          lineSpacing  = 1; //Примерное межстрочное расстояние в тултипе
        int          x            = event.getX() - 1;

        for (Map.Entry<Integer, String> entry : savedTooltipLines.entrySet()) {
            int    originalIndex = entry.getKey(); //Оригинальный индекс
            String line          = entry.getValue();

            //Ищем строку с нашим маркером
            int currentIndex = -1;
            for (int i = 0; i < currentLines.size(); i++) {
                if (currentLines.get(i).contains("·") && i >= originalIndex) { //Ищем маркер
                    currentIndex = i;
                    break;
                }
            }
            if (currentIndex == -1) continue; //Если не нашли, пропускаем

            List<ItemStack> stackToRender = new ArrayList<>();
            List<String>    textParts     = new ArrayList<>(); //Список текстовых частей

            //Парсим плейсхолдеры @<resource>@ и текст между ними
            int lastIndex = 0;
            int indexOfAt = line.indexOf("@");
            while (indexOfAt != -1) {
                int end = line.indexOf("@", indexOfAt + 1);
                if (end != -1) {
                    //Добавляем текст перед плейсхолдером
                    if (indexOfAt > lastIndex) textParts.add(line.substring(lastIndex, indexOfAt).trim());
                    ResourceLocation resource = new ResourceLocation(line.substring(indexOfAt + 1, end));
                    Item             item     = Item.REGISTRY.getObject(resource);
                    if (item != null) stackToRender.add(new ItemStack(item));
                    lastIndex = end + 1; //Обновляем позицию после плейсхолдера
                    indexOfAt = line.indexOf("@", end + 1);
                } else break;
            }
            //Добавляем остаток строки после последнего плейсхолдера
            if (lastIndex < line.length()) textParts.add(line.substring(lastIndex).trim());

            // Переворачиваем списки для обратного порядка
            Collections.reverse(stackToRender);
            Collections.reverse(textParts);

            int maxPerLine = 5; //Максимум 5 элементов на строку

            int offsetX       = x; //Начальная координата X
            int currentLine   = 0;
            int itemCount     = stackToRender.size();
            int textCount     = textParts.size();
            int totalElements = Math.max(itemCount, textCount);


            //Рендерим иконки и текст
            for (int i = 0; i < totalElements; ++i) {
                int lineIndex = i / maxPerLine; //Номер текущей строки
                if (lineIndex > currentLine) {
                    offsetX = x; //Сбрасываем X на начало строки
                    currentLine = lineIndex; //Обновляем номер строки
                }

                //Вычисляем Y-позицию с учётом всех предыдущих строк
                int adjustedY = baseY + (currentIndex * (lineHeight + lineSpacing)) + (lineIndex * (lineHeight + lineSpacing));

                if (i < itemCount) {
                    ItemStack itemStack = stackToRender.get(i);
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.5f, 0.5f, 0.5f);
                    int scaledX = (int) (offsetX * 2.0f);
                    int scaledY = (int) (adjustedY * 2.0f);
                    mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, scaledX, scaledY);
                    GlStateManager.popMatrix();
                    offsetX += 8; //Сдвиг на половину ширины иконки
                }

                if (i < textCount) {
                    String text = textParts.get(i);
                    if (!text.isEmpty()) {
                        offsetX += 2;
                        int textWidth = mc.fontRenderer.getStringWidth(text);
                        mc.fontRenderer.drawStringWithShadow(
                                text,
                                offsetX,
                                adjustedY, //Выравнивание по базовой линии
                                0xFFFFFF
                        );
                        offsetX += textWidth + 4; //Сдвигаем на ширину текста + небольшой отступ
                    }
                }
            }
        }

        //Очищаем после рендера
        savedTooltipLines.clear();
    }
}