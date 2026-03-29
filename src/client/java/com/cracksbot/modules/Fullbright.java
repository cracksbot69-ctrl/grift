package com.cracksbot.modules;

import com.cracksbot.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class Fullbright extends HackModule {
    private final NumberSetting brightness = addSetting(new NumberSetting("Brightness", 16.0, 1.0, 16.0, 1.0));
    private double originalGamma = 1.0;

    public Fullbright() {
        super("Fullbright", "Forces max brightness in all dimensions", ModuleCategory.RENDER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        try {
            originalGamma = getRaw(mc.options.gamma());
        } catch (Exception ignored) {}
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        try {
            setRaw(mc.options.gamma(), brightness.getValue());
        } catch (Exception ignored) {}
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        try {
            setRaw(mc.options.gamma(), originalGamma);
        } catch (Exception ignored) {}
    }

    private static void setRaw(OptionInstance<Double> option, double val) throws Exception {
        for (Field f : OptionInstance.class.getDeclaredFields()) {
            f.setAccessible(true);
            Object v = f.get(option);
            if (v instanceof Double) {
                f.set(option, val);
                return;
            }
        }
    }

    private static double getRaw(OptionInstance<Double> option) throws Exception {
        for (Field f : OptionInstance.class.getDeclaredFields()) {
            f.setAccessible(true);
            Object v = f.get(option);
            if (v instanceof Double d) return d;
        }
        return 1.0;
    }
}
