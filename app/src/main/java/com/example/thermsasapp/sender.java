package com.example.thermsasapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class sender extends Thread {
    private DatagramPacket udpDatagramPacket;
    private DatagramSocket udpDatagramSocket;

    public void run(String databaseServerAddr, String message, int senderPort) {
        try {
            udpDatagramSocket = new DatagramSocket();
            udpDatagramPacket = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName(databaseServerAddr), senderPort);
            udpDatagramSocket.send(udpDatagramPacket);
//            boolean notReceived = true;
//            int count = 0;
//            while (notReceived) {
//                udpDatagramSocket.send(udpDatagramPacket);
//                try {
//                    udpDatagramSocket.setSoTimeout(3000); //times out in 3s
//                    if (count == 0) { //sent it before timeout
//                        notReceived = false;
//                        Log.d("User", "falselsl");
//                    }
//                } catch (SocketException e) {
//                    count++;
//                    if (count >= 3) { //already sent 3 times don't send anymore
//                        notReceived = false;
//                        Log.d("User", "falselsllelse");
//                    }
//                    e.printStackTrace();
//                }
//            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (udpDatagramSocket != null) {
                udpDatagramSocket.close();
            }
        }
    }

}
