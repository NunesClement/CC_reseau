package CC;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Client {

    static Socket client;

    public static void main(String[] args) {
        try {
            client = new Socket("127.0.0.1", 4755);
            Recive recive = new Recive();
            Send send = new Send();
            Thread sed = new Thread(send);
            Thread rec = new Thread(recive);
            rec.start();
            sed.start();
        } catch (IOException e) {
            // System.out.println(e.getMessage());
        }

    }

    static class Recive implements Runnable {
        BufferedReader reader;

        Recive() throws IOException {
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        }

        @Override
        public void run() {
            String temp = null;
            String data = "";
            ObjectMapper om = new ObjectMapper();

            while (true) {
                try {

                    if (reader.ready() && (temp = reader.readLine()) != null) {
                        data += temp;
                        // Deserialize here
                        String[] spliter = data.split("parse-here");
                        String stringUnclean = spliter[0];
                        stringUnclean = stringUnclean.replace("\n", "");

                        try {
                            Message m = om.readValue(stringUnclean, Message.class);
                            if (m.getWho().equals("Server")) {
                                System.out.println("[" + m.getWho() + "] " + m.getContent());
                            } else {
                                System.out
                                        .println(m.getWho() + " a envoyé :" + m.getContent() + " à : " + m.getToWho());
                            }

                        } catch (IOException e) {
                            System.out.println("String - " + data);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                data = "";
            }
        }

    }

    static class Send implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Writer writer = new OutputStreamWriter(client.getOutputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

                    writer.write(reader.readLine() + "\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
