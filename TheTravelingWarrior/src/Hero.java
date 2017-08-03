import processing.core.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//REFERENCE: https://github.com/debalin/project-lalaland


public class Hero extends GameObject
{

	private static final float PLAYER_RADIUS = 7;
	private static int GUN_FIRE_INTERVAL = 10;
	private static int BONUS_TIMEOUT_DURATION = 400;
	private static int gun_reset_framecount = 0;
	private static final PVector PLAYER_COLOR = new PVector(41, 242, 138);
	protected GraphSearch graphSearch;
	protected PVector targetPosition;
	protected LinkedList<Integer> solutionPath;
	private Wander wander;
	private static final int RANDOMISER_INTERVAL = 80;
	protected boolean reached, goalExists;
	protected static final GraphSearch.SearchType searchType = GraphSearch.SearchType.ASTAR;
	protected int destination;
	protected float MAX_ACCELERATION;
	protected PVector acceleration;
	protected float RADIUS_SATISFACTION;
	
	protected enum States
	{
		FOLLOW, ENGAGE, BALLISTIC
	}
	
	protected States state;
	private boolean LEFT, RIGHT, UP, DOWN;
	private List<Bullet> bullets;
	private boolean alive;
	private boolean isradialBulletPowerup = false;

	public Hero(float positionX, float positionY, PApplet parent, Level level)
	{
		super(positionX, positionY, parent, level, PLAYER_RADIUS, PLAYER_COLOR);
		DRAW_BREADCRUMBS = false;
		TIME_TARGET_ROT = 7;
		MAX_VELOCITY = 0.8f;
		MAX_ACCELERATION = 0.3f;
		RADIUS_SATISFACTION = 5;
		LEFT = RIGHT = UP = DOWN = false;
		bullets = Collections.synchronizedList(new LinkedList<>());
		alive = true;
		MAX_LIFE = 100;
		life = MAX_LIFE;
		graphSearch = level.getNewGraphSearch();
		wander = new Wander(RANDOMISER_INTERVAL);
		state = States.ENGAGE;
		goalExists = false;
		targetPosition = new PVector();
		acceleration = new PVector();
		solutionPath = new LinkedList<>();
		reached = true;
	}

	public PVector getVelocity()
	{
		return velocity;
	}


	
	@Override
	public void move()
	{
		switch(state)
		{
			case FOLLOW:
			{
				if(enemiesCloseBy())
					attackClosest(state);
				else
					orientation = (float) Math.atan2(targetPosition.y - position.y, targetPosition.x - position.x);
				
				if(goalExists)
				{
					followPath();
				}
				else
					this.updateState(States.ENGAGE);
				break;
			}
			
			case ENGAGE:
			{
				//wander
				if(!goalExists)
				{
					updatePositionWander();
					if(enemyInPerception()) 
							attackClosest(state);
				}
				else if(goalExists)
				{
					this.updateState(States.FOLLOW);
					this.followPath();
				}
				
				if(tooClose())
				{
					this.updateState(States.BALLISTIC);
				}
				
				break;
			}
			
			case BALLISTIC:
			{
				if(!tooClose())
					this.updateState(States.ENGAGE);
				else
					shootRadialBullets();
				break;
			}
		}
		
		this.controlBullets();
		this.updatePosition();
	}
	
	private boolean enemiesCloseBy()
	{
		List<Monster> enemies = level.getEnemies();
		for(Monster m: enemies)
		{
			if(PVector.dist(this.position, m.position) <= 10 * PLAYER_RADIUS)
				return true;
		}
		return false;
	}
	
	private boolean enemyInPerception()
	{
		List<Monster> enemies = level.getEnemies();
		for(Monster m: enemies)
		{
			if(PVector.dist(this.position, m.position) <= 30 * PLAYER_RADIUS)
				return true;
		}
		return false;
	}
	
	private boolean tooClose()
	{
		List<Monster> enemies = level.getEnemies();
		int counter = 0;
		for(Monster m: enemies)
		{
			if(PVector.dist(this.position, m.position) <= 7.5 * PLAYER_RADIUS)
				counter++;
			if(counter >= 2)
				return true;
		}
		return false;
	}
	
	private void attackClosest(States state)
	{
		int temp = 0;
		if(state == States.ENGAGE)
			temp = 30;
		else if(state == States.FOLLOW)
			temp = 10;
		List<Monster> enemies = level.getEnemies();
		Monster closestEnemy = null;
		float minDist = 99999;
		for(Monster m: enemies)
		{
			float dist = PVector.dist(this.position, m.position);
			if(dist <= temp * PLAYER_RADIUS)
			{
				if(dist < minDist)
				{
					minDist = dist;
					closestEnemy = m;
				}
			}
		}
		
		
		SteeringOutput steering = new SteeringOutput();
		Kinematic h = new Kinematic(this.position, this.velocity, this.orientation, this.rotation);
		Kinematic m = new Kinematic(closestEnemy.position/*.add(closestEnemy.velocity.mult(0.2f))*/, closestEnemy.velocity, closestEnemy.orientation, closestEnemy.rotation);
		steering = Align.getSteering(m, h, 4);
		this.orientation += steering.angular;
		
		shootBullet();
	}
	
