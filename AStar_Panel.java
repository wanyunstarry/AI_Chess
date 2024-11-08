package AStar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

//图形化界面
public class AStar_Panel extends JPanel {
    private static final long serialVersionUID = 667503661521167626L;
    private final int OFFSET = 40;// 棋盘偏移
    private final int CELL_WIDTH = 40;// 棋格宽度
    private int computerSide = AStar_Chess.BLACK;// 默认机器持黑
    private int humanSide = AStar_Chess.WHITE;
    private int cx = AStar_Board.CENTER, cy = AStar_Board.CENTER;
    private boolean isShowOrder = false;// 显示落子顺序
    private int[] lastStep;// 上一个落子点
    private AStar_Board bd;// 棋盘，重要
    private AStar_Brain br;// AI，重要
    public static final int MANUAL = 0;// 双人模式
    public static final int HALF = 1;// 人机模式
    public static final int AUTO = 2;// 双机模式
    public static final int EVAL = 3;// 估值函数
    public static final int TREE = 4;// 估值函数+搜索树
    private int mode;// 模式
    private int intel;// 智能
    private boolean isGameOver = true;
    private TextArea area;

    // 显示落子顺序
    public void troggleOrder() {
        isShowOrder = !isShowOrder;
        repaint();
    }

    // 悔棋
    public void undo() {
        Point p = bd.undo();
        lastStep[0] = p.x;
        lastStep[1] = p.y;
        repaint();
    }

    public AStar_Panel(TextArea area) {
        this.area = area;
        lastStep = new int[2];
        addMouseMotionListener(mouseMotionListener);
        addMouseListener(mouseListener);
        this.setBackground(Color.lightGray);
        setPreferredSize(new Dimension(650, 700));
        bd = new AStar_Board();
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        g2d.setStroke(new BasicStroke(2));
        g2d.setFont(new Font("April", Font.BOLD, 12));
        // 画棋盘
        drawBoard(g2d);
        // 画天元和星
        drawStar(g2d, AStar_Board.CENTER, AStar_Board.CENTER);
        drawStar(g2d, (AStar_Board.BOARD_SIZE + 1) / 4, (AStar_Board.BOARD_SIZE + 1) / 4);
        drawStar(g2d, (AStar_Board.BOARD_SIZE + 1) / 4,
                (AStar_Board.BOARD_SIZE + 1) * 3 / 4);
        drawStar(g2d, (AStar_Board.BOARD_SIZE + 1) * 3 / 4,
                (AStar_Board.BOARD_SIZE + 1) / 4);
        drawStar(g2d, (AStar_Board.BOARD_SIZE + 1) * 3 / 4,
                (AStar_Board.BOARD_SIZE + 1) * 3 / 4);

        // 画提示框
        drawCell(g2d, cx, cy, 0);

        if (!isGameOver) {
            // 画所有棋子
            for (int x = 1; x <= AStar_Board.BOARD_SIZE; ++x) {
                for (int y = 1; y <= AStar_Board.BOARD_SIZE; ++y) {
                    drawChess(g2d, x, y, bd.getData()[x][y].getSide());
                }
            }
            // 画顺序
            if (isShowOrder)
                drawOrder(g2d);
            else {
                if (lastStep[0] > 0 && lastStep[1] > 0) {
                    g2d.setColor(Color.RED);
                    g2d.fillRect((lastStep[0] - 1) * CELL_WIDTH + OFFSET
                                    - CELL_WIDTH / 10, (lastStep[1] - 1) * CELL_WIDTH
                                    + OFFSET - CELL_WIDTH / 10, CELL_WIDTH / 5,
                            CELL_WIDTH / 5);

                }
            }
        }
    }

    // 画棋盘
    private void drawBoard(Graphics g2d) {
        for (int x = 0; x < AStar_Board.BOARD_SIZE; ++x) {
            g2d.drawLine(x * CELL_WIDTH + OFFSET, OFFSET, x * CELL_WIDTH
                    + OFFSET, (AStar_Board.BOARD_SIZE - 1) * CELL_WIDTH + OFFSET);

        }
        for (int y = 0; y < AStar_Board.BOARD_SIZE; ++y) {
            g2d.drawLine(OFFSET, y * CELL_WIDTH + OFFSET,
                    (AStar_Board.BOARD_SIZE - 1) * CELL_WIDTH + OFFSET, y
                            * CELL_WIDTH + OFFSET);

        }
    }

