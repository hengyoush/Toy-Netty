package io.yhheng.buffer;

import java.util.ArrayList;
import java.util.List;

public class AdaptiveRecvAllocator extends DefaultByteBufAllocator {
    private int[] SIZE_TABLE;

    public AdaptiveRecvAllocator(int maxMessagesPerRead, int minIndex, int maxIndex, int initialIndex) {
        super(maxMessagesPerRead);
        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
        this.index = this.initialIndex = initialIndex;
        buildSizeTable();
    }

    private void buildSizeTable() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 512; i += 16) {
            list.add(i);
        }

        for (int i = 512; i > 0; i <<= 1) {
            list.add(i);
        }

        SIZE_TABLE = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            SIZE_TABLE[i] = list.get(i);
        }
    }

    private int getIndexFromSizeTable(int size) {
        int l = 0, h = SIZE_TABLE.length - 1;
        while (true) {
            if (h < l) {
                return l;
            }

            if (h == l) {
                return h;
            }

            int m = l + h >>> 1;
            int a = SIZE_TABLE[m];
            int b = SIZE_TABLE[m + 1];
            if (size < a) {
                h = m - 1;
            } else if (size > b) {
                l = m + 1;
            } else if (size == a) {
                return m;
            } else {
                return m + 1;
            }
        }
    }

    private boolean decreaseNow = false;
    private int index;
    private int nextReceiveBytes;

    private final int DECREMENT = 1;
    private final int INCREMENT = 4;
    private final int minIndex;
    private final int maxIndex;
    private final int initialIndex;

    @Override
    public void recordBytesRead(int attemptBytesRead, int realBytesRead) {
        if (realBytesRead < SIZE_TABLE[index - DECREMENT - 1]) {
            if (decreaseNow) {
                index = Math.max(minIndex, index - DECREMENT);
                nextReceiveBytes = SIZE_TABLE[index];
            } else {
                decreaseNow = true;
            }
        } else if (realBytesRead >= nextReceiveBytes) {
            index = Math.min(maxIndex, index + INCREMENT);
            nextReceiveBytes = SIZE_TABLE[index];
            decreaseNow = false;
        }
        super.recordBytesRead(attemptBytesRead, realBytesRead);
    }

    @Override
    int guess() {
        return nextReceiveBytes;
    }
}
