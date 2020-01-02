package com.droxx69.tp1apr.sender;

import com.droxx69.tp1apr.models.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Sender {

    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private String server;
    private int port, myId;
    private SenderActivity mCtx;

    private Message msg;

    private List<Message> window = new ArrayList<>();


    public Sender(String server, int port, SenderActivity mCtx) {
        this.server = server;
        this.port = port;
        this.mCtx = mCtx;
    }

    public Sender(String server, int port) {
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

    boolean sendMessage(final String strMsg, final int to) {
        final boolean[] result = {true};
        final Message InnerMsg = new Message(myId, to, strMsg);
        InnerMsg.setMsg_id(window.size());


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    sOutput.writeObject(InnerMsg);
                    checkMsg(InnerMsg, this);
                } catch (IOException e) {
                    result[0] = false;
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
        window.add(InnerMsg);

        return result[0];
    }


    private void checkMsg(Message msg, Runnable thread) {
        try {
            synchronized (thread) {
                thread.wait(1000 * 3);
                if (!window.isEmpty() && !window.get(msg.getMsg_id()).isReceived())
                    retry(msg, thread);

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void retry(Message msg, Runnable thread) {
        try {
            sOutput.writeObject(msg);
            checkMsg(msg, thread);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkWindow() {
        if (window.size() >= 2) {
            boolean shouldClean = true;
            for (Message m : window) {
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
                                mCtx.btnSend.setEnabled(true);
                            }
                        });
                    } else if (msg.isReceived()) {
                        window.get(msg.getMsg_id()).setReceived(true);
                        mCtx.runOnUiThread(new Runnable() {
                            public void run() {
                                mCtx.showToast("Message Received ! ");
                                mCtx.btnSend.setEnabled(true);

                            }
                        });

                        checkWindow();
                    } else if (msg.isReceiverDisconnected()) {
                        mCtx.runOnUiThread(new Runnable() {
                            public void run() {
                                mCtx.showToast("Client disconnected");
                                mCtx.btnSend.setEnabled(true);
                                window.clear();
                            }
                        });
                    }

                } catch (IOException e) {
                    break;
                } catch (ClassNotFoundException e2) {

                }
            }
        }
    }

}
