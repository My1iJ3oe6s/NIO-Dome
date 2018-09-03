package cn.joes.multi;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * 对 Selector 的封装，主要是调用 SelectionKey.attachment() 的 run 方法 <p>
 * attachment 可能是 Acceptor 或者 Handler
 *
 * @author wanqiao
 * Created by myijoes on 2018/9/3.
 */
public class Reactor implements Runnable {

    /**
     * 控制 从 Reactor 注册通道
     */
    final Semaphore semaphore = new Semaphore(1);

    final Selector selector;

    public Reactor() throws IOException {
        selector = Selector.open();
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    public void run() {
        try {
            // 死循环
            while (!Thread.interrupted()) {
                try {
                    // 阻塞，直到有通道事件就绪
                    selector.select();
                    // 是否有通道在注册，有则阻塞，无则继续执行
                    semaphore.acquire();
                    // 拿到就绪通道 SelectionKey 的集合
                    Set<SelectionKey> selected = selector.selectedKeys();
                    Iterator<SelectionKey> it = selected.iterator();
                    while (it.hasNext()) {
                        SelectionKey skTmp = it.next();
                        // 根据 key 的事件类型进行分发
                        dispatch(skTmp);
                    }
                    // 清空就绪通道的 key
                    selected.clear();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void dispatch(SelectionKey k) {
        // 拿到通道注册时附加的对象
        Runnable r = (Runnable) (k.attachment());
        if (r != null){
            r.run();
        }
    }
}
