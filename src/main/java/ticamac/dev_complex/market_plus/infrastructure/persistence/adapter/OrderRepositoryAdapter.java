package ticamac.dev_complex.market_plus.infrastructure.persistence.adapter;

import ticamac.dev_complex.market_plus.domain.model.Order;
import ticamac.dev_complex.market_plus.domain.port.out.OrderRepositoryPort;
import ticamac.dev_complex.market_plus.infrastructure.persistence.mapper.OrderMapper;
import ticamac.dev_complex.market_plus.infrastructure.persistence.repository.OrderJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpa;
    private final OrderMapper mapper;

    public OrderRepositoryAdapter(OrderJpaRepository jpa, OrderMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        return mapper.toDomain(jpa.save(mapper.toEntity(order)));
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Order> findByIdAndUserId(UUID id, UUID userId) {
        return jpa.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public Page<Order> findByUserId(UUID userId, Pageable pageable) {
        return jpa.findByUserId(userId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return jpa.findAll(pageable).map(mapper::toDomain);
    }
}