package challenge4.akka_actor;

import java.util.Objects;

class PriceRequest {
    private String productName;

    public PriceRequest(String productName) {
        this.productName = productName;
    }

    @Override
    public String toString() {
        return "PriceRequest{" +
                "productName='" + productName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceRequest that = (PriceRequest) o;
        return Objects.equals(productName, that.productName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productName);
    }
}
