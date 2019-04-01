package main;

import engine.Engine;

import java.awt.Color;

public class Main extends Engine {

	public Main() {
		super(1920, 1080, "Engine Test App", 90, 30);
		loadModel("res/bunny.obj", false);
		lights.add(new Light(Camera.position, Color.RED, 0.05f));
		run();
	}

	@Override
	public void update(long deltaTimeMs) {
		for (Triangle tr : triangles) {
			tr.rotation.y += 0.01f;
		}
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