package com.nexus.trading.model;

import java.util.ArrayList;
import java.util.List;

public class Stock {
    private final String symbol, company;
    private double price;
    private final double previousClose;
    private final List<Double> history = new ArrayList<>();

    public Stock(String symbol, String company, double price, double previousClose) {
        this.symbol=symbol; this.company=company; this.price=price; this.previousClose=previousClose;
        for(int i=0;i<24;i++) history.add(Math.max(.01, price*(1+(i-12)*.002+Math.sin(i*.75)*.008)));
        history.set(history.size()-1, price);
    }
    public String getSymbol(){return symbol;} public String getCompany(){return company;}
    public double getPrice(){return price;} public double getPreviousClose(){return previousClose;}
    public List<Double> getHistory(){return List.copyOf(history);}
    public double getChange(){return price-previousClose;}
    public double getChangePercent(){return previousClose==0?0:getChange()/previousClose*100;}
    public void setPrice(double price){this.price=Math.max(.01,price);history.add(this.price);if(history.size()>36)history.remove(0);}
}
