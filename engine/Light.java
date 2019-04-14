package engine;

import java.util.ArrayList;

public class Light {
	public Vector position;

	public double intensity;

	public Light(Vector _position, double _intensity) {
		this.position = _position;
		this.intensity = _intensity;
	}
	
	public static float[] getIllimunation(Vector[] _rotatedVertices, Vector _surfaceNormal, ArrayList<Light> lights) {
		float[] brightnesses = new float[3];
		
		for (int i = 0; i < 3; i++) {				
			// For Every Light
			for (Light light : lights) {
				// No Need To Translate The Light's position and the triangle's position because it would cancel out
				Vector vertexToLight = Vector.sub(light.position, _rotatedVertices[i]);

				double distance = (double) vertexToLight.getLength();

				vertexToLight.normalize();

				double dotProductTriangleAndLight = Vector.dotProduct(vertexToLight, _surfaceNormal);
					
				if (dotProductTriangleAndLight > 0f) {
					dotProductTriangleAndLight = Math.abs(dotProductTriangleAndLight);
				} else {
					dotProductTriangleAndLight = 0f;
				}
								
				brightnesses[i] += (dotProductTriangleAndLight * light.intensity) / (distance * distance);
			}
							
			if (brightnesses[i] > 1f) brightnesses[i] = 1f;
		}
		
		return brightnesses;
	}
	
	public static Vector[] getColorsWIllumination(Vector[] _rotatedVertices, Vector _surfaceNormal, Vector[] _colors, ArrayList<Light> lights) {
		// Total Brightness
		Vector[] colorsWlighting = new Vector[3];
		
		float[] brightnesses = getIllimunation(_rotatedVertices, _surfaceNormal, lights);

		// For Each Vertex
		for (int i = 0; i < 3; i++) {
			Vector vertexColor = Vector.mul(_colors[i], brightnesses[i]);
			
			// Limit color range (0-255)
			
			vertexColor.constrain();
			
			colorsWlighting[i] = vertexColor;
		}
		
		return colorsWlighting;
	}
}
