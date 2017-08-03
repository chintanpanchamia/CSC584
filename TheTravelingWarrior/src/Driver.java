//REFERENCE: https://github.com/debalin/project-lalaland

import processing.core.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;




public class Driver extends PApplet
{

	private static final PVector RESOLUTION = new PVector(1000, 800);
	private static final int FRAME_RATE = 55;
	private static final int SMOOTH_FACTOR = 4;
	private static final int BONUS_DROP_INTERVAL = 800;
	private static final PVector BACKGROUND_RGB = new PVector(255, 255, 255);
	private static final PVector PLAYER_INITIAL_POSITION = new PVector(RESOLUTION.x / 2 - 100, RESOLUTION.y / 2 - 100);
	private static final PVector NUM_TILES = new PVector(100, 80);

	private Level level;
	private Hero hero;

	private List<Monster> enemies;
	private List<BonusItem> bonusItems;
	private boolean inDEVMode = false;

	private static float time = 0f;

	public static float getTime()
	{
		return time;
	}

	public static PVector getResolution()
	{
		return RESOLUTION;
	}

	public void settings()
	{
		size((int) RESOLUTION.x, (int) RESOLUTION.y, P3D);
		smooth(SMOOTH_FACTOR);
		initializeEnemySpawnDetails();
	}

	public List<Monster> getEnemies()
	{
		return enemies;
	}

	private void initializeEnemySpawnDetails()
	{
		Monster_Soldier.initializeSpawnDetails(FRAME_RATE);
	}

	public void setup()
	{
		noStroke();
		frameRate(FRAME_RATE);
		level = new Level(this, RESOLUTION, NUM_TILES);

		hero = new Hero(PLAYER_INITIAL_POSITION.x, PLAYER_INITIAL_POSITION.y, this, level);
		level.setPlayer(hero);
		enemies = new LinkedList<>();
		level.setEnemies(enemies);

		bonusItems = new LinkedList<>();
		level.setBonusItems(bonusItems);
	}

	public static void main(String args[])
	{
		PApplet.main("Driver");
	}

	public void draw()
	{
		time = millis();

		background(BACKGROUND_RGB.x, BACKGROUND_RGB.y, BACKGROUND_RGB.z);
		level.drawObstacles();
		controlItems();
		if (hero.isAlive())
			spawnBonusItems();
		controlEnemies();
		controlPlayer();
		controlHUD();
	}
	
	public void mousePressed()
	{
		PVector spawnSpot = new PVector(mouseX, mouseY);
		if(level.getHero().goalExists)
		{
			if(!level.onObstacle(spawnSpot))
				enemies.add(new Monster_Soldier(spawnSpot.x, spawnSpot.y, this, level));
		}
		else
		{
			int destinationX = (int) (spawnSpot.x / level.getTileSize().x);
			int destinationY = (int) (spawnSpot.y / level.getTileSize().y);
			level.getHero().destination = destinationY * (int) level.getNumTiles().x + destinationX;
			
			int originX = (int) (level.getHero().position.x / level.getTileSize().x);
			int originY = (int) (level.getHero().position.y / level.getTileSize().y);
			int originNode = originY * (int) level.getNumTiles().x + originX;
			
			level.getHero().solutionPath = null;
			//level.getHero().targetPosition = new PVector();
			if(!level.onObstacle(spawnSpot))
			{
				if (level.getHero().graphSearch.search(originNode, level.getHero().destination, level.getHero().searchType))
				{
					level.getHero().solutionPath = level.getHero().graphSearch.getSolutionPath();
					//System.out.println(level.getHero().solutionPath.toString());
				}
				level.getHero().graphSearch.reset();
			}
			level.getHero().goalExists = true;
			
			//level.getHero().updateState(Hero.States.FOLLOW);
			}
	}

	private void controlPlayer()
	{
		if (hero.isAlive())
			hero.move();
		else
			Utility.drawText("PLAYER 1 Dead", RESOLUTION.x / 2f - 15f, RESOLUTION.y / 2f - 15f, this);
		hero.display();
		//controlPlayerGun();
	}


