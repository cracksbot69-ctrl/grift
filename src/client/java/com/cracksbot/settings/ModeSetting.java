package com.cracksbot.settings;

public class ModeSetting extends Setting<String> {
    private final String[] modes;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name, defaultMode);
        this.modes = modes;
    }

    public String[] getModes() { return modes; }

    public void cycle() {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(getValue())) {
                setValue(modes[(i + 1) % modes.length]);
                return;
            }
        }
        setValue(modes[0]);
    }

    public void cycleBack() {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(getValue())) {
                setValue(modes[(i - 1 + modes.length) % modes.length]);
                return;
            }
        }
        setValue(modes[0]);
    }
}
