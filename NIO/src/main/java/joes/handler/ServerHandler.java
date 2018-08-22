package joes.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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

    }
}
