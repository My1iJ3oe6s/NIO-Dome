package cn.joes;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Acceptor implements Runnable {

    public static final String LOG_PROMPT = "Reactor";

    final ServerSocketChannel serverSocket;

    final Selector selector;

    public Acceptor(ServerSocketChannel serverSocket, Selector selector) {
        this.serverSocket = serverSocket;
        this.selector = selector;
    }

    public void run() {
        try {
            SocketChannel sc = serverSocket.accept(); // 接收连接，非阻塞模式下，没有连接直接返回 null
            if (sc != null) {
                System.out.println(LOG_PROMPT + ": Accept and handler - " + sc.socket().getLocalSocketAddress());
                new BasicHandler(selector, sc); // 单线程处理连接
//					new MultithreadHandler(selector, sc); // 线程池处理连接
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
