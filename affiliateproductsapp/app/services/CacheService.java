package services;

public interface CacheService {

	<T> T get(String key, Class<T> classObject);

	void addToQuickCache(String key, Object value);

	void addToLongCache(String key, Object value);

	void remove(String key);
}
