package com.nexus.trading.model;

public class User {
    private final String name; private double cash;
    public User(String name,double cash){this.name=name;this.cash=cash;}
    public String getName(){return name;} public double getCash(){return cash;}
    public void deposit(double amount){if(amount>0)cash+=amount;}
    public boolean withdraw(double amount){if(amount<=0||amount>cash)return false;cash-=amount;return true;}
}
