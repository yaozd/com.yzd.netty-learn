package com.example.demo.httpserver;

public class Main {
    public static void main(String[] args) {
        NettyHttpServer server = new NettyHttpServer(8085);

        try {
            server.init();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("exception: " + e.getMessage());
        }
        System.out.println("server close!");

    }
}
