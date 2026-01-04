package br.com.jpbassinello.sbcgg.grpc.client.discovery;

import io.grpc.NameResolverRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnBean(DiscoveryClient.class)
public class DiscoveryClientGrpcConfig {

    public DiscoveryClientGrpcConfig(DiscoveryClient discoveryClient) {
        var provider = new DiscoveryClientNameResolverProvider(discoveryClient);
        NameResolverRegistry.getDefaultRegistry().register(provider);
        log.info("Registered DiscoveryClientNameResolverProvider for scheme '{}' with DiscoveryClient: {}",
                DiscoveryClientNameResolverProvider.SCHEME, discoveryClient.getClass().getSimpleName());
    }
}
