/**@author:idevcod@163.com
 * @date:2016年2月1日下午11:07:42
 * @description:qrcode基础代码
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

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

    private static final String ENCODE = "编码";

    private static final String DECODE = "解码";

    private static final String AUTO_WRAP = "自动换行";

    private static final String AUTO_CLEAN = "自动清除";

    private static final String AUTO_COMPLETE = "开启联想";

    private static final String HISTORY_PATH = "data/history.dat";
    /** 空格是为了排版占位的 */
    private static final String HISTORY_TITLE = "history                     ";

    private JFrame frame;

    private JPanel optionPanel;

    private JPanel btnPanel;

    private JPanel historyPanel;

    private JScrollPane listScroPanel;

    private JList<Record> jList;

    private List<Record> recordList = new ArrayList<Record>();

    private JButton encodeBtn;

    private JButton decodeBtn;

    private JTextArea textArea;

    private JScrollPane textScrollPane;

    private QRCanvas canvas;

    private JScrollPane canvasScrollPane;

    private JCheckBox autoWrapBox;

    private JCheckBox autoCompleteBox;

    private JCheckBox autoCleanBox;

    private JTextField cleanField;

    private JPopupMenu listPopMenu;

    private JMenuItem deleteItem;

    private JPopupMenu sugesstionMenu;

    private DefaultListModel<Record> historyListModel = new DefaultListModel<Record>();

    public MainUI()
    {
        init();
    }

    private void init()
    {
        frame = new JFrame();
        btnPanel = new JPanel();
        optionPanel = new JPanel();
        optionPanel.add(btnPanel);
        autoWrapBox = new JCheckBox(AUTO_WRAP, true);
        autoWrapBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (autoWrapBox.isSelected())
                {
                    textArea.setLineWrap(true);
                } else
                {
                    textArea.setLineWrap(false);
                }
            }
        });

        autoCompleteBox = new JCheckBox(AUTO_COMPLETE, false);
        autoCompleteBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (!autoWrapBox.isSelected())
                {
                    hideSuggestion();
                }
            }
        });

        autoCleanBox = new JCheckBox(AUTO_CLEAN, true);
        cleanField = new JTextField(":", 20);

        optionPanel.add(autoWrapBox);
        optionPanel.add(autoCompleteBox);
        optionPanel.add(autoCleanBox);
        optionPanel.add(cleanField);
        frame.getContentPane().add(optionPanel, BorderLayout.NORTH);

        encodeBtn = new JButton(ENCODE);
        btnPanel.add(encodeBtn);
        encodeBtn.addActionListener((e) ->
        {
            String text = textArea.getText();
            if (text == null || "".equals(text))
            {
                LOGGER.warn("textArea is empty.");
                return;
            }

            if (needClean())
            {
                text = text.replace(cleanField.getText(), "");
                LOGGER.debug("need auto clean,after clean ,the string is {}.", text);
            }

            addRecord(text);

            canvas.drawImage(QRCodeUtil.getInstance().createQRImage(text, canvas.getWidth(), canvas.getHeight()));
        });

        decodeBtn = new JButton(DECODE);
        btnPanel.add(decodeBtn);

        JSplitPane splitPane = new JSplitPane();
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.addKeyListener(new KeyListener()
        {

            @Override
            public void keyTyped(KeyEvent e)
            {
                if (Character.isLetterOrDigit(e.getKeyChar()))
                {
                    if (autoCompleteBox.isSelected())
                    {
                        showSuggestionList();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
                if (sugesstionMenu != null)
                {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN)
                    {
                        JList<Record> jlist = getSuggestList();
                        int index = Math.min(jlist.getSelectedIndex() + 1, jlist.getModel().getSize() - 1);
                        jlist.setSelectedIndex(index);
                        jlist.ensureIndexIsVisible(index);
                    } else if (e.getKeyCode() == KeyEvent.VK_UP)
                    {
                        JList<Record> jlist = getSuggestList();
                        int index = Math.max(jlist.getSelectedIndex() - 1, 0);
                        jlist.setSelectedIndex(index);
                        jlist.ensureIndexIsVisible(index);
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                        JList<Record> jlist = getSuggestList();
                        int index = jlist.getSelectedIndex();
                        if (index < 0)
                        {
                            LOGGER.debug("no suggestion has been choosed");
                            return;
                        }

                        textArea.setText(jlist.getModel().getElementAt(index).getStr());
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    textArea.getDocument().remove(textArea.getCaretPosition() - 1, 1);
                                } catch (BadLocationException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        });

                        if (autoCompleteBox.isSelected())
                        {
                            hideSuggestion();
                        }
                    } else
                    {
                        if (autoCompleteBox.isSelected())
                        {
                            hideSuggestion();
                        }
                    }
                }

            }
        });
        textScrollPane = new JScrollPane(textArea);
        canvas = new QRCanvas();
        canvasScrollPane = new JScrollPane(canvas);
        splitPane.setLeftComponent(textScrollPane);
        splitPane.setRightComponent(canvasScrollPane);
        splitPane.setResizeWeight(0.5);

        historyPanel = new JPanel();

        frame.getContentPane().add(historyPanel, BorderLayout.WEST);

        listPopMenu = new JPopupMenu();
        deleteItem = new JMenuItem("delete");
        deleteItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int index = jList.getSelectedIndex();
                if (index < 0)
                {
                    LOGGER.debug("no recrods has been choosen.");
                    return;
                }

                removeRecordByIndex(index);
            }
        });
        listPopMenu.add(deleteItem);

        jList = new JList<Record>(historyListModel);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jList.addMouseListener(new MouseListener()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    int index = jList.locationToIndex(e.getPoint());
                    if (index < 0)
                    {
                        LOGGER.debug("nothing has been right clicked.");
                        return;
                    }

                    jList.setSelectedIndex(index);
                    listPopMenu.show(jList, e.getX(), e.getY());
                }
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                { // 双击事件
                    Object object = e.getSource();
                    if (object instanceof JList)
                    {
                        @SuppressWarnings("unchecked")
                        JList<Record> jList = (JList<Record>) object;
                        int index = jList.getSelectedIndex();
                        if (index < 0)
                        {
                            LOGGER.debug("select nothing.");
                            return;
                        }

                        Record record = historyListModel.getElementAt(index);
                        if (record == null)
                        {
                            LOGGER.error("cloud get the record,the index is {}.", index);
                            return;
                        }

                        textArea.setText(record.getStr());
                    }
                }
            }
        });

        jList.addMouseMotionListener(new MouseMotionListener()
        {

            @Override
            public void mouseMoved(MouseEvent e)
            {
                Object object = e.getSource();
                if (object instanceof JList)
                {
                    @SuppressWarnings("unchecked")
                    JList<Record> jList = (JList<Record>) e.getSource();
                    int index = jList.locationToIndex(e.getPoint());
                    if (index < 0)
                    {
                        LOGGER.debug("hover nothing!");
                        return;
                    }

                    Record record = historyListModel.getElementAt(index);
                    jList.setToolTipText(record.getStr());
                }
            }

            @Override
            public void mouseDragged(MouseEvent e)
            {
            }
        });

        jList.addKeyListener(new KeyListener()
        {

            @Override
            public void keyTyped(KeyEvent e)
            {
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                {
                    int index = jList.getSelectedIndex();
                    if (index < 0)
                    {
                        LOGGER.debug("no item has been selected!");
                        return;
                    }

                    removeRecordByIndex(index);
                }
            }
        });

        listScroPanel = new JScrollPane(jList);

        historyPanel.setLayout(new BorderLayout(0, 0));
        JLabel historyLabel = new JLabel(HISTORY_TITLE);
        Dimension dimension = historyLabel.getPreferredSize();
        historyPanel.add(historyLabel, BorderLayout.NORTH);
        historyPanel.add(listScroPanel, BorderLayout.CENTER);
        // 不知道为毛height填0可以...先这么用
        historyPanel.setPreferredSize(new Dimension(dimension.width, 0));

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
        if (recordList.contains(record))
        {
            removeRecord(record);
        }

        addRecordToHead(record);
    }

    private void removeRecord(Record record)
    {
        recordList.remove(record);
        historyListModel.removeElement(record);
    }

    private void removeRecordByIndex(int index)
    {
        recordList.remove(index);
        historyListModel.remove(index);
    }

    private void addRecordToHead(Record record)
    {
        recordList.add(0, record);
        historyListModel.insertElementAt(record, 0);
    }

    private void addRecord(Record record)
    {
        recordList.add(record);
        historyListModel.addElement(record);
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

    private boolean needClean()
    {
        return autoCleanBox.isSelected();
    }

    private void showSuggestionList()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (sugesstionMenu == null)
                {
                    sugesstionMenu = createSuggestionMenu();
                }

                showSuggestion();
            }

        });
    }

    private void showSuggestion()
    {
        Point location = null;

        String text = textArea.getText();
        if (text == null || "".equals(text))
        {
            LOGGER.debug("textArea is null");
            return;
        }

        JList<Record> suggestionList = getSuggestList();
        DefaultListModel<Record> listModel = (DefaultListModel<Record>) suggestionList.getModel();
        if (listModel.size() != 0)
        {
            for (int i = listModel.getSize() - 1; i >= 0; i--)
            {
                Record record = listModel.getElementAt(i);
                if (!record.getStr().startsWith(text))
                {
                    listModel.removeElementAt(i);
                }
            }
        } else
        {
            hideSuggestion();
            for (Record record : recordList)
            {
                if (record.getStr().startsWith(text))
                {
                    listModel.addElement(record);
                }
            }

            int position = textArea.getCaretPosition();
            try
            {
                location = textArea.modelToView(position).getLocation();
            } catch (BadLocationException e)
            {
                LOGGER.error("BadLocationException, exception is {}.", e);
                return;
            }

            if (location == null)
            {
                LOGGER.error("location is null!");
                return;
            }

            sugesstionMenu.show(textArea, location.x, textArea.getBaseline(0, 0));
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    textArea.requestFocusInWindow();
                }
            });
        }

        if (listModel.size() <= 0)
        {
            LOGGER.debug("records not found.");
            hideSuggestion();
            return;
        }
    }

    private void hideSuggestion()
    {
        if (sugesstionMenu == null)
        {
            return;
        }

        sugesstionMenu.setVisible(false);
        JList<Record> jList = getSuggestList();
        jList.setSelectedIndex(-1);
        ((DefaultListModel<Record>) jList.getModel()).removeAllElements();
    }

    @SuppressWarnings("unchecked")
    private JList<Record> getSuggestList()
    {
        if (sugesstionMenu != null)
        {
            JScrollPane scrollPane = (JScrollPane) sugesstionMenu.getComponent(0);
            JViewport viewport = (JViewport) scrollPane.getComponent(0);
            return (JList<Record>) viewport.getComponent(0);
        }

        // 减少判空
        return new JList<Record>();
    }

    private JPopupMenu createSuggestionMenu()
    {
        JPopupMenu sugesstionMenu = new JPopupMenu();
        sugesstionMenu.setPreferredSize(new Dimension(textArea.getWidth() / 2, textArea.getHeight() / 2));
        JList<Record> sugesstionList = new JList<Record>(new DefaultListModel<>());
        sugesstionList.addMouseListener(new MouseListener()
        {
            
            @Override
            public void mouseReleased(MouseEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void mousePressed(MouseEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void mouseExited(MouseEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void mouseEntered(MouseEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                { // 双击事件
                    Object object = e.getSource();
                    if (object instanceof JList)
                    {
                        @SuppressWarnings("unchecked")
                        JList<Record> jList = (JList<Record>) object;
                        int index = jList.getSelectedIndex();
                        if (index < 0)
                        {
                            LOGGER.debug("select nothing.");
                            return;
                        }

                        Record record = jList.getModel().getElementAt(index);
                        if (record == null)
                        {
                            LOGGER.error("cloud get the record,the index is {}.", index);
                            return;
                        }

                        textArea.setText(record.getStr());
                        hideSuggestion();
                    }
                }
            }
        });
        JScrollPane suggestionScrollPane = new JScrollPane(sugesstionList);
        sugesstionMenu.add(suggestionScrollPane);

        return sugesstionMenu;
    }

}
