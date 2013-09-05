package tracer;

public class View {

	private double ZOOM = 0.018;

	public static final V3 VERTICAL_AXIS = new V3(0.0,0.0,1.0);
	
	public final V3 camera = new V3();
	public final V3 lookAt = new V3();
	public final V3 cameraToLookAt = new V3();
	public final V3 horz = new V3();
	public final V3 vert = new V3();

	//x is horizontal (positive to right)
	//y is back to front (positive is to the back)
	//z is up/down
	
	//0,0,0 is centre of the checked board
	
	public View() {
		camera.set(2, -10, 7);
		lookAt.set(0, 0, 0);
	}
	
	private void update() {
		cameraToLookAt.set(lookAt).sub(camera);

		horz.set(cameraToLookAt.y, -cameraToLookAt.x, 0).norm().mul(ZOOM);
		vert.set(V3.cross(horz, cameraToLookAt)).norm().mul(ZOOM);
	}
	
	public void animate() {
		update();
	}
	
	public void zoomIn() {
		ZOOM -= 0.001;
	}
	
	public void zoomOut() {
		ZOOM += 0.001;
	}
	
	public void rotateLeft() {
		camera.rotate(VERTICAL_AXIS, 0.02);	
	}
	
	public void rotateRight() {
		camera.rotate(VERTICAL_AXIS, -0.02);		
	}

	public void lookUp() {
		lookAt.add(new V3(0,0,0.2));
	}
	
	public void lookDown() {
		lookAt.add(new V3(0,0,-0.2));
	}
	
	public void lookLeft() {
		lookAt.add(new V3(-0.2,0,0));
	}
	
	public void lookRight() {
		lookAt.add(new V3(0.2,0,0));
	}
}
