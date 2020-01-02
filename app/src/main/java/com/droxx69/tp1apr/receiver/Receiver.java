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

    private int lostAck = 0;
    private List<Message> window = new ArrayList<>();

    public Receiver(String server, int port, ReceiverActivity mCtx) {
        this.server = server;
        this.port = port;
        this.mCtx = mCtx;
    }

    public Receiver(String server, int port) {
        this.server = server;
        this.port = port;
    }

    public boolean connect() {
        final boolean[] result = {true};
        new Thread(new Runnable() { //thread anonyme ghir 3la jal cnx y connecti w c bn
            @Override
            public void run() {
                try {
                    socket = new Socket(server, port);
                    sOutput = new ObjectOutputStream(socket.getOutputStream());
                    sInput = new ObjectInputStream(socket.getInputStream());
                    new ListenFromServer().start();
                } catch (Exception ec) {
                    result[0] = false;
                }
            }
        }).start();

        return result[0];
    }

    boolean sendMessage(final Message msg) {
        final boolean[] result = {true};
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sOutput.writeObject(msg);

                } catch (IOException e) {
                    result[0] = false;
                }
            }
        }).start();

        return result[0];
    }

    boolean shouldReturnAck() {
        return mCtx.noise <= lostAck;
    }

    private void checkWindow(){
        if (window.size() >= 2){
            boolean shouldClean =  true;
            for (Message m : window){
                if (!m.isReceived())
                    shouldClean = false;
            }
            if (shouldClean)
                window.clear();
        }
    }

    class ListenFromServer extends Thread {

        public void run() {
            while (true) {
                try {

                    msg = (Message) sInput.readObject();
                    if (msg.getMyId() != -1) {
                        myId = msg.getMyId();
                        mCtx.runOnUiThread(new Runnable() {
                            public void run() {
                                mCtx.showToast("You are the client number : " + myId);
                            }
                        });


                    } else {
                        msg.setReceived(true);

                        if (msg.getMsg_id() == window.size()) {
                            window.add(msg);
                            mCtx.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mCtx.addMessage(msg.getMessage(), msg.getId_Receiver(), msg.getMsg_id());
                                }
                            });
                        }

                        if (shouldReturnAck()) {
                            int idNewReceiver = msg.getId_Sender();
                            msg.setId_Sender(myId);
                            msg.setId_Receiver(idNewReceiver);

                            sendMessage(msg);
                            lostAck = 0;
                        } else {
                            lostAck++;
                        }

                        checkWindow();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (ClassNotFoundException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
}
