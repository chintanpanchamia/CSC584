

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import processing.core.PApplet;


public class Graph
{
	Node[] nodeArray;
	HashMap<Integer, ArrayList<Connection>> hm;
	boolean[][] assister;
	HashSet<Integer> obstacles;
	int numNodes;
	int tileSize;
	PApplet pr;
	Graph(PApplet parent)
	{

		//this.readFromFile("sg.txt"); //this method isn't required now
		this.pr = parent;
		this.hm = new HashMap<>();
		this.tileSize = 100;
		this.numNodes = pr.width * pr.height / (this.tileSize *  this.tileSize);
		nodeArray = new Node[numNodes];
		//hm = new HashMap<>();
		assister = new boolean[pr.height/tileSize][pr.width/tileSize];
		
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
		int obstacleArray[] = {3, 4, 5, 6, 9, 13, 19, 29, 40, 41, 42, 45, 48, 49, 55, 65, 75};
		this.obstacles = new HashSet<>();

		for(int x: obstacleArray)
		{
			
			//this.nodeArray[x].setObstacle(true);
			this.assister[x/10][x%10] = true;
			this.obstacles.add(x);
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

	
	Connection[] aStarPathfinder(int start, int goal)
	{
		ArrayList<Connection> path = new ArrayList<>();

		//code for A*

		this.nodeArray[start].setETC(this.heuristic(start, goal));
		this.nodeArray[start].setCategory(1);
		int openCount = 1;
		int closeCount = 0;
		
		Node current = new Node(0);
		int previous = 0;
		while(openCount > 0)
		{
			current = new Node(this.smallestOpen());
			//System.out.println("up " + current.ID);

			if(current.ID == goal)
			{
				Connection finalEdge = new Connection();
				finalEdge.setFromNode(previous);
				finalEdge.setToNode(current.ID);
				current.setParent(finalEdge);
				break;
			}

			//System.out.println(current.ID);
			Connection[] connections = this.getConnections(current.ID);

			for(Connection c: connections)
			{
				int endNode = c.getToNode();

				//System.out.println("exploring " + endNode);
				int endNodeCost = current.costSoFar + c.getCost();
				int endNodeHeuristic = 0;

				int nodeFinder = this.nodeArray[endNode].getCategory();
				if(nodeFinder == -1)
				{
					continue;
				}
				else if(nodeFinder == 1)
				{
					//System.out.println("current: " + endNodeCost + " Old: " + g.nodeArray[endNode].getCSF());
					if(this.nodeArray[endNode].getCSF() <= endNodeCost)
						continue;

					endNodeHeuristic = this.nodeArray[endNode].getETC() - this.nodeArray[endNode].getCSF();
				}
				else
				{
					endNodeHeuristic = this.heuristic(endNode, goal);
				}

				this.nodeArray[endNode].setCSF(endNodeCost);
				//System.out.println("CSF for: " + endNode + "is" + g.nodeArray[endNode].getCSF());

				this.nodeArray[endNode].setParent(c);
				//System.out.println("parent of " + endNode + " is " + current.ID);
				this.nodeArray[endNode].setETC(endNodeCost + endNodeHeuristic);
				//System.out.println("ETC of " + endNode + " is " + g.nodeArray[endNode].getETC());

				this.nodeArray[endNode].setCategory(1);
				openCount++;
				
				//System.out.print(endNode + " ");
			}

			previous = current.ID;
			//System.out.println("closed: " + previous);
			//System.out.println("before Closing " + current.ID);
			this.nodeArray[current.ID].setCategory(-1);
			openCount--;
		}

		if(current.ID != goal)
		{
			//System.out.println("hey");
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
				current = this.nodeArray[parent];
			}
		}

		Connection[] output = new Connection[path.size()];
		Collections.reverse(path);
		output = path.toArray(output);

		return output;
	}
	
	int heuristic(int start, int goal)
	{
		int currentY = start / 10;
		int currentX = start % 10;
		
		int targetY = goal / 10;
		int targetX = goal % 10;

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