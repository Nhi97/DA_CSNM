package service.impl;

import dao.UserDao;
import dao.impl.UserDaoImpl;
import entities.User;
import service.UserService;

public class UserServiceImpl implements UserService {
    private UserDao userDao;

    public UserServiceImpl() {
        userDao = new UserDaoImpl();
    }

    @Override
    public boolean checkLogin(User user) {
        return userDao.checkLogin(user);
    }

    @Override
    public User getUser(User user) {
        return userDao.getUser(user);
    }
}
