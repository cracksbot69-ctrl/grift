package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Jesus extends HackModule {
    private final ModeSetting mode = addSetting(new ModeSetting("Mode", "Solid", "Solid", "Dolphin"));

    public Jesus() {
        super("Jesus", "Walk on water", ModuleCategory.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
