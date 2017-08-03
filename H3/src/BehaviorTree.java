import java.io.File;
import java.io.FileNotFoundException;

import java.io.PrintWriter;

import processing.core.PApplet;
import processing.core.PVector;

public class BehaviorTree extends PApplet 
{
	public static void main(String[] args)
	{
		PApplet.main("BehaviorTree");
	}
	
	Hero h;
	Monster m;
	boolean isWithinPerception = false, isWithinEatingRange = false;
	int currentAction;
	String action = "";
	PrintWriter pw;
	
	//Decision Tree for Hero
	void climbDecisionTree()
	{
		//get current X and Y
		int newX = (int)h.position.x/h.graph.tileSize;
		int newY = (int)h.position.y/h.graph.tileSize;
		
		int resultantTile = 10*newY + newX;
		
		//begin tree
		if(h.goal != -1) //root node
		{
			if(h.doneFollowing) //doneFollowing  || Add some flag here, which suggests path is complete)
			{
				h.wanderMode = true;
				h.goal = -1;
				h.start = -1;
				h.wander();
			}
			else
			{
				h.follow(h.spritePath);
			}
			
		}
		else
		{
			if(h.detectProximity(resultantTile))
			{
				h.start = resultantTile;
				
				if(h.start % 10 < 5 && h.start / 10 < 4)
				{
					//System.out.println("goint to bottom right");
					h.goal = 67;
				}
				
				if(h.start % 10 >= 5 && h.start / 10 < 4)
				{
					//System.out.println("going to bottom left");
					h.goal = 61;
				}
				
				if(h.start % 10 < 5 && h.start / 10 >= 4)
				{
					//System.out.println("going to top right & start: " + start);
					h.goal = 11;
				}
				
				if(h.start % 10 >= 5 && h.start / 10 >= 4)
				{
					//System.out.println("going to top left");
					h.goal = 27;
				}
					
				h.wanderMode = false;
				profileNewPath(h.graph.tileSize * (h.goal % 10), h.graph.tileSize * (h.goal / 10));
				h.doneFollowing = false;
			}
			else
			{
				h.wanderMode = true;
				h.wander();
			}
		}
		
	}
	
	
	//Behavior Tree for Monster
	void climbBehaviorTree()
	{
		switch(currentAction)
		{
			case 1:
			{
				if(!rangeCheck())
				{
					m.follow(m.spritePath);
				}
				else
				{
					currentAction++;
					action = "chase";
					isWithinPerception = true;
				}
				break;
			}
			
			case 2:
			{
				if(!withinReach() && rangeCheck())
				{
					m.sigMove = true;
					m.seek(h.position);
				}
				else if(!withinReach() && !rangeCheck())
				{
					currentAction--;
					action = "follow";
					isWithinPerception = false;
					m.sigMove = false;
				}
				else
				{	
					currentAction++;
					action = "eat";
				}
				
				break;
			}
			
			case 3:
			{
				action = "follow";
				
				reset();
				break;
			}
			
		}
	}
	
	boolean rangeCheck()
	{
		if(m.reverse)
		{
			if((m.position.x - h.position.x) >= 0)
			{
				if(PVector.dist(m.position, h.position) <= 15*m.r)
					return true;
			}
			else
				if(PVector.dist(m.position, h.position) <= 10*m.r)
					return true;
				
		}
		else
		{
			if((m.position.x - h.position.x) <= 0)
			{
				if(PVector.dist(m.position, h.position) <= 15*m.r)
					return true;
			}
			else
				if(PVector.dist(m.position, h.position) <= 10*m.r)
					return true;
		}
		
//		if(PVector.dist(m.position, h.position) <= 20*m.r)
//			return true;
		return false;
	}
	
	boolean withinReach()
	{
		if(PVector.dist(m.position, h.position) <= 1.5f*m.r)
			return true;
		return false;
	}
	
	
	/////////////////////////////////////////////////////////////////////
	
	//PROCESSING
	
	//INITIALIZATION
	public void settings()
	{
		size(1000, 800);	
	}
	
