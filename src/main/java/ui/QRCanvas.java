/**@author:idevcod@163.com
 * @date:2016年2月1日下午11:23:57
 * @description:<TODO>
 */
package ui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QRCanvas extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(QRCanvas.class);

    private Image image;

    public QRCanvas()
    {
        super();
    }

    public void drawImage(BufferedImage image)
    {
        this.image = image;
        paintComponent(getGraphics());
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        if (image == null)
        {
            LOGGER.debug("image is null!");
            return;
        }

        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
}
