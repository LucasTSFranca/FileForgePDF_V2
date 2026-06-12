package app;

import view.MainView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        //determina a aparencia do programa pelo sistema quee stiver rodando
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception e) {
                
            }
            //chama a View
            MainView view = new MainView();
            view.setVisible(true);
        });
    }

}
