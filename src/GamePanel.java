import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
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
    private static final int BUTTON_WIDTH = 240;
    private static final int BUTTON_HEIGHT = 42;
    private static final int BUTTON_GAP = 14;
    private static final int EATING_ANIMATION_LAST_FRAME = 4;
    private static final String[] IMAGE_EXTENSIONS = {"png", "jpg", "jpeg"};

    private final Random random = new Random();
    private final List<Point> snake = new ArrayList<>();
    private final Timer timer;
    private final List<MenuButton> activeButtons = new ArrayList<>();
    private final BufferedImage snakeHeadImage = loadImage("SnakeHead");
    private final BufferedImage snakeBodyImage = loadImage("SnakeBody");
    private final BufferedImage snakeAppleImage = loadImage("SnakeApple");
    private final BufferedImage[] eatingHeadImages = {
        snakeHeadImage,
        loadImage("SnakeHead2"),
        loadImage("SnakeHead3"),
        loadImage("SnakeHead4"),
        snakeHeadImage
    };

    private Screen screen = Screen.MENU;
    private Direction direction = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private Direction waitingForDirection;
    private Point food;
    private boolean running;
    private boolean gameOver;
    private boolean eatingAnimationActive;
    private int eatingAnimationFrame;
    private int score;
    private int selectedIndex;
    private String settingsMessage = "選擇一個方向後，按下新的控制鍵。";

    private int upKey = KeyEvent.VK_UP;
    private int downKey = KeyEvent.VK_DOWN;
    private int leftKey = KeyEvent.VK_LEFT;
    private int rightKey = KeyEvent.VK_RIGHT;

    public GamePanel() {
        setPreferredSize(new Dimension(BOARD_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(18, 22, 26));
        setFocusable(true);
        addKeyListener(new SnakeKeyAdapter());
        addMouseListener(new SnakeMouseAdapter());

        timer = new Timer(DELAY, event -> {
            if (running) {
                if (eatingAnimationActive) {
                    updateEatingAnimation();
                } else {
                    move();
                    checkCollision();
                }
                repaint();
            }
        });
        timer.start();

        resetSnake();
    }

    private void startGame() {
        resetSnake();
        screen = Screen.PLAYING;
        running = true;
        gameOver = false;
        selectedIndex = 0;
        spawnFood();
        requestFocusInWindow();
        repaint();
    }

    private void showMenu() {
        resetSnake();
        screen = Screen.MENU;
        running = false;
        gameOver = false;
        selectedIndex = 0;
        settingsMessage = "選擇一個方向後，按下新的控制鍵。";
        requestFocusInWindow();
        repaint();
    }

    private void showTutorial() {
        screen = Screen.TUTORIAL;
        running = false;
        selectedIndex = 0;
        requestFocusInWindow();
        repaint();
    }

    private void showSettings() {
        screen = Screen.SETTINGS;
        running = false;
        waitingForDirection = null;
        selectedIndex = 0;
        settingsMessage = "選擇一個方向後，按下新的控制鍵。";
        requestFocusInWindow();
        repaint();
    }

    private void showGameOver() {
        screen = Screen.GAME_OVER;
        running = false;
        gameOver = true;
        selectedIndex = 0;
        requestFocusInWindow();
        repaint();
    }

    private void resetSnake() {
        snake.clear();
        int startX = COLS / 2;
        int startY = ROWS / 2;
        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - 1, startY));
        snake.add(new Point(startX - 2, startY));

        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        score = 0;
        food = null;
        eatingAnimationActive = false;
        eatingAnimationFrame = 0;
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
            startEatingAnimation();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void startEatingAnimation() {
        eatingAnimationActive = true;
        eatingAnimationFrame = 0;
    }

    private void updateEatingAnimation() {
        eatingAnimationFrame++;
        if (eatingAnimationFrame >= EATING_ANIMATION_LAST_FRAME) {
            eatingAnimationActive = false;
            eatingAnimationFrame = 0;
            score += 10;
            spawnFood();
        }
    }

    private void checkCollision() {
        Point head = snake.get(0);

        boolean hitWall = head.x < 0 || head.x >= COLS || head.y < 0 || head.y >= ROWS;
        if (hitWall || hitBody(head)) {
            showGameOver();
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
            showGameOver();
            return;
        }

        food = emptyCells.get(random.nextInt(emptyCells.size()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        activeButtons.clear();

        switch (screen) {
            case MENU:
                drawMenu(g);
                break;
            case TUTORIAL:
                drawTutorial(g);
                break;
            case SETTINGS:
                drawSettings(g);
                break;
            case PLAYING:
                drawGame(g);
                break;
            case GAME_OVER:
                drawGame(g);
                drawGameOver(g);
                break;
            default:
                break;
        }
    }

    private void drawMenu(Graphics g) {
        fillBackground(g);
        drawTitle(g, "貪吃蛇遊戲", 92);
        drawSubtitle(g, "Java Swing Snake Game", 124);

        String[] options = {"開始遊戲", "遊戲教學", "設定", "退出遊戲"};
        drawOptionButtons(g, options, 165);
    }

    private void drawTutorial(Graphics g) {
        fillBackground(g);
        drawTitle(g, "遊戲教學", 60);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        int y = 116;
        drawTextLine(g, "操作方式：", 90, y);
        drawTextLine(g, "上：" + keyName(upKey) + "    下：" + keyName(downKey), 110, y + 34);
        drawTextLine(g, "左：" + keyName(leftKey) + "    右：" + keyName(rightKey), 110, y + 68);
        drawTextLine(g, "規則：", 90, y + 120);
        drawTextLine(g, "吃到紅色食物可獲得 10 分並讓蛇變長。", 110, y + 154);
        drawTextLine(g, "撞到牆壁或自己的身體時，遊戲結束。", 110, y + 188);
        drawTextLine(g, "蛇不能直接往相反方向移動。", 110, y + 222);

        drawSingleButton(g, "返回選單", 360, 0);
    }

    private void drawSettings(Graphics g) {
        fillBackground(g);
        drawTitle(g, "設定", 58);
        drawSubtitle(g, settingsMessage, 90);

        String[] options = {
            "向上：" + keyName(upKey),
            "向下：" + keyName(downKey),
            "向左：" + keyName(leftKey),
            "向右：" + keyName(rightKey),
            "恢復預設",
            "返回選單"
        };
        drawOptionButtons(g, options, 125);
    }

    private void drawGame(Graphics g) {
        drawScoreBar(g);
        drawBoard(g);
        drawFood(g);
        drawSnake(g);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, SCORE_BAR_HEIGHT, BOARD_WIDTH, BOARD_HEIGHT);

        drawTitle(g, "Game Over", 148);
        drawSubtitle(g, "分數：" + score, 184);
        String[] options = {"重新開始", "退出"};
        drawOptionButtons(g, options, 220);
    }

    private void fillBackground(Graphics g) {
        g.setColor(new Color(18, 22, 26));
        g.fillRect(0, 0, BOARD_WIDTH, PANEL_HEIGHT);
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

        int x = food.x * CELL_SIZE;
        int y = food.y * CELL_SIZE + SCORE_BAR_HEIGHT;
        if (snakeAppleImage != null) {
            drawCellImage(g, snakeAppleImage, x, y);
        } else {
            g.setColor(new Color(230, 65, 65));
            g.fillOval(x + 3, y + 3, CELL_SIZE - 6, CELL_SIZE - 6);
        }
    }

    private void drawSnake(Graphics g) {
        for (int i = 0; i < snake.size(); i++) {
            Point part = snake.get(i);
            int x = part.x * CELL_SIZE;
            int y = part.y * CELL_SIZE + SCORE_BAR_HEIGHT;

            if (i == 0) {
                Image headImage = currentHeadImage();
                if (headImage != null) {
                    drawHeadImage(g, headImage, x, y);
                    continue;
                }
                g.setColor(new Color(100, 235, 120));
            } else {
                if (snakeBodyImage != null) {
                    drawCellImage(g, snakeBodyImage, x, y);
                    continue;
                }
                g.setColor(new Color(45, 180, 85));
            }
            g.fillRoundRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, 6, 6);
        }
    }

    private Image currentHeadImage() {
        if (!eatingAnimationActive) {
            return snakeHeadImage;
        }
        return eatingHeadImages[eatingAnimationFrame];
    }

    private void drawCellImage(Graphics g, Image image, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(image, x, y, CELL_SIZE, CELL_SIZE, null);
        g2.dispose();
    }

    private void drawHeadImage(Graphics g, Image image, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.rotate(rotationAngle(direction), x + CELL_SIZE / 2.0, y + CELL_SIZE / 2.0);
        g2.drawImage(image, x, y, CELL_SIZE, CELL_SIZE, null);
        g2.dispose();
    }

    private double rotationAngle(Direction imageDirection) {
        switch (imageDirection) {
            case UP:
                return -Math.PI / 2;
            case DOWN:
                return Math.PI / 2;
            case LEFT:
                return Math.PI;
            case RIGHT:
            default:
                return 0;
        }
    }

    private BufferedImage loadImage(String imageName) {
        for (String extension : IMAGE_EXTENSIONS) {
            BufferedImage image = loadImageFile(imageName + "." + extension);
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    private BufferedImage loadImageFile(String fileName) {
        File imageFile = new File(fileName);
        if (!imageFile.isFile()) {
            return null;
        }

        try {
            return ImageIO.read(imageFile);
        } catch (IOException exception) {
            System.err.println("無法載入圖片：" + fileName);
            return null;
        }
    }

    private void drawTitle(Graphics g, String title, int y) {
        g.setColor(new Color(100, 235, 120));
        g.setFont(new Font("SansSerif", Font.BOLD, 34));
        drawCenteredText(g, title, y);
    }

    private void drawSubtitle(Graphics g, String text, int y) {
        g.setColor(new Color(210, 218, 226));
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        drawCenteredText(g, text, y);
    }

    private void drawOptionButtons(Graphics g, String[] options, int startY) {
        int x = (BOARD_WIDTH - BUTTON_WIDTH) / 2;
        for (int i = 0; i < options.length; i++) {
            int y = startY + i * (BUTTON_HEIGHT + BUTTON_GAP);
            drawButton(g, options[i], new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT), i == selectedIndex);
            activeButtons.add(new MenuButton(new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT), i));
        }
    }

    private void drawSingleButton(Graphics g, String label, int y, int index) {
        int x = (BOARD_WIDTH - BUTTON_WIDTH) / 2;
        drawButton(g, label, new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT), index == selectedIndex);
        activeButtons.add(new MenuButton(new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT), index));
    }

    private void drawButton(Graphics g, String label, Rectangle bounds, boolean selected) {
        if (selected) {
            g.setColor(new Color(67, 140, 88));
        } else {
            g.setColor(new Color(34, 43, 52));
        }
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);

        g.setColor(selected ? Color.WHITE : new Color(224, 230, 235));
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics metrics = g.getFontMetrics();
        int textX = bounds.x + (bounds.width - metrics.stringWidth(label)) / 2;
        int textY = bounds.y + (bounds.height - metrics.getHeight()) / 2 + metrics.getAscent();
        g.drawString(label, textX, textY);
    }

    private void drawCenteredText(Graphics g, String text, int y) {
        FontMetrics metrics = g.getFontMetrics();
        int x = (BOARD_WIDTH - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private void drawTextLine(Graphics g, String text, int x, int y) {
        g.drawString(text, x, y);
    }

    private String keyName(int keyCode) {
        return KeyEvent.getKeyText(keyCode);
    }

    private void chooseSelectedOption() {
        switch (screen) {
            case MENU:
                chooseMenuOption(selectedIndex);
                break;
            case TUTORIAL:
                showMenu();
                break;
            case SETTINGS:
                chooseSettingsOption(selectedIndex);
                break;
            case GAME_OVER:
                chooseGameOverOption(selectedIndex);
                break;
            default:
                break;
        }
    }

    private void chooseMenuOption(int option) {
        if (option == 0) {
            startGame();
        } else if (option == 1) {
            showTutorial();
        } else if (option == 2) {
            showSettings();
        } else if (option == 3) {
            System.exit(0);
        }
    }

    private void chooseSettingsOption(int option) {
        if (option >= 0 && option <= 3) {
            waitingForDirection = Direction.values()[option];
            settingsMessage = "請按下新的「" + directionLabel(waitingForDirection) + "」控制鍵。Esc 可取消。";
        } else if (option == 4) {
            resetDefaultControls();
            settingsMessage = "已恢復預設方向鍵。";
        } else if (option == 5) {
            showMenu();
            return;
        }
        repaint();
    }

    private void chooseGameOverOption(int option) {
        if (option == 0) {
            startGame();
        } else if (option == 1) {
            showMenu();
        }
    }

    private void resetDefaultControls() {
        upKey = KeyEvent.VK_UP;
        downKey = KeyEvent.VK_DOWN;
        leftKey = KeyEvent.VK_LEFT;
        rightKey = KeyEvent.VK_RIGHT;
        waitingForDirection = null;
    }

    private String directionLabel(Direction selectedDirection) {
        switch (selectedDirection) {
            case UP:
                return "向上";
            case DOWN:
                return "向下";
            case LEFT:
                return "向左";
            case RIGHT:
                return "向右";
            default:
                return "";
        }
    }

    private void assignControlKey(int keyCode) {
        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            settingsMessage = "Enter 和 Space 保留給選單確認，請選其他按鍵。";
            repaint();
            return;
        }

        if (isKeyAlreadyUsed(keyCode)) {
            settingsMessage = keyName(keyCode) + " 已被使用，請選其他按鍵。";
            repaint();
            return;
        }

        switch (waitingForDirection) {
            case UP:
                upKey = keyCode;
                break;
            case DOWN:
                downKey = keyCode;
                break;
            case LEFT:
                leftKey = keyCode;
                break;
            case RIGHT:
                rightKey = keyCode;
                break;
            default:
                break;
        }

        settingsMessage = "已將「" + directionLabel(waitingForDirection) + "」設定為 " + keyName(keyCode) + "。";
        waitingForDirection = null;
        repaint();
    }

    private boolean isKeyAlreadyUsed(int keyCode) {
        return keyCode == upKey || keyCode == downKey || keyCode == leftKey || keyCode == rightKey;
    }

    private int optionCount() {
        switch (screen) {
            case MENU:
                return 4;
            case TUTORIAL:
                return 1;
            case SETTINGS:
                return 6;
            case GAME_OVER:
                return 2;
            default:
                return 0;
        }
    }

    private void moveSelection(int delta) {
        int count = optionCount();
        if (count == 0) {
            return;
        }
        selectedIndex = (selectedIndex + delta + count) % count;
        repaint();
    }

    private void handleGameControl(int key) {
        if (key == upKey && direction != Direction.DOWN) {
            nextDirection = Direction.UP;
        } else if (key == downKey && direction != Direction.UP) {
            nextDirection = Direction.DOWN;
        } else if (key == leftKey && direction != Direction.RIGHT) {
            nextDirection = Direction.LEFT;
        } else if (key == rightKey && direction != Direction.LEFT) {
            nextDirection = Direction.RIGHT;
        } else if (key == KeyEvent.VK_ESCAPE) {
            showMenu();
        }
    }

    private enum Screen {
        MENU,
        TUTORIAL,
        SETTINGS,
        PLAYING,
        GAME_OVER
    }

    private static final class MenuButton {
        private final Rectangle bounds;
        private final int optionIndex;

        private MenuButton(Rectangle bounds, int optionIndex) {
            this.bounds = bounds;
            this.optionIndex = optionIndex;
        }
    }

    private final class SnakeKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent event) {
            int key = event.getKeyCode();

            if (screen == Screen.PLAYING) {
                handleGameControl(key);
                return;
            }

            if (screen == Screen.SETTINGS && waitingForDirection != null) {
                if (key == KeyEvent.VK_ESCAPE) {
                    waitingForDirection = null;
                    settingsMessage = "已取消按鍵設定。";
                    repaint();
                } else {
                    assignControlKey(key);
                }
                return;
            }

            if (key == KeyEvent.VK_UP) {
                moveSelection(-1);
            } else if (key == KeyEvent.VK_DOWN) {
                moveSelection(1);
            } else if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
                chooseSelectedOption();
            } else if (key == KeyEvent.VK_ESCAPE) {
                showMenu();
            }
        }
    }

    private final class SnakeMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent event) {
            requestFocusInWindow();
            for (MenuButton button : activeButtons) {
                if (button.bounds.contains(event.getPoint())) {
                    selectedIndex = button.optionIndex;
                    chooseSelectedOption();
                    return;
                }
            }
        }
    }
}
