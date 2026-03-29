package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Timer extends HackModule {
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 2.0, 0.1, 10.0, 0.1));

    public Timer() {
        super("Timer", "Game speed multiplier", ModuleCategory.WORLD, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
