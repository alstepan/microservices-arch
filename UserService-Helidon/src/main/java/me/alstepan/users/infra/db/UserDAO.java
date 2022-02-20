package me.alstepan.users.infra.db;

import io.helidon.common.reactive.Multi;
import io.helidon.dbclient.*;
import me.alstepan.users.domain.User;
import me.alstepan.users.domain.UserRepository;
import me.alstepan.users.errors.UserNotFoundException;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class UserDAO implements UserRepository {

    private final DbClient dbClient;

    public UserDAO(DbClient dbClient) {
        this.dbClient = dbClient;
    }

    @Override
    public CompletionStage<User> get(long id) {
        return this.dbClient
                .execute(dbExecute -> dbExecute.createGet("SELECT * FROM public.users WHERE id=?")
                        .addParam(id)
                        .execute()
                )
                .thenApply(rowOptional -> rowOptional
                        .map(dbRow -> dbRow.as(User.class)).orElseThrow(() -> new UserNotFoundException(id))
                );
    }

    @Override
    public CompletionStage<Long> create(User user) {
        return this.dbClient
                .execute(dbExecute -> dbExecute
                        .query("INSERT INTO public.users(username, firstname, lastname, email, phone) VALUES (?, ?, ?, ?, ?) RETURNING id",
                                user.userName(), user.firstName(), user.lastName(), user.email(), user.phone())
                )
                .compose(Multi::first)
                .map(r -> r.column("id").as(Long.class))
                .first()
                .toStage();
    }

    @Override
    public CompletionStage<Long> delete(long id) {
        return this.dbClient.execute(
                dbExecute -> dbExecute.createDelete("DELETE FROM public.users WHERE id = :id")
                        .addParam("id", id)
                        .execute()
        );
    }

    @Override
    public CompletionStage<Long> update(long id, User user) {
        return this.dbClient.execute(
            dbExecute -> dbExecute
                    .createUpdate("UPDATE public.users SET username = :username, firstname = :firstname, lastname = :lastname, email = :email, phone = :phone WHERE id = :id")
                    .namedParam(user.setId(id))
                    .execute()
                    .map(u -> id)
        );
    }

    @Override
    public CompletionStage<List<User>> list() {
        return this.dbClient.execute( dbExecute ->
                dbExecute
                        .createQuery("SELECT * from public.users")
                        .execute()
        ).collectList().thenApply(rows -> rows.stream().map(u-> u.as(User.class)).toList());
    }
}
