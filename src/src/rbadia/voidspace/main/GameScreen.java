package rbadia.voidspace.main;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;

import rbadia.voidspace.graphics.GraphicsManager;
import rbadia.voidspace.model.Asteroid;
import rbadia.voidspace.model.Bullet;
import rbadia.voidspace.model.EnemyShip;
import rbadia.voidspace.model.Ship;
import rbadia.voidspace.sounds.SoundManager;

/**
 * Main game screen. Handles all game graphics updates and some of the game logic.
 */
public class GameScreen extends JPanel {
	private static final long serialVersionUID = 1L;

	private BufferedImage backBuffer;
	private Graphics2D g2d;

	private static final int NEW_SHIP_DELAY = 500;
	private static final int NEW_ASTEROID_DELAY = 500;
	private static final int NEW_BOSS_SHIP_DELAY = 500;

	private long lastShipTime;
	private long lastAsteroidTime;
	private long lastBossShipTime;

	private Rectangle asteroidExplosion;
	private Rectangle shipExplosion;
	private Rectangle bossShipExplosion;

	private JLabel shipsValueLabel;
	private JLabel asteroidsDestroyedValueLabel;
	private JLabel pointsValueLabel;
	private JLabel levelValueLabel;
	private JLabel shipsDestroyedValueLabel;

	private Random rand;

	private Font originalFont;
	private Font bigFont;
	private Font biggestFont;

	private GameStatus status;
	private SoundManager soundMan;
	private GraphicsManager graphicsMan;
	private GameLogic gameLogic;

	/**
	 * This method initializes 
	 * 
	 */
	public GameScreen() {
		super();
		// initialize random number generator
		rand = new Random();

		initialize();

		// init graphics manager
		graphicsMan = new GraphicsManager();

		// init back buffer image
		backBuffer = new BufferedImage(500, 400, BufferedImage.TYPE_INT_RGB);
		g2d = backBuffer.createGraphics();
	}

	/**
	 * Initialization method (for VE compatibility).
	 */
	private void initialize() {
		// set panel properties
		this.setSize(new Dimension(500, 400));
		this.setPreferredSize(new Dimension(500, 400));
		this.setBackground(Color.BLACK);
	}

