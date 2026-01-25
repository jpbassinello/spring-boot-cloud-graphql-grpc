package br.com.jpbassinello.sbcgg.grpc.client.discovery;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.Status;
import io.grpc.StatusOr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.net.InetSocketAddress;

@Slf4j
class DiscoveryClientNameResolver extends NameResolver {

  private final String serviceName;
  private final DiscoveryClient discoveryClient;
  private Listener2 listener;

  public DiscoveryClientNameResolver(String serviceName, DiscoveryClient discoveryClient) {
    this.serviceName = serviceName;
    this.discoveryClient = discoveryClient;
  }

  @Override
  public String getServiceAuthority() {
    return serviceName;
  }

  @Override
  public void start(Listener2 listener) {
    this.listener = listener;
    resolve();
  }

  @Override
  public void refresh() {
    resolve();
  }

  private void resolve() {
    try {
      var instances = discoveryClient.getInstances(serviceName);
      if (instances.isEmpty()) {
        log.warn("No instances found for service: {}", serviceName);
        listener.onError(Status.UNAVAILABLE.withDescription(
            "No instances available for service: " + serviceName));
        return;
      }

      var addressGroups = instances.stream()
          .map(this::toAddressGroup)
          .toList();

      log.debug("Resolved {} instances for service {}: {}", addressGroups.size(), serviceName, addressGroups);

      var result = ResolutionResult.newBuilder()
          .setAddressesOrError(StatusOr.fromValue(addressGroups))
          .setAttributes(Attributes.EMPTY)
          .build();
      listener.onResult(result);
    } catch (Exception e) {
      log.error("Failed to resolve service: {}", serviceName, e);
      listener.onError(Status.UNAVAILABLE.withDescription(
          "Failed to resolve service: " + serviceName).withCause(e));
    }
  }

  private EquivalentAddressGroup toAddressGroup(ServiceInstance instance) {
    var port = instance.getPort();
    var host = instance.getHost();
    log.trace("Creating address group for {}:{}", host, port);
    return new EquivalentAddressGroup(new InetSocketAddress(host, port));
  }

  @Override
  public void shutdown() {
    // No resources to clean up
  }
}
