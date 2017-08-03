//REFERENCE: https://github.com/debalin/project-lalaland

public class LookWhereYoureGoing extends Align {

	public static SteeringOutput getSteering(Kinematic character, Kinematic target, float timeToTarget) {
		target.orientation = character.velocity.heading();
		return Align.getSteering(character, target, timeToTarget);
	}
}