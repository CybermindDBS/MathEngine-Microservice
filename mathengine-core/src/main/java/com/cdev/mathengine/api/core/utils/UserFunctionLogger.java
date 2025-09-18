package com.cdev.mathengine.api.core.utils;

public class UserFunctionLogger {
    StringBuilder logs = new StringBuilder();

    public void log(String msg) {
        logs.append(msg).append("\n");
    }

    public void log(String msg, int newLines) {
        logs.append(msg);
        logs.append("\n".repeat(Math.max(0, newLines)));
    }

    @Override
    public String toString() {
        return logs.toString();
    }
}
