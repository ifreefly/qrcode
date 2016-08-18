/**@author:idevcod@163.com
 * @date:2016年2月1日下午11:08:38
 * @description:<TODO>
 */
package service;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;


public class QRCodeUtil
{
    private final static QRCodeUtil instance = new QRCodeUtil();
    private final String charset = "UTF-8";


    public static QRCodeUtil getInstance()
    {
        return instance;
    }

    public BufferedImage createQRImage(String data, int width, int height)
    {
        try
        {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset),
                    BarcodeFormat.QR_CODE, width, height);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        } catch (WriterException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public String decodeQRImage()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
