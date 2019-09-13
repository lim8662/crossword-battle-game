import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;


import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.plaf.metal.MetalButtonUI;

public class MainView extends JFrame {
   private Socket socket;
   private String id;
   private String ip = "127.0.0.1";
   private int port = 30000;
   private JPanel mainPanel;
   private JLabel readyHelp;
   private JButton userReadyButton1;
   private JButton userReadyButton2;
   private JTextField answerTf;
   private JTextArea hint;
   private JPanel gamePanel;
   private JLabel userLabel1;
   private JLabel userLabel2;
   private InputStream is;
   private OutputStream os;
   private DataInputStream dis;
   private DataOutputStream dos;
   private String[] str;
   String mypid;
   private int flag = 0;
   private String user1;
   private String user2;
   private String[] str2; // 좌표
   
   public ArrayList<UserDAO> list = new ArrayList<UserDAO>();
   private JButton wordBtn[][] = new JButton[8][8];

   ImageIcon normalIcon = new ImageIcon("images/기본블럭1.png");
   ImageIcon item1 = new ImageIcon("images/h.jpg");
   ImageIcon item2 = new ImageIcon("images/마블리.png");
   JLabel itemHelpLabel = new JLabel(new ImageIcon("images/아이템.png"));
   ImageIcon bg = new ImageIcon("images/배경화면.jpg");
   private JButton itemBtn1_1 = new JButton("1-1", item1);
   private JButton itemBtn1_2 = new JButton("1-2", item2);
   private JButton itemBtn2_1 = new JButton("2-1", item1);
   private JButton itemBtn2_2 = new JButton("2-2", item2);
   private JLabel itemLabel = new JLabel("아이템");
   JLabel xyLabel = new JLabel();
   private JLabel score = new JLabel("점수");
   private JLabel score1 = new JLabel("0");
   private JLabel score2 = new JLabel("0");
   private JLabel iidd = new JLabel("이름");

   // @@@@ result 추가
   private JLabel result1 = new JLabel("");
   private JLabel result2 = new JLabel("");

   public MainView(Socket soc, String id, String ip, int port, String mypid) {
      this.socket = soc;
      this.id = id;
      this.ip = ip;
      this.port = port;
      this.mypid = mypid;
      init();

      network();

   }

   public String getId() {
      return mypid;
   }

   public void network() {
      if (socket != null) // socket이 null값이 아닐때 즉! 연결되었을때
      {
         Connection(); // 연결 메소드를 호출
      }
   }

