import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

class LifeSimulationWindow extends JFrame implements ActionListener, ChangeListener, MouseListener {
    private World world;
    private javax.swing.Timer timer; // timer for updating world
    private javax.swing.Timer modifyTimer; // timer to do whatever action underneath mouse
    private World.WorldDisplay center; // displays world
    private JPanel interactBar; // bar for changing animals and plants
    private JLabel tileIcon, mouseLbl, generationLbl; // labels for showing current state of simulation
    private JButton runBtn, nextBtn, clearAnimalsBtn, clearPlantsBtn, clearBtn;
    private JSlider speedSldr;
    private JComboBox lifeSelect;

    LifeSimulationWindow() {
        world = new World(25);
        timer = new javax.swing.Timer(800, this);
        timer.setActionCommand("step");
        modifyTimer = new javax.swing.Timer(10, this);
        modifyTimer.setActionCommand("modify");
        tileIcon = new JLabel("");
        try {
            tileIcon.setIcon(new ImageIcon(ImageIO.read(new File("Delete.png")).getScaledInstance(20, 20, Image.SCALE_DEFAULT)));
        } catch (IOException exc) {
        }
        String[] addableLife = {"Delete", "Human", "Corn", "Dandelion", "Demon", "Fox", "Knight", "Mage", "MapleTree", "PineTree", "Rose", "Wolf"};
        lifeSelect = new JComboBox(addableLife);
        lifeSelect.addActionListener(this);
        lifeSelect.setActionCommand("switch");
        runBtn = new JButton("\u25B6");
        runBtn.addActionListener(this);
        nextBtn = new JButton("\u23E9");
        nextBtn.addActionListener(this);
        clearAnimalsBtn = new JButton("Clear Animals");
        clearAnimalsBtn.addActionListener(this);
        clearPlantsBtn = new JButton("Clear Plants");
        clearPlantsBtn.addActionListener(this);
        clearBtn = new JButton("Clear All");
        clearBtn.addActionListener(this);
        speedSldr = new JSlider(0, 999, 200);
        speedSldr.addChangeListener(this);
        mouseLbl = new JLabel(String.format("%20s", "Choose what to add."));
        generationLbl = new JLabel(String.format("Generation %-10d", world.turn));

        // create panels
        JPanel pageStart = new JPanel();
        pageStart.setLayout(new BoxLayout(pageStart, BoxLayout.PAGE_AXIS));
        pageStart.setBackground(Color.DARK_GRAY);
        JPanel controls = new JPanel();
        controls.add(runBtn);
        controls.add(nextBtn);
        controls.add(speedSldr);
        interactBar = new JPanel();
        interactBar.add(tileIcon);
        interactBar.add(lifeSelect);
        interactBar.add(clearAnimalsBtn);
        interactBar.add(clearPlantsBtn);
        interactBar.add(clearBtn);
        pageStart.add(controls);
        pageStart.add(interactBar);

        center = world.new WorldDisplay(500, 500);
        center.addMouseListener(this);

        JPanel pageEnd = new JPanel();
        pageEnd.add(mouseLbl);
        pageEnd.add(generationLbl);

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.add(pageStart, BorderLayout.PAGE_START);
        content.add(center, BorderLayout.CENTER);
        content.add(pageEnd, BorderLayout.PAGE_END);

        // set window attributes
        setContentPane(content);
        setTitle("Life Simulation");
        pack();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals("step")) {
            world.step();
            generationLbl.setText(String.format("Generation %-10d", world.turn));
        } else if (evt.getActionCommand().equals("modify")) {
            int row = (int) (MouseInfo.getPointerInfo().getLocation().getY() - center.getLocationOnScreen().getY()) * world.getSize() / center.getHeight();
            int col = (int) (MouseInfo.getPointerInfo().getLocation().getX() - center.getLocationOnScreen().getX()) * world.getSize() / center.getWidth();
            mouseLbl.setText(String.format("%10s (%3d,%3d)", lifeSelect.getSelectedItem(), row, col));
            if (lifeSelect.getSelectedItem().equals("Delete")) {
                world.freeRemove(row, col);
            } else {
                world.freeAdd((String)lifeSelect.getSelectedItem(), row, col);
            }
        } else if (evt.getActionCommand().equals("Clear Animals")) {
            world.orgReset();
        } else if (evt.getActionCommand().equals("Clear Plants")) {
            world.plantReset();
        } else if (evt.getActionCommand().equals("Clear All")) {
            world.orgReset();
            world.plantReset();
        } else if (evt.getActionCommand().equals("switch")) {
            JComboBox cb = (JComboBox) evt.getSource();
            String str = (String) cb.getSelectedItem();
            try {
                tileIcon.setIcon(new ImageIcon(ImageIO.read(new File(str + ".png")).getScaledInstance(20, 20, Image.SCALE_DEFAULT)));
            } catch (IOException exc) {
            }
        }else if (evt.getActionCommand().equals("\u25B6")) { // starts the simulation
            timer.start();
            runBtn.setText("\u23F8");
            nextBtn.setEnabled(false);
        } else if (evt.getActionCommand().equals("\u23F8")) { // pauses the simulation
            timer.stop();
            runBtn.setText("\u25B6");
            nextBtn.setEnabled(true);
        } else if (evt.getActionCommand().equals("\u23E9")) { // advances the simulation by one step (clicked)
            world.step();
            generationLbl.setText(String.format("Generation %-10d", world.turn));
        }
        repaint();
    }

    public void stateChanged(ChangeEvent evt) {
        timer.setDelay(1000 - speedSldr.getValue());
    }

    public void mouseClicked(MouseEvent evt) {
    }

    public void mousePressed(MouseEvent evt) {
        modifyTimer.start();
    }

    public void mouseReleased(MouseEvent evt) {
        modifyTimer.stop();
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }
}
