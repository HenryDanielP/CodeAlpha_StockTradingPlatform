package com.nexus.trading.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    public enum Type { BUY, SELL }
    private final Type type; private final String symbol; private final int quantity; private final double price; private final LocalDateTime timestamp;
    public Transaction(Type type,String symbol,int quantity,double price){this.type=type;this.symbol=symbol;this.quantity=quantity;this.price=price;timestamp=LocalDateTime.now();}
    public Type getType(){return type;} public String getSymbol(){return symbol;} public int getQuantity(){return quantity;} public double getPrice(){return price;} public String getTime(){return timestamp.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm"));}
}
