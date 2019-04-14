package main;

import java.awt.Color;

import engine.Camera;
import engine.Engine;
import engine.Light;
import engine.Texture;
import engine.Triangle;
import engine.Vector;

public class Main extends Engine {
	
	public Main() {
		super(1920, 1080, "Engine Test App", 90, 30);
		
		this.setBackgroundColor(Color.BLUE);
		//this.loadModel("res/teapot.obj", true);
		this.addTexture(new Texture("res/brick.png"));
		this.addTexture(new Texture("res/grass.png"));
		this.addTriangle(new Triangle(
				new Vector[] {
						new Vector(1,  1, 0),
						new Vector(1, -1, 0),
						new Vector(-1, 1, 0),
				},
				new Vector[] {
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
				},
				0,
				true
		));
		this.addTriangle(new Triangle(
				new Vector[] {
						new Vector(1,  -1, 0),
						new Vector(-1, -1, 0),
						new Vector(-1, 1, 0),
				},
				new Vector[] {
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
				},
				0,
				true
		));
		this.addTriangle(new Triangle(
				new Vector[] {
						new Vector(3,  -1, 0),
						new Vector(1, -1, 0),
						new Vector(1, 1, 0),
				},
				new Vector[] {
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
				},
				0,
				true
		));
		this.addTriangle(new Triangle(
				new Vector[] {
						new Vector(3,  1, 0),
						new Vector(3, -1, 0),
						new Vector(1, 1, 0),
				},
				new Vector[] {
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
				},
				0,
				true
		));
		
		
		this.addTriangle(new Triangle(
				new Vector[] {
						new Vector(3,  1, 0),
						new Vector(1, 1, 0),
						new Vector(1, 3, 0),
				},
				new Vector[] {
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
				},
				0,
				true
		));
		this.addTriangle(new Triangle(
				new Vector[] {
						new Vector(3,  3, 0),
						new Vector(3, 1, 0),
						new Vector(1, 3, 0),
				},
				new Vector[] {
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
				},
				0,
				true
		));
		
		
		this.addTriangle(new Triangle(
				new Vector[] {
						new Vector(1,  1, 0),
						new Vector(-1, 1, 0),
						new Vector(-1, 3, 0),
				},
				new Vector[] {
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
				},
				0,
				true
		));
		this.addTriangle(new Triangle(
				new Vector[] {
						new Vector(1,  3, 0),
						new Vector(1, 1, 0),
						new Vector(-1, 3, 0),
				},
				new Vector[] {
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
						new Vector(255, 0, 0),
				},
				1,
				true
		));

		this.addLight(new Light(Camera.position, 5f));
		
		run();	
	}

	@Override
	public void update(long deltaTimeMs) {
		/*for (int i = 0; i < this.numTriangles(); i++) {
			this.getTriangle(i).rotation.z += deltaTimeMs * 0.00001f;
		}*/
	}

	@Override
	public boolean onKeyDown(int keyCode) {
		return false;
	}

	@Override
	public void onMouseClick() {}

	@Override
	public void onMousePress() {}

	@Override
	public void onMouseRelease() {}

	public static void main(String[] args) {
		new Main();
	}
}