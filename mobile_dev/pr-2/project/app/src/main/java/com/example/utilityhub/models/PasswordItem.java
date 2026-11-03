package com.example.utilityhub.models;

public class PasswordItem {
    private String id;
    private String service;
    private String login;
    private String encryptedPassword;
    private String note;

    public PasswordItem(String id, String service, String login,
                        String encryptedPassword, String note) {
        this.id = id;
        this.service = service;
        this.login = login;
        this.encryptedPassword = encryptedPassword;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
