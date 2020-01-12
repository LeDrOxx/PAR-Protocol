package com.droxx69.tp1apr.receiver;


import android.annotation.SuppressLint;

import com.droxx69.tp1apr.models.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

    boolean returnAck(List<Integer> acks, String protocol) {
        for (Integer id : acks)
            try {
                if (!window.isEmpty() && window.get(id) != null) {
                    Message tempMessage = window.get(id).switchId();
                    sOutput.writeObject(tempMessage);
                    if (protocol.equals("selective")) {
                        window.remove(id);
//                        clearMsg(id);
                    }
                }
            } catch (IOException e) {
                return false;
            }

//        window.clear();
        if (protocol.equals("selective"))
            slide();
        else
            window.clear();

        return true;
    }

    @SuppressLint("UseSparseArrays")
    private void slide() {
        int index = 0;
        HashMap<Integer, Message> newWindow = new HashMap<>();

        for (int i = 0; i < 4; i++) {
            Message tempMsg = window.get(i);
            if (tempMsg != null) {
                tempMsg.setMsg_id(index);
                newWindow.put(index, tempMsg);
                slideOnUI(i, index, tempMsg.getMessage());
                index++;
            }
        }

        window = new HashMap<>(newWindow);
    }

    private void slideOnUI(int from, int to, String data) {
        switch (to) {
            case 0: {
                mCtx.runOnUiThread(() -> mCtx.frame0.getEditText().setText(data));
            }
            break;
            case 1: {
                mCtx.runOnUiThread(() -> mCtx.frame1.getEditText().setText(data));
            }
            break;
            case 2: {
                mCtx.runOnUiThread(() -> mCtx.frame2.getEditText().setText(data));
            }
            break;
            case 3: {
                mCtx.runOnUiThread(() -> mCtx.frame3.getEditText().setText(data));
            }
            break;
        }
        clearMsg(from);
    }

    private void clearMsg(int frame) {
        switch (frame) {
            case 0: {
                mCtx.runOnUiThread(() -> mCtx.frame0.getEditText().setText(""));
            }
            break;
            case 1: {
                mCtx.runOnUiThread(() -> mCtx.frame1.getEditText().setText(""));
            }
            break;
            case 2: {
                mCtx.runOnUiThread(() -> mCtx.frame2.getEditText().setText(""));
            }
            break;
            case 3: {
                mCtx.runOnUiThread(() -> mCtx.frame3.getEditText().setText(""));
            }
            break;
        }

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
