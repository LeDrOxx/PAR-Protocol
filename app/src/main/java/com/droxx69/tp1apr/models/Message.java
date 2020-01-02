package com.droxx69.tp1apr.models;

import java.io.Serializable;
import java.net.Socket;

public class Message implements Serializable {
    private int id_Receiver, id_Sender;
    private int myId;

    private String message;
    private boolean received, receiverDisconnected;
    private int msg_id;

    public Message(int id_Sender, int id_Receiver, String message) {
        this.id_Receiver = id_Receiver;
        this.id_Sender = id_Sender;
        this.message = message;
    }

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;

        if (object != null && object instanceof Message) {
            sameSame = this.msg_id == ((Message) object).msg_id;
        }

        return sameSame;
    }

    public Message switchId() {
        int temp = id_Receiver;
        id_Receiver = id_Sender;
        id_Sender = temp;

        return this;
    }

    public boolean isReceiverDisconnected() {
        return receiverDisconnected;
    }

    public void setReceiverDisconnected(boolean receiverDisconnected) {
        this.receiverDisconnected = receiverDisconnected;
    }

    public int getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(int msg_id) {
        this.msg_id = msg_id;
    }

    public Message(int myId) {
        this.myId = myId;
    }

    public int getMyId() {
        return myId;
    }

    public void setMyId(int myId) {
        this.myId = myId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public int getId_Receiver() {
        return id_Receiver;
    }

    public void setId_Receiver(int id_Receiver) {
        this.id_Receiver = id_Receiver;
    }

    public int getId_Sender() {
        return id_Sender;
    }

    public void setId_Sender(int id_Sender) {
        this.id_Sender = id_Sender;
    }

    public String getMessge() {
        return message;
    }

    public void setMessge(String messge) {
        this.message = message;
    }
}
