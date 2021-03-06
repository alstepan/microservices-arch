package me.alstepan.users.infra.db;

import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.spi.DbMapperProvider;
import me.alstepan.users.domain.User;

import javax.annotation.Priority;
import java.util.Optional;

@Priority(1000)
public class UserMapperProvider implements DbMapperProvider {
    private static final UserMapper MAPPER = new UserMapper();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<DbMapper<T>> mapper(Class<T> type) {
        if (type.equals(User.class)) {
            return Optional.of((DbMapper<T>) MAPPER);
        }
        return Optional.empty();
    }
}
