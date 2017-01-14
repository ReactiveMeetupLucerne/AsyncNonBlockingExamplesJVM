package challenge2.camel;

import externalLegacyCodeNotUnderOurControl.PriceService;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;

import java.util.Objects;

/**
 * @author Dmytro Rud
 */
public class PriceExpression implements Expression {
    private final PriceService service;

    public PriceExpression(PriceService service) {
        this.service = Objects.requireNonNull(service);
    }

    @Override
    public <T> T evaluate(Exchange exchange, Class<T> type) {
        Integer price = service.getPrice();
        return (T) price;
    }
}
