package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class NoRender extends HackModule {
    private final BoolSetting noFog = addSetting(new BoolSetting("NoFog", true));
    private final BoolSetting noFire = addSetting(new BoolSetting("NoFire", true));
    private final BoolSetting noHurtCam = addSetting(new BoolSetting("NoHurtCam", true));

    public NoRender() {
        super("NoRender", "Disable visual effects", ModuleCategory.RENDER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
