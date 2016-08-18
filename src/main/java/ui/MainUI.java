/**@author:idevcod@163.com
 * @date:2016年2月1日下午11:07:42
 * @description:qrcode基础代码
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import model.Record;
import service.QRCodeUtil;

public class MainUI
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MainUI.class);

    private static final String HISTORY_PATH = "data/history.dat";

    private JFrame frame;

    private JPanel btnPanel;

    private JPanel historyPanel;

    private JList<Record> jList;

    private List<Record> recordList = new ArrayList<Record>();

    private JButton encodeBtn;

    private JButton decodeBtn;

    private JTextArea textArea;

    private QRCanvas canvas;

    private DefaultListModel<Record> historyList = new DefaultListModel<Record>();

    public MainUI()
    {
        init();
    }

    private void init()
    {
        frame = new JFrame();
        btnPanel = new JPanel();
        frame.getContentPane().add(btnPanel, BorderLayout.NORTH);

        encodeBtn = new JButton("编码");
        btnPanel.add(encodeBtn);
        encodeBtn.addActionListener((e) ->
        {
            String text = textArea.getText();
            if (text == null || "".equals(text))
            {
                LOGGER.warn("textArea is empty.");
                return;
            }

            addRecord(text);

            canvas.drawImage(QRCodeUtil.getInstance().createQRImage(text, canvas.getWidth(), canvas.getHeight()));
        });

        decodeBtn = new JButton("解码");
        btnPanel.add(decodeBtn);

        JSplitPane splitPane = new JSplitPane();
        textArea = new JTextArea();
        canvas = new QRCanvas();
        splitPane.setLeftComponent(textArea);
        splitPane.setRightComponent(canvas);
        splitPane.setResizeWeight(0.5);

        historyPanel = new JPanel();

        frame.getContentPane().add(historyPanel, BorderLayout.WEST);

        jList = new JList<Record>(historyList);
        historyPanel.setLayout(new BorderLayout(0, 0));

        historyPanel.add(new JLabel("history"), BorderLayout.NORTH);
        historyPanel.add(jList, BorderLayout.CENTER);

        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowOpened(WindowEvent windowEvent)
            {
                readFromLocal();
            }

            @Override
            public void windowClosing(WindowEvent windowEvent)
            {
                LOGGER.debug("123");
                saveToLocal();
                System.exit(0);

            }
        });

        frame.setPreferredSize(new Dimension(600, 300));
        frame.pack();
        frame.setLocationRelativeTo(null);
        try
        {
            // 1.6+
            frame.setLocationByPlatform(true);
            frame.setMinimumSize(frame.getSize());
        } catch (Throwable ignoreAndContinue)
        {
        }

        frame.setVisible(true);
    }

    private void addRecord(String text)
    {
        Record record = new Record(text);
        addRecord(record);
    }

    private void addRecord(Record record)
    {
        recordList.add(record);
        historyList.addElement(record);
    }

    private void saveToLocal()
    {
        File file = new File(HISTORY_PATH);
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try
        {
            if (!file.exists())
            {
                File parentFile = file.getParentFile();
                if (!parentFile.exists())
                {
                    LOGGER.debug("parent dir not exist ,then create.");
                    parentFile.mkdirs();
                }

                file.createNewFile();
            }

            bufferedWriter = new BufferedWriter(new FileWriter(file));
            new Gson().toJson(recordList, bufferedWriter);
        } catch (IOException e)
        {
            LOGGER.error("save file failed, exception is {}.", e);
        } finally
        {
            closeQuietly(fileWriter);
            closeQuietly(bufferedWriter);
        }
    }

    private void readFromLocal()
    {
        File file = new File(HISTORY_PATH);
        if (!file.exists())
        {
            LOGGER.debug("no file to load.skip");
            return;
        }

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try
        {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);

            List<Record> tmpRecordList = new Gson().fromJson(bufferedReader, new TypeToken<List<Record>>()
            {
            }.getType());

            if (tmpRecordList != null)
            {
                for (Record record : tmpRecordList)
                {
                    addRecord(record);
                }
            } else
            {
                LOGGER.warn("no records to load.");
                ;
            }

        } catch (JsonSyntaxException e)
        {
            LOGGER.error("JsonSyntaxException, exception is {}.", e);
        } catch (JsonIOException e)
        {
            LOGGER.error("JsonIOException, exception is {}.", e);
        } catch (FileNotFoundException e)
        {
            LOGGER.error("FileNotFoundException, exception is {}.", e);
        } finally
        {
            closeQuietly(fileReader);
            closeQuietly(bufferedReader);
        }
    }

    private void closeQuietly(Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            } catch (IOException e)
            {
                LOGGER.error("close faield,exception is {}.", e);
            }
        }
    }
}
