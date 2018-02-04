package at.ta.se.bluetoothterminalst;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Taurer on 04.02.2018.
 */

public class BTThread extends Thread {

    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler btHandler;

    //creation of the connect thread
    public BTThread(BluetoothSocket socket, Handler btHandler) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.btHandler = btHandler;

        try {
            //Create I/O streams for connection
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[256];
        int bytes;

        // Keep looping to listen for received messages
        while (true) {
            try {
                bytes = mmInStream.read(buffer);            //read bytes from input buffer
                String readMessage = new String(buffer, 0, bytes);
                // Send the obtained bytes to the UI Activity via handler
                btHandler.obtainMessage(0, bytes, -1, readMessage).sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }
    //write method
    public void write(String input) throws IOException {
        mmOutStream.write(input.getBytes());
    }
}