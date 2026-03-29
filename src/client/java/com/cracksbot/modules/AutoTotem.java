package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ClickType;
import org.lwjgl.glfw.GLFW;

public class AutoTotem extends HackModule {
    private final BoolSetting searchInventory = addSetting(new BoolSetting("SearchInventory", true));
    private final BoolSetting preferOffhand   = addSetting(new BoolSetting("PreferOffhand",   true));

    public AutoTotem() {
        super("AutoTotem", "Instantly re-equip totem when current one pops", ModuleCategory.COMBAT, GLFW.GLFW_KEY_T, true);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;
        LocalPlayer p = mc.player;

        // Already has totem in offhand
        if (p.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;

        // Search range: hotbar only (0-8) or full inventory (0-35)
        int limit = searchInventory.getValue() ? 36 : 9;
        int totemSlot = -1;
        for (int i = 0; i < limit; i++) {
            if (p.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                totemSlot = i;
                break;
            }
        }
        if (totemSlot == -1) return;

        // Map hotbar slot (0-8) to container slot
        int containerSlot = totemSlot < 9 ? totemSlot + 36 : totemSlot;

        // Pick up totem
        mc.gameMode.handleInventoryMouseClick(
            p.containerMenu.containerId, containerSlot, 0, ClickType.PICKUP, p
        );
        // Place in offhand (slot 45) or hotbar slot based on setting
        if (preferOffhand.getValue()) {
            mc.gameMode.handleInventoryMouseClick(
                p.containerMenu.containerId, 45, 0, ClickType.PICKUP, p
            );
            // Return any displaced item to original slot
            if (!p.containerMenu.getCarried().isEmpty()) {
                mc.gameMode.handleInventoryMouseClick(
                    p.containerMenu.containerId, containerSlot, 0, ClickType.PICKUP, p
                );
            }
        } else {
            // Just put it back in hotbar slot 0
            mc.gameMode.handleInventoryMouseClick(
                p.containerMenu.containerId, 36, 0, ClickType.PICKUP, p
            );
        }
    }
}
