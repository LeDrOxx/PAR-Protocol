package com.droxx69.tp1apr.sender;

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

    private List<Message> window;
    int received = 0;


    public Sender(String server, int port, SenderActivity mCtx) {
        this.server = server;
        this.port = port;
        this.mCtx = mCtx;
    }


    public boolean connect() {
        final boolean[] result = {true};
        new Thread(() -> { //thread anonyme ghir 3la jal cnx y connecti w c bn
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
        window = new ArrayList<>();
        for (String frame : newWindow) {
            try {
                Message tempMessage = new Message(myId, to, frame);
                tempMessage.setMsg_id(window.size());
                window.add(tempMessage);
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
        Thread thread = new Thread(runnable);
        thread.start();
        return true;
    }

    private void checkWindow(Runnable thread) {
        try {
            synchronized (thread) {
                thread.wait(1000 * 8);
                boolean shouldRetry = false;
                List<Message> lostMessages = new ArrayList<>();
                for (Message msg : window) {
                    if (!msg.isReceived()) {
                        shouldRetry = true;
                        lostMessages.add(msg);
                    }
                }

                if (shouldRetry)
                    switch (protocol) {
                        case "goBack":
                            retry(window, thread);
                            break;
                        case "selective":
                            retry(lostMessages, thread);
                            break;
                    }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void retry(List<Message> messages, Runnable thread) {
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

                    } else if (msg.isReceived()) {
                        window.get(msg.getMsg_id()).setReceived(true);
                        showMessageReceived(msg.getMsg_id());

                        for (Message msg : window)
                            if (!msg.isReceived())
                                showMessageLost(msg.getMsg_id());

                        if (allMsgReceived())
                            mCtx.runOnUiThread(() -> {
                                mCtx.showToast("All messages received");
                                mCtx.btnSend.setEnabled(true);
                                mCtx.ic_frame0.setVisibility(View.GONE);
                                mCtx.ic_frame1.setVisibility(View.GONE);
                                mCtx.ic_frame2.setVisibility(View.GONE);
                                mCtx.ic_frame3.setVisibility(View.GONE);
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
                        });
                    }

                } catch (IOException e) {
                    break;
                } catch (ClassNotFoundException e2) {

                }
            }
        }

        boolean allMsgReceived() {
            for (Message msg : window)
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
