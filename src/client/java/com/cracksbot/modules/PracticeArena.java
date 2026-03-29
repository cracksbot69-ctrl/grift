package com.cracksbot.modules;

import com.cracksbot.CracksBotClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class PracticeArena extends HackModule {
    private boolean built = false;

    public PracticeArena() {
        super("PracticeArena", "Build crystal PVP arena", ModuleCategory.MISC, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onEnable() {
        built = false;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || built) return;
        LocalPlayer p = mc.player;

        // Need to be in creative or have cheats
        int bx = (int) p.getX();
        int by = 64;
        int bz = (int) p.getZ();

        // Build obsidian platform
        sendCmd(p, "fill " + (bx-10) + " " + by + " " + (bz-10) + " " + (bx+10) + " " + by + " " + (bz+10) + " obsidian");
        // Clear above
        sendCmd(p, "fill " + (bx-10) + " " + (by+1) + " " + (bz-10) + " " + (bx+10) + " " + (by+5) + " " + (bz+10) + " air");
        // Walls
        sendCmd(p, "fill " + (bx-11) + " " + (by+1) + " " + (bz-11) + " " + (bx+11) + " " + (by+4) + " " + (bz-11) + " obsidian");
        sendCmd(p, "fill " + (bx-11) + " " + (by+1) + " " + (bz+11) + " " + (bx+11) + " " + (by+4) + " " + (bz+11) + " obsidian");
        sendCmd(p, "fill " + (bx-11) + " " + (by+1) + " " + (bz-11) + " " + (bx-11) + " " + (by+4) + " " + (bz+11) + " obsidian");
        sendCmd(p, "fill " + (bx+11) + " " + (by+1) + " " + (bz-11) + " " + (bx+11) + " " + (by+4) + " " + (bz+11) + " obsidian");

        // Give crystal PVP kit
        sendCmd(p, "gamemode survival");
        sendCmd(p, "give @s end_crystal 64");
        sendCmd(p, "give @s obsidian 64");
        sendCmd(p, "give @s totem_of_undying 5");
        sendCmd(p, "give @s enchanted_golden_apple 16");
        sendCmd(p, "give @s experience_bottle 64");
        sendCmd(p, "give @s netherite_sword{Enchantments:[{id:sharpness,lvl:5}]}");
        sendCmd(p, "give @s netherite_pickaxe{Enchantments:[{id:efficiency,lvl:5}]}");
        sendCmd(p, "give @s netherite_helmet{Enchantments:[{id:protection,lvl:4},{id:blast_protection,lvl:4}]}");
        sendCmd(p, "give @s netherite_chestplate{Enchantments:[{id:protection,lvl:4},{id:blast_protection,lvl:4}]}");
        sendCmd(p, "give @s netherite_leggings{Enchantments:[{id:protection,lvl:4},{id:blast_protection,lvl:4}]}");
        sendCmd(p, "give @s netherite_boots{Enchantments:[{id:protection,lvl:4},{id:blast_protection,lvl:4}]}");
        sendCmd(p, "give @s respawn_anchor 16");
        sendCmd(p, "give @s glowstone 64");
        sendCmd(p, "give @s cobweb 32");

        // TP to arena
        sendCmd(p, "tp @s " + bx + " " + (by+1) + " " + bz);

        // Difficulty
        sendCmd(p, "difficulty hard");

        p.displayClientMessage(Component.literal("\u00A75[CracksBot] \u00A7aCrystal PVP Arena gebaut! Kit gegeben!"), false);
        p.displayClientMessage(Component.literal("\u00A75[CracksBot] \u00A7eSummon bot: /summon player oder /summon zombie"), false);

        built = true;
        toggle(); // auto-disable
    }

    private void sendCmd(LocalPlayer p, String cmd) {
        if (p.connection != null) {
            p.connection.sendCommand(cmd);
        }
    }
}
