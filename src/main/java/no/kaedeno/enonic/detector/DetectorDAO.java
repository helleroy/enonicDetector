package no.kaedeno.enonic.detector;

public interface DetectorDAO<T> {
	public T save(T obj);
	public T findOne(String key, String value);
	public T update(T obj);
	public void remove(T obj);
}
