import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import processing.core.*;

public class SecondGraph extends PApplet
{

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		PApplet.main("SecondGraph");
	}
	
	int goal = 0;
	int start = 0;
	int targetX = 0;
	int targetY = 0;
	Connection[] path = null;
	//HashSet<Integer> obstacles = null;
	int pathCounter = 0;
	int tileSize = 10;
	int numNodes = 0;
	int visited = 0;
	
	class Graph
	{
		Node[] nodeArray;
		HashMap<Integer, ArrayList<Connection>> hm;
		
		Graph()
		{
			
			//this.readFromFile("sg.txt"); //this method isn't required now
			
			numNodes = width * height / (tileSize *  tileSize);
			nodeArray = new Node[numNodes];
			hm = new HashMap<>();
			
			//initialization
			for(int i = 0; i < numNodes; i++)
			{
				this.nodeArray[i] = new Node(i);
			}
			
			//set Obstacles
			setObstacles();
			
			//build edge-table
			for(int i = 0; i < nodeArray.length; i++)
			{
				ArrayList<Connection> temp = new ArrayList<>();
				
				if(i == nodeArray.length - 1)
					continue;
				
				if(!nodeArray[i].getObstacle())
				{
					Connection c = new Connection();
					if(i % 100 == 99)
					{
						if(!nodeArray[i + 100].getObstacle())
						{
							c.setCost(10);
							c.setFromNode(i);
							c.setToNode(i + 100);
							temp.add(c);
						}
					}
					else if(i / 100 == 79)
					{
						if(!nodeArray[i + 1].getObstacle())
						{
							c.setCost(10);
							c.setFromNode(i);
							c.setToNode(i + 1);
							temp.add(c);
						}
					}
					else
					{
						if(!nodeArray[i + 1].getObstacle())
						{
							c.setCost(10);
							c.setFromNode(i);
							c.setToNode(i + 1);
							temp.add(c);
						}
						
						if(!nodeArray[i + 101].getObstacle())
						{
							c = new Connection();
							c.setCost(14);
							c.setFromNode(i);
							c.setToNode(i + 101);
							temp.add(c);
						}
						
						if(!nodeArray[i + 100].getObstacle())
						{
							c = new Connection();
							c.setCost(10);
							c.setFromNode(i);
							c.setToNode(i + 100);
							temp.add(c);
						}
					}
					
					hm.put(i, temp);
				}
			}
			
		}
		
		void setObstacles() //to be changed
		{
			try
			{
				FileReader fileReader = new FileReader("obstaclesSecond.txt");
				BufferedReader br = new BufferedReader(fileReader);
				
				String line = null;
				
				while((line = br.readLine()) != null)
				{
					this.nodeArray[Integer.parseInt(line)].setObstacle(true);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		Connection[] getConnections(int fromNode)
		{
			ArrayList<Connection> output = hm.get(fromNode);
			Connection[] result = new Connection[output.size()];
			result = output.toArray(result);
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
		boolean obstacle;
		
		Node(int ident)
		{
			this.ID = ident;
			this.parent = new Connection();
			this.costSoFar = 0;
			this.estimatedTotalCost = 0;
			this.category = 0;
			this.obstacle = false;
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
		
		boolean getObstacle()
		{
			return this.obstacle;
		}
		
		void setObstacle(boolean flag)
		{
			this.obstacle = flag;
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
		int currentY = node / 100;
		int currentX = node % 100;
		
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
					if(g.nodeArray[endNode].getCSF() <= endNodeCost)
						continue;
					
					g.nodeArray[endNode].setCategory(0);
					endNodeHeuristic = g.nodeArray[endNode].getETC() - g.nodeArray[endNode].getCSF();
				}
				else if(nodeFinder == 1)
				{
					if(g.nodeArray[endNode].getCSF() <= endNodeCost)
						continue;
					
					endNodeHeuristic = g.nodeArray[endNode].getETC() - g.nodeArray[endNode].getCSF();
				}
				else
				{
					endNodeHeuristic = heuristic(endNode);
				}
				
				g.nodeArray[endNode].setCSF(endNodeCost);
				g.nodeArray[endNode].setParent(c);
				g.nodeArray[endNode].setETC(endNodeCost + endNodeHeuristic);
				
				g.nodeArray[endNode].setCategory(1);
				openCount++;
				visited++;
			}
			
			previous = current.ID;
			
			//System.out.println("before Closing " + current.ID);
			g.nodeArray[current.ID].setCategory(-1);
			openCount--;
		}
		
		if(current.ID != goal)
		{
			return new Connection[0];
		}
		else
		{
			while(current.ID != start)
			{
				Connection c = current.getParent();
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
	
	Connection[] dijkstraPathfinder(Graph g, int start, int goal)
	{
		ArrayList<Connection> path = new ArrayList<>();
		
		//code for A*
			
		//g.nodeArray[start].setETC(heuristic(start));
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
			
			Connection[] connections = g.getConnections(current.ID);
			
			for(Connection c: connections)
			{
				int endNode = c.getToNode();
				
				//System.out.println("exploring " + endNode);
				int endNodeCost = current.costSoFar + c.getCost();
				//int endNodeHeuristic = 0;
				
				int nodeFinder = g.nodeArray[endNode].getCategory();
				if(nodeFinder == -1)
					continue;
				
				else if(nodeFinder == 1)
				{
					if(g.nodeArray[endNode].getCSF() <= endNodeCost)
						continue;
					
				}
				
				
				g.nodeArray[endNode].setCSF(endNodeCost);
				g.nodeArray[endNode].setParent(c);
				g.nodeArray[endNode].setETC(endNodeCost + 0);
				
				g.nodeArray[endNode].setCategory(1);
				openCount++;
				visited++;
			}
			
			previous = current.ID;
			//System.out.println("before Closing " + current.ID);
			g.nodeArray[current.ID].setCategory(-1);
			openCount--;
		}
		
		if(current.ID != goal)
		{
			return new Connection[0];
		}
		else
		{
			while(current.ID != start)
			{
				Connection c = current.getParent();
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
	ArrayList<Integer> persistent = new ArrayList<>();
	ArrayList<Integer> persistentPath = new ArrayList<>();
	boolean clickFlag = false;
	
	public void settings()
	{
		size(1000, 800);	
	}
	
	public void mouseClicked()
	{
		if(mouseX <= width && mouseY <= height)
			clickFlag = true;
	}
		
	public void setup()
	{
		smooth();
		background(255, 255, 255);
		
		//Graph g;
		g = new Graph();
		
		start = 0;
		
		goal = 6080;
		
		targetY = goal / 100;
		targetX = goal % 100;
		
		long startT = System.nanoTime();
		//path = aStarPathfinder(g, start, goal);
		path = dijkstraPathfinder(g, start, goal);
		long endT = System.nanoTime();
		long duration = endT - startT;
		
		System.out.println(duration);
		System.out.println("visited: " + visited);
		
	}
	
	public void drawTilesAndObstacles()
	{
		//draw tiles
		for(int i = 0; i < numNodes; i++)
		{
			int tileX = i % 100;
			int tileY = i / 100;
			
			pushMatrix();
			translate(tileX * tileSize, tileY * tileSize);
			if(g.nodeArray[i].getObstacle())
			{
				//System.out.println("black tiles");
				fill(0,0,0);
			}
			//System.out.println("black tiles");
			rect(0, 0, tileSize, tileSize);
			fill(255, 255, 255);
			popMatrix();
		}
		
		
		//draw persistent tiles and path
		if(persistent.size() > 0)
		{
			for(int i = 0; i < persistent.size(); i++)
			{
				int tileX = persistent.get(i) % 100;
				int tileY = persistent.get(i) / 100;
				pushMatrix();
				translate(tileX * tileSize, tileY * tileSize);
				fill(255, 0, 255);
				rect(0, 0, tileSize, tileSize);
				popMatrix();
				fill(255);
			}
		}
		
		if(persistentPath.size() > 0)
		{
			for(int i = 0; i < persistentPath.size(); i++)
			{
				int tileX = persistentPath.get(i) % 100;
				int tileY = persistentPath.get(i) / 100;
				pushMatrix();
				translate(tileX * tileSize, tileY * tileSize);
				fill(255, 0, 0);
				rect(0, 0, tileSize, tileSize);
				popMatrix();
				fill(255);
			}
		}
		
		//draw Start and Goal
		fill(0, 255, 0);
		rect(0, 0, tileSize, tileSize);
		fill(0, 0, 255);
		pushMatrix();
		translate((goal % 100)*tileSize,(goal / 100)*tileSize);
		rect(0, 0, tileSize, tileSize);
		popMatrix();
		//fill(255);
		
		
	}
	
	public void explore(int i)
	{
		Connection c = path[i];
		persistentPath.add(c.getFromNode());
		Connection[] stage = g.getConnections(c.getFromNode());
		for(Connection x: stage)
		{
			persistent.add(x.getToNode());
		}
	}
	
	
	public void draw()
	{
		rect(0, 0, width, height); //blank canvas
		drawTilesAndObstacles(); //fill tiles
		int cycle = 1 * (int)frameRate;
		if(frameCount % cycle == 0 && clickFlag)
		{
			if(pathCounter < path.length)
			{
				explore(pathCounter);
				pathCounter++;
			}
		}
		
	}

}
