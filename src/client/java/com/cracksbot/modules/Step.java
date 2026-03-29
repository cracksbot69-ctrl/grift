package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Step extends HackModule {
    private final NumberSetting height = addSetting(new NumberSetting("Height", 1.0, 0.5, 5.0, 0.5));

    public Step() {
        super("Step", "Step up blocks", ModuleCategory.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
