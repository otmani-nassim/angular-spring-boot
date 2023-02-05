package com.simpr.learnngrx.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Random; 

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.simpr.learnngrx.enumeration.Status;
import com.simpr.learnngrx.model.Server;
import com.simpr.learnngrx.repository.ServerRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ServerServiceImp implements ServerService {


    
    private final ServerRepository serverRepository;

    public ServerServiceImp(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    @Override
    public Server create(Server server) {
        log.info("Saving new server", server.getName());
        server.setImageUrl(getImageUrl());
        return serverRepository.save(server);
    }

    private String getImageUrl() {
        String [] images = {"server1.png","server2.png"};
        String image = images[new Random().nextInt(images.length)];
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/server/image/"+image).toUriString();
    }

    @Override
    public Collection<Server> list(int limit) {
        log.info("Fetching all servers, limit=", limit);
        return this.serverRepository.findAll(PageRequest.of(0, limit)).toList();
    }

    @Override
    public Server get(Long id) {
        log.info("Fetching server by id: ", id);
        return this.serverRepository.findById(id).get();
    }

    @Override
    public Server update(Server server) {
        log.info("Updating server : ", server.getName());
        return this.serverRepository.save(server);
    }

    @Override
    public Boolean delete(Long id) {
        log.info("Deleting server : ", id);
            this.serverRepository.deleteById(id);
        return true;
    }

    @Override
    public Server ping(String ipAddress) {
        log.info("Pinging server: ", ipAddress);
        Server server = this.serverRepository.findByIpAddress(ipAddress);
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            boolean isReachable = address.isReachable(10000);
            server.setStatus(isReachable? Status.SERVER_UP:Status.SERVER_DOWN);
        } catch (UnknownHostException e) {
            log.info("Wrong ipAddress: ", ipAddress);
            e.printStackTrace();        
        } catch (IOException e) {
            log.info("Soemthing went wrong while pinging ipAddress: ", ipAddress);
            e.printStackTrace();
        }
        return this.serverRepository.save(server);
    }
    
}