	/**
	 * Update the game screen.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// draw current backbuffer to the actual game screen
		g.drawImage(backBuffer, 0, 0, this);
	}

	/**
	 * Update the game screen's backbuffer image.
	 */
	public void updateScreen(){
		Ship ship = gameLogic.getShip();
		EnemyShip enemyShip = gameLogic.getEnemyShip();
		EnemyShip secondEnemyShip = gameLogic.getSecondEnemyShip();
		EnemyShip bossShip = gameLogic.getBossShip();
		Asteroid asteroid = gameLogic.getAsteroid();
		Asteroid secondAsteroid = gameLogic.getSecondAsteroid();
		List<Bullet> bullets = gameLogic.getBullets();

		// set orignal font - for later use
		if(this.originalFont == null){
			this.originalFont = g2d.getFont();
			this.bigFont = originalFont;
		}

		// erase screen
		g2d.setPaint(Color.BLACK);
		g2d.fillRect(0, 0, getSize().width, getSize().height);

		// draw 50 random stars
		drawStars(50);

		// if the game is starting, draw "Get Ready" message
		if(status.isGameStarting()){
			drawGetReady();
			return;
		}

		// if the game is over, draw the "Game Over" message
		if(status.isGameOver()){
			// draw the message
			drawGameOver();

			long currentTime = System.currentTimeMillis();
			// draw the explosions until their time passes
			if((currentTime - lastAsteroidTime) < NEW_ASTEROID_DELAY){
				graphicsMan.drawAsteroidExplosion(asteroidExplosion, g2d, this);
			}
			if((currentTime - lastShipTime) < NEW_SHIP_DELAY){
				graphicsMan.drawShipExplosion(shipExplosion, g2d, this);
			}
			if((currentTime- lastBossShipTime) < NEW_BOSS_SHIP_DELAY){
				graphicsMan.drawShipExplosion(bossShipExplosion, g2d, this);
			}
			return;
		}

		// the game has not started yet
		if(!status.isGameStarted()){
			// draw game title screen
			initialMessage();
			return;
		}

		//draw the asteroids
		this.createAsteroid(asteroid, 1, "FIRST");
		this.createAsteroid(secondAsteroid, 1, "SECOND");	
		
		
		//draw enemy ship
		this.createEnemyShip(enemyShip, "LEFT", 2);
		this.createEnemyShip(secondEnemyShip, "RIGHT", 1);

		//draw boss
		this.createBossShip(bossShip, "Middle", 1 );

		// draw bullets
		for(int i=0; i<bullets.size(); i++){
			Bullet bullet = bullets.get(i);
			graphicsMan.drawBullet(bullet, g2d, this);

			boolean remove = gameLogic.moveBullet(bullet);
			if(remove){
				bullets.remove(i);
				i--;
			}
		}

		// check bullet-asteroid collisions
		this.bulletAsteroidCollision(bullets, asteroid);
		this.bulletAsteroidCollision(bullets, secondAsteroid);		
		
		
		// check bullet-enemyShip collisions
		this.bulletEnemyShipCollision(bullets, enemyShip);
		this.bulletEnemyShipCollision(bullets, secondEnemyShip);
		this.bulletBossShipCollision(bullets, bossShip);

		// draw ship
		if(!status.isNewShip()){
			// draw it in its current location
			graphicsMan.drawShip(ship, g2d, this);
		}
		else{
			// draw a new one
			long currentTime = System.currentTimeMillis();
			if((currentTime - lastShipTime) > NEW_SHIP_DELAY){
				lastShipTime = currentTime;
				status.setNewShip(false);
				ship = gameLogic.newShip(this);
			}
			else{
				// draw explosion
				graphicsMan.drawShipExplosion(shipExplosion, g2d, this);
			}
		}

		// check ship-asteroid collisions
		this.shipAsteroidCollision(ship, asteroid);
		this.shipAsteroidCollision(ship, secondAsteroid);
		
		//check ship-enemy ship collisions
		this.shipEnemyShipCollision(ship, enemyShip);
		this.shipEnemyShipCollision(ship, secondEnemyShip);
		this.shipBossShipCollision(ship, bossShip);

		
		
		//update ships destroyed label
		shipsDestroyedValueLabel.setText(Long.toString(status.getEnemyShipsDestroyed()));

		// update asteroids destroyed label
		asteroidsDestroyedValueLabel.setText(Long.toString(status.getAsteroidsDestroyed()));

		// update ships left label
		shipsValueLabel.setText(Integer.toString(status.getShipsLeft()));

		//update points earned
		pointsValueLabel.setText(Integer.toString(status.getPoints()));

		//update the current level
		levelValueLabel.setText(Integer.toString(status.getLevel()));

	}
	
	public void createAsteroid(Asteroid newAsteroid, int speedMultiplier, String position){
		// draw asteroid
		if(!status.isNewAsteroid()){
			// draw the asteroid until it reaches the bottom of the screen
			if(newAsteroid.getY() + newAsteroid.getSpeed() < this.getHeight()){
				newAsteroid.translate(0, newAsteroid.getSpeed() * speedMultiplier);
				graphicsMan.drawAsteroid(newAsteroid, g2d, this);
			}			
			else{
				newAsteroid.setLocation(rand.nextInt(getWidth() - newAsteroid.width), 0);
			}
		} else {
			long currentTime = System.currentTimeMillis();
			if((currentTime - lastAsteroidTime) > NEW_ASTEROID_DELAY){
				// draw a new asteroid
				lastAsteroidTime = currentTime;
				
				if(position.equals("FIRST")){
					status.setNewAsteroid(false);
				}
				if(position.equals("SECOND")){
					status.setNewSecondAsteroid(false);
				}
				newAsteroid.setLocation(rand.nextInt(getWidth() - newAsteroid.width), 0);
			}
			else{
				// draw explosion
				graphicsMan.drawAsteroidExplosion(asteroidExplosion, g2d, this);
			}
		}
	}
	
