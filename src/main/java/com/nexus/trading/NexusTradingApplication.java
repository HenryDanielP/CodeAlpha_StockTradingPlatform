package com.nexus.trading;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NexusTradingApplication extends Application {
    @Override public void start(Stage stage) {
        TradingView view = new TradingView();
        Scene scene = new Scene(view.create(), 1440, 900);
        scene.getStylesheets().add(getClass().getResource("/css/nexus-trading.css").toExternalForm());
        stage.setTitle("NEXUS — Stock Trading Platform");
        stage.setMinWidth(1180); stage.setMinHeight(760);
        stage.setScene(scene); stage.show();
    }
    public static void main(String[] args) { launch(args); }
}
