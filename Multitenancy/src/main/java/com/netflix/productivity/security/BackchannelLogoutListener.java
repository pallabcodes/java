package com.netflix.productivity.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackchannelLogoutListener implements MessageListener {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.logout.backchannel.enabled:false}")
    private boolean enabled;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        if (!enabled) {
            return;
        }
        try {
            String payload = redisTemplate.getStringSerializer().deserialize(message.getBody());
            log.info("Back-channel logout received: {}", payload);
            // Expect payload to contain sessionId or userId; implement revocation hook
        } catch (Exception e) {
            log.error("Error handling back-channel logout", e);
        }
    }
}

