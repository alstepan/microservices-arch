package me.alstepan.users.errors;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(long id) {
    }
}
