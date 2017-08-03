import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import processing.core.*;

public class Flocking extends PApplet 
{

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		PApplet.main("Flocking");
	}
	
	float perceptionDistance = 20;
	float perceptionAngle = PI;
	float perceptionMinDistance = 5;
	
	float alignWeight = 1;
	float cohesionWeight = 0.7f;
	float separationWeight = 0.1f;
	
	class Hero
	{
		PVector position;
		PVector velocity;
		PVector acceleration;
		
		int innerBorder;
		PShape hero;
		float scaleF;
		float orientation;
		
		Hero()
		{
			this.hero = loadShape("hero.svg");
			
			//other primitive fields
			this.innerBorder = 40;
			this.scaleF = (float)0.1;
			this.orientation = 0;
			
			//PVectors
			this.position = new PVector(random(width), random(height));
			this.velocity = new PVector(random(-2, 2), random(-2,2));
			
			this.hero.scale(scaleF);
		}
		
		void render()
		{
			pushMatrix();
			translate(this.position.x, this.position.y);
			this.orientation = -atan2(-this.velocity.y, this.velocity.x);
			rotate(this.orientation);
			shape(this.hero, -this.hero.width/60, -this.hero.height/40);
			fill(0, 255, 255);
			ellipse(0, 0, 5, 5);
			fill(255, 255, 255);
			popMatrix();
		}
		
		ArrayList<Hero> getNeighbors(ArrayList<Hero> heroes)
		{
			ArrayList<Hero> neighbors = new ArrayList<>();
			
			PVector positionDelta = new PVector();
			for(int i = 0; i < heroes.size(); i++)
			{
				Hero h = (Hero)heroes.get(i);
				
				if(h == this)
					continue;
				
				positionDelta = h.position.copy();
				positionDelta.sub(this.position);
				
				if(positionDelta.mag() > perceptionDistance)
					continue;
				
				if(PVector.angleBetween(this.velocity, positionDelta) > perceptionAngle)
					continue;
				
				neighbors.add(h);
			}
			return neighbors;
		}
		
		void updatePosition(ArrayList neighbors)
		{
			PVector positionDelta = new PVector();
			PVector velocityDelta = new PVector();
			
			PVector alignment = new PVector();
			PVector separation = new PVector();
			PVector cohesion = new PVector();
			
			for(int i = 0; i < neighbors.size(); i++)
			{
				Hero h = (Hero)neighbors.get(i);
				
				positionDelta = h.position.copy();
				positionDelta.sub(this.position);
				
				velocityDelta = h.velocity.copy();
				velocityDelta.sub(this.velocity);
				
				alignment.add(velocityDelta);
				cohesion.add(positionDelta);
				
				if(positionDelta.mag() < perceptionMinDistance)
				{
					separation.add(positionDelta);
				}
				
			}
			
			if(alignment.mag() > 0)
				alignment.normalize();
			if(cohesion.mag() > 0)
				cohesion.normalize();
			if(separation.mag() > 0)
				separation.normalize();
			
			alignment.mult(alignWeight);
			cohesion.mult(cohesionWeight);
			separation.mult(separationWeight);
			
			this.acceleration = alignment.copy();
			this.acceleration.add(cohesion);
			this.acceleration.sub(separation);
			
			this.velocity.add(this.acceleration);
			this.position.add(this.velocity);
			
			
		}
		
	}
	
	//configuring
	ArrayList<Hero> heroes;
	
	public void settings()
	{
		size(1000,1000);
	}
	
	public void setup()
	{
		heroes = new ArrayList<Hero>();
		for(int i = 0; i < 100; i++)
			heroes.add(new Hero());
	}
	
	public void draw()
	{
		background(255);
		for(int i = 0; i < heroes.size(); i++)
		{
			Hero h = (Hero)heroes.get(i);
			h.updatePosition(h.getNeighbors(heroes));
			h.render();
		}
	}
	

}
