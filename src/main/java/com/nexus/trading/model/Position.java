package com.nexus.trading.model;

public class Position {
    private final String symbol; private int quantity; private double averageCost;
    public Position(String symbol,int quantity,double averageCost){this.symbol=symbol;this.quantity=quantity;this.averageCost=averageCost;}
    public String getSymbol(){return symbol;} public int getQuantity(){return quantity;} public double getAverageCost(){return averageCost;}
    public void buy(int qty,double price){double total=averageCost*quantity+price*qty;quantity+=qty;averageCost=total/quantity;}
    public void sell(int qty){quantity-=qty;}
}
