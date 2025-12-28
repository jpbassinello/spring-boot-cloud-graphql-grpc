package br.com.jpbassinello.sbcgg.services.grpc.users.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {"br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.persistence"})
@EnableJpaRepositories(basePackages = {"br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.persistence"})
@EntityScan(basePackages = {"br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities"})
public class PersistenceConfig {
}
