package com.cracksbot.settings;

public class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, int defaultArgb) {
        super(name, defaultArgb);
    }

    public int r() { return (getValue() >> 16) & 0xFF; }
    public int g() { return (getValue() >> 8) & 0xFF; }
    public int b() { return getValue() & 0xFF; }
    public int a() { return (getValue() >> 24) & 0xFF; }

    /** Returns the stored color with a custom alpha (0-255). */
    public int withAlpha(int alpha) {
        return (getValue() & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    /** Returns the stored color forced to full opacity. */
    public int opaque() {
        return getValue() | 0xFF000000;
    }
}
