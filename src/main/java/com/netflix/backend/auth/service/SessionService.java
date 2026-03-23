package com.netflix.backend.auth.service;

import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {

	private final RedisTemplate<String, String> redisTemplate;

	private static final String USER_DEVICES = "user:%s:devices";
	private static final String SESSION = "session:%s";

	public void createSession(String email, String deviceId, String token, String ip, String device) {

		// Add device to user
		redisTemplate.opsForSet().add(String.format(USER_DEVICES, email), deviceId);

		// Store session details
		String key = String.format(SESSION, deviceId);

		redisTemplate.opsForHash().put(key, "email", email);
		redisTemplate.opsForHash().put(key, "token", token);
		redisTemplate.opsForHash().put(key, "ip", ip);
		redisTemplate.opsForHash().put(key, "device", device);
		redisTemplate.opsForHash().put(key, "loginTime", String.valueOf(System.currentTimeMillis()));
	}

	public Set<String> getUserDevices(String email) {
		return redisTemplate.opsForSet().members(String.format(USER_DEVICES, email));
	}

	public Map<Object, Object> getSession(String deviceId) {
		return redisTemplate.opsForHash().entries(String.format(SESSION, deviceId));
	}

	public void deleteSession(String email, String deviceId) {
		redisTemplate.opsForSet().remove(String.format(USER_DEVICES, email), deviceId);
		redisTemplate.delete(String.format(SESSION, deviceId));
	}

	public boolean isSuspicious(String email, String ip, String device) {

		Set<String> devices = getUserDevices(email);

		if (devices == null || devices.isEmpty()) {
			return false;
		}

		for (String deviceId : devices) {
			Map<Object, Object> session = getSession(deviceId);

			String oldIp = (String) session.get("ip");
			String oldDevice = (String) session.get("device");

			if (!ip.equals(oldIp) || !device.equals(oldDevice)) {
				return true;
			}
		}

		return false;
	}
}