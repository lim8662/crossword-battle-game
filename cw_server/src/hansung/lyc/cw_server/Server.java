package hansung.lyc.cw_server;

//Server

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.dnd.DropTargetAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Server extends JFrame {
	private JPanel contentPane;

	JTextArea textArea; // Ŭ���̾�Ʈ �� ���� �޽��� ���

	private ServerSocket socket; // ��������
	private Socket soc; // �������
	private int Port; // ��Ʈ��ȣ
	private Vector vc = new Vector(); // ����� ����ڸ� ������ ����

	public static String stage;
	public static HashMap<String, String> wordMap = new HashMap<String, String>();
	public static int wordnum; // ���������� �� �ܾ��

	public static void main(String[] args) {

		Server frame = new Server();
		frame.setVisible(true);

		// DB���� ���� �о����
		Connection conn;
		Statement stmt = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL ����̹� �ε�
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cwdb", "root", "admin"); // JDBC ����
			System.out.println("DB ���� �Ϸ�");
			stmt = conn.createStatement(); // SQL�� ó���� Statement ��ü ����

			// �о�� ������ id�� Random���� ����
			Random random = new Random();
			int ran = random.nextInt(100) + 1;

			ResultSet srs = stmt.executeQuery("select id, data from cwdata where id='" + ran + "'");
			createMap(srs);

		} catch (ClassNotFoundException e) {
			System.out.println("JDBC ����̹� �ε� ����");
		} catch (SQLException e) {
			System.out.println("SQL �������");
		}

		// ���� �����
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("���� ����");
			}
		});
	}

	// �о�� ������ �ؽ��ʿ� ����, �� ���(x,y,���μ���,���� ����)�� key�� �ܾ �����Ѵ�
	private static void createMap(ResultSet srs) throws SQLException {
		srs.next();
		stage = srs.getString("data").trim();
		// ������ ���� üũ
		System.out.println(srs.getString("data"));

		String[] datalist = stage.split("\\|");

		wordnum = datalist.length;

		for (int i = 0; i < datalist.length; i++) {
			String[] data = datalist[i].split(",");
			// ���θ� x��ǥ�� ���ڼ� ��ŭ ������Ű�� �ܾ ����
			if (data[2].equals("0")) {
				int x = Integer.parseInt(data[0]);
				for (int j = 0; j < Integer.parseInt(data[3]); j++) {
					String key = x + "," + data[1] + "," + data[2] + "," + j;
					wordMap.put(key, data[4]);
					x++;
				}
			}
			// ���θ� y��ǥ�� ���ڼ� ��ŭ ������Ű�� �ܾ ����
			else {
				int y = Integer.parseInt(data[1]);
				for (int j = 0; j < Integer.parseInt(data[3]); j++) {
					String key = data[0] + "," + y + "," + data[2] + "," + j;
					wordMap.put(key, data[4]);
					y++;
				}
			}
		}

	}

	public Server() {
		init();
		server_start();
	}

	private void init() { // GUI�� �����ϴ� �޼ҵ�
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(1450, 100, 450, 800);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane js = new JScrollPane();

		textArea = new JTextArea();
		textArea.setColumns(20);
		textArea.setRows(5);
		textArea.setFont(new Font("TimesRoman", Font.PLAIN, 18));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		js.setBounds(0, 0, 430, 750);
		contentPane.add(js);
		js.setViewportView(textArea);

		textArea.setEditable(false); // textArea�� ����ڰ� ���� ���ϰԲ� ���´�.
	}

	private void server_start() {
		try {
			socket = new ServerSocket(30000); // ������ ��Ʈ ���ºκ�

			if (socket != null) // socket �� ���������� ��������
			{
				Connection();
			}

		} catch (IOException e) {
			textArea.append("������ �̹� ������Դϴ�...\n");

		}

	}

	private void Connection() {
		Thread th = new Thread(new Runnable() { // ����� ������ ���� ������
			@Override
			public void run() {
				while (vc.size() < 2) { // Ŭ���̾�Ʈ�� 2������� �޴´�
					try {
						textArea.append("����� ���� �����...\n");
						soc = socket.accept(); // accept�� �Ͼ�� �������� ���� �����
						textArea.append("����� ����!!\n");
						textArea.setCaretPosition(textArea.getText().length());
						UserInfo user = new UserInfo(soc, vc); // ����� ���� ������ �ݹ� ������Ƿ�, user Ŭ���� ���·� ��ü ����
						// �Ű������� ���� ����� ���ϰ�, ���͸� ��Ƶд�
						vc.add(user); // �ش� ���Ϳ� ����� ��ü�� �߰�
						user.start(); // ���� ��ü�� ������ ����
					} catch (IOException e) {
						textArea.append("!!!! accept ���� �߻�... !!!!\n");
						textArea.setCaretPosition(textArea.getText().length());
					}
				}
			}
		});
		th.start();
	}

	class UserInfo extends Thread { // Ŭ���̾�Ʈ�� 1���� �ڵ� ����
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		private Socket user_socket;
		private Vector user_vc; // ����� �������� ��ü�� ����
		private String user_Id; // ������ ���̵�
		private int order; // ������ ���� ����
		private int ready; // ready ����
		private int score; // ����

		public UserInfo(Socket soc, Vector vc) // �����ڸ޼ҵ�
		{
			// �Ű������� �Ѿ�� �ڷ� ����
			this.user_socket = soc;
			this.user_vc = vc;
			this.ready = 0;
			this.score = 0;

			User_network();
		}

		public void User_network() {
			try { // ������ = ���ν�����(connect �޴¿�) 1�� + ��������ŭ
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);

			} catch (Exception e) {
				textArea.append("��Ʈ�� ���� ����\n");
				textArea.setCaretPosition(textArea.getText().length());
			}
		}

		// �޼����� ������ ���
		public void Inmessage(String str) {
			textArea.append(str + "\n");
			textArea.setCaretPosition(textArea.getText().length());
		}

		// ���� �������� �޼��� ó��
		public void protocol(String str) {
			Inmessage(str);
			// �޼��� ���ڿ��� split�ϰ� ���� �պκ��� �������ݷ� �а� �� �� ���ڷ� �˸���
			String[] msg = str.split("\\|");
			switch (msg[0]) {

			case "CONN":
				user_Id = msg[1];
				order = user_vc.size();
				if (order == 1) // ù��° ���� ���ӽ� ���� �ο����� �߰��Ͽ� ������ CONN/1/pc1
					send_Message("CONN|" + user_vc.size() + "|" + user_Id);
				if (order == 2) { // �ι�° ���� ���ӽ� �ο����� ��� ���̵� ��ε�ĳ��Ʈ CONN/2/pc1/pc2
					UserInfo pc1 = (UserInfo) user_vc.elementAt(0);
					broad_cast("CONN|" + user_vc.size() + "|" + pc1.user_Id + "|" + user_Id);
				}
				break;

			case "READY":
				ready = Integer.parseInt(msg[1]);
				
				UserInfo pc1 = (UserInfo) user_vc.elementAt(0);
				UserInfo pc2 = (UserInfo) user_vc.elementAt(1);
				broad_cast(str);
				// ��� ������ ready ���¸� ���� ����
				if (pc1.ready == 1 && pc2.ready == 1) {
					broad_cast("START|" + stage);
				}
				break;

			case "SELECT":
				// �츮���� api�� ������ �ܾ��� �� �˻��ؼ� ��ȯ
				try {
					String url = "https://opendict.korean.go.kr/api/search";
					String key = "7B25693300DB85B6F633F3244D36AEAF";
					String q = wordMap.get(msg[1]);
					String buf [] =msg[1].split(",");
					
					Document doc = Jsoup.connect(url).data("key", key).data("q", q).get();
					String mean = doc.selectFirst("definition").text();
					if(buf[2].equals("0")) //����
					    send_Message("HINT|(����)" + mean);
					else	//����
						send_Message("HINT|(����)" + mean);
				} catch (Exception e) {
					System.out.println("���� �˻� ����");
				}

				break;

			case "WORD":
				// ��ǥ Ű�� �޾� �ش� �ܾ ã�� ������ �ܾ�� ���Ͽ� ���ٸ� OPEN
				String answer = wordMap.get(msg[1]);
				Inmessage("���� : " + answer);
				Inmessage("�Է� : " + msg[2]);
				if (msg[2].equals(answer)) {
					// msg[1]�� 0,1,2,3 ���� 3�� �о� �ܾ� ������ ��ǥ�� ����
					String data[] = msg[1].split(",");
					String word[] = answer.split("");
					String key = "";

					// ���θ� x���� �ܾ� ���۰�����
					if (data[2].equals("0")) {
						int x = Integer.parseInt(data[0]);
						int seq = Integer.parseInt(data[3]);
						x -= seq;
						key = x + "," + data[1] + "," + data[2] + "," + word.length;
					}
					// ���θ� y���� �ܾ� ���۰�����
					else {
						int y = Integer.parseInt(data[1]);
						int seq = Integer.parseInt(data[3]);
						y -= seq;
						key = data[0] + "," + y + "," + data[2] + "," + word.length;
					}
					// �ܾ� ������ ��ǥ, �ܾ�, ���� ������ order�� ����
					broad_cast("OPEN|" + key + "|" + answer + "|" + order);
					// ���� ������ ���� ����
					broad_cast("SCORE|" + order + "|" + ++score);

					// ��� ���߸� END�� ���������� ��������
					UserInfo user1 = (UserInfo) user_vc.elementAt(0);
					UserInfo user2 = (UserInfo) user_vc.elementAt(1);
					if (user1.score + user2.score == wordnum) {
						broad_cast("END|" + 1 + "|" + user1.score + "|" + 2 + "|" + user2.score);
					}
				}
				break;

			case "ITEM_USE":
				// msg[1]�� 0,1,2,3 ���� 3�� �о� �ܾ� ������ ��ǥ�� ����
				String answer1 = wordMap.get(msg[1]);
				String data[] = msg[1].split(",");
				String word[] = answer1.split("");
				String key = "";

				if (msg[2].equals("2")) { // 2 :������ �� �ܾ� ����
					// ���θ� x���� �ܾ� ���۰�����
					if (data[2].equals("0")) {
						int x = Integer.parseInt(data[0]);
						int seq = Integer.parseInt(data[3]);
						x -= seq;
						key = x + "," + data[1] + "," + data[2] + "," + word.length;
					}
					// ���θ� y���� �ܾ� ���۰�����
					else {
						int y = Integer.parseInt(data[1]);
						int seq = Integer.parseInt(data[3]);
						y -= seq;
						key = data[0] + "," + y + "," + data[2] + "," + word.length;
					}
					// ������ ��ȣ , �ܾ� ���� ��ǥ, �ܾ ����
					send_Message("ITEM_EFF|" + msg[2] + "|" + key + "|" + answer1);

				} else { // 1 : ������ �� ���� ����

					int seq = Integer.parseInt(data[3]);
					key = data[0] + "," + data[1];
					// ������ ��ȣ, �ش� ��� ��ǥ, ���ڸ� ����
					send_Message("ITEM_EFF|" + msg[2] + "|" + key + "|" + word[seq]);

				}
				break;

			// ��ǽ� 0������ ����� ���������� ��������
			case "ESCAPE":
				System.out.println(msg[1]);
				score = 0;
			// ���ѽð� �ʰ��� ���������� ��������
			case "TIMEOVER":
				UserInfo user1 = (UserInfo) user_vc.elementAt(0);
				UserInfo user2 = (UserInfo) user_vc.elementAt(1);
				broad_cast("END|" + user1.score + "|" + user2.score);
				break;
			//������ ���� ����� ���ϴ���
			case "DISC":
				try {
					Inmessage(user_Id + "���� ������ �����Ͽ����ϴ�.");
					dos.close();
					dis.close();
					user_socket.close();
					vc.removeElement(this);
					break;
				} catch (Exception e) {
				}

			}

		}
		//��� �������� �޼��� ����
		public void broad_cast(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo imsi = (UserInfo) user_vc.elementAt(i);
				imsi.send_Message(str); // write all
			}
		}
		//�޼��� ����
		public void send_Message(String str) {
			try {
				Inmessage(str);
				dos.writeUTF(str);

			} catch (IOException e) {
				textArea.append("�޽��� �۽� ���� �߻�\n");
				textArea.setCaretPosition(textArea.getText().length());
			}
		}

		public void run() // ������ ����
		{

			while (true) {
				try {

					// ����ڿ��� �޴� �޼���
					String msg = dis.readUTF();
					msg = msg.trim();
					protocol(msg);

				} catch (IOException e) {

				} // �ٱ� catch����

			}

		}// run�޼ҵ� ��

	} // ���� userinfoŬ������

}
