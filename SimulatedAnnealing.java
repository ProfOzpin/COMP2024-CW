import org.w3c.dom.css.CSSImportRule;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

class City {
    int id;
    double x, y;

    public City(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public double distanceTo(City city) {
        double dx = this.x - city.x;
        double dy = this.y - city.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

class Tour {
    List<City> cities;
    double cost;

    public Tour(List<City> cities) {
        this.cities = cities;
        this.cost = computeCost();
    }

    public double computeCost() {
        double cost = 0.0;
        for (int i = 0; i < cities.size() - 1; i++) {
            City fromCity = cities.get(i);
            City toCity = cities.get(i + 1);
            cost += fromCity.distanceTo(toCity);
        }
        cost += cities.get(cities.size() - 1).distanceTo(cities.get(0));
        return cost;
    }

    public void swapCities(int i, int j) {
        City temp = cities.get(i);
        cities.set(i, cities.get(j));
        cities.set(j, temp);
    }
}

public class SimulatedAnnealing {
    static List<City> cities;
    static Tour currentTour;
    static Tour bestTour;

    public class Visualizer extends JFrame {

        ArrayList<Node> nodes;
        ArrayList<edge> edges;


        int[][] coordinates;

        int viewWidth;
        int viewHeight;
        int width;
        int height;
        double scaleW;
        double scaleH;

        int[] tour;


        public Visualizer(int[][] coords, int[] best_tour) {
            super();

            this.tour = best_tour;
            this.coordinates = coords;

            this.setLocationRelativeTo(null);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.pack();
            this.setLocationRelativeTo(null);
            this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            this.setVisible(true);
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
            }
            nodes = new ArrayList<Node>();
            edges = new ArrayList<edge>();
            viewWidth = this.getWidth() - 100;
            viewHeight = this.getHeight() - 100;
            width = 1;
            height = 1;
            for (int i = 0; i < coordinates.length; i++) {
                if (coordinates[i][0] > scaleW) scaleW = (int) coordinates[i][0];
                if (coordinates[i][1] > scaleH) scaleH = (int) coordinates[i][1];
            }
            scaleW = viewWidth / scaleW;
            scaleH = viewHeight / scaleH;
            scaleW *= .5;
            scaleH *= .5;

            draw(tour);

        }

        public void draw(int[] tour) {
            this.nodes.clear();
            this.edges.clear();
            for(int i = 0; i < coordinates.length; i++) {
                int x = (int) (coordinates[i][0] * scaleW);
                int y = (int) (coordinates[i][1] * scaleH);
                addNode(String.valueOf(i+1), x, y);
            }
            for(int i = 0; i < tour.length - 1; i++) {
                addEdge(tour[i], tour[i + 1]);
            }
            this.repaint();
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
            int i, j;

            public edge(int ii, int jj) {
                i = ii;
                j = jj;
            }
        }

        // Add a node at pixel (x,y)
        public void addNode(String name, int x, int y) {
            nodes.add(new Node(name, x, y));
        }

        // Add an edge between nodes i and j
        public void addEdge(int i, int j) {
            edges.add(new edge(i, j));
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
                try {
                    g.drawLine(nodes.get(e.i).x * 2 + centerX, nodes.get(e.i).y * 2 + centerY, nodes.get(e.j).x * 2 + centerX, nodes.get(e.j).y * 2 + centerY);
                } catch (IndexOutOfBoundsException er){

                };
            }
            for (Node n : nodes) {
                int nodeWidth = Math.max(width, f.stringWidth(n.name) + width / 2);
                if(Integer.parseInt(n.name) == tour[0]){
                    g.setColor(Color.green);
                } else if (Integer.parseInt(n.name) == tour[106]){
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

    public static void main(String[] args) throws IOException {
        SimulatedAnnealing solver = new SimulatedAnnealing();
        // Read the cities from the input file
        BufferedReader br = new BufferedReader(new FileReader("TSP_107.txt"));
        String line;
        cities = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\s+");
            try {
                int id = Integer.parseInt(parts[0]);
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                cities.add(new City(id, x, y));
            } catch (NumberFormatException e){

            }

        }
        br.close();



        // Initialize the current tour and the best tour
        currentTour = new Tour(new ArrayList<>(cities));
        bestTour = currentTour;
        Tour final_tour = null;
        for(double temperature = 1000; temperature < 10001; temperature += 3000){
            for(int neighborhood = 100; neighborhood < 5001; neighborhood *= 2){
                for(double coolingRate = 0.949; coolingRate < 1; coolingRate += 0.01){
                    optimizeParameters(temperature,neighborhood,coolingRate);
                    if(final_tour == null){
                        final_tour = bestTour;
                    } else if(bestTour.cost < final_tour.cost) {
                        final_tour = bestTour;
                    }
                }
            }
        }

        int[][] coords = new int[cities.size()][cities.size()];
        int[] tour = new int[cities.size()];
        System.out.print("Final Tour: ");
        for(int i = 0; i < cities.size(); i++){
            coords[i][0] = (int) cities.get(i).x;
            coords[i][1] = (int) cities.get(i).y;
            tour[i] = bestTour.cities.get(i).id;
            System.out.print(tour[i] + " ");
        }

        SimulatedAnnealing.Visualizer graph = solver.new Visualizer(coords, tour);


    }




    public static void optimizeParameters(double temperature, int neighborhood, double coolingRate){
        double initial_temperature = temperature;
        // Run simulated annealing
        Random random = new Random();
        int generation = 0;
        long startTime = System.currentTimeMillis();

        while (temperature > 1.0) {
            for (int i = 0; i < neighborhood; i++) {
                // Generate a new tour by swapping two cities
                int x = random.nextInt(cities.size());
                int y = random.nextInt(cities.size());
                currentTour.swapCities(x, y);

                // Compute the cost of the new tour and decide whether to accept it
                double newCost = currentTour.computeCost();
                double deltaCost = newCost - currentTour.cost;
                if (deltaCost < 0 || Math.exp(-deltaCost / temperature) > random.nextDouble()) {
                    currentTour.cost = newCost;
                    if (currentTour.cost < bestTour.cost) {
                        bestTour = new Tour(new ArrayList<>(currentTour.cities));
                    }
                } else {
                    // Revert the swap
                    currentTour.swapCities(x, y);
                }

            }



            // Cool down the temperature
            temperature *= coolingRate;
            generation++;
        }

        long endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime) / 1000.0;
        System.out.println("This run was done with neighbourhood size of : " + neighborhood + ", initial temperature of " + initial_temperature + ", and cooling rate of " + coolingRate + ". It took " + totalTime +  " seconds long and had the best cost of " + bestTour.cost + ".");
    }
}