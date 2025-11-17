package com.zimono.trg.a.repository;

import com.zimono.trg.a.model.Driver;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DriverRepository implements PanacheRepository<Driver> {


}
