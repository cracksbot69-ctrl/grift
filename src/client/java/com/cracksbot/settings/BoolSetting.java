package com.cracksbot.settings;

public class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public void toggle() {
        setValue(!getValue());
    }
}
