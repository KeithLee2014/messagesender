package com.gmail.keith1987.pojos;

/**
 * Created by keith on 18/03/2018.
 */
public class Settings {

    private String broker;
    private String username;
    private String password;
    private String destinationName;
    private String mode;

    public Settings(String broker, String username, String password, String destinationName, String mode) {
        this.broker = broker;
        this.username = username;
        this.password = password;
        this.destinationName = destinationName;
        this.mode = mode;
    }

    public Settings(){
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}
