package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class AutoEXP extends HackModule {
    private final NumberSetting durability = addSetting(new NumberSetting("MinDura%", 50, 10, 90, 5));

    public AutoEXP() {
        super("AutoEXP", "Auto repair armor with XP", ModuleCategory.PLAYER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;
        LocalPlayer p = mc.player;

        boolean needRepair = false;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = p.getItemBySlot(slot);
            if (armor.isEmpty() || !armor.isDamaged()) continue;
            double pct = (double)(armor.getMaxDamage() - armor.getDamageValue()) / armor.getMaxDamage() * 100;
            if (pct < durability.getValue()) { needRepair = true; break; }
        }
        if (!needRepair) return;

        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getItem(i).is(Items.EXPERIENCE_BOTTLE)) {
                int prev = p.getInventory().getSelectedSlot();
                p.getInventory().setSelectedSlot(i);
                mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
                p.getInventory().setSelectedSlot(prev);
                return;
            }
        }
    }
}