	public void createEnemyShip(EnemyShip newEnemyShip, String direction, int speedMultiplier){
		
		if(!status.isNewEnemyShip()){			
			// draw the enemy ship until it reaches the bottom of the screen
			if(newEnemyShip.getY() + newEnemyShip.getSpeed() < this.getHeight()){
				if(direction.equals("LEFT")){
					
					//the enemy ships go to the left direction
					newEnemyShip.translate(-newEnemyShip.getSpeed(), newEnemyShip.getSpeed() * speedMultiplier);
					graphicsMan.drawEnemyShip(newEnemyShip, g2d, this);
				}
				else if(direction.equals("RIGHT")){
					//the enemy ships go to the right direction
					newEnemyShip.translate(newEnemyShip.getSpeed(), newEnemyShip.getSpeed() * speedMultiplier);
					graphicsMan.drawEnemyShip(newEnemyShip, g2d, this);
				}
			}			
			else{
				newEnemyShip.setLocation(rand.nextInt(getWidth() - newEnemyShip.width), 0);
			}
		} else {
			long currentTime = System.currentTimeMillis();
			if((currentTime - lastAsteroidTime) > NEW_ASTEROID_DELAY){
				// draw a new enemy ship
				lastAsteroidTime = currentTime;
				status.setNewEnemyShip(false);
				newEnemyShip.setLocation(rand.nextInt(getWidth() - newEnemyShip.width), 0);
			}
			else{
				// draw explosion
				graphicsMan.drawAsteroidExplosion(asteroidExplosion, g2d, this);
			}
		}
	}

	public void createBossShip(EnemyShip newBossShip, String direction, int speedMultiplier){

		//if(status.getLevel() % 5 == 0 ){
			if(!status.isNewBossShip()){
				// draw it in its current location
					if(newBossShip.getY() + newBossShip.getSpeed() < this.getHeight()){
						if(direction.equals("Middle")){

							//the enemy ships go to the left direction
							newBossShip.translate(-newBossShip.getSpeed(), newBossShip.getSpeed() * speedMultiplier);
							graphicsMan.drawBossShip(newBossShip, g2d, this);
						} else{
							newBossShip.setLocation(rand.nextInt(getWidth() - newBossShip.width), 0);
						}
					} else {
						long currentTime = System.currentTimeMillis();
						if((currentTime - lastAsteroidTime) > NEW_ASTEROID_DELAY){
							// draw a new enemy ship
							lastAsteroidTime = currentTime;
							status.setNewBossShip(false);
							newBossShip.setLocation(rand.nextInt(getWidth() - newBossShip.width), 0);
						} else{
							// draw explosion
							graphicsMan.drawAsteroidExplosion(asteroidExplosion, g2d, this);
						}
					}
				}

		else{
			long currentTime = System.currentTimeMillis();
			if((currentTime - lastAsteroidTime) > NEW_ASTEROID_DELAY){
				// draw a new enemy ship
				lastAsteroidTime = currentTime;
				status.setNewEnemyShip(false);
				newBossShip.setLocation(rand.nextInt(getWidth() - newBossShip.width), 0);
			}
			else{
				// draw explosion
				graphicsMan.drawAsteroidExplosion(asteroidExplosion, g2d, this);
			}
		}
	}


	public void bulletAsteroidCollision(List<Bullet> bullets, Asteroid newAsteroid){
		for(int i=0; i<bullets.size(); i++){
			Bullet bullet = bullets.get(i);
			if(newAsteroid.intersects(bullet)){
				// increase asteroids destroyed count
				status.setAsteroidsDestroyed(status.getAsteroidsDestroyed() + 1);
				//increase number of points
				status.setPoints(status.getPoints() + 100);
				if(status.getAsteroidsDestroyed() % 5 == 0){
					//increase level each time you destroy 5 asteroids
					status.setLevel(status.getLevel() + 1);

				}

				// "remove" newAsteroid
				asteroidExplosion = new Rectangle(
						newAsteroid.x,
						newAsteroid.y,
						newAsteroid.width,
						newAsteroid.height);
				newAsteroid.setLocation(-newAsteroid.width, -newAsteroid.height);
				status.setNewAsteroid(true);
				lastAsteroidTime = System.currentTimeMillis();

				// play asteroid explosion sound
				soundMan.playAsteroidExplosionSound();

				// remove bullet
				bullets.remove(i);
				break;
			}
		}
	}
	
