package com.netflix.reporting.chaos;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ChaosService {

    private final Environment environment;
    private final Random random = new Random();

    public ChaosService(Environment environment) {
        this.environment = environment;
    }

    public boolean isEnabled() {
        return getBoolean("chaos.enabled", false);
    }

    public long latencyMs() {
        long mean = getLong("chaos.latency_ms_mean", 0);
        long jitter = getLong("chaos.latency_ms_jitter", 0);
        if (mean <= 0 && jitter <= 0) return 0;
        long delta = jitter > 0 ? random.nextLong(jitter + 1) : 0;
        return mean + delta;
    }

    public boolean shouldFail() {
        int rate = getInt("chaos.error_rate_percent", 0);
        if (rate <= 0) return false;
        int pick = random.nextInt(100);
        return pick < rate;
    }

    private boolean getBoolean(String key, boolean def) {
        String v = environment.getProperty(key);
        if (v == null) return def;
        return v.equalsIgnoreCase("true") || v.equalsIgnoreCase("1") || v.equalsIgnoreCase("on");
    }

    private int getInt(String key, int def) {
        String v = environment.getProperty(key);
        if (v == null) return def;
        try { return Integer.parseInt(v); } catch (Exception e) { return def; }
    }

    private long getLong(String key, long def) {
        String v = environment.getProperty(key);
        if (v == null) return def;
        try { return Long.parseLong(v); } catch (Exception e) { return def; }
    }
}


