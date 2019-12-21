package service;

import entities.User;

public interface UserService {
    boolean checkLogin(User user);
    User getUser(User user);
}
