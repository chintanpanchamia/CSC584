//REFERENCE: https://github.com/debalin/project-lalaland

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import processing.core.*;
public class Level 
{

	private PApplet parent;
	private int width, height;
	private List<Obstacle> obstacles;
	private Set<PVector> invalidNodes;
	private PVector tileSize, numTiles;
	private Map<Integer, List<Utility.Neighbour>> adjacencyList;
	private Utility utility;
	private Hero hero;
	private List<Monster> enemies;
	private List<BonusItem> bonusItems;

	public Map<Integer, List<Utility.Neighbour>> getAdjacencyList() 
	{
		return adjacencyList;
	}

	public Utility getUtility() 
	{
		return utility;
	}

	public PVector getNumTiles() 
	{
		return numTiles;
	}

	public PVector getTileSize() 
	{
		return tileSize;
	}

	public List<Monster> getEnemies() 
	{
		return enemies;
	}

	public void setEnemies(List<Monster> enemies) 
	{
		this.enemies = enemies;
	}

	public Level(PApplet parent, PVector resolution, PVector numTiles) 
	{
		this.parent = parent;
		this.width = (int)resolution.x;
		this.height = (int)resolution.y;
		this.numTiles = numTiles;

		obstacles = new ArrayList<>();
		invalidNodes = new HashSet<>();
		utility = new Utility();
		makeTiles((int)numTiles.x, (int)numTiles.y);
		createObstacles();
		buildGraph();
	}

	private void makeTiles(int numTilesX, int numTilesY) 
	{
		numTiles = new PVector(numTilesX, numTilesY);
		tileSize = new PVector(width / numTiles.x, height / numTiles.y);
		//System.out.println("numTiles = " + numTiles + " tileSize = " + tileSize);
		
	}

	public void createObstacles() 
	{
		PVector obstacleColor = new PVector(60, 60, 60);

		obstacles.add(new Obstacle(8, 8, 12, 12, this, obstacleColor));
		obstacles.add(new Obstacle(88, 8, 92, 12, this, obstacleColor));
		
		obstacles.add(new Obstacle(8, 68, 12, 72, this, obstacleColor));
		obstacles.add(new Obstacle(88, 68, 92, 72, this, obstacleColor));
		
		obstacles.add(new Obstacle(49, 39, 51, 41, this, obstacleColor));
		
		obstacles.add(new Obstacle(20, 20, 35, 25, this, obstacleColor));
		obstacles.add(new Obstacle(20, 25, 25, 35, this, obstacleColor));
		
		obstacles.add(new Obstacle(65, 20, 80, 25, this, obstacleColor));
		obstacles.add(new Obstacle(75, 25, 80, 35, this, obstacleColor));
		
		obstacles.add(new Obstacle(20, 45, 25, 55, this, obstacleColor));
		obstacles.add(new Obstacle(20, 55, 35, 60, this, obstacleColor));
		
		obstacles.add(new Obstacle(75, 45, 80, 55, this, obstacleColor));
		obstacles.add(new Obstacle(65, 55, 80, 60, this, obstacleColor));
		
		
		formInvalidNodes();
	}

	public void drawObstacles() 
	{
		parent.pushMatrix();
		for (Obstacle obstacle : obstacles) 
		{
			PVector corner = obstacle.getCorner();
			PVector size = obstacle.getSize();
			PVector obstacleColor = obstacle.getObstacleColor();
			parent.fill(obstacleColor.x, obstacleColor.y, obstacleColor.z);
			parent.rect(corner.x, corner.y, size.x, size.y);
		}
		parent.popMatrix();
	}

