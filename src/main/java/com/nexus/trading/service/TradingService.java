package com.nexus.trading.service;

import com.nexus.trading.model.*;
import java.util.*;

public class TradingService {
    private final User user=new User("Henry",100000);
    private final LinkedHashMap<String,Stock> market=new LinkedHashMap<>();
    private final LinkedHashMap<String,Position> positions=new LinkedHashMap<>();
    private final ArrayList<Transaction> transactions=new ArrayList<>();

    public TradingService(){
        add("NVDA","NVIDIA",142.31,138.72); add("AAPL","Apple",238.45,235.92);
        add("MSFT","Microsoft",511.20,505.40); add("AMZN","Amazon",228.16,224.88);
        add("TSLA","Tesla",346.70,338.55); add("GOOGL","Alphabet",251.34,247.92);
        add("META","Meta Platforms",765.20,754.61); add("AMD","Advanced Micro Devices",174.83,170.45);
        seedPortfolio();
    }
    private void add(String s,String c,double p,double pc){market.put(s,new Stock(s,c,p,pc));}
    private void seedPortfolio(){buy("NVDA",35);buy("AAPL",20);buy("MSFT",10);buy("AMD",15);}
    public Collection<Stock> getMarket(){return market.values();} public Collection<Position> getPositions(){return positions.values();}
    public List<Transaction> getTransactions(){return List.copyOf(transactions);} public User getUser(){return user;}
    public Stock getStock(String symbol){return market.get(symbol);}
    public boolean buy(String symbol,int quantity){
        Stock s=market.get(symbol); if(s==null||quantity<=0)return false; double cost=s.getPrice()*quantity;
        if(!user.withdraw(cost))return false;
        positions.compute(symbol,(k,p)->{if(p==null)return new Position(symbol,quantity,s.getPrice());p.buy(quantity,s.getPrice());return p;});
        transactions.add(new Transaction(Transaction.Type.BUY,symbol,quantity,s.getPrice())); return true;
    }
    public boolean sell(String symbol,int quantity){
        Stock s=market.get(symbol);Position p=positions.get(symbol);
        if(s==null||p==null||quantity<=0||quantity>p.getQuantity())return false;
        user.deposit(s.getPrice()*quantity);p.sell(quantity);if(p.getQuantity()==0)positions.remove(symbol);
        transactions.add(new Transaction(Transaction.Type.SELL,symbol,quantity,s.getPrice()));return true;
    }
    public double getMarketValue(){return positions.values().stream().mapToDouble(p->getStock(p.getSymbol()).getPrice()*p.getQuantity()).sum();}
    public double getCostBasis(){return positions.values().stream().mapToDouble(p->p.getAverageCost()*p.getQuantity()).sum();}
    public double getProfitLoss(){return getMarketValue()-getCostBasis();}
    public double getPortfolioValue(){return user.getCash()+getMarketValue();}
    public double getReturnPercent(){return (getPortfolioValue()-100000)/100000*100;}
    public void simulateTick(){for(Stock s:market.values())s.setPrice(s.getPrice()+(Math.random()-.48)*s.getPrice()*.004);}
}
