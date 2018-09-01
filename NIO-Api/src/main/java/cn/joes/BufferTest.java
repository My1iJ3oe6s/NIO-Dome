package cn.joes;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BufferTest {

    public static void main(String[] args) throws IOException {
        // 创建一个容量为10的byte数据缓冲区
        ByteBuffer buff = ByteBuffer.allocate(10);
        // 填充缓冲区
        buff.put((byte)'A');
        buff.put((byte)'B');
        buff.put((byte)'C');
        buff.put((byte)'D');
        System.out.println("first put : " + new String(buff.array()));
        //翻转
        buff.flip();
        //释放
        System.out.println((char)buff.get());
        System.out.println((char)buff.get());
        //压缩
        buff.compact();
        System.out.println("compact after get : " + new String(buff.array()));
        //继续填充
        buff.put((byte)'E');
        buff.put((byte)'F');
        //输出所有
        System.out.println("put after compact : " + new String(buff.array()));
    }
}
