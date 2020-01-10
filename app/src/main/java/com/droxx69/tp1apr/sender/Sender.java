package com.droxx69.tp1apr.sender;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.droxx69.tp1apr.R;
import com.droxx69.tp1apr.models.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Sender {

    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private String server;
    private int port, myId;
    private SenderActivity mCtx;

    private Message msg;
    private String protocol;
    String data;

    private HashMap<Integer, Message> window = new HashMap<>();
    private HashMap<Integer, Message> receivedMessages = new HashMap<>();


    public Sender(String server, int port, SenderActivity mCtx) {
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

    boolean sendWindow(final List<String> newWindow, final int to, String protocol) {
        this.protocol = protocol;
        window = new HashMap<>();
        for (String frame : newWindow) {
            try {
                Message tempMessage = new Message(myId, to, frame);
                tempMessage.setMsg_id(window.size());
                window.put(window.size(), tempMessage);
                sOutput.writeObject(tempMessage);
            } catch (IOException e) {
                return false;
            }
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                checkWindow(this);
            }
        };

        new Thread(runnable).start();
        return true;
    }

    private void checkWindow(Runnable thread) {
        try {
            synchronized (thread) {
                thread.wait(1000 * 8);
                boolean shouldRetry = false;
                List<Message> lostMessages = new ArrayList<>();
                for (Message msg : window.values()) {
                    if (!msg.isReceived()) {
                        shouldRetry = true;
                        lostMessages.add(msg);
                    }
                }

                if (shouldRetry && window.size() >= 4)
                    switch (protocol) {
                        case "goBack":
                            retry(window.values(), thread);
                            break;
                        case "selective": {
                            retry(window.values(), thread);

                        }
                        break;
                    }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void retry(Collection<Message> messages, Runnable thread) {
        try {
            for (Message msg : messages)
                sOutput.writeObject(msg);
            checkWindow(thread);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class ListenFromServer extends Thread {

        public void run() {
            while (true) {
                try {
                    msg = (Message) sInput.readObject();

                    if (msg.getMyId() != -1) {
                        myId = msg.getMyId();

                        mCtx.runOnUiThread(() -> {
                            mCtx.showToast("You are the client number : " + myId);
                            mCtx.btnSend.setEnabled(true);
                        });

                    } else if (msg.isReceived() && window.get(msg.getMsg_id()) != null) {
                        window.get(msg.getMsg_id()).setReceived(true);
                        if (protocol.equals("goBack")) {
                            showMessageReceived(msg.getMsg_id());
                            for (Message msg : window.values())
                                if (!msg.isReceived())
                                    showMessageLost(msg.getMsg_id());
                        } else {
                            receivedMessages.put(msg.getMsg_id(), msg);
                            window.remove(msg.getMsg_id());
                            clearMsgOnUI(msg.getMsg_id());
                            slide();
                        }

                        if (allMsgReceived())
                            mCtx.runOnUiThread(() -> {
                                mCtx.showToast("All messages received");
                                mCtx.btnSend.setEnabled(true);
                                mCtx.ic_frame0.setVisibility(View.GONE);
                                mCtx.ic_frame1.setVisibility(View.GONE);
                                mCtx.ic_frame2.setVisibility(View.GONE);
                                mCtx.ic_frame3.setVisibility(View.GONE);
                                receivedMessages.clear();
                            });

                    } else if (msg.isReceiverDisconnected()) {
                        mCtx.runOnUiThread(() -> {
                            mCtx.showToast("Client disconnected");
                            mCtx.btnSend.setEnabled(true);
                            showMessageLost(0);
                            showMessageLost(1);
                            showMessageLost(2);
                            showMessageLost(3);
                            window.clear();
                            receivedMessages.clear();
                        });
                    }

                } catch (IOException | ClassNotFoundException e) {
                    break;
                }
            }
        }

        @SuppressLint("UseSparseArrays")
        private void slide() {
            int index = 0;
            HashMap<Integer, Message> newWindow = new HashMap<>();

            for (int i = 0; i < 4; i++) {
                if (window.get(i) != null) {
                    newWindow.put(index, window.get(i));
                    slideOnUI(i, index);
                    index++;
                }
            }

            window = new HashMap<>(newWindow);
        }

        private void slideOnUI(int from, int to) {
            switch (from) {
                case 0: {
                    mCtx.runOnUiThread(() -> data = mCtx.frame0.getEditText().getText().toString());
                }
                break;
                case 1: {
                    mCtx.runOnUiThread(() -> data = mCtx.frame1.getEditText().getText().toString());
                }
                break;
                case 2: {
                    mCtx.runOnUiThread(() -> data = mCtx.frame2.getEditText().getText().toString());
                }
                break;
                case 3: {
                    mCtx.runOnUiThread(() -> data = mCtx.frame3.getEditText().getText().toString());
                }
                break;
            }
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

            clearMsgOnUI(from);

        }

        private void clearMsgOnUI(int frame) {
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
            mCtx.runOnUiThread(() -> mCtx.btnSend.setEnabled(true));

        }

        boolean allMsgReceived() {
            for (Message msg : window.values())
                if (!msg.isReceived())
                    return false;
            return true;
        }

        void showMessageReceived(int frame) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Drawable d = mCtx.getResources().getDrawable(R.drawable.animated_check);
                switch (frame) {
                    case 0: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame0.setImageDrawable(d));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame0.setVisibility(View.VISIBLE));

                    }
                    break;
                    case 1: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame1.setImageDrawable(d));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame1.setVisibility(View.VISIBLE));
                    }
                    break;
                    case 2: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame2.setImageDrawable(d));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame2.setVisibility(View.VISIBLE));
                    }
                    break;
                    case 3: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame3.setImageDrawable(d));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame3.setVisibility(View.VISIBLE));
                    }
                    break;
                }

                if (d instanceof AnimatedVectorDrawable) {
                    AnimatedVectorDrawable avd = (AnimatedVectorDrawable) d;
                    mCtx.runOnUiThread(avd::start);
                } else if (d instanceof AnimatedVectorDrawableCompat) {
                    AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) d;
                    mCtx.runOnUiThread(avd::start);
                }
            } else {
                switch (frame) {
                    case 0: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame0.setBackgroundResource(R.drawable.check));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame0.setVisibility(View.VISIBLE));
                    }
                    break;

                    case 1: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame1.setBackgroundResource(R.drawable.check));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame1.setVisibility(View.VISIBLE));
                    }
                    break;
                    case 2: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame2.setBackgroundResource(R.drawable.check));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame2.setVisibility(View.VISIBLE));
                    }
                    break;
                    case 3: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame3.setBackgroundResource(R.drawable.check));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame3.setVisibility(View.VISIBLE));
                    }
                    break;
                }

            }
        }

        void showMessageLost(int frame) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Drawable d = mCtx.getResources().getDrawable(R.drawable.animated_cross);
                switch (frame) {
                    case 0: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame0.setImageDrawable(d));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame0.setVisibility(View.VISIBLE));

                    }
                    break;
                    case 1: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame1.setImageDrawable(d));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame1.setVisibility(View.VISIBLE));
                    }
                    break;
                    case 2: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame2.setImageDrawable(d));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame2.setVisibility(View.VISIBLE));
                    }
                    break;
                    case 3: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame3.setImageDrawable(d));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame3.setVisibility(View.VISIBLE));
                    }
                    break;
                }
                if (d instanceof AnimatedVectorDrawable) {
                    AnimatedVectorDrawable avd = (AnimatedVectorDrawable) d;
                    mCtx.runOnUiThread(avd::start);
                } else if (d instanceof AnimatedVectorDrawableCompat) {
                    AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) d;
                    mCtx.runOnUiThread(avd::start);
                }
            } else {
                switch (frame) {
                    case 0: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame0.setBackgroundResource(R.drawable.cross));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame0.setVisibility(View.VISIBLE));
                    }
                    break;

                    case 1: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame1.setBackgroundResource(R.drawable.cross));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame1.setVisibility(View.VISIBLE));
                    }
                    break;
                    case 2: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame2.setBackgroundResource(R.drawable.cross));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame2.setVisibility(View.VISIBLE));
                    }
                    break;
                    case 3: {
                        mCtx.runOnUiThread(() -> mCtx.ic_frame3.setBackgroundResource(R.drawable.cross));
                        mCtx.runOnUiThread(() -> mCtx.ic_frame3.setVisibility(View.VISIBLE));
                    }
                    break;
                }

            }
        }

    }

}
