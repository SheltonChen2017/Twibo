package edu.miis.DataTransferPojo;

import edu.miis.Entities.UserBean;

public record UserTransferPojo(Long id, String username) {
    public static UserTransferPojo from(UserBean user) {
        return new UserTransferPojo(user.getId(), user.getUsername());
    }
}
