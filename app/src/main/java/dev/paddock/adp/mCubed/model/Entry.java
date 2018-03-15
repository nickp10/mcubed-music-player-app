package dev.paddock.adp.mCubed.model;

public class Entry<K, V> implements java.util.Map.Entry<K, V> {
	private K key;
	private V value;
	
	public Entry(K key, V value) {
		setKey(key);
		setValue(value);
	}
	
	@Override
	public K getKey() {
		return key;
	}

	public K setKey(K key) {
		K oldKey = this.key;
		this.key = key;
		return oldKey;
	}
	
	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		V oldValue = this.value;
		this.value = value;
		return oldValue;
	}
}
