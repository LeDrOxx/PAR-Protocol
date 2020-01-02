package com.droxx69.tp1apr.models;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    int id;
    Socket socket;

    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;

    public Client(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    public ObjectInputStream getsInput() {
        return sInput;
    }

    public void setsInput(ObjectInputStream sInput) {
        this.sInput = sInput;
    }

    public ObjectOutputStream getsOutput() {
        return sOutput;
    }

    public void setsOutput(ObjectOutputStream sOutput) {
        this.sOutput = sOutput;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
