package cn.joes;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Acceptor 用来处理服务器ServerSocketChannel 的所以客户端的连接事件
 *
 * @author wanqiao
 */

public class Acceptor implements Runnable {

    public static final String LOG_PROMPT = "Reactor";

    final ServerSocketChannel serverSocket;

    final Selector selector;

    public Acceptor(ServerSocketChannel serverSocket, Selector selector) {
        this.serverSocket = serverSocket;
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            // 接收连接，非阻塞模式下，没有连接直接返回 null
            SocketChannel sc = serverSocket.accept();
            if (sc != null) {
                System.out.println(LOG_PROMPT + ": Accept and handler - " + sc.socket().getLocalSocketAddress());
                // 线程池处理连接
                //new MultithreadHandler(selector, sc);
                // 单线程处理连接
                new BasicHandler(selector, sc);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
