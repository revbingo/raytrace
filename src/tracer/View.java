package tracer;

public class View {

	public static final V3 VERTICAL_AXIS = new V3(0.0,0.0,1.0);
	public final V3 camera = new V3();
	public final V3 lookAt = new V3();
	public final V3 look = new V3();
	public final V3 horz = new V3();
	public final V3 vert = new V3();

	public View() {
		calibrateView();
	}
	
	public void calibrateView() {
		camera.set(2, -10, 7);
		lookAt.set(0, 1, 0);

		update();
	}
	
	public void update() {
		look.set(lookAt);
		look.sub(camera);

		horz.set(look.y, -look.x, 0);
		vert.set(V3.cross(horz, look));

		horz.norm();
		vert.norm();

		horz.mul(0.018);
		vert.mul(0.018);
	}
}
