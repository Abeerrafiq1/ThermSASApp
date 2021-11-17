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
       byte[] buffer = new byte[1024];
       DatagramPacket udpDatagramPacket = new DatagramPacket(buffer, 1000);
       String message;
        try {
            while (true) {
                udpDatagramSocket.receive(udpDatagramPacket);
                message = new String(buffer, 0, udpDatagramPacket.getLength());

                JSONObject obj = new JSONObject(message);
                String opcode = obj.getString("opcode");

                switch (opcode) {
                    case "2":
                        try {
                            Thread.sleep(1000);
                            loginActivity.exHandler.sendMessage(loginActivity.exHandler.obtainMessage(1, message));
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