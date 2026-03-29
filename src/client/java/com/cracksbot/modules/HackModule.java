package com.cracksbot.modules;

import com.cracksbot.CracksBotClient;
import com.cracksbot.settings.Setting;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public abstract class HackModule {
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private final int defaultKey;
    private boolean enabled;
    private KeyMapping key;
    private final List<Setting<?>> settings = new ArrayList<>();

    public HackModule(String name, String description, ModuleCategory category, int defaultKey, boolean defaultOn) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.defaultKey = defaultKey;
        this.enabled = defaultOn;
    }

    // Compat constructor
    public HackModule(String name, String categoryName, int defaultKey, boolean defaultOn) {
        this(name, "", categoryFromName(categoryName), defaultKey, defaultOn);
    }

    private static ModuleCategory categoryFromName(String name) {
        for (ModuleCategory c : ModuleCategory.values()) {
            if (c.displayName.equalsIgnoreCase(name)) return c;
        }
        return ModuleCategory.MISC;
    }

    protected <T extends Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public void registerKeybinding() {
        key = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "cracksbot." + name.toLowerCase(), defaultKey, KeyMapping.Category.GAMEPLAY
        ));
    }

    public void checkKeybind() {
        if (key != null) while (key.consumeClick()) toggle();
    }

    public void toggle() {
        enabled = !enabled;
        CracksBotClient.notify(Minecraft.getInstance(),
            name + " " + (enabled ? "\u00A7aON" : "\u00A7cOFF"));
        if (enabled) onEnable(); else onDisable();
    }

    public void setEnabled(boolean e) {
        if (e != enabled) toggle();
    }

    public abstract void onTick(Minecraft mc);
    public void onEnable() {}
    public void onDisable() {}

    public String getName() { return name; }
    public String getDescription() { return description; }
    public ModuleCategory getModuleCategory() { return category; }
    public String getCategory() { return category.displayName; }
    public boolean isEnabled() { return enabled; }
    public List<Setting<?>> getSettings() { return settings; }

    public String getKeyName() {
        return key != null ? key.getTranslatedKeyMessage().getString().toUpperCase() : "NONE";
    }
}
