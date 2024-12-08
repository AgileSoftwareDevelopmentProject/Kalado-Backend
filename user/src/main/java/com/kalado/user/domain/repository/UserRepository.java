package com.kalado.user.domain.repository;

import com.kalado.user.domain.model.User;

import java.util.Optional;

public interface UserRepository<T extends User> {
    Optional<User> findById(long id);

    void save(T newUser);

    void modify(String firstName, String lastName, String address, String phoneNumber, long id);
}