	public void setup()
	{
		//UNCOMMENT FOR WRITING DATA
		//pw = createWriter("data.csv");
		//pw.println("FrameCount, Within Perception?, Within Eating Distance?, Action");
		
		smooth();
		background(255, 255, 255);
		
		h = new Hero(this);
		m = new Monster(this);
		currentAction = 1;
		action = "follow";
		
	}
	
	public void reset()
	{
		h = new Hero(this);
		m = new Monster(this);
		currentAction = 1;
	}
	

	//LISTENING
	public void mouseClicked()
	{
		profileNewPath(mouseX, mouseY);
	}
	
	void profileNewPath(int X, int Y)
	{
		int newX = X/h.graph.tileSize;
		int newY = Y/h.graph.tileSize;

		h.goal = 10*newY + newX;

		int hposX = (int)h.position.x / h.graph.tileSize;
		int hposY = (int)h.position.y / h.graph.tileSize;

		h.start = 10*hposY + hposX;

		

		h.graph = new Graph(this);
		Connection[] path = h.graph.aStarPathfinder(h.start, h.goal);

		int tempX = h.start % 10;
		int tempY = h.start / 10;
		tempX = tempX * h.graph.tileSize + (h.graph.tileSize/2);
		tempY = tempY * h.graph.tileSize + (h.graph.tileSize/2);
		h.spritePath = new SpritePath();

		h.currentIndex = 0;
		h.spritePath.add(tempX, tempY);
		for(int i = 0; i < path.length; i++)
		{
			tempX = path[i].getToNode() % 10;
			tempY = path[i].getToNode() / 10;
			tempX = tempX * h.graph.tileSize + (h.graph.tileSize/2);
			tempY = tempY * h.graph.tileSize + (h.graph.tileSize/2);
			h.spritePath.add(tempX, tempY);
		}



		for(int i = 0; i < h.graph.nodeArray.length; i++)
		{
			h.graph.nodeArray[i].setCSF(0);
			h.graph.nodeArray[i].setETC(0);
			h.graph.nodeArray[i].setCategory(0);
			h.graph.nodeArray[i].setParent(null);
		}
		//clickFlag = true;
		h.wanderMode = false;
		h.doneFollowing = false;
	}
	

	
	//DRAWING
	public void drawTilesAndObstacles()
	{
		//draw tiles
		for(int i = 0; i < h.graph.numNodes; i++)
		{
			int tileX = i % 10;
			int tileY = i / 10;

			pushMatrix();
			translate(tileX * h.graph.tileSize, tileY * h.graph.tileSize);
			if(h.graph.obstacles.contains(i))
			{
				//System.out.println("black tiles");
				fill(0,0,0);
			}
			//System.out.println("black tiles");
			rect(0, 0, h.graph.tileSize, h.graph.tileSize);
			fill(255, 255, 255);
			popMatrix();
		}

		//draw Start and Goal
		if(h.start != -1)
		{
			pushMatrix();
			fill(0, 255, 0);
			translate((h.start % 10)*h.graph.tileSize,(h.start / 10)*h.graph.tileSize);
			rect(0, 0, h.graph.tileSize, h.graph.tileSize);
			popMatrix();
		}

		if(h.goal != -1)
		{
			pushMatrix();
			fill(0, 0, 255);
			translate((h.goal % 10)*h.graph.tileSize,(h.goal / 10)*h.graph.tileSize);
			rect(0, 0, h.graph.tileSize, h.graph.tileSize);
			popMatrix();
		}

	}

	public void draw()
	{
		//UNCOMMENT FOR WRITING DATA
//		if(frameCount % 10 == 0)
//		{
//		StringBuffer sb = new StringBuffer();
//		sb.append(frameCount);
//		sb.append(",");
//		sb.append(rangeCheck() ? 1 : 0);
//		sb.append(",");
//		sb.append(withinReach() ? 1 : 0);
//		sb.append(",");
//		sb.append(action);
//		
//		pw.println(sb.toString());
//		}
		
		rect(0, 0, width, height); //blank canvas
		drawTilesAndObstacles(); //fill tiles
		
		climbDecisionTree();
		h.runHero();
		
		climbBehaviorTree();
		m.runMonster();
		
		
	}
}
