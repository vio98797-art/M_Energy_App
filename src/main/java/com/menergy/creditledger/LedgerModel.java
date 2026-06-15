package com.menergy.creditledger;

public class LedgerModel {
    private final int id;
    private final String name;
    private final long amount;
    private final String type;
    private final String date;

    public LedgerModel(int id, String name, long amount, String type, String date) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public long getAmount() { return amount; }
    public String getType() { return type; }
    public String getDate() { return date; }
}
