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

 Purpose of Class: To receive messages and acknowledgements from the database server.
 The app must communicate with the database server to extract or update data in the databases.
 Then, forward the received message to the appropriate activity for exHandler to handle.
 */
class receiver extends Thread {

    // Class Variables
    private static final int receiverPort = 1100;
    private DatagramSocket udpDatagramSocket = null;
    private sender sender = new sender();
    private DatagramSocket server = null;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;

    // Create new socket for receiving
    public receiver() {
        try {
            udpDatagramSocket = new DatagramSocket(receiverPort);
        } catch (SocketException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        }
    }

    public void run() {

        // Create ack format
        JSONObject ack = new JSONObject();
        try {
            ack.put("opcode", "0");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        }

        byte[] buffer = new byte[30048];
        DatagramPacket udpDatagramPacket = new DatagramPacket(buffer, 30000);
        String message;
        try {
            while (true) {
                // Receive and store the received message
                udpDatagramSocket.receive(udpDatagramPacket);
                message = new String(buffer, 0, udpDatagramPacket.getLength());

                // Extract opcode from received message
                JSONObject obj = new JSONObject(message);
                String opcode = obj.getString("opcode");

                // If ack received, show corresponding message in debug
                if (opcode.equals ("0")){
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
                        // Send received message to loginActivity
                        sender.run(databaseServerAddr, ack.toString(), senderPort);
                        Thread.sleep(1000);
                        loginActivity.exHandler.sendMessage(loginActivity.exHandler.obtainMessage(1, message));
                        break;
                    case "4":
                        // Send received message to registerActivity
                        sender.run(databaseServerAddr, ack.toString(), senderPort);
                        Thread.sleep(1000);
                        registerActivity.exHandler.sendMessage(registerActivity.exHandler.obtainMessage(1, message));
                        break;
                    case "7": case "8":
                        // Send received message to addContactsActivity
                        sender.run(databaseServerAddr, ack.toString(), senderPort);
                        Thread.sleep(1000);
                        addContactsActivity.exHandler.sendMessage(addContactsActivity.exHandler.obtainMessage(1, message));
                        break;
                    case "10":
                        // Send received message to viewStoveVideoListActivity
                        sender.run(databaseServerAddr, ack.toString(), senderPort);
                        Thread.sleep(1000);
                        viewStoveVideoListActivity.exHandler.sendMessage(viewStoveVideoListActivity.exHandler.obtainMessage(1, message));
                        break;
                    case "12": case "14":
                        // Send received message to addStoveActivity
                        sender.run(databaseServerAddr, ack.toString(), senderPort);
                        Thread.sleep(1000);
                        addStoveActivity.exHandler.sendMessage(addStoveActivity.exHandler.obtainMessage(1, message));
                        break;
                    case "16":
                        // Send received message to currentContactsActivity
                        sender.run(databaseServerAddr, ack.toString(), senderPort);
                        Thread.sleep(1000);
                        currentContactsActivity.exHandler.sendMessage(currentContactsActivity.exHandler.obtainMessage(1, message));
                        break;
                    case "18":
                        // Send received message to viewStoveVideoAnalysisActivity
                        sender.run(databaseServerAddr, ack.toString(), senderPort);
                        Thread.sleep(1000);
                        viewStoveVideoAnalysisActivity.exHandler.sendMessage(viewStoveVideoAnalysisActivity.exHandler.obtainMessage(1, message));
                        break;
                    case "22":
                        // Send received message to messageActivity
                        sender.run(databaseServerAddr, ack.toString(), senderPort);
                        Thread.sleep(1000);
                        messageActivity.exHandler.sendMessage(messageActivity.exHandler.obtainMessage(1, message));
                        break;
                    case "25":
                        // Send received message to whoHasAddedUserAsContactActivity
                        sender.run(databaseServerAddr, ack.toString(), senderPort);
                        Thread.sleep(1000);
                        whoHasAddedUserAsContactActivity.exHandler.sendMessage(whoHasAddedUserAsContactActivity.exHandler.obtainMessage(1, message));
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        }
    }
}