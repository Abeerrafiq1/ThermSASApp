package com.example.thermsasapp;

import android.content.Context;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.UnknownHostException;

public class sender extends Thread {
    private DatagramPacket udpDatagramPacket;
    private DatagramSocket udpDatagramSocket;

    public void run(String databaseServerAddr, String message, int senderPort) {
        try {
            udpDatagramSocket = new DatagramSocket();
            udpDatagramPacket = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName(databaseServerAddr), senderPort);
            udpDatagramSocket.send(udpDatagramPacket);
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
