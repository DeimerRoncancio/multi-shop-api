package com.majestic.food.api.majestic_food_api.services;

import com.majestic.food.api.majestic_food_api.entities.Order;
import com.majestic.food.api.majestic_food_api.entities.dtos.OrderCreateDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.OrderUpdateDTO;
import com.majestic.food.api.majestic_food_api.mappers.OrderMapper;
import com.majestic.food.api.majestic_food_api.repositories.OrderRepository;
import com.majestic.food.api.majestic_food_api.repositories.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private UserRepository userRepository;
    
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
    public Order save(OrderCreateDTO dto) {
        Order order = OrderMapper.mapper.orderCreateDTOtoOrder(dto);

        userRepository.findById("a8869b5e-1e7f-40dd-8d2c-836a09a33cea").ifPresent(user -> {
            order.setUser(user);
        });
        
        return repository.save(order);
    }

    @Override
    @Transactional
    public Optional<Order> update(String id, OrderUpdateDTO dto) {
        Optional<Order> orderDb = repository.findById(id);

        orderDb.ifPresent(order -> {
            OrderMapper.mapper.toUpdateOrder(dto, order);

            userRepository.findById("a8869b5e-1e7f-40dd-8d2c-836a09a33cea").ifPresent(user -> {
                order.setUser(user);
            });

            repository.save(order);
        });

        return orderDb;
    }

    @Override
    @Transactional
    public Optional<Order> delete(String id) {
        Optional<Order> orderOptional = repository.findById(id);

        if (orderOptional.isPresent())
            repository.delete(orderOptional.get());
        
        return orderOptional;
    }
}
