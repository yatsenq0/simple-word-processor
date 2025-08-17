/**
 * Простой текстовый редактор, имитирующий базовую функциональность Atlantis Word Processor.
 * Поддерживает создание, открытие, редактирование и сохранение файлов с расширением .doc
 * (на основе HTML-содержимого, совместимого с Microsoft Word).
 *
 * <p>Основные возможности:</p>
 * <ul>
 *   <li>Создание новых документов</li>
 *   <li>Открытие существующих .doc или .html файлов</li>
 *   <li>Сохранение в формате .doc (на самом деле — HTML с расширением .doc)</li>
 *   <li>Вставка заголовков (через меню)</li>
 *   <li>Вставка и кликабельность гиперссылок</li>
 * </ul>
 *
 * <p>Технологии:</p>
 * <ul>
 *   <li>Java SE</li>
 *   <li>Swing (графический интерфейс)</li>
 *   <li>HTMLEditorKit / HTMLDocument (редактирование HTML)</li>
 * </ul>
 *
 * @author Simple Word Processor
 * @version 1.0
 * @since 2025
 */
package com.example;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class Main extends JFrame {

    /**
     * Текстовое поле для редактирования HTML-документа.
     */
    private JEditorPane editorPane;

    /**
     * Редакторский компонент для работы с HTML.
     */
    private HTMLEditorKit editorKit;

    /**
     * Документ, содержащий HTML-содержимое.
     */
    private HTMLDocument document;

    /**
     * Путь к текущему открытому файлу. null, если файл не сохранялся.
     */
    private String currentFilePath;

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Устанавливаем системный внешний вид (Windows, если доступен)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace(); // Игнорируем, если не удалось
            }
            new Main().setVisible(true);
        });
    }

    /**
     * Конструктор главного окна редактора.
     * Инициализирует интерфейс, редактор, меню и настраивает окно.
     */
    public Main() {
        initializeUI();
        setupEditor();
        setupMenu();
        setTitle("Простой текстовый редактор");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null); // Центрируем окно
        setIconImage(createIcon());
    }

    /**
     * Создаёт иконку окна (может быть null — тогда используется стандартная).
     *
     * @return ImageIcon иконки или null
     */
    private Image createIcon() {
        try {
            // В реальном проекте можно загрузить из resources
            // Здесь оставим null — будет стандартная иконка Java
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Инициализирует графический интерфейс: создаёт JEditorPane и добавляет его в JScrollPane.
     */
    private void initializeUI() {
        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(true);
        editorPane.setText("<html><body style='font-family: Arial, sans-serif; font-size: 14px;'>"
                + "<p>Начните вводить текст...</p></body></html>");

        // Обработка кликов по гиперссылкам
        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    URI uri = e.getURL().toURI();
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(uri);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Не удалось открыть ссылку. Попробуйте скопировать URL в браузер.");
                    }
                } catch (URISyntaxException ex) {
                    JOptionPane.showMessageDialog(this, "Некорректный URL: " + ex.getMessage());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка при открытии ссылки: " + ex.getMessage());
                }
            }
        });

        // Добавляем возможность вставки через Ctrl+V
        editorPane.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        // Добавляем редактор в прокручиваемую панель
        add(new JScrollPane(editorPane), BorderLayout.CENTER);
    }

    /**
     * Настраивает HTML-редактор и получает доступ к документу.
     */
    private void setupEditor() {
        editorKit = new HTMLEditorKit();
        document = (HTMLDocument) editorPane.getDocument();
        editorPane.setEditorKit(editorKit);
    }

    /**
     * Создаёт меню "Файл" и "Формат" с пунктами:
     * - Новый, Открыть, Сохранить, Сохранить как, Выход
     * - Вставить заголовок, Вставить ссылку
     */
    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Меню "Файл"
        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newFile = new JMenuItem("Новый");
        newFile.setMnemonic(KeyEvent.VK_N);
        newFile.addActionListener(e -> newFile());

        JMenuItem openFile = new JMenuItem("Открыть...");
        openFile.setMnemonic(KeyEvent.VK_O);
        openFile.addActionListener(e -> openFile());

        JMenuItem saveFile = new JMenuItem("Сохранить");
        saveFile.setMnemonic(KeyEvent.VK_S);
        saveFile.addActionListener(e -> saveFile(false));

        JMenuItem saveAsFile = new JMenuItem("Сохранить как...");
        saveAsFile.addActionListener(e -> saveFile(true));

        JMenuItem exit = new JMenuItem("Выход");
        exit.setMnemonic(KeyEvent.VK_X);
        exit.addActionListener(e -> System.exit(0));

        fileMenu.add(newFile);
        fileMenu.add(openFile);
        fileMenu.addSeparator();
        fileMenu.add(saveFile);
        fileMenu.add(saveAsFile);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        // Меню "Формат"
        JMenu formatMenu = new JMenu("Формат");
        formatMenu.setMnemonic(KeyEvent.VK_R);

        JMenuItem insertHeader = new JMenuItem("Вставить заголовок");
        insertHeader.addActionListener(e -> insertHTML("<h1>Заголовок</h1>"));

        JMenuItem insertLink = new JMenuItem("Вставить ссылку...");
        insertLink.addActionListener(e -> insertLink());

        formatMenu.add(insertHeader);
        formatMenu.add(insertLink);

        menuBar.add(fileMenu);
        menuBar.add(formatMenu);

        setJMenuBar(menuBar);
    }

    /**
     * Создаёт новый пустой документ.
     */
    private void newFile() {
        editorPane.setText("<html><body style='font-family: Arial, sans-serif; font-size: 14px;'>" +
                "<p></p></body></html>");
        currentFilePath = null;
        setTitle("Новый документ — Простой текстовый редактор");
    }

    /**
     * Открывает файл через диалоговое окно.
     * Поддерживаются .doc, .html, .htm.
     */
    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Открыть файл");
        chooser.setFileFilter(new FileNameExtensionFilter("Документы (.doc, .html)", "doc", "html", "htm"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                editorPane.setText(content.toString());
                currentFilePath = file.getAbsolutePath();
                setTitle(file.getName() + " — Простой текстовый редактор");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Не удалось открыть файл: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Сохраняет текущий документ.
     *
     * @param saveAs если true — всегда показывает диалог сохранения
     */
    private void saveFile(boolean saveAs) {
        if (currentFilePath == null || saveAs) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Сохранить как");
            chooser.setFileFilter(new FileNameExtensionFilter("DOC/HTML файлы", "doc", "html"));
            chooser.setSelectedFile(new File("document.doc"));

            int result = chooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) return;

            File file = chooser.getSelectedFile();
            String filename = file.getAbsolutePath();

            // Автоматически добавляем расширение .doc, если нет
            if (!filename.toLowerCase().endsWith(".doc") && !filename.toLowerCase().endsWith(".html")) {
                filename += ".doc";
                file = new File(filename);
            }

            currentFilePath = filename;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(currentFilePath))) {
            writer.print(editorPane.getText());
            JOptionPane.showMessageDialog(this, "Файл сохранён как:\n" + currentFilePath);
            setTitle(new File(currentFilePath).getName() + " — Простой текстовый редактор");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при сохранении файла:\n" + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Вставляет HTML-код в текущую позицию курсора.
     *
     * @param html HTML-фрагмент для вставки
     */
    private void insertHTML(String html) {
        try {
            int pos = editorPane.getCaretPosition();
            editorKit.insertHTML(document, pos, html, 0, 0, null);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при вставке HTML: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Диалог для вставки гиперссылки.
     */
    private void insertLink() {
        String url = JOptionPane.showInputDialog(this, "Введите URL (например, https://example.com):");
        if (url == null || url.trim().isEmpty()) return;

        String text = JOptionPane.showInputDialog(this, "Текст ссылки:", url);
        if (text == null || text.trim().isEmpty()) text = url;

        String link = String.format("<a href=\"%s\" style=\"color: blue; text-decoration: underline;\">%s</a>", url, text);
        insertHTML(link);
    }
}
