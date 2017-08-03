
import java.util.ArrayList;

import processing.core.*;

public class wanderSteering1 extends PApplet {
	PShape ms, circle,triangle;
	int scale;
	float shapewidth, shapeheight;
	int boundary;

	float orientation, targetOrientation, wanderOrientation, maxSpeed, maxAccel;
	PVector location, target, velocity, acceleration;
	float wanderRadius, wanderOffset;
	PVector desiredVelocity, steer;
	float maxForce;

	ArrayList<Float> crumbs;
	int crumbno;
	
	public void settings(){
		size(2000,1500);
		boundary = 50;

		location = new PVector(width/2, height/2);
		target = new PVector(location.x, location.y);
		velocity = new PVector(0,0);
		acceleration = new PVector(0,0);
		orientation = 0;
		targetOrientation = 0;
		wanderOrientation = 0;
		maxSpeed = 10;
		desiredVelocity = new PVector(random(1),random(1));
		steer = new PVector(0,0);
		maxAccel = (float) 0.1;
		maxForce = (float) 0.03;
		wanderRadius = 5;
		wanderOffset = 25;

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
		borders();
		display();
		status();
		addBreadCrumbs();
		leaveBreadCrumbs();
	}
	void update(){
		velocity.add(acceleration).limit(maxSpeed);
		location.add(velocity);
		maxSpeed = random(2,10);
		if(random(1)<0.5){wander();}
		else wander1();
	}
	void seek(PVector target){
		desiredVelocity = PVector.sub(target,location);
		desiredVelocity.normalize().mult(maxSpeed);
		steer = PVector.sub(desiredVelocity, velocity);
		if(steer.mag() > maxForce)
			steer.normalize().mult(maxForce);
		applyForce(steer);
	}
	boolean wander0 = true;
	void wander(){
		wander0=true;
		wanderOrientation += randomBinomial() * TWO_PI;
		targetOrientation = wanderOrientation + orientation;
		
		target = PVector.add(location,(asVector(orientation).mult(wanderOffset)));
		target.add(asVector(targetOrientation).mult(random(1,(float) 2) * wanderRadius));
		seek(target);
	}
	PVector asVector(float theta){
		return new PVector(cos(theta), sin(theta));
	}
	float randomBinomial(){
		return random(-1,1)-random(-1,1);
	}

	void seek1(PVector target) {
		acceleration.add(steer1(target,false));
	}
	void arrive1(PVector target) {
		acceleration.add(steer1(target,true));
	}
	float wandertheta = (float) 0.0;
	void wander1() {
		wander0 = false;
		float wanderR = 10.0f;         
		float wanderD = 50.0f;         
		float change = 15f;
		wandertheta += random(-change,change);
		PVector circleloc = velocity.get();  
		circleloc.normalize();          
		circleloc.mult(wanderD);        
		circleloc.add(location);
		PVector circleOffSet = new PVector(wanderR*cos(wandertheta),wanderR*sin(wandertheta));
		targetOrientation = wandertheta;
		target = PVector.add(circleloc,circleOffSet);
		acceleration.add(steer1(target,false));
		//applyForce(acceleration);
	}
	PVector steer1(PVector t, boolean slowdown) {
		PVector steer; 
		desiredVelocity = PVector.sub(t,location);
		float d = desiredVelocity.mag(); 
		if (d > 0) {
			desiredVelocity.normalize();
			if ((slowdown) && (d < 100.0f)) desiredVelocity.mult(maxSpeed*(d/100.0f)); 
			else desiredVelocity.mult(maxSpeed);
			steer = PVector.sub(desiredVelocity,velocity);
			steer.limit(maxForce);  
		} else {
			steer = new PVector(0,0);
		}
		return steer;
	}
	void wander2(){
		float changeOrientation = (float) random(15);
		targetOrientation += changeOrientation;
		
		
		//applyForce(new PVector(cos(random(5)), sin(random(5))));
	}
	
	void applyForce(PVector force){
		acceleration.add(force).limit(maxAccel);
	}
	void status(){
		displayText("Location: " + location.x + ", " + location.y, 0);
		displayText("Target: " + target.x + ", " + target.y, 1);
		displayText("Direction: " + degrees(orientation), 2);
		displayText("Velocity: " + velocity.mag(), 3);
		displayText("WanderType: " + (((wander0) == true)?"0":"1"), 4);
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
		if(frameCount % 10 == 0)	orient();
		rotate(orientation);
		shape(ms);
		popMatrix();
	}
	void borders() {
		if (location.x < -boundary) location.x = width+boundary;
		if (location.y < -boundary) location.y = height+boundary;
		if (location.x > width+boundary) location.x = -boundary;
		if (location.y > height+boundary) location.y = -boundary;
	}
	void orient(){
		//targetAngle = atan2(target.y-location.y, target.x-location.x);
		int noofsteps = 20;
		float rotation = targetOrientation-orientation;
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
		PApplet.main(new String[] {wanderSteering1.class.getName()});
	}
}