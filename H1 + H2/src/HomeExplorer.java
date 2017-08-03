import processing.core.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;



public class HomeExplorer extends PApplet 
{

	public static void main(String[] args)  
	{
		// TODO Auto-generated method stub
		PApplet.main("HomeExplorer");
	}


	Hero h;
	float newAngle = 0;
	float vDistance = 0, hDistance = 0;
	Queue<PVector> breadCrumbs = new LinkedList<PVector>();
	int numCrumbs = 80;
	boolean activate;

	class Hero
	{
		PShape hero;

		PVector position;
		PVector velocity;
		PVector acceleration;

		PVector target, linearSteer, targetVelocity;
		float rotation;
		float orientation;
		float wandertheta;
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

		Hero()
		{
			//this.hero = loadShape("hero.svg");
			this.target = new PVector(0, 0);
			this.position = new PVector(tileSize/2, tileSize/2);
			this.maxForce = 0.5f;
			this.maxSpeed = 3f;
			this.predictTime = 0.2f;
			this.acceleration = new PVector(0, 0);
			this.maxAcceleration = 1f;
			this.velocity = new PVector(this.maxSpeed, 0);
			this.pathOffset = 50;
			//this.scaleF = (float)0.1;
			this.currentIndex = 0;
			this.r = 10f;
			this.radiusOfSatisfaction = 10;
			this.radiusOfDeceleration = 80;
			//this.hero.scale(scaleF);
		}

		public void runHero(boolean clickFlag)
		{
			//if(clickFlag)
			updateKinematics();
			updateOrientation();
			dropBreadCrumbs();
			renderCrumbs();
			renderPlayer();

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
				translate(temp.x, temp.y);
				fill(255, 0, 0);
				ellipseMode(RADIUS);
				ellipse(0, 0, 1, 1);
				fill(255);
				popMatrix();

			}
		}

