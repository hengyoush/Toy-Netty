package io.yhheng.channel;

public class DefaultChannelHandlerContext extends AbstractChannelHandlerContext {
    private final ChannelHandler channelHandler;

    public DefaultChannelHandlerContext(ChannelPipeline channelPipeline,
                                        ChannelHandler channelHandler) {
        super(channelPipeline);
        this.channelHandler = channelHandler;
        init();
    }

    @Override
    public ChannelHandler handler() {
        return channelHandler;
    }
}
