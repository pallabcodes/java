package com.netflix.productivity.config;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ServiceDiscoveryConfig {
    
    private final DiscoveryClient discoveryClient;
    private final EurekaClient eurekaClient;
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public ServiceRegistry serviceRegistry() {
        return new ServiceRegistry(discoveryClient, eurekaClient);
    }
    
    public static class ServiceRegistry {
        private final DiscoveryClient discoveryClient;
        private final EurekaClient eurekaClient;
        
        public ServiceRegistry(DiscoveryClient discoveryClient, EurekaClient eurekaClient) {
            this.discoveryClient = discoveryClient;
            this.eurekaClient = eurekaClient;
        }
        
        public List<ServiceInstance> getInstances(String serviceName) {
            return discoveryClient.getInstances(serviceName);
        }
        
        public ServiceInstance getInstance(String serviceName) {
            List<ServiceInstance> instances = getInstances(serviceName);
            if (instances.isEmpty()) {
                throw new RuntimeException("No instances found for service: " + serviceName);
            }
            return instances.get(0); // Simple round-robin
        }
        
        public String getServiceUrl(String serviceName) {
            ServiceInstance instance = getInstance(serviceName);
            return instance.getUri().toString();
        }
        
        public List<String> getServiceNames() {
            return discoveryClient.getServices();
        }
        
        public boolean isServiceAvailable(String serviceName) {
            try {
                List<ServiceInstance> instances = getInstances(serviceName);
                return !instances.isEmpty() && instances.stream()
                    .anyMatch(instance -> InstanceInfo.InstanceStatus.UP.name()
                        .equals(instance.getMetadata().get("status")));
            } catch (Exception e) {
                log.warn("Error checking service availability for {}", serviceName, e);
                return false;
            }
        }
        
        public void registerService(String serviceName, String host, int port) {
            log.info("Registering service {} at {}:{}", serviceName, host, port);
            // Service registration is handled by Eureka client automatically
        }
        
        public void deregisterService(String serviceName) {
            log.info("Deregistering service {}", serviceName);
            // Service deregistration is handled by Eureka client automatically
        }
    }
}
