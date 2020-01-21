import javax.swing.*;

public class LifeSimulationDemo {
    public static void main(String[] args) {
        LifeSimulationWindow window = new LifeSimulationWindow();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
