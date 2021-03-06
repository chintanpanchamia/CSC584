//REFERENCE: https://github.com/debalin/project-lalaland

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class ObstacleSteering 
{

  private static final float FUTURE_RAY_VEL_BASE = 15f;

  public static boolean checkForObstacleAvoidance(Kinematic character, PApplet parent, Level level, float rayOffset){
    PVector futureRay1 = PVector.add(character.position, PVector.mult(character.velocity.copy().setMag(FUTURE_RAY_VEL_BASE), rayOffset / 2f));
    PVector futureRay2 = PVector.add(character.position, PVector.mult(character.velocity.copy().setMag(FUTURE_RAY_VEL_BASE), rayOffset));
    PVector futureRay3 = PVector.add(character.position, PVector.fromAngle(character.velocity.heading() - PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * rayOffset / 2));
    PVector futureRay4 = PVector.add(character.position, PVector.fromAngle(character.velocity.heading() + PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * rayOffset / 2));

//    parent.ellipse(futureRay1.x, futureRay1.y, 2, 2);
//    parent.ellipse(futureRay2.x, futureRay2.y, 2, 2);
//    parent.ellipse(futureRay3.x, futureRay3.y, 2, 2);
//    parent.ellipse(futureRay4.x, futureRay4.y, 2, 2);

    return (
      level.onObstacle(futureRay1) ||
      level.onObstacle(futureRay2) ||
      level.onObstacle(futureRay3) ||
      level.onObstacle(futureRay4)
    );
  }

  public static boolean checkForObstacleAvoidance(Level level, List<PVector> futureRays){
    boolean onObstacle = false;
    for (PVector futureRay : futureRays) {
      if (level.onObstacle(futureRay)) {
        onObstacle = true;
        break;
      }
    }
    return onObstacle;
  }

  public static PVector avoidObstacleOnSeek(Kinematic character, Level level, float rayOffset) {
		float angle;
		int i;
		PVector direction = character.velocity.copy();
		
		for (i = 0, angle = direction.heading(); i < 16; i++, angle += PConstants.PI / 8) {
			if (angle > 2 * PConstants.PI)
				angle -= 2 * PConstants.PI;

      List<PVector> futureRays = new ArrayList<>();
			futureRays.add(PVector.add(character.position, PVector.fromAngle(angle).setMag(FUTURE_RAY_VEL_BASE * rayOffset)));
			futureRays.add(PVector.add(character.position, PVector.fromAngle(angle).setMag(FUTURE_RAY_VEL_BASE * rayOffset / 2f)));
      futureRays.add(PVector.add(character.position, PVector.fromAngle(angle - PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * rayOffset / 2f)));
      futureRays.add(PVector.add(character.position, PVector.fromAngle(angle + PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * rayOffset / 2f)));

      boolean onObstacle = false;
			for (PVector futureRay : futureRays) {
        if (level.onObstacle(futureRay)) {
          onObstacle = true;
          break;
        }
      }
      if (!onObstacle)
        break;
		}
		PVector targetPosition = PVector.add(character.position.copy(), PVector.fromAngle(angle).setMag(FUTURE_RAY_VEL_BASE * rayOffset));
		return targetPosition;
	}

  public static PVector avoidObstacleOnSeek(Kinematic character, Level level, List<PVector> futureRays, PApplet parent) {
    float angle;
    int i;

    for (i = 0, angle = 0; i < 16; i++, angle += PConstants.PI / 8) {
      if (angle > 2 * PConstants.PI)
        angle -= 2 * PConstants.PI;

      boolean onObstacle = false;
      for (PVector futureRay : futureRays) {
        futureRay.set(PVector.add(character.position, PVector.fromAngle(PVector.sub(futureRay, character.position).heading() - angle).setMag(30)));
        //parent.ellipse(futureRay.x, futureRay.y, 5, 5);
        if (level.onObstacle(futureRay)) {
          onObstacle = true;
          break;
        }
      }
      if (!onObstacle)
        break;
    }
    PVector targetPosition = PVector.add(character.position, PVector.fromAngle(angle).setMag(futureRays.get(0).mag()));
    return targetPosition;
  }
  
  public static float avoidObstacleOnWander(Kinematic character, PApplet parent, Level level, float rayOffset) {
    float orient;
    Random random = new Random();
    float targetOrientation;
    do {
      orient = random.nextInt(180) - random.nextInt(180);
      targetOrientation = PApplet.radians(orient) + character.orientation;
    } while (checkForObstacleAvoidance(new Kinematic(character.position, PVector.fromAngle(targetOrientation), 0, 0), parent, level, rayOffset));
    return targetOrientation;
  }

  public static SteeringOutput checkAndAvoidObstacle(Kinematic character, Level level, float steeringWeight, float rayOffset) {
    SteeringOutput steeringOutput = new SteeringOutput();

    List<PVector> futureRays = new ArrayList<>();
    futureRays.add(PVector.add(character.position, PVector.mult(character.velocity.copy().setMag(FUTURE_RAY_VEL_BASE), rayOffset)));
    futureRays.add(PVector.add(character.position, PVector.mult(character.velocity.copy().setMag(FUTURE_RAY_VEL_BASE), rayOffset / 2f)));
    futureRays.add(PVector.add(character.position, PVector.fromAngle(character.velocity.heading() - PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * rayOffset / 2f)));
    futureRays.add(PVector.add(character.position, PVector.fromAngle(character.velocity.heading() + PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * rayOffset / 2f)));

    for (PVector futureRay : futureRays) {
      if (level.onObstacle(futureRay)) {
        Obstacle nearestObstacle = level.getNearestObstacle(futureRay, null);
        PVector left, right, up, down;
        left = new PVector(nearestObstacle.getCenterPosition().x - nearestObstacle.getSize().x / 2, nearestObstacle.getCenterPosition().y);
        right = new PVector(nearestObstacle.getCenterPosition().x + nearestObstacle.getSize().x / 2, nearestObstacle.getCenterPosition().y);
        up = new PVector(nearestObstacle.getCenterPosition().x, nearestObstacle.getCenterPosition().y - nearestObstacle.getSize().y / 2);
        down = new PVector(nearestObstacle.getCenterPosition().x, nearestObstacle.getCenterPosition().y + nearestObstacle.getSize().y / 2);
        float minimumDistance = 99999;
        PVector steeringLinear = new PVector();
        if (PVector.dist(left, futureRay) < minimumDistance) {
          minimumDistance = PVector.dist(left, futureRay);
          steeringLinear.x = -steeringWeight;
          steeringLinear.y = 0f;
        }
        if (PVector.dist(right, futureRay) < minimumDistance) {
          minimumDistance = PVector.dist(right, futureRay);
          steeringLinear.x = steeringWeight;
          steeringLinear.y = 0f;
        }
        if (PVector.dist(up, futureRay) < minimumDistance) {
          minimumDistance = PVector.dist(up, futureRay);
          steeringLinear.x = 0f;
          steeringLinear.y = -steeringWeight;
        }
        if (PVector.dist(down, futureRay) < minimumDistance) {
          steeringLinear.x = 0f;
          steeringLinear.y = steeringWeight;
        }
        steeringOutput.linear.add(steeringLinear);
      }
    }

    steeringOutput.linear.setMag(steeringWeight);

    return steeringOutput;
  }

}