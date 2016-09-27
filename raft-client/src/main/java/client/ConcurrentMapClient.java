package client;

import io.atomix.copycat.client.CopycatClient;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentMap;
import statemachine.SnapshotQuery;
import statemachine.ClearCommand;
import statemachine.GetQuery;
import statemachine.PutCommand;
import statemachine.SizeQuery;

/**
 * A {@code ConcurrentMap} that forwards all calls through a {@code CopycatClient} to a Copycat cluster. This class represents only the
 * remote side; the actual map operations are implemented on the server side.
 * <p>
 * All methods are blocking.
 * @param <K> map key type
 * @param <V> map value type
 * @author Initial author: Sebastian Millies
 */
public class ConcurrentMapClient<K, V> implements ConcurrentMap<K, V> {

	private final CopycatClient client;

	public ConcurrentMapClient(CopycatClient client) {
		this.client = client;
	}

	/*
	 * @see java.util.Map#size()
	 */
	@Override
	public int size() throws CompletionException {
		CompletableFuture<Integer> future = client.submit(new SizeQuery());
		return future.join();
	}

	/*
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	/*
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public V get(Object key) {
		CompletableFuture<V> future = client.submit(new GetQuery<V>(key));
		return future.join();
	}

	/*
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V put(K key, V value) {
		CompletableFuture<V> future = client.submit(new PutCommand<>(key, value));
		return future.join();
	}

	/*
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear() {
		CompletableFuture<Void> future = client.submit(new ClearCommand());
		future.join();
	}

	/*
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set<K> keySet() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		// get a serialized snapshot of the entire map, because HashMap.EntrySet itself is not serializable
		CompletableFuture<Set<Entry<K, V>>> future = client.submit(new SnapshotQuery<K, V>()).thenApply(Map::entrySet);
		return future.join();
	}

	// -------------------------------------------------------------------

	/*
	 * @see java.util.concurrent.ConcurrentMap#putIfAbsent(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V putIfAbsent(K key, V value) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * @see java.util.concurrent.ConcurrentMap#remove(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean remove(Object key, Object value) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * @see java.util.concurrent.ConcurrentMap#replace(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/*
	 * @see java.util.concurrent.ConcurrentMap#replace(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V replace(K key, V value) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