	public void bulletEnemyShipCollision(List<Bullet> bullets, EnemyShip newEnemyShip){
		for(int i=0; i<bullets.size(); i++){
			Bullet bullet = bullets.get(i);
			if(newEnemyShip.intersects(bullet)){
				// increase enemy ships destroyed count
				status.setEnemyShipsDestroyed(status.getEnemyShipsDestroyed() + 1);
				//increase number of points
				status.setPoints(status.getPoints() + 250);						

				// "remove" asteroid
				asteroidExplosion = new Rectangle(
						newEnemyShip.x,
						newEnemyShip.y,
						newEnemyShip.width,
						newEnemyShip.height);
				newEnemyShip.setLocation(-newEnemyShip.width, -newEnemyShip.height);
				status.setNewEnemyShip(true);
				lastAsteroidTime = System.currentTimeMillis();

				// play asteroid explosion sound
				soundMan.playAsteroidExplosionSound();

				// remove bullet
				bullets.remove(i);
				break;
			}
		}
	}

	public void bulletBossShipCollision(List<Bullet> bullets, EnemyShip newBossShip){
		for(int i=0; i<bullets.size(); i++){
			Bullet bullet = bullets.get(i);
			if(newBossShip.intersects(bullet)){
				// increase enemy ships destroyed count
				status.setBossShipsDestroyed(status.getBossShipsDestroyed() + 1);
				//increase number of points
				status.setPoints(status.getPoints() + 250);

				// "remove" asteroid
				asteroidExplosion = new Rectangle(
						newBossShip.x,
						newBossShip.y,
						newBossShip.width,
						newBossShip.height);
				newBossShip.setLocation(-newBossShip.width, -newBossShip.height);
				status.setNewBossShip(true);
				lastAsteroidTime = System.currentTimeMillis();

				// play asteroid explosion sound
				soundMan.playAsteroidExplosionSound();

				// remove bullet
				bullets.remove(i);
				break;
			}
		}
	}
	
	public void shipAsteroidCollision(Ship ship, Asteroid newAsteroid){
		if(newAsteroid.intersects(ship)){
			// decrease number of ships left
			status.setShipsLeft(status.getShipsLeft() - 1);

			status.setAsteroidsDestroyed(status.getAsteroidsDestroyed() + 1);

			// "remove" asteroid
			asteroidExplosion = new Rectangle(
					newAsteroid.x,
					newAsteroid.y,
					newAsteroid.width,
					newAsteroid.height);
			newAsteroid.setLocation(-newAsteroid.width, -newAsteroid.height);
			status.setNewAsteroid(true);
			lastAsteroidTime = System.currentTimeMillis();

			// "remove" ship
			shipExplosion = new Rectangle(
					ship.x,
					ship.y,
					ship.width,
					ship.height);
			ship.setLocation(this.getWidth() + ship.width, -ship.height);
			status.setNewShip(true);
			lastShipTime = System.currentTimeMillis();

			// play ship explosion sound
			soundMan.playShipExplosionSound();
			// play asteroid explosion sound
			soundMan.playAsteroidExplosionSound();
		}
	}
	
	
	public void shipEnemyShipCollision(Ship targetShip, EnemyShip targetEnemyShip){
		if(targetEnemyShip.intersects(targetShip)){
			// decrease number of ships left
			status.setShipsLeft(status.getShipsLeft() - 1);

			status.setAsteroidsDestroyed(status.getAsteroidsDestroyed() + 1);

			// "remove" asteroid
			asteroidExplosion = new Rectangle(
					targetEnemyShip.x,
					targetEnemyShip.y,
					targetEnemyShip.width,
					targetEnemyShip.height);
			targetEnemyShip.setLocation(-targetEnemyShip.width, -targetEnemyShip.height);
			status.setNewAsteroid(true);
			lastAsteroidTime = System.currentTimeMillis();

			// "remove" ship
			shipExplosion = new Rectangle(
					targetShip.x,
					targetShip.y,
					targetShip.width,
					targetShip.height);
			targetShip.setLocation(this.getWidth() + targetShip.width, -targetShip.height);
			status.setNewShip(true);
			lastShipTime = System.currentTimeMillis();

			// play ship explosion sound
			soundMan.playShipExplosionSound();
			// play asteroid explosion sound
			soundMan.playAsteroidExplosionSound();
		}
	}