	private void controlEnemies()
	{
		if (!inDEVMode)
			spawnEnemies();
		Iterator<Monster> i = enemies.iterator();
		while (i.hasNext())
		{
			Monster monster = i.next();
			if (monster.isAlive())
			{
				if (hero.isAlive())
					monster.move();
				monster.display();
			} else
			{
				i.remove();
			}
		}
	}

	private void spawnEnemies()
	{
		for (Monster.MonsterTypes enemyType : Monster.MonsterTypes.values())
		{
			switch (enemyType)
			{
				case SOLDIER:
					if ((Monster_Soldier.SPAWN_OFFSET <= frameCount)
							&& ((frameCount - Monster_Soldier.SPAWN_OFFSET) % Monster_Soldier.SPAWN_INTERVAL == 0)
							&& (Monster_Soldier.getSpawnCount() < Monster_Soldier.SPAWN_MAX))
					{
						spawnEnemyNow(Monster.MonsterTypes.SOLDIER);
					}
					break;
			}
		}
	}

	private void spawnEnemyNow(Monster.MonsterTypes enemytype)
	{
		PVector spawnSpot;
		switch (enemytype)
		{
			case SOLDIER:
				spawnSpot = getRandomSpawnSpot();
				enemies.add(new Monster_Soldier(spawnSpot.x, spawnSpot.y, this, level));
				break;
		}
	}

	private PVector getRandomSpawnSpot()
	{
		PVector randomSpawnSpot = new PVector();

		float random = random(1, 100);
		if (random < 50)
			randomSpawnSpot.x = random + 3 * Monster.BORDER_PADDING;
		else
			randomSpawnSpot.x = RESOLUTION.x - 3 * Monster.BORDER_PADDING - (random - 50);

		random = random(1, 100);
		if (random < 50)
			randomSpawnSpot.y = random + 3 * Monster.BORDER_PADDING;
		else
			randomSpawnSpot.y = RESOLUTION.y - 3 * Monster.BORDER_PADDING - (random - 50);

		return randomSpawnSpot;
	}

	private void controlItems()
	{
		Iterator<BonusItem> i = bonusItems.iterator();
		while (i.hasNext())
		{
			BonusItem item = i.next();
			if (!item.isConsumed())
				item.display();
			else
				i.remove();
		}
	}

	private void controlHUD()
	{
		Utility.drawText("HP DAMAGE: " + Monster.getTotalHPDamage(), width - 120, 30, this);
	}

	private void spawnBonusItems()
	{
		if (frameCount % BONUS_DROP_INTERVAL == 0)
		{
			PVector position = level.getRandomValidPosition();
			double rand = Math.random();
			boolean radialBullets = false;
			if (rand > 0.5)
				radialBullets = true;
			bonusItems.add(new BonusItem(position.x, position.y, this, level, radialBullets));
		}
	}

	public void keyPressed()
	{
		hero.setDirection(key, true);
	}

	public void keyReleased()
	{
		hero.setDirection(key, false);
		if (inDEVMode)
			handleEnemySpawn();
	}

	private void handleEnemySpawn()
	{
		switch (key)
		{
			case '1':
				spawnEnemyNow(Monster.MonsterTypes.SOLDIER);
				break;
			// case '2':
			// spawnEnemyNow(Monster.MonsterTypes.HERMIT);
			// break;
			// case '3':
			// spawnEnemyNow(Monster.MonsterTypes.GRUNT);
			// break;
			// case '4':
			// spawnEnemyNow(Monster.MonsterTypes.MARTYR);
			// break;
			// case '5':
			// spawnEnemyNow(Monster.MonsterTypes.FLOCKER);
			// break;
			// case '6':
			// spawnEnemyNow(Monster.MonsterTypes.BLENDER);
			// break;
		}
	}

}
