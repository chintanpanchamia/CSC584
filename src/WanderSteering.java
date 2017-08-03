import java.util.LinkedList;
import java.util.Queue;

import processing.core.*;

public class WanderSteering extends PApplet
{

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		PApplet.main("WanderSteering");
	}
	
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
		PVector wanderCircleCenter;
		
		float rotation;
		float orientation;
		int outerBorder;
		float scaleF;
		
		//arriveVars
		float maxSpeed;
		float targetSpeed;
		float maxAcceleration;
		float radiusOfSatisficing;
		float radiusOfDeceleration;
		float timeToTarget;
		
		//wanderVars
		float wanderOffset;
		float wanderRadius;
		//float wanderRate;
		float targetOrientation;
		float wanderOrientation;
		
		//alignVars
		float maxAngularAcceleration;
		float maxRotation;
		float angularSteer;
		float targetRotation;
		
		Hero()
		{
			this.hero = loadShape("hero.svg");
			
			this.orientation = 0;
			this.maxSpeed = 5;
			this.rotation = 0;
			this.maxRotation = 10;
			this.outerBorder = 40;
			this.maxAcceleration = (float)0.5;
			this.radiusOfSatisficing = 10;
			this.radiusOfDeceleration = 100;
			this.timeToTarget = (float)0.25;
			this.scaleF = (float)0.1;
			
			this.wanderOffset = 200;
			this.wanderRadius = 50;
			//this.wanderRate = 1;
			this.targetOrientation = 0;
			this.wanderOrientation = 0;
			
			this.angularSteer = 0;
			this.targetRotation = 0;
			
			this.position = new PVector(width/2, height/2);
			this.target = new PVector(this.position.x, this.position.y);
			this.velocity = PVector.sub(this.target, this.position);
			this.targetVelocity = PVector.sub(this.target, this.position);			
			this.linearSteer = PVector.sub(this.targetVelocity, this.velocity);
			this.wanderCircleCenter = new PVector(this.velocity.x, this.velocity.y);
			
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
		vDistance = height - 2*h.outerBorder;
		hDistance = width - 2*h.outerBorder;
	}
	
	
	//functions for generating the behavior required
	
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
		
		//if(activateMovement) arrive(h.target);
		wander();
		//wanderAlt();
		
		//COMMENT wander() and UNCOMMENT wanderAlt() to observe ALTERNATE BEHAVIOR
	}
	
	public void wanderAlt()
	{
		h.wanderOrientation += random(-5,5) * TWO_PI;
		wander();
	}
	
	
	public void wander()
	{		
		//Original Orientation behavior
		h.wanderOrientation += random(0,1) - random(0,1);
		h.wanderCircleCenter = h.velocity.copy();
		
		h.wanderCircleCenter.limit(h.wanderOffset);
		h.wanderCircleCenter.add(h.position);
		
		float tempX = h.wanderRadius * cos(h.wanderOrientation);
		float tempY = h.wanderRadius * sin(h.wanderOrientation);
		PVector targetDirection = new PVector(tempX, tempY);
		h.target = PVector.add(h.wanderCircleCenter, targetDirection);
		
		h.targetOrientation = h.wanderOrientation;
//		face(); //call ahead to Align()
//		System.out.println(h.target);
		arrive(h.target);	
	}
	
	public void face()
	{
		float dist = h.velocity.mag();
		if(dist > 0)
		{
			//NEED TO TUNE?
			h.targetOrientation = atan2(h.velocity.y, h.velocity.x);
			align(h.target);
		}
	}
	
	public void align(PVector target)
	{
		h.rotation = h.targetOrientation - h.orientation;
		h.rotation = mapToRange(h.rotation);
		float rotationSize = abs(h.rotation);
		
		if(rotationSize < h.radiusOfSatisficing)
		{
			h.linearSteer.mult(0);
			h.targetVelocity.mult(0);
		}
		else if(rotationSize > h.radiusOfDeceleration)
		{
			h.targetRotation = h.maxRotation;
		}
		else
		{
			h.targetRotation = h.maxRotation * rotationSize / h.radiusOfDeceleration;
		}
		
		h.targetRotation *= h.rotation/rotationSize;
		h.angularSteer = h.targetRotation - h.rotation;
		h.angularSteer /= h.timeToTarget;
		
		float angularAcceleration = abs(h.angularSteer);
		if(angularAcceleration > h.maxAngularAcceleration)
		{
			h.angularSteer /= angularAcceleration;
			h.angularSteer *= h.maxAngularAcceleration;
		}
		
	}
	
	
	public void arrive(PVector target)
	{
		h.targetVelocity = PVector.sub(h.target, h.position);
		newAngle = getNewOrientation(h.targetVelocity, h.orientation);
		float distance = h.targetVelocity.mag();
		if(distance < h.radiusOfSatisficing)
		{
			h.linearSteer.mult(0);
			h.targetVelocity.mult(0);
			
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

	public void outerBorderCheck() 
	{
		if(h.position.x < 0)
		{
			h.position.x = width;
		}
		if(h.position.x > width)
		{
			h.position.x = 0;
		}
		if(h.position.y > height)
		{
			h.position.y = 0;
		}
		if(h.position.y < 0)
		{
			h.position.y = height;
		}
	}
	
	public PVector asVector(float orientation)
	{
		return new PVector(cos(orientation), sin(orientation));
	}
	
	public void updateOrientation()
	{
		float arc;
		h.rotation = h.targetOrientation - h.orientation;
		h.rotation = mapToRange(h.rotation);
		//TO BE USED in BASIC MOTION
//		if(h.target.x == width - h.outerBorder && h.target.y == height - h.outerBorder || h.target.x == h.outerBorder && h.target.y == h.outerBorder)
//		{
//			arc = (float)h.rotation/((float)0.020*hDistance);
//		}
//		else
//			arc = (float)h.rotation/((float)0.020*vDistance);
		arc = (float)h.rotation/15;
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
			breadCrumbs.add(new PVector(h.position.x, h.position.y));
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
		outerBorderCheck();
		dropBreadCrumbs();
		renderCrumbs();
		renderPlayer();
		
	}

	

}
