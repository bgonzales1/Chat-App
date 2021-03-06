package com.example.client;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static int server_port_string = 8080;///Private function for server port

    public static String server_ip_string = "10.0.2.16";///Private function for string

    private Thread thread;///Private function for thread

    private Handler handler;///Private function for handler

    private LinearLayout message_List_view;///Private function for message

    private int clientTextColor;///Private function for color

    private TextView textView1;///Private function for view
    private ClientThreadForConnectingToServer client_thread;///Private function for client

    private EditText Message_to_print;///Private function for message

    private static String packageName2;///Private function for string

    ////The client side of the app has to be on the display of device in order for success
    @Override
    protected void onCreate(Bundle savedInstanceState) {////void function for savedInstance
        super.onCreate(savedInstanceState);////super function on saved instance
                setContentView(R.layout.activity_main);////This creates the view of content for the primary screen on android 
                                //Set Toolbar. 
                                        setTitle("Client_Side");
                                                        //Initialize data on the client side of application
                                                                                 initViews();
        //eliminates any uneccessary background that gets in the way 
                                                                                            StopAppBackgroundResources();
    }

    /////Within this particular function it will locate the sorce of the background processes operating and take them down for better optimization
    private void StopAppBackgroundResources() {
        List<ApplicationInfo> packages;
                                     PackageManager pm;/////Within this particular function it will locate the sorce of the background processes operating and take them down for better optimization
                                                             pm = getPackageManager();
                                                                             ///With this, the amount of apps aquired are sorted in a list
                                                                                              packages = pm.getInstalledApplications(0);/////Within this particular function it will locate the sorce of the background processes operating and take them down for better optimization

                                                                             ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

                                                             for (ApplicationInfo packageInfo : packages) {
                              if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM)==1)continue;
            if(packageInfo.packageName.equals("mypackage")) continue;

                        mActivityManager.killBackgroundProcesses(packageInfo.packageName);
                                             }

                                 Toast.makeText(this, "All Background Peocess stop to increase performance of this chat...", Toast.LENGTH_SHORT).show();
    }

    private void initViews() {/////Within this particular function it will locate the sorce of the background processes operating and take them down for better optimization
        handler = new Handler();
                     message_List_view = findViewById(R.id.message_List_view);
                                textView1 = new TextView(this);
                                         Message_to_print = findViewById(R.id.Message_to_send);
                                                    clientTextColor = ContextCompat.getColor(this, R.color.colorPrimary);
    }

    //its use to print messages comming from either client itself or server
    private TextView PrintMessageOnScreen(String message, Boolean value, int textColor) {

         TextView textView = new TextView(this);
    //its use to print messages comming from either client itself or server
                             if (value) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
    //its use to print messages comming from either client itself or server
        }
                             textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0));
                                                  textView.setPadding(2, 5, 2, 1);
                                                 textView.setText("" + message);
                                        textView.setTextColor(textColor);
                             textView.setTextSize(18);
        return textView;
    }

    //Tio minimize of the amount of message printed this will just print the ones recently used for display on the screen. 
    public void Message_for_Print(final String message, final int textColor, final Boolean value) {
        handler.post(new Runnable() {    //Tio minimize of the amount of message printed this will just print the ones recently used for display on the screen. 
            @Override
            public void run() {
                message_List_view.addView(PrintMessageOnScreen(message, value, textColor));
            }
        });
    }

                            //Within the ids, they are linked together so this provides a clickable function that can be called when needed.
    public void onClick(View view) {
                if (view.getId() == R.id.start_connection) {            //Within the ids, they are linked together so this provides a clickable function that can be called when needed.
                                message_List_view.removeAllViews();
                                         client_thread = new ClientThreadForConnectingToServer();
                                                 Message_for_Print("Connected", clientTextColor, false);
                                                    thread = new Thread(client_thread);            //Within the ids, they are linked together so this provides a clickable function that can be called when needed.
                                        thread.start();
                            return;
        }
        if (view.getId() == R.id.send_to_server) {
            String myMsg = Message_to_print.getText().toString().trim();
            Message_for_Print("MyMsg: "+myMsg, Color.GREEN, false);
            if (null != client_thread) {
                Message_to_print.setText("");
                if (myMsg.length() > 0) {
                    client_thread.send_message_to_server(myMsg);
                }
            }
        }
    }

    //. The background will be running a thread for the remaining time that the server will be actice and alive.
    private class ClientThreadForConnectingToServer implements Runnable {

        private Socket socket;
    //. The background will be running a thread for the remaining time that the server will be actice and alive.
                     private BufferedReader input;

        //method is called for in this function
        @Override
             public void run() {
            try {
                InetAddress server_address = InetAddress.getByName(server_ip_string); //method is called for in this function
                     socket = new Socket(server_address, server_port_string);
                     while (!Thread.currentThread().isInterrupted()) { //method is called for in this function
                       this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                      String message = input.readLine();
                         if (message == null || "Disconnect from Server".contentEquals(message)) {
                         Thread.interrupted();
                                  message = "Disconnected from server";
                                          Message_for_Print(message, Color.RED, false);
                                                 break;
                    }
                    Message_for_Print("Server: " + message, clientTextColor, true);
                }

            } catch (UnknownHostException exception) {
                exception.printStackTrace();
                 } catch (IOException exception) {
                                  Message_for_Print("Check your server IP and try again", Color.RED, false);
                  Thread.interrupted();
                          exception.printStackTrace();
                          } catch (NullPointerException e3) {
            }
        }

        ////When a message is sent through the client it will travel through this thread after the msg is delivered
        public void send_message_to_server(final String message) {  ////When a message is sent through the client it will travel through this thread after the msg is delivered
            new Thread(new Runnable() {
                @Override
                public void run() {  ////When a message is sent through the client it will travel through this thread after the msg is delivered
                    try {
                        if (socket != null) {
                            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                            out.println(message);
                        }
                                } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /// This will kill the connection linked to the client side shortly after the application is taken out of the background process
    @Override
    protected void onDestroy() {    /// This will kill the connection linked to the client side shortly after the application is taken out of the background process
              super.onDestroy();
                             if (client_thread != null) {
                                       client_thread.send_message_to_server("Server Disconnected");
                                }
        client_thread = null;
    }
}
