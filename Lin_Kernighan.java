import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Lin_Kernighan extends JFrame {
    private int n; // number of cities
    private double[][] dist; // distance matrix
    private int[] bestTour; // best tour found

    double[][] coordinates;

    int[] tour;

    private JLabel stats;

    public class Visualizer extends JFrame{

        ArrayList<Node> nodes;
        ArrayList<edge> edges;


        double[][] coordinates;

        int viewWidth;
        int viewHeight;
        int width;
        int height;
        double scaleW;
        double scaleH;

        int[] tour;

        private JLabel stats;

        public Visualizer(double[][] coords, int[] best_tour){
            super();

            this.tour = best_tour;
            this.coordinates = coords;

            this.setLocationRelativeTo(null);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.stats = new JLabel();
            this.pack();
            this.setLocationRelativeTo(null);
            this.add(stats, BorderLayout.SOUTH);
            this.setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH);
            this.setVisible(true);

            try { Thread.sleep(1000); } catch (Exception ex) {}
            nodes = new ArrayList<Node>();
            edges = new ArrayList<edge>();
            viewWidth = this.getWidth() - 100;
            viewHeight = this.getHeight()- 100;
            width = 1;
            height = 1;
            for(int i = 0; i < coordinates.length; i++) {
                if(coordinates[i][0] > scaleW) scaleW = (int) coordinates[i][0];
                if(coordinates[i][1] > scaleH) scaleH = (int) coordinates[i][1];
            }
            scaleW = viewWidth / scaleW;
            scaleH = viewHeight / scaleH;
            scaleW *= .5;
            scaleH *= .5;

        }

        public void draw(int[] tour) {
            this.nodes.clear();
            this.edges.clear();
            for(int i = 0; i < coordinates.length; i++) {
                int x = (int) (coordinates[i][0] * scaleW);
                int y = (int) (coordinates[i][1] * scaleH);
                addNode(String.valueOf(i), x, y);
            }
            for(int i = 0; i < tour.length - 1; i++) {
                addEdge(tour[i], tour[i + 1]);
            }
            this.repaint();
        }

        public void setStat(String text) {
            this.stats.setText(text);
        }

        class Node {
            int x, y;
            String name;

            public Node(String myName, int myX, int myY) {
                x = myX;
                y = myY;
                name = myName;
            }
        }

        class edge {
            int i,j;

            public edge(int ii, int jj) {
                i = ii;
                j = jj;
            }
        }

        // Add a node at pixel (x,y)
        public void addNode(String name, int x, int y) {
            nodes.add(new Node(name,x,y));
        }

        // Add an edge between nodes i and j
        public void addEdge(int i, int j) {
            edges.add(new edge(i,j));
        }

        // Clear and repaint the nodes and edges
        public void paint(Graphics g) {
            super.paint(g);
            Font font = new Font(g.getFont().getName(), Font.PLAIN, 12);
            FontMetrics f = g.getFontMetrics(font);
            int nodeHeight = Math.max(height, f.getHeight());
            int centerX = getWidth() / 20;
            int centerY = getHeight() / 20;
            g.setFont(font);
            g.setColor(Color.black);


            for (edge e : edges) {
                g.drawLine(nodes.get(e.i).x * 2 + centerX, nodes.get(e.i).y * 2 + centerY, nodes.get(e.j).x * 2 + centerX, nodes.get(e.j).y * 2 + centerY);
            }
            for (Node n : nodes) {
                n.name = String.valueOf(Integer.parseInt(n.name) + 1);
                int nodeWidth = Math.max(width, f.stringWidth(n.name) + width / 2);
                if(Integer.parseInt(n.name) == tour[0] + 1){
                    g.setColor(Color.green);
                } else if (Integer.parseInt(n.name) == tour[106] + 1){
                    g.setColor(Color.red);
                } else {
                    g.setColor(Color.white);
                }
                g.fillOval(n.x * 2 - nodeWidth / 2 + centerX, n.y * 2 - nodeHeight / 2 + centerY, nodeWidth, nodeHeight);
                g.setColor(Color.black);
                g.drawOval(n.x * 2 - nodeWidth / 2 + centerX, n.y * 2 - nodeHeight / 2 + centerY, nodeWidth, nodeHeight);
                g.drawString(n.name, n.x * 2 - f.stringWidth(n.name) / 2 + centerX, n.y * 2 + f.getHeight() / 2 + centerY);
            }
        }




    }

    public Lin_Kernighan(int n, double[][] coords) {

        super();
        this.n = n;
        dist = new double[n][n];
        computeDistances(coords);

        this.tour = solve();

        Visualizer graph = new Visualizer(coords, tour);
        graph.draw(tour);

    }



    private void computeDistances(double[][] coords) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double dx = coords[i][0] - coords[j][0];
                double dy = coords[i][1] - coords[j][1];
                dist[i][j] = Math.sqrt(dx * dx + dy * dy);
            }
        }
    }

    public double calculateTourDistance(int[] tour) {
        double distance = 0;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            distance += dist[tour[i]][tour[j]];
        }
        return distance;
    }

    public int[] solve() {
        bestTour = new int[n];
        for (int i = 0; i < n; i++) {
            bestTour[i] = i;
        }
        double bestDist = tourLength(bestTour);

        // perform Lin-Kernighan iterations
        boolean improved;
        do {
            improved = false;
            for (int i = 0; i < n - 2; i++) {
                for (int j = i + 2; j < n; j++) {
                    int[] newTour = reverseSubtour(bestTour, i, j);
                    double newDist = tourLength(newTour);
                    if (newDist < bestDist) {
                        bestTour = newTour;
                        bestDist = newDist;
                        improved = true;
                    }
                }
            }
        } while (improved);

        return bestTour;
    }

    private int[] reverseSubtour(int[] tour, int i, int j) {
        int[] newTour = Arrays.copyOf(tour, n);
        int k = 0;
        for (int p = i; p <= j; p++) {
            newTour[p] = tour[j - k];
            k++;
        }
        return newTour;
    }

    private double tourLength(int[] tour) {
        double length = 0.0;
        for (int i = 0; i < n - 1; i++) {
            length += dist[tour[i]][tour[i + 1]];
        }
        length += dist[tour[n - 1]][tour[0]];
        return length;
    }

    public static void main(String[] args) {



        double[][] coords = new double[107][2];
        int counter = 0;
        try (Scanner scanner = new Scanner(new File("TSP_107.txt"))) {
            String line;
            while (scanner.hasNextLine()) {
                try {
                    line = scanner.nextLine().trim();
                    String[] tokens = line.split("\\s+");
                    coords[counter][0] = Double.parseDouble(tokens[1]);
                    coords[counter][1] = Double.parseDouble(tokens[2]);
                    counter++;
                } catch (ArrayIndexOutOfBoundsException e){

                };
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        long startTime = System.currentTimeMillis();

        Lin_Kernighan solver = new Lin_Kernighan(107, coords);

        long endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime) / 1000.0;

        System.out.print("Final Tour: ");
        for (int i = 0; i < 107; i++) {
            System.out.print((solver.tour[i] + 1) + " ");
        }

        System.out.println();
        double tourDistance = solver.calculateTourDistance(solver.tour);
        System.out.println("Tour cost: " + tourDistance);
        System.out.println("Time Taken: " + totalTime + " ms");


        System.out.println();

    }
}
