package cn.joes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * 单线程基本处理器，I/O 的读写以及业务的计算均由 Reactor 线程处理
 * 
 * @see Reactor
 * @author wanqiao
 */
public class BasicHandler implements Runnable {

	public static String LOG_PROMPT = "BasicHandler";
	
	private static final int MAXIN = 1024;

	private static final int MAXOUT = 1024;

	public final SocketChannel socket;

	public final SelectionKey sk;

	public ByteBuffer input = ByteBuffer.allocate(MAXIN);

	public ByteBuffer output = ByteBuffer.allocate(MAXOUT);

	public String readContent;

	public String writeContent;

	public static final int READING = 0, SENDING = 1;

	public int state = READING;

	public BasicHandler(Selector sel, SocketChannel sc) throws IOException {
		socket = sc;
		// 设置非阻塞
		sc.configureBlocking(false);
		// Optionally try first read now// 注册通道
		sk = socket.register(sel, 0);
		// 将自身附加到 key
		sk.attach(this);
		// 初始设置处理读取事件
		sk.interestOps(SelectionKey.OP_READ);
		sel.wakeup(); // 唤醒 select() 方法，直接返回
		
		System.out.println(LOG_PROMPT + ": Register OP_READ " + sc.socket().getLocalSocketAddress() + " and wakeup Secletor");
	}

	@Override
	public void run() {
		try {
			if (state == READING){
				read(); // 此时通道已经准备好读取字节
			}

			else if (state == SENDING){
				send(); // 此时通道已经准备好写入字节
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			// 关闭连接
			try {
				sk.channel().close();
			} catch (IOException ignore) {
			}

		}
	}

	/**
	 * 从通道读取字节，不同模式下需要注意的问题，n 为 read 返回值：<p>
	 * 非阻塞模式：没读取到内容就返回了，n 可能返回 0 或 -1 <br>
	 * 阻塞模式： n > 0 永远成立，至少读到一个字节才返回
	 */
	protected void read() throws IOException {
		int n = socket.read(input);
		System.out.println(LOG_PROMPT + ": Start reading ... ");
		if (inputIsComplete(n)) {
			process();
			state = SENDING;
			// Normally also do first write now
			sk.interestOps(SelectionKey.OP_WRITE);
		}
	}
	

	/**
	 * @param bytes 读取的字节数，-1 通常是连接被关闭，0 非阻塞模式可能返回
	 */
	protected boolean inputIsComplete(int bytes) {
		// -1 关闭连接，此时 request 的长度为 0，内容为空 ""，后续处理会自动关闭
		if (bytes < 0) { return true; } 
		// 0，继续读取，不能操作缓冲区，放在 flip 之前
		if (bytes == 0) {return false;}
		
		input.flip();
		while (input.hasRemaining()) {
			byte[] readBytes = new byte[input.remaining()];
			input.get(readBytes);
			readContent = new String(readBytes);
		}
		input.clear(); // 清空继续从通道读取，之前的输入已缓存
		return true;
	}

	/**
	 * 业务处理，这里直接将输入的内容返回
	 */
	protected void process() {
		output.put("好吃的不行.".getBytes());
		readContent = "";
		System.out.println(LOG_PROMPT + ": Response - [" + readContent + "]");
	}
	
	protected void send() throws IOException {
		output.flip();
		if (output.hasRemaining()) {
			socket.write(output);
		} else {
			System.out.println(LOG_PROMPT + ": Nothing was send");
		}
		
		// 连接是否断开
		if (outputIsComplete()) {
			sk.cancel();
			System.out.println(LOG_PROMPT + ": Channel close.");
		} else {
			// 否则继续读取
			state = READING;
			sk.interestOps(SelectionKey.OP_READ);
			System.out.println(LOG_PROMPT + ": Continue reading ...");
		}
			
	}

	/**
	 * 这里
	 * 此时 request.length 为 0，有两种情况一是用户直接出入回车，二是socket读取返回-1，简单处理直接关闭
	 */
	protected boolean outputIsComplete() { 

		if (true) {
		    return true;	
		}
		// 清空旧数据，接着处理后续的请求
		output.clear();
		writeContent = "";
		return false;
	}

}
