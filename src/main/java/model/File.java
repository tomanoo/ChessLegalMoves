package model;

public enum File {

    FILE_A("A"),
    FILE_B("B"),
    FILE_C("C"),
    FILE_D("D"),
    FILE_E("E"),
    FILE_F("F"),
    FILE_G("G"),
    FILE_H("H"),
    NONE("");

    public static File[] allFiles = values();
    
    String notation;

    File(String notation) {
        this.notation = notation;
    }

    public static File fromValue(String v) {
        return valueOf(v);
    }

    public String getNotation() {
        return notation;
    }

    public String value() {
        return name();
    }

}
