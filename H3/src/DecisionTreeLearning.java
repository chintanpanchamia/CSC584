
import processing.core.PApplet;
import processing.core.PVector;

public class DecisionTreeLearning extends PApplet 
{
	public static void main(String[] args)
	{
		PApplet.main("DecisionTreeLearning");
	}
	
	Hero h;
	Monster m1, m2; //m1 - BEHAVIOR TREE; m2 - LEARNED DECISION TREE
	boolean isWithinPerception = false, isWithinEatingRange = false;
	int currentAction;
	String action = "";
	
	
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
	
	void climbLearnedDecisionTree()
	{
		if(withinReach(m2))
		{
			reset();
		}
		else
		{
			if(rangeCheck(m2))
			{
				m2.sigMove = true;
				m2.seek(h.position);
			}
			else
			{
				m2.sigMove = false;
				m2.follow(m2.spritePath);
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
				if(!rangeCheck(m1))
				{
					m1.follow(m1.spritePath);
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
				if(!withinReach(m1) && rangeCheck(m1))
				{
					m1.sigMove = true;
					m1.seek(h.position);
				}
				else if(!withinReach(m1) && !rangeCheck(m1))
				{
					currentAction--;
					action = "follow";
					isWithinPerception = false;
					m1.sigMove = false;
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
	
	boolean rangeCheck(Monster m)
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
	
	boolean withinReach(Monster m)
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
		
		
		smooth();
		background(255, 255, 255);
		
		h = new Hero(this);
		m1 = new Monster(this);
		m2 = new Monster(this);
		m2.maxSpeed *= 0.85f;
		currentAction = 1;
		action = "follow";
		
	}
	
	public void reset()
	{
		h = new Hero(this);
		m1 = new Monster(this);
		m2 = new Monster(this);
		m2.maxSpeed *= 0.85f;
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
		
		rect(0, 0, width, height); //blank canvas
		drawTilesAndObstacles(); //fill tiles
		
		climbDecisionTree();
		h.runHero();
		
		climbBehaviorTree();
		m1.runMonster();
		
		climbLearnedDecisionTree();
		m2.runMonster();
		
	}
}
