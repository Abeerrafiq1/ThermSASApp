package com.example.thermsasapp;


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
       byte[] buffer = new byte[15048];
       DatagramPacket udpDatagramPacket = new DatagramPacket(buffer, 15000);
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
                            addSubscribersActivity.exHandler.sendMessage(addSubscribersActivity.exHandler.obtainMessage(1, message));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "10":
                        try {
                            Thread.sleep(1000);
                            viewCookingListActivity.exHandler.sendMessage(viewCookingListActivity.exHandler.obtainMessage(1, message));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "12": case "14":
                        try {
                            Thread.sleep(1000);
                            addStoveActivity.exHandler.sendMessage(addStoveActivity.exHandler.obtainMessage(1, message));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "16":
                        try {
                            Thread.sleep(1000);
                            currentSubscribersActivity.exHandler.sendMessage(currentSubscribersActivity.exHandler.obtainMessage(1, message));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "18":
                        try {
                            Thread.sleep(1000);
                            viewStoveDataActivity.exHandler.sendMessage(viewStoveDataActivity.exHandler.obtainMessage(1, message));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "22":
                        try {
                            Thread.sleep(1000);
                            notificationActivity.exHandler.sendMessage(notificationActivity.exHandler.obtainMessage(1, message));
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