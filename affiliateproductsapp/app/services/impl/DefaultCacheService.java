package services.impl;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import play.Play;
import play.cache.Cache;
import services.CacheService;
import utils.log.Log;

public class DefaultCacheService implements CacheService {

	private static final boolean IS_ENABLED = BooleanUtils.toBoolean(Play.configuration.getProperty("cache.isEnabled"));
	private static final String KEY_PREFIX = Play.configuration.getProperty("cache.key.prefix");
	private static final String EXPIRATION_QUICK = Play.configuration.getProperty("cache.expiration.quick");
	private static final String EXPIRATION_LONG = Play.configuration.getProperty("cache.expiration.long");

	private static Logger logger = Logger.getLogger(DefaultCacheService.class);
	
	@Override
	public <T> T get(String key, Class<T> classObject) {
		if (IS_ENABLED && StringUtils.isNotBlank(key)) {
			try {
				return Cache.get(KEY_PREFIX + key, classObject);
			} catch (Exception e) {
				logger.error(Log.message("Exception occurred in cache.get : "
						+ e.getMessage()));
				Cache.delete(KEY_PREFIX + key);
			}
		}
		return null;
	}

	@Override
	public void addToQuickCache(String key, Object value) {
		if (IS_ENABLED && StringUtils.isNotBlank(key) && value != null) {
			Cache.add(KEY_PREFIX + key, value, EXPIRATION_QUICK);
		}
	}

	@Override
	public void addToLongCache(String key, Object value) {
		if (IS_ENABLED && StringUtils.isNotBlank(key) && value != null) {
			Cache.add(KEY_PREFIX + key, value, EXPIRATION_LONG);
		}
	}

	@Override
	public void remove(String key) {
		if (IS_ENABLED && StringUtils.isNotBlank(key)) {
			Cache.delete(KEY_PREFIX + key);
		}
	}
}
