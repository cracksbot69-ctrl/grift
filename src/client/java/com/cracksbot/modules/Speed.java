package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Speed extends HackModule {
    private final NumberSetting multiplier = addSetting(new NumberSetting("Speed", 1.5, 1.0, 5.0, 0.1));
    private final ModeSetting mode = addSetting(new ModeSetting("Mode", "Strafe", "Strafe", "BHop", "OnGround"));

    public Speed() {
        super("Speed", "Move faster", ModuleCategory.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
