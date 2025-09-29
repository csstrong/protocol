package com.wayeal.cloud.utils;

import java.math.BigInteger;

public class CrcCheckUtil {

    /**
     * crc modbus 16 校验
     * @return
     */
    public static String getCrcModbus( byte[] bytes) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        String crc = Integer.toHexString(CRC);
        if(crc.length()==3){
            crc="0"+crc;
        }
        if (crc.length()==2){
            crc="00"+crc;
        }
        if (crc.length()==1){
            crc="000"+crc;
        }
        return crc;
    }


}
