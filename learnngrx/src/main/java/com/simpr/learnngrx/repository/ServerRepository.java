package com.simpr.learnngrx.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.simpr.learnngrx.model.Server;

public interface ServerRepository extends JpaRepository<Server, Long>{
    public Server findByIpAddress(String ipAddress);
}
