package ticamac.dev_complex.market_plus.infrastructure.persistence.adapter;

import ticamac.dev_complex.market_plus.domain.model.Payment;
import ticamac.dev_complex.market_plus.domain.port.out.PaymentRepositoryPort;
import ticamac.dev_complex.market_plus.infrastructure.persistence.mapper.PaymentMapper;
import ticamac.dev_complex.market_plus.infrastructure.persistence.repository.PaymentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository jpa;
    private final PaymentMapper mapper;

    public PaymentRepositoryAdapter(PaymentJpaRepository jpa, PaymentMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Payment save(Payment payment) {
        return mapper.toDomain(jpa.save(mapper.toEntity(payment)));
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return jpa.findByOrderId(orderId).map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByTransactionId(String transactionId) {
        return jpa.findByTransactionId(transactionId).map(mapper::toDomain);
    }
}