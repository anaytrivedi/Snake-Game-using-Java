import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class SnakeEaterGame extends JPanel implements ActionListener {
    private static final int TILE_SIZE = 25;
    private static final int WIDTH = 30;
    private static final int HEIGHT = 20;
    private static final int DELAY = 150;

    private final LinkedList<Point> snake = new LinkedList<>();
    private Point food;
    private String direction = "RIGHT";
    private boolean running;
    private boolean gameStarted;
    private int score;
    private int highScore = 0;
    private Color snakeBodyColor = Color.GREEN;

    private Timer timer;
    private Image bananaImage;
    private Image lipsImage;
    private JButton playButton;

    public SnakeEaterGame() {
        setPreferredSize(new Dimension(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE));
        setBackground(Color.BLACK);
        setLayout(null); // Use absolute positioning

        // Load images
        bananaImage = new ImageIcon("banana.png").getImage();
        lipsImage = new ImageIcon("lips.jpg").getImage(); // Load lips image

        initializePlayButton();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gameStarted) {
                    if (e.getKeyCode() == KeyEvent.VK_S) {
                        startGame();
                    }
                } else {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP:
                            if (!direction.equals("DOWN")) direction = "UP";
                            break;
                        case KeyEvent.VK_DOWN:
                            if (!direction.equals("UP")) direction = "DOWN";
                            break;
                        case KeyEvent.VK_LEFT:
                            if (!direction.equals("RIGHT")) direction = "LEFT";
                            break;
                        case KeyEvent.VK_RIGHT:
                            if (!direction.equals("LEFT")) direction = "RIGHT";
                            break;
                        case KeyEvent.VK_R:
                            initializeGame();
                            break;
                    }
                }
            }
        });

        initializeGame();
    }

    private void initializePlayButton() {
        playButton = new JButton("Play Now");
        playButton.setFont(new Font("Arial", Font.BOLD, 24));
        playButton.setBounds(WIDTH * TILE_SIZE / 2 - 100, HEIGHT * TILE_SIZE / 2 - 30, 200, 60);
        playButton.setFocusPainted(false);
        playButton.setBackground(Color.BLUE);
        playButton.setForeground(Color.BLACK);
        playButton.addActionListener(e -> {
            remove(playButton);
            revalidate();
            repaint();
            startGame();
        });
        add(playButton);
    }

    private void initializeGame() {
        snake.clear();
        snake.add(new Point(WIDTH / 2, HEIGHT / 2));
        generateFood();
        direction = "RIGHT";
        running = false;
        gameStarted = false;
        score = 0;
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(DELAY, this);
        repaint(); // Ensure screen is updated immediately
    }

    private void startGame() {
        requestFocusInWindow();
        running = true;
        gameStarted = true;
        timer.start();
    }

    private void generateFood() {
        do {
            food = new Point((int) (Math.random() * WIDTH), (int) (Math.random() * HEIGHT));
        } while (snake.contains(food));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!gameStarted) {
            drawStartScreen(g);
        } else if (running) {
            drawGame(g);
        } else {
            drawGameOverScreen(g);
        }
    }

    private void drawStartScreen(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String message = "HELLO PLAYER :)";
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(message, (getWidth() - metrics.stringWidth(message)) / 2, getHeight() / 2 + 80);
    }

    private void drawGame(Graphics g) {
        // Draw banana image as food (scaled)
        int bananaSize = TILE_SIZE * 2; // Make the banana twice the tile size
        g.drawImage(bananaImage,
                food.x * TILE_SIZE - TILE_SIZE / 2,
                food.y * TILE_SIZE - TILE_SIZE / 2,
                bananaSize, bananaSize, this);

        // Draw snake
        for (int i = 0; i < snake.size(); i++) {
            Point point = snake.get(i);
            if (i == 0) {
                // Draw head as lips image
                g.drawImage(lipsImage, point.x * TILE_SIZE, point.y * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
            } else {
                g.setColor(snakeBodyColor);
                g.fillRect(point.x * TILE_SIZE, point.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw score and high score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("High Score: " + highScore, 150, 20);
    }

    private void drawGameOverScreen(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String message = "Game Over! Score: " + score + ". Press R to Restart.";
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(message, (getWidth() - metrics.stringWidth(message)) / 2, getHeight() / 2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkCollision();
            checkFood();
        }
        repaint();
    }

    private void move() {
        Point head = snake.getFirst();
        Point newHead = switch (direction) {
            case "UP" -> new Point(head.x, (head.y - 1 + HEIGHT) % HEIGHT);
            case "DOWN" -> new Point(head.x, (head.y + 1) % HEIGHT);
            case "LEFT" -> new Point((head.x - 1 + WIDTH) % WIDTH, head.y);
            case "RIGHT" -> new Point((head.x + 1) % WIDTH, head.y);
            default -> head;
        };
        snake.addFirst(newHead);
        snake.removeLast();
    }

    private void checkFood() {
        if (snake.getFirst().equals(food)) {
            snake.addLast(new Point(snake.getLast())); // Extend snake
            score += 50; // Increase score
            snakeBodyColor = getRandomColor(); // Change body color
            generateFood();
        }
    }

    private Color getRandomColor() {
        Random random = new Random();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private void checkCollision() {
        Point head = snake.getFirst();
        if (snake.subList(1, snake.size()).contains(head)) {
            running = false;
            timer.stop();
            updateHighScore();
        }
    }

    private void updateHighScore() {
        if (score > highScore) {
            highScore = score;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Eater Game");
        SnakeEaterGame game = new SnakeEaterGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
