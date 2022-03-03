package com.example.thermsasapp;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 @author: Abeer Rafiq

 Purpose of Class: To send requests to the database server so the database server can
 process the requests and/or send requested data back to the app.
 */
public class sender extends Thread {

    // Class Variables
    private DatagramPacket udpDatagramPacket;
    private DatagramSocket udpDatagramSocket;

    public void run(String databaseServerAddr, String message, int senderPort) {
        try {
            // Instantiate datagram socket and datagram packet with message to be sent
            udpDatagramSocket = new DatagramSocket();
            udpDatagramPacket = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName(databaseServerAddr), senderPort);

            // To create ack format again
            JSONObject ack = new JSONObject();
            try {
                ack.put("opcode", "0");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("AppDebug", "Error! " + e.toString());
            }

            // If sending an ack (matching ack format) then print Sending Ack
            // otherwise print Sending Msg
            if (message.equals(ack.toString())) {
                Log.d("AppDebug", "Sending Ack to DS: " + message.toString());
            } else {
                Log.d("AppDebug", "Sending Msg to DS: " + message.toString());
            }

            // Send message up to three times
            int cnt = 0;
            boolean msgNotSent = true;
            while (msgNotSent) {
                // send message
                udpDatagramSocket.send(udpDatagramPacket);
                // If sent before a time out of 3 seconds occurs, no need to resend
                try {
                    udpDatagramSocket.setSoTimeout(3000);
                    if (cnt == 0) {
                        msgNotSent = false;
                    }
                }
                // If sent after a time out of 3 seconds occurs, resend up to three times
                // since (while loop continues)
                catch (SocketException e) {
                    cnt++;
                    if (cnt >= 3) {
                        msgNotSent = false;
                    }
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        } finally {
            if (udpDatagramSocket != null) {
                udpDatagramSocket.close();
            }
        }
    }
}