    // 画天元和星
    private void drawStar(Graphics g2d, int cx, int cy) {
        g2d.fillOval((cx - 1) * CELL_WIDTH + OFFSET - 4, (cy - 1) * CELL_WIDTH
                + OFFSET - 4, 8, 8);
    }

    // 画棋子
    private void drawChess(Graphics g2d, int cx, int cy, int player) {
        if (player == 0)
            return;
        int size = CELL_WIDTH * 5 / 6;
        g2d.setColor(player == AStar_Chess.BLACK ? Color.BLACK : Color.WHITE);
        g2d.fillOval((cx - 1) * CELL_WIDTH + OFFSET - size / 2, (cy - 1)
                * CELL_WIDTH - size / 2 + OFFSET, size, size);
    }

    // 画预选框
    private void drawCell(Graphics g2d, int x, int y, int c) {// c 是style
        int length = CELL_WIDTH / 4;
        int xx = (x - 1) * CELL_WIDTH + OFFSET;
        int yy = (y - 1) * CELL_WIDTH + OFFSET;
        int x1, y1, x2, y2, x3, y3, x4, y4;
        x1 = x4 = xx - CELL_WIDTH / 2;
        x2 = x3 = xx + CELL_WIDTH / 2;
        y1 = y2 = yy - CELL_WIDTH / 2;
        y3 = y4 = yy + CELL_WIDTH / 2;
        g2d.setColor(Color.RED);
        g2d.drawLine(x1, y1, x1 + length, y1);
        g2d.drawLine(x1, y1, x1, y1 + length);
        g2d.drawLine(x2, y2, x2 - length, y2);
        g2d.drawLine(x2, y2, x2, y2 + length);
        g2d.drawLine(x3, y3, x3 - length, y3);
        g2d.drawLine(x3, y3, x3, y3 - length);
        g2d.drawLine(x4, y4, x4 + length, y4);
        g2d.drawLine(x4, y4, x4, y4 - length);
    }

    // 画落子顺序
    private void drawOrder(Graphics g2d) {
        int[][] history = bd.getHistory();
        if (history.length > 0) {
            g2d.setColor(Color.RED);
            for (int i = 0; i < history.length; i++) {
                int x = history[i][0];
                int y = history[i][1];
                String text = String.valueOf(i + 1);
                // 居中
                FontMetrics fm = g2d.getFontMetrics();
                int stringWidth = fm.stringWidth(text);
                int stringAscent = fm.getAscent();
                g2d.drawString(text, (x - 1) * CELL_WIDTH + OFFSET
                        - stringWidth / 2, (y - 1) * CELL_WIDTH + OFFSET
                        + stringAscent / 2);
            }
        }
    }
    public void resetGame() {
        bd.reset();
        area.setText("");
        lastStep[0] = lastStep[1] = AStar_Board.CENTER;
        isGameOver = true;
        repaint();
    }

    // 开始游戏
//    public void startGame(int mode, int intel, int level, int node) {
//        if (isGameOver) {
//            this.mode = mode;
//            this.intel = intel;
//            bd.reset();
//            area.setText("");
//            lastStep[0] = lastStep[1] = AStar_Board.CENTER;
//            br = new AStar_Brain(bd, level, node);
//            bd.start();
//            isGameOver = false;
//            JOptionPane.showMessageDialog(AStar_Panel.this, "Game starts！");
//            repaint();
//            if (mode == AUTO) {// 双机
//                Timer t = new Timer(true);
//                t.schedule(new ComputurTask(), 0, 500);
//            }
//        } else {
//            JOptionPane.showMessageDialog(AStar_Panel.this,
//                    "You already started！");
//        }
//    }
    public void startGame(int mode, int intel, int level, int node,int playerColor) {
        if (isGameOver) {

            this.mode = mode;
            this.intel = intel;
            bd.reset();
            area.setText("");
            lastStep[0] = lastStep[1] = AStar_Board.CENTER;
            br = new AStar_Brain(bd, level, node);// 初始化AI
            //bd.start();
            bd.start(playerColor,mode);
            if(mode!=AUTO){
                humanSide= playerColor;
                computerSide=playerColor==AStar_Chess.BLACK?AStar_Chess.WHITE:AStar_Chess.BLACK;
            }

            isGameOver = false;
            JOptionPane.showMessageDialog(AStar_Panel.this, "Game starts！");
            repaint();
            if (mode == AUTO) {// 双机
                Timer t = new Timer(true);
                t.schedule(new ComputurTask(), 0, 500);
            }
        }
        else
        {
            JOptionPane.showMessageDialog(AStar_Panel.this,
                    "You already started！");
        }
    }

