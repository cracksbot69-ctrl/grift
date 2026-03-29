package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class FastPlace extends HackModule {
    public FastPlace() {
        super("FastPlace", "No place delay", ModuleCategory.WORLD, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
