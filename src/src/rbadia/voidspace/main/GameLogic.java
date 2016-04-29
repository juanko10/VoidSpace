package rbadia.voidspace.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import rbadia.voidspace.model.Asteroid;
import rbadia.voidspace.model.Bullet;
import rbadia.voidspace.model.EnemyShip;
import rbadia.voidspace.model.Ship;
import rbadia.voidspace.sounds.SoundManager;


/**
 * Handles general game logic and status.
 */
public class GameLogic {
	private GameScreen gameScreen;
	private GameStatus status;
	private SoundManager soundMan;
	private ArrayList<Asteroid> asteroids = new ArrayList<>();
	private Ship ship;
	private Asteroid asteroid;
	private Asteroid secondAsteroid;
	private EnemyShip enemyShip;
	private EnemyShip secondEnemyShip;
	private EnemyShip bossShip;
	private List<Bullet> bullets;

	/**
	 * Create a new game logic handler
	 * @param gameScreen the game screen
	 */
	public GameLogic(GameScreen gameScreen){
		this.gameScreen = gameScreen;

		// initialize game status information
		status = new GameStatus();
		// initialize the sound manager
		soundMan = new SoundManager();

		// init some variables
		bullets = new ArrayList<Bullet>();
	}

	/**
	 * Returns the game status
	 * @return the game status 
	 */
	public GameStatus getStatus() {
		return status;
	}

	public SoundManager getSoundMan() {
		return soundMan;
	}

	public GameScreen getGameScreen() {
		return gameScreen;
	}

	/**
	 * Prepare for a new game.
	 */
	public void newGame(){
		status.setGameStarting(true);
		soundMan.playGameTheme();

		// init game variables
		bullets = new ArrayList<Bullet>();

		status.setShipsLeft(3);
		status.setGameOver(false);
		status.setAsteroidsDestroyed(0);
		status.setNewAsteroid(false);
		status.setNewSecondAsteroid(false);
		status.setNewBossShip(false);
		status.setPoints(0);
		status.setLevel(1);

		// init the ship and the asteroid
		newShip(gameScreen);
		newEnemyShip(gameScreen);
		newSecondEnemyShip(gameScreen);
		newBossShip(gameScreen);
		newAsteroid(gameScreen);
		newSecondAsteroid(gameScreen);


		// prepare game screen
		gameScreen.doNewGame();

		// delay to display "Get Ready" message for 1.5 seconds
		Timer timer = new Timer(1500, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				status.setGameStarting(false);
				status.setGameStarted(true);
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

	/**
	 * Check game or level ending conditions.
	 */
	public void checkConditions(){
		// check game over conditions
		if(!status.isGameOver() && status.isGameStarted()){
			if(status.getShipsLeft() == 0){
				gameOver();
			}
		}
	}

	/**
	 * Actions to take when the game is over.
	 */
	public void gameOver(){
		status.setGameStarted(false);
		status.setGameOver(true);
		gameScreen.doGameOver();

		// delay to display "Game Over" message for 3 seconds
		Timer timer = new Timer(3000, new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				status.setGameOver(false);
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

	/**
	 * Fire a bullet from ship.
	 */
	public void fireBullet(){
		Bullet bullet = new Bullet(ship);
		bullets.add(bullet);
		soundMan.playBulletSound();
	}

	/**
	 * Move a bullet once fired.
	 * @param bullet the bullet to move
	 * @return if the bullet should be removed from screen
	 */
	public boolean moveBullet(Bullet bullet){
		if(bullet.getY() - bullet.getSpeed() >= 0){
			bullet.translate(0, -bullet.getSpeed());
			return false;
		}
		else{
			return true;
		}
	}

	/**2
	 * Create a new ship (and replace current one).
	 */
	public Ship newShip(GameScreen screen){
		this.ship = new Ship(screen);
		return ship;
	}

	/**
	 * Create a new asteroid.
	 */
	public Asteroid newAsteroid(GameScreen screen){
		this.asteroid = new Asteroid(screen);
		return asteroid;
	}

	/**
	 * Creates a new second asteroid
	 * @param screen the screen in which it will be drawn
	 * @return the second asteroid
	 */
	public Asteroid newSecondAsteroid(GameScreen screen){
		this.secondAsteroid = new Asteroid(screen);
		return secondAsteroid;
	}

	/**
	 * Creates a new enemy ship
	 * @param screen the screen in which it will be drawn
	 * @return the enemy ship
	 */
	public EnemyShip newEnemyShip(GameScreen screen){
		this.enemyShip = new EnemyShip(screen);
		return enemyShip;
	}

	/**
	 * Creates a new second enemy ship
	 * @param screen the screen in which it will be drawn
	 * @return the second enemy ship
	 */
	public EnemyShip newSecondEnemyShip(GameScreen screen){
		this.secondEnemyShip = new EnemyShip(screen);
		return secondEnemyShip;
	}

	/**
	 * Creates a new boss ship
	 * @param screen the screen in which it will be drawn
	 * @return the sboss ship
	 */
	public EnemyShip newBossShip(GameScreen screen){
		this.bossShip = new EnemyShip(screen);
		return bossShip;
	}
	/**
	 * Returns the ship.
	 * @return the ship
	 */
	public Ship getShip() {
		return ship;
	}

	/**
	 * Returns the enemy ship
	 * @return the enemy ship
	 */
	public EnemyShip getEnemyShip(){
		return enemyShip;
	}

	/**
	 * Returns the second enemy ship
	 * @return the second enemy ship
	 */
	public EnemyShip getSecondEnemyShip(){
		return secondEnemyShip;
	}

	/**
	 * Returns the asteroid.
	 * @return the asteroid
	 */
	public Asteroid getAsteroid() {
		return asteroid;
	}

	/**
	 * Returns a second asteroid
	 * @return the second asteroid
	 */
	public Asteroid getSecondAsteroid(){
		return secondAsteroid;
	}

	/**
	 * Returns the list of bullets.
	 * @return the list of bullets
	 */
	public List<Bullet> getBullets() {
		return bullets;
	}

	/**
	 * Returns the boss ship
	 * @return the boss ship
	 */
	public EnemyShip getBossShip() {return bossShip;}
}
