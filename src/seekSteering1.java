import java.util.ArrayList;

import processing.core.*;

public class seekSteering1 extends PApplet {
	PShape ms, circle,triangle;
	int scale;
	float shapewidth, shapeheight;
	int boundary;

	float orientation, targetAngle, maxSpeed, maxForce, maxAccel;
	PVector location, target, velocity, acceleration;
	float ROS, ROD, AOS, AOD, distanceToTarget, timeToTargetRotation;
	PVector desiredVelocity, steer;
	float steeringAngular;
	boolean seek = false;

	ArrayList<Float> crumbs;
	int crumbno;
	public void settings(){
		size(1000,750);
		boundary = 50;

		location = new PVector(width/2, height/2);
		target = new PVector(location.x, location.y);
		velocity = new PVector(0,0);
		acceleration = new PVector(0,0);
		orientation = 0;
		targetAngle = 0;
		maxSpeed = 10;
		maxForce = (float) 0.03;
		maxAccel = (float) 0.25;
		desiredVelocity = new PVector(0,0);
		steer = new PVector(0,0);
		ROS = 25;
		ROD = 250;
		AOS = PI/8;
		AOD = PI/2;
		distanceToTarget = PVector.sub(target, location).mag();
		timeToTargetRotation = (float) 0.25;

		crumbno = 0;
		crumbs = new ArrayList<Float>();
	}
	public void setup(){
		smooth();
		background(255);
		createMyShape();
	}
	public void draw(){
		//noStroke();
		//fill(255,10);
		//frameRate(10);
		rect(0, 0, width, height);
		update();
		//borders();
		display();
		status();
		addBreadCrumbs();
		leaveBreadCrumbs();
	}
	void update(){
		velocity.add(acceleration).limit(maxSpeed);
		location.add(velocity);
		if(seek)	
			arrive(target);
	}
	public void mouseClicked(){
		target = new PVector(mouseX, mouseY);
		seek = true;
		stroke(0);
		strokeWeight(5);
		line(location.x, location.y, mouseX, mouseY);
		targetAngle = atan2(mouseY - location.y, mouseX - location.x);
	}
//	void seek(PVector target){
//		desiredVelocity = PVector.sub(target,location);
//		distanceToTarget = abs(desiredVelocity.mag());
//		desiredVelocity.normalize().mult(maxSpeed);
//		steer = PVector.sub(desiredVelocity, velocity);
//		if(steer.mag() > maxForce)
//			steer.normalize().mult(maxForce);
//		applyForce(steer);
//	}
	void applyForce(PVector force){
		acceleration.add(force).limit(maxAccel);
	}

	void arrive(PVector target){
		desiredVelocity = PVector.sub(target,location);
		distanceToTarget = desiredVelocity.mag();
		if(distanceToTarget < ROS){
			//println(frameCount + "\t" + distanceToTarget + "\t" + ROS);
			//frameRate(5);
			desiredVelocity.mult((float) 0.0);
			acceleration.mult(0);
		}
		else if (distanceToTarget < ROD){
			desiredVelocity.normalize().mult(maxSpeed*distanceToTarget/ROD);
			//println(frameCount + "\t" + distanceToTarget + "\t" + ROS);
			//desiredVelocity.div(4).limit(maxSpeed);
			//frameRate(5);
			//desiredVelocity.mult(map(distanceToTarget, 0, ROD, 0, maxSpeed));
		}
		else desiredVelocity.normalize().mult(maxSpeed);
		//println(desiredVelocity.mag() +"\t"+distanceToTarget);
		steer = PVector.sub(desiredVelocity, velocity);
		applyForce(steer);
	}
//	void align(){
//		float rotation = targetAngle - orientation;
//		rotation = mapToRange(rotation);
//		float rotationSize = abs(rotation);
//		float goalRotation = targetAngle;
//		int noofsteps = 10;
//		float maxRotation = rotation/ noofsteps;
//		if (rotationSize < ROS)
//			;
//		else if (rotationSize > ROD)
//			goalRotation = maxRotation;
//		else goalRotation = maxRotation * rotationSize / ROD;
//		goalRotation *= (rotation/ abs(rotation));
//		steeringAngular = goalRotation - orientation;
//		steeringAngular /= timeToTargetRotation;
//	}
	void status(){
		displayText("Cursor: " + mouseX + ", " + mouseY, 0);
		displayText("Location: " + location.x + ", " + location.y, 1);
		displayText("Target: " + target.x + ", " + target.y, 2);
		displayText("Direction: " + degrees(orientation), 3);
		displayText("Speed: " + velocity.mag() + " DesiredVel=" + (desiredVelocity.mag()) + " Steer=" + steer.mag(), 4);
		displayText("Proximity: " + distanceToTarget + " (ROS)=" + ROS +", (ROD)=" + ROD, 5);
	}
	void createMyShape(){
		scale=min(width, height);
		shapewidth = (float) (scale*0.025);
		shapeheight = (float) (scale*0.025);
		ms = createShape(GROUP);
		ms.setFill(color(0));
		ellipseMode(RADIUS);
		circle = createShape(ELLIPSE, 0, 0, shapewidth, shapeheight);
		circle.setFill(color(0));
		triangle = createShape();
		triangle.beginShape();
		triangle.vertex( (float) 0.3 * shapewidth, -(float) 0.96 * shapeheight);
		triangle.vertex( (float) 0.3 * shapewidth, (float) 0.96 * shapeheight);
		triangle.vertex( 2 * shapewidth, 0);
		triangle.endShape();
		triangle.setFill(color(0));
		ms.addChild(circle);
		ms.addChild(triangle);
	}
	void display(){
		pushMatrix();
		translate(location.x,location.y);
		orient();
		rotate(orientation);
		shape(ms);
		popMatrix();
	}
//	void borders() {
//		if (location.x < -boundary) location.x = width+boundary;
//		if (location.y < -boundary) location.y = height+boundary;
//		if (location.x > width+boundary) location.x = -boundary;
//		if (location.y > height+boundary) location.y = -boundary;
//	}
	void orient(){
		//targetAngle = atan2(target.y-location.y, target.x-location.x);
		int noofsteps = 20;
		float rotation = targetAngle-orientation;
		rotation = mapToRange(rotation);
		float step = rotation/ noofsteps;
		//step = mapToRange(step);
		orientation += step;
	}
	float mapToRange(float rotation){
		float r = rotation % TWO_PI;
		if (abs(r) <= PI)
			return r;
		else if(r > PI) 
			return r-TWO_PI;
		else return r+TWO_PI;
	}
	void addBreadCrumbs() {
		if(frameCount % 1 == 0){
			crumbs.add(location.x);
			crumbs.add(location.y);
			crumbno++;
		}
	}
	void leaveBreadCrumbs(){
		for(int i=0; i<crumbs.size(); i++){
			pushMatrix();
			fill(0);
			stroke(0);
			translate(crumbs.get(i), crumbs.get(i+1));
			i++;
			ellipseMode(RADIUS);
			ellipse(0, 0, 1, 1);
			fill(255);
			popMatrix();
		}
	}
	void displayText(String s, int offset){
		fill(0);
		this.textSize(25);
		this.textAlign(PConstants.LEFT);
		text(s, boundary, boundary+30*offset);
		fill(255);
	}

	public static void main(String[] args) {
		PApplet.main(new String[] {seekSteering1.class.getName()});
	}
}