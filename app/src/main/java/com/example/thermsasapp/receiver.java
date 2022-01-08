package com.example.thermsasapp;


import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.SocketException;


class receiver extends Thread {
    private static final int receiverPort = 1100;
    private DatagramSocket udpDatagramSocket = null;

    public receiver() {
        try {
            udpDatagramSocket = new DatagramSocket(receiverPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
       byte[] buffer = new byte[2048];
       DatagramPacket udpDatagramPacket = new DatagramPacket(buffer, 2000);
       String message;
        try {
            while (true) {
                udpDatagramSocket.receive(udpDatagramPacket);
                message = new String(buffer, 0, udpDatagramPacket.getLength());

                JSONObject obj = new JSONObject(message);
                String opcode = obj.getString("opcode");

                switch (opcode) {
                    case "2": case "10":
                        try {
                            Thread.sleep(1000);
                            loginActivity.exHandler.sendMessage(loginActivity.exHandler.obtainMessage(1, message));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "4":
                        try {
                            Thread.sleep(1000);
                            registerActivity.exHandler.sendMessage(registerActivity.exHandler.obtainMessage(1, message));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "7": case "8":
                        try {
                            Thread.sleep(1000);
                            MainActivity.notifications.add(0, "help");
                            addSubscribersActivity.exHandler.sendMessage(addSubscribersActivity.exHandler.obtainMessage(1, message));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}