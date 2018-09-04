package cn.joes.multi;

import cn.joes.BasicHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 初始化并配置 ServerSocketChannel，注册到 mainReactor 的 Selector 上
 *
 * Created by myijoes on 2018/9/3.
 */

public class Acceptor implements Runnable {



    final Selector sel;

    final ServerSocketChannel serverSocket;

    int next = 0;

    /**
     * 从 Reactors，用于处理 I/O，可使用 BasicHandler 和  MultithreadHandler 两种处理方式
     */
    private Reactor[] subReactors;

    public Acceptor(Selector sel, int port, Reactor[] subReactors) throws IOException {
        this.sel = sel;
        this.subReactors = subReactors;
        serverSocket = ServerSocketChannel.open();
        // 绑定端口
        serverSocket.socket().bind(new InetSocketAddress(port));
        // 设置成非阻塞模式
        serverSocket.configureBlocking(false);
        // 注册到 选择器 并设置处理 socket 连接事件
        SelectionKey sk = serverSocket.register(sel, SelectionKey.OP_ACCEPT);
        sk.attach(this);
        System.out.println("mainReactor-" + "Acceptor: Listening on port: " + port);
    }

    @Override
    public void run() {
        try {
            SocketChannel sc = serverSocket.accept();
            if (sc != null) {
                System.out.println("mainReactor-" + "Acceptor: " + sc.socket().getLocalSocketAddress()
                        + " 注册到 subReactor-" + next);
                /**
                 *
                 * 将接收的连接注册到从 Reactor 上发现无法直接注册，一直获取不到锁
                 * 这是由于 从 Reactor 目前正阻塞在 select() 方法上，此方法已经锁定了 publicKeys（已注册的key)，直接注册会造成死锁
                 * 如何解决呢，直接调用 wakeup，有可能还没有注册成功又阻塞了，并且这里的 主从 Reactor 使用的是同一个类？
                 * 可以使用信号量或者 CountDownLatch 让 从Reactor 从 select 返回后先阻塞，等注册完后在执行
                 * 使用一个信号量资源数目为 1，select() 返回首先获取资源，如果获取不到那就是有连接在注册，只针对从 Reactor，主 Reactor 始终都能获取到资源
                 *
                 */
                Reactor subReactor = subReactors[next];
                Selector subSel = subReactor.getSelector();
                try {
                    // 首先占住资源
                    subReactor.semaphore.acquire();
                    // 唤醒 selector，它会释放对 publicKeys 的锁定，并且阻塞等待资源
                    subSel.wakeup();
                    // 将连接的通道注册到这个从 Reactor 上（这个 Handler 初始时调用了 wakeup）
                    new BasicHandler(subSel, sc);
//					new MultithreadHandler(subSel, sc);
                } finally {
                    // 释放资源，此时 从Reactor 获取到资源，继续后续处理
                    subReactor.semaphore.release();
                    // 当 从Reactor 处理完毕后，调用 select，会立马返回，处理新注册通道的读写
                    subSel.wakeup();
                }
                if(++next == subReactors.length){
                    next = 0;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
