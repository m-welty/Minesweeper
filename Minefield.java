import java.util.Random;
import java.util.Scanner;

public class Minefield {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_YELLOW_BRIGHT = "\u001B[33;1m";
    public static final String ANSI_RED_BRIGHT = "\u001b[31;1m";
    public static final String ANSI_BLUE_BRIGHT = "\u001b[34;1m";


    private Cell[][] field;
    private int rows, cols, mines;
    private boolean debugMode;
    private boolean gameOver;

    public Minefield() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select difficulty. 1 - Easy, 2 - Medium, 3 - Hard.");
        int level = scanner.nextInt();
        switch (level) {
            case 1:
                this.rows = 5;
                this.cols = 5;
                this.mines = 5;
                break;
            case 2:
                this.rows = 9;
                this.cols = 9;
                this.mines = 12;
                break;
            case 3:
                this.rows = 20;
                this.cols = 20;
                this.mines = 40;
                break;
            default:
                System.out.println("Invalid choice.");
                System.exit(0);
        }
        System.out.println("Debug mode? Y/N.");
        this.debugMode = scanner.next().equalsIgnoreCase("y");
        this.field = new Cell[rows][cols];
        this.gameOver = false;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                field[i][j] = new Cell(false, "-");
            }
        }
        System.out.println("Enter starting coords. x y");
        int startX = scanner.nextInt();
        int startY = scanner.nextInt();
        createMines(startX, startY);
        evaluateField();
        revealStartingArea(startX, startY);
        Game(scanner, startX, startY);
    }

    public void createMines(int x, int y) {
        Random rand = new Random();
        int count = 0;
        while (count < mines) {
            int i = rand.nextInt(rows);
            int j = rand.nextInt(cols);
            if ((i != x || j != y) && !field[i][j].getStatus().equals("M")) {
                field[i][j].setStatus("M");
                count++;
            }
        }
    }

    public void evaluateField() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (field[i][j].getStatus().equals("M")) {
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            int a = i + x;
                            int b = j + y;
                            if (isValid(a, b) && !field[a][b].getStatus().equals("M")) {
                                int currentStatus = field[a][b].getStatus().equals("-") ? 0 : Integer.parseInt(field[a][b].getStatus());
                                field[a][b].setStatus(String.valueOf(currentStatus + 1));
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (field[i][j].getStatus().equals("-")) {
                    field[i][j].setStatus("0");
                }
            }
        }
    }

    public void revealZeroes(int x, int y) {
        Stack1Gen<int[]> stack = new Stack1Gen<>();
        stack.push(new int[]{x, y});
        while (!stack.isEmpty()) {
            int[] cell = stack.pop();
            int i = cell[0], j = cell[1];
            if (isValid(i, j) && !field[i][j].getRevealed() && field[i][j].getStatus().equals("0")) {
                field[i][j].setRevealed(true);
                stack.push(new int[]{i - 1, j});
                stack.push(new int[]{i + 1, j});
                stack.push(new int[]{i, j - 1});
                stack.push(new int[]{i, j + 1});
            }
        }
    }

    public void revealStartingArea(int x, int y) {
        Q1Gen<int[]> queue = new Q1Gen<>();
        queue.add(new int[]{x, y});
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int i = cell[0], j = cell[1];
            if (isValid(i, j) && !field[i][j].getRevealed()) {
                field[i][j].setRevealed(true);
                if (field[i][j].getStatus().equals("M")) {
                    break;
                } else {
                    queue.add(new int[]{i - 1, j});
                    queue.add(new int[]{i + 1, j});
                    queue.add(new int[]{i, j - 1});
                    queue.add(new int[]{i, j + 1});
                }
            }
        }
    }

    public boolean guess(int x, int y, boolean flag) {
        if (!isValid(x, y))
            return false;
        if (flag) {
            String currentStatus = field[x][y].getStatus();
            if (currentStatus.equals("F")) {
                field[x][y].setStatus("-");
            } else if (currentStatus.equals("-")) {
                field[x][y].setStatus("F");
            }
            return false;
        } else {
            if (field[x][y].getStatus().equals("M")) {
                gameOver = true;
                field[x][y].setRevealed(true);
                return true;
            } else if (field[x][y].getStatus().equals("0")) {
                revealZeroes(x, y);
            } else {
                field[x][y].setRevealed(true);
            }
        }
        return false;
    }

    public boolean gameOver() {
        return gameOver || boardCleared();
    }

    private boolean boardCleared() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!field[i][j].getStatus().equals("M") && !field[i][j].getRevealed()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValid(int i, int j) {
        return i >= 0 && i < rows && j >= 0 && j < cols;
    }
    public void debug() {
        for (Cell[] row : field) {
            for (Cell cell : row) {
                System.out.print(cell.getStatus() + " ");
            }
            System.out.println();
        }
    }

    public String toString() {
        StringBuilder output = new StringBuilder();

        output.append("   ");
        for (int c = 0; c < cols; c++) {
            output.append(c).append(" ");
        }
        output.append("\n   ");
        for (int c = 0; c < cols; c++) {
            output.append("_ ").append(" ");
        }
        output.append("\n");

        for (int r = 0; r < rows; r++) {
            if (r < 10) {
                output.append(" ").append(r).append(" | ");
            } else {
                output.append(r).append(" | ");
            }
            for (Cell cell : field[r]) {
                if (cell.getRevealed()) {
                    String colorCode = getColorCode(cell.getStatus());
                    output.append(colorCode).append(cell.getStatus()).append(ANSI_RESET).append(" ");
                } else {
                    output.append("? ");
                }
            }
            output.append("\n");
        }
        return output.toString();
    }


    private String getColorCode(String status) {
        switch (status) {
            case "1":
                return ANSI_BLUE_BRIGHT;
            case "2":
                return ANSI_BLUE;
            case "3":
                return ANSI_CYAN;
            case "4":
                return ANSI_PURPLE;
            case "5":
                return ANSI_GREEN;
            case "6":
                return ANSI_YELLOW;
            case "7":
                return ANSI_RED;
            case "8":
                return ANSI_YELLOW_BRIGHT;
            case "F":
                return ANSI_YELLOW_BRIGHT;
            case "M":
                return ANSI_RED_BRIGHT;
            default:
                return ANSI_WHITE;
        }
    }

    public void Game(Scanner scanner, int startX, int startY) {
        while (!gameOver()) {
            if (debugMode) {
                debug();
            } else {
                System.out.println(this);
            }
            System.out.println("Enter command. Reveal x y; Flag x y):");
            String command = scanner.next();
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            boolean hitMine = guess(x, y, command.equalsIgnoreCase("flag"));
            if (hitMine) {
                System.out.println("KABOOM!!!!");
                break;
            }
        }
        if (boardCleared()) {
            System.out.println("You win!");
        }
    }

    // Stack1Gen
    private class Stack1Gen<E> {
        private Node<E> top;
        public Stack1Gen() {
            top = null;
        }
        public void push(E element) {
            top = new Node<>(element, top);
        }
        public E pop() {
            if (top == null) {
                return null;
            }
            E element = top.getData();
            top = top.getNext();
            return element;
        }
        public boolean isEmpty() {
            return top == null;
        }
        private class Node<E> {
            private E data;
            private Node<E> next;
            public Node(E data, Node<E> next) {
                this.data = data;
                this.next = next;
            }
            public E getData() {
                return data;
            }
            public Node<E> getNext() {
                return next;
            }
        }
    }
    // Q1Gen
    private class Q1Gen<E> {
        private Node<E> front;
        private Node<E> rear;
        private int size;
        public Q1Gen() {
            front = rear = null;
            size = 0;
        }
        public void add(E element) {
            Node<E> newNode = new Node<>(element, null);
            if (size == 0) {
                front = rear = newNode;
            } else {
                rear.setNext(newNode);
                rear = newNode;
            }
            size++;
        }
        public E poll() {
            if (size == 0) {
                return null;
            }
            E element = front.getData();
            front = front.getNext();
            size--;
            if (size == 0) {
                rear = null;
            }
            return element;
        }
        public boolean isEmpty() {
            return size == 0;
        }
        private class Node<E> {
            private E data;
            private Node<E> next;
            public Node(E data, Node<E> next) {
                this.data = data;
                this.next = next;
            }
            public E getData() {
                return data;
            }
            public Node<E> getNext() {
                return next;
            }
            public void setNext(Node<E> next) {
                this.next = next;
            }
        }
    }
}
