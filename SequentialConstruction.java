import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.List;

public class SequentialConstruction
{
    public static int populationSize = 107; // size of chromosomesPopulation
    public static int numOfGenerations = 2000; // number of iterations
    public static int tourSize = 6; // number of cities to be selected for tournament selection
    public static double mutationRate = 0.01; // rate of offspring mutation

    /**
     * This method reads the file containing the data where,
     * the first column is the city node, the second column is the x-coordinate, the third column is the y-coordinate,
     * and adds it into the cities array list of type 'int[]'.
     *
     * @param fileName // the name of the file currently being read
     */



    public static int[][] readTSPDataFile(String fileName) throws IOException {
        BufferedReader dataFile = new BufferedReader(new FileReader(fileName));

        List<int[]> cities = new ArrayList<>();
        String line;

        while (!Objects.equals(line = dataFile.readLine(), "EOF")) {
            String[] column = line.trim().split("\\s+");

            int cityNode = Integer.parseInt(column[0]);
            int xCoordinate = Integer.parseInt(column[1]);
            int yCoordinate = Integer.parseInt(column[2]);
            cities.add(new int[]{cityNode, xCoordinate, yCoordinate});
        }

        dataFile.close();

        int[][] data = new int[cities.size()][3];

        for (int i = 0; i < cities.size(); i++) {
            data[i] = cities.get(i);
        }

        return data;
    }

    /**
     * This method initializes a population with each city having a chromosome.
     * This method also randomizes the chromosome of each city.
     *
     * @param populationSize the size of the population
     * @param numCities      the number of cities in the population
     * @return the initialized population
     */
    public static int[][] populationInitialization(int populationSize, int numCities) {
        int[][] population = new int[populationSize][numCities];

        for (int i = 0; i < populationSize; i++) {
            List<Integer> cities = new ArrayList<>();

            for (int j = 1; j <= numCities; j++) {
                cities.add(j);
            }

            Collections.shuffle(cities);

            for (int k = 0; k < numCities; k++) {
                population[i][k] = cities.get(k);
            }
        }

        return population;
    }

    /**
     * This method calculates the fitness of the tour.
     *
     * @param tour the tours being used for the evaluation
     * @param data the data used from the text file
     * @return the fitness of the tour
     */

    public static double fitnessEvaluation(int[] tour, int[][] data){
        return 1/distanceEvaluation(tour, data);
    }
    public static double distanceEvaluation(int[] tour, int[][] data) {
        double totalDistance = 0;

        for (int i = 0; i < tour.length - 1; i++) {
            totalDistance = totalDistance + distanceBetweenCities(data[tour[i] - 1], data[tour[i + 1] - 1]);
        }

        // adding distance to the city
        totalDistance = totalDistance + distanceBetweenCities(data[tour[tour.length - 1] - 1], data[tour[0] - 1]);

        // maximize fitness
        return totalDistance;
    }

    /**
     * This method calculates distance between two cities using the Euclidean Distance formula.
     *
     * @param city1 the first city
     * @param city2 the second city
     * @return the distance between the first and second city
     */
    public static double distanceBetweenCities(int[] city1, int[] city2) {
        int dXCoordinate = city1[1] - city2[1];
        int dYCoordinate = city1[2] - city2[2];

        return Math.sqrt((dXCoordinate * dXCoordinate) + (dYCoordinate * dYCoordinate));
    }

    /**
     * This method finds the best tour in the population.
     *
     * @param population coordinates of the cities
     * @param fitnessValues the value of the fitness based on the index of the cities in the array
     * @return the array index of the city with the best fitness
     */
    private static int[] findBestTour(int[][] population, double[] fitnessValues) {
        int bestIndex = 0;
        double bestFitness = fitnessValues[0];

        for (int i = 1; i < population.length; i++) {
            double fitness = fitnessValues[i];
            if (fitness > bestFitness) {
                bestIndex = i;
                bestFitness = fitness;
            }
        }

        return population[bestIndex];
    }

    /**
     * This method selects the parents via the Tournament Selection method.
     * This is also known as REPRODUCTION.
     *
     * @param population coordinates of the cities
     * @param fitnessValues the value of the fitness based on the index of the cities in the array
     * @return the array index of the city with the best fitness
     */
    public static int[] parentTournamentSelection(int[][] population, double[] fitnessValues) {
        int[] allTours = new int[tourSize];

        for (int i = 0; i < tourSize; i++) {
            allTours[i] = (int) (Math.random() * population.length);
        }

        int bestTourIndex = 0;
        double bestFitness = fitnessValues[allTours[0]];

        for (int i = 1; i < tourSize; i++) {
            double fitness = fitnessValues[allTours[i]];

            if (fitness > bestFitness) {
                bestTourIndex = i;
                bestFitness = fitness;
            }
        }

        return population[bestTourIndex];
    }