   public void Connection() {
      try { // 스트림 설정
         is = socket.getInputStream();
         dis = new DataInputStream(is);
         os = socket.getOutputStream();
         dos = new DataOutputStream(os);
      } catch (IOException e) {

      }
      Thread th = new Thread(new Runnable() { // 스레드를 돌려서 서버로부터 메세지를 수신
         @SuppressWarnings("null")
         @Override
         public void run() {
            while (true) {
               try {
                  String msg = "";
                  msg = dis.readUTF();
                  str = msg.trim().split("\\|");

                  String command = str[0];
                  switch (command) {
                  case "CONN":
                     // 인게임 유저정보 // 서버에서 아이디 받아와야 함
                     String num = str[1];
                     if (num.equals("1")) {
                        user1 = str[2];
                        userLabel1 = new JLabel(user1); // 본인
                        userLabel1.setFont(new Font("Serif", Font.BOLD, 20));
                        userLabel1.setBounds(500, 50, 150, 40);
                        userLabel1.setForeground(Color.PINK);
                        mainPanel.add(userLabel1);
                        userReadyButton1.setBounds(830, userLabel1.getY(), 120, 40);
                        repaint();
                     } else {
                        user1 = str[2];
                        user2 = str[3];
                        userLabel1 = new JLabel(user1); // 본인
                        userLabel1.setFont(new Font("Serif", Font.BOLD, 20));
                        userLabel1.setBounds(500, 50, 150, 40);
                        userLabel1.setForeground(Color.PINK);

                        mainPanel.add(userLabel1);
                        userReadyButton1.setBounds(830, userLabel1.getY(), 120, 40);
                        mainPanel.add(userReadyButton1);

                        userLabel2 = new JLabel(user2); // 상대
                        userLabel2.setFont(new Font("Serif", Font.BOLD, 20));
                        userLabel2.setBounds(500, 100, 150, 40);
                        userLabel2.setForeground(Color.YELLOW);
                        mainPanel.add(userLabel2);
                        userReadyButton2.setBounds(830, userLabel2.getY(), 120, 40);
                        mainPanel.add(userReadyButton2);
                        if (getId().equals(str[2]))
                           userReadyButton2.setEnabled(false);
                        if (getId().equals(str[3]))
                           userReadyButton1.setEnabled(false);
                        repaint();
                     }
                     break;
                  case "READY":
                     if (str[1].equals("1") && str[2].equals("READY1"))
                        userReadyButton1.setForeground(Color.BLUE);
                     else if (str[1].equals("0") && str[2].equals("READY1"))
                        userReadyButton1.setForeground(Color.BLACK);
                     else if (str[1].equals("1") && str[2].equals("READY2"))
                        userReadyButton2.setForeground(Color.BLUE);
                     else
                        userReadyButton2.setForeground(Color.BLACK);

                     break;
                  case "START":
                     readyHelp.setVisible(false);
                     userReadyButton1.setVisible(false);
                     userReadyButton2.setVisible(false);
                     score.setVisible(true);
                     score1.setVisible(true);
                     score2.setVisible(true);
                     iidd.setVisible(true);

                     if (userLabel1.getText().equals(mypid)) {
                        itemBtn1_1.setVisible(true);
                        itemBtn1_2.setVisible(true);
                     } else {
                        itemBtn2_1.setVisible(true);
                        itemBtn2_2.setVisible(true);
                     }
                     itemLabel.setVisible(true);

                     for (int i = 1; i < str.length; i++) {
                        str2 = str[i].split(",");
                     
                        list.add(new UserDAO(Integer.parseInt(str2[0]), Integer.parseInt(str2[1]),
                              Integer.parseInt(str2[2]), Integer.parseInt(str2[3]), str2[4]));
                 
                        if (list.get(i - 1).getHV() == 0) { // 가로줄
                           int indexX = list.get(i - 1).getX();

                           for (int q = 0; q < list.get(i - 1).getN(); q++) {
                              

                              wordBtn[indexX][list.get(i - 1).getY()].setVisible(true);
                              wordBtn[indexX][list.get(i - 1).getY()].setText(Integer.toString(indexX) + ","
                                    + Integer.toString(list.get(i - 1).getY()) + ",0," + q);

                              wordBtn[indexX][list.get(i - 1).getY()].addMouseListener(new MouseAdapter() {
                                 
                                 public void mousePressed(MouseEvent e) {
                                    JButton jb = (JButton) e.getSource();

                                    try {
                                       xyLabel.setText(jb.getText());
                                       dos.writeUTF("SELECT|" + jb.getText());
                                       answerTf.requestFocus();
                                    } catch (IOException e1) {

                                    }
                                 }
                              });
                              indexX++;

                           }

                        } else {
                           int indexY = list.get(i - 1).getY();

                           for (int w = 0; w < list.get(i - 1).getN(); w++) { // 세로줄
                           
                              wordBtn[list.get(i - 1).getX()][indexY].setVisible(true);
                              wordBtn[list.get(i - 1).getX()][indexY]
                                    .setText(Integer.toString(list.get(i - 1).getX()) + ","
                                          + Integer.toString(indexY) + ",1," + w);
                              wordBtn[list.get(i - 1).getX()][indexY].addMouseListener(new MouseAdapter() {
                           
                                 public void mousePressed(MouseEvent e) {
                                   JButton jb = (JButton) e.getSource();
   
                                   try {
                                       xyLabel.setText(jb.getText());
                                       dos.writeUTF("SELECT|" + jb.getText());
                                       answerTf.requestFocus();
                                    } catch (IOException e1) {

                                    }
                                    gamePanel.repaint();
                                 }
                              });

                              indexY++;
                           }
                        }
                     }

                     JLabel timerLabel = new JLabel();

                     TimerRunnable runnable = new TimerRunnable(timerLabel);
                     Thread tht = new Thread(runnable);
                     timerLabel.setBounds(180, 525, 300, 40);
                     timerLabel.setFont(new Font("Serif", Font.BOLD, 30));
                     timerLabel.setForeground(Color.WHITE);
                     mainPanel.add(timerLabel);
                     timerLabel.setVisible(true);
                     gamePanel.setVisible(true);
                     tht.start();
                     break;

                  case "HINT":
                     hint.setText(str[1]);
                     hint.setFont(new Font("Serif", Font.BOLD, 20));
                     repaint();
                     break;

                  case "OPEN":
                     String[] xy = str[1].split(",");
                     String[] wordd = str[2].split("");
                     if (str[3].equals("1")) {
                        if (xy[2].equals("0")) {
                           int indexXX = Integer.parseInt(xy[0]);
                           for (int h = 0; h < wordd.length; h++) {
                              wordBtn[indexXX][Integer.parseInt(xy[1])].setIcon(null);
                              wordBtn[indexXX][Integer.parseInt(xy[1])].setText(wordd[h]);
                              wordBtn[indexXX][Integer.parseInt(xy[1])].setBackground(Color.PINK);
                              wordBtn[indexXX][Integer.parseInt(xy[1])].setUI(new MetalButtonUI() { 
                                  protected Color getDisabledTextColor() { 
                                      return Color.BLACK; 
                                     } 
                                 }); 
                              wordBtn[indexXX][Integer.parseInt(xy[1])].setEnabled(false);
                              indexXX++;
                           }
                        } else {
                           int indexYY = Integer.parseInt(xy[1]);
                           for (int v = 0; v < wordd.length; v++) {
                              wordBtn[Integer.parseInt(xy[0])][indexYY].setIcon(null);
                              wordBtn[Integer.parseInt(xy[0])][indexYY].setText(wordd[v]);
                              wordBtn[Integer.parseInt(xy[0])][indexYY].setBackground(Color.PINK);
                              wordBtn[Integer.parseInt(xy[0])][indexYY].setUI(new MetalButtonUI() { 
                                  protected Color getDisabledTextColor() { 
                                      return Color.BLACK; 
                                     } 
                                 }); 

                              wordBtn[Integer.parseInt(xy[0])][indexYY].setEnabled(false);
                              indexYY++;
                           }
                        }
                     } else {
                        if (xy[2].equals("0")) {
                           int indexXX = Integer.parseInt(xy[0]);
                           for (int h = 0; h < wordd.length; h++) {
                              wordBtn[indexXX][Integer.parseInt(xy[1])].setIcon(null);
                              wordBtn[indexXX][Integer.parseInt(xy[1])].setText(wordd[h]);
                              wordBtn[indexXX][Integer.parseInt(xy[1])].setBackground(Color.YELLOW);
                              wordBtn[indexXX][Integer.parseInt(xy[1])].setUI(new MetalButtonUI() { 
                                  protected Color getDisabledTextColor() { 
                                      return Color.BLACK; 
                                     } 
                                 }); 
                              wordBtn[indexXX][Integer.parseInt(xy[1])].setEnabled(false);
                              indexXX++;
                           }
                        } else {
                           int indexYY = Integer.parseInt(xy[1]);
                           for (int v = 0; v < wordd.length; v++) {
                              wordBtn[Integer.parseInt(xy[0])][indexYY].setIcon(null);
                              wordBtn[Integer.parseInt(xy[0])][indexYY].setText(wordd[v]);
                              wordBtn[Integer.parseInt(xy[0])][indexYY].setBackground(Color.YELLOW);
                              wordBtn[Integer.parseInt(xy[0])][indexYY].setUI(new MetalButtonUI() { 
                                  protected Color getDisabledTextColor() { 
                                      return Color.BLACK; 
                                     } 
                                 }); 
                              wordBtn[Integer.parseInt(xy[0])][indexYY].setEnabled(false);
                              indexYY++;
                           }
                        }
                     }
                     repaint();
                     break;
                  case "SCORE":
                     if (str[1].equals("1"))
                        score1.setText(str[2]);
                     else
                        score2.setText(str[2]);
                     repaint();
                     break;

                  case "ITEM_EFF":
                     String[] itemXY = str[2].split(",");
                     String[] itemWord = str[3].split("");
                     if (str[1].equals("1")) {
                        wordBtn[Integer.parseInt(itemXY[0])][Integer.parseInt(itemXY[1])].setText(itemWord[0]);
                        wordBtn[Integer.parseInt(itemXY[0])][Integer.parseInt(itemXY[1])].setIcon(null);
                        wordBtn[Integer.parseInt(itemXY[0])][Integer.parseInt(itemXY[1])].setEnabled(false);
                        wordBtn[Integer.parseInt(itemXY[0])][Integer.parseInt(itemXY[1])]
                              .setBackground(Color.WHITE);

                     } else {
                        if (itemXY[2].equals("0")) {
                           int indexX = Integer.parseInt(itemXY[0]);
                           for (int h = 0; h < itemWord.length; h++) {
                              wordBtn[indexX][Integer.parseInt(itemXY[1])].setText(itemWord[h]);
                              wordBtn[indexX][Integer.parseInt(itemXY[1])].setIcon(null);
                              wordBtn[indexX][Integer.parseInt(itemXY[1])].setEnabled(false);
                              wordBtn[indexX][Integer.parseInt(itemXY[1])].setBackground(Color.WHITE);
                              indexX++;

                           }
                        } else {
                           int indexY = Integer.parseInt(itemXY[1]);
                           for (int v = 0; v < itemWord.length; v++) {
                              wordBtn[Integer.parseInt(itemXY[0])][indexY].setText(itemWord[v]);
                              wordBtn[Integer.parseInt(itemXY[0])][indexY].setIcon(null);
                              wordBtn[Integer.parseInt(itemXY[0])][indexY].setEnabled(false);
                              wordBtn[Integer.parseInt(itemXY[0])][indexY].setBackground(Color.WHITE);
                              indexY++;

                           }
                        }
                     }
                     repaint();
                     break;

                  case "END": // END | score1 | score2
                     int sc1 = Integer.parseInt(str[1]);
                     int sc2 = Integer.parseInt(str[2]);
                     score1.setText(str[1]);
                     score2.setText(str[2]);
                     if (sc1 > sc2) {
                        result1.setText("승");
                        result1.setForeground(Color.BLUE);
                        result2.setText("패");
                        result2.setForeground(Color.RED);
                     } else if (sc1 < sc2) {
                        result1.setText("패");
                        result1.setForeground(Color.RED);
                        result2.setText("승");
                        result2.setForeground(Color.BLUE);
                     } else {
                        result1.setText("무");
                        result2.setText("무");
                     }
                     dos.writeUTF("DISC|");
                     result1.setVisible(true);
                     result2.setVisible(true);
                     repaint();

                     break;

                  }
                  repaint();

               } catch (IOException e) {
                  // 서버와 소켓 통신에 문제가 생겼을 경우 소켓을 닫는다
                  // 클라는 쓰레드가 run 중이지만 서버가 DISC받고 소켓을 먼저 닫아버려 예외처리됨
                  try {

                     os.close();
                     is.close();
                     dos.close();
                     dis.close();
                     socket.close();
                     
                     break; // 에러 발생하면 while문 종료
                  } catch (IOException e1) {

                  }

               }

            } // while문 끝
         }// run메소드 끝
      });
      th.start();

      // 서버가 DISC받고 소켓을 먼저 닫아버린 후에 클라가 ESCAPE를 보내게 되서 java.net.SocketException 뜸
      // 안뜨게하려면 이벤트를 받아서 ESCAPE를 보내고 system.exit(0)처리
      Runtime.getRuntime().addShutdownHook(new Thread() {
         public void run() {

            try {
               dos.writeUTF("ESCAPE|기권함");
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      });

   }

   public void init() {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(1000, 600);
      setLocationRelativeTo(null);
      setResizable(false);

      mainPanel = new JPanel();
      setContentPane(mainPanel);
      setLayout(null);
      mainPanel.setBackground(Color.decode("#007700"));
      mainPanel.setVisible(true);

      readyHelp = new JLabel("READY 버튼을 누르세요");
      readyHelp.setBounds(500, 0, 400, 50);
      readyHelp.setFont(new Font("Serif", Font.BOLD, 30));
      readyHelp.setForeground(Color.WHITE);
      mainPanel.add(readyHelp);

      userReadyButton1 = new JButton("READY1");
      userReadyButton2 = new JButton("READY2");

      userReadyButton1.setFont(new Font("Serif", Font.BOLD, 20));

      userReadyButton1.setForeground(Color.BLACK);
      userReadyButton1.setBackground(Color.WHITE);

      userReadyButton2.setFont(new Font("Serif", Font.BOLD, 20));

      userReadyButton2.setForeground(Color.BLACK);
      userReadyButton2.setBackground(Color.WHITE);
      // 레디 이벤트
      userReadyButton1.addActionListener(new ReadyActionListener());
      userReadyButton2.addActionListener(new ReadyActionListener());


      // 정답입력칸
      answerTf = new JTextField("여기에 입력하세요", 30);
      answerTf.setBounds(500, 150, 450, 40);
      mainPanel.add(answerTf);
      answerTf.addActionListener(new TextActionListener());
      answerTf.addFocusListener(new TextFocusListener());
      answerTf.setFocusable(true);
      // 힌트
      hint = new JTextArea();
      JScrollPane scrollPane = new JScrollPane(hint);
      hint.setLineWrap(true);
      scrollPane.setBounds(500, 200, 450, 150);
      hint.setBackground(Color.BLACK);
      hint.setForeground(Color.WHITE);
      mainPanel.add(scrollPane);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

      // 아이템 설명 이미지
      itemHelpLabel.setBounds(500, 360, 450, 150);
      mainPanel.add(itemHelpLabel);
      itemHelpLabel.setVisible(true);

      // 게임패널
      gamePanel = new JPanel();
      gamePanel.setBounds(5, 30, 480, 480);
      mainPanel.add(gamePanel);
      gamePanel.setBackground(Color.BLACK);
      gamePanel.setVisible(false); // false 바꿀것
      gamePanel.setLayout(new GridLayout(8, 8));

      for (int j = 0; j < 8; j++) {
         for (int k = 0; k < 8; k++) {
            wordBtn[k][j] = new JButton("", normalIcon);
            wordBtn[k][j].setHorizontalAlignment(SwingConstants.CENTER);
            wordBtn[k][j].setFont(new Font("Serif", Font.BOLD, 20));
            gamePanel.add(wordBtn[k][j]);
            wordBtn[k][j].setVisible(false);
         }
      }

      score.setBounds(660, 10, 150, 30);
      score.setFont(new Font("Serif", Font.BOLD, 20));
      score1.setBounds(660, 50, 50, 40);
      score1.setFont(new Font("Serif", Font.BOLD, 20));
      score2.setBounds(660, 100, 50, 40);
      score2.setFont(new Font("Serif", Font.BOLD, 20));
      mainPanel.add(score);
      mainPanel.add(score1);
      mainPanel.add(score2);
      score.setVisible(false);
      score1.setVisible(false);
      score2.setVisible(false);
      iidd.setBounds(500, 10, 150, 30);
      iidd.setFont(new Font("Serif", Font.BOLD, 20));
      mainPanel.add(iidd);
      iidd.setVisible(false);
      score.setForeground(Color.WHITE);
      score1.setForeground(Color.PINK);
      score2.setForeground(Color.YELLOW);
      iidd.setForeground(Color.WHITE);
      result1.setForeground(Color.WHITE);
      result2.setForeground(Color.WHITE);
      itemLabel.setForeground(Color.WHITE);

      itemBtn1_1.setBounds(750, 50, 40, 40);
      mainPanel.add(itemBtn1_1);
      itemBtn1_1.setVisible(false);
      itemBtn1_1.addActionListener(new Item1ActionListener());

      itemBtn1_2.setBounds(810, 50, 40, 40);
      mainPanel.add(itemBtn1_2);
      itemBtn1_2.setVisible(false);
      itemBtn1_2.addActionListener(new Item2ActionListener());

      itemBtn2_1.setBounds(750, 100, 40, 40);
      mainPanel.add(itemBtn2_1);
      itemBtn2_1.setVisible(false);
      itemBtn2_1.addActionListener(new Item1ActionListener());

      itemBtn2_2.setBounds(810, 100, 40, 40);
      mainPanel.add(itemBtn2_2);
      itemBtn2_2.setVisible(false);
      itemBtn2_2.addActionListener(new Item2ActionListener());

      itemLabel.setBounds(780, 10, 100, 30);
      itemLabel.setFont(new Font("Serif", Font.BOLD, 20));
      mainPanel.add(itemLabel);
      itemLabel.setVisible(false);

      // 결과
      result1.setBounds(900, 50, 50, 40);
      result1.setFont(new Font("Serif", Font.BOLD, 40));
      result2.setBounds(900, 100, 50, 40);
      result2.setFont(new Font("Serif", Font.BOLD, 40));
      result1.setVisible(false);
      result2.setVisible(false);
      mainPanel.add(result1);
      mainPanel.add(result2);

   }
   
   // 시간제한
   class TimerRunnable implements Runnable {
      JLabel timerLabel;

      public TimerRunnable(JLabel timerLabel) {
         this.timerLabel = timerLabel;
      }

      public void run() {
         int n = 120;

         while (n >= 0) {
            timerLabel.setText("시간제한: " + Integer.toString(n));
            n--;

            try {

               Thread.sleep(1000);
            } catch (InterruptedException e) {
               return;
            }

         }
         try {
            dos.writeUTF("TIMEOVER|");
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }
   
   // 아이템1 이벤트
   class Item1ActionListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         JButton itemBtn = (JButton) e.getSource();
         try {
            itemBtn.setEnabled(false);
            dos.writeUTF("ITEM_USE|" + xyLabel.getText() + "|1");

         } catch (IOException e1) {
            e1.printStackTrace();
         }
      }
   }
   
   // 아이템2 이벤트
   class Item2ActionListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         JButton itemBtn = (JButton) e.getSource();
         try {
            itemBtn.setEnabled(false);
            dos.writeUTF("ITEM_USE|" + xyLabel.getText() + "|2");
         } catch (IOException e1) {
            e1.printStackTrace();
         }
      }
   }

