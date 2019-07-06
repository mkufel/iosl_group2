package visualization;

import TraceGenerationEngine.TraceGenerationEngine;
import org.graphstream.ui.swingViewer.ViewPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Main window of the application.
 *
 * This class contains GUI elements but is not responsible for rendering of the graph.
 * Graph visualization can be found in VisualizationEngine.
 */
public class VisualizationWindow extends JFrame {

    private VisualizationEngine visualizationEngine;
    private TraceGenerationEngine traceGenerationEngine;

    private OnReloadListener onReloadListener;

    // Container for the graph visualization
    private ViewPanel graphView;

    // Start simulation time (only hours), it's read from the config.
    private String startHour;

    /**
     * Customized JTextField.
     *
     * Editing and focusing disabled.
     * Content is centered.
     */
    private static class TextIndicator extends JTextField {
        /**
         * Creates a text field with a standard width (4 symbols).
         */
        TextIndicator() {
            this(4);
        }

        /**
         * Creates a text field with a given width.
         *
         * @param width Width of the text field
         */
        TextIndicator(int width) {
            super(width);
            this.setEditable(false);
            this.setFocusable(false);
            this.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    /**
     * Customized JButton.
     *
     * Focusing disabled.
     */
    private static class Button extends JButton {
        /**
         * Creates a button with a given text on it.
         *
         * @param text Text to be printed on the button
         */
        Button(String text) {
            super(text);
            this.setFocusable(false);
        }
    }

    /**
     * Creates a Window for the visualization.
     *
     * @param engine                The visualization engine that is running the simulation.
     * @param traceGenerationEngine The trace generation engine that loads configurations.
     */
    public VisualizationWindow(VisualizationEngine engine,
                               TraceGenerationEngine traceGenerationEngine) throws IOException {
        super("Dissemination Visualization");

        // Loading the start time of the simulation from the config
        try(InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Unable to load config");
                return;
            }

            Properties prop = new Properties();
            prop.load(input);

            this.startHour = prop.getProperty("start_time");
        }

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
    private void setDefaultWindowConfigurations(ViewPanel graphView) throws IOException {
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
        JMenu menuEdit = createMenu();

        menu.add(menuEdit);

        return menu;
    }

    /**
     * Sets up and adds listeners to the menu.
     *
     * @return The JMenu object.
     */
    private JMenu createMenu() {
        JMenu menu = new JMenu("Control");

        menu.add(new JMenuItem("Pause/Resume"))
                .addActionListener(e -> visualizationEngine.toggleSimulation());

        menu.add(new JMenuItem("Step back"))
                .addActionListener(e -> visualizationEngine.setCurrentTick(visualizationEngine.getCurrentTick() - 1));

        menu.add(new JMenuItem("Step forward"))
                .addActionListener(e -> visualizationEngine.setCurrentTick(visualizationEngine.getCurrentTick() + 1));

        menu.add(new JSeparator());

        menu.add(new JMenuItem("Reload simulation"))
                .addActionListener(e -> {
                    if (onReloadListener != null) {
                        JDialog dialog = getLoadingDialog();

                        SwingWorker<Void, Void> reloadWorker = new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                visualizationEngine.setRunning(false);

                                System.out.println("Reloading simulation...");

                                onReloadListener.onReload();
                                dialog.dispose();

                                System.out.println("Simulation reloaded");

                                return null;
                            }
                        };

                        reloadWorker.execute();
                        dialog.setVisible(true);
                    }
                });

        menu.add(new JMenuItem("Restart from beginning"))
                .addActionListener(e -> {
                    graphView.getCamera().resetView();
                    visualizationEngine.restart();
                });

        return menu;
    }

    /**
     * Creates the Toolbar in the window, sets up listeners.
     *
     * @return The JToolBar object.
     */
    private JToolBar createToolBar() throws IOException {
        JToolBar tools = new JToolBar();

        tools.setFloatable(false);
        tools.setRollover(true);
        tools.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Indicator of simulated population
        JLabel labelSimulatedPopulation = new JLabel("Agents");
        TextIndicator fieldSimulatedPopulation = new TextIndicator();
        fieldSimulatedPopulation.setText(Integer.toString(traceGenerationEngine.getTotal_users()));

        // Slider for the ticks
        JSlider sliderTick = new JSlider(SwingConstants.HORIZONTAL);
        sliderTick.setValue(0);
        sliderTick.setMinimum(0);
        sliderTick.setMaximum(traceGenerationEngine.getTotal_ticks());
        sliderTick.addChangeListener(e -> visualizationEngine.setCurrentTick(sliderTick.getValue()));

        // Indicator for the agents that have data
        JLabel labelActiveAgents = new JLabel("Have data");
        TextIndicator fieldActiveAgents = new TextIndicator();
        fieldActiveAgents.setText("0");

        // Clock
        JLabel labelClock = new JLabel("Time");
        TextIndicator fieldClock = new TextIndicator(8);
        fieldClock.setText(Integer.toString(traceGenerationEngine.getAbsoluteTimeFromTick(startHour, 0)));

        // Indicator of the current tick
        JLabel labelTick = new JLabel("Tick");
        TextIndicator fieldTick = new TextIndicator();
        fieldTick.setText("1");

        // Indicator of the dissemination value
        JLabel labelDissemination = new JLabel("Dissemination");
        TextIndicator fieldDissemination = new TextIndicator();
        fieldDissemination.setText("0");

        // Button for the previous tick
        Button buttonTickPrev = new Button("-");
        buttonTickPrev.setPreferredSize(new Dimension(25, 25));
        buttonTickPrev.addActionListener(e -> {
            final int tick = visualizationEngine.getCurrentTick() - 1;

            if (tick >= 0) {
                visualizationEngine.setCurrentTick(tick);
            }
        });

        // Button for the next tick
        Button buttonTickNext = new Button("+");
        buttonTickNext.setPreferredSize(new Dimension(25, 25));
        buttonTickNext.addActionListener(e -> {
            final int tick = visualizationEngine.getCurrentTick() + 1;

            if (tick <= traceGenerationEngine.getTotal_ticks() - 1) {
                visualizationEngine.setCurrentTick(tick);
            }
        });

        // This happens every tick
        visualizationEngine.setOnTickListener((tick, activeAgents, disseminationFactor) -> {
            int timeH, timeM, timeS;

            try {
                int t = traceGenerationEngine.getAbsoluteTimeFromTick(startHour, tick);
                timeH = t / 3600;
                timeM = (t % 3600) / 60;
                timeS = t % 60;
            }
            catch (IOException e) {
                e.printStackTrace();
                timeH = timeM = timeS = 0;
            }

            fieldClock.setText(String.format("%02d:%02d:%02d", timeH, timeM, timeS));
            fieldTick.setText(Integer.toString(tick + 1));
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
        tools.add(labelClock);
        tools.add(fieldClock);
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
     */
    public OnReloadListener getOnReloadListener() {
        return onReloadListener;
    }

    /**
     * Sets the listener to be called when the simulation is reloaded.
     *
     * @param onReloadListener The listener method.
     */
    public void setOnReloadListener(OnReloadListener onReloadListener) {
        this.onReloadListener = onReloadListener;
    }
}
