package crawling;

/**
 *
 * byte[]配列を受け取り、文字列の場合の文字コード名称(new String(byte[] bytes, String charsetName)
 * で使用可能なもの)のStringを返す。
 *
 */
public class EncodingAnalyzer {
    private static final String ENC_BINALY = null;
    private static final String ENC_UNICODE = "UnicodeLittleUnmarked";
    private static final String ENC_ASCII = "ASCII";
    private static final String ENC_ISO2022JP = "ISO2022JP";
    private static final String ENC_EUC_JP = "EUC_JP";
    private static final String ENC_SJIS = "SJIS";
    private static final String ENC_UTF8 = "UTF8";

    private static final byte bEscape = 0x1B;  // ESC
    private static final byte bAt = 0x40;      // @
    private static final byte bDollar = 0x24;  // $
    private static final byte bAnd = 0x26;     // &
    private static final byte bOpen = 0x28;    // (
    private static final byte bB = 0x42;       // B
    private static final byte bD = 0x44;       // D
    private static final byte bJ = 0x4A;       // J
    private static final byte bI = 0x49;       // I

    public static String analyze(byte[] bytes) {
        int len = bytes.length;
        int b1, b2, b3, b4;
        boolean tmpBool;

        // バイナリーファイル、Unicode　の何れかであるかを判定
        tmpBool = false;
        for (int i = 0; i < len; i++) {
            b1 = bytes[i] & 0xFF;
            if (b1 <= 0x06 || b1 == 0x7F || b1 == 0xFF) {
                // binary
                tmpBool = true;
                if (b1 == 0x00 && i < len - 1 && bytes[i + 1] <= 0x7F) {
                    return ENC_UNICODE;	// Unicodeの半角英数字
                }
            }
        }
        if (tmpBool) {
            return ENC_BINALY;
        }

        // 半角英数字のみかどうかを判定
        tmpBool = true;
        for (int i = 0; i < len; i++) {
            b1 = bytes[i] & 0xFF;
            if (b1 == bEscape || 0x80 <= b1) {	// 半角英数字は0x7Fまで
                tmpBool = false;	// 半角英数字以外を含む
                break;
            }
        }
        if (tmpBool) {
            return ENC_ASCII;
        }

        // iso-2022-jp かどうかを判定
        for (int i = 0; i < len - 2; i++) {
            b1 = bytes[i] & 0xFF;
            b2 = bytes[i + 1] & 0xFF;
            b3 = bytes[i + 2] & 0xFF;

            if (b1 == bEscape) {
                if (b2 == bDollar && b3 == bAt) {	// iso-2022-jp 漢字の開始
                    //JIS_0208 1978
                    return ENC_ISO2022JP;
                } else if (b2 == bDollar && b3 == bB) {	// iso-2022-jp 漢字の開始
                    //JIS_0208 1983
                    return ENC_ISO2022JP;
                } else if (b2 == bOpen && (b3 == bB || b3 == bJ)) {	// iso-2022-jp ASCIIの開始 	JISローマ字の開始
                    //JIS_ASC
                    return ENC_ISO2022JP;
                } else if (b2 == bOpen && b3 == bI) { //	iso-2022-jp 半角カタカナの開始
                    //JIS_KANA
                    return ENC_ISO2022JP;
                }

                if (i < len - 3) {
                    b4 = bytes[i + 3] & 0xFF;

                    if (b2 == bDollar && b3 == bOpen && b4 == bD) {	// iso-2022-jp 補助漢字(JIS X 0212-1990)の開始
                        //JIS_0212
                        return ENC_ISO2022JP;
                    }

                    if (i < len - 5 &&
                        b2 == bAnd && b3 == bAt && b4 == bEscape &&
                        bytes[i + 4] == bDollar && bytes[i + 5] == bB) {	// iso-2022-jp 漢字の開始
                        //JIS_0208 1990
                        return ENC_ISO2022JP;
                    }
                }
            }
        }

        // SJIS, EUC, UTF-8 の何れかを判定
        int sjis = 0;
        int euc = 0;
        int utf8 = 0;

        for (int i = 0; i < len - 1; i++) {
            b1 = bytes[i] & 0xFF;
            b2 = bytes[i + 1] & 0xFF;
            if (((0x81 <= b1 && b1 <= 0x9F) || (0xE0 <= b1 && b1 <= 0xFC)) &&
                ((0x40 <= b2 && b2 <= 0x7E) || (0x80 <= b2 && b2 <= 0xFC))) {
                //SJIS_C
                sjis += 2;
                i++;
            }
        }

        for (int i = 0; i < len - 1; i++) {
            b1 = bytes[i] & 0xFF;
            b2 = bytes[i + 1] & 0xFF;
            if (((0xA1 <= b1 && b1 <= 0xFE) && (0xA1 <= b2 && b2 <= 0xFE)) ||
                (b1 == 0x8E && (0xA1 <= b2 && b2 <= 0xDF))) {
                //EUC_C
                //EUC_KANA
                euc += 2;
                i++;
            } else if (i < len - 2) {
                b3 = bytes[i + 2] & 0xFF;
                if (b1 == 0x8F && (0xA1 <= b2 && b2 <= 0xFE) &&
                    (0xA1 <= b3 && b3 <= 0xFE)) {
                    //EUC_0212
                    euc += 3;
                    i += 2;
                }
            }
        }

        for (int i = 0; i < len - 1; i++) {
            b1 = bytes[i] & 0xFF;
            b2 = bytes[i + 1] & 0xFF;
            if ((0xC0 <= b1 && b1 <= 0xDF) && (0x80 <= b2 && b2 <= 0xBF)) {
                //UTF8
                utf8 += 2;
                i++;
            } else if (i < len - 2) {
                b3 = bytes[i + 2] & 0xFF;
                if ((0xE0 <= b1 && b1 <= 0xEF) && (0x80 <= b2 && b2 <= 0xBF) &&
                    (0x80 <= b3 && b3 <= 0xBF)) {
                    //UTF8
                    utf8 += 3;
                    i += 2;
                }
            }
        }

        if (euc > sjis && euc > utf8)
        {
            //EUC
            return ENC_EUC_JP;
        }
        else if (sjis > euc && sjis > utf8)
        {
            //SJIS
            return ENC_SJIS;
        }
        else if (utf8 > euc && utf8 > sjis)
        {
            //UTF8
            return ENC_UTF8;
        }

        // 以上の何れにも該当しなかった場合、バイナリーとみなす。
        return ENC_BINALY;
    }
}
