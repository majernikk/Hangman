package org.games.hangman;

import java.io.*;
import java.net.*;

public class Server{
    public static void main(String[] args){
        try{
            ServerSocket serverSocket = new ServerSocket(3333);
            Socket socket = serverSocket.accept();
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            while(true){
                String str = dis.readUTF();
                long millis = System.currentTimeMillis();
                java.util.Date date = new java.util.Date(millis);
                System.out.println(date+": " + str);
            }

        }catch(Exception e){
            System.out.println(e);
        }
    }
}