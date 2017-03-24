package challenge4.akka_actor;

class Price {
    public final int amount;

    public Price(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Price{" +
                "amount=" + amount +
                '}';
    }
}
