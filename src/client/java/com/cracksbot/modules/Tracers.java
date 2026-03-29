package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Tracers extends HackModule {
    private final BoolSetting players = addSetting(new BoolSetting("Players", true));
    private final BoolSetting mobs = addSetting(new BoolSetting("Mobs", false));

    public Tracers() {
        super("Tracers", "Lines to entities", ModuleCategory.RENDER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
