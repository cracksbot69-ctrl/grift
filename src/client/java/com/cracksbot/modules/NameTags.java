package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class NameTags extends HackModule {
    public NameTags() {
        super("NameTags", "Enhanced player nametags", ModuleCategory.RENDER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
