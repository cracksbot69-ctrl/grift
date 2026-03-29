package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class ChestStealer extends HackModule {
    private final NumberSetting delay = addSetting(new NumberSetting("Delay", 50, 0, 500, 10));

    public ChestStealer() {
        super("ChestStealer", "Auto loot chests", ModuleCategory.PLAYER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
