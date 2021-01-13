package io.yhheng.buffer;

public class FixBufferSizeRecvAllocator extends DefaultByteBufAllocator {
    private final int bufferSize;
    public FixBufferSizeRecvAllocator(int maxMessagesPerRead, int bufferSize) {
        super(maxMessagesPerRead);
        this.bufferSize = bufferSize;
    }

    @Override
    int guess() {
        return bufferSize;
    }
}
