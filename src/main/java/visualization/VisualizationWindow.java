package visualization;

import TraceGenerationEngine.TraceGenerationEngine;
import org.graphstream.ui.swingViewer.ViewPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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
        JMenu menuEdit = createEditMenu();

        menu.add(menuEdit);

        return menu;
    }


    /**
     * Sets up and adds listeners to the Edit menu.
     *
     * @return The JMenu object.
     */
    private JMenu createEditMenu() {
        JMenu menuEdit = new JMenu("Control");

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
                        JDialog dialog = getLoadingDialog();
                        SwingWorker<Void, Void> reloadWorker = new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                visualizationEngine.setRunning(false);
                                System.out.println("Reloading simulation...");
                                onSimulationReloadListener.reloadSimulation();
                                dialog.dispose();
                                System.out.println("Reloaded simulation");
                                return null;
                            }

                        };
                        reloadWorker.execute();
                        dialog.setVisible(true);

                    }
                });

        menuEdit.add(new JMenuItem("Restart from beginning"))
                .addActionListener(e -> {
                    graphView.getCamera().resetView();
                    visualizationEngine.restart();
                });

        return menuEdit;
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
        sliderTick.addChangeListener(e -> {
            visualizationEngine.setCurrentTick(sliderTick.getValue());
        });

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
     * Returns a loading dialog ready to be displayed.
     *
     * @return A JDialog component to display
     */
    private JDialog getLoadingDialog() {
        final JDialog loadingDialog = new JDialog(this);
        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBorder(new EmptyBorder(20, 20, 20, 20));
        p1.add(new JLabel("Please wait..."), BorderLayout.CENTER);
        loadingDialog.setUndecorated(true);
        loadingDialog.getContentPane().add(p1);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loadingDialog.setModal(true);
        return loadingDialog;
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
