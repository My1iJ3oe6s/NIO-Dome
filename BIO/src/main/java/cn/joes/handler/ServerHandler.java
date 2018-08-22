package cn.joes.handler;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * socket服务器的处理类
 *
 * @author wanqiao
 * Created by myijoes on 2018/8/21.
 */
public class ServerHandler implements Runnable{

    private Socket socket;

    public ServerHandler(Socket socket) {
        this.socket = socket;
    }


    @Override
    public void run() {
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            System.out.println("接收到的客户端消息为:" + input.readUTF());

            Thread.sleep(3000);
            //发送消息
            out.writeUTF("已收到消息");

        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                System.out.println("socket关闭");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
