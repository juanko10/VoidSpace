package rbadia.voidspace.model;

import java.awt.Rectangle;
import java.util.Random;

import rbadia.voidspace.main.GameScreen;

/**
 * Represents a ship/space craft.
 *
 */
public class EnemyShip extends Rectangle {
	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_SPEED = 2;

	private int enemyShipWidth = 25;
	private int enemyShipHeight = 25;
	private int speed = DEFAULT_SPEED;
	private Random rand = new Random();


	/**
	 * Creates a new enemy ship at the default initial location. 
	 * @param screen the game screen
	 */
	public EnemyShip(GameScreen screen){
		this.setLocation(rand.nextInt(screen.getWidth() - enemyShipWidth), 0);
		this.setSize(enemyShipWidth, enemyShipHeight);		
	}

	/**
	 * Get the default ship width
	 * @return the default ship width
	 */
	public int getEnemyShipWidth() {
		return enemyShipWidth;
	}

	/**
	 * Get the default ship height
	 * @return the default ship height
	 */
	public int getEnemyShipHeight() {
		return enemyShipHeight;
	}

	/**
	 * Returns the current ship speed
	 * @return the current ship speed
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * Set the current ship speed
	 * @param speed the speed to set
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	/**
	 * Returns the default ship speed.
	 * @return the default ship speed
	 */
	public int getDefaultSpeed(){
		return DEFAULT_SPEED;
	}

}
