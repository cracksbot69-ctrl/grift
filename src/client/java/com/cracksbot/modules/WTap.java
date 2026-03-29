package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class WTap extends HackModule {
    public WTap() {
        super("W-Tap", "Auto sprint reset for KB", ModuleCategory.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
