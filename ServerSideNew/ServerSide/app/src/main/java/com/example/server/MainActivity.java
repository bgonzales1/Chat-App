package com.example.server;

import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity  {

    private ServerSocket serverSocket;
    private Socket socket_for_client;
    private Handler handler;
    private EditText editText_message;
    private TextView ipTextviewID;
    private Thread thread_for_server = null;
    private int textColor;
    private LinearLayout msgScrollViewList;
    public static final int server_port = 8080;

    //this function will help to get ip address from mobile operating system..
    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array())
                .getHostAddress();
    }

    //server side need to enable before client side to start connection with the same server id on client side...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Server_Side");
        initViews();
        String SERVER_IP = "";
        try {
            SERVER_IP = getLocalIpAddress();
            ipTextviewID.setText("Server IP: "+SERVER_IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Log.e("TAG", "onCreateonCreateonCreateonCreate: "+SERVER_IP );
    }

    //Initialization of required data here...
    private void initViews() {
        handler = new Handler();
        msgScrollViewList = findViewById(R.id.msgScrollViewList);
        editText_message = findViewById(R.id.editText_message);
        ipTextviewID = findViewById(R.id.ipTextviewID);
        textColor = ContextCompat.getColor(this, R.color.colorPrimary);
    }

    //funtion for minimize the code print again and again for printing over the screen...
    private void Message_for_Print(final String message, final int textColor, final Boolean value) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgScrollViewList.addView(PrintMessageOnScreen(message, textColor, value));
            }
        });
    }

    //this function print text in the chat view...
    public TextView PrintMessageOnScreen(String message, int textColor, Boolean value) {

        TextView textView = new TextView(this);
        if (value) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }
        textView.setTextColor(textColor);
        textView.setTextSize(18);
        textView.setText(""+message);
        textView.setPadding(2, 5, 2, 1);
        return textView;
    }

    //Message will be send using this thread to Client...
    private void send_message_to_client(final String message) {
        try {
            if (socket_for_client != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter printWriter = null;
                        try {
                            printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket_for_client.getOutputStream())), true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        printWriter.println(message);
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //this thread is used to start connection between server and client for communication between them...
    private class ServerThread implements Runnable {
        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(server_port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (null != serverSocket) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        Thread_for_connection commThread = new Thread_for_connection(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //this thread retunr the value to input BufferReader for communication with client...
    private class Thread_for_connection implements Runnable {

        private Socket Socket_socket;
        private BufferedReader input;
        public Thread_for_connection(Socket clientSocket) {
            Socket_socket = clientSocket;
            socket_for_client = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(Socket_socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String message = input.readLine();
                    if (message == null || "Disconnect".contentEquals(message)) {
                        Thread.interrupted();
                        message = "Client_is_Offline";
                        Message_for_Print("Client: " + message,  Color.RED, true);
                        break;
                    }else {
                        Message_for_Print("Client: " + message, textColor, true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Function call when button's will be pressed...
    public void onClick(View view) {
        if (view.getId() == R.id.start_server) {
            msgScrollViewList.removeAllViews();
            Message_for_Print("Server Started", Color.GREEN, false);
            thread_for_server = new Thread(new ServerThread());
            thread_for_server.start();
            view.setVisibility(View.GONE);
            return;
        }
        if (view.getId() == R.id.send_data_to_client) {
            String msg = editText_message.getText().toString().trim();
            Message_for_Print("Server: " + msg, R.color.colorPrimaryDark, false);
            if (msg.length() > 0) {
                send_message_to_client(msg);
            }
            editText_message.setText("");
            return;
        }
    }

    //destroy all connection when app will closed...
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (thread_for_server != null) {
            send_message_to_client("Server Disconnected");
            thread_for_server.interrupt();
        }
        thread_for_server = null;
    }
}
