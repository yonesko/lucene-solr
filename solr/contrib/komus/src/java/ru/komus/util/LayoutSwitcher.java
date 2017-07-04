package ru.komus.util;


public class LayoutSwitcher {
    private static final String[] charTable = new String[81];
    private static final String[] charTableRU = new String[126];

    private static final char START_CHAR = 'Ё';

    static {
        charTable['А' - START_CHAR] = "F";
        charTable['Б' - START_CHAR] = "<";
        charTable['б' - START_CHAR] = ",";
        charTable['В' - START_CHAR] = "D";
        charTable['Г' - START_CHAR] = "U";
        charTable['Д' - START_CHAR] = "L";
        charTable['Е' - START_CHAR] = "T";
        charTable['Ё' - START_CHAR] = "`";
        charTable['Ж' - START_CHAR] = ":";
        charTable['ж' - START_CHAR] = ";";
        charTable['З' - START_CHAR] = "P";
        charTable['И' - START_CHAR] = "B";
        charTable['Й' - START_CHAR] = "Q";
        charTable['К' - START_CHAR] = "R";
        charTable['Л' - START_CHAR] = "K";
        charTable['М' - START_CHAR] = "V";
        charTable['Н' - START_CHAR] = "Y";
        charTable['О' - START_CHAR] = "J";
        charTable['П' - START_CHAR] = "G";
        charTable['Р' - START_CHAR] = "H";
        charTable['С' - START_CHAR] = "C";
        charTable['Т' - START_CHAR] = "N";
        charTable['У' - START_CHAR] = "E";
        charTable['Ф' - START_CHAR] = "A";
        charTable['Х' - START_CHAR] = "{";
        charTable['х' - START_CHAR] = "[";
        charTable['Ц' - START_CHAR] = "W";
        charTable['Ч' - START_CHAR] = "X";
        charTable['Ш' - START_CHAR] = "I";
        charTable['Щ' - START_CHAR] = "O";
        charTable['ъ' - START_CHAR] = "]";
        charTable['Ъ' - START_CHAR] = "}";
        charTable['Ы' - START_CHAR] = "S";
        charTable['Ь' - START_CHAR] = "M";
        charTable['Э' - START_CHAR] = "'";
        charTable['Ю' - START_CHAR] = ">";
        charTable['ю' - START_CHAR] = ".";
        charTable['Я' - START_CHAR] = "Z";
        charTableRU['F'] = "А";
        charTableRU[','] = "б";
        charTableRU['<'] = "Б";
        charTableRU['D'] = "В";
        charTableRU['U'] = "Г";
        charTableRU['L'] = "Д";
        charTableRU['T'] = "Е";
        charTableRU['`'] = "Ё";
        charTableRU[';'] = "ж";
        charTableRU[':'] = "Ж";
        charTableRU['P'] = "З";
        charTableRU['B'] = "И";
        charTableRU['Q'] = "Й";
        charTableRU['R'] = "К";
        charTableRU['K'] = "Л";
        charTableRU['V'] = "М";
        charTableRU['Y'] = "Н";
        charTableRU['J'] = "О";
        charTableRU['G'] = "П";
        charTableRU['H'] = "Р";
        charTableRU['C'] = "С";
        charTableRU['N'] = "Т";
        charTableRU['E'] = "У";
        charTableRU['A'] = "Ф";
        charTableRU['{'] = "Х";
        charTableRU['['] = "х";
        charTableRU['W'] = "Ц";
        charTableRU['X'] = "Ч";
        charTableRU['I'] = "Ш";
        charTableRU['O'] = "Щ";
        charTableRU[']'] = "Ъ";
        charTableRU['S'] = "Ы";
        charTableRU['M'] = "Ь";
        charTableRU['\''] = "Э";
        charTableRU['.'] = "ю";
        charTableRU['>'] = "Ю";
        charTableRU['Z'] = "Я";
        charTableRU[' '] = " ";

        for (int i = 0; i < charTable.length; i++) {
            char idxEN = (char) ((char) i + START_CHAR);
            char lowerEN = new String(new char[]{idxEN}).toLowerCase().charAt(0);
            if ((charTable[i] != null)) {
                if ((charTable[lowerEN - START_CHAR] == null)) {
                    charTable[lowerEN - START_CHAR] = charTable[i].toLowerCase();
                }
            }
        }
        for (int i = 0; i < charTableRU.length; i++) {
            char idxRU = (char) i;
            char lowerRU = new String(new char[]{idxRU}).toLowerCase().charAt(0);
            if ((charTableRU[i] != null)) {
                if ((charTableRU[lowerRU] == null)) {
                    charTableRU[lowerRU] = charTableRU[i].toLowerCase();
                }
            }
        }
    }

    public static String doSwitch(String text) {
        char charBuffer[] = text.toCharArray();
        StringBuilder sb = new StringBuilder(text.length());
        for (char symbol : charBuffer) {
            int i = symbol - START_CHAR;

            if (i >= 0 && i < charTable.length) {
                String replace = charTable[i];
                sb.append(replace == null ? symbol : replace);
            } else {
                i = symbol;
                String replace = null;
                if (i >= 0 && i < charTableRU.length) {
                    replace = charTableRU[i];
                }
                sb.append(replace == null ? symbol : replace);
            }
        }
        return sb.toString();
    }
}
