package no.kaedeno.enonic.detector;

public interface DetectorDAO<A,B,C> {
	public A findOne(String key, String value);
	public B save(C obj);
}
