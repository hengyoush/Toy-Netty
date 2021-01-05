package io.yhheng.eventloop;

/**
 * @version V1.0
 * @author yhheng
 * @date 2020/12/30
 */
public interface EventLoopGroup {
    EventLoop next();
}
