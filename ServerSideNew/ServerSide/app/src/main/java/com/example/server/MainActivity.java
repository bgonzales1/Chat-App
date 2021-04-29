package com.example.server;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import android.os.Handler;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import java.net.InetAddress;


public class MainActivity extends AppCompatActivity  {

    private ServerSocket serverSocket;//// This is a private function for the socket server
    
            private Socket socket_for_client;//// This is a private function for the socket client
    
            private Handler handler;//// This is a private function for the handler
    
            private EditText editText_message;//// This is a private function for the message
            private TextView ipTextviewID;//// This is a private function for the textview
    
            private Thread thread_for_server = null;//// This is a private function for the thread
    
         private int textColor;//// This is a private function for the textcolor
    
        private LinearLayout msgScrollViewList;//// This is a private function for the viewlist
    
    public static final int server_port = 8080;//// This is a private function for the server port

    //. With the help of this function, the ip address will be retrieved from OS.
    private String getLocalIpAddress() throws UnknownHostException {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    assert wifiManager != null;
                             WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                             int ipInt = wifiInfo.getIpAddress();
                         return InetAddress.getByAddress(
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array())
                .getHostAddress();
    }

    /// For this function to work the server side must be connected for the client side to initiate connection.
    @Override
    protected void onCreate(Bundle savedInstanceState) {    /// For this function to work the server side must be connected for the client side to initiate connection.
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

    //// In this section the process of finding the message being sent will link connection
    private void initViews() {
        handler = new Handler();//// In this section the process of finding the message being sent will link connection
                msgScrollViewList = findViewById(R.id.msgScrollViewList);
                            editText_message = findViewById(R.id.editText_message);//// In this section the process of finding the message being sent will link connection
                                ipTextviewID = findViewById(R.id.ipTextviewID);
                textColor = ContextCompat.getColor(this, R.color.colorPrimary);
    }

    ///This part is to help minimize the code and help print the following messages on the screen so that they can fit properly.
    private void Message_for_Print(final String message, final int textColor, final Boolean value) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgScrollViewList.addView(PrintMessageOnScreen(message, textColor, value));
            }
        });
    }

    //The following is for the printing of the text that a user wh=ants to send.
    public TextView PrintMessageOnScreen(String message, int textColor, Boolean value) {

        TextView textView = new TextView(this);
    //The following is for the printing of the text that a user wh=ants to send.
        if (value) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }
        textView.setTextColor(textColor);
    //The following is for the printing of the text that a user wh=ants to send.
        textView.setTextSize(18);
        textView.setText(""+message);
        textView.setPadding(2, 5, 2, 1);
        return textView;
    }

    ////The messages will be run under a thread to be delivered to the client using the following function below.
    private void send_message_to_client(final String message) {   ////The messages will be run under a thread to be delivered to the client using the following function below.
        try {
            if (socket_for_client != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter printWriter = null;   ////The messages will be run under a thread to be delivered to the client using the following function below.
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

    ///// When the previous functions are enables this will allow both the client and server to stablize a successful connection for the user
    private class ServerThread implements Runnable {
        public void run() {//The following thread will give back or return the value that corresponds to the input BufferReader in order to transfer data to the client for such user
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

    //The following thread will give back or return the value that corresponds to the input BufferReader in order to transfer data to the client for such user
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

        public void run() {//The following thread will give back or return the value that corresponds to the input BufferReader in order to transfer data to the client for such user
            while (!Thread.currentThread().isInterrupted()) {//The following thread will give back or return the value that corresponds to the input BufferReader in order to transfer data to the client for such user
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

    //This fucntion below helps command the performance of the buttons when being pressed so they can be called.
    public void onClick(View view) {  //This fucntion below helps command the performance of the buttons when being pressed so they can be called.
        if (view.getId() == R.id.start_server) {  //This fucntion below helps command the performance of the buttons when being pressed so they can be called.
            msgScrollViewList.removeAllViews();
                Message_for_Print("Server Started", Color.GREEN, false);
                    thread_for_server = new Thread(new ServerThread());
                        thread_for_server.start();
                                view.setVisibility(View.GONE);//This fucntion below helps command the performance of the buttons when being pressed so they can be called.
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

    //In the end of the whole application when the entire program is shut off by the user this will exit and kill the chat app for the user.
    @Override
    protected void onDestroy() { //In the end of the whole application when the entire program is shut off by the user this will exit and kill the chat app for the user.
        super.onDestroy(); //In the end of the whole application when the entire program is shut off by the user this will exit and kill the chat app for the user.
        if (thread_for_server != null) {
            send_message_to_client("Server Disconnected");
            thread_for_server.interrupt();
        }
        thread_for_server = null;
    }
}