	public void shipBossShipCollision(Ship targetShip, EnemyShip targetBossShip){
		if(targetBossShip.intersects(targetShip)){
			// decrease number of ships left
			status.setShipsLeft(status.getShipsLeft() - 1);

			status.setAsteroidsDestroyed(status.getAsteroidsDestroyed() + 1);

			// "remove" asteroid
			asteroidExplosion = new Rectangle(
					targetBossShip.x,
					targetBossShip.y,
					targetBossShip.width,
					targetBossShip.height);
			targetBossShip.setLocation(-targetBossShip.width, -targetBossShip.height);
			status.setNewAsteroid(true);
			lastAsteroidTime = System.currentTimeMillis();

			// "remove" ship
			shipExplosion = new Rectangle(
					targetShip.x,
					targetShip.y,
					targetShip.width,
					targetShip.height);
			targetShip.setLocation(this.getWidth() + targetShip.width, -targetShip.height);
			status.setNewBossShip(true);
			lastShipTime = System.currentTimeMillis();

			// play ship explosion sound
			soundMan.playShipExplosionSound();
			// play asteroid explosion sound
			soundMan.playAsteroidExplosionSound();
		}
	}

	/**
	 * Draws the "Game Over" message.
	 */
	private void drawGameOver() {
		String gameOverStr = "GIT GUD OR GET REKT";
		Font currentFont = biggestFont == null? bigFont : biggestFont;
		float fontSize = currentFont.getSize2D();
		bigFont = currentFont.deriveFont(fontSize + 1).deriveFont(Font.BOLD);
		FontMetrics fm = g2d.getFontMetrics(bigFont);
		int strWidth = fm.stringWidth(gameOverStr);
		if(strWidth > this.getWidth() - 10){
			biggestFont = currentFont;
			bigFont = biggestFont;
			fm = g2d.getFontMetrics(bigFont);
			strWidth = fm.stringWidth(gameOverStr);
		}
		int ascent = fm.getAscent();
		int strX = (this.getWidth() - strWidth)/2;
		int strY = (this.getHeight() + ascent)/2;
		g2d.setFont(bigFont);
		g2d.setPaint(Color.WHITE);
		g2d.drawString(gameOverStr, strX, strY);
	}

	/**
	 * Draws the initial "Get Ready!" message.
	 */
	private void drawGetReady() {
		String readyStr = "Get Ready!";
		g2d.setFont(originalFont.deriveFont(originalFont.getSize2D() + 1));
		FontMetrics fm = g2d.getFontMetrics();
		int ascent = fm.getAscent();
		int strWidth = fm.stringWidth(readyStr);
		int strX = (this.getWidth() - strWidth)/2;
		int strY = (this.getHeight() + ascent)/2;
		g2d.setPaint(Color.WHITE);
		g2d.drawString(readyStr, strX, strY);
	}

	/**
	 * Draws the specified number of stars randomly on the game screen.
	 * @param numberOfStars the number of stars to draw
	 */
	private void drawStars(int numberOfStars) {
		g2d.setColor(Color.WHITE);
		for(int i=0; i<numberOfStars; i++){
			int x = (int)(Math.random() * this.getWidth());
			int y = (int)(Math.random() * this.getHeight());
			g2d.drawLine(x, y, x, y);
		}
	}

