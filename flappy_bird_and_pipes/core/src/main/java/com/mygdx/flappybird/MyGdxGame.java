package com.mygdx.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MyGdxGame extends ApplicationAdapter {
    int puntuacion = 0;
    SpriteBatch batch;
    Texture background;
    Texture flappyBirdUp;
    Texture[] birds;
    Texture topTube, bottomTube;
    BitmapFont font;

    int flapState = 0;

    float birdY = 0;
    float velocity = 0;
    float gameState = 0;

    float gap = 500;

    Random randomGenerator;
    int totalTubes = 10;
    float[] tubeX = new float[totalTubes];
    float[] tubeOffset = new float[totalTubes];
    float distanceBetweenTubes;

    Circle birdCircle;
    Circle birdCircleScore;
    ShapeRenderer shapeRenderer;

    Rectangle[] topTubeRectangles;
    Rectangle[] bottomTubeRectangles;

    Rectangle[] tubeScore;

    Texture gameOver;

    @Override
    public void create() {
        font = new BitmapFont();
        batch = new SpriteBatch();
        background = new Texture("background.png");
        flappyBirdUp = new Texture("flappybirdup.png");
        birds = new Texture[2];
        birds[0] = new Texture("flappybirdup.png");
        birds[1] = new Texture("flappybirddown.png");

        topTube = new Texture("toptube.png");
        bottomTube = new Texture("bottomtube.png");

        birdCircle = new Circle();
        birdCircleScore = new Circle();
        shapeRenderer = new ShapeRenderer();

        birdY = Gdx.graphics.getHeight() / 2 - birds[0].getHeight() / 2;

        randomGenerator = new Random();

        distanceBetweenTubes = Gdx.graphics.getWidth() / 2;

        topTubeRectangles = new Rectangle[totalTubes];
        bottomTubeRectangles = new Rectangle[totalTubes];

        gameOver = new Texture("gameover.png");

        tubeScore = new Rectangle[totalTubes];
    }

    @Override
    public void render() {

        batch.begin();

        batch.draw(background,
            0,
            0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());


        if (gameState == 1) {

            if (Gdx.input.justTouched()) {
                velocity = -30;
            }

            for (int i = 0; i < totalTubes; i++) {

                if (tubeX[i] < -topTube.getWidth()) {
                    tubeX[i] += totalTubes * distanceBetweenTubes;
                    tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight()
                        - gap - 940);
                } else {
                    tubeX[i] -= 4 * 2;
                }

                batch.draw(topTube,
                    tubeX[i],
                    Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i]
                );

                batch.draw(bottomTube,
                    tubeX[i],
                    Gdx.graphics.getHeight() / 2 - gap / 2 - bottomTube.getHeight()
                        + tubeOffset[i]
                );

                topTubeRectangles[i] = new Rectangle(
                    tubeX[i],
                    Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i],
                    topTube.getWidth(),
                    topTube.getHeight()
                );

                bottomTubeRectangles[i] = new Rectangle(
                    tubeX[i],
                    Gdx.graphics.getHeight() / 2 - gap / 2 - bottomTube.getHeight()
                        + tubeOffset[i],
                    bottomTube.getWidth(),
                    bottomTube.getHeight()
                );

                tubeScore[i] = new Rectangle(
                    tubeX[i],
                    (Gdx.graphics.getHeight() / 2 - gap / 2 - bottomTube.getHeight()
                        + tubeOffset[i]) + bottomTube.getHeight(),
                    1,
                    gap
                );
            }

            if (birdY > 0 || velocity < 0) {
                velocity += 2;
                birdY -= velocity;
            } else {
                gameState = 2;
            }

        } else if (gameState == 0) {
            if (Gdx.input.justTouched()) {
                gameState = 1;
                StartGame();
            }
        } else if (gameState == 2) {
            batch.draw(gameOver,
                Gdx.graphics.getWidth() / 2 - gameOver.getWidth() / 2,
                Gdx.graphics.getWidth() / 2 - gameOver.getWidth() / 2
            );

            if (Gdx.input.justTouched()) {
                gameState = 1;
                velocity = 1;
                StartGame();
            }
        }

        if (flapState == 0) {
            flapState = 1;
        } else {
            flapState = 0;
        }

        batch.draw(birds[flapState],
            Gdx.graphics.getWidth() / 2 - birds[flapState].getWidth() / 2,
            birdY);


        birdCircle.set(
            Gdx.graphics.getWidth() / 2,
            birdY + birds[flapState].getHeight() / 2,
            birds[flapState].getWidth() / 2
        );

        birdCircleScore.set(
            Gdx.graphics.getWidth() / 2,
            birdY + birds[flapState].getHeight() / 2,
            0.5f
        );

        for (int i = 0; i < totalTubes; i++) {
            if (birdCircle != null && topTubeRectangles[i] != null
                && bottomTubeRectangles[i] != null && tubeScore[i] != null) {
                //Colisión
                if (Intersector.overlaps(birdCircle, topTubeRectangles[i])
                    || Intersector.overlaps(birdCircle, bottomTubeRectangles[i])) {
                    gameState = 2;
                }

                //puntuacion
                if (Intersector.overlaps(birdCircleScore, tubeScore[i])) {
                    puntuacion++;
                }
            }
        }

        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        font.getData().setScale(6, 6);
        font.draw(batch, "Puntuación: " +  puntuacion, 40, Gdx.graphics.getHeight() - 40);


        batch.end();
    }

    public void StartGame() {
        birdY = Gdx.graphics.getHeight() / 2 - birds[0].getHeight() / 2;

        for (int i = 0; i < totalTubes; i++) {
            tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f)
                * (Gdx.graphics.getHeight() - gap - 940);

            tubeX[i] = Gdx.graphics.getWidth() / 2
                - topTube.getWidth() / 2
                + Gdx.graphics.getWidth()
                + i * distanceBetweenTubes;

            topTubeRectangles[i] = new Rectangle();
            bottomTubeRectangles[i] = new Rectangle();
            puntuacion  = 0;
        }
    }
}
//Gdx.graphics.getHeight()<pajaro = game over
//clase para que muestre numeros cada vez que colisiona con una tuberia (sin parar el juego)