	private void updatePosition()
	{
		//System.out.println(this.state);
		this.position.add(this.velocity);
		
		Kinematic target = new Kinematic(this.targetPosition, null, 0, 0);
		//System.out.println(targetPosition);
		
		SteeringOutput steering = new SteeringOutput();
		
		if(state == States.ENGAGE || state == States.BALLISTIC)
		{
			if(state == States.BALLISTIC)
				this.orientation += PApplet.radians(30);
			updatePositionWander();
		}
		else
		{
			steering = Seek.getSteering(this, target, MAX_ACCELERATION, RADIUS_SATISFACTION);
			steering.linear.setMag(MAX_ACCELERATION);
			if (steering.linear.mag() == 0)
			{
				velocity.set(0, 0);
				acceleration.set(0, 0);
				reached = true;
				return;
			}
			reached = false;
			velocity.add(steering.linear);
			if (velocity.mag() >= MAX_VELOCITY)
				velocity.setMag(MAX_VELOCITY);
		}
		
	}
	
	private void followPath()
	{
		//System.out.println(reached);
		if (solutionPath != null && solutionPath.size() != 0 && reached)
		{
			int node = solutionPath.poll();
			if(solutionPath.size() > 0)
				node = solutionPath.poll();
			if(solutionPath.size() > 0)
				node = solutionPath.poll();
			//System.out.println(node);
			int gridY = (int) (node / level.getNumTiles().x);
			int gridX = (int) (node % level.getNumTiles().x);
			targetPosition.x = gridX * level.getTileSize().x + level.getTileSize().x / 2;
			targetPosition.y = gridY * level.getTileSize().y + level.getTileSize().y / 2;
			
		} 
		else if (solutionPath == null || solutionPath.size() == 0)
		{
			this.goalExists = false;
			updateState(States.ENGAGE);
			
		}
	}
	
	protected void updateState(Hero.States state)
	{
		this.state = state;
	}
	
	private void updatePositionWander()
	{
		KinematicOutput kinematic = wander.getOrientationMatchingSteering(this, level, parent, 20,
				MAX_VELOCITY/2, 2.5f);
		orientation += kinematic.rotation;
		velocity.set(kinematic.velocity);
		position.add(velocity);
	}

	public void reduceLife(float damage)
	{
		if (life <= 0)
		{
			System.out.println("Player dead.");
			level.getEnemies().forEach(enemy -> enemy.printCommonMetrics());
			alive = false;
		} else
			life -= damage;
	}

	public boolean isAlive()
	{
		return alive;
	}

	private void controlBonusItemPicking()
	{
		BonusItem item = level.onBonusItem(position);
		if (item != null)
		{
			if (item.isRadialBullet())
			{
				isradialBulletPowerup = true;
			} else
			{
				setGUN_FIRE_INTERVAL(4);
			}
			gun_reset_framecount = parent.frameCount + BONUS_TIMEOUT_DURATION;
		}
		handleGunReset();
	}

	private void handleGunReset()
	{
		if (parent.frameCount == gun_reset_framecount)
		{
			if (isradialBulletPowerup)
				isradialBulletPowerup = false;
			// else {
			setGUN_FIRE_INTERVAL(10);
			// }
		}
	}

	private void controlBullets()
	{
		synchronized (bullets)
		{
			Iterator<Bullet> i = bullets.iterator();
			while (i.hasNext())
			{
				Bullet bullet = i.next();
				if (!level.outOfBounds(bullet.getPosition(), 0) && !level.onObstacle(bullet.getPosition()))
				{
					bullet.move();
					bullet.display();
				} else
				{
					i.remove();
				}
			}
		}
		Logger.log("Number of bullets = " + getBullets().size());
	}

	public void setDirection(int key, boolean set)
	{
		switch (key)
		{
			case 'W':
			case 'w':
				UP = set;
				break;
			case 'S':
			case 's':
				DOWN = set;
				break;
			case 'A':
			case 'a':
				LEFT = set;
				break;
			case 'D':
			case 'd':
				RIGHT = set;
				break;
		}
	}

	public void shootBullet()
	{
		if (isradialBulletPowerup)
		{
			shootRadialBullets();
		} else
		{
			getBullets().add(new Bullet(position.x, position.y, orientation, parent, 3, new PVector(255, 0, 0)));
		}
	}

	public void shootRadialBullets()
	{
		int RADIAL_NUM = 5;
		float orient;
		for (int i = 0; i < RADIAL_NUM; i++)
		{
			orient = orientation + i * PConstants.TWO_PI / RADIAL_NUM;
			bullets.add(new Bullet(position.x, position.y, orient, parent, 3, new PVector(255, 0, 0)));
		}

	}

	public List<Bullet> getBullets()
	{
		return bullets;
	}

	public static int getGUN_FIRE_INTERVAL()
	{
		return GUN_FIRE_INTERVAL;
	}

	public static void setGUN_FIRE_INTERVAL(int gUN_FIRE_INTERVAL)
	{
		GUN_FIRE_INTERVAL = gUN_FIRE_INTERVAL;
	}
}