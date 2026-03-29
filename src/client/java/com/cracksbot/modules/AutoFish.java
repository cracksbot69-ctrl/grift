package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AutoFish extends HackModule {
    public AutoFish() {
        super("AutoFish", "Auto fishing", ModuleCategory.PLAYER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
