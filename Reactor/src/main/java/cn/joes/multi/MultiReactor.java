package cn.joes.multi;

import cn.joes.BasicHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 为了匹配 CPU 和 IO 的速率，可设计多个 Reactor（即 Selector 池）：<p>
 * 主 Reacotr 负责监听连接，然后将连接注册到从 Reactor，将 I/O 转移了<br>
 * 从 Reacotr 负责通道 I/O 的读写，处理器可选择单线程或线程池
 * <p>
 * <b>注意这里的 Reactor 是 MultiReactor 的内部类</b>
 *
 * @see Reactor
 * @author wanqiao
 */
public class MultiReactor {

	public static void main(String[] args) throws IOException {
		MultiReactor mr = new MultiReactor(10393);
		mr.start();
	}

	private static final int POOL_SIZE = 3;

	/**
	 * Reactor（Selector） 线程池，其中一个线程被 mainReactor 使用，剩余线程都被 subReactor 使用
	 */
	static Executor selectorPool = Executors.newFixedThreadPool(POOL_SIZE);

	/**
	 * 主 Reactor，接收连接，把 SocketChannel 注册到从 Reactor 上
 	 */
	private Reactor mainReactor;

	/**
	 * 从 Reactors，用于处理 I/O，可使用 BasicHandler 和  MultithreadHandler 两种处理方式
	 */
	private Reactor[] subReactors = new Reactor[POOL_SIZE - 1];

	public MultiReactor(int port) {
		try {
			this.port = port;
			mainReactor = new Reactor();

			for (int i = 0; i < subReactors.length; i++) {
				subReactors[i] = new Reactor();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private int port;
	/**
	 * 启动主从 Reactor，初始化并注册 Acceptor 到主 Reactor
	 */
	public void start() throws IOException {
		Thread mrThread = new Thread(mainReactor);
		mrThread.setName("mainReactor");
		// 将 ServerSocketChannel 注册到 mainReactor
		new Acceptor(mainReactor.getSelector(), port, subReactors);
		selectorPool.execute(mrThread);

		for (int i = 0; i < subReactors.length; i++) {
			Thread srThread = new Thread(subReactors[i]);
			srThread.setName("subReactor-" + i);
			selectorPool.execute(srThread);
		}
	}

}
