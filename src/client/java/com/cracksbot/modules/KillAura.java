package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class KillAura extends HackModule {
    private final NumberSetting range = addSetting(new NumberSetting("Range", 4.0, 2.0, 6.0, 0.1));
    private final NumberSetting cps = addSetting(new NumberSetting("CPS", 12, 1, 20, 1));
    private final ModeSetting priority = addSetting(new ModeSetting("Priority", "Distance", "Distance", "Health", "Angle"));
    private final BoolSetting players = addSetting(new BoolSetting("Players", true));
    private final BoolSetting mobs = addSetting(new BoolSetting("Mobs", false));

    public KillAura() {
        super("KillAura", "Combat", GLFW.GLFW_KEY_R, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        // TODO: implement
    }
}
