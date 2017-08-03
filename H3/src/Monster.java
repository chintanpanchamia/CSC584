
import java.util.LinkedList;
import java.util.Queue;

import processing.core.*;

public class Monster
{
	PShape monster;

	PVector position;
	PVector velocity;
	PVector acceleration;

	PVector target, linearSteer, targetVelocity;
	float rotation;
	float orientation;
	float wandertheta;
	float newAngle;
	float radiusOfSatisfaction;
	float radiusOfDeceleration;
	float targetSpeed;
	float maxAcceleration;

	float scaleF;
	float maxSpeed;
	float maxForce;

	float predictTime;
	float pathOffset;

	int currentIndex;
	float r;
	
	boolean wanderMode;
	boolean doneFollowing;
	boolean reverse;
	boolean sigMove;
	
	Queue<PVector> breadCrumbs;
	int numCrumbs;
	
	int start;
	int goal;
	
	Graph graph;
	SpritePath spritePath;
	
	PApplet pr;

	Monster(PApplet parent)
	{
		//this.hero = loadShape("hero.svg");
		this.graph = new Graph(parent);
		this.start = -1;
		this.goal = -1;
		this.spritePath = new SpritePath();
		this.spritePath = this.populate(this.spritePath);
		this.numCrumbs = 80;
		this.target = new PVector(0, 0);
		
		this.position = new PVector(this.spritePath.getStart().x, this.spritePath.getStart().y);
		this.maxForce = 0.5f;
		this.maxSpeed = 2.5f;
		this.predictTime = 0.2f;
		this.acceleration = new PVector(0, 0);
		this.maxAcceleration = 1f;
		this.velocity = new PVector(this.maxSpeed, 0);
		this.pathOffset = 50;
		
		this.currentIndex = 0;
		this.r = 10f;
		this.radiusOfSatisfaction = 10;
		this.radiusOfDeceleration = 80;
		this.breadCrumbs = new LinkedList<PVector>();
		this.pr = parent;
		
	}

	public void runMonster()
	{
		updateKinematics();
		updateOrientation();
			
		dropBreadCrumbs();
		renderCrumbs();
		renderPlayer();
	}

	public void dropBreadCrumbs()
	{
		if(pr.frameCount % this.maxSpeed == 0)
		{
			if(breadCrumbs.size() == this.numCrumbs)
			{
				this.breadCrumbs.remove();
			}
			this.breadCrumbs.add(new PVector(this.position.x, this.position.y));

		}
	}

	public void renderCrumbs()
	{

		for(PVector temp: this.breadCrumbs)
		{
			pr.pushMatrix();
			pr.translate(temp.x, temp.y);
			pr.fill(255, 0, 0);
			pr.ellipseMode(PConstants.RADIUS);
			pr.ellipse(0, 0, 1, 1);
			pr.fill(255);
			pr.popMatrix();

		}
	}

	void follow(SpritePath sp)
	{
		this.target = sp.spritePath.get(this.currentIndex);

		if(PVector.dist(this.position, this.target) <= this.graph.tileSize/2)
		{
			if(this.reverse)
				this.currentIndex = this.currentIndex - 1;
			else
				this.currentIndex = this.currentIndex + 1;
			
		}
		
		if(this.currentIndex == sp.spritePath.size() || this.currentIndex == -1)
		{
			if(this.reverse)
				this.currentIndex += 1;
			else
				this.currentIndex -= 1;
			
			if(this.position.dist(this.target) <= this.radiusOfSatisfaction)
				this.reverse = !this.reverse;
			//this.arrive(this.target);
			
		}
		
		this.seek(this.target);
			
	}
	
	SpritePath populate(SpritePath sp)
	{
		int[] monsterPath = {30, 31, 32, 33, 43, 44, 34, 35, 36, 46, 47, 37, 38, 39};
		for(int i = 0; i < monsterPath.length; i++)
		{
			int tempX = monsterPath[i] % 10;
			int tempY = monsterPath[i] / 10;
			tempX = tempX * this.graph.tileSize + this.graph.tileSize/2;
			tempY = tempY * this.graph.tileSize + this.graph.tileSize/2;
			
			sp.add(tempX, tempY);
		}
		return sp;
	}

	void updateKinematics()
	{
		this.velocity.add(this.acceleration);
		this.velocity.limit(this.maxSpeed);
		this.position.add(this.velocity);
		this.acceleration.mult(0);
	}

	void applyForce(PVector force)
	{
		this.acceleration.add(force);
	}

