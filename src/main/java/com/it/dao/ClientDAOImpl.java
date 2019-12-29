package com.it.dao;

import com.it.model.Client;
import org.springframework.stereotype.Repository;

@Repository
public class ClientDAOImpl extends GenericDAOImpl<Client, Long> implements ClientDAO {
    private static ClientDAOImpl instance;

    private ClientDAOImpl() {
        super(Client.class);
    }

    synchronized public static ClientDAOImpl getInstance() {
        if (instance == null) {
            instance = new ClientDAOImpl();
        }
        return instance;
    }
}
