package joes.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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

    /**
     * 服务器端口号
     */
    public static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("客户端启动");


    }
}
