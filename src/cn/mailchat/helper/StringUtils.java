package cn.mailchat.helper;

public final class StringUtils {

    public static boolean isNullOrEmpty(String string){
        return string == null || string.isEmpty();
    }
    public static boolean isNullStrOrEmpty(String string){
        return string == null || string.isEmpty()||("null").equals(string);
    }
    public static boolean containsAny(String haystack, String[] needles) {
        if (haystack == null) {
            return false;
        }

        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }

        return false;
    }

	public static String[] convertStrToArray(String update_tag) {
		String[] strArray = null;
		strArray = update_tag.split(",");
		return strArray;
	}
}
