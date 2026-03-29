package com.cracksbot.hud;

import com.cracksbot.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModuleHud {

    public static void render(GuiGraphics gfx, List<HackModule> modules) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        // Watermark top-left
        gfx.drawString(mc.font, "\u00A7bGrift \u00A78v" + com.cracksbot.CracksBotClient.VERSION, 4, 4, 0xFFFFFFFF, true);

        // ArrayList top-right (Meteor style - sorted by width, waterfall effect)
        List<HackModule> enabled = new ArrayList<>();
        for (HackModule m : modules) {
            if (m.isEnabled()) enabled.add(m);
        }
        enabled.sort(Comparator.comparingInt((HackModule m) -> mc.font.width(m.getName())).reversed());

        int y = 2;
        for (int i = 0; i < enabled.size(); i++) {
            HackModule m = enabled.get(i);
            String name = m.getName();
            // Category label prefix (short, dimmed)
            String cat   = m.getModuleCategory().displayName.substring(0, 2).toUpperCase();
            String label = "\u00A78[" + cat + "] \u00A7r" + name;
            int textW = mc.font.width(label);
            int x = screenW - textW - 4;

            // Background bar
            gfx.fill(x - 2, y, screenW, y + 11, 0xA0101020);
            // Right accent bar (category color)
            gfx.fill(screenW - 1, y, screenW, y + 11, m.getModuleCategory().color);
            // Rainbow name color
            int hue = (int) ((System.currentTimeMillis() / 10 + i * 16) % 360);
            int color = hsbToRgb(hue, 0.6f, 1.0f);
            gfx.drawString(mc.font, label, x, y + 1, color, false);

            y += 11;
        }

        // Info HUD bottom-left
        int infoY = screenH - 42;
        gfx.fill(2, infoY - 2, 110, screenH - 2, 0x80101020);

        float hp = mc.player.getHealth();
        int ping = mc.getConnection() != null && mc.getConnection().getPlayerInfo(mc.player.getUUID()) != null
            ? mc.getConnection().getPlayerInfo(mc.player.getUUID()).getLatency() : 0;

        gfx.drawString(mc.font, String.format("\u00A77FPS: \u00A7f%d", mc.getFps()), 5, infoY, 0xFFFFFFFF, true);
        gfx.drawString(mc.font, String.format("\u00A77Ping: \u00A7f%dms", ping), 5, infoY + 10, 0xFFFFFFFF, true);
        gfx.drawString(mc.font, String.format("\u00A77HP: %s%.0f", hp > 14 ? "\u00A7a" : hp > 8 ? "\u00A7e" : "\u00A7c", hp), 5, infoY + 20, 0xFFFFFFFF, true);
        gfx.drawString(mc.font, String.format("\u00A77XYZ: \u00A7f%.0f / %.0f / %.0f",
            mc.player.getX(), mc.player.getY(), mc.player.getZ()), 5, infoY + 30, 0xFFFFFFFF, true);
    }

    private static int hsbToRgb(int hue, float sat, float bri) {
        float c = bri * sat;
        float x = c * (1 - Math.abs((hue / 60f) % 2 - 1));
        float m = bri - c;
        float r, g, b;
        if (hue < 60)       { r = c; g = x; b = 0; }
        else if (hue < 120) { r = x; g = c; b = 0; }
        else if (hue < 180) { r = 0; g = c; b = x; }
        else if (hue < 240) { r = 0; g = x; b = c; }
        else if (hue < 300) { r = x; g = 0; b = c; }
        else                { r = c; g = 0; b = x; }
        int ri = (int) ((r + m) * 255);
        int gi = (int) ((g + m) * 255);
        int bi = (int) ((b + m) * 255);
        return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
    }
}
