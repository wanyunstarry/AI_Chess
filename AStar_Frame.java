package AStar;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//一般用Swing包中的JFrame类(JavaBean类)来创建窗口，然后在窗口中添加各种组件，比如按钮、文本框、标签等。
//JFrame 界面，窗体
//子类呢?也表示界面，窗体
//规定:AStar_Frame这个界面表示的就是五子棋的主界面
//以后跟五子棋相关的所有逻辑都写在这个类中
public class AStar_Frame extends JFrame {// 五子棋界面设置
    private static final long serialVersionUID = -7844061449912554572L;
    JRadioButton manualBtn = new JRadioButton("2P");//两人对战模式按钮
    JRadioButton halfAutoBtn = new JRadioButton("1P", true);// 一人对战AI模式按钮，是默认选中的模式
    JRadioButton autoBtn = new JRadioButton("0P");// AI自我对战模式按钮
    JCheckBox orderBtn = new JCheckBox("显示落子顺序");
    JRadioButton oneBtn = new JRadioButton("估值函数");
    JRadioButton treeBtn = new JRadioButton("估值函数+搜索树", true);
    JComboBox<Integer> levelCombo = new JComboBox<Integer>(new Integer[] { 1,
            2, 3 ,4});
    JComboBox<Integer> nodeCombo = new JComboBox<Integer>(new Integer[] { 3, 5,
            10 });
    JButton btn = new JButton("New Game");
    //btn.setFont(new Font("宋体", Font.PLAIN, 14));
    JButton undoBtn = new JButton("Retract");
    TextArea area = new TextArea();
    AStar_Panel panel = new AStar_Panel(area);// 棋盘面板
    // 在 AStar_Frame 类中添加新的成员变量
    JRadioButton playerBlackBtn = new JRadioButton("Player Black", true);
    JRadioButton playerWhiteBtn = new JRadioButton("Player White");

    ButtonGroup playerColorGroup = new ButtonGroup();
//    playerColorGroup.add(playerBlackBtn);
//    playerColorGroup.add(playerWhiteBtn);
    public AStar_Frame() {
        super("五子棋博弈系统");
        add(panel, BorderLayout.EAST);
        setAlwaysOnTop(true);//设置界面置顶

        ButtonGroup grp_mode = new ButtonGroup();
        grp_mode.add(manualBtn);
        grp_mode.add(halfAutoBtn);
        grp_mode.add(autoBtn);
        ButtonGroup grp_alg = new ButtonGroup();
        grp_alg.add(oneBtn);
        grp_alg.add(treeBtn);

        JPanel rightPanel = new JPanel();
        area.setEditable(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JPanel optPanel = new JPanel();
        optPanel.setLayout(new BoxLayout(optPanel, BoxLayout.Y_AXIS));
        optPanel.setBorder(new TitledBorder("Game Setting"));

        JPanel panel2 = new JPanel();
        panel2.setBorder(new TitledBorder("Mode"));
        panel2.add(manualBtn);
        panel2.add(halfAutoBtn);
        panel2.add(autoBtn);
        optPanel.add(panel2);

        JPanel panel3 = new JPanel();
        panel3.add(oneBtn);
        panel3.add(treeBtn);
        //optPanel.add(panel3);

        JPanel panel4 = new JPanel();
        panel4.setBorder(new TitledBorder("AI Chess Force"));
        panel4.add(new JLabel("Depth"));
        panel4.add(levelCombo);
        panel4.add(new JLabel("Nodes"));
        panel4.add(nodeCombo);
        optPanel.add(panel4);

        rightPanel.add(optPanel);

        JPanel panel5 = new JPanel();
        panel5.setBorder(new TitledBorder("Operation"));
        panel5.add(btn);
        panel5.add(undoBtn);
        rightPanel.add(panel5);

        add(rightPanel);
        btn.addActionListener(l);
        orderBtn.addActionListener(l);
        undoBtn.addActionListener(l);

        setSize(900, 700);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // 添加在 AStar_Frame 构造函数或UI初始化部分
        playerColorGroup.add(playerBlackBtn);
        playerColorGroup.add(playerWhiteBtn);
        // 创建一个面板用于放置玩家颜色选择的单选按钮
        JPanel playerColorPanel = new JPanel();
        playerColorPanel.setBorder(BorderFactory.createTitledBorder("Player Color"));
        playerColorPanel.add(playerBlackBtn);
        playerColorPanel.add(playerWhiteBtn);
        optPanel.add(playerColorPanel);// 将玩家颜色选择面板添加到游戏设置面板中
    }

    private ActionListener l = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == btn)
            {
                int mode = -1, intel = -1, level, node;

                // 新增的变量，用于存储玩家选择的棋子颜色
                int playerColor = playerBlackBtn.isSelected() ? AStar_Chess.BLACK : AStar_Chess.WHITE;

                if (manualBtn.isSelected())
                    mode = AStar_Panel.MANUAL;
                else if (halfAutoBtn.isSelected())
                    mode = AStar_Panel.HALF;
                else if (autoBtn.isSelected())
                    mode = AStar_Panel.AUTO;

                if (oneBtn.isSelected())
                    intel = AStar_Panel.EVAL;
                else if (treeBtn.isSelected())
                    intel = AStar_Panel.TREE;
                else intel = AStar_Panel.TREE;

                level = (Integer) levelCombo.getSelectedItem();
                node = (Integer) nodeCombo.getSelectedItem();

                //panel.startGame(mode, intel, level, node);
                // 调用 startGame 方法，并传入玩家选择的颜色
                panel.resetGame(); // 调用重置游戏状态的方法
                panel.startGame(mode, intel, level, node, playerColor);
            } else if (source == orderBtn)
            {
                panel.troggleOrder();
            } else if (source == undoBtn)
            {
                panel.undo();
            }
        }
    };
}

