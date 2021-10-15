package org.redlamp.util;

public class EICrypt
{

    public static void main(String[] args)
    {

        System.out.println(decrypt("911811301021121901511"));
    }

    public static String encrypt(String srcString)
    {
        if (srcString == null)
        {
            return null;
        }

        String rsDestText = "";
        String sKey = GenerateKey();
        String psSourceText = ReverseString(srcString);

        for (int nTextLength = psSourceText.length(); nTextLength > 0; nTextLength = psSourceText.length())
        {
            String sCurrentChar = String.valueOf(psSourceText.charAt(0));
            psSourceText = psSourceText.substring(1);

            int i = sCurrentChar.charAt(0);
            String sHold = sKey.substring(nTextLength + 1, nTextLength + 2);

            long j = Long.valueOf(sHold);
            long nCurrentVal = (long) i + j;

            sCurrentChar = String.valueOf(nCurrentVal).trim();
            if (sCurrentChar.length() == 1)
            {
                sCurrentChar = "00".concat(String.valueOf(String.valueOf(sCurrentChar)));
            } else if (sCurrentChar.length() == 2)
            {
                sCurrentChar = "0".concat(String.valueOf(String.valueOf(sCurrentChar)));
            }
            rsDestText = String.valueOf(rsDestText) + String.valueOf(sCurrentChar);
        }

        rsDestText = IntSkipString(rsDestText);
        return ReverseString(rsDestText);
    }

    public static String decrypt(String srcString)
    {
        if (srcString == null)
        {
            return null;
        }

        String rsDestText = "";
        String sKey = GenerateKey();
        String psSourceText = ReverseString(srcString);

        psSourceText = IntSkipString(psSourceText);
        for (int nTextLength = psSourceText.length() / 3; nTextLength > 0; nTextLength = psSourceText.length() / 3)
        {
            String sCurrentChar = psSourceText.substring(0, 3);
            psSourceText = psSourceText.substring(3);

            int i = Integer.valueOf(sCurrentChar);
            String sHold = sKey.substring(nTextLength + 1, nTextLength + 2);

            long j = Long.valueOf(sHold);
            long nCurrentVal = (long) i - j;

            sCurrentChar = String.valueOf((char) (int) nCurrentVal);
            rsDestText = String.valueOf(rsDestText) + String.valueOf(sCurrentChar);
        }

        return ReverseString(rsDestText);
    }

    private static String ReverseString(String Inputstr)
    {
        String oString = "";
        int j = Inputstr.length() - 1;

        for (int i = j; i >= 0; i--)
        {
            oString = String.valueOf(oString) + String.valueOf(Inputstr.charAt(i));
        }

        return oString;
    }

    private static String GenerateKey()
    {
        double pi = 3.14159265358979D;
        double log2 = 0.69314718055993996D;

        String sTemp1 = String.valueOf(pi);
        String sKey = sTemp1.substring(sTemp1.length() - 12);

        String sTemp2 = String.valueOf(log2);
        sTemp1 = sTemp2.substring(sTemp2.length() - 12);

        sKey = String.valueOf(sKey) + String.valueOf(sTemp1);
        sTemp1 = sKey;

        sKey = StrRepeat(sTemp1, 10);
        return sKey;
    }

    private static String StrRepeat(String sStr, int nTimes)
    {
        String Destr = sStr;
        for (int i = 1; i < nTimes; i++)
        {
            Destr = String.valueOf(Destr) + String.valueOf(sStr);
        }

        return Destr;
    }

    static String IntSkipString(String psSourceText)
    {
        String DestStr = "";
        String sTempChunk = "", sTemp1;
        String saTextChunks[] = new String[psSourceText.length()];

        int nBytes;
        if ((double) psSourceText.length() % 2D == (double) 0)
        {
            nBytes = 2;
        } else if ((double) psSourceText.length() % 3D == (double) 0)
        {
            nBytes = 3;
        } else
        {
            nBytes = 1;
        }

        int nCount;
        for (nCount = 2; psSourceText.length() != 0; nCount++)
        {
            sTemp1 = psSourceText.substring(0, nBytes);
            saTextChunks[nCount] = sTemp1;
            psSourceText = psSourceText.substring(nBytes);
        }

        int nMax = nCount;
        nCount = 2;
        int nChunkCount = 0;
        while (nCount <= nMax)
        {
            if (nChunkCount < 2)
            {
                if (nCount % 2 == 0)
                {
                    if (nCount < nMax)
                    {
                        sTempChunk = String.valueOf(sTempChunk) + String.valueOf(saTextChunks[nCount]);
                    }
                } else if (nCount != nMax)
                {
                    sTemp1 = saTextChunks[nCount];
                    sTemp1 = String.valueOf(sTemp1) + String.valueOf(sTempChunk);
                    sTempChunk = sTemp1;
                }
                nChunkCount++;
                nCount++;
            } else
            {
                DestStr = String.valueOf(DestStr) + String.valueOf(sTempChunk);
                sTempChunk = "";
                nChunkCount = 0;
            }
        }
        DestStr = String.valueOf(DestStr) + String.valueOf(sTempChunk);
        return DestStr;
    }
}