   // 정답 입력 이벤트
   class TextActionListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         JTextField jt = (JTextField) e.getSource();
         try {
            dos.writeUTF("WORD|" + xyLabel.getText() + "|" + jt.getText());
            jt.setText("");
         } catch (IOException e1) {
            e1.printStackTrace();
         }

      }
   }
   
   // 입력칸 포커스 이벤트
   class TextFocusListener implements FocusListener {
      public void focusGained(FocusEvent e) {
         answerTf.setText("");
      }

      @Override
      public void focusLost(FocusEvent e) {
         answerTf.requestFocus();
      }
   }

   // 버튼 레디 이벤트
   class ReadyActionListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         JButton b = (JButton) e.getSource();
         if (b.getForeground() == Color.BLACK) {
            b.setForeground(Color.BLUE);
            flag++;
         } else {
            b.setForeground(Color.BLACK);
            flag--;
         }
         String u_id;
         if (b.getText().equals("READY1"))
            u_id = "|READY1";
         else {
            u_id = "|READY2";
         }
         // flag 값 서버에 보내주기

         String sendFlag = "READY|" + Integer.toString(flag) + u_id;

         try {
            dos.writeUTF(sendFlag);
         } catch (IOException e1) {
            e1.printStackTrace();
         }
      }
   }

}