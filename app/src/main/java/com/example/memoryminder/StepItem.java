package com.example.memoryminder;

public class StepItem {
    private final String steps;
    private final String dataKey;

    public StepItem(String steps, String dataKey) {
        this.steps = steps;
        this.dataKey = dataKey;
    }

    public String getSteps() {
        return steps;
    }

    public String getDataKey() {
        return dataKey;
    }
}