	/**
	 * Display initial game title screen.
	 */
	private void initialMessage() {
		String gameTitleStr = "Caw Space";

		Font currentFont = biggestFont == null? bigFont : biggestFont;
		float fontSize = currentFont.getSize2D();
		bigFont = currentFont.deriveFont(fontSize + 1).deriveFont(Font.BOLD).deriveFont(Font.ITALIC);
		FontMetrics fm = g2d.getFontMetrics(bigFont);
		int strWidth = fm.stringWidth(gameTitleStr);
		if(strWidth > this.getWidth() - 10){
			bigFont = currentFont;
			biggestFont = currentFont;
			fm = g2d.getFontMetrics(currentFont);
			strWidth = fm.stringWidth(gameTitleStr);
		}
		g2d.setFont(bigFont);
		int ascent = fm.getAscent();
		int strX = (this.getWidth() - strWidth)/2;
		int strY = (this.getHeight() + ascent)/2 - ascent;
		g2d.setPaint(Color.YELLOW);
		g2d.drawString(gameTitleStr, strX, strY);

		g2d.setFont(originalFont);
		fm = g2d.getFontMetrics();
		String newGameStr = "Press <Space> to Start a New Game.";
		strWidth = fm.stringWidth(newGameStr);
		strX = (this.getWidth() - strWidth)/2;
		strY = (this.getHeight() + fm.getAscent())/2 + ascent + 16;
		g2d.setPaint(Color.WHITE);
		g2d.drawString(newGameStr, strX, strY);

		fm = g2d.getFontMetrics();
		String exitGameStr = "Press <Esc> to Exit the Game.";
		strWidth = fm.stringWidth(exitGameStr);
		strX = (this.getWidth() - strWidth)/2;
		strY = strY + 16;
		g2d.drawString(exitGameStr, strX, strY);
	}

	/**
	 * Prepare screen for game over.
	 */
	public void doGameOver(){
		shipsValueLabel.setForeground(new Color(128, 0, 0));
	}

	/**
	 * Prepare screen for a new game.
	 */
	public void doNewGame(){		
		lastAsteroidTime = -NEW_ASTEROID_DELAY;
		lastShipTime = -NEW_SHIP_DELAY;

		bigFont = originalFont;
		biggestFont = null;

		// set labels' text
		shipsValueLabel.setForeground(Color.BLACK);
		shipsValueLabel.setText(Integer.toString(status.getShipsLeft()));
		asteroidsDestroyedValueLabel.setText(Long.toString(status.getAsteroidsDestroyed()));
		shipsDestroyedValueLabel.setText(Long.toString(status.getAsteroidsDestroyed()));
		pointsValueLabel.setText(Integer.toString(status.getPoints()));
		levelValueLabel.setText(Integer.toString(status.getLevel()));


	}

	/**
	 * Sets the game graphics manager.
	 * @param graphicsMan the graphics manager
	 */
	public void setGraphicsMan(GraphicsManager graphicsMan) {
		this.graphicsMan = graphicsMan;
	}

	/**
	 * Sets the game logic handler
	 * @param gameLogic the game logic handler
	 */
	public void setGameLogic(GameLogic gameLogic) {
		this.gameLogic = gameLogic;
		this.status = gameLogic.getStatus();
		this.soundMan = gameLogic.getSoundMan();
	}

	/**
	 * Sets the label that displays the value for asteroids destroyed.
	 * @param asteroidsDestroyedValueLabel the label to set
	 */
	public void setAsteroidsDestroyedValueLabel(JLabel asteroidsDestroyedValueLabel) {
		this.asteroidsDestroyedValueLabel = asteroidsDestroyedValueLabel;
	}
	
	/**
	 * Sets the label that displays the value for enemy ships destroyed.
	 * @param shipsDestroyedValueLabel the label to set
	 */
	public void setShipsDestroyedValueLabel(JLabel shipsDestroyedValueLabel){
		this.shipsDestroyedValueLabel = shipsDestroyedValueLabel;
	}

	/**
	 * Sets the label that displays the value for ship (lives) left
	 * @param shipsValueLabel the label to set
	 */
	public void setShipsValueLabel(JLabel shipsValueLabel) {
		this.shipsValueLabel = shipsValueLabel;
	}

	/**
	 * Sets the label that displays the points accumulated
	 * @param pointsValueLabel the label to set
	 */
	public void setPointsValueLabel(JLabel pointsValueLabel) {
		this.pointsValueLabel = pointsValueLabel;
	}

	/**
	 * Sets the label that displays the level that you're currently in
	 * @param levelValueLabel the label to set
	 */
	public void setLevelValueLabel(JLabel levelValueLabel){
		this.levelValueLabel = levelValueLabel;
	}	
	
}
