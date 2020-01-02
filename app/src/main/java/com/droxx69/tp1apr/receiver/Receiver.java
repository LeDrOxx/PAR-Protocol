package com.droxx69.tp1apr.receiver;


import android.annotation.SuppressLint;

import com.droxx69.tp1apr.models.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Receiver {

    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;


    private String server;
    private int port, myId;
    private ReceiverActivity mCtx;

    private Message msg;

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, Message> window = new HashMap<>();

    public Receiver(String server, int port, ReceiverActivity mCtx) {
        this.server = server;
        this.port = port;
        this.mCtx = mCtx;
    }


    public boolean connect() {
        final boolean[] result = {true};
        new Thread(() -> {
            try {
                socket = new Socket(server, port);
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                new ListenFromServer().start();
            } catch (Exception ec) {
                result[0] = false;
            }
        }).start();

        return result[0];
    }

    boolean returnAck(List<Integer> acks) {
        for (Integer id : acks)
            try {
                if (!window.isEmpty()) {
                    Message tempMessage = window.get(id).switchId();
                    sOutput.writeObject(tempMessage);
                }
            } catch (IOException e) {
                return false;
            }
        if (acks.size() >= 4)
            window.clear();

        return true;
    }

    class ListenFromServer extends Thread {

        public void run() {
            while (true) {
                try {

                    msg = (Message) sInput.readObject();
                    if (msg.getMyId() != -1) {
                        myId = msg.getMyId();
                        mCtx.runOnUiThread(() -> mCtx.showToast("You are the client number : " + myId));


                    } else if (msg.isReceiverDisconnected()) {
                        mCtx.runOnUiThread(() -> mCtx.showToast("Sender disconnected"));
                    } else {
                        msg.setReceived(true);


                        if (!window.keySet().contains(msg.getMsg_id())) {
                            window.put(msg.getMsg_id(), msg);
                            setFrameText(msg.getMessage(), msg.getMsg_id());
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (ClassNotFoundException e2) {
                    e2.printStackTrace();
                }
            }
        }

        void setFrameText(String text, int number) {
            switch (number) {
                case 0:
                    mCtx.runOnUiThread(() -> mCtx.frame0.getEditText().setText(text));
                    break;
                case 1:
                    mCtx.runOnUiThread(() -> mCtx.frame1.getEditText().setText(text));
                    break;
                case 2:
                    mCtx.runOnUiThread(() -> mCtx.frame2.getEditText().setText(text));
                    break;
                case 3:
                    mCtx.runOnUiThread(() -> mCtx.frame3.getEditText().setText(text));
                    break;
            }
        }
    }
}
