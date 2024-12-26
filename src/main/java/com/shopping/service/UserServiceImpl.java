package com.shopping.service;

import com.shopping.client.OrderClient;
import com.shopping.db.User;
import com.shopping.db.UserDao;
import com.shopping.server.UserServer;
import com.shopping.stubs.order.Order;
import com.shopping.stubs.user.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    //why it move there: if we keep in below it will create object again and again
    private UserDao userDao = new UserDao();

    @Override
    public void getUserDetails(UserRequest request, StreamObserver<UserResponse> responseObserver) {
//        UserDao userDao = new UserDao();
        User user = userDao.getDetails(request.getUsername());

        UserResponse.Builder userResponseBuilder = UserResponse.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setName(user.getName())
                .setAge(user.getAge())
                .setGender(Gender.valueOf(user.getGender()));

        List<Order> orders = getOrders(userResponseBuilder);

        userResponseBuilder.setNoOfOrders(orders.size());
        UserResponse userResponse = userResponseBuilder.build();
        responseObserver.onNext(userResponse);
        responseObserver.onCompleted();
    }

    private List<Order> getOrders(UserResponse.Builder userResponseBuilder) {
        //get orders by invoking the Order Client
        logger.info("Creating a channel and calling the Order Client");
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:50052")
                .usePlaintext().build();
        OrderClient orderClient = new OrderClient(channel);
        List<Order> orders = orderClient.getOrders(userResponseBuilder.getId());


        // ensure no more calls to do shut down this channel resource to reduce the leakage
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            logger.log(Level.SEVERE, "Channel did not shutdown", exception);
        }
        return orders;
    }
}
