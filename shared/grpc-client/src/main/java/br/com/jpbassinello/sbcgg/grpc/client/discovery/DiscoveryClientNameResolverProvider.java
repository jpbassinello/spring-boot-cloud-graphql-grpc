package br.com.jpbassinello.sbcgg.grpc.client.discovery;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import java.net.URI;
import org.springframework.cloud.client.discovery.DiscoveryClient;

class DiscoveryClientNameResolverProvider extends NameResolverProvider {

    public static final String SCHEME = "discovery";
    private static final int PRIORITY = 5;

    private final DiscoveryClient discoveryClient;

    public DiscoveryClientNameResolverProvider(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return PRIORITY;
    }

    @Override
    public String getDefaultScheme() {
        return SCHEME;
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        if (!SCHEME.equals(targetUri.getScheme())) {
            return null;
        }
        var serviceName = targetUri.getAuthority();
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = targetUri.getPath();
            if (serviceName != null && serviceName.startsWith("/")) {
                serviceName = serviceName.substring(1);
            }
        }
        if (serviceName == null || serviceName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid discovery URI: " + targetUri + ". Expected format: discovery:///service-name");
        }
        return new DiscoveryClientNameResolver(serviceName, discoveryClient);
    }
}
