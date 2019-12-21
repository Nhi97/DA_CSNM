package dao;

import entities.User;

public interface UserDao {
    boolean checkLogin(User user);
    User getUser(User user);
}
