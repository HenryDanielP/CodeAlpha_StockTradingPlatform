package com.nexus.trading;

import com.nexus.trading.model.*;
import com.nexus.trading.service.TradingService;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.*;

public class TradingView {
    private final TradingService service=new TradingService();
    private final BorderPane root=new BorderPane();
    private final Label portfolio=new Label(), cash=new Label(), pnl=new Label(), returns=new Label();
    private final Label asset=new Label(), price=new Label(), change=new Label();
    private final LineChart<String,Number> chart;
    private final TextField search=new TextField();
    private String selected="NVDA";
    private VBox marketRows=new VBox(8);

    public TradingView(){
        CategoryAxis x=new CategoryAxis(); NumberAxis y=new NumberAxis();
        chart=new LineChart<>(x,y); chart.setLegendVisible(false); chart.setAnimated(true);
        chart.setCreateSymbols(false); chart.setPrefHeight(340);
    }
    public BorderPane create(){
        root.getStyleClass().add("app"); root.setLeft(sidebar()); root.setTop(topbar());
        showDashboard(); startMarketTicker(); return root;
    }
    private Node sidebar(){
        VBox s=new VBox(18); s.getStyleClass().add("sidebar"); s.setPadding(new Insets(26,18,24,18));
        Label logo=new Label("NEXUS"), sub=new Label("MARKET INTELLIGENCE");
        logo.getStyleClass().add("logo"); sub.getStyleClass().add("eyebrow");
        s.getChildren().addAll(logo,sub,new Separator());
        s.getChildren().addAll(nav("⌂","Dashboard",this::showDashboard),nav("◈","Markets",this::showMarkets),
            nav("▣","Portfolio",this::showPortfolio),nav("↻","Transactions",this::showTransactions));
        Region r=new Region(); VBox.setVgrow(r,Priority.ALWAYS);
        Label task=new Label("CODEALPHA • TASK 2");task.getStyleClass().add("muted");s.getChildren().addAll(r,task);return s;
    }
    private Button nav(String i,String t,Runnable action){
        Button b=new Button(i+"   "+t);b.getStyleClass().add("nav-button");b.setMaxWidth(Double.MAX_VALUE);b.setAlignment(Pos.CENTER_LEFT);
        b.setOnAction(e->{action.run();fade(root.getCenter());});return b;
    }
    private Node topbar(){
        HBox b=new HBox(14);b.getStyleClass().add("topbar");b.setAlignment(Pos.CENTER_LEFT);
        Label t=new Label("TRADING TERMINAL");t.getStyleClass().add("page-title");
        Region r=new Region();HBox.setHgrow(r,Priority.ALWAYS);
        Label open=new Label("● MARKET OPEN"), user=new Label("HENRY  •  PAPER ACCOUNT");
        open.getStyleClass().add("market-open");user.getStyleClass().add("user-chip");b.getChildren().addAll(t,r,open,user);return b;
    }
    private void showDashboard(){
        VBox box=new VBox(18);box.getChildren().addAll(heading("Market overview","Simulated market data • paper trading environment"),
            stats(),dashboardGrid(),section("Market pulse"),marketStrip());setContent(box);
    }
    private Node stats(){
        HBox h=new HBox(14);h.getChildren().addAll(card("PORTFOLIO VALUE",portfolio,()->money(service.getPortfolioValue())),
            card("CASH AVAILABLE",cash,()->money(service.getUser().getCash())),card("TOTAL P/L",pnl,()->money(service.getProfitLoss())),
            card("RETURN",returns,()->String.format("%+.2f%%",service.getReturnPercent())));return h;
    }
    private VBox card(String title,Label value,java.util.function.Supplier<String> supplier){
        VBox v=new VBox(8);v.getStyleClass().add("stat-card");Label t=new Label(title);t.getStyleClass().add("stat-title");
        value.setText(supplier.get());value.getStyleClass().add("stat-value");v.getChildren().addAll(t,value);HBox.setHgrow(v,Priority.ALWAYS);return v;
    }
    private Node dashboardGrid(){
        HBox g=new HBox(18);VBox left=new VBox(12),right=new VBox(12);left.getStyleClass().add("panel");right.getStyleClass().add("panel");
        left.setPadding(new Insets(18));right.setPadding(new Insets(18));
        asset.getStyleClass().add("asset-symbol");price.getStyleClass().add("asset-price");change.getStyleClass().add("change-up");
        HBox ah=new HBox(12,asset,price,change);ah.setAlignment(Pos.CENTER_LEFT);refreshSelected();
        left.getChildren().addAll(section("Selected asset"),ah,chart);refreshChart();
        right.getChildren().addAll(section("Quick trade"),tradeControls(),section("Holdings"),holdings());
        HBox.setHgrow(left,Priority.ALWAYS);HBox.setHgrow(right,Priority.ALWAYS);g.getChildren().addAll(left,right);return g;
    }
    private Node tradeControls(){
        VBox v=new VBox(10);ComboBox<String> symbol=new ComboBox<>();symbol.getItems().addAll(service.getMarket().stream().map(Stock::getSymbol).toList());
        symbol.setValue(selected);symbol.setMaxWidth(Double.MAX_VALUE);Spinner<Integer> qty=new Spinner<>(1,1000,10);qty.setMaxWidth(Double.MAX_VALUE);
        HBox buttons=new HBox(8);Button buy=new Button("BUY"),sell=new Button("SELL");buy.getStyleClass().add("buy-button");sell.getStyleClass().add("sell-button");
        buy.setMaxWidth(Double.MAX_VALUE);sell.setMaxWidth(Double.MAX_VALUE);HBox.setHgrow(buy,Priority.ALWAYS);HBox.setHgrow(sell,Priority.ALWAYS);
        buy.setOnAction(e->trade(symbol.getValue(),qty.getValue(),true));sell.setOnAction(e->trade(symbol.getValue(),qty.getValue(),false));buttons.getChildren().addAll(buy,sell);
        symbol.setOnAction(e->{selected=symbol.getValue();refreshSelected();refreshChart();});
        v.getChildren().addAll(field("ASSET"),symbol,field("QUANTITY"),qty,buttons);return v;
    }
    private VBox holdings(){
        VBox v=new VBox(7);for(Position p:service.getPositions()){Stock s=service.getStock(p.getSymbol());Label l=new Label(p.getSymbol()+"   "+p.getQuantity()+" shares   "+money(s.getPrice()*p.getQuantity()));l.getStyleClass().add("row-label");v.getChildren().add(l);}return v;
    }
    private void showMarkets(){
        VBox box=new VBox(14);box.getChildren().add(heading("Markets","Browse instruments and place paper trades"));
        search.setPromptText("Search symbol or company...");search.setText("");marketRows=new VBox(8);
        search.textProperty().addListener((o,a,b)->refreshMarkets());box.getChildren().addAll(search,marketRows);refreshMarkets();setContent(box);
    }
    private void refreshMarkets(){
        marketRows.getChildren().clear();String q=search.getText().toLowerCase();
        for(Stock s:service.getMarket()){if(!q.isBlank()&&!s.getSymbol().toLowerCase().contains(q)&&!s.getCompany().toLowerCase().contains(q))continue;
            HBox r=new HBox(12);r.getStyleClass().add("market-row");r.setAlignment(Pos.CENTER_LEFT);
            Label sy=new Label(s.getSymbol()),co=new Label(s.getCompany()),pr=new Label(money(s.getPrice())),ch=new Label(String.format("%+.2f%%",s.getChangePercent()));
            sy.getStyleClass().add("symbol");co.getStyleClass().add("muted");pr.getStyleClass().add("price");ch.getStyleClass().add(s.getChange()>=0?"change-up":"change-down");
            Region sp=new Region();HBox.setHgrow(sp,Priority.ALWAYS);Button tr=new Button("TRADE");tr.setOnAction(e->{selected=s.getSymbol();showDashboard();});
            r.getChildren().addAll(sy,co,sp,pr,ch,tr);marketRows.getChildren().add(r);
        }
    }
    private Node marketStrip(){
        HBox h=new HBox(10);for(Stock s:service.getMarket()){Label l=new Label(s.getSymbol()+"  "+String.format("%.2f",s.getPrice()));l.getStyleClass().add("ticker");h.getChildren().add(l);}return h;
    }
    private void showPortfolio(){
        VBox box=new VBox(14);box.getChildren().addAll(heading("Portfolio","Positions, exposure and unrealized performance"),stats());
        VBox rows=new VBox(8);for(Position p:service.getPositions()){Stock s=service.getStock(p.getSymbol());double value=s.getPrice()*p.getQuantity(),pl=(s.getPrice()-p.getAverageCost())*p.getQuantity();
            HBox r=new HBox(18);r.getStyleClass().add("market-row");r.setAlignment(Pos.CENTER_LEFT);r.getChildren().addAll(big(p.getSymbol()),big(p.getQuantity()+" shares"),big(money(p.getAverageCost())),big(money(value)),big(String.format("%+.2f",pl)));rows.getChildren().add(r);}
        box.getChildren().addAll(section("Open positions"),rows);setContent(box);
    }
    private void showTransactions(){
        VBox box=new VBox(14);box.getChildren().add(heading("Transactions","Complete audit trail of paper trades"));VBox rows=new VBox(8);
        List<Transaction> tx=new ArrayList<>(service.getTransactions());Collections.reverse(tx);
        for(Transaction t:tx){HBox r=new HBox(14);r.getStyleClass().add("market-row");r.getChildren().addAll(badge(t.getType().name()),big(t.getSymbol()),big(t.getQuantity()+" shares"),big(money(t.getPrice())),big(t.getTime()));rows.getChildren().add(r);}
        ScrollPane sp=new ScrollPane(rows);sp.setFitToWidth(true);VBox.setVgrow(sp,Priority.ALWAYS);box.getChildren().add(sp);setContent(box);
    }
    private void trade(String symbol,int qty,boolean buy){
        boolean ok=buy?service.buy(symbol,qty):service.sell(symbol,qty);
        Alert a=new Alert(ok?Alert.AlertType.INFORMATION:Alert.AlertType.WARNING);a.setTitle(ok?"Trade Executed":"Trade Rejected");
        a.setHeaderText(ok?(buy?"BUY order completed":"SELL order completed"):"Order could not be completed");
        a.setContentText(ok?qty+" shares of "+symbol+" at "+money(service.getStock(symbol).getPrice()):"Check cash balance and available position quantity.");
        a.showAndWait();showDashboard();
    }
    private void refreshSelected(){Stock s=service.getStock(selected);if(s==null)return;asset.setText(s.getSymbol()+"  •  "+s.getCompany());price.setText(money(s.getPrice()));change.setText(String.format("%+.2f%%",s.getChangePercent()));change.getStyleClass().removeAll("change-up","change-down");change.getStyleClass().add(s.getChange()>=0?"change-up":"change-down");}
    private void refreshChart(){Stock s=service.getStock(selected);if(s==null)return;chart.getData().clear();XYChart.Series<String,Number> ser=new XYChart.Series<>();int i=1;for(double n:s.getHistory())ser.getData().add(new XYChart.Data<>(String.valueOf(i++),n));chart.getData().add(ser);}
    private void setContent(Node n){VBox wrapper=new VBox(n);wrapper.setPadding(new Insets(24));ScrollPane sp=new ScrollPane(wrapper);sp.setFitToWidth(true);sp.getStyleClass().add("content-scroll");root.setCenter(sp);fade(wrapper);}
    private Node heading(String a,String b){VBox v=new VBox(5);Label t=new Label(a.toUpperCase()),s=new Label(b);t.getStyleClass().add("heading");s.getStyleClass().add("subtitle");v.getChildren().addAll(t,s);return v;}
    private Label section(String s){Label l=new Label(s.toUpperCase());l.getStyleClass().add("section-title");return l;}
    private Label field(String s){Label l=new Label(s);l.getStyleClass().add("field-label");return l;}
    private Label big(String s){Label l=new Label(s);l.getStyleClass().add("row-label");return l;}
    private Label badge(String s){Label l=new Label(s);l.getStyleClass().add("badge");return l;}
    private String money(double n){return String.format("$%,.2f",n);}
    private void fade(Node n){if(n==null)return;FadeTransition f=new FadeTransition(Duration.millis(300),n);f.setFromValue(0);f.setToValue(1);ScaleTransition s=new ScaleTransition(Duration.millis(300),n);s.setFromX(.985);s.setFromY(.985);s.setToX(1);s.setToY(1);f.play();s.play();}
    private void startMarketTicker(){Timeline t=new Timeline(new KeyFrame(Duration.seconds(3),e->{service.simulateTick();refreshSelected();refreshChart();if(root.getCenter()!=null){portfolio.setText(money(service.getPortfolioValue()));cash.setText(money(service.getUser().getCash()));pnl.setText(money(service.getProfitLoss()));returns.setText(String.format("%+.2f%%",service.getReturnPercent()));}}));t.setCycleCount(Timeline.INDEFINITE);t.play();}
}
