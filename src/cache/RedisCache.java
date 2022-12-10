package cache;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import srv.api.service.rest.authentication.Session;
import srv.data.user.User;
import utils.AzureKeys;

import java.util.LinkedList;
import java.util.List;

/*import static srv.api.service.rest.AuctionResource.ABOUT_TO_CLOSE_AUCTIONS;*/

public class RedisCache {
	private static final String SESSIONID = "session:";
	private static final long CACHE_EXPIRATION_TIME = 60; //seconds

	/*
	No need to have all elements in list, bad usage of cache I think
	LIST_ABOUT_TO_CLOSE needs to have lower limit since auctions in this list are been closed frequently
	 */
	//public static final String HOST_NAME = AzureKeys.getInstance().getRedisHostname();
	//public static final String KEY = AzureKeys.getInstance().getRedisKey();

	private static JedisPool instance;
	private static RedisCache cache;

	public synchronized static JedisPool getCachePool() {
		if (instance != null)
			return instance;
		final JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(128);
		poolConfig.setMaxIdle(128);
		poolConfig.setMinIdle(16);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		poolConfig.setNumTestsPerEvictionRun(3);
		poolConfig.setBlockWhenExhausted(true);
		String redisHost = System.getenv("REDIS");
		instance = new JedisPool(poolConfig, redisHost, 6379, 1000);
		return instance;

	}

	public RedisCache() {
	}

	public synchronized static RedisCache getInstance() {
		if (cache != null)
			return cache;
		cache = new RedisCache();
		return cache;
	}

	//Session
	public void setSession(Session session) {
		ObjectMapper mapper = new ObjectMapper();
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			String cacheId = SESSIONID + session.getUid();
			jedis.set(cacheId, mapper.writeValueAsString(session));
			jedis.expire(cacheId, CACHE_EXPIRATION_TIME);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public Session getSession(String sessionId) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			String sId = jedis.get(SESSIONID + sessionId);
			ObjectMapper mapper = new ObjectMapper();
			Session session = null;
			try {
				session = mapper.readValue(sId, Session.class);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			return session;
		}
	}

	public void deleteSession(String sessionId) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.del(SESSIONID + sessionId);
		}
	}

	public <T> void set(String id, T object) {
		ObjectMapper mapper = new ObjectMapper();
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			String cacheId = object.getClass().getSimpleName() + ":" + id;
			jedis.set(cacheId, mapper.writeValueAsString(object));
			jedis.expire(cacheId, CACHE_EXPIRATION_TIME);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}


	public <T> T get(String id, Class<T> objectClass) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			String str = jedis.get(objectClass.getSimpleName() + ":" + id);
			ObjectMapper mapper = new ObjectMapper();
			T object = null;
			try {
				object = mapper.readValue(str, objectClass);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			return object;
		}
	}

	public <T> void delete(String id, Class<T> objectClass) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.del(objectClass.getSimpleName() + ":" + id);
		}
	}

/*	//LIST SECTION NEEDS TO BE DISCUSSED
	//add elements to list and keep limit (keeps list with 5 elements 0 -> 4)
	public <T> void addToList(T object, String name) {
		ObjectMapper mapper = new ObjectMapper();
		int limit = chooseLimit(name);
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			long cnt = jedis.lpush(name, mapper.writeValueAsString(object));
			if (cnt > limit)
				jedis.ltrim(name, 0, limit - 1);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	//get elements of a list
	//NOTE: jedis.lrange("MostRecent"+objectClassToString(objectClass), 0, -1); -> -1 refers to end of list
	public <T> List<T> list(String name, Class<T> objectClass) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			List<String> lst = jedis.lrange(name, 0, -1);
			ObjectMapper mapper = new ObjectMapper();
			List<T> objects = new LinkedList<>();

			lst.forEach((item) -> {
				try {
					objects.add(mapper.readValue(item, objectClass));
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			return objects;

		}
	}

	//poor efficient could use sorted set but takes much more memory (200% more than list)
	public <T> void replaceInList(T object, String name) {
		ObjectMapper mapper = new ObjectMapper();
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			Long pos = jedis.lpos(name, mapper.writeValueAsString(object));
			if (pos != null) {
				jedis.lset(name, pos, mapper.writeValueAsString(object));
			} else {
				addToList(object, name);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}*/

	//increment and get value of a counter
	public <T> long increment(Class<T> objectClass) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			return jedis.incr("Num" + objectClass.getSimpleName());
		}
	}

/*	private int chooseLimit(String name) {
		if (name.equals(ABOUT_TO_CLOSE_AUCTIONS)) {
			return LIST_ABOUT_TO_CLOSE_LIMIT;
		} else {
			return LIST_LIMIT;
		}

	}*/
}