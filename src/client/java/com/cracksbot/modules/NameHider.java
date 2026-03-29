package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class NameHider extends HackModule {
    public NameHider() {
        super("NameHider", "Hide your name", ModuleCategory.MISC, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
