package com.cracksbot.modules;

import com.cracksbot.settings.BoolSetting;
import com.cracksbot.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class AutoEat extends HackModule {
    private final NumberSetting threshold = addSetting(new NumberSetting("Hunger", 14, 1, 19, 1));
    private final BoolSetting bestFood = addSetting(new BoolSetting("BestFood", true));
    private final BoolSetting eatInCombat = addSetting(new BoolSetting("EatInCombat", true));
    private int savedSlot = -1;
    private int cooldown = 0;

    public AutoEat() {
        super("AutoEat", "Auto-eats best food when hunger is low", ModuleCategory.PLAYER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;
        if (cooldown > 0) { cooldown--; return; }

        LocalPlayer p = mc.player;
        if (p.getFoodData().getFoodLevel() >= threshold.getValue()) return;
        if (!eatInCombat.getValue() && p.getCombatTracker().getCombatDuration() > 0) return;

        int bestSlot = findBestFood(p);
        if (bestSlot == -1) return;

        savedSlot = p.getInventory().getSelectedSlot();
        p.getInventory().setSelectedSlot(bestSlot);
        mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
        cooldown = 32;
    }

    private int findBestFood(LocalPlayer p) {
        int bestSlot = -1;
        float bestScore = -1f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food == null) continue;

            float score = bestFood.getValue()
                ? food.nutrition() + food.saturation() * 2f
                : food.nutrition();

            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}
