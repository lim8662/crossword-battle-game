import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

// 대기화면
public class Client extends JFrame {
   private JPanel mainPanel;
   private JTextField tf_ID;
   private JLabel help;
   private JLabel title = new JLabel(new ImageIcon("images/제목.jpg"));
   private JLabel enterID;
   private String id;
   private String ip = "127.0.0.1";
   private int port = 30000;
   ImageIcon bg = new ImageIcon("images/배경화면.jpg");

   public Client() {
      
      init();
   }

   public void init() {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(1000, 600);
      setLocationRelativeTo(null);
      setResizable(false);

      mainPanel = new JPanel() {
         public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension d = getSize();
            g.drawImage(bg.getImage(), 0, 0,d.width, d.height, null);
            setOpaque(true);
      
         }
      };
      setContentPane(mainPanel);
      setLayout(null);
      setResizable(false);
      
      title.setBounds(245, 50, 489, 298);
      mainPanel.add(title);
      title.setVisible(true);

      
      help = new JLabel("아이디 입력 후 엔터키를 누르면 대기화면으로 이동합니다");
      help.setBounds(100, 350, 900, 30);
      help.setFont(new Font("Serif", Font.PLAIN, 30));
      mainPanel.add(help);


      enterID = new JLabel("아이디 입력 : ");
      enterID.setBounds(250, 450, 200, 30);
      enterID.setFont(new Font("Serif", Font.PLAIN, 30));
      mainPanel.add(enterID);

      tf_ID = new JTextField(15);
      tf_ID.setBounds(450, 450, 200, 30);
      tf_ID.setFont(new Font("Serif", Font.PLAIN, 25));
      mainPanel.add(tf_ID);
      setVisible(true);

      
      
      // CONN
      tf_ID.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            id = "CONN|" + tf_ID.getText().trim();
            String mypid = tf_ID.getText().trim(); // 본인의 아이디
            try {
               Socket soc = new Socket(ip, port);
               DataOutputStream out = new DataOutputStream(soc.getOutputStream());

               out.writeUTF(id);

               MainView view = new MainView(soc, id, ip, port, mypid);
               mainPanel.setVisible(false);
               dispose(); // 개꿀띠
               view.setVisible(true);

            }

            catch (Exception e) {

            }
         }
      });
   }
}