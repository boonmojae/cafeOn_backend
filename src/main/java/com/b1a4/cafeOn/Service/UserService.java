package com.b1a4.cafeOn.Service;

import com.b1a4.cafeOn.Entity.UserEntity;
import com.b1a4.cafeOn.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

//    1. 회원가입
    public UserEntity create(final UserEntity userEntity) {

    }
}
