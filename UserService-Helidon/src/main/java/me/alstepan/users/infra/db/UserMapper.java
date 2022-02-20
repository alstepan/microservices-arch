package me.alstepan.users.infra.db;

import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import me.alstepan.users.domain.User;

import java.util.List;
import java.util.Map;

public class UserMapper implements DbMapper<User> {
    @Override
    public User read(DbRow dbRow) {
        return new User(
                dbRow.column("id").as(Long.class),
                dbRow.column("username").as(String.class),
                dbRow.column("firstname").as(String.class),
                dbRow.column("lastname").as(String.class),
                dbRow.column("email").as(String.class),
                dbRow.column("phone").as(String.class)
        );
    }

    @Override
    public Map<String, Object> toNamedParameters(User user) {
        return Map.of(
                "id", user.id(),
                "username", user.userName(),
                "firstname", user.firstName(),
                "lastname", user.lastName(),
                "email", user.email(),
                "phone", user.phone()
                );
    }

    @Override
    public List<?> toIndexedParameters(User user) {
        return List.of(
                user.id(),
                user.userName(),
                user.firstName(),
                user.lastName(),
                user.email(),
                user.phone()
        );
    }
}
