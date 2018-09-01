package joes.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Socket客户端
 *
 * @author wanqiao
 *         Created by myijoes on 2018/8/21.
 */
public class SocketClient {

    /**
     * 服务器地址
     */
    public static final String IP_ADDR = "localhost";

    private Selector selector = null;

    private volatile boolean stop = false;

    /**
     * 服务器端口号
     */
    public static final int PORT = 12345;

    public static void main(String[] args) throws IOException {
        System.out.println("客户端启动");
        SocketClient client = new SocketClient();
        client.init();
    }

    public void init() throws IOException {
        selector = Selector.open();
        SocketChannel channel = SocketChannel.open();
        // 设置为非阻塞模式，这个方法必须在实际连接之前调用(所以open的时候不能提供服务器地址，否则会自动连接)
        channel.configureBlocking(false);

        if (channel.connect(new InetSocketAddress(12345))) {
            channel.register(selector, SelectionKey.OP_READ);
            //发送消息
            doWrite(channel, "百果園的水果好吃嗎 ？？");
        } else {
            channel.register(selector, SelectionKey.OP_CONNECT);
        }
        while (!stop) {
            selector.select(1000);
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();
            SelectionKey key = null;
            while (it.hasNext()) {
                key = it.next();
                it.remove();
                handler(key);
            }
        }

    }

    private void handler(SelectionKey key) {
        // OP_CONNECT 两种情况，链接成功或失败这个方法都会返回true
        if (key.isConnectable()) {
            handlerConnection(key);
        }else if (key.isReadable()) {
            handlerReader(key);
        }
    }

    /**
     * 处理连接请求
     *
     * @param key
     * @throws IOException
     */
    public void handlerConnection(SelectionKey key)  {
        SocketChannel channel = (SocketChannel) key.channel();
        // 由于非阻塞模式，connect只管发起连接请求，finishConnect()方法会阻塞到链接结束并返回是否成功
        // 另外还有一个isConnectionPending()返回的是是否处于正在连接状态(还在三次握手中)
        try {
            if (channel.finishConnect()) {
                channel.register(selector, SelectionKey.OP_READ);
                //    new Thread(new DoWrite(channel)).start();
                doWrite(channel, "百果園的水果好吃嗎 ？？");
            } else {
                //链接失敗
                channel.close();
                key.cancel();
            }
        } catch (IOException e) {
            //链接失败，进程推出或直接抛出IOException
            System.exit(1);
        }
    }

    /**
     * 处理讀任務
     *
     * @param key
     * @throws IOException
     */
    public void handlerReader(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        //读取服务端的响应
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        String content = "";
        try {
            int readBytes = channel.read(buffer);
            if (readBytes > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                content += new String(bytes);
                stop = true;
                System.out.println(content);
                key.interestOps(SelectionKey.OP_READ);

            } else if (readBytes < 0) {
                //对端链路关闭
                key.cancel();
                channel.close();
            }
        } catch (IOException e) {
            //讀取數據出現IO Exception， 进程推出或直接抛出IOException
            System.exit(1);
        }
    }

    private  void doWrite(SocketChannel sc,String data) throws IOException{
        byte[] req =data.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(req.length);
        byteBuffer.put(req);
        byteBuffer.flip();
        sc.write(byteBuffer);
        if(!byteBuffer.hasRemaining()){
            System.out.println("發送百果園客戶的問題： " + data);
        }
    }
}

