package me.alstepan.users.domain;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface UserRepository {
    CompletionStage<User> get(long id);

    CompletionStage<Long> create(User user);

    CompletionStage<Long> delete(long id);

    CompletionStage<Long> update(long id, User user);

    CompletionStage<List<User>> list();
}
