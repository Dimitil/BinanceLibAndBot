package SimpleStupidBot;

import binance.Binance;
import static java.lang.Thread.sleep;

public class SimpleStupidBot implements Runnable {
    Binance b = null;
    String pair = null;
    String curSymbol = null;
    double balance;
    double qty;
    long periodMills = 5000;
    double balanceForBuy = 0.1;
    double stopLimit = 0.9;
    double profitLimit = 1.05;
    double lessForBuyLimit = 0.98;
    double trendForBuy = 1.03;
    public SimpleStupidBot(Binance b, String pair, String curSymbol) {
        this.b = b;
        this.pair = pair;
        this.curSymbol = curSymbol;
    }

    @Override
    public void run() {
        try {
            balance = b.getBalance(curSymbol);
            double lastPrice = b.getLastPrice(pair);
            double curPrice = 0;
            qty = balance * balanceForBuy / curPrice;
            b.postBuyOrder(pair, qty, lastPrice);
            boolean wasBuy = true;
            while (true) {
                sleep(periodMills);
                qty = b.getBalance(curSymbol);
                if (wasBuy && qty > 0) {
                    curPrice = b.getLastPrice(pair);
                    if ((curPrice <= lastPrice * stopLimit) || (curPrice > lastPrice * profitLimit)) {
                        b.postSellOrder(curSymbol, qty, curPrice);
                        wasBuy = false;
                        lastPrice = curPrice;
                    }
                } else {
                    curPrice = b.getLastPrice(pair);
                    if (curPrice >= lastPrice * trendForBuy || curPrice < lastPrice * lessForBuyLimit) {
                        qty = balance * balanceForBuy / curPrice;
                        b.postBuyOrder(pair, qty, lastPrice);
                        wasBuy = true;
                        lastPrice = curPrice;
                    }
                }
            }
        } catch ( InterruptedException e) {
            e.printStackTrace();
        }

    }
}
