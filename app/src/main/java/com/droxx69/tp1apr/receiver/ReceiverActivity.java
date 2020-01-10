package com.droxx69.tp1apr.receiver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.buildware.widget.indeterm.IndeterminateCheckBox;
import com.droxx69.tp1apr.R;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class ReceiverActivity extends AppCompatActivity {

    int portnumber = 8000;
//        String serverAdresse = "192.168.1.108";
    String serverAdresse = "192.168.43.9";
    public Button btnReturnAck;
    String protocol = "goBack";

    public TextInputLayout frame0, frame1, frame2, frame3;
    private IndeterminateCheckBox checkFrame0, checkFrame1, checkFrame2, checkFrame3;


    public void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reveiver);

        LabeledSwitch labeledSwitch = findViewById(R.id.switch_button2);
        labeledSwitch.setOnToggledListener((toggleableView, isOn) -> protocol = isOn ? "selective" : "goBack");


        btnReturnAck = findViewById(R.id.btnReturnAck);

        frame0 = findViewById(R.id.frameZeroInput);
        frame1 = findViewById(R.id.frameOneInput);
        frame2 = findViewById(R.id.frameTwoInput);
        frame3 = findViewById(R.id.frameThreeInput);

        checkFrame0 = findViewById(R.id.checkFrame0);
        checkFrame1 = findViewById(R.id.checkFrame1);
        checkFrame2 = findViewById(R.id.checkFrame2);
        checkFrame3 = findViewById(R.id.checkFrame3);


        Receiver receiver = new Receiver(serverAdresse, portnumber, this);

        if (receiver.connect())
            Toast.makeText(this, "Connected to server", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();

        btnReturnAck.setOnClickListener(v -> {
            List<Integer> acks = new ArrayList<>();
            if (checkFrame0.getState())
                acks.add(0);
            if (checkFrame1.getState())
                acks.add(1);
            if (checkFrame2.getState())
                acks.add(2);
            if (checkFrame3.getState())
                acks.add(3);

            if (receiver.returnAck(acks , protocol)) {
                Toast.makeText(this, "Acks sent", Toast.LENGTH_SHORT).show();
                if (acks.size() == 4) {
                    frame0.getEditText().setText("");
                    frame1.getEditText().setText("");
                    frame2.getEditText().setText("");
                    frame3.getEditText().setText("");
                }
            }

        });

    }


}
