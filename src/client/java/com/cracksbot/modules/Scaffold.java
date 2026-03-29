package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Scaffold extends HackModule {
    private final BoolSetting tower = addSetting(new BoolSetting("Tower", true));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));

    public Scaffold() {
        super("Scaffold", "Place blocks below", ModuleCategory.WORLD, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
