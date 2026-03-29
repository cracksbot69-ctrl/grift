package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class NoFall extends HackModule {
    private final ModeSetting mode = addSetting(new ModeSetting("Mode", "Packet", "Packet", "Bucket"));

    public NoFall() {
        super("NoFall", "No fall damage", ModuleCategory.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
