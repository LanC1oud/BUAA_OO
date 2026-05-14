public class FlTr {
    public static Integer strToInt(String floorName) {
        char prefix = floorName.charAt(0);
        int num = Integer.parseInt(floorName.substring(1));
        if (prefix == 'B') {  return -num; }
        else { return num; }
    }
    
    public static String intToStr(Integer floorNumber) {
        if (floorNumber < 0) { return "B" + (-floorNumber); }
        else { return "F" + floorNumber; }
    }
}
