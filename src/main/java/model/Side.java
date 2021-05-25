package model;

public enum Side {

    WHITE,
    BLACK;

    public static Side[] allSides = values();

    public static Side fromValue(String v) {
        return valueOf(v);
    }

    public String value() {
        return name();
    }

}
