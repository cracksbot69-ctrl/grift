package com.cracksbot.settings;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }

    public void increment() {
        setValue(Math.min(max, getValue() + step));
    }

    public void decrement() {
        setValue(Math.max(min, getValue() - step));
    }

    public void setFromRatio(double ratio) {
        double raw = min + (max - min) * ratio;
        double stepped = Math.round(raw / step) * step;
        setValue(Math.max(min, Math.min(max, stepped)));
    }

    public double getRatio() {
        return (getValue() - min) / (max - min);
    }
}
