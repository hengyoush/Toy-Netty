package io.yhheng.channel;

import io.netty.buffer.ByteBuf;
import io.yhheng.concurrent.Promise;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ChannelOutboundBufferImpl implements ChannelOutboundBuffer {
    // 第一个要flush的Entry
    private Entry flushedEntry;
    // 第一个没有加入到flush中的
    private Entry unflushedEntry;
    // 最后一个
    private Entry tailEntry;

    private int flushed;

    @Override
    public void addMessage(Object msg, int size, Promise<Void> promise) {
        Entry entry = new Entry();
        if (tailEntry == null) {
            tailEntry = entry;
            flushedEntry = entry;
        } else {
            tailEntry.next = entry;
            tailEntry = entry;
        }

        if (unflushedEntry == null) {
            unflushedEntry = entry;
        }
    }

    @Override
    public void addFlush() {
        if (unflushedEntry != null) {
            Entry unflushedEntry = this.unflushedEntry;
            while (unflushedEntry != null) {
                flushed++;
                unflushedEntry = unflushedEntry.next;
            }
        }
    }

    @Override
    public Object current() {
        if (flushedEntry != null) {
            return flushedEntry.msg;
        }
        return null;
    }

    @Override
    public boolean remove() {
        if (flushedEntry == null) {
            return false;
        }

        Entry entry = this.flushedEntry;
        Promise<Void> promise = entry.promise;

        if (tailEntry == entry) {
            tailEntry = null;
            unflushedEntry = null;
        }
        flushedEntry = flushedEntry.next;

        promise.setSuccess(null);
        entry.recycle();

        return true;
    }

    @Override
    public boolean remove(Throwable cause) {
        if (flushedEntry == null) {
            return false;
        }

        Entry entry = this.flushedEntry;
        Promise<Void> promise = entry.promise;

        if (tailEntry == entry) {
            tailEntry = null;
            unflushedEntry = null;
        }
        flushedEntry = flushedEntry.next;

        promise.setFail(cause);
        entry.recycle();

        return true;
    }

    @Override
    public void removeBytes(int removeBytes) {

    }

    @Override
    public int size() {
        return flushed;
    }

    @Override
    public boolean isEmpty() {
        return flushed == 0;
    }

    @Override
    public ByteBuffer[] nioBuffers(int maxCount, long maxBytes) {
        int nioBuffersCount = 0;
        int nioBufferBytes = 0;
        Entry entry = this.flushedEntry;
        List<ByteBuffer> result = new ArrayList<>();
        while (entry != null && entry != unflushedEntry && nioBuffersCount < maxCount) {
            ByteBuf buf = (ByteBuf) entry.msg;
            int readableBytes = buf.readableBytes();
            if (readableBytes > 0) {
                if (maxBytes < readableBytes + nioBufferBytes && nioBuffersCount != 0) {
                    break;
                }

                int count = buf.nioBufferCount();
                if (count == 1) {
                    result.add(buf.nioBuffer());
                    nioBufferBytes += buf.readableBytes();
                    nioBuffersCount++;
                } else if (count > 1) {
                    ByteBuffer[] byteBuffers = buf.nioBuffers();
                    for (int i = 0; i < byteBuffers.length && i < maxCount - nioBuffersCount; i++) {
                        ByteBuffer byteBuffer = byteBuffers[i];
                        if (byteBuffer.hasRemaining()) {
                            result.add(byteBuffer);
                            nioBufferBytes += byteBuffer.remaining(); // TODO 对不对？
                            nioBuffersCount++;
                        }
                    }
                }
            }

            entry = entry.next;
        }
        return result.toArray(new ByteBuffer[]{});
    }

    private static class Entry {
        Object msg;
        Entry next;
        Promise<Void> promise;

        public void recycle() {

        }
    }
}
