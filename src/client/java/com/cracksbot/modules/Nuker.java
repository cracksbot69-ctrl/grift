package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Nuker extends HackModule {
    private final NumberSetting range = addSetting(new NumberSetting("Range", 4.0, 1.0, 6.0, 0.5));
    private final ModeSetting mode = addSetting(new ModeSetting("Mode", "All", "All", "Flatten", "Smash"));

    public Nuker() {
        super("Nuker", "Break blocks around you", ModuleCategory.WORLD, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
