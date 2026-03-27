public class Preprocesser {
    public static String preprocess(String input) {
        String result;
        result = input.replaceAll("\\s+","");
        boolean changed = true;
        while (changed) {
            final int len = result.length();
            result = result.replace("++","+");
            result = result.replace("+-","-");
            result = result.replace("-+","-");
            result = result.replace("--","+");
            changed = (result.length() != len);
        }
        return result;
    }
}
