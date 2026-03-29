package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class TotemPopCounter extends HackModule {
    private final Map<String, Float> lastHealth = new HashMap<>();
    private final Map<String, Integer> popCount = new HashMap<>();

    public TotemPopCounter() {
        super("PopCounter", "Count totem pops", ModuleCategory.MISC, GLFW.GLFW_KEY_UNKNOWN, true);
    }

    @Override
    public void onEnable() {
        lastHealth.clear();
        popCount.clear();
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            if (!player.isAlive()) {
                // Player died, announce total pops
                String name = player.getName().getString();
                if (popCount.containsKey(name) && popCount.get(name) > 0) {
                    mc.player.displayClientMessage(
                        Component.literal("\u00A75[CracksBot] \u00A7c" + name + " \u00A7fdied after \u00A7e" + popCount.get(name) + " \u00A7fpops!"),
                        false
                    );
                    popCount.remove(name);
                }
                lastHealth.remove(name);
                continue;
            }

            String name = player.getName().getString();
            float hp = player.getHealth();
            Float prevHp = lastHealth.get(name);

            // Detect totem pop: health was very low, then jumped back up
            if (prevHp != null && prevHp <= 2.0f && hp > prevHp + 5.0f) {
                int pops = popCount.getOrDefault(name, 0) + 1;
                popCount.put(name, pops);
                mc.player.displayClientMessage(
                    Component.literal("\u00A75[CracksBot] \u00A7f" + name + " \u00A7epopped totem \u00A7c#" + pops),
                    false
                );
            }

            lastHealth.put(name, hp);
        }
    }
}
