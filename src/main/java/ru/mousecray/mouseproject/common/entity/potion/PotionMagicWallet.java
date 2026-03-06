package ru.mousecray.mouseproject.common.entity.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.mousecray.mouseproject.Tags;

import javax.annotation.Nonnull;
import java.util.List;

public class PotionMagicWallet extends MPDefaultPotion {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/gui/potion_effects.png");
    private final        int              textureIndex;

    public PotionMagicWallet(String name, int color, int textureIndex) {
        super(name, false, color);
        this.textureIndex = textureIndex;
        setBeneficial();
    }

    @Override
    protected void onAddEffectToEntity(@Nonnull World world, @Nonnull EntityLivingBase entity, PotionEffect effect) {
        if (world.isRemote) return;

        if (entity instanceof EntityPlayer) {
            List<EntityPlayer> list = world.getPlayers(EntityPlayer.class, p -> p.getDistanceSq(entity.posX, entity.posY, entity.posZ) <= 10000);
            for (EntityPlayer player : list) {
                player.sendMessage(new TextComponentTranslation("message." + Tags.MOD_ID + ".wallet.get_legendary_effect", entity.getDisplayName()));
            }
            world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(@Nonnull PotionEffect effect, Gui gui, int x, int y, float z) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(TEXTURE);
        int textureStart = textureIndex * 18;
        int size         = textureStart + 18;
        gui.drawTexturedModalRect(x, y, textureStart, textureStart, size, size);
    }

    @Override @SideOnly(Side.CLIENT)
    public void renderHUDEffect(@Nonnull PotionEffect effect, Gui gui, int x, int y, float z, float alpha) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(TEXTURE);
        int textureStart = textureIndex * 18;
        int size         = textureStart + 18;
        gui.drawTexturedModalRect(x, y, textureStart, textureStart, size, size);
    }
}