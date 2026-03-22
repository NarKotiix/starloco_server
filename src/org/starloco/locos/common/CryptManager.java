package org.starloco.locos.common;

import org.apache.commons.lang.StringEscapeUtils;
import org.starloco.locos.area.map.CellCacheImpl;
import org.starloco.locos.area.map.GameCase;
import org.starloco.locos.area.map.GameMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class CryptManager {

    public final static char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '-', '_'};

    /** Table de correspondance inversée HASH[c] → index, O(1) au lieu de O(64). */
    private static final int[] HASH_REVERSE;
    static {
        HASH_REVERSE = new int[128];
        Arrays.fill(HASH_REVERSE, -1);
        for (int i = 0; i < HASH.length; i++) {
            HASH_REVERSE[HASH[i]] = i;
        }
    }

    private final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final int MAX_MAPDATA_CHARS = 200_000; // garde-fou anti-DoS
    private static final int MAX_MESSAGE_CHARS = 16_384;  // borne raisonnable pour paquets chiffrés

    public String cellID_To_Code(int cellID) {

        int char1 = cellID / 64, char2 = cellID % 64;
        return HASH[char1] + "" + HASH[char2];
    }

    public static int cellCode_To_ID(String cellCode) {
        if (cellCode == null || cellCode.length() < 2) {
            return -1;
        }
        char char1 = cellCode.charAt(0), char2 = cellCode.charAt(1);
        int code1 = (char1 < 128 ? HASH_REVERSE[char1] : -1);
        int code2 = (char2 < 128 ? HASH_REVERSE[char2] : -1);
        if (code1 < 0 || code2 < 0) {
            return -1;
        }
        return (code1 << 6) + code2;
    }

    public static int getIntByHashedValue(char c) {
        return (c < 128) ? HASH_REVERSE[c] : -1;
    }

    public static char getHashedValueByInt(int c) {
        return (c >= 0 && c < HASH.length) ? HASH[c] : '\0';
    }

    public ArrayList<GameCase> parseStartCell(GameMap map, int num) {
        if (map == null || num < 0) {
            return null;
        }

        String places = map.getPlaces();
        if (places == null || places.equalsIgnoreCase("-1")) {
            return null;
        }

        String infos = extractPlaceSegment(places, num);
        if (infos == null || infos.isEmpty()) {
            return null;
        }

        ArrayList<GameCase> list = new ArrayList<>();
        for (int a = 0; a + 1 < infos.length(); a += 2) {
            int hi = getIntByHashedValue(infos.charAt(a));
            int lo = getIntByHashedValue(infos.charAt(a + 1));
            if (hi < 0 || lo < 0) {
                continue;
            }
            GameCase cell = map.getCase((hi << 6) + lo);
            if (cell != null && cell.isWalkable(false)) {
                list.add(cell);
            }
        }
        return list;
    }

    public String key = "8fd8ad4a38cdd0432248a76f8f148ceb";

    private boolean[] cellWalkable(GameMap map) {
        int h = map.getH();
        int w = map.getW() - 1;
        int maxCellId = (h * (w + 1) + ((h - 1) * w)) - 1;
        if (maxCellId < 0) {
            return new boolean[0];
        }

        boolean[] forbidden = new boolean[maxCellId + 1];
        int val = 0;
        for (int y = 0; y < h; y++) {
            if (y == 0) val = w;
            else val = val + (w * 2) + 1;

            if (val >= 0 && val < forbidden.length) {
                forbidden[val] = true;
            }
            int mirror = val - w;
            if (mirror >= 0 && mirror < forbidden.length) {
                forbidden[mirror] = true;
            }
        }

        for (int x = 1; x < w; x++) {
            if (x >= 0 && x < forbidden.length) {
                forbidden[x] = true;
            }
            int down = maxCellId - x;
            if (down >= 0 && down < forbidden.length) {
                forbidden[down] = true;
            }
        }
        return forbidden;
    }

    public String prepareMapDataKey(String key) {
        StringBuilder data = new StringBuilder();
        int num2 = (key.length() - 2), i = 0;

        while ((i <= num2)) {
            data.append((char) Integer.parseInt(key.substring(i, i+2), 16));
            i = (i + 2);
        }

        return unescape(data.toString());
    }

    private String unescape(String data) {
        return StringEscapeUtils.unescapeJava(data);
    }

    public String checksumKey(String data) {
        int num = 0;
        int num3 = (data.length() - 1);
        int i = 0;
        while ((i <= num3)) {
            num = (num + ((int) (data.substring(i, i + 1).charAt(0)) % 16));
            i++;
        }

        String[] strArray = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        return strArray[(num % 16)];
    }

    public String decryptMapData(String mapData, String key) {
        if (mapData == null || mapData.isEmpty() || key == null || key.isEmpty() || (mapData.length() & 1) != 0) {
            return "";
        }

        key = prepareKey(key);
        if (key == null || key.isEmpty()) {
            return "";
        }

        String strsum = checksumKey(key);
        int checksum = Integer.parseInt(strsum, 16) * 2;
        return decypherData(mapData, key, checksum);
    }

    public String decypherData(String Data, String Key, int Checksum) {
        if (Data == null || Key == null || Key.isEmpty() || (Data.length() & 1) != 0) {
            return "";
        }

        StringBuilder dataToDecrypt = new StringBuilder(Data.length() / 2);
        int num4 = (Data.length() - 2);
        int i = 0;
        while ((i <= num4)) {
            int num = parseHexByte(Data, i);
            if (num < 0) {
                return "";
            }
            int s = (((i / 2) + Checksum) % Key.length());
            int num2 = Key.charAt(s);
            dataToDecrypt.append((char) (num ^ num2));
            i = (i + 2);
        }
        return unescape(dataToDecrypt.toString());
    }

    /**
     * Détecte si le mapData est en format chiffré (XOR + hex).
     *
     * <p>Deux formats de mapData sont supportés :
     * <ul>
     *   <li><b>Format HASH plain</b> : 10 chars/cellule, encodé en base64 Dofus (HASH).
     *       Exemple : {@code HhaaeaaaaaHhaaeaaaaa...}
     *       Caractéristique : contient des lettres hors [0-9a-fA-F] dès les premiers chars.</li>
     *   <li><b>Format hex chiffré (XOR)</b> : 20 chars/cellule (2 chars hex par byte),
     *       chiffré XOR avec la clé de la map.
     *       Exemple : {@code 0512182d12113c071b05...}
     *       Caractéristique : seulement des chars [0-9a-fA-F].</li>
     * </ul>
     *
     * <p>Détection en O(1) : on vérifie les 16 premiers caractères.
     * Un mapData plain HASH contient forcément des chars hors hexadécimal
     * (ex. 'H', 'G', 'b', etc.) dans ses premières cellules.
     * Un mapData chiffré XOR est exclusivement hexadécimal.
     *
     * @param mapData la chaîne brute stockée en base
     * @return {@code true} si le mapData est en format hex chiffré, {@code false} s'il est plain
     */
    private boolean mapCrypted(String mapData) {
        if (mapData == null || mapData.isEmpty()) {
            return false;
        }
        // Vérification O(1) : les 16 premiers chars suffisent.
        // Un plain HASH contient dès la 1ʳᵉ cellule des chars non-hex (H, G, b, h…).
        // Un hex chiffré XOR ne contient QUE [0-9a-fA-F].
        final int checkLen = Math.min(16, mapData.length());
        for (int i = 0; i < checkLen; i++) {
            char c = mapData.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                return false; // char non-hex → format plain HASH
            }
        }
        return true; // tous hex → format chiffré XOR
    }

    /**
     * Décompile le mapData (plain HASH ou hex chiffré XOR) en liste de {@link GameCase}.
     *
     * <p>Supporte automatiquement les deux formats :
     * <ul>
     *   <li>Format plain HASH : {@code len % 10 == 0}, 10 chars par cellule.</li>
     *   <li>Format hex chiffré XOR : {@code len % 20 == 0}, 20 chars par cellule
     *       (déchiffré avec la clé → 10 chars par cellule).</li>
     * </ul>
     */
    public List<GameCase> decompileMapData(GameMap map, String data, byte sniffed) {
        if (map == null || data == null || data.isEmpty() || data.length() > MAX_MAPDATA_CHARS) {
            return Collections.emptyList();
        }

        // Accepte les deux formats : plain (len%10==0) et hex chiffré (len%20==0)
        if ((data.length() % 10) != 0) {
            return Collections.emptyList();
        }

        if (mapCrypted(data)) {
            // Format hex chiffré : doit être len%20==0 (2 chars/byte, 20 chars/cell)
            if ((data.length() % 20) != 0 || map.getKey() == null || map.getKey().isEmpty()) {
                return Collections.emptyList();
            }
            data = this.decryptMapData(data, map.getKey());
            if (data.isEmpty() || (data.length() % 10) != 0) {
                return Collections.emptyList();
            }
        }

        final int cellCount = data.length() / 10;
        List<GameCase> cells = new ArrayList<>(cellCount);
        List<Short> losCells = new ArrayList<>(cellCount);

        String mapSizeKey = map.getW() + "_" + map.getH();
        if (PathFinding.outForbiddenCells.get(mapSizeKey) == null)
            PathFinding.outForbiddenCells.put(mapSizeKey, cellWalkable(map));
        try {
            for (short cellId = 0; cellId < cellCount; cellId++) {
                int offset = cellId * 10;

                int a0 = getIntByHashedValue(data.charAt(offset));
                int a2 = getIntByHashedValue(data.charAt(offset + 2));
                int a7 = getIntByHashedValue(data.charAt(offset + 7));
                int a8 = getIntByHashedValue(data.charAt(offset + 8));
                int a9 = getIntByHashedValue(data.charAt(offset + 9));
                if (a0 < 0 || a2 < 0 || a7 < 0 || a8 < 0 || a9 < 0) {
                    continue;
                }

                boolean isSpecialBlockedCell =
                        (data.charAt(offset) == 'b' && data.charAt(offset + 1) == 'h' && data.charAt(offset + 2) == 'G' &&
                                data.charAt(offset + 3) == 'a' && data.charAt(offset + 4) == 'e' && data.charAt(offset + 5) == 'a' &&
                                data.charAt(offset + 6) == 'a' && data.charAt(offset + 7) == 'a' && data.charAt(offset + 8) == 'a' &&
                                data.charAt(offset + 9) == 'a')
                                ||
                        (data.charAt(offset) == 'H' && data.charAt(offset + 1) == 'h' && data.charAt(offset + 2) == 'a' &&
                                data.charAt(offset + 3) == 'a' && data.charAt(offset + 4) == 'e' && data.charAt(offset + 5) == 'a' &&
                                data.charAt(offset + 6) == 'a' && data.charAt(offset + 7) == 'a' && data.charAt(offset + 8) == 'a' &&
                                data.charAt(offset + 9) == 'a');

                boolean walkable = (((a2 & 56) >> 3) != 0) && !isSpecialBlockedCell;
                boolean los = (a0 & 1) != 0;
                if(los) {
                    losCells.add(cellId);
                }

                int layerObject2 = ((a0 & 2) << 12) + ((a7 & 1) << 12) + (a8 << 6) + a9;
                boolean layerObject2Interactive = ((a7 & 2) >> 1) != 0;
                int obj = layerObject2Interactive ? layerObject2 : -1;

                cells.add(new GameCase(map, cellId, walkable, los, obj));

            }
            CellCacheImpl cache = new CellCacheImpl(losCells, map.getW(), map.getH());
            map.setCellCache(cache);
        } catch (Exception e) {
            System.err.println("Erreur decompileMapData mapId=" + map.getId() + " : " + e.getMessage());
        }
        return cells;
    }


    // prepareData
    public String cryptMessage(String message, String key) {
        StringBuilder str = new StringBuilder();
        message = message.replace("'", "\'");
        // Append keyId
        str.append(HEX_CHARS[1]);
        // Append checksum
        int checksum = checksum(message);
        str.append(HEX_CHARS[checksum]);
        // Prepare key cause it's hexa form
        int c = checksum * 2;
        String data = encode(message);
        int keyLength = key.length();

        for (int i = 0; i < data.length(); i++)
            str.append(decimalToHexadecimal(data.charAt(i) ^ key.charAt((i + c) % keyLength)));

        return str.toString();
    }

    public String decryptMessage(String message, String key) {
        if (message == null || key == null || key.isEmpty() || message.length() < 2 || message.length() > MAX_MESSAGE_CHARS || (message.length() & 1) != 0) {
            return "";
        }
        try {
            int c = Integer.parseInt(Character.toString(message.charAt(1)), 16) * 2;
            StringBuilder str = new StringBuilder((message.length() - 2) / 2);
            int j = 0, keyLength = key.length();

            for (int i = 2; i < message.length(); i = i + 2) {
                int byteValue = parseHexByte(message, i);
                if (byteValue < 0) {
                    return "";
                }
                str.append((char) (byteValue ^ key.charAt((j++ + c) % keyLength)));
            }
            String data = str.toString();
            data = data.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            data = data.replaceAll("\\+", "%2B");
            return URLDecoder.decode(data, "UTF-8").replace("'", "\\'");
        } catch (Exception e) {
            return "";
        }
    }

    public String prepareKey(String key) {
        if (key == null || key.isEmpty() || (key.length() & 1) != 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder(key.length() / 2);
        for (int i = 0; i < key.length(); i += 2) {
            int value = parseHexByte(key, i);
            if (value < 0) {
                return null;
            }
            sb.append((char) value);
        }

        try {
            return URLDecoder.decode(sb.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private int checksum(String data) {
        int result = 0;
        for(char c : data.toCharArray())
            result += c % 16;
        return result % 16;
    }

    private String decimalToHexadecimal(int c) {
        if(c > 255) c = 255;
        return HEX_CHARS[c / 16] + "" + HEX_CHARS[c % 16];
    }

    private String encode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private boolean isUnsafe(char ch) {
        return ch > 255 || "+%".indexOf(ch) >= 0;
    }

    private static int parseHexByte(String data, int index) {
        if (data == null || index < 0 || index + 1 >= data.length()) {
            return -1;
        }
        int hi = Character.digit(data.charAt(index), 16);
        int lo = Character.digit(data.charAt(index + 1), 16);
        if (hi < 0 || lo < 0) {
            return -1;
        }
        return (hi << 4) + lo;
    }

    private static String extractPlaceSegment(String places, int num) {
        int start = 0;
        for (int i = 0; i < num; i++) {
            int sep = places.indexOf('|', start);
            if (sep == -1) {
                return null;
            }
            start = sep + 1;
        }

        int end = places.indexOf('|', start);
        if (end == -1) {
            end = places.length();
        }
        return start <= end ? places.substring(start, end) : null;
    }
}
