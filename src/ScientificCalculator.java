import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Math;

public class ScientificCalculator extends JFrame implements ActionListener {

    private JTextField textField;
    private String inputBuffer = "";
    private double memory = 0.0;

    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setSize(600, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set custom UI colors
        Color orangeColor = new Color(255, 165, 0);
        Color blackColor = Color.BLACK;
        Color whiteColor = Color.WHITE;

        UIManager.put("TextField.background", new ColorUIResource(orangeColor));
        UIManager.put("Button.background", new ColorUIResource(orangeColor));
        UIManager.put("Button.foreground", new ColorUIResource(blackColor));
        Font buttonFont = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
        UIManager.put("Button.font", buttonFont);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.black);

        textField = new JTextField();
        textField.setBackground(whiteColor);
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.setEditable(false);
        textField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        panel.add(textField, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(8, 4, 0, 0));
        buttonPanel.setBackground(blackColor);

        String[] buttonLabels = {
                "7", "8", "9", "(", ")",
                "4", "5", "6", "+", "-",
                "1", "2", "3", "*", "/",
                "0", ".", "sin", "cos",
                "tan", "sqrt", "log", "exp",
                "π", "x^2", "x^3", "x^y", "x√y",
                "e", "1/x", "log10", "ce", "mc",
                "mr", "ms", "m+", "m-", "=" //
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(this);
            button.setBackground(Color.orange);
            button.setForeground(blackColor);
            button.setFont(buttonFont);
            button.setPreferredSize(new Dimension(100, 80));
            buttonPanel.add(button);
        }

        panel.add(buttonPanel, BorderLayout.CENTER);

        getContentPane().setBackground(Color.orange);
        add(panel);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "=":
                evaluateExpression();
                break;
            case "CE":
                clearEntry();
                break;
            case "π":
                addToInput(Math.PI);
                break;
            case "e":
                addToInput(Math.E);
                break;
            case "x^2":
                addToInput("^2");
                break;
            case "x^3":
                addToInput("^3");
                break;
            case "sin":
                addToInput("sin(");
                break;
            case "cos":
                addToInput("cos(");
                break;
            case "tan":
                addToInput("tan(");
                break;
            case "sqrt":
                addToInput("sqrt(");
                break;
            case "log":
                addToInput("log(");
                break;
            case "exp":
                addToInput("exp(");
                break;
            case "1/x":
                addToInput("1/");
                break;
            case "log10":
                addToInput("log10(");
                break;
            case "MC":
                memoryClear();
                break;
            case "MR":
                memoryRecall();
                break;
            case "MS":
                memoryStore();
                break;
            case "M+":
                memoryAdd();
                break;
            case "M-":
                memorySubtract();
                break;
            default:
                addToInput(command);
                break;
        }
    }

    private void addToInput(String s) {
        inputBuffer += s;
        textField.setText(inputBuffer);
    }

    private void addToInput(double d) {
        inputBuffer += d;
        textField.setText(inputBuffer);
    }

    private void evaluateExpression() {
        try {
            double result = eval(inputBuffer);
            textField.setText(Double.toString(result));
            inputBuffer = "";
        } catch (Exception ex) {
            textField.setText("Error");
            inputBuffer = "";
        }
    }

    private void clearEntry() {
        inputBuffer = "";
        textField.setText("");
    }

    private void memoryClear() {
        memory = 0.0;
    }

    private void memoryRecall() {
        addToInput(memory);
    }

    private void memoryStore() {
        try {
            memory = Double.parseDouble(textField.getText());
        } catch (NumberFormatException e) {
            textField.setText("Invalid Memory Value");
        }
    }

    private void memoryAdd() {
        try {
            double value = Double.parseDouble(textField.getText());
            memory += value;
        } catch (NumberFormatException e) {
            textField.setText("Invalid Memory Value");
        }
    }

    private void memorySubtract() {
        try {
            double value = Double.parseDouble(textField.getText());
            memory -= value;
        } catch (NumberFormatException e) {
            textField.setText("Invalid Memory Value");
        }
    }

    private double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') {
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else if (func.equals("log")) x = Math.log(x);
                    else if (func.equals("exp")) x = Math.exp(x);
                    else if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("log10")) x = Math.log10(x);
                    else if (func.equals("x^2")) x = Math.pow(x, 2);
                    else if (func.equals("x^3")) x = Math.pow(x, 3);
                    else if (func.equals("1/x")) x = 1 / x;
                    else if (func.equals("x^y")) {
                        eat('^');
                        double y = parseFactor();
                        x = Math.pow(x, y);
                    }
                    else if (func.equals("x√y")) {
                        eat('√');
                        double y = parseFactor();
                        x = Math.pow(x, 1.0 / y);
                    }
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor());

                return x;
            }
        }.parse();
    }
}