	private void formInvalidNodes() {
		for (Obstacle obstacle : obstacles) 
		{
			for (PVector tileLocation : obstacle.getTileLocations()) 
			{
				invalidNodes.add(tileLocation);
				invalidNodes.add(new PVector(tileLocation.x + 1, tileLocation.y));
				invalidNodes.add(new PVector(tileLocation.x, tileLocation.y + 1));
				invalidNodes.add(new PVector(tileLocation.x - 1, tileLocation.y));
				invalidNodes.add(new PVector(tileLocation.x, tileLocation.y - 1));
				invalidNodes.add(new PVector(tileLocation.x + 1, tileLocation.y + 1));
				invalidNodes.add(new PVector(tileLocation.x + 1, tileLocation.y - 1));
				invalidNodes.add(new PVector(tileLocation.x - 1, tileLocation.y + 1));
				invalidNodes.add(new PVector(tileLocation.x - 1, tileLocation.y - 1));
			}
		}
	}

	public void buildGraph() 
	{
		adjacencyList = utility.buildGraph(invalidNodes, numTiles);
	}

	public boolean onObstacle(PVector position) 
	{
		int gridX = (int)(position.x / tileSize.x);
		int gridY = (int)(position.y / tileSize.y);

		return invalidNodes.contains(new PVector(gridX, gridY));
	}

	public BonusItem onBonusItem(PVector position)
	{
		Iterator<BonusItem> i = bonusItems.iterator();
		while(i.hasNext()){
			BonusItem item = i.next();
			if(Utility.calculateEuclideanDistance(position, item.getPosition()) < 12){
				item.consumeItem();
				return item;
			}
		}
		return null;
	}

	public PVector getRandomValidPosition()
	{
		float BORDER_PADDING = 8;
		PVector randPosition;
		do
		{
			float x = parent.random(BORDER_PADDING, width-BORDER_PADDING);
			float y = parent.random(BORDER_PADDING, height-BORDER_PADDING);
			randPosition = new PVector(x,y);
		}
		while(onObstacle(randPosition) || onBonusItem(randPosition) != null);
		return randPosition;
	}

	public boolean outOfBounds(PVector position, float padding) 
	{
		if (position.x >= width - padding || position.x <= padding)
			return true;
		if (position.y >= height - padding || position.y <= padding)
			return true;
		return false;
	}

	public boolean inSameGrid(PVector position1, PVector position2) 
	{
		int gridX1 = (int)(position1.x / tileSize.x);
		int gridY1 = (int)(position1.y / tileSize.y);
		int gridX2 = (int)(position2.x / tileSize.x);
		int gridY2 = (int)(position2.y / tileSize.y);

		int gridX1_LT = gridX1 - 1;
		int gridY1_LT = gridY1 - 1;
		int gridX1_RB = gridX1 + 1;
		int gridY1_RB = gridY1 + 1;

		if (gridX2 >= gridX1_LT && gridX2 <= gridX1_RB && gridY2 >= gridY1_LT && gridY2 <= gridY1_RB)
			return true;
		else
			return false;
	}

	public Obstacle getNearestObstacle(PVector position, Obstacle exceptThisObstacle) 
	{
		Obstacle nearestObstacle = null;
		float minimumDistance = 999999;
		for (Obstacle obstacle : obstacles) 
		{
			float distance = PVector.dist(obstacle.getCenterPosition(), position);
			if (distance < minimumDistance && obstacle != exceptThisObstacle) 
			{
				minimumDistance = distance;
				nearestObstacle = obstacle;
			}
		}
		return nearestObstacle;
	}

	public Obstacle getFarthestObstacle(PVector position, Obstacle exceptThisObstacle) 
	{
		Obstacle farthestObstacle = null;
		float maximumDistance = 0;
		for (Obstacle obstacle : obstacles) 
		{
			float distance = PVector.dist(obstacle.getCenterPosition(), position);
			if (distance > maximumDistance && obstacle != exceptThisObstacle) 
			{
				maximumDistance = distance;
				farthestObstacle = obstacle;
			}
		}
		return farthestObstacle;
	}

	public Hero getHero() 
	{
		return hero;
	}

	public void setPlayer(Hero player) 
	{
		this.hero = player;
	}

	public void setBonusItems(List<BonusItem> items)
	{
		this.bonusItems = items;
	}

	public GraphSearch getNewGraphSearch() 
	{
		return (new GraphSearch(this, (int)(numTiles.x * numTiles.y)));
	}
}