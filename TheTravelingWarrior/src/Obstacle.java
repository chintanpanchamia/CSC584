//REFERENCE: https://github.com/debalin/project-lalaland

import java.util.*;
import processing.core.*;



public class Obstacle {
  
  private PVector corner;
  private PVector size;
  private List<PVector> tileLocations;
  private Level level;
  private PVector obstacleColor;
  private PVector centerPosition;
  
  public PVector getCorner() {
    return corner;
  }

  public PVector getSize() {
    return size;
  }
  
  public List<PVector> getTileLocations() {
    return tileLocations;
  }
  
  public Obstacle(int firstGridX, int firstGridY, int lastGridX, int lastGridY, Level level, PVector obstacleColor) {
    tileLocations = new ArrayList<>();
    this.level = level;
    this.obstacleColor = obstacleColor;
    PVector centerPosition1 = new PVector(firstGridX * level.getTileSize().x + level.getTileSize().x / 2, firstGridY * level.getTileSize().y + level.getTileSize().y / 2);
    PVector centerPosition2 = new PVector(lastGridX * level.getTileSize().x + level.getTileSize().x / 2, lastGridY * level.getTileSize().y + level.getTileSize().y / 2);
    centerPosition = new PVector((centerPosition1.x + centerPosition2.x) / 2, (centerPosition1.y + centerPosition2.y) / 2);
    
    for (int i = firstGridY; i <= lastGridY; i++) {
      for (int j = firstGridX; j <= lastGridX; j++) {
        tileLocations.add(new PVector(j, i));
        Logger.log("X = " + j + " Y = " + i);
      }
    }
    
    createGameSpaceCorners();
  }
  
  public PVector getObstacleColor() {
    return obstacleColor;
  }

  public void createGameSpaceCorners() {
    corner = new PVector(tileLocations.get(0).x * level.getTileSize().x, tileLocations.get(0).y * level.getTileSize().y);
    float width = (tileLocations.get(tileLocations.size() - 1).x - tileLocations.get(0).x + 1) * level.getTileSize().x;
    float height = (tileLocations.get(tileLocations.size() - 1).y - tileLocations.get(0).y + 1) * level.getTileSize().y;
    size = new PVector(width, height);
  }

  public PVector getCenterPosition() {
    return centerPosition;
  }
}