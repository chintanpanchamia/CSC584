import java.util.ArrayList;

import processing.core.PVector;

class SpritePath
{
	ArrayList<PVector> spritePath;
	float pathRadius;

	SpritePath()
	{
		this.spritePath = new ArrayList<>();
		this.pathRadius = 20f;
	}

	PVector getStart()
	{
		return this.spritePath.get(0);
	}

	void add(int x, int y)
	{
		this.spritePath.add(new PVector(x, y));
	}

	PVector getEnd()
	{
		return this.spritePath.get(this.spritePath.size() - 1);
	}
}