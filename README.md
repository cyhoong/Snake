# Java Swing Snake Game

A simple Snake game built with Java Swing. Use the arrow keys to guide the snake, eat food to grow, and avoid hitting the wall or yourself.

## Features

- Main menu with start, tutorial, settings, and exit options
- Classic grid-based Snake gameplay
- Java Swing desktop window
- Customizable keyboard controls
- Score display
- Food spawning that avoids the snake body
- Game over detection for wall and self collisions
- Restart or return to the menu after Game Over
- No external dependencies

## Project Structure

```text
src/
  Direction.java
  GamePanel.java
  SnakeGame.java
```

## Requirements

- JDK 8 or newer

Check your Java installation:

```powershell
java -version
javac -version
```

## Compile

From the project root:

```powershell
javac src/*.java
```

## Run

```powershell
java -cp src SnakeGame
```

## Controls

The default controls are:

| Key | Action |
| --- | --- |
| Arrow Up | Move up |
| Arrow Down | Move down |
| Arrow Left | Move left |
| Arrow Right | Move right |
| Enter | Confirm menu option |
| Space | Confirm menu option |
| Esc | Return to menu while playing |

You can change the movement keys from the in-game settings menu.

## Game Rules

- Eat red food to gain 10 points.
- The snake grows after eating food.
- The game ends if the snake hits a wall.
- The game ends if the snake hits its own body.
- The snake cannot instantly reverse direction.

## Notes

This project uses only the Java standard library, so it can be opened directly in most Java IDEs or run from the command line.
