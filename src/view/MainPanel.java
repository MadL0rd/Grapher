package view;

import controller.ModelUpdater;
import framesLib.Screen;
import view.elements.*;
import view.grapher.GraphicsView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static view.elements.ElementsList.OFFSET;

public class MainPanel extends Screen {
    private static final int WIDTH_0 = 1280;
    private static final int HEIGHT_0 = 720;
    public static int WIDTH = WIDTH_0;
    public static int HEIGHT = HEIGHT_0;
    public static int GRAPH_WIDTH = WIDTH - ElementsList.WIDTH;
    private GraphicsView graphicsView;
    private Point mousePosition;
    public MainPanel(){
        setLayout(null);
        JButton btn_help = new JButton("Help");
        btn_help.setBounds(OFFSET,620, ElementsList.WIDTH / 3 - 2 * OFFSET, TextElement.HEIGHT);
        add(btn_help);
        btn_help.addActionListener((e)-> changeScreen(TextViewer.openText("Help")));
        JButton btn_calc_help = new JButton("Calculator help");
        btn_calc_help.setBounds(2*OFFSET + ElementsList.WIDTH / 3, 620, 2 * ElementsList.WIDTH / 3 - 3 * OFFSET, TextElement.HEIGHT);
        add(btn_calc_help);
        btn_calc_help.addActionListener((e)-> changeScreen(TextViewer.openText("Calc_Help")));
        mousePosition = new Point();

        ModelUpdater updater = new ModelUpdater(this::repaint);
        ElementsList graphics = new ElementsList("Graphics", 0, 0, updater::addVRemove, updater::startSettings);
        graphics.addTo(this);
        graphicsView = new GraphicsView(graphics, updater);

        CalculatorView calculator = new CalculatorView(updater::recalculate);
        calculator.addTo(this);
        calculator.setBounds(0, 620 - OFFSET - CalculatorView.CALC_HEIGHT);

        FunctionsView functions = new FunctionsView(updater::recalculate);
        functions.addTo(this);
        functions.setBounds(0, 620 - 3 * OFFSET - CalculatorView.CALC_HEIGHT - FunctionsView.FUNC_HEIGHT);

        updater.setStringElements(functions, calculator);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mousePosition.setLocation(e.getX(), e.getY());
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updater.translate(e.getX() - mousePosition.x, e.getY() - mousePosition.y);
                mousePosition.setLocation(e.getX(), e.getY());
            }
        });
        addMouseWheelListener(e -> updater.resize(e.getPreciseWheelRotation(), e.getX() - ElementsList.WIDTH, e.getY()));
        JButton btn_resize = new JButton("Resize");
        btn_resize.setBounds(OFFSET, 620 + TextElement.HEIGHT + OFFSET, TextElement.WIDTH, TextElement.HEIGHT);
        btn_resize.addActionListener(e -> {
            WIDTH = getWidth();
            HEIGHT = getHeight();
            GRAPH_WIDTH = WIDTH - ElementsList.WIDTH;
            updater.recalculate();
        });
        add(btn_resize);

    }
    @Override
    public void onSetSize() {
        setSize(WIDTH_0, HEIGHT_0);
    }
    @Override
    public void onShow() {
        setTitle("Grapher by Math_way");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        graphicsView.paint(g);
    }
}