    // 鼠标移动
    private MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
        public void mouseMoved(MouseEvent e) {
            int tx = Math.round((e.getX() - OFFSET) * 1.0f / CELL_WIDTH) + 1;
            int ty = Math.round((e.getY() - OFFSET) * 1.0f / CELL_WIDTH) + 1;
            if (tx != cx || ty != cy) {
                if (tx >= 1 && tx <= AStar_Board.BOARD_SIZE && ty >= 1
                        && ty <= AStar_Board.BOARD_SIZE) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    repaint();
                } else
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                cx = tx;
                cy = ty;
            }
        }
    };

    // 鼠标点击
    private MouseListener mouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (isGameOver) {
                JOptionPane.showMessageDialog(AStar_Panel.this, "Please start the new game!");
                return;
            }
            int x = Math.round((e.getX() - OFFSET) * 1.0f / CELL_WIDTH) + 1;
            int y = Math.round((e.getY() - OFFSET) * 1.0f / CELL_WIDTH) + 1;
            if (cx >= 1 && cx <= AStar_Board.BOARD_SIZE && cy >= 1
                    && cy <= AStar_Board.BOARD_SIZE) {
                if (mode == MANUAL) {// 双人
                    int mods = e.getModifiers();
                    if ((mods & InputEvent.BUTTON3_MASK) != 0) {// 鼠标右键
                        area.append(bd.getData()[x][y].getDetail());
                    } else if ((mods & InputEvent.BUTTON1_MASK) != 0)// 鼠标左键
                        putChess(x, y);
                } else if (mode == HALF) {// 人机
                    if (bd.getPlayer() == humanSide) {
                        int mods = e.getModifiers();
                        if ((mods & InputEvent.BUTTON3_MASK) != 0) {// 鼠标右键
                            area.append(bd.getData()[x][y].getDetail());
                        } else if ((mods & InputEvent.BUTTON1_MASK) != 0) {// 鼠标左键
                            if (putChess(x, y)) {
                                System.out.println("\n----白棋完毕----");
                                if (intel == EVAL) {
                                    int[] bestStep = br.findOneBestStep();// 估值函数AI
                                    putChess(bestStep[0], bestStep[1]);
                                } else if (intel == TREE) {
                                    int[] bestStep = br.findTreeBestStep();// 估值函数+搜索树AI
                                    putChess(bestStep[0], bestStep[1]);
                                }
                                System.out.println("\n----黑棋完毕----");
                            }
                        }
                    }
                }
            }
        }
    };

    private boolean putChess(int x, int y) {
        if (bd.putChess(x, y)) {
            lastStep[0] = x;// 保存上一步落子点
            lastStep[1] = y;
            repaint();
            int winSide = bd.isGameOver();// 判断终局
            if (winSide > 0) {
                if (winSide == humanSide) {
                    JOptionPane.showMessageDialog(AStar_Panel.this, "The "+(humanSide==AStar_Chess.BLACK?"Black":"White")+" wins！");
                } else if (winSide == computerSide) {
                    JOptionPane.showMessageDialog(AStar_Panel.this, "The "+(computerSide==AStar_Chess.BLACK?"Black":"White")+" wins！");
                } else {
                    JOptionPane.showMessageDialog(AStar_Panel.this, "Draw");
                }

                // 清除
//                bd.reset();
//                area.setText("");
//                isGameOver = true;
//                repaint();
                resetGame(); // 调用重置游戏状态的方法
                return false;
            }

            return true;
        }
        return false;

    }

    // 双机
    private class ComputurTask extends TimerTask {
        @Override
        public void run() {
            int[] bestStep = br.findTreeBestStep();
            if (!putChess(bestStep[0], bestStep[1]))
                this.cancel();
        }

    }
}

