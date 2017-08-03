import java.util.LinkedList;
import java.util.Queue;

import processing.core.*;

public class ArriveSteering extends PApplet 
{
	public static void main(String args[])
	{
		PApplet.main("ArriveSteering");
	}
	
	//creating class for instantiating character anywhere
		Hero h;
		float newAngle = 0;
		float vDistance = 0, hDistance = 0;
		Queue<PVector> breadCrumbs = new LinkedList<PVector>();
		int numCrumbs = 80;
		boolean activateMovement;
		
		class Hero
		{
			PShape hero;
			PVector position;
			
			PVector velocity;
			PVector targetVelocity;
			PVector linearSteer; //acceleration to be added
			PVector target;
			
			float rotation;
			float orientation;
			int innerBorder;
			float scaleF;
			float maxSpeed;
			float targetSpeed;
			float maxAcceleration;
			float radiusOfSatisficing;
			float radiusOfDeceleration;
			float timeToTarget;
			
			Hero()
			{
				this.hero = loadShape("hero.svg");
				
				this.orientation = 0;
				this.maxSpeed = 5;
				this.rotation = 0;
				this.innerBorder = 40;
				this.maxAcceleration = (float)0.5;
				this.radiusOfSatisficing = 10;
				this.radiusOfDeceleration = 100;
				this.timeToTarget = (float)0.25;
				this.scaleF = (float)0.1;
				
				this.position = new PVector(width/2, height/2);
				this.target = new PVector(this.position.x, this.position.y);
				this.velocity = PVector.sub(this.target, this.position);
				this.targetVelocity = PVector.sub(this.target, this.position);
				this.linearSteer = PVector.sub(this.targetVelocity, this.velocity);
				
				this.hero.scale(scaleF);
			}
		}
		
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
		
		
		//functions for generating the behavior required
		
		public void mouseClicked()
		{
			h.target = new PVector(mouseX, mouseY);
			//System.out.println(mouseX + ", " + mouseY);
			activateMovement = true;
			ellipse(h.target.x, h.target.y, 3, 3);
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
			h.velocity.add(h.linearSteer).limit(h.maxSpeed);
			h.position.add(h.velocity);
			
			if(activateMovement)
				arrive(h.target);
		}
		
		public void arrive(PVector target)
		{
			h.targetVelocity = PVector.sub(h.target, h.position);
			newAngle = getNewOrientation(h.targetVelocity, h.orientation);
			float distance = h.targetVelocity.mag();
			if(distance < h.radiusOfSatisficing)
			{
				h.linearSteer.mult(0);
				h.targetVelocity.mult((float)0.0);
				
			}
			else if(distance < h.radiusOfDeceleration)
			{
				h.targetSpeed = h.maxSpeed * distance / h.radiusOfDeceleration;
			}
			else
			{
				h.targetSpeed = h.maxSpeed;
			}
			
			
			h.targetVelocity.normalize();
			h.targetVelocity.mult(h.targetSpeed);
			
			h.linearSteer = PVector.sub(h.targetVelocity, h.velocity);
			h.linearSteer.div(h.timeToTarget);
			
			if(h.linearSteer.mag() > h.maxAcceleration)
			{
				h.linearSteer.normalize();
				h.linearSteer.mult(h.maxAcceleration);
			}
			
			
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
			ellipse(width/2, height/2, 7, 7);
			updatePosition();
			updateOrientation();
			dropBreadCrumbs();
			renderCrumbs();
			renderPlayer();
			
		}
	
}

