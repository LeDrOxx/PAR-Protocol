package com.droxx69.tp1apr.receiver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.droxx69.tp1apr.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ReceiverActivity extends AppCompatActivity {

    TextView waiting, noiseLevel;
    TextInputLayout textView;
    int portnumber = 8000;
//    String serverAdresse = "192.168.1.108";
    String serverAdresse = "192.168.43.9";

    public SeekBar seekBar;
    public int noise;

    public void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reveiver);

        noiseLevel = findViewById(R.id.textView2);

//        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                noise = progress;
                noiseLevel.setText("Noise level : " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Receiver receiver = new Receiver(serverAdresse, portnumber, this);

        if (receiver.connect()) {
            Toast.makeText(this, "Connected to server", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
        }
//        textView = findViewById(R.id.textView);
        waiting = findViewById(R.id.textView3);
    }

    public void addMessage(String message, int id_sender, int msgId) {
        String text = "Client " + id_sender + " : " + message + "\n";
        textView.getEditText().append(text);

        switch (msgId) {
            case 0:
                waiting.setText("Waiting for the frame 1");
                break;
            case 1:
                waiting.setText("Waiting for the frame 0");
                break;
            default:
                waiting.setText("Unknown frame number !!!");

        }
    }

}
