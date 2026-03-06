package com.example.app;

import java.io.Serializable;

public class LED_item implements Serializable {
    private String name;
    private boolean state;
    private int color;
    private String apiAddress;
    private String gpioPin;

    public LED_item(String name, boolean state, int color, String apiAddress, String gpioPin) {
        this.name = name;
        this.state = state;
        this.color = color;
        this.apiAddress = apiAddress;
        this.gpioPin = gpioPin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getApiAddress() {
        return apiAddress;
    }

    public void setApiAddress(String apiAddress) {
        this.apiAddress = apiAddress;
    }

    public String getGpioPin() {
        return gpioPin;
    }

    public void setGpioPin(String gpioPin) {
        this.gpioPin = gpioPin;
    }
}
