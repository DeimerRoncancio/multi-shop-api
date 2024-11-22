package com.majestic.food.api.majestic_food_api.services;

import com.majestic.food.api.majestic_food_api.entities.Order;
import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.entities.dtos.NewOrderDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.UpdateOrderDTO;
import com.majestic.food.api.majestic_food_api.mappers.OrderMapper;
import com.majestic.food.api.majestic_food_api.repositories.OrderRepository;
import com.majestic.food.api.majestic_food_api.repositories.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public Order save(NewOrderDTO dto) {
        Order order = OrderMapper.mapper.orderCreateDTOtoOrder(dto);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = (String) authentication.getPrincipal();
        System.out.println(principal);
        
        Optional<User> userOptional = userRepository.findByEmail(principal);

        if (userOptional.isEmpty())
            userOptional = userRepository.findByPhoneNumber(Long.parseLong(principal));
        
        order.setUser(userOptional.orElseThrow());

        return repository.save(order);
    }

    @Override
    @Transactional
    public Optional<Order> update(String id, UpdateOrderDTO dto) {
        Optional<Order> orderDb = repository.findById(id);

        orderDb.ifPresent(order -> {
            OrderMapper.mapper.toUpdateOrder(dto, order);

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
