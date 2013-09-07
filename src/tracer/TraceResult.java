package tracer;

public class TraceResult {
	
	public enum Action {
		REFLECTED,
		ABSORBED
	}
	
	public Action action;
	public long color;
	public V3 nextRay;
}
