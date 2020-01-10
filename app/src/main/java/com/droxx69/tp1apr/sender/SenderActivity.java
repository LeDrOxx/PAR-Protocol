package com.droxx69.tp1apr.sender;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.droxx69.tp1apr.R;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;


public class SenderActivity extends AppCompatActivity {

    int portNumber = 8000;
//        String serverAddress = "192.168.1.108";
    String serverAddress = "192.168.43.9";
    public Button btnSend;

    String protocol = "goBack";

    public ImageView ic_frame0, ic_frame1, ic_frame2, ic_frame3;
    public TextInputLayout frame0, frame1, frame2, frame3;

    public void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);


        LabeledSwitch labeledSwitch = findViewById(R.id.switch_button);
        labeledSwitch.setOnToggledListener((toggleableView, isOn) -> protocol = isOn ? "selective" : "goBack");

        final EditText toInput = findViewById(R.id.to);

        frame0 = findViewById(R.id.frameZeroInput);
        frame1 = findViewById(R.id.frameOneInput);
        frame2 = findViewById(R.id.frameTwoInput);
        frame3 = findViewById(R.id.frameThreeInput);

        ic_frame0 = findViewById(R.id.ic_frameZero);
        ic_frame1 = findViewById(R.id.ic_frameOne);
        ic_frame2 = findViewById(R.id.ic_frameTwo);
        ic_frame3 = findViewById(R.id.ic_frameThree);


        btnSend = findViewById(R.id.btnSend);
        btnSend.setEnabled(false);


        final Sender sender = new Sender(serverAddress, portNumber, this);

        if (sender.connect()) {
            Toast.makeText(this, "Connected To Server", Toast.LENGTH_SHORT).show();
            btnSend.setEnabled(true);
            btnSend.setOnClickListener(v -> {
                String textFrame0 = frame0.getEditText().getText().toString().trim();
                String textFrame1 = frame1.getEditText().getText().toString().trim();
                String textFrame2 = frame2.getEditText().getText().toString().trim();
                String textFrame3 = frame3.getEditText().getText().toString().trim();
                int receiverId = Integer.parseInt(toInput.getText().toString());

                if (!textFrame0.isEmpty() || !textFrame1.isEmpty() || !textFrame2.isEmpty() || !textFrame3.isEmpty() || receiverId != -1) {
                    List<String> frames = new ArrayList<>();
                    frames.add(textFrame0);
                    frames.add(textFrame1);
                    frames.add(textFrame2);
                    frames.add(textFrame3);

                    if (!sender.sendWindow(frames, receiverId, protocol))
                        Toast.makeText(SenderActivity.this, "Failed to send window", Toast.LENGTH_SHORT).show();
                    else {
                        btnSend.setEnabled(false);
                        Toast.makeText(SenderActivity.this, "Window sent , waiting for ACK", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(SenderActivity.this, "Missing info .. try again", Toast.LENGTH_LONG).show();
                }
            });
        } else
            Toast.makeText(this, "Couldn't Connect To Server", Toast.LENGTH_LONG).show();

    }


}