		void follow(SpritePath sp)
		{
			this.target = sp.spritePath.get(this.currentIndex);

			if(PVector.dist(this.position, this.target) <= tileSize)
			{
				this.currentIndex = this.currentIndex + 1;
				
			}
			
			if(this.currentIndex == sp.spritePath.size())
			{
				if(this.position.dist(this.target) <= this.radiusOfSatisfaction)
					this.doneFollowing = true;
				this.arrive(this.target);
				this.currentIndex -= 1;
			}
			else
			{
				this.seek(this.target);
			}
				
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
			newAngle = getNewOrientation(desired, h.orientation);

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
			h.targetVelocity = PVector.sub(h.target, h.position);
			//newAngle = getNewOrientation(h.targetVelocity, h.orientation);
			float distance = h.targetVelocity.mag();
			if(distance < h.radiusOfSatisfaction)
			{
				h.acceleration.mult(0);
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
			
			this.applyForce(h.linearSteer);
			
		}
		
		public float getNewOrientation(PVector velocity, float orient)
		{
			if(velocity.mag() > 0)
			{
				return atan2(velocity.y, velocity.x);
			}
			return orient;
		}

		public void updateOrientation()
		{
			float arc;
			h.rotation = newAngle - h.orientation;
			h.rotation = mapToRange(h.rotation);
			//if(h.target.x == width - h.innerBorder && h.target.y == height - h.innerBorder || h.target.x == h.innerBorder && h.target.y == h.innerBorder)
			//{
			arc = (float)h.rotation/((float)0.020*500);
			//}
			//else
			//arc = (float)h.rotation/((float)0.020*800);
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

		@SuppressWarnings("deprecation")
		void renderPlayer()
		{
			pushMatrix();
			translate(this.position.x, this.position.y);
			//float theta = this.velocity.heading() + radians(90);
			rotate(this.orientation + radians(90));
			fill(0);
			ellipse(0, 0, r, r);
			fill(0);
			beginShape();
			vertex(0, 0);
			vertex(-r*1.732f/2, -r/2);
			vertex(0,-2*r);
			vertex(r*1.732f/2, -r/2);
			endShape();
			fill(0, 255, 255);
			ellipse(0, 0, this.r/2, this.r/2);
			fill(255, 255, 255);
			popMatrix();
		}
		
		//Decision Tree
		void climbDecisionTree()
		{
			//get current X and Y
			int resultantTile = (int)h.position.y/tileSize;
			resultantTile += 10*((int)h.position.x/tileSize);
			
			//begin tree
			if(goal != -1) //root node
			{
				if(doneFollowing) //doneFollowing  || Add some flag here, which suggests path is complete)
				{
					this.wanderMode = true;
					goal = -1;
					start = -1;
					this.wander();
				}
				else
				{
					this.follow(spritePath);
				}
				
			}
			else
			{
				if(this.detectProximity(resultantTile))
				{
					start = resultantTile;
					if(start % 10 <= 5 && start / 10 <= 4)
						goal = 67;
					else if(start % 10 <= 5 && start / 10 >= 4)
						goal = 27;
					else if(start % 10 >= 5 && start / 10 <= 4)
						goal = 61;
					else
						goal = 11;
					this.wanderMode = false;
					profileNewPath(tileSize * (goal % 10), tileSize * (goal / 10));
					this.doneFollowing = false;
				}
				else
				{
					this.wanderMode = true;
					this.wander();
				}
			}
			
		}
		
		boolean detectProximity(int resultantTile)
		{
			//decide on how to check nearest tile
			float warning = 1.414f*tileSize/2 + this.r;
			int i = resultantTile % 10;
			int j = resultantTile / 10;
			boolean top = false, bottom = false, right = false, left = false;
			if(i - 1 >= 0)
			{
				top = true;
				if(g.assister[i - 1][j])
				{
					if(distance(h.position, j, i - 1) < warning)
					{
						return true;
					}
				}
				
			}
			
			if(i + 1 <= g.assister.length - 1)
			{
				bottom = true;
				if(g.assister[i + 1][j])
				{
					if(distance(h.position, j, i + 1) < warning)
					{
						return true;
					}
				}
			}
			
			if(j - 1 >= 0)
			{
				left = true;
				if(g.assister[i][j - 1])
				{
					if(distance(h.position, j - 1, i) < warning)
					{
						return true;
					}
				}
			}
			
			if(j + 1 <= g.assister[i].length - 1)
			{
				right = true;
				if(g.assister[i][j + 1])
				{
					if(distance(h.position, j + 1, i) < warning)
					{
						return true;
					}
				}
			}
			
			if(top && left)
			{
				if(g.assister[i - 1][j - 1])
				{
					if(distance(h.position, j - 1, i - 1) < warning)
					{
						return true;
					}
				}
			}
			
			if(top && right)
			{
				if(g.assister[i - 1][j + 1])
				{
					if(distance(h.position, j + 1, i - 1) < warning)
					{
						return true;
					}
				}
			}
			
			if(bottom && right)
			{
				if(g.assister[i + 1][j + 1])
				{
					if(distance(h.position, j + 1, i + 1) < warning)
					{
						return true;
					}
				}
			}
			
			if(bottom && left)
			{
				if(g.assister[i + 1][j - 1])
				{
					if(distance(h.position, j - 1, i + 1) < warning)
					{
						return true;
					}
				}
			}
			
			
			return false;
		}
		
		float distance(PVector pos, int tileMidX, int tileMidY)
		{
			
			tileMidX *= tileSize;
			tileMidY *= tileSize;
			
			tileMidX += tileSize/2;
			tileMidY += tileSize/2;
			
			float xDist = pos.x - tileMidX;
			float yDist = pos.y - tileMidY;
			return (float)Math.sqrt(xDist*xDist + yDist*yDist);
		}
		
		void wander() 
		{
			float wanderR = 10;         					// Radius for our "wander circle"
			float wanderD = 80;         					// Distance for our "wander circle"
			float change = 0.95f;
			wandertheta += random(-change,change);     	// Randomly change wander theta

			// Now we have to calculate the new position to steer towards on the wander circle
			PVector circlepos = velocity.get();    			// Start with velocity
			circlepos.normalize();            				// Normalize to get heading
			circlepos.mult(wanderD);          				// Multiply by distance
			circlepos.add(position);               			// Make it relative to boid's position

			float h = velocity.heading();        			// We need to know the heading to offset wandertheta

			PVector circleOffSet = new PVector(wanderR*cos(wandertheta+h),wanderR*sin(wandertheta+h));
			PVector target = PVector.add(circlepos,circleOffSet);
			target = check(target);
			seek(target);

		}
		
		PVector check(PVector p)
		{
	        PVector check = p.copy();
	        if(p.x > width - 2*this.r || p.x < 0 + 2*this.r)  
	        	check.x = random(p.x - 100*(p.x)/abs(p.x), p.x);
	        if(p.y > height - 2*this.r || p.y < 0 + 2*this.r)  
	        	check.y = random(p.y - 100*(p.y)/abs(p.y), p.y);
	        return check;
	    }
	}
	
	
	
	

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





	//////////////////////////////////////////////////////////////////////////////

	int goal = -1;
	int start = -1;
	int targetX = 0;
	int targetY = 0;
	Connection[] path = null;
	HashSet<Integer> obstacles = null;
	int pathCounter = 0;
	int tileSize = 100;
	int numNodes = 0;
	int visited = 0;
	

	class Graph
	{
		Node[] nodeArray;
		HashMap<Integer, ArrayList<Connection>> hm;
		boolean[][] assister;

		Graph()
		{

			//this.readFromFile("sg.txt"); //this method isn't required now
			hm = new HashMap<>();
			numNodes = width * height / (tileSize *  tileSize);
			nodeArray = new Node[numNodes];
			//hm = new HashMap<>();
			assister = new boolean[height/tileSize][width/tileSize];

			//initialization
			for(int i = 0; i < numNodes; i++)
			{
				this.nodeArray[i] = new Node(i);
				this.assister[i/10][i%10] = false;
			}

			//set Obstacles
			setObstacles();

		}

		void setObstacles() 
		{
			try
			{
				FileReader fileReader = new FileReader("obstaclesFinal.txt");
				BufferedReader br = new BufferedReader(fileReader);

				String line = null;
				obstacles = new HashSet<>();

				while((line = br.readLine()) != null)
				{
					int x = Integer.parseInt(line);
					//this.nodeArray[x].setObstacle(true);
					this.assister[x/10][x%10] = true;
					obstacles.add(x);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}


		Connection[] getConnections(int fromNode)
		{
			int i = fromNode / 10;
			int j = fromNode % 10;
			boolean up = false, down = false, right = false, left = false;
			ArrayList<Connection> temp = new ArrayList<>();

			if(!this.assister[i][j])
			{
				Connection c = new Connection();
				if((i - 1) >= 0)
				{
					if(!this.assister[i - 1][j])
					{
						up = true;
						c.setCost(10);
						c.setFromNode(fromNode);
						c.setToNode(fromNode - 10);
						//						temp.add(fromNode - 10);
						//						temp.add(10);
						temp.add(c);
					}
				}

				if((j + 1) < assister[i].length)
				{
					if(!this.assister[i][j + 1])
					{
						right = true;
						c = new Connection();
						c.setCost(10);
						c.setFromNode(fromNode);
						c.setToNode(fromNode + 1);
						temp.add(c);
						//						temp.add(fromNode + 1);
						//						temp.add(10);
					}
				}

				if((i + 1) < assister.length)
				{
					if(!this.assister[i + 1][j])
					{
						down = true;
						c = new Connection();
						c.setCost(10);
						c.setFromNode(fromNode);
						c.setToNode(fromNode + 10);
						temp.add(c);
						//						temp.add(fromNode + 10);
						//						temp.add(10);
					}
				}

				if((j - 1) >= 0)
				{
					if(!this.assister[i][j - 1])
					{
						left = true;
						c = new Connection();
						c.setCost(10);
						c.setFromNode(fromNode);
						c.setToNode(fromNode - 1);
						temp.add(c);
						//						temp.add(fromNode - 1);
						//						temp.add(10);
					}
				}

				if(up && right)
				{
					if(!this.assister[i - 1][j + 1])
					{
						c = new Connection();
						c.setCost(14);
						c.setFromNode(fromNode);
						c.setToNode(fromNode - 9);
						temp.add(c);
						//						temp.add(fromNode - 9);
						//						temp.add(14);
					}
				}

				if(up && left)
				{
					if(!this.assister[i - 1][j - 1])
					{
						c = new Connection();
						c.setCost(14);
						c.setFromNode(fromNode);
						c.setToNode(fromNode - 11);
						temp.add(c);
						//						temp.add(fromNode - 11);
						//						temp.add(14);
					}
				}

				if(down && right)
				{
					if(!this.assister[i + 1][j + 1])
					{
						c = new Connection();
						c.setCost(14);
						c.setToNode(fromNode + 11);
						c.setFromNode(fromNode);
						temp.add(c);
						//						temp.add(fromNode + 11);
						//						temp.add(14);
					}
				}

				if(down && left)
				{
					if(!this.assister[i + 1][j - 1])
					{
						c = new Connection();
						c.setCost(14);
						c.setFromNode(fromNode);
						c.setToNode(fromNode + 9);
						temp.add(c);
						//						temp.add(fromNode + 9);
						//						temp.add(14);
					}
				}
			}

			Connection[] result = new Connection[temp.size()];
			result = temp.toArray(result);
			return result;
		}

		int smallestOpen()
		{
			int i = 0, mini = 0; 
			int min = Integer.MAX_VALUE;
			while(i < this.nodeArray.length)
			{
				if(nodeArray[i].category == 1)
				{
					int temp = nodeArray[i].getETC();
					if(min > temp)
					{
						min = temp;
						mini = i;
					}

				}
				i++;
			}
			return mini;
		}

	}

	

	class Node
	{
		int ID; //nodeInteger
		Connection parent; //connection to retrace path
		//ArrayList<Integer> connections; //getConnections()
		int costSoFar; //csf
		int estimatedTotalCost; //etc
		int category; // -1 for closed, 0 for unvisited, 1 for open


		Node(int ident)
		{
			this.ID = ident;
			this.parent = new Connection();
			this.costSoFar = 0;
			this.estimatedTotalCost = 0;
			this.category = 0;

		}

		int getID()
		{
			return this.ID;
		}

		Connection getParent()
		{
			return this.parent;
		}

		void setParent(Connection parent)
		{
			this.parent = parent;
		}

		int getCSF()
		{
			return this.costSoFar;
		}

		void setCSF(int csf)
		{
			this.costSoFar = csf; 
		}

		int getETC()
		{
			return this.estimatedTotalCost;
		}

		void setETC(int etc)
		{
			this.estimatedTotalCost = etc;
		}

		int getCategory()
		{
			return this.category;
		}

		void setCategory(int category)
		{
			this.category = category;
		}


	}

	

	class Connection
	{
		int cost;
		int fromNode;
		int toNode;

		Connection()
		{
			cost = 0;
			fromNode = -1;
			toNode = -1;
		}

		int getCost()
		{
			return this.cost;
		}

		int getFromNode()
		{
			return this.fromNode;
		}

		int getToNode()
		{
			return this.toNode;
		}

		void setCost(int cost)
		{
			this.cost = cost;
		}

		void setFromNode(int fromNode)
		{
			this.fromNode = fromNode;
		}

		void setToNode(int toNode)
		{
			this.toNode = toNode;
		}
	}

	int heuristic(int node)
	{
		int currentY = node / 10;
		int currentX = node % 10;

		int xDistance = Math.abs(currentX - targetX);
		int yDistance = Math.abs(currentY - targetY);
		int heuristic = 0;

		if(xDistance > yDistance)
		{
			heuristic = 14 * yDistance + 10 * (xDistance - yDistance);
		}
		else
		{
			heuristic = 14 * xDistance + 10 * (yDistance - xDistance);
		}

		return heuristic;
	}

	Connection[] aStarPathfinder(Graph g, int start, int goal)
	{
		ArrayList<Connection> path = new ArrayList<>();

		//code for A*

		g.nodeArray[start].setETC(heuristic(start));
		g.nodeArray[start].setCategory(1);
		int openCount = 1;
		int closeCount = 0;
		visited++;
		Node current = new Node(0);
		int previous = 0;
		while(openCount > 0)
		{
			current = new Node(g.smallestOpen());
			//System.out.println("up " + current.ID);

			if(current.ID == goal)
			{
				Connection finalEdge = new Connection();
				finalEdge.setFromNode(previous);
				finalEdge.setToNode(current.ID);
				current.setParent(finalEdge);
				break;
			}

			System.out.println(current.ID);
			Connection[] connections = g.getConnections(current.ID);

			for(Connection c: connections)
			{
				int endNode = c.getToNode();

				//System.out.println("exploring " + endNode);
				int endNodeCost = current.costSoFar + c.getCost();
				int endNodeHeuristic = 0;

				int nodeFinder = g.nodeArray[endNode].getCategory();
				if(nodeFinder == -1)
				{
					continue;
				}
				else if(nodeFinder == 1)
				{
					//System.out.println("current: " + endNodeCost + " Old: " + g.nodeArray[endNode].getCSF());
					if(g.nodeArray[endNode].getCSF() <= endNodeCost)
						continue;

					endNodeHeuristic = g.nodeArray[endNode].getETC() - g.nodeArray[endNode].getCSF();
				}
				else
				{
					endNodeHeuristic = heuristic(endNode);
				}

				g.nodeArray[endNode].setCSF(endNodeCost);
				//System.out.println("CSF for: " + endNode + "is" + g.nodeArray[endNode].getCSF());

				g.nodeArray[endNode].setParent(c);
				//System.out.println("parent of " + endNode + " is " + current.ID);
				g.nodeArray[endNode].setETC(endNodeCost + endNodeHeuristic);
				//System.out.println("ETC of " + endNode + " is " + g.nodeArray[endNode].getETC());

				g.nodeArray[endNode].setCategory(1);
				openCount++;
				visited++;
				//System.out.print(endNode + " ");
			}

			previous = current.ID;
			//System.out.println("closed: " + previous);
			//System.out.println("before Closing " + current.ID);
			g.nodeArray[current.ID].setCategory(-1);
			openCount--;
		}

		if(current.ID != goal)
		{
			System.out.println("hey");
			return new Connection[0];
		}
		else
		{
			while(current.ID != start)
			{
				Connection c = current.getParent();
				//System.out.println(c.getFromNode());
				path.add(c);
				int parent = c.getFromNode();
				current = g.nodeArray[parent];
			}
		}

		Connection[] output = new Connection[path.size()];
		Collections.reverse(path);
		output = path.toArray(output);

		return output;
	}	


	Graph g = null;
	ArrayList<Integer> persistentPath = new ArrayList<>();
	boolean clickFlag = false;
	SpritePath spritePath = null;



	public void settings()
	{
		size(1000, 800);	
	}

	public void mouseClicked()
	{
		profileNewPath(mouseX, mouseY);
	}
	
	void profileNewPath(int X, int Y)
	{
		int newX = X/tileSize;
		int newY = Y/tileSize;

		goal = 10*newY + newX;

		int hposX = (int)h.position.x / tileSize;
		int hposY = (int)h.position.y / tileSize;

		start = 10*hposY + hposX;

		targetY = goal / 10;
		targetX = goal % 10;

		g = new Graph();
		path = aStarPathfinder(g, start, goal);

		int tempX = start % 10;
		int tempY = start / 10;
		tempX = tempX * tileSize + (tileSize/2);
		tempY = tempY * tileSize + (tileSize/2);
		spritePath = new SpritePath();

		h.currentIndex = 0;
		spritePath.add(tempX, tempY);
		for(int i = 0; i < path.length; i++)
		{
			tempX = path[i].getToNode() % 10;
			tempY = path[i].getToNode() / 10;
			tempX = tempX * tileSize + (tileSize/2);
			tempY = tempY * tileSize + (tileSize/2);
			spritePath.add(tempX, tempY);
		}



		for(int i = 0; i < g.nodeArray.length; i++)
		{
			g.nodeArray[i].setCSF(0);
			g.nodeArray[i].setETC(0);
			g.nodeArray[i].setCategory(0);
			g.nodeArray[i].setParent(null);
		}
		clickFlag = true;
		h.wanderMode = false;
		h.doneFollowing = false;
	}
	

	public void setup()
	{
		smooth();
		background(255, 255, 255);
		g = new Graph();
		h = new Hero();
	}

	public void drawTilesAndObstacles()
	{
		//draw tiles
		for(int i = 0; i < numNodes; i++)
		{
			int tileX = i % 10;
			int tileY = i / 10;

			pushMatrix();
			translate(tileX * tileSize, tileY * tileSize);
			if(obstacles.contains(i))
			{
				//System.out.println("black tiles");
				fill(0,0,0);
			}
			//System.out.println("black tiles");
			rect(0, 0, tileSize, tileSize);
			fill(255, 255, 255);
			popMatrix();
		}

		//draw Start and Goal
		if(start != -1)
		{
			pushMatrix();
			fill(0, 255, 0);
			translate((start % 10)*tileSize,(start / 10)*tileSize);
			rect(0, 0, tileSize, tileSize);
			popMatrix();
		}

		if(goal != -1)
		{
			pushMatrix();
			fill(0, 0, 255);
			translate((goal % 10)*tileSize,(goal / 10)*tileSize);
			rect(0, 0, tileSize, tileSize);
			popMatrix();
		}

	}

	public void draw()
	{
		rect(0, 0, width, height); //blank canvas
		drawTilesAndObstacles(); //fill tiles
		
		h.climbDecisionTree();
		h.runHero(clickFlag);
	}

}
