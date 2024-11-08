package com.majestic.food.api.majestic_food_api.services;

import com.majestic.food.api.majestic_food_api.entities.Order;
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
    public Order save(Order order) {
        userRepository.findById("de983a69-9f2b-42c3-bf43-565c8db09464").ifPresent(user -> {
            order.setUser(user);
        });
        
        return repository.save(order);
    }

    @Override
    @Transactional
    public Optional<Order> update(String id, Order order) {
        Optional<Order> orderDb = repository.findById(id);

        orderDb.ifPresent(ord -> {
            ord.setOrderName(order.getOrderName());
            ord.setNotes(order.getNotes());
            ord.setOrderDate(order.getOrderDate());

            userRepository.findById("de983a69-9f2b-42c3-bf43-565c8db09464").ifPresent(user -> {
                ord.setUser(user);
            });

            repository.save(ord);
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
