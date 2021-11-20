import SimpleStupidBot.SimpleStupidBot;
import binance.Binance;

public class MainBot {
    public static void main(String str[]) {
        if(str.length < 2 ) System.out.println("Enter API and secret code, example MainBot QWEQWE QWEQWE");
        Binance b = new Binance();
        b.setAPIKey(str[0]);
        b.setSecretKey(str[1]);

        SimpleStupidBot ssb = new SimpleStupidBot(b, "ETHBTC", "BTC");
        Thread t = new Thread(ssb);
        t.start();

        SimpleStupidBot ssb2 = new SimpleStupidBot(b, "LTCBTC", "BTC");
        Thread t2 = new Thread(ssb2);
        t2.start();

    }
}

