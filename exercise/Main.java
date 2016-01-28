
public class Main {
 
 public static void main(String argv[]) {
    System.out.println(argv[0]);
    Steg s = new Steg();
    //s.hideString("Baron Von Yolo", argv[0]);

    //System.out.println(s.extractString("bee_steg.bmp"));

    s.hideFile("arrays.c", argv[0]);
    s.extractFile("bee_steg.bmp");
 }  

}
