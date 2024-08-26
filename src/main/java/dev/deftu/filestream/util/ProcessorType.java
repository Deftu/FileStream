package dev.deftu.filestream.util;

public enum ProcessorType {

    UNKNOWN,
    X86((byte) 32),
    X64((byte) 64);

    public final byte bitSize;

    ProcessorType(byte bitSize) {
        this.bitSize = bitSize;
    }

    ProcessorType() {
        this((byte) -1);
    }

}
