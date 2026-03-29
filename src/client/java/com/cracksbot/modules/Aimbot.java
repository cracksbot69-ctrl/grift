package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Aimbot extends HackModule {
    private final NumberSetting fov = addSetting(new NumberSetting("FOV", 90, 10, 360, 5));
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", 5.0, 1.0, 10.0, 0.5));
    private final BoolSetting players = addSetting(new BoolSetting("Players", true));
    private final BoolSetting mobs = addSetting(new BoolSetting("Mobs", false));

    public Aimbot() {
        super("Aimbot", "Auto aim at targets", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
