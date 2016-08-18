/**@author:idevcod@163.com
 * @date:2016年2月1日下午11:41:51
 * @description:<TODO>
 */
package main;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ui.MainUI;

public class Main
{

    public static void main(String[] args)
    {
        final Logger logger = LoggerFactory.getLogger(Main.class);

        EventQueue.invokeLater(new Runnable()
        {

            @SuppressWarnings("unused")
            @Override
            public void run()
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    new MainUI();
                } catch (ClassNotFoundException e)
                {
                    logger.error("ClassNotFoundException, exception is {}.", e);
                } catch (InstantiationException e)
                {
                    logger.error("InstantiationException, exception is {}.", e);
                } catch (IllegalAccessException e)
                {
                    logger.error("IllegalAccessException, exception is {}.", e);
                } catch (UnsupportedLookAndFeelException e)
                {
                    logger.error("UnsupportedLookAndFeelException, exception is {}.", e);
                }
            }
        });
    }
}
