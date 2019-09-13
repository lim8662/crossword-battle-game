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

	JTextArea textArea; // 클라이언트 및 서버 메시지 출력

	private ServerSocket socket; // 서버소켓
	private Socket soc; // 연결소켓
	private int Port; // 포트번호
	private Vector vc = new Vector(); // 연결된 사용자를 저장할 벡터

	public static String stage;
	public static HashMap<String, String> wordMap = new HashMap<String, String>();
	public static int wordnum; // 스테이지내 총 단어수

	public static void main(String[] args) {

		Server frame = new Server();
		frame.setVisible(true);

		// DB에서 문제 읽어오기
		Connection conn;
		Statement stmt = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL 드라이버 로드
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cwdb", "root", "admin"); // JDBC 연결
			System.out.println("DB 연결 완료");
			stmt = conn.createStatement(); // SQL문 처리용 Statement 객체 생성

			// 읽어올 문제의 id를 Random으로 설정
			Random random = new Random();
			int ran = random.nextInt(100) + 1;

			ResultSet srs = stmt.executeQuery("select id, data from cwdata where id='" + ran + "'");
			createMap(srs);

		} catch (ClassNotFoundException e) {
			System.out.println("JDBC 드라이버 로드 오류");
		} catch (SQLException e) {
			System.out.println("SQL 실행오류");
		}

		// 서버 종료시
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("서버 종료");
			}
		});
	}

	// 읽어온 문제를 해쉬맵에 저장, 각 블록(x,y,가로세로,글자 순서)을 key로 단어를 저장한다
	private static void createMap(ResultSet srs) throws SQLException {
		srs.next();
		stage = srs.getString("data").trim();
		// 가져온 문제 체크
		System.out.println(srs.getString("data"));

		String[] datalist = stage.split("\\|");

		wordnum = datalist.length;

		for (int i = 0; i < datalist.length; i++) {
			String[] data = datalist[i].split(",");
			// 가로면 x좌표를 글자수 만큼 증가시키며 단어를 저장
			if (data[2].equals("0")) {
				int x = Integer.parseInt(data[0]);
				for (int j = 0; j < Integer.parseInt(data[3]); j++) {
					String key = x + "," + data[1] + "," + data[2] + "," + j;
					wordMap.put(key, data[4]);
					x++;
				}
			}
			// 세로면 y좌표를 글자수 만큼 증가시키며 단어를 저장
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

	private void init() { // GUI를 구성하는 메소드
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

		textArea.setEditable(false); // textArea를 사용자가 수정 못하게끔 막는다.
	}

	private void server_start() {
		try {
			socket = new ServerSocket(30000); // 서버가 포트 여는부분

			if (socket != null) // socket 이 정상적으로 열렸을때
			{
				Connection();
			}

		} catch (IOException e) {
			textArea.append("소켓이 이미 사용중입니다...\n");

		}

	}

	private void Connection() {
		Thread th = new Thread(new Runnable() { // 사용자 접속을 받을 스레드
			@Override
			public void run() {
				while (vc.size() < 2) { // 클라이언트는 2명까지만 받는다
					try {
						textArea.append("사용자 접속 대기중...\n");
						soc = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
						textArea.append("사용자 접속!!\n");
						textArea.setCaretPosition(textArea.getText().length());
						UserInfo user = new UserInfo(soc, vc); // 연결된 소켓 정보는 금방 사라지므로, user 클래스 형태로 객체 생성
						// 매개변수로 현재 연결된 소켓과, 벡터를 담아둔다
						vc.add(user); // 해당 벡터에 사용자 객체를 추가
						user.start(); // 만든 객체의 스레드 실행
					} catch (IOException e) {
						textArea.append("!!!! accept 에러 발생... !!!!\n");
						textArea.setCaretPosition(textArea.getText().length());
					}
				}
			}
		});
		th.start();
	}

	class UserInfo extends Thread { // 클라이언트당 1개씩 자동 생성
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		private Socket user_socket;
		private Vector user_vc; // 연결된 유저들을 객체로 저장
		private String user_Id; // 유저의 아이디
		private int order; // 유저의 접속 순서
		private int ready; // ready 여부
		private int score; // 점수

		public UserInfo(Socket soc, Vector vc) // 생성자메소드
		{
			// 매개변수로 넘어온 자료 저장
			this.user_socket = soc;
			this.user_vc = vc;
			this.ready = 0;
			this.score = 0;

			User_network();
		}

		public void User_network() {
			try { // 스레드 = 메인스레드(connect 받는용) 1개 + 유저수만큼
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);

			} catch (Exception e) {
				textArea.append("스트림 셋팅 에러\n");
				textArea.setCaretPosition(textArea.getText().length());
			}
		}

		// 메세지를 서버에 출력
		public void Inmessage(String str) {
			textArea.append(str + "\n");
			textArea.setCaretPosition(textArea.getText().length());
		}

		// 받은 프로토콜 메세지 처리
		public void protocol(String str) {
			Inmessage(str);
			// 메세지 문자열을 split하고 가장 앞부분을 프로토콜로 읽고 그 뒤 인자로 알맞은
			String[] msg = str.split("\\|");
			switch (msg[0]) {

			case "CONN":
				user_Id = msg[1];
				order = user_vc.size();
				if (order == 1) // 첫번째 유저 접속시 순서 인원수를 추가하여 돌려줌 CONN/1/pc1
					send_Message("CONN|" + user_vc.size() + "|" + user_Id);
				if (order == 2) { // 두번째 유저 접속시 인원수와 모든 아이디를 브로드캐스트 CONN/2/pc1/pc2
					UserInfo pc1 = (UserInfo) user_vc.elementAt(0);
					broad_cast("CONN|" + user_vc.size() + "|" + pc1.user_Id + "|" + user_Id);
				}
				break;

			case "READY":
				ready = Integer.parseInt(msg[1]);
				
				UserInfo pc1 = (UserInfo) user_vc.elementAt(0);
				UserInfo pc2 = (UserInfo) user_vc.elementAt(1);
				broad_cast(str);
				// 모든 유저가 ready 상태면 게임 시작
				if (pc1.ready == 1 && pc2.ready == 1) {
					broad_cast("START|" + stage);
				}
				break;

			case "SELECT":
				// 우리말샘 api로 선택한 단어의 뜻 검색해서 반환
				try {
					String url = "https://opendict.korean.go.kr/api/search";
					String key = "7B25693300DB85B6F633F3244D36AEAF";
					String q = wordMap.get(msg[1]);
					String buf [] =msg[1].split(",");
					
					Document doc = Jsoup.connect(url).data("key", key).data("q", q).get();
					String mean = doc.selectFirst("definition").text();
					if(buf[2].equals("0")) //가로
					    send_Message("HINT|(가로)" + mean);
					else	//세로
						send_Message("HINT|(세로)" + mean);
				} catch (Exception e) {
					System.out.println("사전 검색 오류");
				}

				break;

			case "WORD":
				// 좌표 키를 받아 해당 단어를 찾고 보내준 단어와 비교하여 같다면 OPEN
				String answer = wordMap.get(msg[1]);
				Inmessage("정답 : " + answer);
				Inmessage("입력 : " + msg[2]);
				if (msg[2].equals(answer)) {
					// msg[1]의 0,1,2,3 에서 3를 읽어 단어 시작점 좌표를 생성
					String data[] = msg[1].split(",");
					String word[] = answer.split("");
					String key = "";

					// 가로면 x값을 단어 시작값으로
					if (data[2].equals("0")) {
						int x = Integer.parseInt(data[0]);
						int seq = Integer.parseInt(data[3]);
						x -= seq;
						key = x + "," + data[1] + "," + data[2] + "," + word.length;
					}
					// 세로면 y값을 단어 시작값으로
					else {
						int y = Integer.parseInt(data[1]);
						int seq = Integer.parseInt(data[3]);
						y -= seq;
						key = data[0] + "," + y + "," + data[2] + "," + word.length;
					}
					// 단어 시작점 좌표, 단어, 맞춘 유저의 order를 보냄
					broad_cast("OPEN|" + key + "|" + answer + "|" + order);
					// 맞춘 유저의 점수 갱신
					broad_cast("SCORE|" + order + "|" + ++score);

					// 모두 맞추면 END랑 점수보내고 게임종료
					UserInfo user1 = (UserInfo) user_vc.elementAt(0);
					UserInfo user2 = (UserInfo) user_vc.elementAt(1);
					if (user1.score + user2.score == wordnum) {
						broad_cast("END|" + 1 + "|" + user1.score + "|" + 2 + "|" + user2.score);
					}
				}
				break;

			case "ITEM_USE":
				// msg[1]의 0,1,2,3 에서 3를 읽어 단어 시작점 좌표를 생성
				String answer1 = wordMap.get(msg[1]);
				String data[] = msg[1].split(",");
				String word[] = answer1.split("");
				String key = "";

				if (msg[2].equals("2")) { // 2 :아이템 한 단어 공개
					// 가로면 x값을 단어 시작값으로
					if (data[2].equals("0")) {
						int x = Integer.parseInt(data[0]);
						int seq = Integer.parseInt(data[3]);
						x -= seq;
						key = x + "," + data[1] + "," + data[2] + "," + word.length;
					}
					// 세로면 y값을 단어 시작값으로
					else {
						int y = Integer.parseInt(data[1]);
						int seq = Integer.parseInt(data[3]);
						y -= seq;
						key = data[0] + "," + y + "," + data[2] + "," + word.length;
					}
					// 아이템 번호 , 단어 시작 좌표, 단어를 보냄
					send_Message("ITEM_EFF|" + msg[2] + "|" + key + "|" + answer1);

				} else { // 1 : 아이템 한 글자 공개

					int seq = Integer.parseInt(data[3]);
					key = data[0] + "," + data[1];
					// 아이템 번호, 해당 블록 좌표, 글자를 보냄
					send_Message("ITEM_EFF|" + msg[2] + "|" + key + "|" + word[seq]);

				}
				break;

			// 기권시 0점으로 만들고 점수보내고 게임종료
			case "ESCAPE":
				System.out.println(msg[1]);
				score = 0;
			// 제한시간 초과시 점수보내고 게임종료
			case "TIMEOVER":
				UserInfo user1 = (UserInfo) user_vc.elementAt(0);
				UserInfo user2 = (UserInfo) user_vc.elementAt(1);
				broad_cast("END|" + user1.score + "|" + user2.score);
				break;
			//유저가 연결 종료시 소켓닫음
			case "DISC":
				try {
					Inmessage(user_Id + "님이 게임을 종료하였습니다.");
					dos.close();
					dis.close();
					user_socket.close();
					vc.removeElement(this);
					break;
				} catch (Exception e) {
				}

			}

		}
		//모든 유저에게 메세지 전송
		public void broad_cast(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo imsi = (UserInfo) user_vc.elementAt(i);
				imsi.send_Message(str); // write all
			}
		}
		//메세지 전송
		public void send_Message(String str) {
			try {
				Inmessage(str);
				dos.writeUTF(str);

			} catch (IOException e) {
				textArea.append("메시지 송신 에러 발생\n");
				textArea.setCaretPosition(textArea.getText().length());
			}
		}

		public void run() // 스레드 정의
		{

			while (true) {
				try {

					// 사용자에게 받는 메세지
					String msg = dis.readUTF();
					msg = msg.trim();
					protocol(msg);

				} catch (IOException e) {

				} // 바깥 catch문끝

			}

		}// run메소드 끝

	} // 내부 userinfo클래스끝

}
