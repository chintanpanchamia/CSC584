import java.util.ArrayList;

import processing.core.*;

public class FlockingBoid extends PApplet 
{

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		PApplet.main("FlockingBoid");
	}
	
	float perceptionDistance = 25;
	float minimumPerceptionDistance = 10;
	
	//configuration
	public void settings()
	{
		size(800, 800);	
	}
	
	public void setup()
	{
		smooth();
		background(255, 255, 255);
		f = new Flock();
		
		for(int i = 0; i < 30; i++)
		{
			Hero h = new Hero(random(width), random(height));
			h.leadFlag = i % 15 == 0 ? true:false;
			h.renderPlayer();
			f.addHero(h);
		}
	}
	
	public void mousePressed()
	{
		Hero h = new Hero(mouseX, mouseY);
		f.addHero(h);
	}
	
	public void draw()
	{
		background(255);
		f.run();
	}
	
	//components
	Flock f;
	class Flock 
	{
		ArrayList<Hero> heroes;
		Flock() 
		{
			heroes = new ArrayList<Hero>();
			
		}
		void addHero(Hero h) 
		{
			heroes.add(h);
		}
		void run() 
		{
		    for (Hero h : heroes) 
		    {
		    	h.run(heroes); 
		    }
		}
	}
	
	
	
	
	class Hero
	{
		PShape hero;
		
		PVector position;
		PVector velocity;
		PVector acceleration;
		float maxSpeed;
		float maxAcceleration;
		float maxForce;
		
		boolean leadFlag;
		
		PVector targetVelocity;
		PVector linearSteer; //acceleration to be added
		PVector target;
		PVector wanderCircleCenter;
		
		float rotation;
		float orientation;
		
		float scaleF;
		
		//arriveVars
		
		float targetSpeed;
		
		float radiusOfSatisficing;
		float radiusOfDeceleration;
		float timeToTarget;
		float newAngle;
		
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
		
		Hero(float x, float y)
		{
			this.hero = loadShape("hero.svg");
			
			this.orientation = 0;
			this.maxSpeed = 2;
			this.rotation = 0;
			this.maxRotation = 10;
			
			this.maxAcceleration = (float)0.01;
			this.radiusOfSatisficing = 10;
			this.radiusOfDeceleration = 100;
			this.timeToTarget = (float)0.25;
			this.scaleF = (float)0.08;
			
			this.wanderOffset = 100;
			this.wanderRadius = 50;
			//this.wanderRate = 1;
			this.targetOrientation = 0;
			this.wanderOrientation = 0;
			this.leadFlag = false;
			this.maxForce = 0.01f;
			
			this.angularSteer = 0;
			this.targetRotation = 0;
			this.newAngle = 0;
			this.position = new PVector(x, y);
			this.target = new PVector(this.position.x, this.position.y);
			this.velocity = new PVector(random(width), random(height));
			this.acceleration = new PVector(0, 0);
			this.targetVelocity = PVector.sub(this.target, this.position);			
			this.linearSteer = PVector.sub(this.targetVelocity, this.velocity);
			this.wanderCircleCenter = new PVector(this.velocity.x, this.velocity.y);
			
			this.hero.scale(scaleF);
		}
		
		
		
		//methods
		

		
		public void run(ArrayList<Hero> heroes)
		{
			flock(heroes);
			updatePosition();
			outerBorderCheck();
			
			renderPlayer();
			if(leadFlag)
			{
				wander();
			}
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
		
		
		
		
		public void flock(ArrayList<Hero> heroes)
		{
			PVector separation = separate(heroes);
			PVector cohesion = cohesion(heroes);
			PVector align = align(heroes);
			
			PVector weights = new PVector(1.8f, 1.15f, 1.35f);
			//weights.normalize();
			
			separation.mult(weights.x);
			cohesion.mult(weights.y);
			align.mult(weights.z);
			
			this.acceleration.add(separation);
			this.acceleration.add(cohesion);
			this.acceleration.add(align);
		}
		
		public PVector separate(ArrayList<Hero> heroes)
		{
			PVector steering = new PVector(0, 0);
			int counter = 0;
			
			for(int i = 0; i < heroes.size(); i++)
			{
				Hero h = (Hero)heroes.get(i);
				float positionDist = abs(this.position.mag() - h.position.mag());
				
				if(positionDist > 0 && positionDist < minimumPerceptionDistance)
				{
					PVector positionDelta = PVector.sub(this.position, h.position);
					positionDelta.normalize().div(positionDist);
					steering.add(positionDelta);
					counter++;
				}
					
			}
			if(counter > 0)
				steering.div((float)counter);
			
			if(steering.mag() > 0)
			{
				steering.normalize();
				steering.mult(this.maxSpeed);
				steering.sub(this.velocity);
				steering.limit(this.maxForce);
			}
			
			return steering;
		}
		
		public PVector align(ArrayList<Hero> heroes)
		{
			PVector sum = new PVector(0,0);
			int counter = 0;
			for(int i = 0; i < heroes.size(); i++)
			{
				Hero h = (Hero)heroes.get(i);
				float positionDist = abs(this.position.mag() - h.position.mag());
				if(h.leadFlag && (positionDist < perceptionDistance))
				{
					h.velocity.normalize().mult(h.maxSpeed);
					return PVector.sub(h.velocity, velocity).limit(this.maxForce);
			    }
				if(positionDist > 0 && positionDist < perceptionDistance)
				{
					sum.add(h.velocity);
					counter++;
				}
			}
			
			if(counter > 0)
			{
				sum.div((float)counter);
				sum.normalize();
				sum.mult(this.maxSpeed);
				PVector steering = PVector.sub(sum, this.velocity);
				steering.limit(this.maxForce);
				return steering;
			}
			else
			{
				return new PVector(0,0);
			}
		}
		
		public PVector cohesion(ArrayList<Hero> heroes)
		{
			PVector sum = new PVector(0,0);
			int counter = 0;
			for(int i = 0; i < heroes.size(); i++)
			{
				Hero h = (Hero)heroes.get(i);
				float positionDist = abs(this.position.mag() - h.position.mag());
				if(h.leadFlag)
				{
					PVector stickup = PVector.mult(h.velocity, -2).normalize();
					stickup.mult((float)0.25).add(h.position);
					return seek(stickup);
				}
				if(positionDist > 0 && positionDist < perceptionDistance)
				{
					sum.add(h.position);
					counter++;
				}
			}
			
			if(counter > 0)
			{
				sum.div(counter);
				return seek(sum);
			}
			else
			{
				return new PVector(0, 0);
			}
		}

		public PVector seek(PVector target)
		{
			this.target = PVector.sub(target, this.position);
			this.target.normalize();
			this.target.mult(this.maxSpeed);
			
			PVector steering = PVector.sub(this.target, this.velocity);
			steering.limit(this.maxForce);
			return steering;
		}
		
		public void updatePosition()
		{
			this.velocity.add(this.acceleration).limit(maxSpeed);
			this.position.add(this.velocity);
			this.acceleration.mult(0);
		}
		
		public void outerBorderCheck()
		{
			if(this.position.x < 0)
			{
				this.position.x = width;
			}
			if(this.position.x > width)
			{
				this.position.x = 0;
			}
			if(this.position.y > height)
			{
				this.position.y = 0;
			}
			if(this.position.y < 0)
			{
				this.position.y = height;
			}
		}
		
		public void renderPlayer()
		{
			pushMatrix();
			translate(this.position.x, this.position.y);
			rotate(this.velocity.heading());
			shape(this.hero, -this.hero.width/60, -this.hero.height/40);
			if(this.leadFlag) 
			{ 
				fill(255, 0, 0); 
				ellipse(0, 0, 7, 7); 
			}
			else 
			{ 
				fill(0, 255, 255); 
				ellipse(0, 0, 3, 3);
			}
			
			fill(255, 255, 255);
			popMatrix();
		}
		
		public void wander()
		{
			        
		    float change = 15f;
		    this.wanderOrientation += random(-change,change);     // Randomly change wander theta

		    // Now we have to calculate the new position to steer towards on the wander circle
		    PVector circlepos = velocity.copy();    // Start with velocity
		    circlepos.normalize();            // Normalize to get heading
		    circlepos.mult(this.wanderOffset);          // Multiply by distance
		    circlepos.add(position);               // Make it relative to boid's position

		    float targetOrientation = this.velocity.heading();       // We need to know the heading to offset wandertheta
		    targetOrientation = this.wanderOrientation + targetOrientation;
		    PVector circleOffSet = new PVector(this.wanderRadius*cos(targetOrientation),this.wanderRadius*sin(targetOrientation));
		    PVector target = PVector.add(circlepos,circleOffSet);
		    this.acceleration.add(steering(target, false));
		}
		
		PVector steering(PVector target, boolean decelerate) 
		{
		      PVector steering; 
		      PVector desiredTarget = PVector.sub(target,this.position);
		      float distance = desiredTarget.mag(); 
		      if (distance > 0) 
		      {
		    	  desiredTarget.normalize();
		    	  if ((decelerate) && (distance < this.wanderOffset)) 
		    		  desiredTarget.mult(this.maxSpeed*(distance/this.wanderOffset)); 
		    	  else 
		    		  desiredTarget.mult(this.maxSpeed);
		    	  steering = PVector.sub(desiredTarget,velocity);
		    	  steering.limit(this.maxForce);  
		      } 
		      else 
		      {
		    	  steering = new PVector(0,0);
		      }
		      return steering;
		}
		
	}
	
	
	
	

}
