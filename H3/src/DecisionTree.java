import processing.core.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;



public class DecisionTree extends PApplet 
{

	public static void main(String[] args)  
	{
		// TODO Auto-generated method stub
		PApplet.main("DecisionTree");
	}


	Hero h;
	
	
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
	

	public void setup()
	{
		smooth();
		background(255, 255, 255);
		
		h = new Hero(this);
		
	}

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
		
	}

}
