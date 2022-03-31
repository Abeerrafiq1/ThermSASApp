package com.example.thermsasapp;
import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.SocketException;

/**
 @author: Abeer Rafiq

 Purpose of Class: To send a request to the messageRetriever to retrieve messages.
 Then wait to receive a packet with user messages/notifications from the messageRetriever.
 Once received, forward associating messages to LoginActivity exhandler to show pop ups and
 phone notifications.
 */
class messageRetriever extends Thread{

    // Class Variables
    private static final int receiverPort = 2100;
    public static DatagramSocket udpDatagramSocket = null;
    private sender sender = new sender();
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 2000;
    private String username;
    public Boolean terminate;

    // Create new socket for receiving
    public messageRetriever(String username) {
        try {
            this.username = username;
            this.terminate = false;
            udpDatagramSocket = new DatagramSocket(receiverPort);
        } catch (SocketException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        }
    }

    // When username = "NoUser" there is no polling for messages
    // When username != "NoUser", a user is logged in and polling begins
    public void setUsername(String username){
        this.username = username;
    }
    public void exitThread(Boolean TrueOrFalse){
        this.terminate = TrueOrFalse;
        if (terminate) {
            this.interrupt();
        }
    }

    public void run() {
        // Wait 5 seconds to poll after logging in
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create ack format
        JSONObject ack = new JSONObject();
        try {
            ack.put("opcode", "0");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        }

        //
        byte[] buffer = new byte[15048];
        DatagramPacket udpDatagramPacket = new DatagramPacket(buffer, 15000);
        String message;
        JSONObject messageRequest = new JSONObject();
        try {
            // - While terminate = false means user is not logged out so poll for user messages
            // - If terminate = true it means user is logged out and polling for user shouldn't continue
            //   until they login again
            while (!terminate) {
                // Create and send request to messageRetriever
                messageRequest.put("opcode", "1");
                messageRequest.put("username", username);
                sender.run(databaseServerAddr, messageRequest.toString(), senderPort);

                // Receive and store the received message or ack
                udpDatagramSocket.receive(udpDatagramPacket);
                message = new String(buffer, 0, udpDatagramPacket.getLength());

                // Extract opcode from received message
                JSONObject obj = new JSONObject(message);
                String opcode = obj.getString("opcode");

                // If ack received, show corresponding message in debug
                if (opcode.equals("0")) {
                    Log.d("AppDebug", "Received Ack from DS: " + obj.toString());
                } else {
                    Log.d("AppDebug", "Received Msg from DS: " + obj.toString());
                }

                // Based on opcode received, direct app to proper activity
                // for the exHandler to handle the received data
                switch (opcode) {
                    case "0":
                        // ACK received
                        break;
                    case "2":
                        // Get user messages and send ack
                        String messages = obj.getString("messages");
                        sender.run(databaseServerAddr, ack.toString(), senderPort);

                        // If user logged out, don't continue to retrieve messages
                        if (terminate) {
                            this.interrupt();
                            return;
                        }

                        // If there is a message that needs immediate stove attention, show a pop up and a phone notification
                        // by sending message to loginActivity exhandler
                        // Poll every 5 seconds
                        if (messages.contains("Your stove was on too long!") ||
                                messages.contains("has the stove on too long! Please make sure everything is okay")) {
                            // If message is about another user's stove
                            if (messages.contains("has the stove on too long! Please make sure everything is okay")) {
                                // Get first username the stove risk is related to
                                String[] username;
                                username = messages.split("\\* ");
                                loginActivity.popUpUsername = username[1].replace(" ", "");
                                loginActivity.exHandler1.sendMessage(loginActivity.exHandler1.obtainMessage(1, message));
                                Thread.sleep(5000);
                            }
                            // If message is about stove owner's stove (logged in user's stove)
                            if (messages.contains("Your stove was on too long!")) {
                                loginActivity.exHandler2.sendMessage(loginActivity.exHandler2.obtainMessage(1, message));
                                Thread.sleep(5000);
                            }
                        } else {
                            // Do nothing if no stove risk message
                            Thread.sleep(5000);
                        }
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        }
    }
}

