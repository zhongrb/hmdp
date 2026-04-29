package com.hmdp.utils;

import com.hmdp.dto.UserDTO;

public final class UserHolder {

    private static final ThreadLocal<UserDTO> USER_HOLDER = new ThreadLocal<>();

    private UserHolder() {
    }

    public static void saveUser(UserDTO user) {
        USER_HOLDER.set(user);
    }

    public static UserDTO getUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        UserDTO user = USER_HOLDER.get();
        return user == null ? null : user.getId();
    }

    public static void removeUser() {
        USER_HOLDER.remove();
    }
}
