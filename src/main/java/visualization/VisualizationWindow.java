package visualization;

import TraceGenerationEngine.TraceGenerationEngine;
import org.graphstream.ui.swingViewer.ViewPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class VisualizationWindow extends JFrame {

    private VisualizationEngine visualizationEngine;
    private TraceGenerationEngine traceGenerationEngine;
    private ViewPanel graphView;
    private OnSimulationReloadListener onSimulationReloadListener;

    private static class TextIndicator extends JTextField {
        TextIndicator() {
            super(4);
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
     *
     * @param engine                The visualization engine that is runnning the simulation.
     * @param traceGenerationEngine The trace generation engine that loads configurations.
     */
    public VisualizationWindow(VisualizationEngine engine,
                               TraceGenerationEngine traceGenerationEngine) {
        super("Dissemination Visualization");
        this.visualizationEngine = engine;
        this.traceGenerationEngine = traceGenerationEngine;
        this.graphView = engine.getViewPanel();
        setDefaultWindowConfigurations(graphView);
    }

    /**
     * Sets up default settings for the View.
     *
     * @param graphView The Panel object that contains the GraphStream Graph.
     */
    private void setDefaultWindowConfigurations(ViewPanel graphView) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setJMenuBar(this.createMenuBar());
        this.getContentPane().add(this.createToolBar(), BorderLayout.NORTH);
        this.getContentPane().add(graphView, BorderLayout.CENTER);

        graphView.setPreferredSize(new Dimension(800, 600));
        graphView.resizeFrame(800, 600);

        this.pack();
    }

    /**
     * Sets up the menu in the Window.
     *
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
     *
     * @return The JMenu object.
     */
    private JMenu createHelpMenu() {
        JMenu menuHelp = new JMenu("Help");

        menuHelp.add(new JMenuItem("About"));

        return menuHelp;
    }

    /**
     * Sets up and adds listeners to the Edit menu.
     *
     * @return The JMenu object.
     */
    private JMenu createEditMenu() {
        JMenu menuEdit = new JMenu("Edit");

        menuEdit.add(new JMenuItem("Pause/Resume"))
                .addActionListener(e -> visualizationEngine.toggleSimulation());

        menuEdit.add(new JMenuItem("Step back"))
                .addActionListener(e -> visualizationEngine.setCurrentTick(visualizationEngine.getCurrentTick() - 1));

        menuEdit.add(new JMenuItem("Step forward"))
                .addActionListener(e -> visualizationEngine.setCurrentTick(visualizationEngine.getCurrentTick() + 1));

        menuEdit.add(new JSeparator());

        menuEdit.add(new JMenuItem("Reload simulation"))
                .addActionListener(e -> {
                    if (onSimulationReloadListener != null) {
                        try {
                            onSimulationReloadListener.reloadSimulation();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        menuEdit.add(new JMenuItem("Restart visualization"))
                .addActionListener(e -> {
                    graphView.getCamera().resetView();
                    visualizationEngine.restart();
                });

        return menuEdit;
    }

    /**
     * Sets up the File menu and adds listeners.
     *
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
     *
     * @return The JToolBar object.
     */
    private JToolBar createToolBar() {
        JToolBar tools = new JToolBar();

        tools.setFloatable(false);
        tools.setRollover(true);
        tools.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel labelSimulatedPopulation = new JLabel("Simulated population");
        TextIndicator fieldSimulatedPopulation = new TextIndicator();
        fieldSimulatedPopulation.setText(Integer.toString(traceGenerationEngine.getTotal_users()));

        JSlider sliderTick = new JSlider(SwingConstants.HORIZONTAL);
        sliderTick.setValue(0);
        sliderTick.setMinimum(0);
        sliderTick.setMaximum(traceGenerationEngine.getTotal_ticks());
        sliderTick.addChangeListener(e -> visualizationEngine.setCurrentTick(sliderTick.getValue()));

        JLabel labelActiveAgents = new JLabel("Active agents");
        TextIndicator fieldActiveAgents = new TextIndicator();
        fieldActiveAgents.setText("0");

        JLabel labelTick = new JLabel("Tick");
        TextIndicator fieldTick = new TextIndicator();
        fieldTick.setText("0");

        JLabel labelDissemination = new JLabel("Dissemination");
        TextIndicator fieldDissemination = new TextIndicator();
        fieldDissemination.setText("0");

        Button buttonTickPrev = new Button("-");
        buttonTickPrev.setPreferredSize(new Dimension(25, 25));
        buttonTickPrev.addActionListener(e -> {
            final int tick = visualizationEngine.getCurrentTick() - 1;

            if (tick >= 0) {
                visualizationEngine.setCurrentTick(tick);
            }
        });

        Button buttonTickNext = new Button("+");
        buttonTickNext.setPreferredSize(new Dimension(25, 25));
        buttonTickNext.addActionListener(e -> {
            final int tick = visualizationEngine.getCurrentTick() + 1;

            if (tick <= traceGenerationEngine.getTotal_ticks() - 1) {
                visualizationEngine.setCurrentTick(tick);
            }
        });

        visualizationEngine.setOnVisualizationStateChangedListener((tick, activeAgents, disseminationFactor) -> {
            fieldTick.setText(Integer.toString(tick));
            sliderTick.setValue(tick);
            fieldActiveAgents.setText(Integer.toString(activeAgents));
            fieldDissemination.setText(String.format("%.2f%%", disseminationFactor));
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
        tools.add(labelDissemination);
        tools.add(fieldDissemination);
        tools.addSeparator();

        return tools;
    }

    /**
     * Returns the simulation reload listener.
     *
     * @return
     */
    public OnSimulationReloadListener getOnSimulationReloadListener() {
        return onSimulationReloadListener;
    }

    /**
     * Sets the listener to be called when the simulation is reloaded.
     *
     * @param onSimulationReloadListener The listener method.
     */
    public void setOnSimulationReloadListener(OnSimulationReloadListener onSimulationReloadListener) {
        this.onSimulationReloadListener = onSimulationReloadListener;
    }
}
