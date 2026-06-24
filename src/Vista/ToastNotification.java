package Vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ToastNotification extends JWindow {

    private final Color colorPietras = new Color(244, 119, 21); // Naranja
    private final Color colorCobrizo = new Color(184, 139, 74); // Cobrizo
    private float opacity = 0.0f;
    private final Timer timerFadeIn;
    private Timer timerFadeOut = null;

    public ToastNotification(String titulo, String mensaje) {
        setAlwaysOnTop(true);
        // Intentar habilitar fondo transparente
        setBackground(new Color(0, 0, 0, 0));

        // Panel contenedor con bordes redondeados
        JPanel panel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Degradado premium
                GradientPaint gp = new GradientPaint(0, 0, colorPietras, getWidth(), getHeight(), colorCobrizo);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        panel.setBorder(new EmptyBorder(12, 18, 12, 18));
        panel.setPreferredSize(new Dimension(320, 75));

        // Contenido
        JLabel lblIcono = new JLabel("🛍️");
        lblIcono.setFont(new Font("SansSerif", Font.PLAIN, 28));
        lblIcono.setForeground(Color.WHITE);

        JPanel pnlTexto = new JPanel(new GridLayout(2, 1, 2, 2));
        pnlTexto.setOpaque(false);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblMsg = new JLabel(mensaje);
        lblMsg.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblMsg.setForeground(new Color(242, 240, 235)); // Hueso

        pnlTexto.add(lblTitulo);
        pnlTexto.add(lblMsg);

        panel.add(lblIcono, BorderLayout.WEST);
        panel.add(pnlTexto, BorderLayout.CENTER);
        add(panel);

        // Hacer clic para cerrar
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                timerFadeIn.stop();
                timerFadeOut.stop();
                dispose();
            }
        });

        pack();

        // Posicionar en la esquina inferior derecha
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int x = bounds.x + bounds.width - getWidth() - 20;
        int y = bounds.y + bounds.height - getHeight() - 20;
        setLocation(x, y);

        // Intentar setear opacidad inicial
        try {
            setOpacity(0.0f);
        } catch (Exception e) {
            // OS no soporta transparencia por ventana entera
        }

        // Animación de Entrada (Fade In)
        timerFadeIn = new Timer(25, null);
        timerFadeIn.addActionListener(e -> {
            opacity += 0.05f;
            if (opacity >= 0.95f) {
                opacity = 0.95f;
                timerFadeIn.stop();
                // Iniciar temporizador de espera y luego salida
                Timer waitTimer = new Timer(5000, evt -> timerFadeOut.start());
                waitTimer.setRepeats(false);
                waitTimer.start();
            }
            try {
                setOpacity(opacity);
            } catch (Exception ex) {}
        });

        // Animación de Salida (Fade Out)
        timerFadeOut = new Timer(25, null);
        timerFadeOut.addActionListener(e -> {
            opacity -= 0.05f;
            if (opacity <= 0.0f) {
                opacity = 0.0f;
                timerFadeOut.stop();
                dispose();
            }
            try {
                setOpacity(opacity);
            } catch (Exception ex) {}
        });
    }

    public void mostrar() {
        setVisible(true);
        timerFadeIn.start();
    }
}
