package com.nowcoder.seckill.service;

import com.nowcoder.seckill.entity.User;

public interface UserService {

    void register(User user);

    User login(String phone, String password);

    User findUserById(int id);

    User findUserFromCache(int id);

}
