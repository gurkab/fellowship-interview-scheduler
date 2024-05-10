package com.gurkab.fellowshipinterviewscheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DateAssigner {

    private static final int MAX_V = 1000; // Adjust as needed
    private int[][] capacity; // Capacity matrix
    private List<List<Integer>> graph; // Adjacency list representation
    private int[] parent; // For storing the path found by BFS
    private final Map<Integer, Program> indexToProgram; // Reverse mapping from index to Program
    private final Map<LocalDate, Integer> dateToIndex; // Mapping from LocalDate to index
    private final Set<LocalDate> assignedDates;
    private final ObjectMapper objectMapper;

    public DateAssigner() {
        this.graph = new ArrayList<>(MAX_V);
        for (int i = 0; i < MAX_V; i++) {
            this.graph.add(new ArrayList<>());
        }
        this.capacity = new int[MAX_V][MAX_V];
        this.parent = new int[MAX_V];
        this.indexToProgram = new HashMap<>();
        this.dateToIndex = new HashMap<>();
        this.assignedDates = new HashSet<>();
        this.objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
        objectMapper.registerModule(module);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // Method to construct the graph and solve the matching problem
    public void assignDates(List<Program> programs) {
        // Build and print a 'date to program map' to show all available dates
        printDateToProgramMap(buildDateToProgramMap(programs));

        int programIndexStart = 1; // Assuming 0 is the source node index
        int dateIndex = programIndexStart + programs.size(); // Dates start after all programs
        int source = 0;

        // Reset or Initialize mappings and structures with the correct size
        this.capacity = new int[MAX_V][MAX_V];
        this.graph = new ArrayList<>(Collections.nCopies(MAX_V, new ArrayList<>()));
        this.parent = new int[MAX_V];
        this.indexToProgram.clear();
        this.dateToIndex.clear();
        this.assignedDates.clear();

        for (Program program : programs) {
            int programNode = programIndexStart++;
            indexToProgram.put(programNode, program);

            for (LocalDate date : program.getAvailableDates()) {
                if (!this.dateToIndex.containsKey(date)) { // Only add new date indices for unique dates
                    this.dateToIndex.put(date, dateIndex);
                    dateIndex++; // Increment only for new unique dates
                }
                int dateNode = this.dateToIndex.get(date);
                addEdge(programNode, dateNode); // Add edge from program to date
            }
            addEdge(source, programNode); // Add edge from source to program
        }

        int sink = dateIndex; // Adjust sink index based on the actual number of unique dates added
        for (int i = programIndexStart; i < sink; i++) { // Connect all date nodes to sink
            addEdge(i, sink);
        }

        // Apply the Ford-Fulkerson algorithm
        fordFulkerson(source, sink);

        // After Ford-Fulkerson, update assigned dates
        updateProgramsWithAssignedDates();

        // Add remaining dates as secondary options
        addAdditionalDates(programs);

        // Print our results
        printResults(programs);
    }

    // Method to add edges to the graph
    private void addEdge(int from, int to) {
        graph.get(from).add(to);
        graph.get(to).add(from); // Reverse edge for Ford-Fulkerson
        capacity[from][to] = 1;
    }

    // Ford-Fulkerson method to find maximum flow
    private void fordFulkerson(int s, int t) {
        while (bfs(s, t)) {
            int pathFlow = Integer.MAX_VALUE;
            for (int v = t; v != s; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, capacity[u][v]);
            }
            for (int v = t; v != s; v = parent[v]) {
                int u = parent[v];
                capacity[u][v] -= pathFlow;
                capacity[v][u] += pathFlow;
            }
        }
    }

    // BFS to find path from source to sink
    private boolean bfs(int s, int t) {
        Arrays.fill(parent, -1);
        Queue<Integer> queue = new LinkedList<>();
        queue.add(s);
        parent[s] = -2;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : graph.get(u)) {
                if (parent[v] == -1 && capacity[u][v] > 0) {
                    parent[v] = u;
                    if (v == t) return true; // Path found
                    queue.add(v);
                }
            }
        }
        return false; // No path found
    }

    private void updateProgramsWithAssignedDates() {
        for (Map.Entry<Integer, Program> entry : indexToProgram.entrySet()) {
            int programNode = entry.getKey();
            Program program = entry.getValue();

            for (LocalDate date : program.getAvailableDates()) {
                int dateNode = dateToIndex.get(date); // Get the node index of this date
                // Check if this date was actually assigned based on residual capacity
                if (capacity[programNode][dateNode] == 0) {
                    program.setAssignedDate(date);
                    assignedDates.add(date);
                }
            }
        }
    }

    private void addAdditionalDates(List<Program> programs) {
        for (Program program : programs) {
            for (LocalDate date : program.getAvailableDates()) {
                if (!assignedDates.contains(date)) {
                    program.addSecondaryDate(date);
                }
            }
        }
    }

    private void printResults(List<Program> programs) {
        System.out.println("Algorithm results:");
        try {
            System.out.println(objectMapper.writeValueAsString(programs));
        } catch (JsonProcessingException exception) {
            System.out.println(exception.getMessage());
        }
        programs
            .stream()
            .filter(program -> program.getAssignedDate() == null)
            .forEach(program -> System.out.println(program.getProgramName() + " has no assigned date"));
    }

    private Map<LocalDate, List<String>> buildDateToProgramMap(List<Program> programs) {
        Map<LocalDate, List<String>> dateToProgramMap = new TreeMap<>();
        for (Program program : programs) {
            List<LocalDate> availableDates = program.getAvailableDates();
            for (LocalDate date : availableDates) {
                dateToProgramMap.computeIfAbsent(date, k -> new ArrayList<>()).add(program.getProgramName());
            }
        }
        return dateToProgramMap;
    }

    private void printDateToProgramMap(Map<LocalDate, List<String>> dateToProgramMap) {
        System.out.println("Date to programs:");
        try {
            System.out.println(objectMapper.writeValueAsString(dateToProgramMap));
        } catch (JsonProcessingException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
