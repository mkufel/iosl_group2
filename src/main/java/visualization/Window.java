package visualization;

import org.graphstream.ui.swingViewer.ViewPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Window extends JFrame {

    VisualizationEngine visualizationEngine;
    ViewPanel graphView;

    private static class TextIndicator extends JTextField {
        TextIndicator() {
            super();
            this.setEditable(false);
            this.setFocusable(false);
            this.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private static class Button extends JButton {
        Button(String text) {
            super(text);
            this.setFocusable(false);
        }
    }

    public Window(String title, ViewPanel graphView, VisualizationEngine engine) throws HeadlessException {
        super(title);
        this.visualizationEngine = engine;
        this.graphView = graphView;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setJMenuBar(this.createMenuBar());
        this.getContentPane().add(this.createToolBar(), BorderLayout.NORTH);

        graphView.setPreferredSize(new Dimension(800, 600));
        graphView.resizeFrame(800, 600);
        this.getContentPane().add(graphView, BorderLayout.CENTER);


/*
        ((Component) graphView).addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                e.consume();
                System.out.println(e.toString());
                int i = 2;
                double factor = Math.pow(1.25, i);
                Camera cam = graphView.getCamera();
                double zoom = cam.getViewPercent() * factor;
                Point2 pxCenter = cam.transformGuToPx(cam.getViewCenter().x, cam.getViewCenter().y, 0);
                Point3 guClicked = cam.transformPxToGu(e.getX(), e.getY());
                double newRatioPx2Gu = cam.getMetrics().ratioPx2Gu / factor;
                double x = guClicked.x + (pxCenter.x - e.getX()) / newRatioPx2Gu;
                double y = guClicked.y - (pxCenter.y - e.getY()) / newRatioPx2Gu;
                cam.setViewCenter(x, y, 0);
                cam.setViewPercent(zoom);
            }
        });*/
        this.pack();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menu = new JMenuBar();

        JMenu menuFile = createFileMenu();

        JMenu menuEdit = createEditMenu();

        JMenu menuHelp = createHelpMenu();

        menu.add(menuFile);
        menu.add(menuEdit);
        menu.add(menuHelp);

        return menu;
    }

    private JMenu createHelpMenu() {
        JMenu menuHelp = new JMenu("Help");
        menuHelp.add(new JMenuItem("About"));
        return menuHelp;
    }

    private JMenu createEditMenu() {
        JMenu menuEdit = new JMenu("Edit");
        menuEdit.add(new JMenuItem("Pause/Resume")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                graphView.resizeFrame(800, 600);
                graphView.repaint();
                // visualizationEngine.toggleSimulation();x
            }
        });
        menuEdit.add(new JMenuItem("Step back")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // graphView.getCamera().setGraphViewport();
                visualizationEngine.setCurrentTick(visualizationEngine.getCurrentTick() - 1);
            }
        });
        menuEdit.add(new JMenuItem("Step forward")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visualizationEngine.setCurrentTick(visualizationEngine.getCurrentTick() + 1);
            }
        });
        menuEdit.add(new JSeparator());
        menuEdit.add(new JMenuItem("Restart visualization")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphView.getCamera().resetView();

                // visualizationEngine.restart();
            }
        });
        menuEdit.add(new JMenuItem("Reload simulation")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Reload pressed.");
            }
        });
        return menuEdit;
    }

    private JMenu createFileMenu() {
        JMenu menuFile = new JMenu("File");
        menuFile.add(new JMenuItem("Open"));
        menuFile.add(new JMenuItem("Save as..."));
        menuFile.add(new JSeparator());
        menuFile.add(new JMenuItem("Settings"));
        menuFile.add(new JMenuItem("Exit"));
        return menuFile;
    }

    private JToolBar createToolBar() {
        JToolBar tools = new JToolBar();

        tools.setFloatable(false);
        tools.setRollover(true);
        tools.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel labelSimulatedPopulation = new JLabel("Simulated population");
        TextIndicator fieldSimulatedPopulation = new TextIndicator();
        fieldSimulatedPopulation.setText("3000");

        JLabel labelActiveAgents = new JLabel("Active agents");
        TextIndicator fieldActiveAgents = new TextIndicator();
        fieldActiveAgents.setText("0");

        JLabel labelTick = new JLabel("Tick");
        TextIndicator fieldTick = new TextIndicator();
        fieldTick.setText("0");

        visualizationEngine.setOnVisualizationStateChangedListener(new OnVisualizationStateChangedListener() {
            @Override
            public void onTick(int tick, int activeAgents) {
                fieldTick.setText(Integer.toString(tick));
                fieldActiveAgents.setText(Integer.toString(activeAgents));
            }
        });

        Button buttonTickPrev = new Button("-");
        buttonTickPrev.setPreferredSize(new Dimension(25, 25));

        buttonTickPrev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visualizationEngine.setCurrentTick(visualizationEngine.getCurrentTick() - 1);
            }
        });
        Button buttonTickNext = new Button("+");
        buttonTickNext.setPreferredSize(new Dimension(25, 25));

        buttonTickNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visualizationEngine.setCurrentTick(visualizationEngine.getCurrentTick() + 1);
            }
        });
        JSlider sliderTick = new JSlider(SwingConstants.HORIZONTAL);

        tools.add(labelSimulatedPopulation);
        tools.add(fieldSimulatedPopulation);
        tools.addSeparator();
        tools.add(labelActiveAgents);
        tools.add(fieldActiveAgents);
        tools.addSeparator();
        tools.add(labelTick);
        tools.add(buttonTickPrev);
        tools.add(fieldTick);
        tools.add(buttonTickNext);
        tools.add(sliderTick);
        tools.addSeparator();

        return tools;
    }

}
