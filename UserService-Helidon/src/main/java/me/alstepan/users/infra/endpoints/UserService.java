
package me.alstepan.users.infra.endpoints;

import java.util.Collections;
import java.util.logging.Logger;

import javax.json.*;

import io.helidon.common.http.Http;
import io.helidon.common.reactive.Single;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import me.alstepan.users.domain.User;
import me.alstepan.users.domain.UserRepository;
import me.alstepan.users.errors.UserNotFoundException;

public class UserService implements Service {

    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    private UserRepository userRepository = null;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * A service registers itself by updating the routing rules.
     * @param rules the routing rules.
     */
    @Override
    public void update(Routing.Rules rules) {
        rules
            .post("/", this::createUserHandler)
            .put("/{id}", this::updateUser)
            .delete("/{id}", this::deleteUser)
            .get("/{id}", this::getUserByIdHandler)
            .get("/", this::listUsers);
    }

    private void listUsers(ServerRequest serverRequest, ServerResponse serverResponse) {
        userRepository
            .list()
            .thenAccept(l -> {
                var builder = JSON.createArrayBuilder();
                l.stream().map(this::userToJson).forEach(builder::add);
                sendResponse(JSON.createObjectBuilder().add("users", builder.build()).build(), serverResponse);
            })
            .handle((res, err) -> sendError(err, serverResponse));
    }

    private void deleteUser(ServerRequest serverRequest, ServerResponse serverResponse) {
        try {
            userRepository
                    .delete(getUserId(serverRequest))
                    .thenAccept(i ->sendResponse(JSON.createObjectBuilder().add("id", getUserId(serverRequest)).build(), serverResponse));
        }
        catch (Throwable ex){
            sendError(ex, serverResponse);
        }
    }

    private void updateUser(ServerRequest serverRequest, ServerResponse serverResponse) {
        try {
            serverRequest.content().as(User.class)
                    .thenApply(u -> userRepository
                            .update(getUserId(serverRequest), u)
                    )
                    .thenAccept(i ->
                        i.thenAccept(id -> sendResponse(JSON.createObjectBuilder().add("id", id).build(), serverResponse))
                                .handle((res, err) -> sendError(err, serverResponse))
                    )
                    .handle((res, err) -> sendError(err, serverResponse));
        }
        catch (Throwable ex) {
            sendError(ex, serverResponse);
        }
    }

    private void getUserByIdHandler(ServerRequest serverRequest, ServerResponse serverResponse) {
        try {
            userRepository.get(getUserId(serverRequest))
                    .thenApply(this::userToJson)
                    .thenAccept(u -> sendResponse(u.build(), serverResponse))
                    .handle((res, err) -> sendError(err, serverResponse));
        }
        catch (Throwable ex) {
            sendError(ex, serverResponse);
        }
    }

    private void createUserHandler(ServerRequest serverRequest, ServerResponse serverResponse) {
        serverRequest.content().as(User.class)
            .thenApply(userRepository::create)
            .thenAccept(id -> id.thenAccept( i -> sendResponse(JSON.createObjectBuilder().add("id", i).build(), serverResponse)))
                .exceptionallyAccept(ex -> sendError(ex, serverResponse))
            .handle((res, err) -> sendError(err, serverResponse));
    }

    private Single<ServerResponse> sendError(Throwable ex, ServerResponse response) {
        if (ex instanceof NumberFormatException) {
            LOGGER.warning("Bad request: " + ex);
            return response
                    .status(Http.Status.BAD_REQUEST_400)
                    .send(JSON.createObjectBuilder().add("error", "Invalid user id"));
        } else if (ex instanceof UserNotFoundException)
            return response
                    .status(Http.Status.NOT_FOUND_404)
                    .send(JSON.createObjectBuilder().add("error", "User not found"));
        else if (ex != null ) {
            LOGGER.warning("Internal error: " + ex);
            return response
                    .status(Http.Status.INTERNAL_SERVER_ERROR_500)
                    .send(JSON.createObjectBuilder().add("error", "Unknown error. Contact site administrator"));
        }
        return null;
    }

    private Single<ServerResponse> sendResponse(JsonObject obj, ServerResponse response) {
            return response
                    .status(Http.Status.OK_200)
                    .send(obj);
    }

    private long getUserId(ServerRequest serverRequest) throws NumberFormatException {
        return Long.parseLong(serverRequest.path().param("id"));
    }

    private JsonObjectBuilder userToJson(User user) {
        return JSON.createObjectBuilder()
                .add("id", user.id())
                .add("userName", user.userName())
                .add("firstName", user.firstName())
                .add("lastName", user.lastName())
                .add("phone", user.phone())
                .add("email", user.email());
    }

}