	void seek(PVector target)
	{
		PVector desired = PVector.sub(target, this.position);
		this.newAngle = getNewOrientation(desired, this.orientation);

		desired.normalize();
		float tempSpeed = this.maxSpeed;
		if(this.wanderMode)
			tempSpeed = 0.5f*tempSpeed;
		desired.mult(tempSpeed);

		PVector steering = PVector.sub(desired, this.velocity);
		float tempForce = this.maxForce;
		if(this.wanderMode)
			tempForce = 0.5f*tempForce;
		steering.limit(tempForce);
		this.applyForce(steering);
	}

	public void arrive(PVector target)
	{
		this.targetVelocity = PVector.sub(this.target, this.position);
		//newAngle = getNewOrientation(h.targetVelocity, h.orientation);
		float distance = this.targetVelocity.mag();
		if(distance < this.radiusOfSatisfaction)
		{
			this.acceleration.mult(0);
			this.targetVelocity.mult((float)0.0);
			
		}
		else if(distance < this.radiusOfDeceleration)
		{
			this.targetSpeed = this.maxSpeed * distance / this.radiusOfDeceleration;
		}
		else
		{
			this.targetSpeed = this.maxSpeed;
		}
		
		
		this.targetVelocity.normalize();
		this.targetVelocity.mult(this.targetSpeed);
		
		this.linearSteer = PVector.sub(this.targetVelocity, this.velocity);
		
		this.applyForce(this.linearSteer);
		
	}
	
	public float getNewOrientation(PVector velocity, float orient)
	{
		if(velocity.mag() > 0)
		{
			return PApplet.atan2(velocity.y, velocity.x);
		}
		return orient;
	}

	public void updateOrientation()
	{
		float arc;
		if(!this.sigMove)
		{
			this.rotation = newAngle - this.orientation;
			this.rotation = mapToRange(this.rotation);
			//if(h.target.x == width - h.innerBorder && h.target.y == height - h.innerBorder || h.target.x == h.innerBorder && h.target.y == h.innerBorder)
			//{
			arc = (float)this.rotation/((float)0.020*500);
		}
		else
			arc = PApplet.radians(30);
		//}
		//else
		//arc = (float)h.rotation/((float)0.020*800);
		this.orientation += arc;
	}
	
	public void signatureMove()
	{
		this.orientation += PApplet.radians(30);
	}

	public float mapToRange(float rotation)
	{
		rotation = rotation % PConstants.TWO_PI;
		if(PApplet.abs(rotation) <= PConstants.PI)
		{
			return rotation;
		}
		else if(rotation > PConstants.PI)
			return rotation - PConstants.TWO_PI;
		else
			return rotation + PConstants.TWO_PI;
	}

	@SuppressWarnings("deprecation")
	void renderPlayer()
	{
		pr.pushMatrix();
		pr.translate(this.position.x, this.position.y);
		//float theta = this.velocity.heading() + radians(90);
		//if(!this.sigMove)
		pr.rotate(this.orientation + PApplet.radians(90));
		pr.fill(0);
		pr.ellipse(0, 0, r, r);
		pr.fill(0);
		pr.beginShape();
		pr.vertex(0, 0);
		pr.vertex(-r*1.732f/2, -r/2);
		pr.vertex(0,-2*r);
		pr.vertex(r*1.732f/2, -r/2);
		pr.endShape();
		pr.fill(255, 0, 0);
		pr.ellipse(0, 0, this.r/2, this.r/2);
		pr.fill(255, 255, 255);
		pr.popMatrix();
	}
	
