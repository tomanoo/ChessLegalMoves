package model;

public enum Rank {

    RANK_1("1"),
    RANK_2("2"),
    RANK_3("3"),
    RANK_4("4"),
    RANK_5("5"),
    RANK_6("6"),
    RANK_7("7"),
    RANK_8("8"),
    NONE("");

    public static Rank[] allRanks = values();
    
    String notation;

    Rank(String notation) {
        this.notation = notation;
    }

    public static Rank fromValue(String v) {
        return valueOf(v);
    }

    public String getNotation() {
        return notation;
    }

    public String value() {
        return name();
    }

}
