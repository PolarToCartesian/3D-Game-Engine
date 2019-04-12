package main;

import java.awt.Color;

import engine.Engine;

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
				1,
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
				1,
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
				1,
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
				false
		));

		this.addLight(new Light(Camera.position, new Vector(255, 255, 255), 5f));
		
		run();	
	}

	@Override
	public void update(long deltaTimeMs) {
		/*for (Triangle tr : triangles) {
			tr.rotation.y += 0.0001f * deltaTimeMs;
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