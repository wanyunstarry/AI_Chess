package AStar;

import java.awt.*;
import java.util.Arrays;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AStar_Board {// 棋盘类
    // 棋型信息
    public static enum Level {
        CON_5("长连", 0, new String[] { "11111", "22222" }, 100000), ALIVE_4(
                "活四", 1, new String[] { "011110", "022220" }, 10000), GO_4(
                "冲四", 2, new String[] { "011112|0101110|0110110",
                "022221|0202220|0220220" }, 500), DEAD_4("死四", 3,
                new String[] { "211112", "122221" }, -5), ALIVE_3("活三", 4,
                new String[] { "01110|010110", "02220|020220" }, 200), SLEEP_3(
                "眠三", 5, new String[] {
                "001112|010112|011012|10011|10101|2011102",
                "002221|020221|022021|20022|20202|1022201" }, 50), DEAD_3(
                "死三", 6, new String[] { "21112", "12221" }, -5), ALIVE_2("活二",
                7, new String[] { "00110|01010|010010", "00220|02020|020020" },
                5), SLEEP_2("眠二", 8, new String[] {
                "000112|001012|010012|10001|2010102|2011002",
                "000221|002021|020021|20002|1020201|1022001" }, 3), DEAD_2(
                "死二", 9, new String[] { "2112", "1221" }, -5), NULL("null", 10,
                new String[] { "", "" }, 0);
        private String name;
        private int index;
        private String[] regex;// 正则表达式
        int score;// 分值

        // 构造方法
        private Level(String name, int index, String[] regex, int score) {
            this.name = name;
            this.index = index;
            this.regex = regex;
            this.score = score;
        }

        // 覆盖方法
        @Override
        public String toString() {
            return this.name;
        }
    };

    // 方向
    private static enum Direction {
        HENG, SHU, PIE, NA
    };

    // 位置分
    private static int[][] position = {
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
            { 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0 },
            { 0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 1, 0 },
            { 0, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 3, 2, 1, 0 },
            { 0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0 },
            { 0, 1, 2, 3, 4, 5, 6, 6, 6, 5, 4, 3, 2, 1, 0 },
            { 0, 1, 2, 3, 4, 5, 6, 7, 6, 5, 4, 3, 2, 1, 0 },
            { 0, 1, 2, 3, 4, 5, 6, 6, 6, 5, 4, 3, 2, 1, 0 },
            { 0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0 },
            { 0, 1, 2, 3, 4, 4, 4, 4, 4, 4, 4, 3, 2, 1, 0 },
            { 0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 1, 0 },
            { 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0 },
            { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };
    public static final int BOARD_SIZE = 15;// 棋盘格数
    public static final int BT = BOARD_SIZE + 2;
    public static final int CENTER = BOARD_SIZE / 2 + 1;// 中心点
    private int minx, maxx, miny, maxy; // 当前棋局下所有棋子的最小x，最大x，最小y，最大y，用于缩小搜索落子点的范围
    private int currentPlayer = 0;// 当前玩家
    private Stack<Point> history;// 落子历史记录
    private AStar_Chess[][] data;// 1-15
    private AStar_Chess[] sorted;// 根据各点的落子估值从大到小排序的数组

    public AStar_Board() {
        data = new AStar_Chess[BT][BT];
        for (int i = 0; i < BT; i++)
            for (int j = 0; j < BT; j++) {
                data[i][j] = new AStar_Chess(i, j);
                if (i == 0 || i == BT - 1 || j == 0 || j == BT - 1)
                    data[i][j].setSide(AStar_Chess.BORDER);// 边界
            }

        history = new Stack<Point>();
    }

    public AStar_Board(AStar_Board b) {// 深度拷贝
        AStar_Chess[][] b_data = b.getData();
        AStar_Chess[] b_sorted = b.getSorted();
        data = new AStar_Chess[BT][BT];
        for (int i = 0; i < BT; i++)
            for (int j = 0; j < BT; j++) {
                data[i][j] = new AStar_Chess(i, j);
                AStar_Chess c = b_data[i][j];
                data[i][j].sum = c.sum;
                data[i][j].defence = c.defence;
                data[i][j].offense = c.offense;
                data[i][j].side = c.side;

            }
        sorted = new AStar_Chess[b_sorted.length];
        for (int i = 0; i < sorted.length; i++) {
            AStar_Chess c = b_sorted[i];
            sorted[i] = new AStar_Chess(c.x, c.y);
            sorted[i].sum = c.sum;
            sorted[i].defence = c.defence;
            sorted[i].offense = c.offense;
            sorted[i].side = c.side;
        }
        currentPlayer = b.getPlayer();
        minx = b.minx;
        maxx = b.maxx;
        miny = b.miny;
        maxy = b.maxy;
        history = new Stack<Point>();
    }

//    public void start() {
//        currentPlayer = AStar_Chess.BLACK;// 默认黑子先行
//        putChess(CENTER, CENTER);// 默认第一步落在中心
//        minx = maxx = miny = maxy = CENTER;
//    }
    // AStar_Board 类中（棋盘类）

    // 在 AStar_Board 类中修改 start 方法以接受玩家颜色参数
    public void start(int playerColor,int mode) {

        // 如果玩家选择白棋，AI 将默认第一步落在中心
        if (playerColor == AStar_Chess.WHITE) {
            currentPlayer = AStar_Chess.BLACK;
            putChess(CENTER, CENTER);
            minx = maxx = miny = maxy = CENTER;// 默认第一步落在中心
        }
        // 如果玩家选择黑棋，AI 将等待玩家落子
        else {
            currentPlayer = AStar_Chess.BLACK;
            if(mode==2) {
                putChess(CENTER, CENTER);
                minx = maxx = miny = maxy = CENTER;// 默认第一步落在中心
            }
            minx = 1;
            maxx = BOARD_SIZE;
            miny = 1;
            maxy = BOARD_SIZE;
        }

    }

    public void reset() {
        for (int i = 1; i < BT - 1; i++)
            for (int j = 1; j < BT - 1; j++) {
                data[i][j].reset();
            }
        history.clear();
    }

    public Point undo() {// 悔棋
        if (!history.isEmpty()) {
            Point p1 = history.pop();
            Point p2 = history.pop();
            data[p1.x][p1.y].setSide(AStar_Chess.EMPTY);
            data[p2.x][p2.y].setSide(AStar_Chess.EMPTY);
            return history.peek();
        }
        return null;
    }

    public AStar_Chess[][] getData() {
        return data;
    }

    public AStar_Chess[] getSorted() {
        return sorted;
    }

    public int getPlayer() {
        return currentPlayer;
    }

    public int[][] getHistory() {
        int length = history.size();
        int[][] array = new int[length][2];
        for (int i = 0; i < length; i++)
            for (int j = 0; j < 2; j++) {
                Point p = history.get(i);
                array[i][0] = (int) p.getX();
                array[i][1] = (int) p.getY();
            }
        return array;
    }


    /**
     * 在点(x,y)落子
     */
    public boolean putChess(int x, int y) {
        if (data[x][y].isEmpty()) {
            // 棋盘搜索范围限制
            minx = Math.min(minx, x);
            maxx = Math.max(maxx, x);
            miny = Math.min(miny, y);
            maxy = Math.max(maxy, y);
            data[x][y].setSide(currentPlayer);
            history.push(new Point(x, y));
            trogglePlayer();
            sorted = getSortedChess(currentPlayer);// 重要
            System.out.printf(" 【" + (char) (64 + x) + (16 - y) + "】");
            return true;
        }
        return false;
    }

    private void trogglePlayer() {
        currentPlayer = 3 - currentPlayer;
    };

    private int check(int x, int y, int dx, int dy, int chess) {
        int sum = 0;
        for (int i = 0; i < 4; ++i) {
            x += dx;
            y += dy;
            if (x < 1 || x > BOARD_SIZE || y < 1 || y > BOARD_SIZE) {
                break;
            }
            if (data[x][y].getSide() == chess) {
                sum++;
            } else {
                break;
            }
        }
        return sum;
    }

    public int isGameOver() {
        if (!history.isEmpty()) {
            int chess = (history.size() % 2 == 1) ? AStar_Chess.BLACK : AStar_Chess.WHITE;
            Point lastStep = history.peek();
            int x = (int) lastStep.getX();
            int y = (int) lastStep.getY();
            if (check(x, y, 1, 0, chess) + check(x, y, -1, 0, chess) >= 4) {
                return chess;
            }
            if (check(x, y, 0, 1, chess) + check(x, y, 0, -1, chess) >= 4) {
                return chess;
            }
            if (check(x, y, 1, 1, chess) + check(x, y, -1, -1, chess) >= 4) {
                return chess;
            }
            if (check(x, y, 1, -1, chess) + check(x, y, -1, 1, chess) >= 4) {
                return chess;
            }
        }
        // 进行中
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j)
                if (data[i][j].isEmpty()) {
                    return 0;
                }
        }
        // 平局
        return 3;
    }

    /**
     * 玩家(player)轮，根据各点的落子估值从大到小排序
     *
     */
    public AStar_Chess[] getSortedChess(int player) {
        // 限制范围
        int px = Math.max(minx - 5, 1);
        int py = Math.max(miny - 5, 1);
        int qx = Math.min(maxx + 5, AStar_Board.BT - 1);
        int qy = Math.min(maxy + 5, AStar_Board.BT - 1);
        AStar_Chess[] temp = new AStar_Chess[(qx - px + 1) * (qy - py + 1)];
        int count = 0;
        for (int x = px; x <= qx; x++) {
            for (int y = py; y <= qy; y++) {
                temp[count] = new AStar_Chess(x, y);
                if (data[x][y].isEmpty()) {
                    data[x][y].clearDetail();
                    data[x][y].append("================================\n");
                    int o = getScore(x, y, player) + 1;// 攻击分，优先
                    data[x][y].append("\n");
                    int d = getScore(x, y, 3 - player);// 防守分
                    data[x][y].append("\n");
                    String cs = "【" + (char) (64 + x) + (16 - y) + "】 ";
                    data[x][y].append(cs).append(" 攻击:" + o).append(" 防守:" + d)
                            .append("\n\n");
                    data[x][y].offense = temp[count].offense = o;
                    data[x][y].defence = temp[count].defence = d;
                    data[x][y].sum = temp[count].sum = o + d;// 综合分
                }
                count++;
            }
        }
        Arrays.sort(temp);
        return temp;
    }

    /**
     * 在点(x,y)落下棋子(chess)后的落子估值
     *
     */
    public int getScore(int x, int y, int chess) {
        data[x][y].append("-");
        Level l1 = getLevel(x, y, Direction.HENG, chess);
        data[x][y].append("|");
        Level l2 = getLevel(x, y, Direction.SHU, chess);
        data[x][y].append("/");
        Level l3 = getLevel(x, y, Direction.PIE, chess);
        data[x][y].append("\\");
        Level l4 = getLevel(x, y, Direction.NA, chess);
        return level2Score(l1, l2, l3, l4) + position[x - 1][y - 1];
    }

    /**
     * 在点(x,y)落下棋子(chess)后， 方向(direction)形成的棋型
     *
     */
    public Level getLevel(int x, int y, Direction direction, int chess) {
        String seq, left = "", right = "";
        if (direction == Direction.HENG) {
            left = getHalfSeq(x, y, -1, 0, chess);
            right = getHalfSeq(x, y, 1, 0, chess);
        } else if (direction == Direction.SHU) {
            left = getHalfSeq(x, y, 0, -1, chess);
            right = getHalfSeq(x, y, 0, 1, chess);
        } else if (direction == Direction.PIE) {
            left = getHalfSeq(x, y, -1, 1, chess);
            right = getHalfSeq(x, y, 1, -1, chess);
        } else if (direction == Direction.NA) {
            left = getHalfSeq(x, y, -1, -1, chess);
            right = getHalfSeq(x, y, 1, 1, chess);
        }
        seq = left + chess + right;
        String rseq = new StringBuilder(seq).reverse().toString();
        data[x][y].append("\t" + seq + "\t");
        // seq2Level

        for (Level level : Level.values()) {
            Pattern pat = Pattern.compile(level.regex[chess - 1]);
            Matcher mat = pat.matcher(seq);
            boolean rs1 = mat.find();
            mat = pat.matcher(rseq);
            boolean rs2 = mat.find();
            if (rs1 || rs2) {
                data[x][y].append(level.name).append("\n");
                return level;
            }
        }
        return Level.NULL;
    }

    private String getHalfSeq(int x, int y, int dx, int dy, int chess) {
        String sum = "";
        boolean isR = false;
        if (dx < 0 || (dx == 0 && dy == -1))
            isR = true;
        for (int i = 0; i < 5; ++i) {
            x += dx;
            y += dy;
            if (x < 1 || x > BOARD_SIZE || y < 1 || y > BOARD_SIZE) {
                break;
            }
            if (isR) {
                sum = data[x][y].getSide() + sum;
            } else
                sum = sum + data[x][y].getSide();

        }
        return sum;
    }

    /**
     * 将各方向的棋型统计成初步的打分
     */
    public int level2Score(Level l1, Level l2, Level l3, Level l4) {
        int size = Level.values().length;
        int[] levelCount = new int[size];
        for (int i = 0; i < size; i++) {
            levelCount[i] = 0;
        }
        levelCount[l1.index]++;
        levelCount[l2.index]++;
        levelCount[l3.index]++;
        levelCount[l4.index]++;
        int score = 0;
        if (levelCount[Level.GO_4.index] >= 2
                || levelCount[Level.GO_4.index] >= 1
                && levelCount[Level.ALIVE_3.index] >= 1)// 双活4，冲4活三
            score = 10000;
        else if (levelCount[Level.ALIVE_3.index] >= 2)// 双活3
            score = 5000;
        else if (levelCount[Level.SLEEP_3.index] >= 1
                && levelCount[Level.ALIVE_3.index] >= 1)// 活3眠3
            score = 1000;
        else if (levelCount[Level.ALIVE_2.index] >= 2)// 双活2
            score = 100;
        else if (levelCount[Level.SLEEP_2.index] >= 1
                && levelCount[Level.ALIVE_2.index] >= 1)// 活2眠2
            score = 10;

        score = Math.max(
                score,
                Math.max(Math.max(l1.score, l2.score),
                        Math.max(l3.score, l4.score)));
        return score;
    }

}

