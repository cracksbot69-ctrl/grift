package com.cracksbot.modules;

public enum ModuleCategory {
    COMBAT("Combat", 0xFFFF4444),
    MOVEMENT("Movement", 0xFF4488FF),
    RENDER("Render", 0xFFAA44FF),
    PLAYER("Player", 0xFF44FF44),
    WORLD("World", 0xFFFFAA44),
    MISC("Misc", 0xFF44FFFF);

    public final String displayName;
    public final int color;

    ModuleCategory(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }
}
