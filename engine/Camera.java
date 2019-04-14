package engine;

public class Camera {
	public static Vector position = new Vector(0, 1, -5);
	public static Vector rotation = new Vector(0, 0, 0);
	
	public static boolean isRotatedTriangleFacingCamera(Vector[] _rotatedVertices, Vector _surfaceNormal) {
		Vector triangleRotatedCenter = Vector.div(Vector.add(Vector.add(_rotatedVertices[0], _rotatedVertices[1]), _rotatedVertices[2]), 3);
		Vector triangleToCamera = Vector.normalize(Vector.sub(Camera.position, triangleRotatedCenter));

		return (Vector.dotProduct(_surfaceNormal, triangleToCamera) > 0.f);
	}
}
