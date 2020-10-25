package service;

import domain.User;

public class ATMService {

    public String login(User user) {
        //真实的操作应该依赖dao的数据
        if ("dp".equals(user.getName()) && 123456 == user.getPassword()) {
            return "success";
        }
        return "failed";
    }
}
