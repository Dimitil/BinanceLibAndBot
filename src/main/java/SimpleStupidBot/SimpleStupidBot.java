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
            double lastPrice = b.getLastPrice(pair);
            double curPrice = lastPrice;
            boolean wasBuy = false;
            do {
                sleep(periodMills);
                balance = b.getBalance(curSymbol);
                curPrice = b.getLastPrice(pair);
                if (wasBuy && qty > 0) {
                    if ((curPrice <= lastPrice * stopLimit) || (curPrice > lastPrice * profitLimit)) {
                        b.postSellOrder(pair, qty, curPrice);
                        wasBuy = false;
                    }
                } else {
                    if (curPrice >= lastPrice * trendForBuy || curPrice < lastPrice * lessForBuyLimit) {
                        qty = balance * balanceForBuy / curPrice;
                        b.postBuyOrder(pair, qty, curPrice);
                        wasBuy = true;
                    }
                }
                lastPrice = curPrice;
            } while (true);
        } catch ( InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
