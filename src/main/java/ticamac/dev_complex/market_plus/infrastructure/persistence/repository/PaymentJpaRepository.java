package ticamac.dev_complex.market_plus.infrastructure.persistence.repository;

import ticamac.dev_complex.market_plus.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByOrderId(UUID orderId);

    Optional<PaymentEntity> findByTransactionId(String transactionId);
}