package com.multi.shop.api.multi_shop_api.orders.services;

import org.springframework.transaction.annotation.Transactional;

import com.multi.shop.api.multi_shop_api.orders.entities.Order;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.orders.dtos.NewOrderDTO;
import com.multi.shop.api.multi_shop_api.orders.dtos.UpdateOrderDTO;
import com.multi.shop.api.multi_shop_api.orders.mappers.OrderMapper;
import com.multi.shop.api.multi_shop_api.orders.repositories.OrderRepository;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findOne(String id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public NewOrderDTO save(NewOrderDTO dto) {
        Order order = OrderMapper.MAPPER.orderCreateDTOtoOrder(dto);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = (String) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findByEmail(principal);

        if (userOptional.isEmpty())
            userOptional = userRepository.findByPhoneNumber(Long.parseLong(principal));
        
        order.setUser(userOptional.orElseThrow());

        repository.save(order);
        return dto;
    }

    @Override
    @Transactional
    public Optional<Order> update(String id, UpdateOrderDTO dto) {
        Optional<Order> orderDb = repository.findById(id);

        orderDb.ifPresent(order -> {
            OrderMapper.MAPPER.toUpdateOrder(dto, order);
            repository.save(order);
        });

        return orderDb;
    }

    @Override
    @Transactional
    public Optional<Order> delete(String id) {
        Optional<Order> orderOptional = repository.findById(id);
        orderOptional.ifPresent(repository::delete);
        return orderOptional;
    }
}
