package com.droxx69.tp1apr.sender;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.droxx69.tp1apr.R;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;


public class SenderActivity extends AppCompatActivity {

    int portNumber = 8000;
    //    String serverAddress = "192.168.1.108";
    String serverAddress = "192.168.43.9";
    public Button btnSend;

    String protocol;


    public void enableBtn() {
        btnSend.setEnabled(true);
    }

    public void disableBtn() {
        btnSend.setEnabled(false);
    }

    public void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);


        LabeledSwitch labeledSwitch = findViewById(R.id.switch_button);
        labeledSwitch.setOnToggledListener((toggleableView, isOn) -> {
            protocol = isOn ? "selective" : "goBack";
        });

        final EditText toInput = findViewById(R.id.to);




        //final EditText userInput = findViewById();
        btnSend = findViewById(R.id.btnSend);
        btnSend.setEnabled(false);


        final Sender sender = new Sender(serverAddress, portNumber, this);

        if (sender.connect()) {
            Toast.makeText(this, "Connected To Server", Toast.LENGTH_SHORT).show();
            btnSend.setEnabled(true);
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sender.sendMessage(toInput.getText().toString().trim(),
                            Integer.parseInt(toInput.getText().toString()))) {
                        btnSend.setEnabled(false);
                        Toast.makeText(SenderActivity.this, "Message sent , waiting for ACK", Toast.LENGTH_SHORT).show();
                        toInput.setText("");
                    } else {
                        Toast.makeText(SenderActivity.this, "Failed to send", Toast.LENGTH_LONG).show();
                    }

                }
            });
        } else
            Toast.makeText(this, "Couldn't Connect To Server", Toast.LENGTH_LONG).show();


    }


}
