package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ClickType;
import org.lwjgl.glfw.GLFW;

public class DoubleHand extends HackModule {
    public DoubleHand() {
        super("DoubleHand", "Crystal + Totem management", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;
        LocalPlayer p = mc.player;

        // Keep totem in offhand, crystal in mainhand
        if (!p.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            for (int i = 0; i < 36; i++) {
                if (p.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                    int slot = i < 9 ? i + 36 : i;
                    mc.gameMode.handleInventoryMouseClick(p.containerMenu.containerId, slot, 0, ClickType.PICKUP, p);
                    mc.gameMode.handleInventoryMouseClick(p.containerMenu.containerId, 45, 0, ClickType.PICKUP, p);
                    break;
                }
            }
        }

        if (!p.getMainHandItem().is(Items.END_CRYSTAL)) {
            for (int i = 0; i < 9; i++) {
                if (p.getInventory().getItem(i).is(Items.END_CRYSTAL)) {
                    p.getInventory().setSelectedSlot(i);
                    break;
                }
            }
        }
    }
}
