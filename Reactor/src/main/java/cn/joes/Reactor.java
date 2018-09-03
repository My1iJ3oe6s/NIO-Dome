package cn.joes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Reactor 接收连接，直接负责 I/O 的读写，拿到字节后有单线程和多线程两种处理器：
 * <p>
 * 单线程处理器：业务处理也由 Reactor 线程来做
 * <br>
 * 多线程处理器：业务处理由线程池线程来做
 *
 * @author wanqiao
 */
public class Reactor implements Runnable {

    public static void main(String[] args) {
        try {
            Thread th = new Thread(new Reactor(12345));
            th.setName("Reactor");
            th.start();
            th.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String LOG_PROMPT = "Reactor";

    final Selector selector;

    final ServerSocketChannel serverSocket;

    public Reactor(int port) throws IOException {
        // 选择器，为多个通道提供服务
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        // 绑定端口
        serverSocket.socket().bind(new InetSocketAddress(port));
        // 设置成非阻塞模式
        serverSocket.configureBlocking(false);
        // 注册到 选择器 并设置处理 socket 连接事件
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        // 添加一个附加对象
        sk.attach(new Acceptor(serverSocket, selector));

        System.out.println(LOG_PROMPT + ": Listening on port " + port);
    }


    @Override
    public void run() { // normally in a new Thread
        try {
            // 死循环
            while (!Thread.interrupted()) {
                selector.select(); // 阻塞，直到有通道事件就绪
                // 拿到就绪通道 SelectionKey 的集合
                Set<SelectionKey> selected = selector.selectedKeys();
                Iterator<SelectionKey> it = selected.iterator();
                while (it.hasNext()) {
                    SelectionKey skTmp = it.next();
                    String action = "";
                    if (skTmp.isReadable()) {
                        action = "OP_READ";
                    } else if (skTmp.isWritable()) {
                        action = "OP_WRITE";
                    } else if (skTmp.isAcceptable()) {
                        action = "OP_ACCEPT";
                    } else if (skTmp.isConnectable()) {
                        action = "OP_CONNECT";
                    }
                    System.out.println(LOG_PROMPT + ": Action - " + action);
                    // 分发
                    dispatch(skTmp);
                }
                selected.clear(); // 清空就绪通道的 key
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void dispatch(SelectionKey k) {
        // 拿到通道注册时附加的对象
        Runnable r = (Runnable) (k.attachment());
        if (r != null) {
            r.run();
        }
    }

}
