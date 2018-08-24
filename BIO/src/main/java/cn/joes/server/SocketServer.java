package cn.joes.server;


import cn.joes.handler.ServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 服务端
 *
 * @author wanqiao
 * Created by myijoes on 2018/8/21.
 */
public class SocketServer {


    public static void main(String[] args) throws IOException {
        //创建socket服务,监听10101端口
        ServerSocket server=new ServerSocket(12345);
        System.out.println("服务器启动！");

        while(true){
            // 获取一个套接字（阻塞）
            final Socket socket = server.accept();
            System.out.println("开始处理通信");
            new Thread(new ServerHandler(socket)).start();
        }

    }

}