    /**
     * This method applies the sequential constructive crossover operator to the selected parents.
     * This is also known as RECOMBINATION.
     *
     * @param parent1 the first parent selected
     * @param parent2 the second parent selected
     * @param data the data obtained from the text file
     * @return the offspring of parent1 and parent2
     */
    public static int[] SequentialConstructiveCrossover(int[] parent1, int[] parent2, int[][] data) {
        int currentIndex = 0;
        int[] child = new int[parent1.length];
        Set<Integer> visited = new HashSet<>();

        visited.add(parent1[currentIndex]);
        child[currentIndex] = parent1[currentIndex];
        currentIndex++;

        while (visited.size() < parent1.length) {
            int tempIndex = currentIndex;
            int nextCity1 = parent1[(currentIndex) % parent1.length];
            int nextCity2 = parent2[(currentIndex) % parent2.length];
            //If both cities visited find unvisted city
            if (visited.contains(nextCity1) && visited.contains(nextCity2)) {
                while (visited.contains(nextCity1) && visited.contains(nextCity2)){
                    tempIndex++;
                    nextCity1 = parent1[(tempIndex) % parent1.length];
                    nextCity2 = parent2[(tempIndex % parent2.length)];
                }
            }
            if (visited.contains(nextCity1)) {
                // if only the first city is visited, choose the second city
                visited.add(nextCity2);
                child[currentIndex] = nextCity2;
                currentIndex = (currentIndex + 1) % parent1.length;

            } else if (visited.contains(nextCity2)) {
                // if only the second city is visited, choose the first city
                visited.add(nextCity1);
                child[currentIndex] = nextCity1;
                currentIndex = (currentIndex + 1) % parent1.length;

            } else {

                // if both cities are unvisited, choose the one with shorter distance
                double distance1 = distanceBetweenCities(data[parent1[currentIndex] - 1], data[nextCity1 - 1]);
                double distance2 = distanceBetweenCities(data[parent1[currentIndex] - 1], data[nextCity2 - 1]);

                if (distance1 < distance2) {
                    visited.add(nextCity1);
                    child[currentIndex] = nextCity1;
                } else {
                    visited.add(nextCity2);
                    child[currentIndex] = nextCity2;
                }
                currentIndex = (currentIndex + 1) % parent1.length;
            }
        }
        //       }

        return child;
    }

    /**
     * This method swaps the offspring with their parents in the eventual new population.
     *
     * @param tour the tours being used for the evaluation
     */
    public static void mutate(int[] tour) {
        for (int i = 0; i < tour.length; i++) {
            if (Math.random() < mutationRate) {
                int j = (int) (tour.length * Math.random());
                int temp = tour[i];
                tour[i] = tour[j];
                tour[j] = temp;
            }
        }
    }

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

    /**
     * This method is the main method which executes the Genetic Algorithm with Sequential Constructive Crossover
     * to optimize the Travelling Salesman Problem.
     */
    public static void main(String[] args) throws IOException {
        int currentGeneration = 0;
        double overallShortestRoute = 0;
        int[] overallBestTour = {};

        // reading city data from the text file provided
        String textFile = "TSP_107.txt";
        int[][] data = readTSPDataFile(textFile);
        int numOfCities = data.length;

        long startTime = System.currentTimeMillis();

        // initializing the population
        int[][] population = populationInitialization(populationSize, numOfCities);


        // termination criteria; the loop of the number of generations
        while (currentGeneration < numOfGenerations) {
            // calculating the fitness of each tour
            double[] fitnessValues = new double[populationSize];

            // iteration through the entire population
            for (int i = 0; i < populationSize; i++) {
                int[] tour = population[i];
                double fitness = fitnessEvaluation(tour, data);
                fitnessValues[i] = 1.0 / fitness;
            }

            // finding the best tour in the population
            int[] bestTour = findBestTour(population, fitnessValues);
            double shortestRoute = fitnessEvaluation(bestTour, data);
            double shortestDistance = distanceEvaluation(bestTour, data);

            if (shortestRoute > overallShortestRoute) {
                overallBestTour = bestTour;
                overallShortestRoute = shortestRoute;
            }

            // print statements displaying the shortest possible route for the generation
            System.out.println("Generation: " + currentGeneration);
            System.out.println("Shortest Possible Route: " + shortestDistance);

            // creating the next generation
            int[][] nextPopulation = new int[populationSize][numOfCities];

            for (int i = 0; i < populationSize; i++) {
                // selecting the two parents using Tournament Selection (REPRODUCTION)
                int[] parent1 = parentTournamentSelection(population, fitnessValues);
                int[] parent2 = parentTournamentSelection(population, fitnessValues);

                // performing the Sequential Constructive Crossover (RECOMBINATION)
                int[] offspring = SequentialConstructiveCrossover(parent1, parent2, data);

                // mutating/swapping the offspring with its parents in the eventual new population
                mutate(offspring);

                // adding the offspring to the new population for the next generation
                nextPopulation[i] = offspring;
            }

            // replacing the previous population with the new population consisting of the offspring rather than the parents
            currentGeneration++;
            population = nextPopulation;

        }


        double overallShortestDistance = distanceEvaluation(overallBestTour, data);

        long endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime) / 1000.0;

        System.out.println("The shortest route was: " + overallShortestDistance);
        System.out.println("The total time was: " + totalTime + " seconds.");
        int[][] coordinates = new int[107][2];
        for(int i = 0; i < 107; i++){
            coordinates[i][0] = data[i][1];
            coordinates[i][1] = data[i][2];
        }

        for(int i = 0; i < 107; i++){
            System.out.print(overallBestTour[i] + " ");
        }



        SequentialConstruction solver = new SequentialConstruction();
        SequentialConstruction.Visualizer graph = solver.new Visualizer(coordinates,overallBestTour);

    }
}