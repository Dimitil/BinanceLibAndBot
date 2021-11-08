package binance;

public class Ask implements Comparable {
    double price;
    double qty;

    public Ask(double price, double qty) {
        this.price = price;
        this.qty = qty;
    }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getQty() { return qty; }
    public void setQty(double qty) { this.qty = qty; }

    @Override
    public int compareTo(Object o) {
        Ask a = (Ask) o;
        double res = this.getPrice() - a.getPrice();
        if(res > 0) {
            return 1;
        }
        else if(res < 0) {
            return -1;
        }
        return 0;
    }

}