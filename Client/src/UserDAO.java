
public class UserDAO {
   // x��ǥ, y��ǥ, ����/����(0: ���� / 1: ����), ���ڼ�
   private int x, y, hv, n;
   
   private String word;
   
   public UserDAO() {
      
   }
   
   public UserDAO(int x, int y, int hv, int n, String word) {
      this.x = x;
      this.y = y;
      this.hv = hv;
      this.n = n;
      this.word = word;
   }
   

   
   
   public int getX() {
      return x;
   }
   public int getY() {
      return y;
   }
   public int getHV() {
      return hv;
   }
   public int getN() {
      return n;
   }
   public String getWord() {
      return word;
   }

   public void setX(int x) {
      this.x = x;
   }

   public void setY(int y) {
      this.y = y;
   }

   public void setHv(int hv) {
      this.hv = hv;
   }

   public void setN(int n) {
      this.n = n;
   }

   public void setWord(String word) {
      this.word = word;
   }
}