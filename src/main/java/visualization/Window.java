package visualization;

import org.graphstream.ui.swingViewer.ViewPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Window extends JFrame {

    VisualizationEngine visualizationEngine;

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

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setJMenuBar(this.createMenuBar());
        this.getContentPane().add(this.createToolBar(), BorderLayout.NORTH);

        graphView.setPreferredSize(new Dimension(800, 600));

        this.getContentPane().add(graphView, BorderLayout.CENTER);

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
                visualizationEngine.toggleSimulation();
            }
        });
        menuEdit.add(new JMenuItem("Step back")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                visualizationEngine.restart();
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
        fieldActiveAgents.setText("934");

        JLabel labelTick = new JLabel("Tick");
        TextIndicator fieldTick = new TextIndicator();
        fieldTick.setText("105347");

        Button buttonTickPrev = new Button("-");
        buttonTickPrev.setPreferredSize(new Dimension(25, 25));

        Button buttonTickNext = new Button("+");
        buttonTickNext.setPreferredSize(new Dimension(25, 25));

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
