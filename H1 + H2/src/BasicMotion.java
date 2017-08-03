import java.util.LinkedList;
import java.util.Queue;

import processing.core.*;

public class BasicMotion extends PApplet {

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		PApplet.main("BasicMotion");	
	}
	
	//creating class for instantiating character anywhere
	Hero h;
	float newAngle = 0;
	float vDistance = 0, hDistance = 0;
	Queue<PVector> breadCrumbs = new LinkedList<PVector>();
	int numCrumbs = 80;
	
	class Hero
	{
		PVector position;
		float orientation;
		PVector velocity;
		float rotation;
		int innerBorder;
		PShape hero;
		float scaleF;
		PVector target;
		int maxSpeed;
		
		Hero()
		{
			this.hero = loadShape("hero.svg");
			
			this.orientation = 0;
			this.maxSpeed = 3;
			this.rotation = 0;
			this.innerBorder = 40;
			this.scaleF = (float)0.1;
			this.position = new PVector(this.innerBorder, height - this.innerBorder);
			this.target = new PVector(width - this.innerBorder, height - this.innerBorder);
			this.velocity = PVector.sub(this.target, this.position);
			this.velocity.normalize();
			this.velocity.mult(this.maxSpeed);
			this.hero.scale(scaleF);
		}
	}
	
	
	
	//functions for generating the behavior required
	
	public void settings()
	{
		size(800, 800);	
	}
	
	public void setup()
	{
		smooth();
		background(255, 255, 255);
		h = new Hero();
		vDistance = height - 2*h.innerBorder;
		hDistance = width - 2*h.innerBorder;
		
	}
	
	public void innerBorderCheck()
	{
		//LowL to LowR
		if(h.position.x >= width - h.innerBorder && h.target.y == height - h.innerBorder)
		{
			h.position.x = width - h.innerBorder;
			h.target.y = h.innerBorder;
				
		}
		//LowR to UpperR
		else if(h.position.y <= h.innerBorder && h.target.x == width - h.innerBorder)
		{
			h.position.y = h.innerBorder;
			h.target.x = h.innerBorder;
		}
		//UpperR to UpperL
		else if(h.position.x <= h.innerBorder && h.target.y == h.innerBorder)
		{
			h.position.x = h.innerBorder;
			h.target.y = height - h.innerBorder;
		}
		//UpperL to LowL
		else if(h.position.y >= height - h.innerBorder && h.target.x == h.innerBorder)
		{
			h.position.y = height - h.innerBorder;				
			h.target.x = width - h.innerBorder;
		}
		
		h.velocity = PVector.sub(h.target, h.position);
		h.velocity.normalize();
		h.velocity.mult(h.maxSpeed);
		newAngle = getNewOrientation(h.velocity, h.orientation);
	}
	
	public float getNewOrientation(PVector velocity, float orient)
	{
		if(velocity.mag() > 0)
		{
			return atan2(velocity.y, velocity.x);
		}
		return orient;
	}
	
	public void updatePosition()
	{
		h.position.add(h.velocity);
		innerBorderCheck();
		updateOrientation();
	}
	
	
	public void updateOrientation()
	{
		float arc;
		h.rotation = newAngle - h.orientation;
		h.rotation = mapToRange(h.rotation);
		if(h.target.x == width - h.innerBorder && h.target.y == height - h.innerBorder || h.target.x == h.innerBorder && h.target.y == h.innerBorder)
		{
			arc = (float)h.rotation/((float)0.020*hDistance);
		}
		else
			arc = (float)h.rotation/((float)0.020*vDistance);
		h.orientation += arc;
	}
	
	public float mapToRange(float rotation)
	{
		rotation = rotation % TWO_PI;
		if(abs(rotation) <= PI)
		{
			return rotation;
		}
		else if(rotation > PI)
			return rotation - TWO_PI;
		else
			return rotation + TWO_PI;
	}
	
	public void dropBreadCrumbs()
	{
		if(frameCount % h.maxSpeed == 0)
		{
			if(breadCrumbs.size() == numCrumbs)
			{
				breadCrumbs.remove();
			}
			//System.out.println(frameCount + ": " + h.position.x + ", " + h.position.y);
			
			breadCrumbs.add(new PVector(h.position.x, h.position.y));
			//PVector t = breadCrumbs.get(breadCrumbs.size() - 1);
			//System.out.println("pushed: " + t.x + ", " + t.y);
		}
	}
	
	public void renderCrumbs()
	{

		for(PVector temp: breadCrumbs)
		{
			pushMatrix();
			//System.out.println(temp.x + ", " + temp.y);
			translate(temp.x, temp.y);
			fill(255, 0, 0);
			ellipseMode(RADIUS);
			ellipse(0, 0, 1, 1);
			fill(255);
			popMatrix();
			
		}
	}
	
	public void renderPlayer()
	{
		pushMatrix();
		translate(h.position.x, h.position.y);
		rotate(h.orientation);
		shape(h.hero, -h.hero.width/60, -h.hero.height/40);
		fill(0, 255, 255);
		ellipse(0, 0, 5, 5);
		fill(255, 255, 255);
		popMatrix();
	}
	
	public void draw()
	{
		
		rect(0, 0, width, height);
		updatePosition();
		dropBreadCrumbs();
		renderCrumbs();
		renderPlayer();
		
	}

}


//Overly complicated and now discarded BreadCrumb spawn logic
//if(!breadCrumbs.isEmpty())
//{
//	if(h.target.x == width - h.innerBorder && h.target.y == height - h.innerBorder)
//	{
//		
//	}
//	else if(h.target.x == width - h.innerBorder && h.target.y == h.innerBorder)
//	{
//		if(frameCount % 3 == 0)
//		{
//			if(breadCrumbs.size() == numCrumbs)
//			{
//				breadCrumbs.remove();
//			}
//			breadCrumbs.add(h.position);
//		}
//	}
//	else if(h.target.x == h.innerBorder && h.target.y == h.innerBorder)
//	{
//		if(frameCount % 3 == 0)
//		{
//			if(breadCrumbs.size() == numCrumbs)
//			{
//				breadCrumbs.remove();
//			}
//			breadCrumbs.add(h.position);
//		}
//	}
//	else if(h.target.x == h.innerBorder && h.target.y == height - h.innerBorder)
//	{
//		if(frameCount % 3 == 0)
//		{
//			if(breadCrumbs.size() == numCrumbs)
//			{
//				breadCrumbs.remove();
//			}
//			breadCrumbs.add(h.position);
//		}
//	}
//}
//else
//{
//	breadCrumbs.add(h.position);
//}
