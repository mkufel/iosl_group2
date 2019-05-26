package visualization;

import TraceGenerationEngine.ConfigurationChangedListener;
import TraceGenerationEngine.TraceGenerationEngine;
import org.graphstream.ui.swingViewer.ViewPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VisualizationWindow extends JFrame {

    private VisualizationEngine visualizationEngine;
    private TraceGenerationEngine traceGenerationEngine;
    private ViewPanel graphView;

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

    /**
     * Initializes the Window for the visualization.
     * @param graphView The graph object to show.
     * @param engine The visualization engine that is runnning the simulation.
     * @param traceGenerationEngine The trace generation engine that loads configurations.
     * @throws HeadlessException
     */
    public VisualizationWindow(ViewPanel graphView,
                               VisualizationEngine engine,
                               TraceGenerationEngine traceGenerationEngine) throws HeadlessException {
        super("Dissemination Visualization");
        this.visualizationEngine = engine;
        this.traceGenerationEngine = traceGenerationEngine;
        this.graphView = graphView;
        setDefaultWindowConfigurations(graphView);
    }

    /**
     * Sets up default settings for the View.
     * @param graphView The Panel object that contains the GraphStream Graph.
     */
    private void setDefaultWindowConfigurations(ViewPanel graphView) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setJMenuBar(this.createMenuBar());
        this.getContentPane().add(this.createToolBar(), BorderLayout.NORTH);

        graphView.setPreferredSize(new Dimension(800, 600));
        graphView.resizeFrame(800, 600);
        this.getContentPane().add(graphView, BorderLayout.CENTER);
        this.pack();

    }

    /**
     * Sets up the menu in the Window.
     * @return The JMenuBar object set up.
     */
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

    /**
     * Sets up the Help menu.
     * @return The JMenu object.
     */
    private JMenu createHelpMenu() {
        JMenu menuHelp = new JMenu("Help");
        menuHelp.add(new JMenuItem("About"));
        return menuHelp;
    }

    /**
     * Sets up and adds listeners to the Edit menu.
     * @return The JMenu object.
     */
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

    /**
     * Sets up the File menu and adds listeners.
     * @return The JMenu object.
     */
    private JMenu createFileMenu() {
        JMenu menuFile = new JMenu("File");
        menuFile.add(new JMenuItem("Open"));
        menuFile.add(new JMenuItem("Save as..."));
        menuFile.add(new JSeparator());
        menuFile.add(new JMenuItem("Settings"));
        menuFile.add(new JMenuItem("Exit"));
        return menuFile;
    }

    /**
     * Creates the Toolbar in the window, sets up listeners.
     * @return The JToolBar object.
     */
    private JToolBar createToolBar() {
        JToolBar tools = new JToolBar();

        tools.setFloatable(false);
        tools.setRollover(true);
        tools.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel labelSimulatedPopulation = new JLabel("Simulated population");
        TextIndicator fieldSimulatedPopulation = new TextIndicator();
        fieldSimulatedPopulation.setText("3000");

        JSlider sliderTick = new JSlider(SwingConstants.HORIZONTAL);
        sliderTick.setValue(0);
        traceGenerationEngine.setConfigurationChangedListener(new ConfigurationChangedListener() {
            @Override
            public void onConfigurationChanged(int totalPopulation, int totalTicks) {
                fieldSimulatedPopulation.setText(Integer.toString(totalPopulation));
                sliderTick.setMaximum(totalTicks);
                sliderTick.setMinimum(0);
            }
        });

        sliderTick.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int tickSelected = sliderTick.getValue();
                visualizationEngine.setCurrentTick(tickSelected);
            }
        });

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
