import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class SudokuSolver extends JFrame {
    private JTextField[][] cells = new JTextField[9][9];
    private JButton solveButton;
    private JButton clearButton;
    private JLabel messageLabel;
    private JButton fillRandomButton;

    public SudokuSolver() {
        setTitle("Sudoku Solver");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(9, 9));
        Font font = new Font("Arial", Font.BOLD, 20);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j] = new JTextField();
                cells[i][j].setFont(font);
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);

                // Add color to 3x3 subgrids
                if ((i / 3 + j / 3) % 2 == 0) {
                    cells[i][j].setBackground(Color.LIGHT_GRAY);
                } else {
                    cells[i][j].setBackground(Color.WHITE);
                }

                gridPanel.add(cells[i][j]);
            }
        }

        // Set border with wooden color
        Color woodenColor = new Color(139, 69, 19); // Brown color for wooden effect
        gridPanel.setBorder(new LineBorder(woodenColor, 20));

        solveButton = new JButton("Solve");
        clearButton = new JButton("Clear");
        fillRandomButton = new JButton("Fill Random");
        messageLabel = new JLabel(" ");

        solveButton.addActionListener(new SolveButtonListener());
        clearButton.addActionListener(new ClearButtonListener());
        fillRandomButton.addActionListener(new FillRandomButtonListener());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(solveButton);
        controlPanel.add(clearButton);
        controlPanel.add(fillRandomButton);
        controlPanel.add(messageLabel);

        add(gridPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private class SolveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            messageLabel.setText("Checking input...");
            solveButton.setEnabled(false);
            clearButton.setEnabled(false);
            fillRandomButton.setEnabled(false);

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    int[][] board = new int[9][9];
                    try {
                        for (int i = 0; i < 9; i++) {
                            for (int j = 0; j < 9; j++) {
                                String text = cells[i][j].getText();
                                if (!text.isEmpty()) {
                                    int value = Integer.parseInt(text);
                                    if (value < 1 || value > 9) {
                                        throw new NumberFormatException();
                                    }
                                    board[i][j] = value;
                                } else {
                                    board[i][j] = 0;
                                }
                            }
                        }

                        if (!isValidSudoku(board)) {
                            return false;
                        }

                        return solveSudoku(board);
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                }

                @Override
                protected void done() {
                    try {
                        Boolean result = get();
                        if (result) {
                            messageLabel.setText("Solved!");
                        } else {
                            messageLabel.setText("Invalid input or no solution exists.");
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                        messageLabel.setText("An error occurred.");
                    } finally {
                        solveButton.setEnabled(true);
                        clearButton.setEnabled(true);
                        fillRandomButton.setEnabled(true);
                    }
                }
            };

            worker.execute();
        }
    }

    private class ClearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    cells[i][j].setText("");
                    // Reset background color to default
                    if ((i / 3 + j / 3) % 2 == 0) {
                        cells[i][j].setBackground(Color.LIGHT_GRAY);
                    } else {
                        cells[i][j].setBackground(Color.WHITE);
                    }
                }
            }
            messageLabel.setText(" ");
        }
    }

    private class FillRandomButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            clearBoard();
            fillRandomValues();
            messageLabel.setText("Random values filled.");
        }
    }

    private void clearBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setText("");
                if ((i / 3 + j / 3) % 2 == 0) {
                    cells[i][j].setBackground(Color.LIGHT_GRAY);
                } else {
                    cells[i][j].setBackground(Color.WHITE);
                }
            }
        }
    }

    private void fillRandomValues() {
        Random random = new Random();
        HashSet<String> filledPositions = new HashSet<>();

        while (filledPositions.size() < 20) {
            int row = random.nextInt(9);
            int col = random.nextInt(9);
            int value = random.nextInt(9) + 1;

            String pos = row + "," + col;

            if (cells[row][col].getText().isEmpty() && !filledPositions.contains(pos) && isSafe(getBoard(), row, col, value)) {
                cells[row][col].setText(String.valueOf(value));
                cells[row][col].setBackground(Color.ORANGE); // Highlight filled cells in red
                filledPositions.add(pos);
            }
        }
    }

    private int[][] getBoard() {
        int[][] board = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String text = cells[i][j].getText();
                if (!text.isEmpty()) {
                    board[i][j] = Integer.parseInt(text);
                } else {
                    board[i][j] = 0;
                }
            }
        }
        return board;
    }

    private boolean solveSudoku(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num;
                            updateBoard(board, row, col, true);
                            try {
                                Thread.sleep(100); // delay to visualize the solving process
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (solveSudoku(board)) {
                                return true;
                            }
                            board[row][col] = 0;
                            updateBoard(board, row, col, false);
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private void updateBoard(int[][] board, int row, int col, boolean inserting) {
        SwingUtilities.invokeLater(() -> {
            cells[row][col].setText(board[row][col] == 0 ? "" : Integer.toString(board[row][col]));
            if (inserting) {
                cells[row][col].setBackground(Color.YELLOW); // Highlight when a number is inserted
            } else {
                cells[row][col].setBackground(Color.PINK); // Highlight when a number is removed
            }
        });
    }

    private boolean isSafe(int[][] board, int row, int col, int num) {
        for (int x = 0; x < 9; x++) {
            if (board[row][x] == num || board[x][col] == num ||
                board[row - row % 3 + x / 3][col - col % 3 + x % 3] == num) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidSudoku(int[][] board) {
        // Check rows and columns
        for (int i = 0; i < 9; i++) {
            boolean[] rowCheck = new boolean[10];
            boolean[] colCheck = new boolean[10];
            for (int j = 0; j < 9; j++) {
                if (board[i][j] != 0) {
                    if (rowCheck[board[i][j]]) {
                        return false;
                    }
                    rowCheck[board[i][j]] = true;
                }
                if (board[j][i] != 0) {
                    if (colCheck[board[j][i]]) {
                        return false;
                    }
                    colCheck[board[j][i]] = true;
                }
            }
        }
        // Check 3x3 subgrids
        for (int row = 0; row < 9; row += 3) {
            for (int col = 0; col < 9; col += 3) {
                boolean[] subGridCheck = new boolean[10];
                for (int i = row; i < row + 3; i++) {
                    for (int j = col; j < col + 3; j++) {
                        if (board[i][j] != 0) {
                            if (subGridCheck[board[i][j]]) {
                                return false;
                            }
                            subGridCheck[board[i][j]] = true;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SudokuSolver solver = new SudokuSolver();
            solver.setVisible(true);
        });
    }
}
