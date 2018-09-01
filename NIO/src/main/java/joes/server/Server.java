package joes.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by myijoes on 2018/8/22.
 */
public class Server {

    // 通道管理器
    private Selector selector;

    /**
     * 启动服务端测试
     *
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = new Server();
        server.initServer(12345);
        server.listen();
    }

    /**
     * 获得一个ServerSocket通道，并对该通道做一些初始化的工作
     *
     * @param port
     *            绑定的端口号
     * @throws IOException
     */
    public void initServer(int port) throws IOException {
        // 获得一个ServerSocket通道
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        // 设置通道为非阻塞(与select一起使用必须为非阻塞)
        serverChannel.configureBlocking(false);
        // 将该通道对应的ServerSocket绑定到port端口
        serverChannel.socket().bind(new InetSocketAddress(port));
        // 获得一个通道管理器
        this.selector = Selector.open();
        // 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，
        // 当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
     *
     * @throws IOException
     */
    public void listen() throws IOException, InterruptedException {
        System.out.println("服务端启动成功！");
        // 轮询访问selector
        while (true) {
            // 当注册的事件到达时，方法返回；否则,该方法会一直阻塞
            selector.select();
            // 获得selector中选中的项的迭代器，选中的项为注册的事件
            Iterator<?> ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next();
                // 由于select操作只管对selectedKeys进行添加，所以key处理后我们需要从里面把key去掉
                ite.remove();
                handler(key);
            }
            System.out.println("結束本次select的查詢");
        }
    }

    /**
     * 处理请求
     *
     * @param key
     * @throws IOException
     */
    public void handler(SelectionKey key) throws IOException {
        // 客户端请求连接事件
        if (key.isAcceptable()) {
            handlerAccept(key);
            // 获得了可读的事件
        } else if (key.isReadable()) {
            handelerRead(key);
        }
    }

    /**
     * 处理连接请求
     *
     * @param key
     * @throws IOException
     */
    public void handlerAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        // 获得和客户端连接的通道
        SocketChannel channel = server.accept();
        // 设置成非阻塞
        channel.configureBlocking(false);
        // 在这里可以给客户端发送信息哦
        System.out.println("新的客户端连接");
        // 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    /**
     * 处理读的事件
     *
     * @param key
     * @throws IOException
     */
    public void handelerRead(SelectionKey key) throws IOException {
        // 服务器可读取消息:得到事件发生的Socket通道
        SocketChannel channel = (SocketChannel) key.channel();
        // 创建读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = 0;
        try {
            read = channel.read(buffer);
        } catch (IOException e) {
            key.cancel();
            channel.close();
            return ;
        }
        String content = "";
        if(read > 0){
            buffer.flip(); //为write()准备
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            content+=new String(bytes);
            System.out.println("收到百果園客戶的問題：" + content);
            //回应客户端
            doWrite(channel);
        }else{
            System.out.println("客户端关闭");
            key.cancel();
        }
        // 写完就把状态关注去掉，否则会一直触发写事件(改变自身关注事件)
        key.interestOps(SelectionKey.OP_READ);
    }

    private void doWrite(SocketChannel sc) throws IOException{
        String content = "百果園的水果是天下最好吃的。";
        byte[] req =content.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(req.length);
        byteBuffer.put(req);
        byteBuffer.flip();
        sc.write(byteBuffer);
        if(!byteBuffer.hasRemaining()){
            System.out.println("回答百果園客戶的問題：" + content);
        }
    }

}