	//Decision Tree
	
	
	//BehaviorTree
	
	
	boolean detectProximity(int resultantTile)
	{
		//decide on how to check nearest tile
		float warning = 1.414f*this.graph.tileSize/2 + this.r;
		int i = resultantTile / 10;
		int j = resultantTile % 10;
		boolean top = false, bottom = false, right = false, left = false;
		
		if(PVector.dist(this.position, new PVector(this.position.x, pr.height)) < 2*this.r)
			return true;
		
		if(PVector.dist(this.position, new PVector(this.position.x, 0)) < 2*this.r)
			return true;
		
		if(PVector.dist(this.position, new PVector(pr.width, this.position.y)) < 2.5f*this.r)
			return true;
		
		if(PVector.dist(this.position, new PVector(0, this.position.y)) < 2.5f*this.r)
			return true;
		
		if(i - 1 >= 0)
		{
			top = true;
			if(this.graph.assister[i - 1][j])
			{
				if(distance(this.position, j, i - 1) < warning)
				{
					return true;
				}
			}
			
		}
		
		if(i + 1 <= this.graph.assister.length - 1)
		{
			bottom = true;
			if(this.graph.assister[i + 1][j])
			{
				if(distance(this.position, j, i + 1) < warning)
				{
					return true;
				}
			}
		}
		
		if(j - 1 >= 0)
		{
			left = true;
			if(this.graph.assister[i][j - 1])
			{
				if(distance(this.position, j - 1, i) < warning)
				{
					return true;
				}
			}
		}
		
		if(j + 1 <= this.graph.assister[i].length - 1)
		{
			right = true;
			if(this.graph.assister[i][j + 1])
			{
				if(distance(this.position, j + 1, i) < warning)
				{
					return true;
				}
			}
		}
		
		if(top && left)
		{
			if(this.graph.assister[i - 1][j - 1])
			{
				if(distance(this.position, j - 1, i - 1) < warning)
				{
					return true;
				}
			}
		}
		
		if(top && right)
		{
			if(this.graph.assister[i - 1][j + 1])
			{
				if(distance(this.position, j + 1, i - 1) < warning)
				{
					return true;
				}
			}
		}
		
		if(bottom && right)
		{
			if(this.graph.assister[i + 1][j + 1])
			{
				if(distance(this.position, j + 1, i + 1) < warning)
				{
					return true;
				}
			}
		}
		
		if(bottom && left)
		{
			if(this.graph.assister[i + 1][j - 1])
			{
				if(distance(this.position, j - 1, i + 1) < warning)
				{
					return true;
				}
			}
		}
		
		
		return false;
	}
	
	float distance(PVector pos, int tileMidX, int tileMidY)
	{
		
		tileMidX *= this.graph.tileSize;
		tileMidY *= this.graph.tileSize;
		
		tileMidX += this.graph.tileSize/2;
		tileMidY += this.graph.tileSize/2;
		
		float xDist = pos.x - tileMidX;
		float yDist = pos.y - tileMidY;
		return (float)Math.sqrt(xDist*xDist + yDist*yDist);
	}
	
	void wander() 
	{
		float wanderR = 10;         					// Radius for our "wander circle"
		float wanderD = 120;         					// Distance for our "wander circle"
		float change = 1f;
		wandertheta += pr.random(-change,change);     	// Randomly change wander theta

		// Now we have to calculate the new position to steer towards on the wander circle
		PVector circlepos = velocity.get();    			// Start with velocity
		circlepos.normalize();            				// Normalize to get heading
		circlepos.mult(wanderD);          				// Multiply by distance
		circlepos.add(position);               			// Make it relative to boid's position

		float h = velocity.heading();        			// We need to know the heading to offset wandertheta

		PVector circleOffSet = new PVector(wanderR*PApplet.cos(wandertheta+h),wanderR*PApplet.sin(wandertheta+h));
		PVector target = PVector.add(circlepos,circleOffSet);
		target = check(target);
		seek(target);

	}
	
	
	PVector check(PVector p)
	{
        PVector check = p.copy();
        if(p.x > pr.width - this.graph.tileSize || p.x < 0 + this.graph.tileSize)  
        	check.x = pr.random(p.x - 250*(p.x)/pr.abs(p.x), p.y);
        if(p.y > pr.height - this.graph.tileSize || p.y < 0 + this.graph.tileSize)  
        	check.y = pr.random(p.x, p.y - 250*(p.y)/pr.abs(p.y));
        return check;
    }
	
//	PVector check(PVector p)
//	{
//        PVector check = p.copy();
//        if(p.x > pr.width - 5*this.r && p.x <= pr.width)  
//        	check.x = pr.random(p.x, p.x - this.graph.tileSize/2); //- 100*(p.x)/PApplet.abs(p.x)
//        if(p.x <= 0 + 5*this.r && p.x > 0)
//        	check.x = pr.random(p.x, p.x + this.graph.tileSize/2);
//        if(p.y > pr.height - 5*this.r && p.y <= pr.height)  
//        	check.y = pr.random(p.y, p.y - this.graph.tileSize/2); //100*(p.y)/PApplet.abs(p.y)
//        if(p.y <= 0 + 5*this.r && p.y > 0)
//        	check.y = pr.random(p.y, p.y + this.graph.tileSize/2);
//        return check;
//    }
}