package br.com.jpbassinello.sbcgg.grpc.client.config;

import br.com.jpbassinello.sbcgg.grpc.client.discovery.DiscoveryClientGrpcConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({DiscoveryClientGrpcConfig.class})
public class GrpcClientConfig {
}
