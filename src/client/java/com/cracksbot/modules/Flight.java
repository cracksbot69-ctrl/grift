package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Flight extends HackModule {
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 2.0, 0.5, 10.0, 0.5));
    private final ModeSetting mode = addSetting(new ModeSetting("Mode", "Vanilla", "Vanilla", "Creative", "Glide"));

    public Flight() {
        super("Flight", "Fly around", ModuleCategory.MOVEMENT, GLFW.GLFW_KEY_G, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
