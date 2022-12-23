package net.gunivers.dispenser.observer;

public enum NbtType {

    BYTE,
    BOOLEAN,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    STRING,
    LIST,
    COMPOUND,
    BYTE_ARRAY,
    INT_ARRAY,
    LONG_ARRAY;

    public static NbtType getNbtType(String type) {
        if(type.equalsIgnoreCase("uuid"))
            return INT_ARRAY;
        try {
            return NbtType.valueOf(type.toUpperCase());
        } catch(IllegalArgumentException e) {
            return NbtType.COMPOUND;
        }
    }
}
