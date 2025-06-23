import java.io.*;
import java.util.*;

public class movieGraphClass {
    // Graph that represents Map of movies and actors
    private final Map<String, Set<String>> adjacencyListForMoviesAndActors;
    // Ratings map (non-static now)
    public static Map<String, Double> ratingsWithFilms;
    public movieGraphClass() {
        adjacencyListForMoviesAndActors = new HashMap<>();
        ratingsWithFilms = new HashMap<>();
    }

    // Edge Class
    public class Edge {
        private final String v1;
        private final String v2;
        private final String films;

        public Edge(String v1, String v2, String films) {
            this.v1 = v1;
            this.v2 = v2;
            this.films = films;
        }

        public String getV1() { return v1; }
        public String getV2() { return v2; }
        public String getFilms() { return films; }
    }

    public Map<String, List<Edge>> buildActorEdgeGraph() {
        Map<String, List<Edge>> actorGraph = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : adjacencyListForMoviesAndActors.entrySet()) {
            String movie = entry.getKey();
            List<String> actors = new ArrayList<>(entry.getValue());

            for (int i = 0; i < actors.size(); i++) {
                for (int j = i + 1; j < actors.size(); j++) {
                    String a1 = actors.get(i);
                    String a2 = actors.get(j);

                    actorGraph.putIfAbsent(a1, new ArrayList<>());
                    actorGraph.putIfAbsent(a2, new ArrayList<>());

                    actorGraph.get(a1).add(new Edge(a1, a2, movie));
                    actorGraph.get(a2).add(new Edge(a2, a1, movie));
                }
            }
        }

        return actorGraph;
    }

    public List<String> bfsGetPathMovies(String start, String end) {
	    Map<String, List<Edge>> actorGraph = buildActorEdgeGraph();
	    Map<String, Edge> howWeGotHere = new HashMap<>();
	    Queue<String> queue = new LinkedList<>();
	    Set<String> visited = new HashSet<>();

	    queue.add(start);
	    visited.add(start);

	    while (!queue.isEmpty()) {
	        String current = queue.poll();

	        for (Edge edge : actorGraph.getOrDefault(current, new ArrayList<>())) {
	            String neighbor = edge.getV2();

	            if (!visited.contains(neighbor)) {
	                visited.add(neighbor);
	                queue.add(neighbor);
	                howWeGotHere.put(neighbor, edge);

	                if (neighbor.equals(end)) {
	                    return extractMoviePath(start, end, howWeGotHere);
	                }
	            }
	        }
	    }

	    return new ArrayList<>(); // no path
	}

	private List<String> extractMoviePath(String start, String end, Map<String, Edge> edgeMap) {
	    List<String> movies = new ArrayList<>();
	    String current = end;

	    while (!current.equals(start)) {
	        Edge edge = edgeMap.get(current);
	        if (edge != null) {
	            movies.add(edge.getFilms());
	            current = edge.getV1();
	        } else {
	            break;
	        }
	    }

	    Collections.reverse(movies);
	    return movies;
	}
	//BigO(1) constant time, sorting overall takes O(nlogn)
	public void printSortedPathMoviesByRating(List<String> movieList) {
	    movieList.sort((a, b) -> {
	        double ratingA = ratingsWithFilms.getOrDefault(a, 0.0);
	        double ratingB = ratingsWithFilms.getOrDefault(b, 0.0);
	        return Double.compare(ratingB, ratingA);
	    });

	    System.out.println("\nMovies in connection path, sorted by rating:");
	    for (String movie : movieList) {
	        System.out.printf("%s (%.1f)\n", movie, ratingsWithFilms.getOrDefault(movie, 0.0));
	    }
	}


	//O(V+E) V is the number actors(nodes), E is the number of connections
    public String bfsFindPath(String start, String end) {
        if (start.equals(end)) return "Start and end actor are the same.";

        Map<String, List<Edge>> actorGraph = buildActorEdgeGraph();
        if (!actorGraph.containsKey(start) || !actorGraph.containsKey(end)) {
            return "One or both actors not in dataset.";
        }

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, Edge> howWeGotHere = new HashMap<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            for (Edge edge : actorGraph.get(current)) {
                String neighbor = edge.getV2();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    howWeGotHere.put(neighbor, edge);
                    queue.add(neighbor);

                    if (neighbor.equals(end)) {
                        return buildEdgePath(start, end, howWeGotHere);
                    }
                }
            }
        }

        return "No connection found.";
    }

    private String buildEdgePath(String start, String end, Map<String, Edge> pathMap) {
        LinkedList<String> output = new LinkedList<>();
        String current = end;

        output.addFirst("" + end);

        while (!current.equals(start)) {
            Edge edge = pathMap.get(current);
            current = edge.getV1();
            output.addFirst(" was in " + edge.getFilms() + " with ");
            output.addFirst(" " + current);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(start).append(" has a ").append(end).append(" number of ").append((output.size() - 1) / 2).append(".\n\n");
        for (String line : output) {
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    public void readJsonLikeFile() {
        String fileName = "jsonformatter.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            String currentMovie = null;
            Set<String> castSet = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("\"") && line.contains(": [")) {
                    int quoteEnd = line.indexOf("\":");
                    currentMovie = line.substring(1, quoteEnd);
                    castSet = new HashSet<>();
                    adjacencyListForMoviesAndActors.put(currentMovie, castSet);
                } else if (line.startsWith("\"")) {
                    if (currentMovie != null && castSet != null) {
                        String actor = line.replace("\"", "").replace(",", "").trim();
                        if (!actor.isEmpty()) {
                            castSet.add(actor);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public void readRatingsFromCSV() {
	    String fileName = "tmdb_5000_movies.csv";
	    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
	        String line;
	        br.readLine(); // Skip header

	        while ((line = br.readLine()) != null) {
	            String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Handles commas inside quotes

	            if (tokens.length < 2) continue;

	            String title = tokens[0].trim().replaceAll("^\"|\"$", "");
	            String ratingStr = tokens[1].trim();

	            try {
	                double rating = Double.parseDouble(ratingStr);
	                ratingsWithFilms.put(title, rating);
	            } catch (NumberFormatException e) {
	                System.out.println("Skipping bad rating for: " + title);
	            }
	        }
	    } catch (IOException e) {
	        System.out.println("Error reading CSV file: " + e.getMessage());
	    }
	}


    public static void printMoviesWithRatings() {
        for (Map.Entry<String, Double> entry : ratingsWithFilms.entrySet()) {
            String movieName = entry.getKey();
            Double rating = entry.getValue();
            System.out.println(movieName + " --> " + rating);
        }
    }

    public static void main(String[] args) {
        movieGraphClass graph = new movieGraphClass();
        graph.readJsonLikeFile();
        graph.readRatingsFromCSV();
        //printMoviesWithRatings();
        Scanner sc = new Scanner(System.in);

		System.out.print("Enter actor 1: ");
		String actor1 = sc.nextLine();

		System.out.print("Enter actor 2: ");
		String actor2 = sc.nextLine();

		System.out.println(graph.bfsFindPath(actor1, actor2));

		System.out.print("\nSort films from this path by rating? Y/N: ");
		if (sc.nextLine().trim().equalsIgnoreCase("y")) {
		    List<String> pathMovies = graph.bfsGetPathMovies(actor1, actor2);
		    graph.printSortedPathMoviesByRating(pathMovies);
		}
    }
}
