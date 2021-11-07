package binance;

import java.util.SortedSet;
import java.util.TreeSet;

public class OrderBook {
    private SortedSet<Bid> bids = new TreeSet<>();
    private SortedSet<Ask> asks = new TreeSet<>();

    public void addAsk(Ask ask) {
        asks.add(ask);
    }
    public void addBid(Bid bid){
        bids.add(bid);
    }

    public int size(){
        return bids.size();
    }

    public Bid getBid(int ind) {
        return (Bid) bids.toArray()[ind];
    }

    public Ask getAsk(int ind) {
        return (Ask) asks.toArray()[ind];
    }
}
