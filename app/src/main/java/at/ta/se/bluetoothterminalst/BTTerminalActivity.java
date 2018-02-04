package at.ta.se.bluetoothterminalst;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class BTTerminalActivity extends AppCompatActivity {

    private TextView tvTerminalLog;
    private EditText etMessage;
    private Handler btHandler;
    private String btDeviceName;

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private BTThread btThread;

    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btterminal);

        tvTerminalLog = findViewById(R.id.tvTerminalLog);
        tvTerminalLog.setMovementMethod(new ScrollingMovementMethod());
        etMessage = findViewById(R.id.etMessage);

        btHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    String receivedMessage = (String) msg.obj;
                    tvTerminalLog.append(receivedMessage);
                }
            }
        };

        checkBTState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);
        btDeviceName = intent.getStringExtra(MainActivity.EXTRA_DEVICE_NAME);

        tvTerminalLog.append("Connecting to '" + address + "' ...\n");

        BluetoothDevice btDevice = btAdapter.getRemoteDevice(address);
        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed!", Toast.LENGTH_LONG).show();
        }
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e1) {
                Toast.makeText(getBaseContext(), "Could not connect to BT socket!", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        btThread = new BTThread(btSocket, btHandler);
        btThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            btSocket.close();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Could not close BT socket!", Toast.LENGTH_LONG).show();
        }
    }

    public void btSendPressed(View view) {
        tvTerminalLog.append(etMessage.getText().toString() + "\n");
        try {
            btThread.write(etMessage.getText().toString());
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Could not send text: '" + etMessage.getText().toString() + "'!", Toast.LENGTH_LONG).show();
        }
    }

    //method to check if the device has Bluetooth and if it is on.
    //Prompts the user to turn it on if it is off
    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        btAdapter = BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!btAdapter.isEnabled()) {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}
