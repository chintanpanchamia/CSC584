//REFERENCE: https://github.com/debalin/project-lalaland

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Wander
{

	private static Kinematic target;
	private static float time;
	private static float TIME_TO_TARGET_ROTATION = 30;

	private int randomiserCounter;
	private int randomiserLimit;
	private float targetOrientation;

	static
	{
		time = -2001;
		target = new Kinematic();
	}

	public Wander(int randomiserLimit)
	{
		randomiserCounter = 0;
		this.randomiserLimit = randomiserLimit;
		targetOrientation = 0f;
	}

	public static SteeringOutput getPositionMatchingSteering(Kinematic character, float maxLinearAcc,
			float maxAngularAcc, float timeToTarget, float ros)
	{
		float mainTime = Driver.getTime();
		SteeringOutput steering;

		if ((mainTime - time) > Math.random() * 2000)
		{
			time += 2000;
			target.position.x = (float) (Math.random() * Driver.getResolution().x);
			target.position.y = (float) (Math.random() * Driver.getResolution().y);
		}

		steering = Face.getSteering(character, target, timeToTarget, ros);
		steering.linear = asVector(character.orientation);
		steering.linear.setMag(maxLinearAcc);
		if (steering.angular > maxAngularAcc)
			steering.angular = maxAngularAcc;

		return steering;
	}

	public KinematicOutput getOrientationMatchingSteering(Kinematic character, Level level, PApplet parent,
			int BORDER_PADDING, float MAX_VELOCITY, float rayOffset)
	{
		KinematicOutput kinematicOutput = new KinematicOutput();
		randomiserCounter++;
		if (randomiserCounter == randomiserLimit)
		{
			targetOrientation = Utility.randomBinomial() * PConstants.PI + character.orientation;
			randomiserCounter = 0;
		}

		boolean onObstacle = ObstacleSteering.checkForObstacleAvoidance(character, parent, level, rayOffset);
		if (onObstacle)
		{
			targetOrientation = ObstacleSteering.avoidObstacleOnWander(character, parent, level, rayOffset);
		} else if (BoundarySteering.checkForBoundaryAvoidance(character, parent, BORDER_PADDING))
		{
			targetOrientation = BoundarySteering.avoidBoundaryOnWander(character, parent, BORDER_PADDING);
		}
		kinematicOutput.rotation = rotateShapeDirection(character, targetOrientation);
		kinematicOutput.velocity = calculateVelocityPerOrientation(character, MAX_VELOCITY);
		return kinematicOutput;
	}

	public KinematicOutput getOrientationMatchingSteering(Kinematic character, Level level, PApplet parent,
			int BORDER_PADDING, float MAX_VELOCITY, PVector desiredLocation, float randomAngle, float rayOffset)
	{
		KinematicOutput kinematicOutput = new KinematicOutput();

		boolean onObstacle = ObstacleSteering.checkForObstacleAvoidance(character, parent, level, rayOffset);
		if (onObstacle)
		{
			targetOrientation = ObstacleSteering.avoidObstacleOnWander(character, parent, level, rayOffset);
		} else if (BoundarySteering.checkForBoundaryAvoidance(character, parent, BORDER_PADDING))
		{
			targetOrientation = BoundarySteering.avoidBoundaryOnWander(character, parent, BORDER_PADDING);
		} else
		{
			targetOrientation = PVector.sub(desiredLocation, character.position).heading() + randomAngle;
		}
		kinematicOutput.rotation = rotateShapeDirection(character, targetOrientation);
		kinematicOutput.velocity = calculateVelocityPerOrientation(character, MAX_VELOCITY);
		return kinematicOutput;
	}

	protected float rotateShapeDirection(Kinematic character, float angle)
	{
		angle = (angle - character.orientation) / TIME_TO_TARGET_ROTATION;
		return angle;
	}

	protected PVector calculateVelocityPerOrientation(Kinematic character, float MAX_VELOCITY)
	{
		PVector velocity = PVector.fromAngle(character.orientation);
		velocity.setMag(MAX_VELOCITY);
		return velocity;
	}

	private static PVector asVector(float orientation)
	{
		PVector v = new PVector();
		v.y = PApplet.sin(orientation);
		v.x = PApplet.cos(orientation);
		return v;
	}

}