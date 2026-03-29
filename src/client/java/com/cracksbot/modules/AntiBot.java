package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AntiBot extends HackModule {
    public AntiBot() {
        super("AntiBot", "Filter fake players", ModuleCategory.MISC, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
