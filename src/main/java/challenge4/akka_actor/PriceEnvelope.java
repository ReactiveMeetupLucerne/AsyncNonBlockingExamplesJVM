package challenge4.akka_actor;

/**
 * Contains service request and response in order to map them.
 */
class PriceEnvelope {
    public final PriceRequest priceRequest;
    public final Price price;

    public PriceEnvelope(PriceRequest priceRequest, Price price) {
        this.priceRequest = priceRequest;
        this.price = price;
    }

    @Override
    public String toString() {
        return "PriceEnvelope{" +
                "priceRequest=" + priceRequest +
                ", price=" + price +
                '}';
    }
}
