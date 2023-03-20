package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SpaceNinja extends Application {
	//declare final variables
	private static final Random rand = new Random();
	private static final int width = 800;
	private static final int height = 600;
	private static final int SizePlayer = 80;
	static final Image playerImg = new Image("file:/Users/holdenbajorek/git/SpaceNinja/SpaceNinjaProject/SpaceNinja/src/images/spaceNinja4.png"); 
	static final Image explosion = new Image("file:/Users/holdenbajorek/git/SpaceNinja/SpaceNinjaProject/SpaceNinja/src/images/explosion.png");
	static final int explosionWidth = 128;
	static final int explosionRows = 3;
	static final int explosionCols = 3;
	static final int explosionHeight = 128;
	static final int explosionSTEPS = 15;
	
	//declare variables
	final int maxInvaders = 10,  maxShots = maxInvaders * 2;
	boolean gameOver = false;
	private GraphicsContext gc;
	private double mouseX;
	private int score;
	
	//load images in invadersImg array for invaders
	static final Image invadersImg[] = {
			new Image("file:/Users/holdenbajorek/git/SpaceNinja/SpaceNinjaProject/SpaceNinja/src/images/invader.png"),
			new Image("file:/Users/holdenbajorek/git/SpaceNinja/SpaceNinjaProject/SpaceNinja/src/images/alien1.png"),
			new Image("file:/Users/holdenbajorek/git/SpaceNinja/SpaceNinjaProject/SpaceNinja/src/images/invader2.png"),
			new Image("file:/Users/holdenbajorek/git/SpaceNinja/SpaceNinjaProject/SpaceNinja/src/images/invader3.png"),
			new Image("file:/Users/holdenbajorek/git/SpaceNinja/SpaceNinjaProject/SpaceNinja/src/images/Asteroid1.png"),
			new Image("file:/Users/holdenbajorek/git/SpaceNinja/SpaceNinjaProject/SpaceNinja/src/images/Asteroid2.png"),
			new Image("file:/Users/holdenbajorek/git/SpaceNinja/SpaceNinjaProject/SpaceNinja/src/images/Asteroid3.png"),
			new Image("file:/Users/holdenbajorek/git/SpaceNinja/SpaceNinjaProject/SpaceNinja/src/images/alien2.png"),
			new Image("file:/Users/holdenbajorek/git/SpaceNinja/SpaceNinjaProject/SpaceNinja/src/images/alien3.png"),
			new Image("file:/Users/holdenbajorek/git/SpaceNinja/SpaceNinjaProject/SpaceNinja/src/images/Asteroid4.png"),
	};
	
	//updating list arrays
	Ninja player;
	List<Shot> shots;
	List<Universe> univ;
	List<invader> Invaders;

	//start
	public void start(Stage stage) throws Exception {
		//create canvas object to draw on
		Canvas canvas = new Canvas(width, height);	
		gc = canvas.getGraphicsContext2D();
		//create timeline to create animation
		//run for 0.1 seconds animating explosions, grow/shrink effect
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> run(gc)));
		//animation played until stopped (Indefinite)
		timeline.setCycleCount(Timeline.INDEFINITE); 
		timeline.play();
		//set cursor on mouse move
		canvas.setCursor(Cursor.MOVE);
		canvas.setOnMouseMoved(e -> mouseX = e.getX());
		//on mouse click, shoot laser shots
		canvas.setOnMouseClicked(e -> {
			if(shots.size() < maxShots) shots.add(player.shoot());
			//if game over, set gameOver to false and call method setup to reset game
			if(gameOver) { 
				gameOver = false;
				setup();
			}
		});
		//call setup to start game
		setup();
		//set stage
		stage.setScene(new Scene(new StackPane(canvas)));
		stage.setTitle("Space Ninja");
		stage.show();
		
	}

	//setup the game
	private void setup() {
		//create arrays
		univ = new ArrayList<>();
		shots = new ArrayList<>();
		Invaders = new ArrayList<>();
		//set player size
		player = new Ninja(width / 2, height - SizePlayer, SizePlayer, playerImg);
		//set score to 0
		score = 0;
		//int value stream, setup invaders in range
		IntStream.range(0, maxInvaders).mapToObj(i -> this.newinvader()).forEach(Invaders::add);
	}
		
	//run method for graphics
	private void run(GraphicsContext gc) {
		//display score during game play
		gc.setFill(Color.grayRgb(20));
		gc.fillRect(0, 0, width, height);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setFont(Font.font(20));
		gc.setFill(Color.WHITE);
		gc.fillText("Score: " + score, 60,  20);
	
		//if game over, tell user, display score and ask player if they want to play again
		if(gameOver) {
			gc.setFont(Font.font(35));
			gc.setFill(Color.YELLOW);
			gc.fillText("Game Over \n Your Score is: " + score + " \n Click to play again.", width / 2, height /2.5);
		}
		//universe array list, draw
		univ.forEach(Universe::draw);
		//call methods for player
		player.update();
		player.draw();
		//position player with mouse move along x axis
		player.posX = (int) mouseX;
		
		//invaders stream to see elements flow through pipeline of game
		Invaders.stream().peek(Ninja::update).peek(Ninja::draw).forEach(e -> {
			//if player collides with invader(e) and if player is not currently exploding
			if(player.collide(e) && !player.exploding) {
				//explode player
				player.explode();
			}
		});
		
		//for each i in shots array 
		for (int i = shots.size() - 1; i >= 0 ; i--) {
			Shot laser = shots.get(i);
			//if laser is greater than 0 on Y axis or laser to remove
			if(laser.posY < 0 || laser.toRemove)  { 
				//remove shot
				shots.remove(i);
				continue;
			}
			//update method laser
			laser.update();
			//draw method laser
			laser.draw();
			//for each invader 
			for (invader invader : Invaders) {
				//if laser shot collides with invader and invader is not exploding
				if(laser.collide(invader) && !invader.exploding) {
					//increase score
					score++;
					//explode invader
					invader.explode();
					//remove laser shot = true
					laser.toRemove = true;
				}
			}
		}
		//for each i in invader array
		for (int i = Invaders.size() - 1; i >= 0; i--){  
			//if invader is destroyed 
			if(Invaders.get(i).destroyed)  {
				//set new invader
				Invaders.set(i, newinvader());
			}
		}
		//if player is destroyed, game over
		gameOver = player.destroyed;
		//if random int is greater than 2, add new universe
		if(rand.nextInt(10) > 2) {
			univ.add(new Universe());
		}
		
		//for each i in universe array
		for (int i = 0; i < univ.size(); i++) {
			//if universe position on Y axis is greater than height
			if(univ.get(i).posY > height)
				//remove 
				univ.remove(i);
		}
	}

	//player Ninja
	public class Ninja {
		//declare variables
		int posX, posY, size;
		boolean exploding, destroyed;
		Image img;
		int explosionStep = 0; //set explosion steps = 0
		
		//constructor
		public Ninja(int posX, int posY, int size,  Image image) {
			this.posX = posX;
			this.posY = posY;
			this.size = size;
			img = image;
		}
		
		//shoot method
		public Shot shoot() {
			//return new shot position
			return new Shot(posX + size / 2 - Shot.size / 2, posY - Shot.size);
		}
		
		//update method
		public void update() {
			//if exploding, add 1 to explosion step
			if(exploding) explosionStep++;
				destroyed = explosionStep > explosionSTEPS;
		}
		
		//draw method
		public void draw() {
			//if exploding
			if(exploding) {
				//draw explosion to expand and then get smaller/ disappear 
				gc.drawImage(explosion, explosionStep % explosionCols * explosionWidth, (explosionStep / explosionRows) * explosionHeight + 1,
						explosionWidth, explosionHeight,
						posX, posY, size, size);
			}
			else {
				//else draw image
				gc.drawImage(img, posX, posY, size, size);
			}
		}
		
		//collide method
		public boolean collide(Ninja other) {
			//set distance 
			int d = distance(this.posX + size / 2, this.posY + size /2, 
							other.posX + other.size / 2, other.posY + other.size / 2);
			//return distance size
			return d < other.size / 2 + this.size / 2 ;
		}
		
		//explode method
		public void explode() {
			//if exploding = true, decrease explosionStep by 1
			exploding = true;
			explosionStep = -1;
		}

	}
		
	//invaders
	public class invader extends Ninja {
		//set speed based on score
		int speed = (score/5)+2;
		
		//constructor
		public invader(int posX, int posY, int size, Image image) {
			super(posX, posY, size, image);
		}
		
		//call method update
		public void update() {
			super.update();
			//if not exploding and not destroyed, increase position Y and speed
			if(!exploding && !destroyed) posY += speed;
			//destroy if reaches the bottom
			if(posY > height) destroyed = true;
		}
	}

	//ninja laser
	public class Shot {
		
		//declare variables
		public boolean toRemove;

		int posX, posY, speed = 10;
		static final int size = 6;
		
		//constructor
		public Shot(int posX, int posY) {
			this.posX = posX;
			this.posY = posY;
		}
		
		//call method update
		public void update() {
			//position of Y axis -= speed of shot
			posY-=speed;
		}
		
		//call method draw (POWER MODE)
		public void draw() {
			//laser shot graphics
			gc.setFill(Color.RED);
			//if score is between 50 and 70 or if score is greater or = to 100
			if (score >=50 && score<=70 || score>=100) {
				//set laser color different
				gc.setFill(Color.YELLOWGREEN);
				//increase speed
				speed = 50;
				//make laser shots larger
				gc.fillRect(posX-5, posY-10, size+10, size+30);
			} else {
				//else fill normally
				gc.fillOval(posX, posY, size, size);
			}
		}
		
		//call method collide 
		public boolean collide(Ninja Ninja) {
			//declare and set distance of ninja
			int distance = distance(this.posX + size / 2, this.posY + size / 2, 
					Ninja.posX + Ninja.size / 2, Ninja.posY + Ninja.size / 2);
			return distance  < Ninja.size / 2 + size / 2;
		} 
		
	}
		
	//environment of game, universe
	public class Universe {
		//declare variables
		int posX, posY;
		private double opacity;
		private int h, w, b, g, r;
		
		//constructor
		public Universe() {
			//set position to be random
			posX = rand.nextInt(width);
			posY = 0;
			w = rand.nextInt(5) + 1; //width
			h =  rand.nextInt(5) + 1;  //height
			b = rand.nextInt(100) + 150; //blue
			g = rand.nextInt(100) + 150; //green
			r = rand.nextInt(100) + 150; //red
			//set opacity to random
			opacity = rand.nextFloat();
			//if opacity is less than 0, mult value by -1
			if(opacity < 0) {
				opacity *= -1;
			}
			//if opacity is greater than 0.5, set to 0.5
			if(opacity > 0.5) {
				opacity = 0.5;
			}
		}
		
		//call method draw
		public void draw() {
			//
			if(opacity > 0.8) {
				opacity -= 0.01;
			}
			if(opacity < 0.1) {
				opacity+=0.01;
			}
			//set graphics
			gc.setFill(Color.rgb(r, g, b, opacity));
			gc.fillOval(posX, posY, w, h);
			posY+=20; //position on Y axis
		}
	}
		
	//new invader position
	invader newinvader() {
		return new invader(50 + rand.nextInt(width - 100), 0, SizePlayer, invadersImg[rand.nextInt(invadersImg.length)]);
	}
	
	//new distance 
	int distance(int x1, int y1, int x2, int y2) {
		return (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}
	
	//launch (move to main later)
	public static void main(String[] args) {
		launch();
	}
}
