package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Xray extends HackModule {
    private final BoolSetting diamonds = addSetting(new BoolSetting("Diamonds", true));
    private final BoolSetting gold = addSetting(new BoolSetting("Gold", true));
    private final BoolSetting iron = addSetting(new BoolSetting("Iron", false));
    private final BoolSetting spawners = addSetting(new BoolSetting("Spawners", true));

    public Xray() {
        super("Xray", "See ores through blocks", ModuleCategory.RENDER, GLFW.GLFW_KEY_X, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
