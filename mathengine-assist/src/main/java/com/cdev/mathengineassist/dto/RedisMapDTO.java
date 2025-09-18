package com.cdev.mathengineassist.dto;

public class RedisMapDTO {

    String input;
    String output;

    public RedisMapDTO() {

    }

    public RedisMapDTO(String input, String output) {
        this.input = input;
        this.output = output;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
