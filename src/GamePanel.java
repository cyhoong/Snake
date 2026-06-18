import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel {
    private static final int CELL_SIZE = 20;
    private static final int COLS = 30;
    private static final int ROWS = 20;
    private static final int SCORE_BAR_HEIGHT = 40;
    private static final int BOARD_WIDTH = COLS * CELL_SIZE;
    private static final int BOARD_HEIGHT = ROWS * CELL_SIZE;
    private static final int PANEL_HEIGHT = BOARD_HEIGHT + SCORE_BAR_HEIGHT;
    private static final int DELAY = 120;

    private final Random random = new Random();
    private final List<Point> snake = new ArrayList<>();
    private final Timer timer;

    private Direction direction = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private Point food;
    private boolean running;
    private boolean gameOver;
    private int score;

    public GamePanel() {
        setPreferredSize(new Dimension(BOARD_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(18, 22, 26));
        setFocusable(true);
        addKeyListener(new SnakeKeyAdapter());

        timer = new Timer(DELAY, event -> {
            if (running) {
                move();
                checkCollision();
                repaint();
            }
        });
        timer.start();

        resetToStartScreen();
    }

    private void resetToStartScreen() {
        snake.clear();
        int startX = COLS / 2;
        int startY = ROWS / 2;
        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - 1, startY));
        snake.add(new Point(startX - 2, startY));

        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        score = 0;
        gameOver = false;
        running = false;
        spawnFood();
        repaint();
    }

    private void startGame() {
        snake.clear();
        int startX = COLS / 2;
        int startY = ROWS / 2;
        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - 1, startY));
        snake.add(new Point(startX - 2, startY));

        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        score = 0;
        gameOver = false;
        running = true;
        spawnFood();
        repaint();
    }

    private void move() {
        direction = nextDirection;

        Point head = snake.get(0);
        Point newHead = new Point(head);

        switch (direction) {
            case UP:
                newHead.y--;
                break;
            case DOWN:
                newHead.y++;
                break;
            case LEFT:
                newHead.x--;
                break;
            case RIGHT:
                newHead.x++;
                break;
            default:
                break;
        }

        snake.add(0, newHead);

        if (newHead.equals(food)) {
            score += 10;
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void checkCollision() {
        Point head = snake.get(0);

        boolean hitWall = head.x < 0 || head.x >= COLS || head.y < 0 || head.y >= ROWS;
        if (hitWall || hitBody(head)) {
            running = false;
            gameOver = true;
        }
    }

    private boolean hitBody(Point head) {
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void spawnFood() {
        List<Point> emptyCells = new ArrayList<>();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Point candidate = new Point(col, row);
                if (!snake.contains(candidate)) {
                    emptyCells.add(candidate);
                }
            }
        }

        if (emptyCells.isEmpty()) {
            running = false;
            gameOver = true;
            return;
        }

        food = emptyCells.get(random.nextInt(emptyCells.size()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawScoreBar(g);
        drawBoard(g);
        drawFood(g);
        drawSnake(g);

        if (!running && !gameOver) {
            drawCenteredMessage(g, "Press Enter or Space to Start", 0);
        } else if (gameOver) {
            drawCenteredMessage(g, "Game Over", -18);
            drawCenteredMessage(g, "Press Enter or Space to Restart", 18);
        }
    }

    private void drawScoreBar(Graphics g) {
        g.setColor(new Color(11, 14, 18));
        g.fillRect(0, 0, BOARD_WIDTH, SCORE_BAR_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Score: " + score, 16, 26);
    }

    private void drawBoard(Graphics g) {
        g.setColor(new Color(18, 22, 26));
        g.fillRect(0, SCORE_BAR_HEIGHT, BOARD_WIDTH, BOARD_HEIGHT);
    }

    private void drawFood(Graphics g) {
        if (food == null) {
            return;
        }

        g.setColor(new Color(230, 65, 65));
        int x = food.x * CELL_SIZE;
        int y = food.y * CELL_SIZE + SCORE_BAR_HEIGHT;
        g.fillOval(x + 3, y + 3, CELL_SIZE - 6, CELL_SIZE - 6);
    }

    private void drawSnake(Graphics g) {
        for (int i = 0; i < snake.size(); i++) {
            Point part = snake.get(i);
            int x = part.x * CELL_SIZE;
            int y = part.y * CELL_SIZE + SCORE_BAR_HEIGHT;

            if (i == 0) {
                g.setColor(new Color(100, 235, 120));
            } else {
                g.setColor(new Color(45, 180, 85));
            }
            g.fillRoundRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, 6, 6);
        }
    }

    private void drawCenteredMessage(Graphics g, String message, int yOffset) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));

        FontMetrics metrics = g.getFontMetrics();
        int x = (BOARD_WIDTH - metrics.stringWidth(message)) / 2;
        int y = SCORE_BAR_HEIGHT + (BOARD_HEIGHT - metrics.getHeight()) / 2 + metrics.getAscent() + yOffset;
        g.drawString(message, x, y);
    }

    private final class SnakeKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent event) {
            int key = event.getKeyCode();

            if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                if (!running) {
                    startGame();
                }
                return;
            }

            if (!running) {
                return;
            }

            if (key == KeyEvent.VK_UP && direction != Direction.DOWN) {
                nextDirection = Direction.UP;
            } else if (key == KeyEvent.VK_DOWN && direction != Direction.UP) {
                nextDirection = Direction.DOWN;
            } else if (key == KeyEvent.VK_LEFT && direction != Direction.RIGHT) {
                nextDirection = Direction.LEFT;
            } else if (key == KeyEvent.VK_RIGHT && direction != Direction.LEFT) {
                nextDirection = Direction.RIGHT;
            }
        }
    }
}
